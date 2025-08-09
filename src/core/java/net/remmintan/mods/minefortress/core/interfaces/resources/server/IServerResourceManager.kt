package net.remmintan.mods.minefortress.core.interfaces.resources.server

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager
import net.remmintan.mods.minefortress.core.utils.extractItemsConsideringSimilar

@Suppress("UnstableApiUsage")
interface IServerResourceManager : IServerManager {

    fun getStorage(): Storage<ItemVariant>

    fun hasItems(itemInfos: List<ItemInfo>): Boolean {
        val storage = getStorage()
        Transaction.openOuter().use { tr ->
            for (itemInfo in itemInfos) {
                val itemVariant = itemInfo.item
                val amountToExtract = itemInfo.amount
                val extractedAmount = storage.extractItemsConsideringSimilar(itemVariant, amountToExtract, tr)
                if (extractedAmount != amountToExtract) {
                    return false
                }
            }
        }
        return true
    }

}
