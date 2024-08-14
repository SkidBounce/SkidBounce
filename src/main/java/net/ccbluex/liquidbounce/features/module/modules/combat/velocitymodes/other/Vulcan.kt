/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.other

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.VelocityMode
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitymodes.vanilla.Vanilla
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.server.S32PacketConfirmTransaction

/**
 * @author SkidBounce/SkidBounce
 * @author ManInMyVan
 */
object Vulcan : VelocityMode("Vulcan") {
    private var queuedTransaction: S32PacketConfirmTransaction? = null
    private var skipNext = false

    override fun onDisable() {
        PacketUtils.handlePacket(queuedTransaction ?: return)
    }

    override fun onPacket(event: PacketEvent) {
        if (event.packet is S32PacketConfirmTransaction) {
            if (event.packet.actionNumber % 20 != 0) {
                event.cancelEvent()
                return
            }

            if (skipNext) {
                skipNext = false
                event.cancelEvent()
                return
            }

            if (queuedTransaction != null) {
                PacketUtils.handlePacket(queuedTransaction)
            }

            queuedTransaction = event.packet

        }
    }

    override fun onVelocityPacket(event: PacketEvent) {
        Vanilla.onVelocityPacket(event)
        queuedTransaction = null
        skipNext = true
    }
}
