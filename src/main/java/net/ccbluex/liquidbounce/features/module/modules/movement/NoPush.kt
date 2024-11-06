/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.PushOutEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BooleanValue

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object NoPush : Module("NoPush", Category.MOVEMENT) {
    private val blocks by BooleanValue("Blocks", true)
    val pistons by BooleanValue("Pistons", false)
    val pistonBlocks by BooleanValue("PistonBlocks", true)

    @EventTarget
    fun onPush(event: PushOutEvent) {
        if (blocks) event.cancelEvent()
    }
}
