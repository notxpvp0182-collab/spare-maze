package com.ahmed.sparemaze.mixin;

import com.ahmed.sparemaze.SpareMazeClient;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MouseRotationLockMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            SpareMazeClient mod = SpareMazeClient.getInstance();
            if (mod != null && mod.getRotationController().isLocked()) {
                ci.cancel();
            }
        }
    }
}
