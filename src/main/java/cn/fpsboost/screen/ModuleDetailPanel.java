package cn.fpsboost.screen;

import cn.fpsboost.Client;
import cn.fpsboost.module.Module;
import cn.fpsboost.util.render.RenderUtil;
import cn.fpsboost.util.render.font.FontUtil;
import cn.fpsboost.value.Value;
import cn.fpsboost.value.impl.BooleanValue;
import cn.fpsboost.value.impl.ModeValue;
import cn.fpsboost.value.impl.NumberValue;
import cn.fpsboost.value.impl.TextValue;
import lombok.Getter;

import java.awt.Color;

/**
 * @author LangYa466
 * @date 2025/7/13
 */
public class ModuleDetailPanel {
    private Module module;
    private int x, y, width, height;
    @Getter
    private boolean visible = false;
    private float animationProgress = 0.0f;
    private long lastAnimationTime;
    
    // 滑动相关
    @Getter
    private boolean isDraggingSlider = false;
    private Value<?> draggedValue = null;

    // 颜色主题
    private static final int PANEL_BACKGROUND = new Color(25, 35, 55, 220).getRGB();
    private static final int HEADER_COLOR = new Color(100, 150, 255, 180).getRGB();
    private static final int VALUE_BACKGROUND = new Color(35, 45, 65, 200).getRGB();
    private static final int VALUE_HOVER = new Color(45, 55, 75, 220).getRGB();
    private static final int TEXT_COLOR = new Color(220, 230, 255).getRGB();
    private static final int DISABLED_COLOR = new Color(100, 100, 120).getRGB();
    private static final int ACCENT_COLOR = new Color(80, 130, 235).getRGB();

    public ModuleDetailPanel() {
        this.lastAnimationTime = System.currentTimeMillis();
    }
    
    public void setModule(Module module) {
        this.module = module;
        this.visible = module != null;
        if (visible) {
            animationProgress = 0.0f;
        }
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void draw(int mouseX, int mouseY) {
        if (!visible || module == null) return;
        
        updateAnimation();
        
        // 计算实际显示高度
        int actualHeight = (int) (height * animationProgress);
        if (actualHeight <= 0) return;
        
        // 绘制面板背景
        RenderUtil.drawRect(x, y, x + width, y + actualHeight, PANEL_BACKGROUND);
        RenderUtil.drawOutline(x, y, width, actualHeight, HEADER_COLOR);
        
        // 绘制标题栏
        drawHeader();
        
        // 绘制Value列表
        if (animationProgress > 0.3f) {
            drawValues(mouseX, mouseY, actualHeight);
        }
        
        // 绘制装饰
        drawDecorations(actualHeight);
    }
    
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastAnimationTime) / 16.67f;
        lastAnimationTime = currentTime;
        
        if (visible && animationProgress < 1.0f) {
            animationProgress += deltaTime * 0.15f;
            if (animationProgress > 1.0f) animationProgress = 1.0f;
        } else if (!visible && animationProgress > 0.0f) {
            animationProgress -= deltaTime * 0.15f;
            if (animationProgress < 0.0f) {
                animationProgress = 0.0f;
                module = null;
            }
        }
    }
    
    private void drawHeader() {
        // 标题栏背景
        RenderUtil.drawRect(x, y, x + width, y + 30, HEADER_COLOR);
        
        // 模块名称
        FontUtil.font18.drawString(module.getName(), x + 10, y + 8, TEXT_COLOR);

        // 关闭按钮
        String closeBtn = "×";
        FontUtil.font18.drawString(closeBtn, x + width - 15, y + 8, TEXT_COLOR);
    }
    
    private void drawValues(int mouseX, int mouseY, int maxHeight) {
        int valueY = y + 32;
        int valueHeight = 25;
        int valueWidth = width - 20;
        int valueX = x + 10;
        
        for (Value<?> value : module.getValues()) {
            if (valueY + valueHeight > y + maxHeight - 10) break;
            
            boolean hovered = mouseX >= valueX && mouseX <= valueX + valueWidth &&
                            mouseY >= valueY && mouseY <= valueY + valueHeight;
            
            // Value背景
            int bgColor = hovered ? VALUE_HOVER : VALUE_BACKGROUND;
            RenderUtil.drawRect(valueX, valueY, valueX + valueWidth, valueY + valueHeight, bgColor);
            
            // Value名称
            FontUtil.font18.drawString(value.getName(), valueX + 5, valueY + 8, TEXT_COLOR);
            
            // Value值
            drawValueContent(value, valueX + valueWidth - 100, valueY, 90, valueHeight, hovered);
            
            // 绘制工具提示
            if (hovered && value instanceof NumberValue) {
                drawNumberValueTooltip((NumberValue) value, mouseX, mouseY);
            }
            
            valueY += valueHeight + 5;
        }
    }
    
    private void drawValueContent(Value<?> value, int x, int y, int width, int height, boolean hovered) {
        if (value instanceof BooleanValue) {
            drawBooleanValue((BooleanValue) value, x, y, width, height, hovered);
        } else if (value instanceof NumberValue) {
            drawNumberValue((NumberValue) value, x, y, width, height, hovered);
        } else if (value instanceof ModeValue) {
            drawModeValue((ModeValue) value, x, y, width, height, hovered);
        } else if (value instanceof TextValue) {
            drawTextValue((TextValue) value, x, y, width, height, hovered);
        } else {
            // 默认显示
            String valueStr = value.getValue().toString();
            FontUtil.font18.drawString(valueStr, x + 5, y + 5, DISABLED_COLOR);
        }
    }
    
    private void drawBooleanValue(BooleanValue booleanValue, int x, int y, int width, int height, boolean hovered) {
        boolean value = booleanValue.getValue();
        
        // 绘制开关背景
        int switchBgColor = value ? ACCENT_COLOR : new Color(60, 60, 80).getRGB();
        int switchSize = 16;
        int switchX = x + width - switchSize - 5;
        int switchY = y + (height - switchSize) / 2;
        
        RenderUtil.drawRect(switchX, switchY, switchX + switchSize, switchY + switchSize, switchBgColor);
        RenderUtil.drawOutline(switchX, switchY, switchSize, switchSize, TEXT_COLOR);
    }
    
    private void drawNumberValue(NumberValue numberValue, int x, int y, int width, int height, boolean hovered) {
        Number value = numberValue.getValue();
        Number min = numberValue.getMin();
        Number max = numberValue.getMax();
        Number inc = numberValue.getInc();

        // 滑块参数
        int sliderHeight = 9;
        int sliderY = y + height / 2;
        int sliderWidth = width - 30;
        int sliderX = x + 15;

        // 计算进度
        float progress = (value.floatValue() - min.floatValue()) / (max.floatValue() - min.floatValue());
        progress = Math.max(0, Math.min(1, progress));
        int fillWidth = (int)(sliderWidth * progress);

        // 滑块轨道
        RenderUtil.drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, new Color(60, 80, 120, 180).getRGB());
        RenderUtil.drawRect(sliderX, sliderY, sliderX + fillWidth, sliderY + sliderHeight, hovered ? new Color(120, 170, 255, 220).getRGB() : new Color(100, 150, 255, 200).getRGB());

        // 滑块按钮
        int knobHeight = 9;
        int knobCenter = sliderX + fillWidth;
        float knobWidth = 3;
        float knobX1 = knobCenter - knobWidth / 2F;
        float knobX2 = knobCenter + knobWidth / 2F;
        float knobY1 = sliderY + sliderHeight / 2F - knobHeight / 2F;
        float knobY2 = sliderY + sliderHeight / 2F + knobHeight / 2F;

        RenderUtil.drawRect(knobX1, knobY1, knobX2, knobY2, hovered ? new Color(120, 170, 255).getRGB() : new Color(100, 150, 255).getRGB());
        RenderUtil.drawRect(knobX1, knobY1, knobX2, knobY2, Color.WHITE.getRGB());

        // 当前值
        String valueStr = inc.floatValue() >= 1.0f ? String.format("%.0f", value.floatValue())
                      : inc.floatValue() >= 0.1f ? String.format("%.1f", value.floatValue())
                      : String.format("%.2f", value.floatValue());
        int valueWidth = FontUtil.font18.getStringWidth(valueStr);
        FontUtil.font18.drawString(valueStr, knobCenter - valueWidth / 2, sliderY - 13, new Color(120, 200, 255).getRGB());

        // 最小值/最大值
        String minStr = String.format("%.0f", min.floatValue());
        String maxStr = String.format("%.0f", max.floatValue());
        FontUtil.font16.drawString(minStr, sliderX - FontUtil.font16.getStringWidth(minStr) - 2, sliderY + sliderHeight / 2 - 4, new Color(150, 150, 180).getRGB());
        FontUtil.font16.drawString(maxStr, sliderX + sliderWidth + 4, sliderY + sliderHeight / 2 - 4, new Color(150, 150, 180).getRGB());
    }
    
    private void drawSliderTicks(int sliderX, int sliderY, int sliderWidth, int sliderHeight, float min, float max) {
        int tickCount = (int)(max - min) + 1;
        if (tickCount > 20) return; // 太多刻度不绘制
        
        int tickColor = new Color(120, 140, 160).getRGB();
        
        for (int i = 0; i <= tickCount; i++) {
            float tickProgress = (float)i / tickCount;
            int tickX = (int)(sliderX + tickProgress * sliderWidth);
            
            // 绘制刻度线
            RenderUtil.drawVerticalLine(tickX, sliderY - 2, sliderY + sliderHeight + 2, tickColor);
        }
    }
    
    private void drawModeValue(ModeValue modeValue, int x, int y, int width, int height, boolean hovered) {
        String currentMode = modeValue.getValue();
        String[] modes = modeValue.getModes();
        
        // 显示当前模式
        FontUtil.font18.drawString(currentMode, x + 5, y + 8, TEXT_COLOR);
        
        // 绘制模式切换按钮
        String switchBtn = hovered ? "◀▶" : "◀▶";
        int btnColor = hovered ? ACCENT_COLOR : DISABLED_COLOR;
        FontUtil.font18.drawString(switchBtn, x + width - 25, y + 8, btnColor);
    }
    
    private void drawTextValue(TextValue textValue, int x, int y, int width, int height, boolean hovered) {
        String value = textValue.getValue();
        
        // 显示文本值
        String displayText = value.length() > 8 ? value.substring(0, 8) + "..." : value;
        FontUtil.font18.drawString(displayText, x + 5, y + 5, TEXT_COLOR);
        
        // 绘制编辑指示器
        if (hovered) {
            FontUtil.font18.drawString("✎", x + width - 15, y + 5, ACCENT_COLOR);
        }
    }
    
    private void drawNumberValueTooltip(NumberValue numberValue, int mouseX, int mouseY) {
        Number value = numberValue.getValue();
        Number min = numberValue.getMin();
        Number max = numberValue.getMax();
        Number inc = numberValue.getInc();
        
        String tooltipText = String.format("%s: %.2f (%.0f-%.0f, Inc:%.1f)",
            numberValue.getName(), value.floatValue(), min.floatValue(), max.floatValue(), inc.floatValue());
        
        int tooltipWidth = FontUtil.font18.getStringWidth(tooltipText) + 10;
        int tooltipHeight = 20;
        int tooltipX = mouseX - tooltipWidth - 10;
        int tooltipY = mouseY - tooltipHeight - 10;
        
        // 确保工具提示不超出屏幕
        if (tooltipX < 0) {
            tooltipX = mouseX + 10;
        }
        if (tooltipY < 0) {
            tooltipY = mouseY + 10;
        }
        
        // 绘制工具提示背景
        RenderUtil.drawRect(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 
            new Color(20, 30, 50, 200).getRGB());
        RenderUtil.drawOutline(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 
            new Color(100, 150, 255).getRGB());
        
        // 绘制文本
        FontUtil.font18.drawString(tooltipText, tooltipX + 5, tooltipY + 5, new Color(220, 230, 255).getRGB());
    }
    
    private void drawDecorations(int maxHeight) {
        // 绘制右侧装饰线
        int decorationColor = new Color(100, 150, 255, 60).getRGB();
        RenderUtil.drawVerticalLine(x + width - 1, y, y + maxHeight, decorationColor);
        
        // 绘制底部装饰
        int bottomDecorationY = y + maxHeight - 2;
        RenderUtil.drawHorizontalLine(x, x + width, bottomDecorationY, decorationColor);
    }
    
    public boolean isMouseOver(int mouseX, int mouseY) {
        if (!visible || module == null) return false;
        
        // 计算实际显示高度
        int actualHeight = (int) (height * animationProgress);
        if (actualHeight <= 0) return false;

        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y && mouseY <= y + actualHeight;
    }
    
    public boolean isCloseButtonHovered(int mouseX, int mouseY) {
        if (!visible) return false;

        return mouseX >= x + width - 20 && mouseX <= x + width - 5 &&
               mouseY >= y + 5 && mouseY <= y + 20;
    }
    
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || module == null) return;
        
        // 检查关闭按钮
        if (isCloseButtonHovered(mouseX, mouseY)) {
            visible = false;
            return;
        }
        
        // 处理Value点击
        int valueY = y + 35;
        int valueHeight = 25;
        int valueWidth = width - 20;
        int valueX = x + 10;
        
        for (Value<?> value : module.getValues()) {
            boolean hovered = mouseX >= valueX && mouseX <= valueX + valueWidth &&
                            mouseY >= valueY && mouseY <= valueY + valueHeight;
            
            if (hovered) {
                if (Client.isDev) {
                    System.out.printf("点击在Value上: %s, 类型: %s%n", value.getName(), value.getClass().getSimpleName());
                }
                handleValueClick(value, mouseX, mouseY, mouseButton, valueX, valueY, valueWidth, valueHeight);
                break;
            }
            
            valueY += valueHeight + 5;
        }
    }
    
    public void onMouseWheel(int mouseX, int mouseY, int wheel) {
        if (!visible || module == null || wheel == 0) return;
        
        // 处理Value滚轮
        int valueY = y + 35;
        int valueHeight = 25;
        int valueWidth = width - 20;
        int valueX = x + 10;
        
        for (Value<?> value : module.getValues()) {
            boolean hovered = mouseX >= valueX && mouseX <= valueX + valueWidth &&
                            mouseY >= valueY && mouseY <= valueY + valueHeight;
            
            if (hovered && value instanceof NumberValue) {
                NumberValue numberValue = (NumberValue) value;
                Number inc = numberValue.getInc();
                
                if (wheel > 0) {
                    numberValue.increment();
                } else {
                    numberValue.decrement();
                }
                break;
            }
            
            valueY += valueHeight + 5;
        }
    }
    
    private void handleValueClick(Value<?> value, int mouseX, int mouseY, int mouseButton, int valueX, int valueY, int valueWidth, int valueHeight) {
        if (value instanceof BooleanValue) {
            ((BooleanValue) value).toggle();
        } else if (value instanceof ModeValue) {
            ((ModeValue) value).setNextValue();
        } else if (value instanceof NumberValue) {
            NumberValue numberValue = (NumberValue) value;
            
            // 计算滑块区域 - 修复坐标计算
            int sliderX = valueX + valueWidth - 100;
            int sliderY = valueY + valueHeight - 10;
            int sliderWidth = 90;
            int sliderHeight = 6;
            
            // 检查是否点击了滑块区域（扩大检测范围）
            if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth &&
                mouseY >= sliderY - 3 && mouseY <= sliderY + sliderHeight + 3) {
                
                if (mouseButton == 0) { // 左键拖拽
                    isDraggingSlider = true;
                    draggedValue = value;
                }
            } else {
                // 点击滑块区域外，直接跳转到对应位置
                float clickProgress = (float)(mouseX - sliderX) / sliderWidth;
                clickProgress = Math.max(0, Math.min(1, clickProgress));
                
                float min = numberValue.getMin().floatValue();
                float max = numberValue.getMax().floatValue();
                float newValue = min + (max - min) * clickProgress;
                
                // 根据增量调整值
                Number inc = numberValue.getInc();
                if (inc.floatValue() > 0) {
                    newValue = Math.round(newValue / inc.floatValue()) * inc.floatValue();
                }
                
                numberValue.setValue(newValue);
            }
        }
    }
    
    public void onMouseDrag(int mouseX, int mouseY) {
        if (isDraggingSlider && draggedValue instanceof NumberValue) {
            NumberValue numberValue = (NumberValue) draggedValue;
            
            // 计算滑块区域
            int valueY = y + 35;
            int valueHeight = 25;
            int valueWidth = width - 20;
            int valueX = x + 10;
            
            // 找到当前拖拽的Value位置
            for (Value<?> value : module.getValues()) {
                if (value == draggedValue) {
                    int sliderX = valueX + valueWidth - 100;
                    int sliderWidth = 90;
                    
                    // 计算拖拽进度
                    float dragProgress = (float)(mouseX - sliderX) / sliderWidth;
                    dragProgress = Math.max(0, Math.min(1, dragProgress));
                    
                    // 计算新值
                    float min = numberValue.getMin().floatValue();
                    float max = numberValue.getMax().floatValue();
                    float newValue = min + (max - min) * dragProgress;
                    
                    // 根据增量调整值
                    Number inc = numberValue.getInc();
                    if (inc.floatValue() > 0) {
                        newValue = Math.round(newValue / inc.floatValue()) * inc.floatValue();
                    }
                    
                    // 确保值在范围内
                    newValue = Math.max(min, Math.min(max, newValue));
                    
                    numberValue.setValue(newValue);
                    break;
                }
                
                valueY += valueHeight + 5;
            }
        }
    }
    
    public void onMouseRelease() {
        if (isDraggingSlider) {
            if (Client.isDev) {
                System.out.println("滑块拖拽结束");
            }
        }
        isDraggingSlider = false;
        draggedValue = null;
    }

    public boolean isDraggingValue() {
        return draggedValue != null;
    }

    public void hide() {
        this.visible = false;
    }
} 