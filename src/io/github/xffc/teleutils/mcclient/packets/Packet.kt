package io.github.xffc.teleutils.mcclient.packets

import io.github.xffc.teleutils.mcclient.State
import io.github.xffc.teleutils.mcclient.packets.clientbound.ClientboundPacketInfo
import io.github.xffc.teleutils.mcclient.packets.serverbound.ServerboundPacketInfo
import kotlinx.io.Buffer
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance

interface Packet {
    fun info() =
        this::class.companionObjectInstance as PacketInfo<*>

    fun write(buffer: Buffer): Unit =
        error("Wrong side of packet! Can't write packet $this")

    interface PacketInfo<T: Packet> {
        val id: Int

        val state: State

        fun from(buffer: Buffer): T =
            error("Wrong side of network! Can't read packet $this")
    }

    companion object {
        fun <T: PacketInfo<*>> KClass<T>.registry(): Map<State, Map<Int, T>> = State.entries.associate { state ->
            state to buildMap {
                sealedSubclasses.forEach { kClass ->
                    val instance = kClass.objectInstance ?: return@forEach
                    put(instance.id, instance)
                }
            }
        }

        val clientboundRegistry = ClientboundPacketInfo::class.registry()
        val serverboundRegistry = ServerboundPacketInfo::class.registry()
    }
}