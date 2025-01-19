/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.blink.BlinkHandler

object ActiveBlinkHandlersCommand : Command("activeblinkhandlers") {
    override fun execute(args: Array<String>) {
        chat("Client: " + BlinkHandler.clientBlinkStates.joinToString { it.javaClass.name })
        chat("Server: " + BlinkHandler.serverBlinkStates.joinToString { it.javaClass.name })
    }
}
