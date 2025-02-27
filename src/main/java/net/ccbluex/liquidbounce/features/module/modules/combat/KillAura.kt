/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventManager.callEvent
import net.ccbluex.liquidbounce.event.EventState.POST
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.events.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.targets.*
import net.ccbluex.liquidbounce.features.module.modules.world.Fucker
import net.ccbluex.liquidbounce.features.module.modules.world.Nuker
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.utils.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.CooldownHelper.getAttackCooldownProgress
import net.ccbluex.liquidbounce.utils.CooldownHelper.resetLastAttackedTicks
import net.ccbluex.liquidbounce.utils.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.RaycastUtils.runWithModifiedRaycastResult
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.getRotationDifference
import net.ccbluex.liquidbounce.utils.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.isRotationFaced
import net.ccbluex.liquidbounce.utils.RotationUtils.isVisible
import net.ccbluex.liquidbounce.utils.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils.isConsumingItem
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK
import net.minecraft.network.play.client.C02PacketUseEntity.Action.INTERACT
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.WorldSettings
import java.awt.Color
import kotlin.math.max

object KillAura : Module("KillAura", Category.COMBAT) {
    /**
     * OPTIONS
     */

    private val simulateCooldown by BooleanValue("SimulateCooldown", false)
    private val simulateDoubleClicking by BooleanValue("SimulateDoubleClicking", false) { !simulateCooldown }

    // CPS - Attack speed
    private val maxCPSValue = object : IntValue("MaxCPS", 8, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(minCPS, newValue)
        }

        override fun isSupported() = !simulateCooldown
    }

    private val maxCPS by maxCPSValue

    private val minCPS: Int by object : IntValue("MinCPS", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(newValue, maxCPS)
        }

        override fun isSupported() = !maxCPSValue.isMinimal && !simulateCooldown
    }

    private val hurtTime by IntValue("HurtTime", 10, 0..10) { !simulateCooldown }

    private val clickOnly by BooleanValue("ClickOnly", false)

    // Range
    // TODO: Make block range independent from attack range
    private val range: Double by object : DoubleValue("Range", 3.7, 1.0..8.0) {
        override fun onChanged(oldValue: Double, newValue: Double) {
            blockRange = blockRange.coerceAtMost(newValue)
        }
    }
    private val scanRange by DoubleValue("ScanRange", 2.0, 0.0..10.0)
    private val throughWallsRange by DoubleValue("ThroughWallsRange", 3.0, 0.0..8.0)
    private val rangeSprintReduction by DoubleValue("RangeSprintReduction", 0.0, 0.0..0.4)

    // Modes
    private val priority by ListValue(
        "Priority", arrayOf(
            "Health",
            "Distance",
            "Direction",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier"
        ), "Distance"
    )
    private val targetMode by ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val limitedMultiTargets by IntValue("LimitedMultiTargets", 0, 0..50) { targetMode == "Multi" }
    private val maxSwitchFOV by DoubleValue("MaxSwitchFOV", 90.0, 30.0..180.0) { targetMode == "Switch" }

    // Delay
    private val switchDelay by LongValue("SwitchDelay", 15, 1L..1000L) { targetMode == "Switch" }

    // Bypass
    private val swing by SwingValue()

    // Settings
    // TODO: only swap view if the user didn't manually change view
    private val autoF5 by BooleanValue("AutoF5", false)
    private val onScaffold by BooleanValue("OnScaffold", false)
    private val onDestroyBlock by BooleanValue("OnDestroyBlock", false)

    // AutoBlock
    private val autoBlock by ListValue("AutoBlock", arrayOf("Off", "Packet", "Fake"), "Packet")
    private val releaseAutoBlock by BooleanValue("ReleaseAutoBlock", true)
    { autoBlock !in arrayOf("Off", "Fake") }
    private val ignoreTickRule by BooleanValue("IgnoreTickRule", false)
    { autoBlock !in arrayOf("Off", "Fake") && releaseAutoBlock }
    private val blockRate by IntValue("BlockRate", 100, 1..100)
    { autoBlock !in arrayOf("Off", "Fake") && releaseAutoBlock }

    private val uncpAutoBlock by BooleanValue("UpdatedNCPAutoBlock", false)
    { autoBlock !in arrayOf("Off", "Fake") && !releaseAutoBlock }

    private val switchStartBlock by BooleanValue("SwitchStartBlock", false)
    { autoBlock !in arrayOf("Off", "Fake") }

    private val interactAutoBlock by BooleanValue("InteractAutoBlock", true)
    { autoBlock !in arrayOf("Off", "Fake") }

    // AutoBlock conditions
    private val smartAutoBlock by BooleanValue("SmartAutoBlock", false) { autoBlock != "Off" }

    // Ignore all blocking conditions, except for block rate, when standing still
    private val forceBlock by BooleanValue("ForceBlockWhenStill", true)
    { autoBlock != "Off" && smartAutoBlock }

    // Don't block if target isn't holding a sword or an axe
    private val checkWeapon by BooleanValue("CheckEnemyWeapon", true)
    { autoBlock != "Off" && smartAutoBlock }

    // TODO: Make block range independent from attack range
    private var blockRange by object : DoubleValue("BlockRange", range, 1.0..8.0) {
        override fun isSupported() = autoBlock != "Off" && smartAutoBlock

        override fun onChange(oldValue: Double, newValue: Double) = newValue.coerceAtMost(this@KillAura.range)
    }

    // Don't block when you can't get damaged
    private val maxOwnHurtTime by IntValue("MaxOwnHurtTime", 3, 0..10)
    { autoBlock != "Off" && smartAutoBlock }

    // Don't block if target isn't looking at you
    private val maxDirectionDiff by FloatValue("MaxOpponentDirectionDiff", 60f, 30f..180f)
    { autoBlock != "Off" && smartAutoBlock }

    // Don't block if target is swinging an item and therefore cannot attack
    private val maxSwingProgress by IntValue("MaxOpponentSwingProgress", 1, 0..5)
    { autoBlock != "Off" && smartAutoBlock }

    // Turn Speed
    private val startFirstRotationSlow by BooleanValue("StartFirstRotationSlow", false)
    private val maxHorizontalSpeedValue = object : FloatValue("MaxHorizontalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalSpeed)
    }
    private val maxHorizontalSpeed by maxHorizontalSpeedValue

    private val minHorizontalSpeed: Float by object : FloatValue("MinHorizontalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalSpeed)
        override fun isSupported() = !maxHorizontalSpeedValue.isMinimal
    }

    private val maxVerticalSpeedValue = object : FloatValue("MaxVerticalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minVerticalSpeed)
    }
    private val maxVerticalSpeed by maxVerticalSpeedValue

    private val minVerticalSpeed: Float by object : FloatValue("MinVerticalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxVerticalSpeed)
        override fun isSupported() = !maxVerticalSpeedValue.isMinimal
    }

    // Raycast
    private val raycastValue = BooleanValue("RayCast", true)
    private val raycast by raycastValue
    private val raycastIgnored by BooleanValue("RayCastIgnored", false) { raycastValue.isActive() }
    private val livingRaycast by BooleanValue("LivingRayCast", true) { raycastValue.isActive() }

    // Hit delay
    private val useHitDelay by BooleanValue("UseHitDelay", false)
    private val hitDelayTicks by IntValue("HitDelayTicks", 1, 1..5) { useHitDelay }

    // Rotations
    private val keepRotationTicks by object : IntValue("KeepRotationTicks", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minimum)
    }
    private val angleThresholdUntilReset by FloatValue("AngleThresholdUntilReset", 5f, 0.1f..180f)
    private val silentRotationValue = BooleanValue("SilentRotation", true)
    private val silentRotation by silentRotationValue
    private val rotationStrafe by ListValue(
        "Strafe",
        arrayOf("Off", "Strict", "Silent"),
        "Off"
    ) { silentRotationValue.isActive() }
    private val smootherMode by ListValue("SmootherMode", arrayOf("Linear", "Relative"), "Relative")
    private val simulateShortStop by BooleanValue("SimulateShortStop", false)
    private val randomCenter by BooleanValue("RandomCenter", true)
    private val gaussianOffset by BooleanValue("GaussianOffset", false) { randomCenter }
    private val outborder by BooleanValue("Outborder", false)
    private val fov by FloatValue("FOV", 180f, 0f..180f)

    // Prediction
    private val predictClientMovement by IntValue("PredictClientMovement", 2, 0..5)
    private val predictEnemyPosition by DoubleValue("PredictEnemyPosition", 1.5, -1.0..2.0)

    // Extra swing
    private val failSwing by BooleanValue("FailSwing", true) { swing != "Off" }
    private val respectMissCooldown by BooleanValue("RespectMissCooldown", false) { swing != "Off" && failSwing }
    private val swingOnlyInAir by BooleanValue("SwingOnlyInAir", true) { swing != "Off" && failSwing }
    private val maxRotationDifferenceToSwing by FloatValue("MaxRotationDifferenceToSwing", 180f, 0f..180f)
    { swing != "Off" && failSwing }
    private val swingWhenTicksLate = object : BooleanValue("SwingWhenTicksLate", false) {
        override fun isSupported() = swing != "Off" && failSwing && maxRotationDifferenceToSwing != 180f
    }
    private val ticksLateToSwing by IntValue("TicksLateToSwing", 4, 0..20)
    { swing != "Off" && failSwing && swingWhenTicksLate.isActive() }

    // Inventory
    private val simulateClosingInventory by BooleanValue("SimulateClosingInventory", false) { !noInventoryAttack }
    private val noInventoryAttack by BooleanValue("NoInvAttack", false)
    private val noInventoryDelay by IntValue("NoInvDelay", 200, 0..500) { noInventoryAttack }
    private val noConsumeAttack by ListValue(
        "NoConsumeAttack",
        arrayOf("Off", "NoHits", "NoRotation"),
        "Off",
    )

    // Visuals
    private val mark by ListValue("Mark", arrayOf("Off", "Platform", "Box"), "Platform", subjective = true)
    private val boxOutline by BooleanValue("Outline", true, subjective = true) { mark == "Box" }
    private val fakeSharp by BooleanValue("FakeSharp", true, subjective = true)

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var hittable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0
    private var clicks = 0
    private var attackTickTimes = mutableListOf<Pair<MovingObjectPosition, Int>>()

    // Container Delay
    private var containerOpen = -1L

    // Block status
    var renderBlocking = false
    var blockStatus = false
    private var blockStopInDead = false

    // Swing
    private var cancelNextSwing = false

    // Switch Delay
    private val switchTimer = MSTimer()

    /**
     * Disable kill aura module
     */
    override fun onToggle(state: Boolean) {
        target = null
        hittable = false
        prevTargetEntities.clear()
        attackTickTimes.clear()
        attackTimer.reset()
        clicks = 0

        if (autoF5)
            mc.gameSettings.thirdPersonView = 0

        stopBlocking()
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState != POST) {
            return
        }

        update()
    }

    fun update() {
        if (cancelRun || (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay))) return

        // Update target
        updateTarget()

        if (autoF5 && mc.gameSettings.thirdPersonView != 1 && (target != null || mc.thePlayer.swingProgress > 0)) {
            mc.gameSettings.thirdPersonView = 1
        }
    }

    @EventTarget
    fun onWorldChange(event: WorldEvent) {
        attackTickTimes.clear()
    }

    /**
     * Tick event
     */
    @EventTarget
    fun onTick(event: TickEvent) {
        if (clickOnly && !mc.gameSettings.keyBindAttack.isKeyDown) return

        if (blockStatus && autoBlock == "Packet" && releaseAutoBlock && !ignoreTickRule) {
            clicks = 0
            stopBlocking()
            return
        }

        if (cancelRun) {
            target = null
            hittable = false
            stopBlocking()
            return
        }

        if (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay)) {
            target = null
            hittable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (simulateCooldown && getAttackCooldownProgress() < 1f) {
            return
        }

        if (target == null && !blockStopInDead) {
            blockStopInDead = true
            stopBlocking()
            return
        }

        if (target != null) {
            if (mc.thePlayer.getDistanceToEntityBox(target!!) > range && blockStatus) {
                stopBlocking()
                return
            } else if (autoBlock != "Off") {
                renderBlocking = true
            }

            // Usually when you butterfly click, you end up clicking two (and possibly more) times in a single tick.
            // Sometimes you also do not click. The positives outweigh the negatives, however.
            val extraClicks = if (simulateDoubleClicking && !simulateCooldown) nextInt(-1, 1) else 0

            val maxClicks = clicks + extraClicks

            repeat(maxClicks) {
                val wasBlocking = blockStatus

                runAttack(it + 1 == maxClicks)
                clicks--

                if (wasBlocking && !blockStatus && (releaseAutoBlock && !ignoreTickRule || autoBlock == "Off")) {
                    return
                }
            }
        } else {
            renderBlocking = false
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (cancelRun) {
            target = null
            hittable = false
            return
        }

        if (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay)) {
            target = null
            hittable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        if (attackTimer.hasTimePassed(attackDelay)) {
            if (maxCPS > 0)
                clicks++
            attackTimer.reset()
            attackDelay = randomClickDelay(minCPS, maxCPS)
        }

        val hittableColor = if (hittable) Color(37, 126, 255, 70) else Color(255, 0, 0, 70)

        if (targetMode != "Multi") {
            when (mark.lowercase()) {
                "platform" -> drawPlatform(target!!, hittableColor)
                "box" -> drawEntityBox(target!!, hittableColor, boxOutline)
            }
        }
    }

    /**
     * Packet event
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C0APacketAnimation && cancelNextSwing) {
            cancelNextSwing = false
            event.cancelEvent()
        }
    }

    /**
     * Attack enemy
     */
    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    private fun runAttack(isLastClick: Boolean) {
        var currentTarget = this.target ?: return

        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return

        if (noConsumeAttack == "NoHits" && isConsumingItem) {
            return
        }

        // Settings
        val multi = targetMode == "Multi"
        val manipulateInventory = simulateClosingInventory && !noInventoryAttack && serverOpenInventory

        // Close inventory when open
        if (manipulateInventory) serverOpenInventory = false

        updateHittable()

        currentTarget = this.target ?: return

        if (hittable && currentTarget.hurtTime > hurtTime) {
            return
        }

        // Check if enemy is not hittable
        if (!hittable) {
            if (swing != "Off" && failSwing) {
                val rotation = currentRotation ?: thePlayer.rotation

                // Left click miss cool-down logic:
                // When you click and miss, you receive a 10 tick cool down.
                // It decreases gradually (tick by tick) when you hold the button.
                // If you click and then release the button, the cool down drops from where it was immediately to 0.
                // Most humans will release the button 1-2 ticks max after clicking, leaving them with an average of 10 CPS.
                // The maximum CPS allowed when you miss is 20 CPS, if you click and release immediately, which is highly unlikely.
                // With that being said, we force an average of 10 CPS by doing this below, since 10 CPS when missing is possible.
                if (respectMissCooldown && ticksSinceClick() <= 1) {
                    return
                }

                // Can humans keep click consistency when performing massive rotation changes?
                // (10-30 rotation difference/doing large mouse movements for example)
                // Maybe apply to attacks too?
                if (getRotationDifference(rotation) > maxRotationDifferenceToSwing) {
                    // At the same time there is also a chance of the user clicking at least once in a while
                    // when the consistency has dropped a lot.
                    val shouldIgnore = swingWhenTicksLate.isActive() && ticksSinceClick() >= ticksLateToSwing

                    if (!shouldIgnore) {
                        return
                    }
                }

                runWithModifiedRaycastResult(rotation, range, throughWallsRange) {
                    if (swingOnlyInAir && it.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
                        return@runWithModifiedRaycastResult
                    }

                    if (!shouldDelayClick(it.typeOfHit)) {
                        if (it.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            val entity = it.entityHit

                            // Use own function instead of clickMouse() to maintain keep sprint, auto block, etc
                            if (entity is EntityLivingBase) {
                                attackEntity(entity, isLastClick)
                            }
                        } else {
                            if (swing == "Visual")
                                cancelNextSwing = true
                            // Imitate game click
                            mc.clickMouse()
                            if (swing == "Packet")
                                mc.thePlayer.isSwingInProgress = false
                        }
                        attackTickTimes += it to runTimeTicks
                    }

                    if (isLastClick) {
                        // We return false because when you click literally once, the attack key's [pressed] status is false.
                        // Since we simulate clicks, we are supposed to respect that behavior.
                        mc.sendClickBlockToController(false)
                    }
                }
            }
        } else {
            blockStopInDead = false
            // Attack
            if (!multi) {
                attackEntity(currentTarget, isLastClick)
            } else {
                var targets = 0

                for (entity in theWorld.loadedEntityList) {
                    val distance = thePlayer.getDistanceToEntityBox(entity)

                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity, isLastClick)

                        targets += 1

                        if (limitedMultiTargets != 0 && limitedMultiTargets <= targets) break
                    }
                }
            }

            val switchMode = targetMode == "Switch"

            if (!switchMode || switchTimer.hasTimePassed(switchDelay)) {
                prevTargetEntities += currentTarget.entityId

                if (switchMode) {
                    switchTimer.reset()
                }
            }
        }

        // Open inventory
        if (manipulateInventory) serverOpenInventory = true
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        if (!onScaffold && Scaffold.handleEvents() && (Scaffold.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        // Reset fixed target to null
        target = null

        // Settings
        val fov = fov
        val switchMode = targetMode == "Switch"

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        val theWorld = mc.theWorld
        val thePlayer = mc.thePlayer

        for (entity in theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && entity.entityId in prevTargetEntities)) continue

            // Will skip new target nearby if fail to hit/couldn't be hit.
            // Since without this check, it seems killaura (Switch) will get stuck.
            // Temporary fix
            if (switchMode && !hittable && prevTargetEntities.isNotEmpty()) continue

            var distance = thePlayer.getDistanceToEntityBox(entity)

            if (Backtrack.handleEvents()) {
                val trackedDistance = Backtrack.getNearestTrackedDistance(entity)

                if (distance > trackedDistance) {
                    distance = trackedDistance
                }
            }

            val entityFov = getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov)) {
                if (switchMode && isLookingOnEntities(entity, maxSwitchFOV) || !switchMode) {
                    targets += entity
                }
            }
        }

        // Sort targets by priority
        when (priority.lowercase()) {
            "distance" -> {
                targets.sortBy {
                    var result = 0.0

                    Backtrack.runWithNearestTrackedDistance(it) {
                        result = thePlayer.getDistanceToEntityBox(it) // Sort by distance
                    }

                    result
                }
            }

            "direction" -> targets.sortBy {
                var result = 0f

                Backtrack.runWithNearestTrackedDistance(it) {
                    result = getRotationDifference(it) // Sort by FOV
                }

                result
            }

            "health" -> targets.sortBy { it.health } // Sort by health
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> targets.sortBy { it.totalArmorValue } // Sort by armor
            "hurtresistance" -> targets.sortBy { it.hurtResistantTime } // Sort by armor hurt time
            "hurttime" -> targets.sortBy { it.hurtTime } // Sort by hurt time
            "healthabsorption" -> targets.sortBy { it.health + it.absorptionAmount } // Sort by full health with absorption effect
            "regenamplifier" -> targets.sortBy {
                if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(
                    Potion.regeneration
                ).amplifier else -1
            }
        }

        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            var success = false

            Backtrack.runWithNearestTrackedDistance(entity) {
                success = updateRotations(entity)
            }

            if (!success) {
                // when failed then try another target
                continue
            }

            // Set target to current entity
            target = entity
            return
        }

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: Entity?): Boolean {
        return isSelected(entity, true)
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase, isLastClick: Boolean) {
        // Stop blocking
        val thePlayer = mc.thePlayer

        if (!onScaffold && Scaffold.handleEvents() && (Scaffold.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        if ((thePlayer.isBlocking || renderBlocking) && (autoBlock == "Off" && blockStatus || autoBlock == "Packet" && releaseAutoBlock)) {
            stopBlocking()

            if (!ignoreTickRule || autoBlock == "Off") {
                return
            }
        }

        // The function is only called when we are facing an entity
        if (shouldDelayClick(MovingObjectPosition.MovingObjectType.ENTITY)) {
            return
        }

        // Call attack event
        callEvent(AttackEvent(entity))

        // Attack target
        mc.thePlayer.swing(swing)

        sendPacket(C02PacketUseEntity(entity, ATTACK))

        if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
            thePlayer.attackTargetEntityWithCurrentItem(entity)

        // FakeSharp
        if (EnchantmentHelper.getModifierForCreature(thePlayer.heldItem, entity.creatureAttribute) <= 0f && fakeSharp)
            thePlayer.onEnchantmentCritical(entity)

        CPSCounter.registerClick(CPSCounter.MouseButton.LEFT)

        // Start blocking after attack
        if (autoBlock != "Off" && (thePlayer.isBlocking || canBlock) && isLastClick) {
            startBlocking(entity, interactAutoBlock, autoBlock == "Fake")
        }

        resetLastAttackedTicks()
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        val player = mc.thePlayer ?: return false

        if (!onScaffold && Scaffold.handleEvents() && (Scaffold.placeInfo != null || Scaffold.placeRotation != null))
            return false

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return false

        val (predictX, predictY, predictZ) = entity.currPos.subtract(entity.prevPos)
            .times(2 + predictEnemyPosition)

        val boundingBox = entity.hitBox.offset(predictX, predictY, predictZ)
        val (currPos, oldPos) = player.currPos to player.prevPos

        val simPlayer = SimulatedPlayer.fromClientPlayer(player.movementInput)

        repeat(predictClientMovement + 1) {
            simPlayer.tick()
        }

        player.setPosAndPrevPos(simPlayer.pos)

        val rotation = searchCenter(
            boundingBox,
            outborder && !attackTimer.hasTimePassed(attackDelay / 2),
            randomCenter,
            gaussianOffset = this.gaussianOffset,
            predict = false,
            lookRange = range.toFloat() + scanRange.toFloat(),
            attackRange = range.toFloat(),
            throughWallsRange = throughWallsRange.toFloat()
        )

        if (rotation == null) {
            player.setPosAndPrevPos(currPos, oldPos)

            return false
        }

        setTargetRotation(
            rotation,
            keepRotationTicks,
            silentRotation && rotationStrafe != "Off",
            silentRotation && rotationStrafe == "Strict",
            !silentRotation,
            minHorizontalSpeed..maxHorizontalSpeed to minVerticalSpeed..maxVerticalSpeed,
            angleThresholdUntilReset,
            smootherMode,
            simulateShortStop,
            startFirstRotationSlow
        )

        player.setPosAndPrevPos(currPos, oldPos)

        return true
    }

    private fun ticksSinceClick() = runTimeTicks - (attackTickTimes.lastOrNull()?.second ?: 0)

    /**
     * Check if enemy is hittable with current rotations
     */
    private fun updateHittable() {
        val eyes = mc.thePlayer.eyes

        val currentRotation = currentRotation ?: mc.thePlayer.rotation
        val target = this.target ?: return

        if (!onScaffold && Scaffold.handleEvents() && (Scaffold.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        var chosenEntity: Entity? = null

        if (raycast) {
            chosenEntity = raycastEntity(
                range,
                currentRotation.yaw,
                currentRotation.pitch
            ) { entity -> !livingRaycast || entity is EntityLivingBase && entity !is EntityArmorStand }

            if (chosenEntity != null && chosenEntity is EntityLivingBase && (Friends.handleEvents() || !(chosenEntity is EntityPlayer && chosenEntity.isClientFriend))) {
                if (raycastIgnored && target != chosenEntity) {
                    this.target = chosenEntity
                }
            }

            hittable = this.target == chosenEntity
        } else {
            hittable = isRotationFaced(target, range, currentRotation)
        }

        if (!hittable) {
            return
        }

        val targetToCheck = chosenEntity ?: this.target ?: return

        // If player is inside entity, automatic yes because the intercept below cannot check for that
        // Minecraft does the same, see #EntityRenderer line 353
        if (targetToCheck.hitBox.isVecInside(eyes)) {
            return
        }

        var checkNormally = true

        if (Backtrack.handleEvents()) {
            Backtrack.loopThroughBacktrackData(targetToCheck) {
                if (targetToCheck.hitBox.isVecInside(eyes)) {
                    checkNormally = false
                    return@loopThroughBacktrackData true
                }

                // Recreate raycast logic
                val intercept = targetToCheck.hitBox.calculateIntercept(
                    eyes,
                    eyes + getVectorForRotation(currentRotation) * range
                )

                if (intercept != null) {
                    // Is the entity box raycast vector visible? If not, check through-wall range
                    hittable = isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange

                    if (hittable) {
                        checkNormally = false
                        return@loopThroughBacktrackData true
                    }
                }

                return@loopThroughBacktrackData false
            }
        }

        if (!checkNormally) {
            return
        }

        // Recreate raycast logic
        val intercept = targetToCheck.hitBox.calculateIntercept(
            eyes,
            eyes + getVectorForRotation(currentRotation) * range
        )

        // Is the entity box raycast vector visible? If not, check through-wall range
        hittable = isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean, fake: Boolean = false) {
        if (blockStatus && !uncpAutoBlock)
            return

        if (!onScaffold && Scaffold.handleEvents() && (Scaffold.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        if (mc.thePlayer.isBlocking) {
            blockStatus = true
            renderBlocking = true
            return
        }

        if (!fake) {
            if (!(blockRate > 0 && nextInt(endExclusive = 100) <= blockRate)) return

            if (interact) {
                val positionEye = mc.thePlayer.eyes

                val boundingBox = interactEntity.hitBox

                val (yaw, pitch) = currentRotation ?: mc.thePlayer.rotation

                val vec = getVectorForRotation(Rotation(yaw, pitch))

                val lookAt = positionEye.add(vec * maxRange)

                val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
                val hitVec = movingObject.hitVec

                sendPackets(
                    C02PacketUseEntity(interactEntity, hitVec - interactEntity.positionVector),
                    C02PacketUseEntity(interactEntity, INTERACT)
                )

            }

            if (switchStartBlock) {
                InventoryUtils.serverSlot = (InventoryUtils.serverSlot + 1) % 9
                InventoryUtils.serverSlot = mc.thePlayer.inventory.currentItem
            }

            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            blockStatus = true
        }

        renderBlocking = true

        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockStatus && !mc.thePlayer.isBlocking) {
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockStatus = false
        }

        renderBlocking = false
    }

    /**
     * Check if raycast landed on a different object
     *
     * The game requires at least 1 tick of cooldown on raycast object type change (miss, block, entity)
     * We are doing the same thing here but allow more cool down.
     */
    private fun shouldDelayClick(type: MovingObjectPosition.MovingObjectType): Boolean {
        if (!useHitDelay) {
            return false
        }

        val lastAttack = attackTickTimes.lastOrNull()

        return lastAttack != null && lastAttack.first.typeOfHit != type && runTimeTicks - lastAttack.second <= hitDelayTicks
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun
        inline get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer) || FreeCam.handleEvents() || (noConsumeAttack == "NoRotation" && isConsumingItem)

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() {
            if (target != null && mc.thePlayer?.heldItem?.item is ItemSword) {
                if (smartAutoBlock) {
                    if (!isMoving && forceBlock) return true

                    if (checkWeapon && (target!!.heldItem?.item !is ItemSword && target!!.heldItem?.item !is ItemAxe))
                        return false

                    if (mc.thePlayer.hurtTime > maxOwnHurtTime) return false

                    val rotationToPlayer = toRotation(mc.thePlayer.hitBox.center, true, target!!)

                    if (getRotationDifference(rotationToPlayer, target!!.rotation) > maxDirectionDiff)
                        return false

                    if (target!!.swingProgressInt > maxSwingProgress) return false

                    if (target!!.getDistanceToEntityBox(mc.thePlayer) > blockRange) return false
                }

                return true
            }

            return false
        }

    /**
     * Range
     */
    private val maxRange
        get() = max(range + scanRange, throughWallsRange)

    private fun getRange(entity: Entity) =
        (if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRange) range + scanRange else throughWallsRange) - if (mc.thePlayer.isSprinting) rangeSprintReduction else 0.0

    /**
     * HUD Tag
     */
    override val tag
        get() = targetMode

    val isBlockingChestAura
        get() = handleEvents() && target != null
}

