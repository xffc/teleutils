package io.github.xffc.teleutils.commands

import com.pengrad.telegrambot.model.Update
import io.github.xffc.teleutils.commands.util.Argument
import io.github.xffc.teleutils.commands.util.CommandData

sealed class ExecutableCommand(
    val name: String,
    vararg val aliases: String
) {
    abstract fun run(update: Update, args: List<String>)

    companion object {
        val registry: Map<String, CommandData> = buildMap {
            ExecutableCommand::class.sealedSubclasses.forEach { kClass ->
                val instance = kClass.objectInstance ?: return@forEach

                val arguments = kClass.java.declaredFields
                    .mapNotNull {
                        it.trySetAccessible()
                        it.get(instance) as? Argument<*>
                    }

                put(instance.name, CommandData(instance, arguments, instance.aliases.toList(), false))
                instance.aliases.forEach { alias -> put(alias, CommandData(instance, arguments, null, true)) }
            }
        }
    }
}

