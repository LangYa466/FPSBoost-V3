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

import java.awt.Color;

/**
 * @author LangYa466
 * @date 2025/7/13
 */
public class ModuleDetailPanel {
    private Module module;
    private int x, y, width, height;
    private boolean visible = false;
    private float animationProgress = 0.0f;
    private long lastAnimationTime;
    
    // 滑动相关
    private boolean isDraggingSlider = false;
    private Value<?> draggedValue = null;
    private int dragStartX = 0;
    private float dragStartValue = 0.0f;
    
    // 颜色主题
    private static final int PANEL_BACKGROUND = new Color(25, 35, 55, 220).getRGB();
    private static final int HEADER_COLOR = new Color(100, 150, 255, 180).getRGB();
    private static final int VALUE_BACKGROUND = new Color(35, 45, 65, 200).getRGB();
    private static final int VALUE_HOVER = new Color(45, 55, 75, 220).getRGB();
    private static final int TEXT_COLOR = new Color(220, 230, 255).getRGB();
    private static final int DISABLED_COLOR = new Color(100, 100, 120).getRGB();
    private static final int ACCENT_COLOR = new Color(80, 130, 235).getRGB();
    private static final int SLIDER_BG_COLOR = new Color(60, 60, 80).getRGB();
    private static final int SLIDER_FILL_COLOR = new Color(100, 150, 255).getRGB();
    private static final int SLIDER_HOVER_COLOR = new Color(120, 170, 255).getRGB();
    
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
        int valueY = y + 35;
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
            FontUtil.font18.drawString(value.getName(), valueX + 5, valueY + 5, TEXT_COLOR);
            
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
        
        // 显示当前值（根据增量调整精度）
        String valueStr;
        if (inc.floatValue() >= 1.0f) {
            valueStr = String.format("%.0f", value.floatValue());
        } else if (inc.floatValue() >= 0.1f) {
            valueStr = String.format("%.1f", value.floatValue());
        } else {
            valueStr = String.format("%.2f", value.floatValue());
        }
        FontUtil.font18.drawString(valueStr, x + 5, y + 5, TEXT_COLOR);
        
        // 显示范围信息（悬停时）
        if (hovered) {
            String rangeStr = String.format("%.0f-%.0f", min.floatValue(), max.floatValue());
            FontUtil.font18.drawString(rangeStr, x + 5, y + height - 12, DISABLED_COLOR);
        }
        
        // 绘制滑块背景
        int sliderHeight = 6;
        int sliderY = y + height - 10;
        int sliderWidth = width - 10;
        int sliderX = x + 5;
        
        // 绘制滑块轨道
        RenderUtil.drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, SLIDER_BG_COLOR);
        RenderUtil.drawOutline(sliderX, sliderY, sliderWidth, sliderHeight, new Color(80, 80, 100).getRGB());
        
        // 计算滑块位置和进度
        float progress = (value.floatValue() - min.floatValue()) / (max.floatValue() - min.floatValue());
        progress = Math.max(0, Math.min(1, progress));
        
        // 绘制填充进度
        int fillWidth = (int)(sliderWidth * progress);
        if (fillWidth > 0) {
            RenderUtil.drawRect(sliderX, sliderY, sliderX + fillWidth, sliderY + sliderHeight, SLIDER_FILL_COLOR);
        }
        
        // 绘制滑块
        int sliderSize = 12;
        int sliderPos = (int)(sliderX + progress * sliderWidth - sliderSize / 2);
        int sliderColor = (hovered || draggedValue == numberValue) ? SLIDER_HOVER_COLOR : SLIDER_FILL_COLOR;
        
        // 绘制滑块阴影
        RenderUtil.drawRect(sliderPos + 1, sliderY - 3 + 1, sliderPos + sliderSize + 1, sliderY + sliderHeight + 3 + 1, 
            new Color(0, 0, 0, 50).getRGB());
        
        // 绘制滑块主体
        RenderUtil.drawRect(sliderPos, sliderY - 3, sliderPos + sliderSize, sliderY + sliderHeight + 3, sliderColor);
        RenderUtil.drawOutline(sliderPos, sliderY - 3, sliderSize, sliderHeight + 6, new Color(150, 200, 255).getRGB());
        
        // 绘制滑块中心线
        RenderUtil.drawHorizontalLine(sliderPos + 2, sliderPos + sliderSize - 2, sliderY + sliderHeight / 2, 
            new Color(200, 220, 255).getRGB());
        
        // 绘制刻度标记（如果范围不是太大）
        if (max.floatValue() - min.floatValue() <= 20) {
            drawSliderTicks(sliderX, sliderY, sliderWidth, sliderHeight, min.floatValue(), max.floatValue());
        }
        
        // 调试信息 - 显示滑块区域边界（仅在开发模式下）
        if (hovered && cn.fpsboost.Client.isDev) {
            RenderUtil.drawOutline(sliderX, sliderY - 3, sliderWidth, sliderHeight + 6, new Color(255, 255, 0).getRGB());
        }
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
        FontUtil.font18.drawString(currentMode, x + 5, y + 5, TEXT_COLOR);
        
        // 绘制模式切换按钮
        String switchBtn = hovered ? "◀▶" : "◀▶";
        int btnColor = hovered ? ACCENT_COLOR : DISABLED_COLOR;
        FontUtil.font18.drawString(switchBtn, x + width - 25, y + 5, btnColor);
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
        
        String tooltipText = String.format("%s: %.2f (%.0f-%.0f, 步长:%.1f)", 
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
        
        boolean result = mouseX >= x && mouseX <= x + width && 
               mouseY >= y && mouseY <= y + actualHeight;
        
        // 调试信息
        if (cn.fpsboost.Client.isDev && result) {
            System.out.printf("鼠标在详情面板上: x=%d, y=%d, panelX=%d, panelY=%d, width=%d, height=%d%n", 
                mouseX, mouseY, x, y, width, actualHeight);
        }
        
        return result;
    }
    
    public boolean isCloseButtonHovered(int mouseX, int mouseY) {
        if (!visible) return false;
        
        boolean result = mouseX >= x + width - 20 && mouseX <= x + width - 5 &&
               mouseY >= y + 5 && mouseY <= y + 20;
        
        // 调试信息
        if (cn.fpsboost.Client.isDev && result) {
            System.out.println("鼠标在关闭按钮上");
        }
        
        return result;
    }
    
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || module == null) return;
        
        // 调试信息
        if (cn.fpsboost.Client.isDev) {
            System.out.printf("ModuleDetailPanel鼠标点击: x=%d, y=%d, button=%d%n", mouseX, mouseY, mouseButton);
        }
        
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
                if (cn.fpsboost.Client.isDev) {
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
                    dragStartX = mouseX;
                    dragStartValue = numberValue.getValue().floatValue();
                    
                    // 调试信息
                    if (cn.fpsboost.Client.isDev) {
                        System.out.printf("开始滑块拖拽: mouseX=%d, mouseY=%d, sliderX=%d, sliderY=%d%n", 
                            mouseX, mouseY, sliderX, sliderY);
                    }
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
                
                // 调试信息
                if (cn.fpsboost.Client.isDev) {
                    System.out.printf("滑块点击跳转: mouseX=%d, progress=%.2f, value=%.2f%n", 
                        mouseX, clickProgress, newValue);
                }
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
                    
                    // 调试信息
                    if (Client.isDev) {
                        System.out.printf("滑块拖拽: mouseX=%d, sliderX=%d, progress=%.2f, value=%.2f%n", 
                            mouseX, sliderX, dragProgress, newValue);
                    }
                    break;
                }
                
                valueY += valueHeight + 5;
            }
        }
    }
    
    public void onMouseRelease() {
        if (isDraggingSlider) {
            if (cn.fpsboost.Client.isDev) {
                System.out.println("滑块拖拽结束");
            }
        }
        isDraggingSlider = false;
        draggedValue = null;
    }
    
    public boolean isDraggingSlider() {
        return isDraggingSlider;
    }
    
    public boolean isDraggingValue() {
        return draggedValue != null;
    }
    
    // 测试方法 - 在开发模式下验证滑块功能
    public void testSliderFunctionality() {
        if (!cn.fpsboost.Client.isDev) return;
        
        System.out.println("=== 滑块功能测试 ===");
        System.out.printf("面板位置: x=%d, y=%d, width=%d, height=%d%n", x, y, width, height);
        System.out.printf("可见性: %s, 动画进度: %.2f%n", visible, animationProgress);
        
        if (module != null) {
            System.out.printf("模块: %s, Value数量: %d%n", module.getName(), module.getValues().size());
            
            for (Value<?> value : module.getValues()) {
                if (value instanceof NumberValue) {
                    NumberValue nv = (NumberValue) value;
                    System.out.printf("  NumberValue: %s = %.2f (%.0f-%.0f, 步长:%.1f)%n", 
                        nv.getName(), nv.getValue().floatValue(), 
                        nv.getMin().floatValue(), nv.getMax().floatValue(), nv.getInc().floatValue());
                }
            }
        }
        System.out.println("==================");
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void hide() {
        this.visible = false;
    }
} 