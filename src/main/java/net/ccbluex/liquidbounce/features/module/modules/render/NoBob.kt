/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoBob : Module("NoBob", Category.RENDER, gameDetecting = false) {

    @EventTarget
    fun onMotion(event: MotionEvent) {
        mc.thePlayer?.distanceWalkedModified = -1f
    }
}
