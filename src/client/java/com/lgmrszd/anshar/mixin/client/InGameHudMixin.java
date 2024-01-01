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
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private ChatHud chatHud;

    @Shadow private int ticks;
    
    @Shadow private TextRenderer getTextRenderer() { return null; }
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void anshar$render(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (PlayerTransportComponent.KEY.get(client.player).isInNetwork() && !client.options.hudHidden) {
            var node = PlayerTransportComponent.KEY.get(client.player).getNearestLookedAt();

            RenderSystem.enableBlend();
            if (node != null) {
                
                TextRenderer textRenderer = getTextRenderer();
                int scaledWidth = context.getScaledWindowWidth();
                int scaledHeight = context.getScaledWindowHeight();

                context.getMatrices().push();
                context.getMatrices().translate(scaledWidth / 2, scaledHeight - 20, 0.0f);
                
                var nameWidth = textRenderer.getWidth((StringVisitable)node.getName());
                int rgb = (int)(node.getColor()[0] * 255);
                rgb = (rgb<<8) + (int)(node.getColor()[1] * 255);
                rgb = (rgb<<8) + (int)(node.getColor()[2] * 255);
                context.drawText(textRenderer, node.getName(), -nameWidth/2, 0, rgb, false);

                var coords = Text.literal(node.getPos().toShortString());
                context.drawText(textRenderer, coords, -textRenderer.getWidth((StringVisitable)coords)/2, -9, 0xFFFFFF, false);

                context.getMatrices().pop();
                
            }
            
            // allow chat rendering
            Window window = this.client.getWindow();
            int n = MathHelper.floor((double)(this.client.mouse.getX() * (double)window.getScaledWidth() / (double)window.getWidth()));
            int p = MathHelper.floor((double)(this.client.mouse.getY() * (double)window.getScaledHeight() / (double)window.getHeight()));
            this.client.getProfiler().push("chat");
            this.chatHud.render(context, this.ticks, n, p);
            this.client.getProfiler().pop();
            RenderSystem.disableBlend();

            ci.cancel();
        }
        
    }
}
