/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.watchdog

import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing.DOWN

/**
 * @author SkidderMC/FDPClient
 */
class WatchDog : NoSlowMode("WatchDog", antiDesync = true, allowFood = false, allowDrink = false, allowBow = false) {
    override fun onMotion(event: MotionEvent) {
        if (!mc.thePlayer.onGround)
            return

        if (mc.thePlayer.ticksExisted % 2 == 0 && event.eventState == PRE)
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos(-1, -1, -1), DOWN))

        if (mc.thePlayer.ticksExisted % 2 != 0 && event.eventState == POST)
            sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.heldItem, 0f, 0f, 0f))
    }
}
