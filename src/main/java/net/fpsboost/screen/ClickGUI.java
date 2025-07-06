package net.fpsboost.screen;

import net.fpsboost.Client;
import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.util.render.font.FontUtil;
import net.fpsboost.value.Value;
import net.fpsboost.value.impl.BooleanValue;
import net.fpsboost.value.impl.ModeValue;
import net.fpsboost.value.impl.NumberValue;
import net.fpsboost.value.impl.TextValue;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @author Cursor
public class ClickGUI extends GuiScreen {
    public static final ClickGUI INSTANCE = new ClickGUI();
    
    // 现代配色方案
    private static final int BACKGROUND_COLOR = 0xE01A1A1A; // 深色背景
    private static final int CARD_BACKGROUND = 0xE0252525; // 卡片背景
    private static final int ACTIVE_COLOR = 0xFF00D4FF; // 青色
    private static final int INACTIVE_COLOR = 0xFF555555; // 灰色
    private static final int HOVER_COLOR = 0x33FFFFFF; // 悬停效果
    private static final int SHADOW_COLOR = 0x80000000; // 阴影
    private static final int TEXT_COLOR = 0xFFE0E0E0; // 文字颜色
    private static final int ACCENT_COLOR = 0xFFFF6B6B; // 强调色
    
    private final MainWindow mainWindow;
    private final int panelWidth = 160;
    private final int panelHeight = 28;
    private final int valueHeight = 24;
    private final int borderRadius = 6;
    
    // 交互状态
    private Value<?> selectedValue = null;
    private boolean isDraggingSlider = false;
    private boolean isBindingKey = false;
    private Module bindingModule = null;
    private boolean isEditingText = false;
    private TextValue editingTextValue = null;
    private boolean isModeDropdownOpen = false;
    private ModeValue openModeValue = null;
    
    public ClickGUI() {
        this.mainWindow = new MainWindow();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        mainWindow.draw(mouseX, mouseY);
        
        // 绘制绑定提示
        if (isBindingKey) {
            drawBindingOverlay();
        }
        
        // 绘制文本编辑提示
        if (isEditingText) {
            drawTextEditOverlay();
        }
        
        // 绘制模式下拉框
        if (isModeDropdownOpen && openModeValue != null) {
            drawModeDropdown(mouseX, mouseY);
        }
    }
    
    private void drawBindingOverlay() {
        String text = "请按下要绑定的按键...";
        int textWidth = FontUtil.font18.getStringWidth(text);
        int x = (width - textWidth) / 2;
        int y = height / 2 - 20;
        
        drawRoundedRect(x - 20, y - 15, x + textWidth + 20, y + 25, borderRadius, BACKGROUND_COLOR);
        FontUtil.font18.drawString(text, x, y, TEXT_COLOR);
    }
    
    private void drawTextEditOverlay() {
        String text = "正在编辑: " + editingTextValue.getDisplayName();
        String valueText = "当前值: " + editingTextValue.getValue();
        String hint = "按Enter确认，按Esc取消";
        
        int textWidth = Math.max(FontUtil.font18.getStringWidth(text), 
                               Math.max(FontUtil.font18.getStringWidth(valueText), 
                                      FontUtil.font18.getStringWidth(hint)));
        int x = (width - textWidth) / 2;
        int y = height / 2 - 30;
        
        drawRoundedRect(x - 20, y - 15, x + textWidth + 20, y + 45, borderRadius, BACKGROUND_COLOR);
        FontUtil.font18.drawString(text, x, y, TEXT_COLOR);
        FontUtil.font18.drawString(valueText, x, y + 15, ACTIVE_COLOR);
        FontUtil.font18.drawString(hint, x, y + 30, INACTIVE_COLOR);
    }
    
    private void drawModeDropdown(int mouseX, int mouseY) {
        int x = mainWindow.x + 10;
        int y = mainWindow.y + mainWindow.titleHeight + 10;
        
        // 找到当前模式值的位置
        for (CategoryPanel panel : mainWindow.categoryPanels.values()) {
            if (panel.containsModeValue(openModeValue)) {
                x = panel.getModeValueX(openModeValue);
                y = panel.getModeValueY(openModeValue) + valueHeight;
                break;
            }
        }
        
        int dropdownWidth = 120;
        int dropdownHeight = openModeValue.getModes().length * 20 + 10;
        
        // 下拉框背景
        drawRoundedRect(x, y, x + dropdownWidth, y + dropdownHeight, borderRadius, CARD_BACKGROUND);
        drawRoundedRect(x, y, x + dropdownWidth, y + dropdownHeight, borderRadius, SHADOW_COLOR);
        
        // 模式选项
        int currentY = y + 5;
        for (String mode : openModeValue.getModes()) {
            boolean isSelected = mode.equals(openModeValue.getValue());
            boolean isHovered = isHover(x + 2, currentY, x + dropdownWidth - 2, currentY + 18, mouseX, mouseY);
            
            int bgColor = isSelected ? ACTIVE_COLOR : (isHovered ? HOVER_COLOR : 0x00FFFFFF);
            drawRoundedRect(x + 2, currentY, x + dropdownWidth - 2, currentY + 18, 3, bgColor);
            
            FontUtil.font18.drawString(mode, x + 8, currentY + 2, isSelected ? 0xFF000000 : TEXT_COLOR);
            currentY += 20;
        }
    }
    
    // 绘制圆角矩形
    private void drawRoundedRect(int left, int top, int right, int bottom, int radius, int color) {
        // 简化的圆角矩形实现
        drawRect(left + radius, top, right - radius, bottom, color);
        drawRect(left, top + radius, left + radius, bottom - radius, color);
        drawRect(right - radius, top + radius, right, bottom - radius, color);
        
        // 圆角部分（简化处理）
        drawRect(left, top, left + radius, top + radius, color);
        drawRect(right - radius, top, right, top + radius, color);
        drawRect(left, bottom - radius, left + radius, bottom, color);
        drawRect(right - radius, bottom - radius, right, bottom, color);
    }
    
    // 绘制开关样式
    private void drawSwitch(int x, int y, int width, int height, boolean enabled) {
        int bgColor = enabled ? ACTIVE_COLOR : INACTIVE_COLOR;
        drawRoundedRect(x, y, x + width, y + height, height / 2, bgColor);
        
        int knobSize = height - 4;
        int knobX = enabled ? x + width - knobSize - 2 : x + 2;
        drawRoundedRect(knobX, y + 2, knobX + knobSize, y + height - 2, knobSize / 2, 0xFFFFFFFF);
    }
    
    // 绘制按钮样式
    private void drawButton(int x, int y, int width, int height, String text, boolean hovered) {
        int bgColor = hovered ? ACTIVE_COLOR : CARD_BACKGROUND;
        drawRoundedRect(x, y, x + width, y + height, borderRadius, bgColor);
        FontUtil.font18.drawString(text, x + (width - FontUtil.font18.getStringWidth(text)) / 2, y + (height - 8) / 2, TEXT_COLOR);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isBindingKey || isEditingText) {
            return;
        }
        
        // 检查模式下拉框点击
        if (isModeDropdownOpen && openModeValue != null) {
            if (handleModeDropdownClick(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
        
        mainWindow.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    private boolean handleModeDropdownClick(int mouseX, int mouseY, int mouseButton) {
        int x = mainWindow.x + 10;
        int y = mainWindow.y + mainWindow.titleHeight + 10;
        
        for (CategoryPanel panel : mainWindow.categoryPanels.values()) {
            if (panel.containsModeValue(openModeValue)) {
                x = panel.getModeValueX(openModeValue);
                y = panel.getModeValueY(openModeValue) + valueHeight;
                break;
            }
        }
        
        int dropdownWidth = 120;
        int dropdownHeight = openModeValue.getModes().length * 20 + 10;
        
        if (isHover(x, y, x + dropdownWidth, y + dropdownHeight, mouseX, mouseY)) {
            int currentY = y + 5;
            for (String mode : openModeValue.getModes()) {
                if (isHover(x + 2, currentY, x + dropdownWidth - 2, currentY + 18, mouseX, mouseY)) {
                    openModeValue.setValue(mode);
                    isModeDropdownOpen = false;
                    openModeValue = null;
                    return true;
                }
                currentY += 20;
            }
        }
        
        isModeDropdownOpen = false;
        openModeValue = null;
        return false;
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        isDraggingSlider = false;
        selectedValue = null;
        mainWindow.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isBindingKey) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                isBindingKey = false;
                bindingModule = null;
            } else if (keyCode != Keyboard.KEY_NONE) {
                bindingModule.setKeyCode(keyCode);
                isBindingKey = false;
                bindingModule = null;
            }
            return;
        }
        
        if (isEditingText) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                isEditingText = false;
                editingTextValue = null;
            } else if (keyCode == Keyboard.KEY_RETURN) {
                isEditingText = false;
                editingTextValue = null;
            } else if (keyCode == Keyboard.KEY_BACK) {
                String current = editingTextValue.getValue();
                if (current.length() > 0) {
                    editingTextValue.setValue(current.substring(0, current.length() - 1));
                }
            } else if (typedChar >= 32 && typedChar <= 126) {
                String current = editingTextValue.getValue();
                editingTextValue.setValue(current + typedChar);
            }
            return;
        }
        
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    private class MainWindow {
        private int x = 50, y = 50;
        private final int width = 900;
        private final int height = 600;
        private final int titleHeight = 35;
        private boolean dragging = false;

        private final Map<Category, CategoryPanel> categoryPanels = new HashMap<>();
        
        public MainWindow() {
            int panelX = x + 15;
            for (Category category : Category.values()) {
                List<Module> modules = getModulesByCategory(category);
                CategoryPanel panel = new CategoryPanel(category, panelX, y + titleHeight + 15, panelWidth, modules);
                categoryPanels.put(category, panel);
                panelX += panelWidth + 20;
            }
        }
        
        private List<Module> getModulesByCategory(Category category) {
            List<Module> modules = new ArrayList<>();
            for (Module module : Client.moduleManager.getModules().values()) {
                if (!module.isCanDisplay()) continue;
                if (module.getCategory() == category) {
                    modules.add(module);
                }
            }
            return modules;
        }
        
        public void draw(int mouseX, int mouseY) {
            // 主窗口背景 - 扁平化设计
            drawRoundedRect(x, y, x + width, y + height, borderRadius, BACKGROUND_COLOR);
            
            // 标题栏
            drawRoundedRect(x, y, x + width, y + titleHeight, borderRadius, CARD_BACKGROUND);
            
            // 标题文字
            FontUtil.font18.drawString("⚙ FPSBoost ClickGUI", x + 15, y + 8, TEXT_COLOR);
            
            // 关闭按钮
            String closeText = "✕";
            int closeX = x + width - 25;
            boolean closeHovered = isHover(closeX, y + 5, closeX + 20, y + titleHeight - 5, mouseX, mouseY);
            int closeColor = closeHovered ? ACCENT_COLOR : TEXT_COLOR;
            FontUtil.font18.drawString(closeText, closeX, y + 8, closeColor);
            
            // 绘制分类面板
            for (CategoryPanel panel : categoryPanels.values()) {
                panel.draw(mouseX, mouseY);
            }
        }
        
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            // System.out.println("MainWindow mouseClicked: x=" + mouseX + " y=" + mouseY + " button=" + mouseButton);
            
            // 检查关闭按钮
            if (mouseButton == 0 && isHover(x + width - 25, y + 5, x + width - 5, y + titleHeight - 5, mouseX, mouseY)) {
                mc.displayGuiScreen(null);
                return;
            }
            
            // 检查标题栏拖拽
            if (mouseButton == 0 && isHover(x, y, x + width, y + titleHeight, mouseX, mouseY)) {
                dragging = true;
                return;
            }
            
            // 检查分类面板点击
            for (CategoryPanel panel : categoryPanels.values()) {
                if (panel.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return;
                }
            }
        }
        
        public void mouseReleased(int mouseX, int mouseY, int state) {
            dragging = false;
            for (CategoryPanel panel : categoryPanels.values()) {
                panel.mouseReleased(mouseX, mouseY, state);
            }
        }
        
        private boolean isHover(int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
            return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
        }
    }
    
    private class CategoryPanel {
        private final Category category;
        private int x, y;
        private final int width;
        private final List<Module> modules;
        private boolean expanded = true;
        private boolean dragging = false;

        public CategoryPanel(Category category, int x, int y, int width, List<Module> modules) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
            this.modules = modules;
        }
        
        public void draw(int mouseX, int mouseY) {
            // 分类卡片背景
            drawRoundedRect(x, y, x + width, y + getTotalHeight(), borderRadius, CARD_BACKGROUND);
            
            // 分类标题栏
            int titleColor = isHover(x, y, x + width, y + panelHeight, mouseX, mouseY) ? ACTIVE_COLOR : CARD_BACKGROUND;
            drawRoundedRect(x, y, x + width, y + panelHeight, borderRadius, titleColor);
            
            // 分类名称
            FontUtil.font18.drawString(category.name(), x + 12, y + 6, TEXT_COLOR);
            
            // 展开/收起按钮
            String expandText = expanded ? "▼" : "▶";
            FontUtil.font18.drawStringWithShadow(expandText, x + width - 20, y + 6, TEXT_COLOR);
            
            if (expanded) {
                int currentY = y + panelHeight + 8;
                
                for (Module module : modules) {
                    // 模块卡片
                    int moduleColor = module.isEnabled() ? ACTIVE_COLOR : INACTIVE_COLOR;
                    boolean moduleHovered = isHover(x + 4, currentY, x + width - 4, currentY + panelHeight, mouseX, mouseY);
                    int hoverColor = moduleHovered ? HOVER_COLOR : 0x00FFFFFF;
                    
                    drawRoundedRect(x + 4, currentY, x + width - 4, currentY + panelHeight, borderRadius, moduleColor);
                    drawRoundedRect(x + 4, currentY, x + width - 4, currentY + panelHeight, borderRadius, hoverColor);
                    
                    // 模块名称
                    FontUtil.font18.drawStringWithShadow(module.getDisplayName(), x + 12, currentY + 6, TEXT_COLOR);
                    
                    // 按键绑定按钮
                    if (module.getKeyCode() != 0) {
                        String keyName = "[" + Keyboard.getKeyName(module.getKeyCode()) + "]";
                        int keyWidth = FontUtil.font18.getStringWidth(keyName) + 8;
                        drawButton(x + width - keyWidth - 8, currentY + 2, keyWidth, panelHeight - 4, keyName, false);
                    }
                    
                    currentY += panelHeight + 6;
                    
                    // 绘制模块的values
                    for (Value<?> value : module.getValues()) {
                        drawValue(value, x + 8, currentY, width - 16, mouseX, mouseY);
                        currentY += valueHeight + 4;
                    }
                }
            }
        }
        
        private String getCategoryIcon(Category category) {
            return switch (category) {
                case MISC -> "⚡";
                case RENDER -> "🎨";
                case CLIENT -> "⚙";
                case DEV -> "🔧";
                default -> "📁";
            };
        }
        
        private int getTotalHeight() {
            if (!expanded) return panelHeight;
            
            int height = panelHeight + 8;
            for (Module module : modules) {
                height += panelHeight + 6;
                height += module.getValues().size() * (valueHeight + 4);
            }
            return height;
        }
        
        private void drawValue(Value<?> value, int x, int y, int width, int mouseX, int mouseY) {
            // Value卡片背景
            boolean hovered = isHover(x, y, x + width, y + valueHeight, mouseX, mouseY);
            int bgColor = hovered ? HOVER_COLOR : 0x00FFFFFF;
            drawRoundedRect(x, y, x + width, y + valueHeight, borderRadius, bgColor);
            
            String valueName = value.getDisplayName();
            FontUtil.font18.drawString(valueName, x + 8, y + 4, TEXT_COLOR);
            
            if (value instanceof BooleanValue) {
                BooleanValue boolValue = (BooleanValue) value;
                // 开关样式
                drawSwitch(x + width - 50, y + 2, 40, valueHeight - 4, boolValue.getValue());
                
            } else if (value instanceof NumberValue) {
                NumberValue numValue = (NumberValue) value;
                String numText = String.format("%.1f", numValue.getValue().doubleValue());
                FontUtil.font18.drawString(numText, x + width - FontUtil.font18.getStringWidth(numText) - 8, y + 4, ACTIVE_COLOR);
                
                // 现代滑块
                float progress = (numValue.getValue().floatValue() - numValue.getMin().floatValue()) / 
                               (numValue.getMax().floatValue() - numValue.getMin().floatValue());
                int sliderWidth = width - 80;
                int sliderX = x + 8;
                int sliderY = y + valueHeight - 8;
                
                // 滑块轨道
                drawRoundedRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, 2, INACTIVE_COLOR);
                // 滑块填充
                drawRoundedRect(sliderX, sliderY, sliderX + (int)(sliderWidth * progress), sliderY + 4, 2, ACTIVE_COLOR);
                // 滑块手柄
                int handleX = sliderX + (int)(sliderWidth * progress) - 3;
                drawRoundedRect(handleX, sliderY - 2, handleX + 6, sliderY + 6, 3, TEXT_COLOR);
                
            } else if (value instanceof ModeValue) {
                ModeValue modeValue = (ModeValue) value;
                String modeText = modeValue.getValue();
                boolean dropdownHovered = isHover(x + width - 80, y + 2, x + width - 8, y + valueHeight - 2, mouseX, mouseY);
                
                // 模式按钮 - 左键切换，右键展开
                drawButton(x + width - 80, y + 2, 72, valueHeight - 4, modeText, dropdownHovered);
                
            } else if (value instanceof TextValue) {
                TextValue textValue = (TextValue) value;
                String text = textValue.getValue();
                if (text.length() > 15) {
                    text = text.substring(0, 15) + "...";
                }
                
                // 输入框样式
                drawRoundedRect(x + width - 100, y + 2, x + width - 8, y + valueHeight - 2, borderRadius, CARD_BACKGROUND);
                FontUtil.font18.drawString(text, x + width - 95, y + 4, TEXT_COLOR);
            }
        }
        
        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
            // System.out.println("CategoryPanel mouseClicked: x=" + mouseX + " y=" + mouseY + " button=" + mouseButton);
            
            // 检查是否点击了标题栏
            if (isHover(x, y, x + width, y + panelHeight, mouseX, mouseY)) {
                if (mouseButton == 0) {
                    // 检查是否点击了展开按钮
                    if (isHover(x + width - 25, y, x + width - 5, y + panelHeight, mouseX, mouseY)) {
                        expanded = !expanded;
                    } else {
                        dragging = true;
                    }
                }
                return true;
            }
            
            if (!expanded) return false;
            
            // 检查模块点击
            int currentY = y + panelHeight + 8;
            for (Module module : modules) {
                if (isHover(x + 4, currentY, x + width - 4, currentY + panelHeight, mouseX, mouseY)) {
                    if (mouseButton == 0) {
                        module.toggle();
                    } else if (mouseButton == 1) {
                        // 右键绑定按键
                        isBindingKey = true;
                        bindingModule = module;
                    }
                    return true;
                }
                currentY += panelHeight + 6;
                
                // 检查value点击
                for (Value<?> value : module.getValues()) {
                    if (isHover(x + 8, currentY, x + width - 8, currentY + valueHeight, mouseX, mouseY)) {
                        // System.out.println("点击到Value: " + value.getClass().getSimpleName() + " 名称: " + value.getName());
                        handleValueClick(value, mouseButton, mouseX, mouseY);
                        return true;
                    }
                    currentY += valueHeight + 4;
                }
            }
            
            return false;
        }
        
        public void mouseReleased(int mouseX, int mouseY, int state) {
            dragging = false;
        }
        
        private void handleValueClick(Value<?> value, int mouseButton, int mouseX, int mouseY) {
            // System.out.println("handleValueClick 被调用: " + value.getClass().getSimpleName() + " 鼠标按钮: " + mouseButton);
            
            if (value instanceof BooleanValue boolValue) {
                boolValue.toggle();
                
            } else if (value instanceof NumberValue numValue) {
                // System.out.println("处理 NumberValue: " + numValue.getName());
                
                // 找到当前Value的实际Y坐标
                int currentY = y + panelHeight + 8;
                int valueY = -1;
                
                for (Module module : modules) {
                    currentY += panelHeight + 6;
                    for (Value<?> moduleValue : module.getValues()) {
                        if (moduleValue == value) {
                            valueY = currentY;
                            break;
                        }
                        currentY += valueHeight + 4;
                    }
                    if (valueY != -1) break;
                }
                
                if (valueY == -1) {
                    // System.out.println("未找到Value的Y坐标");
                    return;
                }
                
                // 检查是否点击了滑块
                int sliderWidth = width - 88;
                int sliderX = x + 8;
                int sliderY = valueY + valueHeight - 8;
                
                // System.out.println("滑块区域: x=" + sliderX + " y=" + sliderY + " width=" + sliderWidth + " 鼠标: x=" + mouseX + " y=" + mouseY);
                
                if (isHover(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, mouseX, mouseY)) {
                    // 开始拖拽滑块
                    isDraggingSlider = true;
                    selectedValue = value;
                    updateSliderValue(numValue, mouseX, sliderX, sliderWidth);
                    // System.out.println("开始拖拽滑块: " + numValue.getName() + " 当前值: " + numValue.getValue());
                } else {
                    // System.out.println("未点击到滑块区域");
                }
                
            } else if (value instanceof ModeValue modeValue) {
                if (mouseButton == 0) {
                    // 左键：切换到下一个模式
                    modeValue.setNextValue();
                } else if (mouseButton == 1) {
                    // 右键：展开下拉框
                    isModeDropdownOpen = true;
                    openModeValue = modeValue;
                }
                
            } else if (value instanceof TextValue textValue) {
                if (mouseButton == 0) {
                    // 开始编辑文本
                    isEditingText = true;
                    editingTextValue = textValue;
                }
            }
        }

        public static void updateSliderValue(NumberValue numValue, int mouseX, int sliderX, int sliderWidth) {
            float progress = Math.max(0, Math.min(1, (float)(mouseX - sliderX) / sliderWidth));
            float value = numValue.getMin().floatValue() + progress * (numValue.getMax().floatValue() - numValue.getMin().floatValue());
            numValue.setValue(value);
        }
        
        public boolean containsModeValue(ModeValue modeValue) {
            for (Module module : modules) {
                for (Value<?> value : module.getValues()) {
                    if (value == modeValue) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public int getModeValueX(ModeValue modeValue) {
            return x + 8;
        }
        
        public int getModeValueY(ModeValue modeValue) {
            int currentY = y + panelHeight + 8;
            for (Module module : modules) {
                currentY += panelHeight + 6;
                for (Value<?> value : module.getValues()) {
                    if (value == modeValue) {
                        return currentY;
                    }
                    currentY += valueHeight + 4;
                }
            }
            return currentY;
        }
        
        private boolean isHover(int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
            return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
        }
        
        public boolean containsNumberValue(NumberValue numberValue) {
            for (Module module : modules) {
                for (Value<?> value : module.getValues()) {
                    if (value == numberValue) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    // 处理滑块拖拽
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDraggingSlider && selectedValue instanceof NumberValue numValue) {

            // 找到当前滑块的位置
            for (CategoryPanel panel : mainWindow.categoryPanels.values()) {
                if (panel.containsNumberValue(numValue)) {
                    // 找到当前Value的实际Y坐标
                    int currentY = panel.y + panelHeight + 8;
                    int valueY = -1;
                    
                    for (Module module : panel.modules) {
                        currentY += panelHeight + 6;
                        for (Value<?> moduleValue : module.getValues()) {
                            if (moduleValue == selectedValue) {
                                valueY = currentY;
                                break;
                            }
                            currentY += valueHeight + 4;
                        }
                        if (valueY != -1) break;
                    }
                    
                    if (valueY != -1) {
                        // 计算滑块的实际位置
                        int sliderWidth = panelWidth - 88; // 160 - 88
                        int sliderX = panel.x + 8; // x + 8
                        CategoryPanel.updateSliderValue(numValue, mouseX, sliderX, sliderWidth);
                        // System.out.println("拖拽滑块: " + numValue.getName() + " 新值: " + numValue.getValue());
                    }
                    break;
                }
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    private boolean isHover(int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }
} 