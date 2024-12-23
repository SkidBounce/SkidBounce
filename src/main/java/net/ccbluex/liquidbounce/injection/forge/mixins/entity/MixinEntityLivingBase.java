/*
 * SkidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge, Forked from LiquidBounce.
 * https://github.com/SkidBounce/SkidBounce/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.JumpEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.movement.AirJump;
import net.ccbluex.liquidbounce.features.module.modules.movement.Jesus;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoJumpDelay;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sprint;
import net.ccbluex.liquidbounce.features.module.modules.render.Animations;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.extensions.ExtensionsKt;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {
    @Shadow public float rotationYawHead;
    @Shadow public boolean isJumping;
    @Shadow public int jumpTicks;
    @Shadow protected abstract float getJumpUpwardsMotion();
    @Shadow public abstract PotionEffect getActivePotionEffect(Potion potionIn);
    @Shadow public abstract boolean isPotionActive(Potion potionIn);
    @Shadow public void onLivingUpdate() {}
    @Shadow protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);
    @Shadow public abstract float getHealth();
    @Shadow public abstract ItemStack getHeldItem();
    @Shadow protected abstract void updateAITick();

    /**
     * @author CCBlueX
     */
    @SuppressWarnings("OverwriteAuthorRequired")
    @Overwrite
    protected void jump() {
        final JumpEvent jumpEvent = new JumpEvent(getJumpUpwardsMotion(), 0.2f);
        EventManager.INSTANCE.callEvent(jumpEvent);
        if (jumpEvent.isCancelled()) return;

        motionY = jumpEvent.getMotion();

        if (isPotionActive(Potion.jump))
            motionY += (float) (getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;

        if (isSprinting() && MovementUtils.INSTANCE.isMoving()) {
            float fixedYaw = this.rotationYaw;

            final RotationUtils rotationUtils = RotationUtils.INSTANCE;
            final Rotation currentRotation = rotationUtils.getCurrentRotation();
            final RotationUtils.RotationData rotationData = rotationUtils.getRotationData();
            if (currentRotation != null && rotationData != null && rotationData.getStrafe()) {
                fixedYaw = currentRotation.getYaw();
            }

            final Sprint sprint = Sprint.INSTANCE;
            if (sprint.handleEvents() && sprint.getJumpDirections()) {
                fixedYaw += ExtensionsKt.toDegreesF(MovementUtils.INSTANCE.getDirection()) - this.rotationYaw;
            }

            final float f = fixedYaw * 0.017453292F;
            motionX -= MathHelper.sin(f) * jumpEvent.getSprintBoost();
            motionZ += MathHelper.cos(f) * jumpEvent.getSprintBoost();
        }

        isAirBorne = true;
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void headLiving(CallbackInfo callbackInfo) {
        if (NoJumpDelay.INSTANCE.handleEvents()) jumpTicks = 0;
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;isJumping:Z", ordinal = 1))
    private void onJumpSection(CallbackInfo callbackInfo) {
        if (AirJump.INSTANCE.handleEvents() && isJumping && jumpTicks == 0) {
            jump();
            jumpTicks = 10;
        }

        final Jesus jesus = Jesus.INSTANCE;

        if (jesus.handleEvents() && !isJumping && !isSneaking() && isInWater() && jesus.getMode().equals("Swim")) {
            updateAITick();
        }
    }

    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void getLook(CallbackInfoReturnable<Vec3> callbackInfoReturnable) {
        //noinspection ConstantConditions
        if (((EntityLivingBase) (Object) this) instanceof EntityPlayerSP)
            callbackInfoReturnable.setReturnValue(getVectorForRotation(rotationPitch, rotationYaw));
    }

    /**
     * Inject head yaw rotation modification
     */
    @SuppressWarnings("UnreachableCode")
    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;updateEntityActionState()V", shift = At.Shift.AFTER))
    private void hookHeadRotations(CallbackInfo ci) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        this.rotationYawHead = ((EntityLivingBase) (Object) this) instanceof EntityPlayerSP && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : this.rotationYawHead;
    }

    /**
     * Inject body rotation modification
     */
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F", ordinal = 0))
    private float hookBodyRotationsA(EntityLivingBase instance) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        return instance instanceof EntityPlayerSP && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.rotationYaw;
    }

    /**
     * Inject body rotation modification
     */
    @Redirect(method = "updateDistance", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F"))
    private float hookBodyRotationsB(EntityLivingBase instance) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        return instance instanceof EntityPlayerSP && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.rotationYaw;
    }

    /**
     * @author SuperSkidder
     * @reason Animations swing speed
     */
    @ModifyConstant(method = "getArmSwingAnimationEnd", constant = @Constant(intValue = 6))
    private int injectAnimationsModule(int constant) {
        Animations module = Animations.INSTANCE;

        return module.handleEvents() ? (2 + (20 - module.getSwingSpeed())) : constant;
    }
}
