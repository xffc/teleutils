package io.github.xffc.teleutils.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed interface Command {
    companion object {
        val registry = Command::class.sealedSubclasses.map { it.objectInstance!! }
    }

    val name: String
    val arguments: List<Argument<*>>

    suspend fun Context.execute()

    suspend fun BehaviourContext.register() =
        onCommandWithArgs(name) { message, args ->
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    Context
                        .build(this@Command, message, args.toList())
                        .execute()
                } catch (e: Exception) {
                    bot.reply(
                        message.chat.id,
                        message.messageId,
                        "❌ ${e.message}",
                        parseMode = HTMLParseMode
                    )
                }
            }
        }

    class Context private constructor(
        val message: CommonMessage<TextContent>,
        val args: Map<String, Any?>
    ) {
        companion object {
            suspend fun build(command: Command, message: CommonMessage<TextContent>, args: List<String>): Context {
                val arguments = command.arguments.associate {
                    val position = command.arguments.indexOf(it)
                    val arg = args.getOrNull(position)

                    if (it.required && arg == null && it.default != null)
                        throw IllegalArgumentException("Argument ${it.name} with position ${position + 1} is required, but not present")

                    it.name to (arg?.let { arg -> it.from(arg) } ?: it.default)
                }

                return Context(
                    message,
                    arguments
                )
            }
        }
    }
}