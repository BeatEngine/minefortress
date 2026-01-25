package net.remmintan.mods.minefortress.blocks.building

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Vec2f
import net.remmintan.mods.minefortress.core.dtos.buildings.HudBar
import net.remmintan.mods.minefortress.core.isClientInFortressGamemode
import net.remmintan.mods.minefortress.core.services.BarRenderer



object BuildingsHudRenderer {

    private val visibleBuildings = mutableSetOf<BuildingRenderState>()

    fun addVisibleBuilding(pos: Vec2f, distance: Double, bars: List<HudBar>, icon: Item?) {
        val state = BuildingRenderState(pos, distance.toFloat(), bars, icon)
        visibleBuildings.add(state)
    }

    fun register() {
        HudRenderCallback.EVENT.register { context, tickDelta ->
            if (isClientInFortressGamemode()) {
                val scaleFactor = MinecraftClient.getInstance().window.scaleFactor
                val windowScaleRatio = scaleFactor / 2.0

                for ((screenPos, distance, bars, icon) in visibleBuildings) {
                    val matrices = context.matrices
                    matrices.push()
                    matrices.translate(
                        screenPos.x.toDouble() / windowScaleRatio,
                        screenPos.y.toDouble() / windowScaleRatio,
                        -1000.0
                    )

                    val adjustedDistance = distance / 8f
                    val ratio = 1 / adjustedDistance
                    matrices.scale(ratio, ratio, ratio)

                    val barRenderer = BarRenderer(ctx = context)
                    bars.forEach { (index, progress, color) ->
                        barRenderer.renderBarWithProgress(index, color, progress)
                    }
                    try {
                    context.drawItem(ItemStack(icon), -8, -20)
                    } catch (e: Throwable) {

                    }
                    matrices.pop()
                }
            }


            visibleBuildings.clear()
        }
    }

}

private data class BuildingRenderState(
    val screenPos: Vec2f,
    val distance: Float,
    val bars: List<HudBar>,
    val icon: Item?
)