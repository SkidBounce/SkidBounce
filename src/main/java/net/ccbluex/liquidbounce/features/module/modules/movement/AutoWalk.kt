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
import net.ccbluex.liquidbounce.utils.extensions.update

object AutoWalk : Module("AutoWalk", Category.MOVEMENT, gameDetecting = false) {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindForward.pressed = true
    }

    override fun onDisable() {
        mc.gameSettings.keyBindForward.update()
    }
}
