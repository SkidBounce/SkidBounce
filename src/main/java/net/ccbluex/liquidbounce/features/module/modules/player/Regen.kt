/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.MovementUtils.serverOnGround
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.IntValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion

object Regen : Module("Regen", Category.PLAYER) {

    private val mode by ListValue("Mode", arrayOf("Spartan", "Vanilla"), "Vanilla")
    private val speed by IntValue("Speed", 100, 1..100) { mode == "Vanilla" }

    private val delay by IntValue("Delay", 0, 0..10000)
    private val health by IntValue("Health", 18, 0..20)
    private val food by IntValue("Food", 18, 0..20)

    private val noAir by BooleanValue("NoAir", false)
    private val potionEffect by BooleanValue("PotionEffect", false)

    private val timer = MSTimer()

    private var resetTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F
        } else {
            resetTimer = false
        }

        val thePlayer = mc.thePlayer ?: return

        if (
            !mc.playerController.gameIsSurvivalOrAdventure()
            || noAir && !serverOnGround
            || thePlayer.foodStats.foodLevel <= food
            || !thePlayer.isEntityAlive
            || thePlayer.health >= health
            || (potionEffect && !thePlayer.isPotionActive(Potion.regeneration))
            || !timer.hasTimePassed(delay)
        ) return

        when (mode.lowercase()) {
            "vanilla" -> {
                repeat(speed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }
            }

            "spartan" -> {
                if (!isMoving && serverOnGround) {
                    repeat(9) {
                        sendPacket(C03PacketPlayer(serverOnGround))
                    }

                    mc.timer.timerSpeed = 0.45F
                    resetTimer = true
                }
            }
        }

        timer.reset()
    }
}
