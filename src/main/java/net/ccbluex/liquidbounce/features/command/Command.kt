/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.LiquidBounce.commandManager
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

abstract class Command(val command: String, vararg val alias: String) : MinecraftInstance() {
    /**
     * Execute commands with provided [args]
     */
    abstract fun execute(args: Array<String>)

    /**
     * Returns a list of command completions based on the provided [args].
     * If a command does not implement [tabComplete] an [EmptyList] is returned by default.
     *
     * @param args an array of command arguments that the player has passed to the command so far
     * @return a list of matching completions for the command the player is trying to autocomplete
     * @author NurMarvin
     */
    open fun tabComplete(args: Array<String>) = emptyList<String>()

    /**
     * Print [msg] to chat
     */
    protected fun chat(msg: String) = displayClientMessage("§3$msg")

    /**
     * Print [syntax] of command to chat
     */
    protected fun chatSyntax(syntax: String) = displayClientMessage("§3Syntax: §7${commandManager.prefix}$syntax")

    /**
     * Print [syntaxes] of command to chat
     */
    protected fun chatSyntax(syntaxes: Array<String>) {
        displayClientMessage("§3Syntax:")

        for (syntax in syntaxes)
            displayChatMessage("§8> §7${commandManager.prefix}$command ${syntax.lowercase()}")
    }

    /**
     * Print a syntax error to chat
     */
    protected fun chatSyntaxError() = displayClientMessage("§3Syntax error")

    /**
     * Play edit sound
     */
    protected fun playEdit() = mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.anvil_use"), 1F))
}
