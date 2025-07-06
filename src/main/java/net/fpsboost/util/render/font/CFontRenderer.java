package net.fpsboost.util.render.font;

import lombok.Getter;
import net.fpsboost.manager.impl.DrawTextHookManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

public class CFontRenderer extends FontRenderer {
    private static final int[] COLOR_CODES = new int[32];
    private static final String COLOR_CODE_CHARACTERS = "0123456789abcdefklmnor"; // Used for § parsing

    // Internal constants for texture atlas
    private static final int ATLAS_GRID_SIZE = 16; // 16x16 characters per texture
    private static final int ATLAS_CHAR_HEIGHT_PADDING = 5; // Extra padding for characters in the atlas texture

    static {
        for (int i = 0; i < 32; ++i) {
            int base = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + base;
            int green = (i >> 1 & 1) * 170 + base;
            int blue = (i & 1) * 170 + base;
            if (i == 6) { // Gold color adjustment
                red += 85;
            }

            if (i >= 16) { // Darker versions
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            COLOR_CODES[i] = (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
        }
    }

    private final Font font;
    @Getter
    private final float fontSize; // Font's point size

    // Character dimensions (actual size, before 0.5x scaling)
    private final int charActualWidth;
    private final int charActualHeight;

    // Texture atlas dimensions
    private final int atlasTextureWidth;
    private final int atlasTextureHeight;

    // Caches:
    // charWidths_internal[charPage][charInPage]
    private final byte[][] charWidths_internal = new byte[256][];
    // textureIDs[charPage]
    private final int[] textureIDs = new int[256];

    private final FontRenderContext fontRenderContext = new FontRenderContext(new AffineTransform(), true, true);

    public CFontRenderer(Font font) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
                Minecraft.getMinecraft().getTextureManager(), false);
        this.font = font;
        this.fontSize = font.getSize2D();

        Arrays.fill(textureIDs, -1); // Mark all textures as not generated

        Rectangle2D maxBounds = font.getMaxCharBounds(fontRenderContext);
        this.charActualWidth = (int) Math.ceil(maxBounds.getWidth());
        this.charActualHeight = (int) Math.ceil(maxBounds.getHeight());

        if (charActualWidth > 127 || charActualHeight > 127) { // char widths are stored as bytes
            throw new IllegalArgumentException("Font size too large! Max character width/height is 127.");
        }

        this.atlasTextureWidth = calculateNextPowerOfTwo(this.charActualWidth * ATLAS_GRID_SIZE);
        this.atlasTextureHeight = calculateNextPowerOfTwo((this.charActualHeight + ATLAS_CHAR_HEIGHT_PADDING) * ATLAS_GRID_SIZE);
        FONT_HEIGHT = getFontHeight();
    }

    public float drawCenteredString(String text, float x, float y, int color) {
        return drawString(text, (int) (x - getStringWidth(text) / 2f), (int) y, color);
    }

    public float drawCenteredStringNoFormat(String text, float x, float y, int color) {
        return drawStringNoFormat(text, x - getStringWidth(text) / 2.0f, y, color, false);
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, int color) {
        drawStringWithShadow(text, x - getStringWidth(text) / 2.0f, y, color);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        // Draw shadow (darkened and offset)
        drawString(text, x + 0.5f, y + 0.5f, color, true);
        // Draw main text
        return drawString(text, x, y, color, false);
    }
    
    // Overload for double arguments
    public void drawStringWithShadow(String text, double x, double y, int color) {
        drawStringWithShadow(text, (float) x, (float) y, color);
    }

    // Another shadow variant from original code
    public float drawStringWithShadow(String text, double x, double y, double shadowOffset, int color) {
        float shadowWidth = this.drawString(text, (float) (x + shadowOffset), (float) (y + shadowOffset), color, true);
        return Math.max(shadowWidth, this.drawString(text, (float) x, (float) y, color, false));
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean shadow) {
        // EventText event = new EventText(text);
        // Client.instance.eventManager.call(event);
        String finalText = text;
        if (finalText == null || finalText.isEmpty()) {
            return 0;
        }
        return drawStringInternal(finalText, x, y, color, shadow, false);
    }

    public final int drawStringNoFormat(String text, float x, float y, int color, boolean shadow) {
        // EventText event = new EventText(text);
        // Client.instance.eventManager.call(event);
        String finalText = text;
        if (finalText == null || finalText.isEmpty()) {
            return 0;
        }
        return drawStringInternal(finalText, x, y, color, shadow, true);
    }
    
    private int drawStringInternal(String text, float x, float y, int initialColor, boolean shadow, boolean noFormatCodes) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        text = DrawTextHookManager.hookMethod(text).getDisplayText();


        // Apply transformations:
        // Original logic: y_orig = y_orig - 2; x_render = x_orig * 2; y_render = y_orig * 2; y_render = y_render - 2; then glScale(0.5)
        // Effective final screen Y: inputY - 3
        // Effective final screen X: inputX
        // Internal coordinates are 2x, then scaled down by 0.5
        float currentX = x * 2.0f;
        float drawY = (y - 2.0f) * 2.0f - 2.0f; // This matches original y transform sequence

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int currentColor = initialColor;
        if (shadow) {
            currentColor = (currentColor & 0xFCFCFC) >> 2 | currentColor & 0xFF000000;
        }

        float alpha = ((currentColor >> 24) & 0xFF) / 255.0f;
        if (alpha == 0 && (currentColor & 0x00FFFFFF) != 0) alpha = 1.0f; // Prevent fully transparent if color exists but alpha is 0
        else if (alpha == 0 && (currentColor & 0x00FFFFFF) == 0) alpha = 1.0f; // Default to opaque if color is black with 0 alpha (common initialColor -1)

        float red = ((currentColor >> 16) & 0xFF) / 255.0f;
        float green = ((currentColor >> 8) & 0xFF) / 255.0f;
        float blue = (currentColor & 0xFF) / 255.0f;
        GlStateManager.color(red, green, blue, alpha);

        int currentTexturePage = -1;
        boolean inQuadMode = false;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (character == '§' && i + 1 < text.length()) {
                if (noFormatCodes) {
                    i++; // Skip format char, but don't process color
                    continue;
                }
                
                int colorIndex = COLOR_CODE_CHARACTERS.indexOf(Character.toLowerCase(text.charAt(i + 1)));
                i++; // Consume format char

                if (colorIndex != -1) {
                    if (colorIndex < 16) { // Standard color codes 0-f
                        int newColor = COLOR_CODES[colorIndex + (shadow ? 16 : 0)];
                        red = ((newColor >> 16) & 0xFF) / 255.0f;
                        green = ((newColor >> 8) & 0xFF) / 255.0f;
                        blue = (newColor & 0xFF) / 255.0f;
                        // Alpha remains from initialColor or shadow modification
                    } else {
                        // Minecraft's k-o (obfuscated, bold, etc.) are not handled by custom font renderer typically
                        // r (reset) should reset to initial color
                        if (colorIndex == 21) { // 'r' for reset
                           int resetColor = initialColor;
                           if (shadow) {
                                resetColor = (resetColor & 0xFCFCFC) >> 2 | resetColor & 0xFF000000;
                           }
                           red = ((resetColor >> 16) & 0xFF) / 255.0f;
                           green = ((resetColor >> 8) & 0xFF) / 255.0f;
                           blue = (resetColor & 0xFF) / 255.0f;
                        }
                    }
                    if (inQuadMode) {
                        glEnd(); // End previous batch before color change
                        inQuadMode = false;
                    }
                    GlStateManager.color(red, green, blue, alpha);
                }
                continue;
            }

            int charPage = character >> 8; // High byte determines the character page/texture
            int charInPage = character & 0xFF; // Low byte is the char index within the page

            if (charPage != currentTexturePage) {
                if (inQuadMode) {
                    glEnd();
                }
                GlStateManager.bindTexture(getOrGenerateTextureID(charPage));
                currentTexturePage = charPage;
                glBegin(GL_QUADS); // Start new quad mode
                inQuadMode = true;
            } else if (!inQuadMode) { // Same texture page, but quads were ended (e.g. by color change)
                 glBegin(GL_QUADS);
                 inQuadMode = true;
            }


            byte charRenderWidth = getOrGenerateCharWidths(charPage)[charInPage];
            renderCharQuad(currentX, drawY, charInPage, charRenderWidth);
            currentX += charRenderWidth;
        }

        if (inQuadMode) {
            glEnd();
        }
        
        GlStateManager.popMatrix();
        return (int) ((currentX / 2.0f) - (x)); // Return width drawn in scaled units
    }

    private void renderCharQuad(float x, float y, int charInPageID, int charRenderWidth) {
        int texX = (charInPageID % ATLAS_GRID_SIZE) * charActualWidth;
        int texY = (charInPageID / ATLAS_GRID_SIZE) * (charActualHeight + ATLAS_CHAR_HEIGHT_PADDING);

        double u1 = (double) texX / atlasTextureWidth;
        double v1 = (double) texY / atlasTextureHeight;
        double u2 = (double) (texX + charRenderWidth) / atlasTextureWidth;
        double v2 = (double) (texY + charActualHeight) / atlasTextureHeight;

        glTexCoord2d(u1, v1); glVertex2f(x, y);
        glTexCoord2d(u1, v2); glVertex2f(x, y + charActualHeight);
        glTexCoord2d(u2, v2); glVertex2f(x + charRenderWidth, y + charActualHeight);
        glTexCoord2d(u2, v1); glVertex2f(x + charRenderWidth, y);
    }


    @Override
    public int getCharWidth(char character) {
        // This method is expected to return width after 0.5 scaling
        return getCharActualWidth(character) / 2;
    }

    private int getCharActualWidth(char character) {
        if (character == '§') {
            return 0;
        }
        int charPage = character >> 8;
        int charInPage = character & 0xFF;
        return getOrGenerateCharWidths(charPage)[charInPage];
    }

    @Override
    public int getStringWidth(String text) {
        // EventText event = new EventText(text);
        // Client.instance.eventManager.call(event);
        String finalText = DrawTextHookManager.hookMethod(text).getDisplayText();

        if (finalText == null || finalText.isEmpty()) {
            return 0;
        }

        int totalWidth = 0;
        for (int i = 0; i < finalText.length(); i++) {
            char character = finalText.charAt(i);
            if (character == '§' && i + 1 < finalText.length()) {
                i++; // Skip color code character
                continue;
            }
            totalWidth += getCharActualWidth(character);
        }
        return totalWidth / 2; // Scaled width
    }
    
    @Override
    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    @Override
    public String trimStringToWidth(String text, int maxWidth, boolean reverse) {
        // maxWidth is in scaled units, so convert to actual internal units
        int actualMaxWidth = maxWidth * 2;
        StringBuilder sb = new StringBuilder();
        int currentWidth = 0;

        int  startIndex = reverse ? text.length() - 1 : 0;
        int  endIndex   = reverse ? -1 : text.length();
        int  step       = reverse ? -1 : 1;

        for (int i = startIndex; i != endIndex; i += step) {
            char character = text.charAt(i);
            int charWidth = 0;

            if (character == '§' && (i + step) != endIndex && (i + step) >=0 ) {
                char nextChar = text.charAt(i + step);
                if (COLOR_CODE_CHARACTERS.indexOf(Character.toLowerCase(nextChar)) != -1) {
                    // It's a color code. Does it fit? If so, include both § and the code char.
                    // Color codes themselves have zero width.
                    if (currentWidth <= actualMaxWidth) { // No width added, just check if we can add more chars
                        if (reverse) {
                            sb.insert(0, nextChar).insert(0, character);
                        } else {
                            sb.append(character).append(nextChar);
                        }
                    } else {
                        break; // Cannot fit even zero-width color codes if max width is already met or exceeded
                    }
                    i += step; // also consume the color char
                    continue; 
                } else {
                    // § not followed by valid color code, treat as normal char (width from getCharActualWidth)
                    charWidth = getCharActualWidth(character); // Likely 0 for '§' itself
                }
            } else {
                 charWidth = getCharActualWidth(character);
            }

            if (currentWidth + charWidth > actualMaxWidth) {
                break;
            }
            currentWidth += charWidth;
            if (reverse) {
                sb.insert(0, character);
            } else {
                sb.append(character);
            }
        }
        return sb.toString();
    }


    public int getFontHeight() {
        // Corresponds to scaled height
        return charActualHeight / 2;
    }

    public int getHeight() {
        return getFontHeight();
    }

    public double getStringHeight() { // From original
        return getFontHeight();
    }

    private int getOrGenerateTextureID(int charPageID) {
        if (textureIDs[charPageID] == -1) {
            textureIDs[charPageID] = generateAtlasTexture(charPageID);
        }
        return textureIDs[charPageID];
    }

    private byte[] getOrGenerateCharWidths(int charPageID) {
        if (charWidths_internal[charPageID] == null) {
            charWidths_internal[charPageID] = generateCharWidthsMap(charPageID);
        }
        return charWidths_internal[charPageID];
    }

    private int generateAtlasTexture(int charPageID) {
        int textureID = glGenTextures();
        int charOffset = charPageID << 8; // Base character code for this page

        BufferedImage atlasImage = new BufferedImage(atlasTextureWidth, atlasTextureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) atlasImage.getGraphics();

        // Setup graphics for font rendering
        g2d.setFont(this.font);
        g2d.setColor(new Color(0, 0, 0, 0)); // Fully transparent background
        g2d.fillRect(0, 0, atlasTextureWidth, atlasTextureHeight);
        g2d.setColor(Color.WHITE); // Draw characters in white

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        FontMetrics fontMetrics = g2d.getFontMetrics();

        for (int y = 0; y < ATLAS_GRID_SIZE; y++) {
            for (int x = 0; x < ATLAS_GRID_SIZE; x++) {
                int charIndexInPage = (y * ATLAS_GRID_SIZE) + x;
                char character = (char) (charOffset + charIndexInPage);
                String charStr = String.valueOf(character);
                
                // Calculate position in atlas
                int drawX = x * charActualWidth;
                int drawY = y * (charActualHeight + ATLAS_CHAR_HEIGHT_PADDING) + fontMetrics.getAscent();
                
                g2d.drawString(charStr, drawX, drawY);
            }
        }
        g2d.dispose();

        // Upload to OpenGL
        GlStateManager.bindTexture(textureID); // bind by new ID
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // Linear filtering for smoother scaling
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, atlasTextureWidth, atlasTextureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageToByteBuffer(atlasImage));
        
        return textureID;
    }

    private byte[] generateCharWidthsMap(int charPageID) {
        int charOffset = charPageID << 8;
        byte[] widths = new byte[256]; // One page has 256 characters
        for (int i = 0; i < widths.length; i++) {
            char character = (char) (charOffset + i);
            widths[i] = (byte) Math.round(font.getStringBounds(String.valueOf(character), fontRenderContext).getWidth());
        }
        return widths;
    }

    private int calculateNextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power *= 2;
        }
        return power;
    }

    private static ByteBuffer imageToByteBuffer(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        // This specific bit manipulation (pixel << 8 | pixel >> 24 & 0xFF) converts ARGB to RRGGBBAA (effectively ABGR byte order if read as int)
        // It's common for OpenGL texture uploads with GL_RGBA when data source is ARGB int array.
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * pixels.length);
        for (int pixel : pixels) {
            buffer.putInt(pixel << 8 | (pixel >> 24 & 0xFF));
        }
        buffer.flip();
        return buffer;
    }

    public float getMiddleOfBox(float height) { // from original
        return height / 2f - getFontHeight() / 2f;
    }

    public final void drawLimitedString(String text, float x, float y, int color, float maxWidth) {
        drawLimitedStringWithAlpha(text, x, y, color, ((color >> 24) & 0xFF) / 255f, maxWidth);
    }

    public final void drawLimitedStringWithAlpha(String text, float x, float y, int initialColor, float alphaParam, float maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return;
        }

        float actualMaxWidth = maxWidth * 2.0f;
        
        float currentX = x * 2.0f;
        float originalX = currentX;
        float drawY = (y - 2.0f) * 2.0f - 2.0f;
        float currentLineRenderedWidth = 0;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        final boolean wasBlendEnabled = glGetBoolean(GL_BLEND); // Check if blend was already enabled
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int currentColorInt = initialColor;
        // Apply alphaParam to the initial color's alpha channel
        currentColorInt = (currentColorInt & 0x00FFFFFF) | (((int)(alphaParam * 255.0f) & 0xFF) << 24);

        float r = ((currentColorInt >> 16) & 0xFF) / 255.0f;
        float g = ((currentColorInt >> 8) & 0xFF) / 255.0f;
        float b = (currentColorInt & 0xFF) / 255.0f;
        float currentAlphaGL = ((currentColorInt >> 24) & 0xFF) / 255.0f;
        if (currentAlphaGL == 0 && (currentColorInt & 0x00FFFFFF) != 0) currentAlphaGL = 1.0f;
        else if (currentAlphaGL == 0 && (currentColorInt & 0x00FFFFFF) == 0) currentAlphaGL = 1.0f;

        GlStateManager.color(r, g, b, currentAlphaGL);
        
        int currentTexturePage = -1;
        boolean inQuadMode = false;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (character == '\r') {
                currentX = originalX;
                currentLineRenderedWidth = 0;
                continue;
            }
            if (character == '\n') {
                drawY += getFontHeight() * 2.0f; // Increment by charActualHeight (scaled font height * 2)
                currentX = originalX;
                currentLineRenderedWidth = 0;
                if (inQuadMode) {
                    glEnd();
                    inQuadMode = false;
                }
                continue;
            }

            if (character == '§' && i + 1 < text.length()) {
                int colorCodeIndex = COLOR_CODE_CHARACTERS.indexOf(Character.toLowerCase(text.charAt(i + 1)));
                i++; 

                if (colorCodeIndex != -1) {
                     if (colorCodeIndex < 16) { // Color change
                        currentColorInt = COLOR_CODES[colorCodeIndex];
                        // Preserve the original alphaParam
                        currentColorInt = (currentColorInt & 0x00FFFFFF) | (((int)(alphaParam * 255.0f) & 0xFF) << 24);
                     } else if (colorCodeIndex == 21) { // 'r' for reset
                        currentColorInt = initialColor; // Reset to original color
                        // And re-apply original alphaParam
                        currentColorInt = (currentColorInt & 0x00FFFFFF) | (((int)(alphaParam * 255.0f) & 0xFF) << 24);
                     }
                    // Other format codes (bold, italic, etc.) are ignored for color processing here.
                    
                    r = ((currentColorInt >> 16) & 0xFF) / 255.0f;
                    g = ((currentColorInt >> 8) & 0xFF) / 255.0f;
                    b = (currentColorInt & 0xFF) / 255.0f;
                    // currentAlphaGL (derived from alphaParam) remains the same
                    if (inQuadMode) {
                        glEnd();
                        inQuadMode = false;
                    }
                    GlStateManager.color(r, g, b, currentAlphaGL);
                }
                continue;
            }
            
            int charPage = character >> 8;
            int charInPage = character & 0xFF;
            byte charRenderWidth = getOrGenerateCharWidths(charPage)[charInPage];

            if (currentLineRenderedWidth + charRenderWidth > actualMaxWidth) {
                if (inQuadMode) {
                    glEnd();
                    inQuadMode = false;
                }
                break; 
            }

            if (charPage != currentTexturePage) {
                if (inQuadMode) glEnd();
                GlStateManager.bindTexture(getOrGenerateTextureID(charPage));
                currentTexturePage = charPage;
                glBegin(GL_QUADS);
                inQuadMode = true;
            } else if (!inQuadMode) {
                 glBegin(GL_QUADS);
                 inQuadMode = true;
            }
            
            renderCharQuad(currentX, drawY, charInPage, charRenderWidth);
            currentX += charRenderWidth;
            currentLineRenderedWidth += charRenderWidth;
        }

        if (inQuadMode) {
            glEnd();
        }

        if (!wasBlendEnabled) { // Only disable blend if it wasn't enabled before this method
            glDisable(GL_BLEND);
        }
        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f); // Reset color state
    }


    public final void drawOutlinedString(String text, float x, float y, int internalColor, int externalColor) {
        // Using shadow version for outline parts to give a slightly darker/offset look, as per original logic
        drawString(text, x - 0.5f, y, externalColor, true); // shadow=true
        drawString(text, x + 0.5f, y, externalColor, true); // shadow=true
        drawString(text, x, y - 0.5f, externalColor, true); // shadow=true
        drawString(text, x, y + 0.5f, externalColor, true); // shadow=true
        // Draw main text on top, not shadowed
        drawString(text, x, y, internalColor, false); // shadow=false
    }


    @Override
    protected final void finalize() throws Throwable {
        for (int textureId : textureIDs) {
            if (textureId != -1)
                glDeleteTextures(textureId);
        }
    }

    /**
     * Call this method during client shutdown to properly release OpenGL texture resources.
     */
    public void cleanup() {
        for (int i = 0; i < textureIDs.length; i++) {
            if (textureIDs[i] != -1) {
                GlStateManager.deleteTexture(textureIDs[i]);
                textureIDs[i] = -1;
            }
        }
    }
}