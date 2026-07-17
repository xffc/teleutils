package io.github.xffc.teleutils.mcclient.packets.serverbound

import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.Packet
import io.github.xffc.teleutils.mcclient.writePrefixedString
import io.github.xffc.teleutils.mcclient.writeVarInt
import kotlinx.io.Buffer
import kotlinx.io.writeUShort

class HandshakePacket(
    val protocolVersion: Int,
    val serverAddress: String,
    val serverPort: UShort,
    val intent: State
): Packet {
    override fun write(buffer: Buffer) {
        buffer.writeVarInt(protocolVersion)
        buffer.writePrefixedString(serverAddress)
        buffer.writeUShort(serverPort)
        buffer.writeVarInt(intent.ordinal)
    }

    companion object: ServerboundPacketInfo<HandshakePacket> {
        override val id = 0
        override val state = State.HANDSHAKE
    }
}

