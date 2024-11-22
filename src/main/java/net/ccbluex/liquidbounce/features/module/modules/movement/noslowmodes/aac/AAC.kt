/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.aac

import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventState.PRE
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement.field_179726_a
import net.minecraft.util.EnumFacing.DOWN

/**
 * @author Aspw-w/NightX-Client
 */
class AAC : NoSlowMode("AAC", antiDesync = true, swordOnly = true) {
    override fun onMotion(event: MotionEvent) {
        when (event.eventState) {
            PRE -> if (mc.thePlayer.ticksExisted % 3 == 0)
                sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, field_179726_a, DOWN))

            POST -> if (mc.thePlayer.ticksExisted % 3 != 0)
                sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))

            else -> {}
        }
    }
}
