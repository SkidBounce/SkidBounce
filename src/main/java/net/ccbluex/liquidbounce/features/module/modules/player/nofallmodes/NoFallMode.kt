/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes

import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.utils.ClassUtils.getValues
import net.ccbluex.liquidbounce.utils.MinecraftInstance

open class NoFallMode(val modeName: String) : MinecraftInstance() {
    open fun onMove(event: MoveEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onRender3D(event: Render3DEvent) {}
    open fun onBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}

    open fun onEnable() {}
    open fun onDisable() {}
    open fun onToggle() {}

    val values
        get() = getValues(this)
}
