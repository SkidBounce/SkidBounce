/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.FloatValue

object FastBreak : Module("FastBreak", Category.WORLD) {

    private val breakDamage by FloatValue("BreakDamage", 0.8F, 0.1F..1F)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.playerController.blockHitDelay = 0

        if (mc.playerController.curBlockDamageMP > breakDamage)
            mc.playerController.curBlockDamageMP = 1F

        if (Fucker.currentDamage > breakDamage)
            Fucker.currentDamage = 1F

        if (Nuker.currentDamage > breakDamage)
            Nuker.currentDamage = 1F
    }
}
