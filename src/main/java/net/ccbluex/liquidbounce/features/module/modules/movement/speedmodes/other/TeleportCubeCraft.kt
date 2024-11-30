/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.direction
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author CCBlueX/LiquidBounce
 */
object TeleportCubeCraft : SpeedMode("TeleportCubeCraft", true) {
    private val portLength by FloatValue("PortLength", 1f, 0.1f..2f)
    private val timer = MSTimer()
    override fun onMove(event: MoveEvent) {
        if (isMoving && mc.thePlayer.onGround && timer.hasTimePassed(300)) {
            val yaw = direction
            val length = portLength
            event.x = -sin(yaw) * length
            event.z = cos(yaw) * length
            timer.reset()
        }
    }
}
