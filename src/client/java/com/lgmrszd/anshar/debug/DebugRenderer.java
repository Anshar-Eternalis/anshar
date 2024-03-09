package com.lgmrszd.anshar.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static com.lgmrszd.anshar.debug.DebugClient.DEBUG_LINES;

public class DebugRenderer {

    public static void init() {
        WorldRenderEvents.END.register(DebugRenderer::debugRenderer);
    }

    private static void debugLine(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrixStack, BlockPos pos1, BlockPos pos2) {
        debugLine(tessellator, buffer, matrixStack, pos1, pos2, ColorHelper.Argb.getArgb(255, 0, 255, 0), ColorHelper.Argb.getArgb(255, 255, 0, 0));
    }

    private static void debugLine(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrixStack, BlockPos pos1, BlockPos pos2, int color1, int color2) {
        Vec3d lineStart = pos1.toCenterPos();
        Vec3d lineEnd = pos2.toCenterPos();
        Vector3f lineVec = lineEnd.subtract(lineStart).toVector3f();

        matrixStack.push();
        matrixStack.translate(lineStart.x, lineStart.y, lineStart.z);

        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(positionMatrix, 0, 0, 0).color(color1).next();
        buffer.vertex(positionMatrix, lineVec.x, lineVec.y, lineVec.z).color(color2).next();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//        RenderSystem.lineWidth(500f);

        tessellator.draw();

        matrixStack.pop();

    }
    private static void debugLineTriangle(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrixStack, BlockPos pos1, BlockPos pos2) {
        debugLineTriangle(tessellator, buffer, matrixStack, pos1, pos2, ColorHelper.Argb.getArgb(255, 0, 255, 0), ColorHelper.Argb.getArgb(255, 255, 0, 0));
    }

    private static void debugLineTriangle(Tessellator tessellator, BufferBuilder buffer, MatrixStack matrixStack, BlockPos pos1, BlockPos pos2, int color1, int color2) {
        Vec3d lineStart = pos1.toCenterPos();
        Vec3d lineEnd = pos2.toCenterPos();
        Vector3f lineVec = lineEnd.subtract(lineStart).toVector3f();
        Vector3f xzVec = new Vector3f(lineVec);
        xzVec.y = 0;
        Vector3f v1;
//        v1 = new Vector3f(lineVec).cross(xzVec).normalize().mul(0.1f);
        if (lineVec.y == 0) v1 = new Vector3f(lineVec).cross(new Vector3f(0, 1, 0)).normalize().mul(0.1f);
        else if (xzVec.length() == 0) v1 = new Vector3f(0.1f, 0, 0);
        else v1 = new Vector3f(lineVec).cross(xzVec).normalize().mul(0.1f);
        Vector3f v2 = new Vector3f(lineVec).cross(v1).normalize().mul(0.1f);
        Vector3f v3 = new Vector3f().sub(v1);
        Vector3f v4 = new Vector3f().sub(v2);


        matrixStack.push();
        matrixStack.translate(lineStart.x, lineStart.y, lineStart.z);

        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();


        buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        buffer.vertex(positionMatrix, v1.x, v1.y, v1.z).color(color1).next();
        buffer.vertex(positionMatrix, v2.x, v2.y, v2.z).color(color1).next();
        buffer.vertex(positionMatrix, lineVec.x, lineVec.y, lineVec.z).color(color2).next();

        buffer.vertex(positionMatrix, v2.x, v2.y, v2.z).color(color1).next();
        buffer.vertex(positionMatrix, v3.x, v3.y, v3.z).color(color1).next();
        buffer.vertex(positionMatrix, lineVec.x, lineVec.y, lineVec.z).color(color2).next();

        buffer.vertex(positionMatrix, v3.x, v3.y, v3.z).color(color1).next();
        buffer.vertex(positionMatrix, v4.x, v4.y, v4.z).color(color1).next();
        buffer.vertex(positionMatrix, lineVec.x, lineVec.y, lineVec.z).color(color2).next();

        buffer.vertex(positionMatrix, v4.x, v4.y, v4.z).color(color1).next();
        buffer.vertex(positionMatrix, v1.x, v1.y, v1.z).color(color1).next();
        buffer.vertex(positionMatrix, lineVec.x, lineVec.y, lineVec.z).color(color2).next();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        tessellator.draw();

        matrixStack.pop();

    }
    private static void debugRenderer(WorldRenderContext context) {
        Camera camera = context.camera();
//        boolean sec = context.world().getTime() % 100 == 0;
//        if (sec) {
//            LOGGER.info("\n======================");
//        }

//        Vec3d targetPosition = new Vec3d(0, 100, 0);
        Vec3d cameraPos = camera.getPos();
//        Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        // TODO remove those
        debugLine(tessellator, buffer, matrixStack, new BlockPos(-10, 0, 0), new BlockPos(10, 0, 0));
        debugLine(tessellator, buffer, matrixStack, new BlockPos(0, 0, -10), new BlockPos(0, 0, 10));
        debugLine(tessellator, buffer, matrixStack, new BlockPos(0, -10, 0), new BlockPos(0, 10, 0));

        for (Map.Entry<DebugLine, Long> entry : DEBUG_LINES.entrySet()) {
            BlockPos start = entry.getKey().start();
            BlockPos end = entry.getKey().end();
            int startColor = entry.getKey().startColor();
            int endColor = entry.getKey().endColor();
            debugLineTriangle(tessellator, buffer, matrixStack, start, end, startColor, endColor);
        }

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableCull();
    }
}
