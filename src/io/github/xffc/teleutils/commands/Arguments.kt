package io.github.xffc.teleutils.commands

sealed interface Argument<T : Any> {
    val name: String
    val default: T?
    val required: Boolean

    suspend fun from(arg: String): T

    data class TextArgument(
        override val name: String,
        override val required: Boolean,
        override val default: String? = null
    ): Argument<String> {
        override suspend fun from(arg: String): String = arg
    }

    data class IntArgument(
        override val name: String,
        override val required: Boolean,
        override val default: Int? = null
    ): Argument<Int> {
        override suspend fun from(arg: String): Int =
            arg.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid number argument: $name")
    }

    data class PortArgument(
        override val name: String,
        override val required: Boolean,
        override val default: UShort? = null
    ): Argument<UShort> {
        override suspend fun from(arg: String): UShort =
            arg.toUShortOrNull()
                ?: throw IllegalArgumentException("Invalid port argument: $name")
    }
}