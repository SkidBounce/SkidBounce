/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.targets.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.targets.Friends
import net.ccbluex.liquidbounce.features.module.modules.targets.Teams
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.value.BooleanValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

object HitBox : Module("HitBox", Category.COMBAT) {

    private val targetPlayers by BooleanValue("TargetPlayers", true)
    private val playerSize by FloatValue("PlayerSize", 0.4F, 0F..1F) { targetPlayers }
    private val friendSize by FloatValue("FriendSize", 0.4F, 0F..1F) { targetPlayers }
    private val teamMateSize by FloatValue("TeamMateSize", 0.4F, 0F..1F) { targetPlayers }
    private val botSize by FloatValue("BotSize", 0.4F, 0F..1F) { targetPlayers }

    private val targetMobs by BooleanValue("TargetMobs", false)
    private val mobSize by FloatValue("MobSize", 0.4F, 0F..1F) { targetMobs }

    private val targetAnimals by BooleanValue("TargetAnimals", false)
    private val animalSize by FloatValue("AnimalSize", 0.4F, 0F..1F) { targetAnimals }

    fun determineSize(entity: Entity): Float {
        return when (entity) {
            is EntityPlayer -> {
                if (entity.isSpectator || !targetPlayers) {
                    return 0F
                }

                if (isBot(entity)) {
                    return botSize
                } else if (entity.isClientFriend && !Friends.handleEvents()) {
                    return friendSize
                } else if (Teams.handleEvents() && Teams.isInYourTeam(entity)) {
                    return teamMateSize
                }

                playerSize
            }

            else -> {
                if (entity.isMob && targetMobs) return mobSize
                else if (entity.isAnimal && targetAnimals) return animalSize

                0F
            }
        }
    }
}
