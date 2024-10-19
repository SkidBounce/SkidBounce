/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.other

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes.NoSlowMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 * @author SkidderMC/FDPClient
 */
class EmptyPlace : NoSlowMode("EmptyPlace") {
    private val packetTiming by ListValue("PacketTiming", arrayOf("Pre", "Post", "Both"), "Pre")

    override fun onMotion(event: MotionEvent) {
        if (packetTiming.equals(event.eventState.name, true) || packetTiming == "Both")
            sendPacket(C08PacketPlayerBlockPlacement())
    }
}
