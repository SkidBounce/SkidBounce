/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/ManInMyVan/SkidBounce/
 */
package net.ccbluex.liquidbounce.event.events

import net.ccbluex.liquidbounce.event.Event

/**
 * Called when player is going to step
 */
class StepEvent(var stepHeight: Float) : Event()
