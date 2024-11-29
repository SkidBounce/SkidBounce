package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.event.events.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.extensions.update
import net.minecraft.init.Blocks.*

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Karhu : SpeedMode("Karhu") {
    private var wasSpeed = false

    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = 0.0265f
        if (isMoving) {
            mc.gameSettings.keyBindJump.pressed = true
            wasSpeed = true
        } else if (wasSpeed) {
            mc.thePlayer.stopXZ()
            wasSpeed = false
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindJump.update()
    }

    override fun onJump(event: JumpEvent) {
        event.motion = when (mc.thePlayer.position.down().getBlock()) {
            water, lava -> 0f
            slime_block, soul_sand -> 0.39f
            else -> 0.415f
        }
    }
}
