package net.remmintan.mods.minefortress.core.interfaces.resources

import net.remmintan.mods.minefortress.core.dtos.ItemInfo

interface IClientResourceHelper {

    fun getMetRequirements(costs: List<ItemInfo>): Map<ItemInfo, Boolean>

}