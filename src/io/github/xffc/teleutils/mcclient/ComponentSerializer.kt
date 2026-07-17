package io.github.xffc.teleutils.mcclient

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.kyori.adventure.text.Component

typealias SerializableComponent = @Serializable(with = ComponentSerializer::class) Component

object ComponentSerializer : KSerializer<Component> {
    override val descriptor = buildClassSerialDescriptor(Component::class.qualifiedName!!)

    override fun serialize(encoder: Encoder, value: Component) =
        throw NotImplementedError()

    override fun deserialize(decoder: Decoder): Component =
        when (val element = (decoder as? JsonDecoder)?.decodeJsonElement() ?: error("Not a JSON decoder")) {
            is JsonPrimitive if element.isString -> element.content.tryComponent

            is JsonObject -> element.toString().tryComponent

            else -> error("Unexpected json element ${element::class.simpleName}")
        }
}