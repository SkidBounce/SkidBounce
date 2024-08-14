/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.event.events

import net.ccbluex.liquidbounce.event.CancellableEvent

/**
 * Called in "moveFlying"
 */
class StrafeEvent(val strafe: Float, val forward: Float, val friction: Float) : CancellableEvent()
