/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.event.events

import net.ccbluex.liquidbounce.event.Event
import net.ccbluex.liquidbounce.event.EventState

/**
 * Called in "onUpdateWalkingPlayer"
 *
 * @param eventState PRE or POST
 */
class MotionEvent(val eventState: EventState) : Event()
