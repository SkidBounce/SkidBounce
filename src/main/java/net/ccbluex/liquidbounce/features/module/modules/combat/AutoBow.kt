/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.value.BooleanValue
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object AutoBow : Module("AutoBow", Category.COMBAT) {

    private val waitForBowAimbot by BooleanValue("WaitForBowAimbot", true)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer

        if (thePlayer.isUsingItem && thePlayer.heldItem?.item is ItemBow &&
            thePlayer.itemInUseDuration > 20 && (!waitForBowAimbot || !BowAimBot.handleEvents() || BowAimBot.hasTarget())
        ) {
            thePlayer.stopUsingItem()
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
        }
    }
}
