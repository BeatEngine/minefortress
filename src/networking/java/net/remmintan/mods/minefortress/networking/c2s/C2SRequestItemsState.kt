package net.remmintan.mods.minefortress.networking.c2s

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.utils.ServerModUtils

@Suppress("UnstableApiUsage")
class C2SRequestItemsState(private val items: Set<ItemVariant>) : FortressC2SPacket {

    constructor(buf: PacketByteBuf) : this(buf.readCollection({ _ -> mutableSetOf<ItemVariant>() }) { b ->
        ItemVariant.fromPacket(
            b
        )
    })

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        ServerModUtils.getManagersProvider(player).orElseThrow().resourceHelper.syncRequestedItems(items, player)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeCollection(items) { b, i -> i.toPacket(b) }
    }

    companion object {
        const val CHANNEL = "fortress_request_items_state"
    }

}