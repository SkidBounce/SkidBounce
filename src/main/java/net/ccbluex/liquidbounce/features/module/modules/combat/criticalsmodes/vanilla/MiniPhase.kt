/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.vanilla

import net.ccbluex.liquidbounce.features.module.modules.combat.criticalsmodes.CriticalsMode
import net.minecraft.entity.Entity

/**
 * @author Aspw-w/NightX-Client
 */
object MiniPhase : CriticalsMode("MiniPhase") {
    override fun onAttack(entity: Entity) {
        sendPacket(-0.0125, false)
        sendPacket(0.01275, false)
        sendPacket(-0.00025, true)
        crit(entity)
    }
}
