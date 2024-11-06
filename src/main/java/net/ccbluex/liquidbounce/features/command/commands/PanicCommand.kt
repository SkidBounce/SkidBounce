/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.Category

object PanicCommand : Command("panic") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        var modules = moduleManager.modules.filter { it.state }
        val msg: String

        when (args.getOrNull(1)?.lowercase()) {
            "all" -> msg = "all"

            "nonrender", null -> {
                modules = modules.filter { it.category !in listOf(Category.RENDER, Category.CLIENT, Category.TARGETS) }
                msg = "all non-render"
            }

            else -> {
                val categories = Category.entries.filter { it.displayName.equals(args[1], true) }

                if (categories.isEmpty() || categories.all { it == Category.TARGETS }) {
                    chat("Category ${args[1]} not found")
                    return
                }

                val category = categories[0]
                modules = modules.filter { it.category == category && it.category != Category.TARGETS }
                msg = "all ${category.displayName}"
            }
        }

        for (module in modules)
            module.state = false

        chat("Disabled $msg modules.")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("all", "nonrender", "combat", "player", "movement", "render", "world", "misc", "exploit", "fun", "client")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
