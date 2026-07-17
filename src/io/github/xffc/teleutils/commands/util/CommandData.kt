package io.github.xffc.teleutils.commands.util

import io.github.xffc.teleutils.commands.ExecutableCommand

data class CommandData(
    val instance: ExecutableCommand,
    val arguments: List<Argument<*>>,
    val aliases: List<String>?,
    val isAlias: Boolean
)