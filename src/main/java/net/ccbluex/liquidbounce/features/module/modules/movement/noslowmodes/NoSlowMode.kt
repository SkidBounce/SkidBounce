/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.noslowmodes

import net.ccbluex.liquidbounce.event.events.MotionEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.utils.ClassUtils.getValues
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.value.Value

open class NoSlowMode(
    val modeName: String,
    val allowNoMove: Boolean = true,
    val handlePacketEventOnNoMove: Boolean = false,
    val antiDesync: Boolean = false
) : MinecraftInstance() {
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}
    open fun onPacket(event: PacketEvent) {}

    open val values: List<Value<*>>
        get() = getValues(this)

    open val canNoSlow = true
}
