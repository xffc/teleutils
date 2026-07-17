package io.github.xffc.teleutils.mcclient

import io.github.xffc.teleutils.logger
import io.github.xffc.teleutils.mcclient.packets.Packet
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import java.net.InetSocketAddress
import java.net.Socket

class Client private constructor(
    private val socket: Socket
): Runnable {
    private val inputStream = socket.getInputStream()
    private val outputStream = socket.getOutputStream()

    var state = State.HANDSHAKE

    val packetHandlers: MutableList<(Client, Packet) -> Unit> = mutableListOf()

    fun sendPacket(packet: Packet, flush: Boolean = true) {
        val packetBuffer = Buffer()
        packetBuffer.writeVarInt(packet.info().id)
        packet.write(packetBuffer)

        val outputBuffer = Buffer()
        outputBuffer.writeVarInt(packetBuffer.size.toInt())
        outputBuffer.write(packetBuffer.readByteArray())

        outputStream.write(outputBuffer.readByteArray())
        if (flush) outputStream.flush()
    }

    override fun run() {
        startListening()
        disconnect()
    }

    private fun startListening() {
        try {
            while (!socket.isClosed) {
                val length = inputStream.readVarInt() ?: break

                val packetBytes = inputStream.readNBytes(length)
                if (packetBytes.size < length) break

                val packetBuffer = Buffer()
                packetBuffer.write(packetBytes)

                val packetId = packetBuffer.readVarInt()

                handleBuffer(packetId, packetBuffer)
            }
        } catch (e: Exception) {
            logger.error("Client connection raised an error", e)
        }
    }

    private fun handleBuffer(packetId: Int, packetBuffer: Buffer) {
        val packet = Packet.clientboundRegistry.getValue(state)[packetId]?.from(packetBuffer)
            ?: return logger.warn("Unknown packet $packetId")

        packetHandlers.forEach { it(this, packet) }
    }

    fun disconnect() {
        socket.close()
    }

    companion object {
        fun connect(host: String, port: Int): Client {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port))

            val client = Client(socket)
            Thread.startVirtualThread(client)

            return client
        }
    }
}