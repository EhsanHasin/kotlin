/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jvm.abi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.incremental.LocalFileKotlinClass
import org.jetbrains.kotlin.incremental.isClassFile
import org.jetbrains.kotlin.jvm.abi.asm.AbiClassBuilder
import org.jetbrains.kotlin.jvm.abi.asm.AbiExtensionAsmApiVersion
import org.jetbrains.kotlin.jvm.abi.asm.FilterInnerClassesVisitor
import org.jetbrains.kotlin.jvm.abi.asm.InnerClassesCollectingVisitor
import org.jetbrains.kotlin.load.kotlin.FileBasedKotlinClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import org.jetbrains.org.objectweb.asm.*
import java.io.File
import java.util.*

class JvmAbiAnalysisHandlerExtension(
    private val compilerConfiguration: CompilerConfiguration
) : AnalysisHandlerExtension {
    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        val bindingContext = bindingTrace.bindingContext
        if (bindingContext.diagnostics.any { it.severity == Severity.ERROR }) return null

        val targetId = TargetId(
            name = compilerConfiguration[CommonConfigurationKeys.MODULE_NAME] ?: module.name.asString(),
            type = "java-production"
        )

        val generationState = GenerationState.Builder(
            project,
            AbiBinaries,
            module,
            bindingContext,
            files.toList(),
            compilerConfiguration
        ).targetId(targetId).build()
        KotlinCodegenFacade.compileCorrectFiles(generationState, CompilationErrorHandler.THROW_EXCEPTION)

        val outputDir = compilerConfiguration.get(JVMConfigurationKeys.OUTPUT_DIRECTORY)!!
        val outputs = ArrayList<AbiOutput>()

        for (outputFile in generationState.factory.asList()) {
            val file = File(outputDir, outputFile.relativePath)
            outputs.add(AbiOutput(file, outputFile.sourceFiles, outputFile.asByteArray()))
        }

        removeUnneededClasses(outputs)
        outputs.forEach { it.flush() }
        return null
    }

    /**
     * Removes private or local classes from outputs
     */
    private fun removeUnneededClasses(outputs: Iterable<AbiOutput>) {
        // maps internal names of classes: class -> inner classes
        val innerClasses = HashMap<String, Collection<String>>()
        val internalNameToFile = HashMap<String, File>()

        for (output in outputs) {
            if (!output.file.isClassFile()) continue

            val visitor = InnerClassesCollectingVisitor()
            output.accept(visitor)
            val outputInternalName = visitor.ownInternalName!!
            internalNameToFile[outputInternalName] = output.file
            innerClasses[outputInternalName] = visitor.innerClasses
        }

        // internal names of removed files
        val classesToRemoveQueue = ArrayDeque<String>()
        for (output in outputs) {
            if (!output.file.isClassFile()) continue

            val classData = output.classData() ?: continue
            val header = classData.classHeader
            val isNeededForAbi = when (header.kind) {
                KotlinClassHeader.Kind.CLASS -> {
                    val (_, classProto) = JvmProtoBufUtil.readClassDataFrom(header.data!!, header.strings!!)
                    val visibility = Flags.VISIBILITY.get(classProto.flags)
                    visibility != ProtoBuf.Visibility.PRIVATE && visibility != ProtoBuf.Visibility.LOCAL
                }
                KotlinClassHeader.Kind.SYNTHETIC_CLASS -> false
                else -> true
            }

            if (!isNeededForAbi) {
                val jvmClassName = JvmClassName.byClassId(classData.classId)
                classesToRemoveQueue.add(jvmClassName.internalName)
            }
        }

        // we can remove inner classes of removed classes
        val classesToRemove = HashSet<String>()
        classesToRemove.addAll(classesToRemoveQueue)
        while (classesToRemoveQueue.isNotEmpty()) {
            val classToRemove = classesToRemoveQueue.removeFirst()
            innerClasses[classToRemove]?.forEach {
                if (classesToRemove.add(it)) {
                    classesToRemoveQueue.add(it)
                }
            }
        }

        val classFilesToRemove = classesToRemove.mapTo(HashSet()) { internalNameToFile[it] }
        for (output in outputs) {
            if (!output.file.isClassFile()) continue

            if (output.file in classFilesToRemove) {
                output.delete()
            } else {
                output.transform { writer ->
                    FilterInnerClassesVisitor(classesToRemove, AbiExtensionAsmApiVersion, writer)
                }
            }
        }
    }

    private object AbiBinaries : ClassBuilderFactory {
        override fun getClassBuilderMode(): ClassBuilderMode =
            ClassBuilderMode.ABI

        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder =
            AbiClassBuilder(ClassWriter(0))

        override fun asText(builder: ClassBuilder): String =
            throw UnsupportedOperationException("AbiBinaries generator asked for text")

        override fun asBytes(builder: ClassBuilder): ByteArray {
            val visitor = builder.visitor as ClassWriter
            return visitor.toByteArray()
        }

        override fun close() {}
    }

    private data class ClassData(
        val classId: ClassId,
        val classVersion: Int,
        val classHeader: KotlinClassHeader
    )

    private class AbiOutput(
        val file: File,
        // todo report
        val sources: List<File>,
        // null bytes means that file should not be written
        private var bytes: ByteArray?
    ) {
        fun classData(): ClassData? =
            when {
                bytes == null -> null
                !file.isClassFile() -> null
                else -> FileBasedKotlinClass.create(bytes!!) { classId, classVersion, classHeader, _ ->
                    ClassData(classId, classVersion, classHeader)
                }
            }

        fun delete() {
            bytes = null
        }

        fun transform(fn: (writer: ClassWriter) -> ClassVisitor) {
            val bytes = bytes ?: return
            val cr = ClassReader(bytes)
            val cw = ClassWriter(0)
            val visitor = fn(cw)
            cr.accept(visitor, 0)
            this.bytes = cw.toByteArray()
        }

        fun accept(visitor: ClassVisitor) {
            val bytes = bytes ?: return
            val cr = ClassReader(bytes)
            cr.accept(visitor, 0)
        }

        fun flush() {
            bytes?.let { FileUtil.writeToFile(file, it) }
        }
    }
}