/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.extensions.jmp

object Parkour : Module("Parkour", Category.MOVEMENT, gameDetecting = false) {

    @Suppress("UNUSED_PARAMETER")
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (isMoving && thePlayer.onGround && !thePlayer.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown &&
            mc.theWorld.getCollidingBoundingBoxes(
                thePlayer, thePlayer.entityBoundingBox
                    .offset(0.0, -0.5, 0.0).expand(-0.001, 0.0, -0.001)
            ).isEmpty()
        )
            thePlayer.jmp()
    }
}
