/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.targets.Animals
import net.ccbluex.liquidbounce.features.module.modules.targets.Invisible
import net.ccbluex.liquidbounce.features.module.modules.targets.Mobs
import net.ccbluex.liquidbounce.features.module.modules.targets.Players

object TargetCommand : Command("target") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            chatSyntax("target <players/mobs/animals/invisible>")
        }

        when (args[1].lowercase()) {
            "players" -> {
                Players.state = !Players.state
                chat("§7Target player toggled ${if (Players.state) "on" else "off"}.")
                playEdit()
            }

            "mobs" -> {
                Mobs.state = !Mobs.state
                chat("§7Target mobs toggled ${if (Mobs.state) "on" else "off"}.")
                playEdit()
            }

            "animals" -> {
                Animals.state = !Animals.state
                chat("§7Target animals toggled ${if (Animals.state) "on" else "off"}.")
                playEdit()
            }

            "invisible" -> {
                Invisible.state = !Invisible.state
                chat("§7Target Invisible toggled ${if (Invisible.state) "on" else "off"}.")
                playEdit()
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("players", "mobs", "animals", "invisible")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
