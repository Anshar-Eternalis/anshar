package com.lgmrszd.anshar.mixin.client.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lgmrszd.anshar.transport.PlayerTransportComponent;
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
        PlayerTransportComponent transportComponent = PlayerTransportComponent.KEY.get(client.player);
        if (transportComponent.isInNetwork() && !client.options.hudHidden) {
            var node = transportComponent.getNearestLookedAt();

            RenderSystem.enableBlend();
            if (node != null) {
                
                TextRenderer textRenderer = getTextRenderer();
                int scaledWidth = context.getScaledWindowWidth();
                int scaledHeight = context.getScaledWindowHeight();

                context.getMatrices().push();
                context.getMatrices().translate(scaledWidth / 2, 0, 0.0f);
                
                int rgb = (int)(node.getColor()[0] * 255);
                rgb = (rgb<<8) + (int)(node.getColor()[1] * 255);
                rgb = (rgb<<8) + (int)(node.getColor()[2] * 255);
                anshar$drawText(context, textRenderer, node.getName(), scaledHeight-20, rgb);

                var coords = Text.literal(node.getPos().toShortString());
                anshar$drawText(context, textRenderer, coords, scaledHeight-32, 0xFFFFFF);

                // panic instructions
                if (transportComponent.shouldShowHelp()) {
                    int helpColor = (int)(0xFF * ((Math.sin(client.player.getWorld().getTime() / 20.0) + 1.0)/2.0)) << 16;
                    anshar$drawText(context, textRenderer, Text.translatable("anshar.help.transport.gate"), 10, helpColor);
                    anshar$drawText(context, textRenderer, Text.translatable("anshar.help.transport.exit"), 22, helpColor);
                }

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

    private void anshar$drawText(DrawContext context, TextRenderer textRenderer, Text text, int verticalPos, int color) {
        context.drawText(textRenderer, text, -getTextRenderer().getWidth((StringVisitable)text)/2, verticalPos, color, false);
    }
}
