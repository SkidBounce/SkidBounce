/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.command.special

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.client.LiquidChat
import net.ccbluex.liquidbounce.utils.misc.StringUtils

object PrivateChatCommand : Command("pchat", "privatechat", "lcpm") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            if (!LiquidChat.state) {
                chat("§cError: §7LiquidChat is disabled!")
                return
            }

            if (!LiquidChat.client.isConnected()) {
                chat("§cError: §LiquidChat is currently not connected to the server!")
                return
            }

            val target = args[1]
            val message = StringUtils.toCompleteString(args, 2)

            LiquidChat.client.sendPrivateMessage(target, message)
            chat("Message was successfully sent.")
        } else
            chatSyntax("pchat <username> <message>")
    }
}
