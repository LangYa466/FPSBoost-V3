package cn.fpsboost.util.render;

import cn.fpsboost.screen.ClickGUI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static cn.fpsboost.Wrapper.mc;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class RenderUtil {
    public static boolean isHovered(float x, float y, float width, float height, int mouseX, int mouseY) {
        return isHovered(x, y, width, height, (float) mouseX, (float) mouseY);
    }

    public static boolean isHovered(float x, float y, float width, float height, float mouseX, float mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void drawRect(float p_drawRect_0_, float p_drawRect_1_, float p_drawRect_2_, float p_drawRect_3_, int p_drawRect_4_) {
        if (p_drawRect_0_ < p_drawRect_2_) {
            float lvt_5_1_ = p_drawRect_0_;
            p_drawRect_0_ = p_drawRect_2_;
            p_drawRect_2_ = lvt_5_1_;
        }

        if (p_drawRect_1_ < p_drawRect_3_) {
            float lvt_5_2_ = p_drawRect_1_;
            p_drawRect_1_ = p_drawRect_3_;
            p_drawRect_3_ = lvt_5_2_;
        }

        float lvt_5_3_ = (float)(p_drawRect_4_ >> 24 & 255) / 255.0F;
        float lvt_6_1_ = (float)(p_drawRect_4_ >> 16 & 255) / 255.0F;
        float lvt_7_1_ = (float)(p_drawRect_4_ >> 8 & 255) / 255.0F;
        float lvt_8_1_ = (float)(p_drawRect_4_ & 255) / 255.0F;
        Tessellator lvt_9_1_ = Tessellator.getInstance();
        WorldRenderer lvt_10_1_ = lvt_9_1_.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(lvt_6_1_, lvt_7_1_, lvt_8_1_, lvt_5_3_);
        lvt_10_1_.begin(7, DefaultVertexFormats.POSITION);
        lvt_10_1_.pos(p_drawRect_0_, p_drawRect_3_, 0.0F).endVertex();
        lvt_10_1_.pos(p_drawRect_2_, p_drawRect_3_, 0.0F).endVertex();
        lvt_10_1_.pos(p_drawRect_2_, p_drawRect_1_, 0.0F).endVertex();
        lvt_10_1_.pos(p_drawRect_0_, p_drawRect_1_, 0.0F).endVertex();
        lvt_9_1_.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawHorizontalLine(float p_drawHorizontalLine_1_, float p_drawHorizontalLine_2_, float p_drawHorizontalLine_3_, int p_drawHorizontalLine_4_) {
        if (p_drawHorizontalLine_2_ < p_drawHorizontalLine_1_) {
            float lvt_5_1_ = p_drawHorizontalLine_1_;
            p_drawHorizontalLine_1_ = p_drawHorizontalLine_2_;
            p_drawHorizontalLine_2_ = lvt_5_1_;
        }

        drawRect(p_drawHorizontalLine_1_, p_drawHorizontalLine_3_, p_drawHorizontalLine_2_ + 1, p_drawHorizontalLine_3_ + 1, p_drawHorizontalLine_4_);
    }

    public static void drawVerticalLine(float p_drawVerticalLine_1_, float p_drawVerticalLine_2_, float p_drawVerticalLine_3_, int p_drawVerticalLine_4_) {
        if (p_drawVerticalLine_3_ < p_drawVerticalLine_2_) {
            float lvt_5_1_ = p_drawVerticalLine_2_;
            p_drawVerticalLine_2_ = p_drawVerticalLine_3_;
            p_drawVerticalLine_3_ = lvt_5_1_;
        }

        drawRect(p_drawVerticalLine_1_, p_drawVerticalLine_2_ + 1, p_drawVerticalLine_1_ + 1, p_drawVerticalLine_3_, p_drawVerticalLine_4_);
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        // 使用局部变量缓存 tex 坐标计算
        float texU1 = u * f;
        float texV1 = v * f1;
        float texU2 = (u + width) * f;
        float texV2 = (v + height) * f1;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(texU1, texV2).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex(texU2, texV2).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex(texU2, texV1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(texU1, texV1).endVertex();
        tessellator.draw();
    }
    
    public static void drawOutline(float x, float y, float width, float height, int color) {
        drawHorizontalLine(x, x + width, y, color);
        drawHorizontalLine(x, x + width, y + height, color);

        drawVerticalLine(x, y, y + height, color);
        drawVerticalLine(x + width, y, y + height, color);
    }

    public static void drawImage(ResourceLocation texture, float x, float y, int width, int height) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 1); // 确保每次绘制时重置颜色
        mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.disableBlend();
    }

    public static void drawCircle(float centerX, float centerY, float radius, int color) {
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1 = centerX + (float) Math.cos(angle1) * radius;
            float y1 = centerY + (float) Math.sin(angle1) * radius;
            float x2 = centerX + (float) Math.cos(angle2) * radius;
            float y2 = centerY + (float) Math.sin(angle2) * radius;

            RenderUtil.drawHorizontalLine(x1, x2, y1, color);
        }
    }
}