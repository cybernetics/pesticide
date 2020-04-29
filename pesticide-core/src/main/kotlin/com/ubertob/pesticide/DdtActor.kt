package com.ubertob.pesticide

import java.io.File
import java.net.URI
import java.util.function.Consumer


data class DdtStep<D : DomainUnderTest<*>>(val description: String, val action: (D) -> D) {

    val stackTraceElement = Thread.currentThread().stackTrace[5]

    fun testSourceURI(): URI? =
        stackTraceElement?.toSourceReference(sourceRoot) //TODO can we guess better the sourceRoot?

    fun StackTraceElement.toSourceReference(sourceRoot: File): URI? {
        val fileName = fileName ?: return null
        val type = Class.forName(className)

        val pathpesticide =
            sourceRoot.toPath().resolve(type.`package`.name.replace(".", "/")).resolve(fileName).toFile().absolutePath

        return URI("file://$pathpesticide?line=$lineNumber")

    }

}

private val sourceRoot = listOf(
    File("src/test/kotlin"),
    File("src/test/java")
).find { it.isDirectory } ?: File(".")

abstract class DdtActor<D : DomainUnderTest<*>> {

    abstract val name: String

    private fun getCurrentMethodName() =
        Thread.currentThread().stackTrace[3].methodName //TODO needs a better way to find the exact stack trace relevant instead of just 3...

    fun generateStepName(block: D.() -> Unit): DdtStep<D> =
        step(getCurrentMethodName(), block)

    fun generateStepName(vararg parameters: Any, block: D.() -> Unit): DdtStep<D> =
        step(getCurrentMethodName().replaceDollars(parameters), block)

    fun step(stepDesc: String, block: D.() -> Unit): DdtStep<D> =
        DdtStep(stepDesc) { it.also(block) }


    //mainly for Java use
    fun step(stepDesc: String, block: Consumer<D>): DdtStep<D> =
        step(stepDesc, block::accept)

}

private fun String.replaceDollars(parameters: Array<out Any>): String = parameters
    .map(Any::toString)
    .fold(this) { text, param ->
        text.replaceFirst("$", param)
    }
