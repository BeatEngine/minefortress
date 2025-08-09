package net.remmintan.mods.minefortress.core.dtos.professions

import net.minecraft.item.Item
import net.minecraft.network.PacketByteBuf

data class ProfessionCost(val item: Item, val requiredAmount: Long, val totalAmount: Long) {

    val enoughItems: Boolean = totalAmount >= requiredAmount

    fun writeToPacketByteBuf(buf: PacketByteBuf) {
        buf.writeInt(Item.getRawId(item))
        buf.writeLong(requiredAmount)
        buf.writeLong(totalAmount)
    }

    companion object {

        fun readFromPacketByteBuf(buf: PacketByteBuf): ProfessionCost {
            val item = Item.byRawId(buf.readInt())
            val requiredAmount = buf.readLong()
            val totalAmount = buf.readLong()

            return ProfessionCost(item, requiredAmount, totalAmount)
        }

    }

}
