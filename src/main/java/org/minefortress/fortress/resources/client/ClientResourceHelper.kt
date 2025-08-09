package org.minefortress.fortress.resources.client

import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceHelper
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager
import net.remmintan.mods.minefortress.core.utils.ClientModUtils

class ClientResourceHelper : IClientResourceHelper {

    override fun getMetRequirements(costs: List<ItemInfo>): Map<ItemInfo, Boolean> {
        val resourceManager = getResourceManager()
        return costs.associateWith {
            resourceManager.hasItems(listOf(it))
        }
    }

    private fun getResourceManager(): IClientResourceManager {
        return ClientModUtils.getFortressManager().resourceManager
    }

}