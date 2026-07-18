package com.mcnf.containermark.client.mixin;

import com.mcnf.containermark.client.ClientMarkProcessor;
import com.mcnf.containermark.client.ClientModState;
import com.mcnf.containermark.client.KeyBindings;
import com.mcnf.containermark.network.MarkSlotPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    protected Slot focusedSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void containermark$keyPressed(KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindings.markKey != null && KeyBindings.markKey.matchesKey(keyInput)) {
            if (focusedSlot != null && focusedSlot.hasStack() && !focusedSlot.getStack().isEmpty()) {
                int slotIndex = focusedSlot.id;
                if (ClientModState.isServerInstalled()) {
                    // Server mode: send network packet
                    ClientPlayNetworking.send(new MarkSlotPayload(slotIndex));
                } else {
                    // Standalone mode: client-side processing
                    ClientMarkProcessor.handleSlotMark(MinecraftClient.getInstance(), slotIndex);
                }
                cir.setReturnValue(true);
            }
        }
    }
}
