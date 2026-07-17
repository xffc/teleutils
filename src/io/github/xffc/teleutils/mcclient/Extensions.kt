package io.github.xffc.teleutils.mcclient

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.io.writeString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.io.InputStream

val String.tryComponent get() =
    try {
        GsonComponentSerializer.gson().deserialize(this)
    } catch (_: Exception) {
        Component.text(this)
    }

fun Sink.writeVarInt(value: Int) {
    var value = value

    do {
        val byte = (value and 0x7F) or if (value > 0x7F) 0x80 else 0
        writeByte(byte.toByte())
        value = value ushr 7
    } while (value != 0)
}

fun Source.readVarInt(): Int {
    var value = 0
    var shift = 0
    var byte: Int

    do {
        byte = readByte().toInt()
        value = value or ((byte and 0x7F) shl shift)
        shift += 7
    } while (byte and 0x80 != 0)

    return value
}

fun Sink.writePrefixedString(value: String) {
    writeVarInt(value.length)
    writeString(value)
}

fun Source.readPrefixedString(): String =
    readString(readVarInt().toLong())

fun InputStream.readVarInt(): Int? {
    var value = 0
    var shift = 0
    var byte: Int

    do {
        byte = read()
        if (byte == -1) return null

        value = value or ((byte and 0x7F) shl shift)
        shift += 7
    } while (byte and 0x80 != 0)

    return value
}