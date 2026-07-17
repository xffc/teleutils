package io.github.xffc.teleutils.commands.util

sealed interface Argument<T> {
    val name: String
    val required: Boolean
    val default: T?

    fun fromString(value: String): T

    fun getValue(value: String?): T? {
        if (value == null) {
            if (required) throw IllegalArgumentException("Argument $name is required, but not present")
            return default
        }

        return fromString(value)
    }

    data class WordArgument(
        override val name: String,
        override val required: Boolean = true,
        override val default: String? = null
    ): Argument<String> {
        override fun fromString(value: String) = value
    }

    data class IntegerArgument(
        override val name: String,
        val bounds: IntRange? = null,
        override val required: Boolean = false,
        override val default: Int? = null,
    ): Argument<Int> {
        override fun fromString(value: String): Int {
            val int = value.toIntOrNull()
            requireNotNull(int) { "Argument $name should be an integer, but failed to parse" }
            if (bounds != null) require(int in bounds) { "Argument $name should be in bounds ${bounds.first} until ${bounds.last}" }
            return int
        }
    }
}