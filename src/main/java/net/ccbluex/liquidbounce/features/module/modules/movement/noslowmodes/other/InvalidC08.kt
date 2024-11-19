package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos

class InvalidC08 : NoSlowMode("InvalidC08") {
    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), -1, mc.thePlayer.heldItem, -1f, -1f, -1f))
        }
    }
}
