package com.lgmrszd.anshar.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.beacon.PlayerTransportComponent;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    
    @Shadow
    private TextRenderer getTextRenderer() { return null; }
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void anshar$render(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (PlayerTransportComponent.KEY.get(client.player).isInNetwork()) {
            var node = PlayerTransportComponent.KEY.get(client.player).getNearestLookedAt();
            if (node != null) {

                RenderSystem.enableBlend();
                TextRenderer textRenderer = getTextRenderer();
                int scaledWidth = context.getScaledWindowWidth();
                int scaledHeight = context.getScaledWindowHeight();

                context.getMatrices().push();
                context.getMatrices().translate(scaledWidth / 2, scaledHeight - 20, 0.0f);
                
                var nameWidth = textRenderer.getWidth((StringVisitable)node.getName());
                int rgb = (int)node.getColor()[0];
                rgb = (rgb<<8) + (int)node.getColor()[1];
                rgb = (rgb<<8) + (int)node.getColor()[2];
                context.drawText(textRenderer, node.getName(), -nameWidth/2, 0, rgb, false);

                var coords = Text.literal(node.getPos().toShortString());
                context.drawText(textRenderer, coords, -textRenderer.getWidth((StringVisitable)coords)/2, -9, 0xFFFFFF, false);

                context.getMatrices().pop();
                RenderSystem.disableBlend();
            }
            ci.cancel();
        };
        
    }
}
