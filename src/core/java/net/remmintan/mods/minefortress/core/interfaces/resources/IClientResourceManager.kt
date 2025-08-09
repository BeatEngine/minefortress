package net.remmintan.mods.minefortress.core.interfaces.resources

import net.remmintan.mods.minefortress.core.dtos.ItemInfo


interface IClientResourceManager {

    fun hasItems(stacks: List<ItemInfo>): Boolean
    fun syncRequestedItems(stacks: List<ItemInfo>)

}
