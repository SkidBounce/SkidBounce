/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.hypixel

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.Render3DEvent
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.autoOff
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.checkFallDist
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.maxFallDist
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.minFallDist
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.simulateDebug
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.state
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.ClientUtils.displayClientMessage
import net.ccbluex.liquidbounce.utils.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.blink.IBlink
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import java.awt.Color

object Blink : NoFallMode("Blink"), IBlink {
    private val tick = TickTimer()

    override fun onDisable() {
        blinkingClient = false
        tick.reset()
    }

    override fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isDead)
            return

        val simPlayer = SimulatedPlayer.fromClientPlayer(thePlayer.movementInput)

        simPlayer.tick()

        if (simPlayer.onGround && blinkingClient) {
            if (thePlayer.onGround) {
                tick.update()

                if (tick.hasTimePassed(100)) {
//                    release(server = false)
                    blinkingClient = false
                    displayClientMessage("Unblink")

                    if (autoOff) {
                        state = false
                    }
                    tick.reset()
                }
            }
        }

        if (event.packet is C03PacketPlayer) {
            if (blinkingClient && thePlayer.fallDistance > minFallDist.get()) {
                if (thePlayer.fallDistance < maxFallDist.get()) {
                    if (blinkingClient) {
                        event.packet.onGround = thePlayer.ticksExisted % 2 == 0
                    }
                } else {
                    displayClientMessage("rewriting ground")
//                    release(server = false)
                    blinkingClient = false
                    event.packet.onGround = false
                }
            }
        }

        // Re-check #1
        repeat(2) {
            simPlayer.tick()
        }

        if (simPlayer.isOnLadder() || simPlayer.inWater || simPlayer.isInLava() || simPlayer.isInWeb || simPlayer.isCollided)
            return

        if (thePlayer.motionY > 0 && blinkingClient)
            return

        if (simPlayer.onGround)
            return

        // Re-check #2
        if (checkFallDist) {
            repeat(6) {
                simPlayer.tick()
            }
        }

        val fallingPlayer = FallingPlayer(thePlayer)

        if ((checkFallDist && simPlayer.fallDistance > minFallDist.get()) ||
            !checkFallDist && fallingPlayer.findCollision(60) != null && simPlayer.motionY < 0) {
            if (thePlayer.onGround && !blinkingClient) {
                blinkingClient = true

                displayClientMessage("Blinked")
            }
        }
    }

    @EventTarget
    override fun onRender3D(event: Render3DEvent) {
        if (!simulateDebug) return

        val thePlayer = mc.thePlayer ?: return

        val simPlayer = SimulatedPlayer.fromClientPlayer(thePlayer.movementInput)

        repeat(4) {
            simPlayer.tick()
        }

        thePlayer.run {
            val targetEntity = thePlayer as IMixinEntity

            if (targetEntity.truePos) {

                val x = simPlayer.posX - mc.renderManager.renderPosX
                val y = simPlayer.posY - mc.renderManager.renderPosY
                val z = simPlayer.posZ - mc.renderManager.renderPosZ

                val axisAlignedBB = entityBoundingBox.offset(-posX, -posY, -posZ).offset(x, y, z)

                drawBacktrackBox(
                    AxisAlignedBB.fromBounds(
                        axisAlignedBB.minX,
                        axisAlignedBB.minY,
                        axisAlignedBB.minZ,
                        axisAlignedBB.maxX,
                        axisAlignedBB.maxY,
                        axisAlignedBB.maxZ
                    ), Color.BLUE
                )
            }
        }
    }
}
