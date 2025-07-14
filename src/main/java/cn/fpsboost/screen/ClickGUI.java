package cn.fpsboost.screen;

import cn.fpsboost.Client;
import cn.fpsboost.manager.impl.ClickGUIManager;
import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;
import cn.fpsboost.util.render.RenderUtil;
import cn.fpsboost.util.render.font.FontUtil;
import lombok.Data;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.WorldRenderer;

/**
 * @author LangYa466
 * @date 2025/7/13
 */
public class ClickGUI extends GuiScreen {
    public static final ClickGUI INSTANCE = new ClickGUI();
    
    // 颜色主题 - 浅蓝色调
    private static final int PRIMARY_COLOR = new Color(100, 150, 255).getRGB();
    private static final int SECONDARY_COLOR = new Color(120, 170, 255).getRGB();
    private static final int ACCENT_COLOR = new Color(80, 130, 235).getRGB();
    private static final int BACKGROUND_COLOR = new Color(20, 30, 50, 180).getRGB();
    private static final int PANEL_BACKGROUND = new Color(30, 45, 70, 200).getRGB();
    private static final int HOVER_COLOR = new Color(140, 190, 255).getRGB();
    private static final int TEXT_COLOR = new Color(220, 230, 255).getRGB();
    private static final int DISABLED_COLOR = new Color(100, 100, 120).getRGB();
    
    // GUI组件
    private List<CategoryPanel> categoryPanels;
    private SearchBox searchBox;
    private ModuleDetailPanel detailPanel;
    private String searchText = "";
    private boolean isSearching = false;
    
    // 滚动相关
    private float scrollY = 0.0f;
    private float maxScrollY = 0.0f;
    private boolean isScrolling = false;
    private boolean isDraggingScrollBar = false;
    private int scrollBarWidth = 8;
    private int scrollBarColor = new Color(100, 150, 255, 150).getRGB();
    private int scrollBarHoverColor = new Color(120, 170, 255, 200).getRGB();
    private int scrollBarBackgroundColor = new Color(30, 45, 70, 100).getRGB();
    
    // 横向滚动变量
    private float scrollX = 0.0f;
    private float maxScrollX = 0.0f;
    
    // 动画相关
    private float animationProgress = 0.0f;
    private long lastAnimationTime;
    
    // 拖拽相关
    private boolean isDragging = false;
    private boolean isMiddleDragging = false;
    private int dragStartX, dragStartY;
    private int middleDragStartX, middleDragStartY;
    private float middleDragStartScrollY;
    private CategoryPanel draggedPanel = null;
    
    // 拖动判定相关变量
    private boolean isPanelDragPending = false;
    private CategoryPanel pendingPanel = null;
    private int pendingPressX = 0, pendingPressY = 0;
    
    public ClickGUI() {
        this.categoryPanels = new ArrayList<>();
        this.searchBox = new SearchBox(10, 10, 200, 20);
        this.detailPanel = new ModuleDetailPanel();
        this.lastAnimationTime = System.currentTimeMillis();
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        // 初始化分类面板
        if (categoryPanels.isEmpty()) {
            ScaledResolution sr = new ScaledResolution(mc);
            int startX = 50;
            int startY = 80;
            int panelWidth = 180;
            int panelHeight = 250;
            int spacing = 20;
            
            for (Category category : Category.values()) {
                if (category == Category.DEV && !Client.isDev) continue;
                
                // 尝试加载保存的配置
                ClickGUIManager.PanelConfig config = Client.clickGUIManager.getPanelConfig(category.name());
                int panelX = config.x > 0 ? config.x : startX;
                int panelY = config.y > 0 ? config.y : startY;
                
                CategoryPanel panel = new CategoryPanel(
                    category, 
                    panelX, 
                    panelY, 
                    panelWidth, 
                    panelHeight
                );
                panel.setExpanded(config.expanded);
                categoryPanels.add(panel);
                
                startX += panelWidth + spacing;
            }
        }
        
        // 重置滚动
        scrollY = 0.0f;
        scrollX = 0.0f; // 重置横向滚动
        
        // 计算最大滚动距离
        calculateMaxScroll();
        
        // 重置动画
        animationProgress = 0.0f;
        lastAnimationTime = System.currentTimeMillis();
        
        // 调试信息
        if (cn.fpsboost.Client.isDev) {
            System.out.println("ClickGUI初始化完成");
            // 测试滚动功能
            testScrollFunctionality();
            // 测试展开/收起功能
            testExpandCollapseFunctionality();
        }
    }
    
    private void calculateMaxScroll() {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenHeight = sr.getScaledHeight();
        int screenWidth = sr.getScaledWidth();
        if (categoryPanels.isEmpty()) {
            maxScrollY = 0;
            maxScrollX = 0;
            return;
        }
        // 纵向内容高度
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        // 横向内容宽度
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        for (CategoryPanel panel : categoryPanels) {
            minY = Math.min(minY, panel.getY());
            maxY = Math.max(maxY, panel.getY() + panel.getHeight());
            minX = Math.min(minX, panel.getX());
            maxX = Math.max(maxX, panel.getX() + panel.getWidth());
        }
        int contentHeight = maxY - minY + 50; // 50为底部边距
        int contentWidth = maxX - minX + 50;  // 50为右侧边距
        maxScrollY = Math.max(0, contentHeight - screenHeight + 100);
        maxScrollX = Math.max(0, contentWidth - screenWidth + 100);
        if (scrollY > maxScrollY) scrollY = maxScrollY;
        if (scrollX > maxScrollX) scrollX = maxScrollX;
        if (cn.fpsboost.Client.isDev) {
            System.out.printf("计算滚动范围: contentHeight=%d, screenHeight=%d, maxScrollY=%.1f, contentWidth=%d, screenWidth=%d, maxScrollX=%.1f\n", 
                contentHeight, screenHeight, maxScrollY, contentWidth, screenWidth, maxScrollX);
        }
    }
    
    public void updateScrollRange() {
        calculateMaxScroll();
    }
    
    // 强制重新计算滚动范围
    public void forceUpdateScrollRange() {
        calculateMaxScroll();
        if (cn.fpsboost.Client.isDev) {
            System.out.println("强制更新滚动范围");
        }
    }
    
    // 测试滚动功能
    public void testScrollFunctionality() {
        if (!cn.fpsboost.Client.isDev) return;
        
        System.out.println("=== 滚动功能测试 ===");
        System.out.printf("当前滚动位置: %.1f%n", scrollY);
        System.out.printf("最大滚动距离: %.1f%n", maxScrollY);
        System.out.printf("面板数量: %d%n", categoryPanels.size());
        
        for (int i = 0; i < categoryPanels.size(); i++) {
            CategoryPanel panel = categoryPanels.get(i);
            System.out.printf("面板 %d: %s at (%d, %d), 高度: %d, 展开: %s%n", 
                i, panel.getCategory().name(), panel.getX(), panel.getY(), panel.getHeight(), 
                panel.isExpanded() ? "是" : "否");
        }
        
        // 测试滚动到不同位置
        System.out.println("测试滚动到顶部...");
        scrollY = 0;
        System.out.println("测试滚动到底部...");
        scrollY = maxScrollY;
        System.out.println("测试滚动到中间...");
        scrollY = maxScrollY / 2;
        
        System.out.println("==================");
    }
    
    // 测试展开/收起功能
    public void testExpandCollapseFunctionality() {
        if (!cn.fpsboost.Client.isDev) return;
        
        System.out.println("=== 展开/收起功能测试 ===");
        
        for (CategoryPanel panel : categoryPanels) {
            System.out.printf("面板: %s, 当前状态: %s%n", 
                panel.getCategory().name(), panel.isExpanded() ? "展开" : "收起");
        }
        
        // 测试切换第一个面板
        if (!categoryPanels.isEmpty()) {
            CategoryPanel firstPanel = categoryPanels.get(0);
            boolean currentState = firstPanel.isExpanded();
            firstPanel.setExpanded(!currentState);
            System.out.printf("切换第一个面板状态: %s -> %s%n", 
                currentState ? "展开" : "收起", !currentState ? "展开" : "收起");
        }
        
        System.out.println("==================");
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 更新动画
        updateAnimation();
        
        // 绘制半透明背景
        drawBackground();
        
        // 绘制搜索框
        searchBox.draw(mouseX, mouseY);
        
        // 应用滚动变换
        GlStateManager.pushMatrix();
        GlStateManager.translate(-scrollX, -scrollY, 0);
        
        // 绘制分类面板
        for (CategoryPanel panel : categoryPanels) {
            panel.draw(mouseX + (int)scrollX, mouseY + (int)scrollY);
        }
        
        GlStateManager.popMatrix();
        
        // 绘制模块详情面板（不受滚动影响）
        detailPanel.draw(mouseX, mouseY);
        
        // 绘制右侧滚动条
        drawScrollBar(mouseX, mouseY);
        
        // 绘制下方横向滚动条
        drawHorizontalScrollBar(mouseX, mouseY);
        
        // 绘制滚动状态提示
        drawScrollStatus(mouseX, mouseY);
        
        // 绘制装饰元素
        drawDecorations();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastAnimationTime) / 16.67f; // 60 FPS
        lastAnimationTime = currentTime;
        
        animationProgress += deltaTime * 0.02f;
        if (animationProgress > 1.0f) {
            animationProgress = 1.0f;
        }
    }
    
    private void drawBackground() {
        // 绘制渐变背景
        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();
        
        // 主背景
        RenderUtil.drawRect(0, 0, width, height, BACKGROUND_COLOR);
        
        // 装饰性网格背景
        drawGridBackground(width, height);
    }
    
    private void drawGridBackground(int width, int height) {
        int gridSize = 40;
        int gridColor = new Color(50, 70, 100, 30).getRGB();
        
        for (int x = 0; x < width; x += gridSize) {
            RenderUtil.drawVerticalLine(x, 0, height, gridColor);
        }
        for (int y = 0; y < height; y += gridSize) {
            RenderUtil.drawHorizontalLine(0, width, y, gridColor);
        }
    }
    
    private void drawDecorations() {
        ScaledResolution sr = new ScaledResolution(mc);
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();
        
        // 绘制角落装饰
        int cornerSize = 60;
        int cornerColor = new Color(100, 150, 255, 80).getRGB();
        
        // 左上角
        drawCornerDecoration(0, 0, cornerSize, cornerColor, 0);
        // 右上角
        drawCornerDecoration(width - cornerSize, 0, cornerSize, cornerColor, 1);
        // 左下角
        drawCornerDecoration(0, height - cornerSize, cornerSize, cornerColor, 2);
        // 右下角
        drawCornerDecoration(width - cornerSize, height - cornerSize, cornerSize, cornerColor, 3);
    }
    
    private void drawCornerDecoration(int x, int y, int size, int color, int corner) {
        int lineLength = size / 3;
        int thickness = 2;
        
        switch (corner) {
            case 0: // 左上
                RenderUtil.drawHorizontalLine(x, x + lineLength, y, color);
                RenderUtil.drawVerticalLine(x, y, y + lineLength, color);
                break;
            case 1: // 右上
                RenderUtil.drawHorizontalLine(x + size - lineLength, x + size, y, color);
                RenderUtil.drawVerticalLine(x + size, y, y + lineLength, color);
                break;
            case 2: // 左下
                RenderUtil.drawHorizontalLine(x, x + lineLength, y + size, color);
                RenderUtil.drawVerticalLine(x, y + size - lineLength, y + size, color);
                break;
            case 3: // 右下
                RenderUtil.drawHorizontalLine(x + size - lineLength, x + size, y + size, color);
                RenderUtil.drawVerticalLine(x + size, y + size - lineLength, y + size, color);
                break;
        }
    }
    
    private void drawScrollBar(int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        
        // 滚动条位置
        int scrollBarX = screenWidth - scrollBarWidth - 5;
        int scrollBarY = 50;
        int scrollBarHeight = screenHeight - 100;
        
        // 绘制滚动条背景（半透明）
        RenderUtil.drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, scrollBarBackgroundColor);
        
        // 计算滚动条滑块
        if (maxScrollY > 0) {
            float scrollProgress = scrollY / maxScrollY;
            int sliderHeight = Math.max(30, (int)(scrollBarHeight * (scrollBarHeight / (maxScrollY + scrollBarHeight))));
            int sliderY = scrollBarY + (int)((scrollBarHeight - sliderHeight) * scrollProgress);
            
            // 检查鼠标悬停
            boolean hovered = mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth &&
                            mouseY >= sliderY && mouseY <= sliderY + sliderHeight;
            
            int sliderColor = hovered ? scrollBarHoverColor : scrollBarColor;
            
            // 绘制滑块阴影
            if (hovered) {
                RenderUtil.drawRect(scrollBarX + 1, sliderY + 1, scrollBarX + scrollBarWidth + 1, sliderY + sliderHeight + 1, 
                    new Color(0, 0, 0, 50).getRGB());
            }
            
            // 绘制滑块（圆角效果）
            drawRoundedSlider(scrollBarX, sliderY, scrollBarWidth, sliderHeight, sliderColor);
            
            // 绘制滑块边框
            RenderUtil.drawOutline(scrollBarX, sliderY, scrollBarWidth, sliderHeight, 
                hovered ? new Color(150, 200, 255).getRGB() : new Color(100, 150, 255, 100).getRGB());
            
            // 绘制工具提示
            if (hovered) {
                String tooltip = String.format("滚动: %.0f / %.0f", scrollY, maxScrollY);
                drawScrollTooltip(tooltip, mouseX, mouseY);
            }
        }
    }
    
    private void drawHorizontalScrollBar(int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        int scrollBarY = screenHeight - 15;
        int scrollBarHeight = 8;
        int scrollBarX = 50;
        int scrollBarWidth = screenWidth - 100;
        // 背景
        RenderUtil.drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, scrollBarBackgroundColor);
        if (maxScrollX > 0) {
            float scrollProgress = scrollX / maxScrollX;
            int sliderWidth = Math.max(30, (int)(scrollBarWidth * (scrollBarWidth / (maxScrollX + scrollBarWidth))));
            int sliderX = scrollBarX + (int)((scrollBarWidth - sliderWidth) * scrollProgress);
            boolean hovered = mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight && mouseX >= sliderX && mouseX <= sliderX + sliderWidth;
            int sliderColor = hovered ? scrollBarHoverColor : scrollBarColor;
            RenderUtil.drawRect(sliderX, scrollBarY, sliderX + sliderWidth, scrollBarY + scrollBarHeight, sliderColor);
            RenderUtil.drawOutline(sliderX, scrollBarY, sliderWidth, scrollBarHeight, hovered ? new Color(150, 200, 255).getRGB() : new Color(100, 150, 255, 100).getRGB());
            // 工具提示
            if (hovered) {
                String tooltip = String.format("横向滚动: %.0f / %.0f", scrollX, maxScrollX);
                drawScrollTooltip(tooltip, mouseX, mouseY);
            }
        }
    }
    
    private void drawScrollTooltip(String text, int mouseX, int mouseY) {
        int tooltipWidth = FontUtil.font18.getStringWidth(text) + 10;
        int tooltipHeight = 20;
        int tooltipX = mouseX - tooltipWidth - 10;
        int tooltipY = mouseY - tooltipHeight - 10;
        
        // 确保工具提示不超出屏幕
        ScaledResolution sr = new ScaledResolution(mc);
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
        FontUtil.font18.drawString(text, tooltipX + 5, tooltipY + 5, new Color(220, 230, 255).getRGB());
    }
    
    private void drawRoundedSlider(int x, int y, int width, int height, int color) {
        // 绘制中心矩形
        RenderUtil.drawRect(x + 1, y, x + width - 1, y + height, color);
        
        // 绘制左右圆角
        int cornerRadius = 3;
        
        // 左上角
        drawCircleCorner(x + 1, y + cornerRadius, cornerRadius, color, 1, 2);
        // 右上角
        drawCircleCorner(x + width - 1 - cornerRadius, y + cornerRadius, cornerRadius, color, 2, 3);
        // 左下角
        drawCircleCorner(x + 1, y + height - cornerRadius, cornerRadius, color, 4, 1);
        // 右下角
        drawCircleCorner(x + width - 1 - cornerRadius, y + height - cornerRadius, cornerRadius, color, 3, 4);
    }
    
    private void drawCircleCorner(int x, int y, int radius, int color, int startQuadrant, int endQuadrant) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        
        GlStateManager.color(r, g, b, a);
        
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        
        int segments = 8;
        float angleStep = (float) (Math.PI / 2 / segments);
        
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        
        for (int i = 0; i < segments; i++) {
            float angle1 = (startQuadrant - 1) * (float) (Math.PI / 2) + i * angleStep;
            float angle2 = (startQuadrant - 1) * (float) (Math.PI / 2) + (i + 1) * angleStep;
            
            if (endQuadrant < startQuadrant) {
                if (angle1 > endQuadrant * (float) (Math.PI / 2)) break;
                if (angle2 > endQuadrant * (float) (Math.PI / 2)) {
                    angle2 = endQuadrant * (float) (Math.PI / 2);
                }
            }
            
            float x1 = x + (float) Math.cos(angle1) * radius;
            float y1 = y + (float) Math.sin(angle1) * radius;
            float x2 = x + (float) Math.cos(angle2) * radius;
            float y2 = y + (float) Math.sin(angle2) * radius;
            
            worldrenderer.pos(x, y, 0).endVertex();
            worldrenderer.pos(x1, y1, 0).endVertex();
            worldrenderer.pos(x2, y2, 0).endVertex();
        }
        
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        // 调试信息
        if (cn.fpsboost.Client.isDev) {
            System.out.printf("ClickGUI鼠标点击: x=%d, y=%d, button=%d%n", mouseX, mouseY, mouseButton);
        }
        
        // 处理鼠标中键拖拽
        if (mouseButton == 2) { // 中键
            isMiddleDragging = true;
            middleDragStartX = mouseX;
            middleDragStartY = mouseY;
            middleDragStartScrollY = scrollY;
            return;
        }
        
        // 处理详情面板点击（优先处理）
        if (detailPanel.isVisible() && detailPanel.isMouseOver(mouseX, mouseY)) {
            if (cn.fpsboost.Client.isDev) {
                System.out.println("点击在详情面板上");
            }
            detailPanel.onMouseClick(mouseX, mouseY, mouseButton);
            return;
        }
        
        // 处理滚动条点击
        if (handleScrollBarClick(mouseX, mouseY, mouseButton)) {
            return;
        }
        
        // 处理搜索框点击
        if (searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(true);
            isSearching = true;
            return;
        } else {
            searchBox.setFocused(false);
            isSearching = false;
        }
        
        // 处理面板点击（考虑滚动偏移）
        int adjustedMouseY = mouseY + (int)scrollY;
        for (CategoryPanel panel : categoryPanels) {
            if (panel.isMouseOver(mouseX, adjustedMouseY)) {
                if (mouseButton == 0) { // 左键
                    if (panel.isHeaderHovered(mouseX, adjustedMouseY)) {
                        // 只记录按下，不立即切换展开
                        isPanelDragPending = true;
                        pendingPanel = panel;
                        pendingPressX = mouseX;
                        pendingPressY = adjustedMouseY;
                        dragStartX = mouseX - panel.getX();
                        dragStartY = adjustedMouseY - panel.getY();
                    } else {
                        panel.onMouseClick(mouseX, adjustedMouseY, mouseButton);
                    }
                } else if (mouseButton == 1) { // 右键
                    Module clickedModule = panel.getModuleAt(mouseX, adjustedMouseY);
                    if (clickedModule != null) {
                        showModuleDetail(clickedModule, mouseX, mouseY);
                    }
                }
                break;
            }
        }
    }
    
    private boolean handleScrollBarClick(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        
        int scrollBarX = screenWidth - scrollBarWidth - 5;
        int scrollBarY = 50;
        int scrollBarHeight = screenHeight - 100;
        
        // 检查是否点击了滚动条区域
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth &&
            mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
            
            if (maxScrollY > 0) {
                // 计算滑块位置
                float scrollProgress = scrollY / maxScrollY;
                int sliderHeight = Math.max(30, (int)(scrollBarHeight * (scrollBarHeight / (maxScrollY + scrollBarHeight))));
                int sliderY = scrollBarY + (int)((scrollBarHeight - sliderHeight) * scrollProgress);
                
                // 检查是否点击了滑块
                if (mouseY >= sliderY && mouseY <= sliderY + sliderHeight) {
                    isDraggingScrollBar = true;
                } else {
                    // 点击滚动条空白区域，直接跳转到对应位置
                    float clickProgress = (float)(mouseY - scrollBarY) / scrollBarHeight;
                    scrollY = clickProgress * maxScrollY;
                    scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
                }
            }
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        
        // 调试信息
        if (cn.fpsboost.Client.isDev) {
            System.out.printf("ClickGUI鼠标释放: x=%d, y=%d, state=%d%n", mouseX, mouseY, state);
        }
        
        // 处理鼠标中键释放
        if (state == 2) { // 中键
            isMiddleDragging = false;
            return;
        }
        
        // 处理详情面板的鼠标释放
        if (detailPanel.isVisible()) {
            detailPanel.onMouseRelease();
        }
        
        // 处理面板拖动/展开
        if (isPanelDragPending && pendingPanel != null) {
            // 没有拖动，判定为点击，切换展开
            int adjustedMouseY = mouseY + (int)scrollY;
            if (pendingPanel.isHeaderHovered(mouseX, adjustedMouseY)) {
                pendingPanel.setExpanded(!pendingPanel.isExpanded());
            }
        }
        isPanelDragPending = false;
        pendingPanel = null;
        isDragging = false;
        isDraggingScrollBar = false;
        draggedPanel = null;
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        
        // 调试信息
        if (cn.fpsboost.Client.isDev && detailPanel.isVisible()) {
            System.out.printf("ClickGUI鼠标拖拽: x=%d, y=%d%n", mouseX, mouseY);
        }
        
        // 处理鼠标中键拖拽（优先处理）
        if (isMiddleDragging) {
            int deltaY = mouseY - middleDragStartY;
            scrollY = middleDragStartScrollY - deltaY;
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            if (cn.fpsboost.Client.isDev) {
                System.out.printf("中键拖拽: deltaY=%d, scrollY=%.1f%n", deltaY, scrollY);
            }
            return;
        }
        
        // 处理详情面板的滑块拖拽（仅在详情面板可见且正在拖拽时）
        if (detailPanel.isVisible() && detailPanel.isDraggingSlider()) {
            detailPanel.onMouseDrag(mouseX, mouseY);
            return; // 如果详情面板正在拖拽滑块，不处理其他拖拽
        }
        
        // 处理滚动条拖拽
        if (isDraggingScrollBar && maxScrollY > 0) {
            ScaledResolution sr = new ScaledResolution(mc);
            int screenHeight = sr.getScaledHeight();
            int scrollBarY = 50;
            int scrollBarHeight = screenHeight - 100;
            
            float clickProgress = (float)(mouseY - scrollBarY) / scrollBarHeight;
            scrollY = clickProgress * maxScrollY;
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            return;
        }
        
        // 拖动判定
        if (isPanelDragPending && pendingPanel != null) {
            int dx = Math.abs(mouseX - pendingPressX);
            int dy = Math.abs((mouseY + (int)scrollY) - pendingPressY);
            if (dx > 5 || dy > 5) { // 超过5像素判定为拖动
                isDragging = true;
                draggedPanel = pendingPanel;
                isPanelDragPending = false;
            }
        }
        // 处理面板拖拽
        if (isDragging && draggedPanel != null) {
            int adjustedMouseY = mouseY + (int)scrollY;
            draggedPanel.setPosition(mouseX - dragStartX, adjustedMouseY - dragStartY);
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        
        if (isSearching) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                searchText = "";
                isSearching = false;
                searchBox.setFocused(false);
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (searchText.length() > 0) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
            } else if (keyCode == Keyboard.KEY_RETURN) {
                isSearching = false;
                searchBox.setFocused(false);
            } else if (typedChar >= 32 && typedChar < 127) {
                searchText += typedChar;
            }
            searchBox.setText(searchText);
        } else {
            // 处理滚动快捷键
            handleScrollShortcuts(keyCode);
        }
    }
    
    private void handleScrollShortcuts(int keyCode) {
        float scrollSpeed = 50.0f;
        
        switch (keyCode) {
            case Keyboard.KEY_PRIOR: // Page Up
                scrollY -= scrollSpeed;
                break;
            case Keyboard.KEY_NEXT: // Page Down
                scrollY += scrollSpeed;
                break;
            case Keyboard.KEY_HOME: // Home
                scrollY = 0;
                break;
            case Keyboard.KEY_END: // End
                scrollY = maxScrollY;
                break;
            case Keyboard.KEY_UP: // 上箭头
                scrollY -= scrollSpeed * 0.5f;
                break;
            case Keyboard.KEY_DOWN: // 下箭头
                scrollY += scrollSpeed * 0.5f;
                break;
            case Keyboard.KEY_R: // R键重置滚动
                scrollY = 0;
                break;
            case Keyboard.KEY_F5: // F5强制更新滚动范围
                forceUpdateScrollRange();
                break;
            case Keyboard.KEY_T: // T键切换所有面板展开状态
                toggleAllPanels();
                break;
            case Keyboard.KEY_E: // E键展开所有面板
                expandAllPanels();
                break;
            case Keyboard.KEY_C: // C键收起所有面板
                collapseAllPanels();
                break;
        }
        
        // 限制滚动范围
        scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        
        // 调试信息
        if (cn.fpsboost.Client.isDev) {
            System.out.printf("键盘滚动: keyCode=%d, scrollY=%.1f%n", keyCode, scrollY);
        }
    }
    
    private void toggleAllPanels() {
        if (!cn.fpsboost.Client.isDev) return;
        
        boolean allExpanded = categoryPanels.stream().allMatch(CategoryPanel::isExpanded);
        if (allExpanded) {
            collapseAllPanels();
        } else {
            expandAllPanels();
        }
    }
    
    private void expandAllPanels() {
        if (!cn.fpsboost.Client.isDev) return;
        
        for (CategoryPanel panel : categoryPanels) {
            panel.setExpanded(true);
        }
        System.out.println("展开所有面板");
    }
    
    private void collapseAllPanels() {
        if (!cn.fpsboost.Client.isDev) return;
        
        for (CategoryPanel panel : categoryPanels) {
            panel.setExpanded(false);
        }
        System.out.println("收起所有面板");
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    private void showModuleDetail(Module module, int mouseX, int mouseY) {
        detailPanel.setModule(module);
        detailPanel.setSize(250, 300);
        
        // 计算详情面板位置，避免超出屏幕
        ScaledResolution sr = new ScaledResolution(mc);
        int panelX = mouseX + 10;
        int panelY = mouseY + 10;
        
        if (panelX + 250 > sr.getScaledWidth()) {
            panelX = mouseX - 260;
        }
        if (panelY + 300 > sr.getScaledHeight()) {
            panelY = mouseY - 310;
        }
        
        detailPanel.setPosition(panelX, panelY);
        
        // 在开发模式下测试滑块功能
        if (cn.fpsboost.Client.isDev) {
            detailPanel.testSliderFunctionality();
        }
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        
        // 保存面板配置
        for (CategoryPanel panel : categoryPanels) {
            Client.clickGUIManager.savePanelConfig(
                panel.getCategory().name(),
                panel.getX(),
                panel.getY(),
                panel.isExpanded()
            );
        }
        
        // 保存配置
        Client.clickGUIManager.save();
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        // 处理鼠标滚轮
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (shift) {
                // 横向滚动
                float scrollSpeed = 20.0f;
                if (wheel > 0) {
                    scrollX -= scrollSpeed;
                } else {
                    scrollX += scrollSpeed;
                }
                scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            } else {
                // 纵向滚动
                // 检查鼠标是否在详情面板上
                if (detailPanel.isVisible() && detailPanel.isMouseOver(mouseX, mouseY)) {
                    detailPanel.onMouseWheel(mouseX, mouseY, wheel);
                    return;
                }
                handleMainScroll(wheel, mouseX, mouseY);
            }
        }
    }
    
    private void handleMainScroll(int wheel, int mouseX, int mouseY) {
        // 检查鼠标是否在面板区域（排除滚动条区域）
        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int scrollBarX = screenWidth - scrollBarWidth - 5;
        
        // 如果鼠标在滚动条区域，不处理滚动
        if (mouseX >= scrollBarX) {
            return;
        }
        
        // 如果鼠标在详情面板上，不处理主界面滚动
        if (detailPanel.isVisible() && detailPanel.isMouseOver(mouseX, mouseY)) {
            return;
        }
        
        // 处理滚动
        float scrollSpeed = 20.0f;
        if (wheel > 0) {
            scrollY -= scrollSpeed;
        } else {
            scrollY += scrollSpeed;
        }
        
        // 限制滚动范围
        scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        
        // 调试信息
        if (cn.fpsboost.Client.isDev) {
            System.out.printf("主界面滚动: wheel=%d, scrollY=%.1f, maxScrollY=%.1f%n", wheel, scrollY, maxScrollY);
        }
    }
    
    private void drawScrollStatus(int mouseX, int mouseY) {
        // 在开发模式下显示滚动状态
        if (cn.fpsboost.Client.isDev) {
            String statusText = String.format("滚动: %.0f/%.0f, 横向: %.0f/%.0f", scrollY, maxScrollY, scrollX, maxScrollX);
            if (isMiddleDragging) {
                statusText += " [中键拖拽中]";
            }
            if (isDraggingScrollBar) {
                statusText += " [滚动条拖拽中]";
            }
            
            FontUtil.font18.drawString(statusText, 10, 40, new Color(255, 255, 0).getRGB());
        }
        
        // 显示滚动提示（当有内容可滚动时）
        if (maxScrollY > 0 && scrollY == 0) {
            String hintText = "使用鼠标滚轮或中键拖拽滚动，按住Shift横向滚动";
            int hintWidth = FontUtil.font18.getStringWidth(hintText);
            int hintX = (width - hintWidth) / 2;
            int hintY = height - 30;
            
            FontUtil.font18.drawString(hintText, hintX, hintY, new Color(150, 200, 255, 150).getRGB());
        }
    }
    
    // 搜索框内部类
    private class SearchBox {
        private int x, y, width, height;
        @Setter
        private String text = "";
        private boolean focused = false;
        private long cursorBlinkTime = 0;
        
        public SearchBox(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public void draw(int mouseX, int mouseY) {
            boolean hovered = isMouseOver(mouseX, mouseY);
            
            // 绘制背景
            int bgColor = focused ? HOVER_COLOR : (hovered ? SECONDARY_COLOR : PANEL_BACKGROUND);
            RenderUtil.drawRect(x, y, x + width, y + height, bgColor);
            RenderUtil.drawOutline(x, y, width, height, PRIMARY_COLOR);
            
            // 绘制搜索图标
            int iconSize = 12;
            int iconX = x + 5;
            int iconY = y + (height - iconSize) / 2;
            drawSearchIcon(iconX, iconY, iconSize, TEXT_COLOR);
            
            // 绘制文本
            String displayText = text;
            if (focused && (System.currentTimeMillis() - cursorBlinkTime) % 1000 < 500) {
                displayText += "|";
            }
            
            FontUtil.font18.drawString(displayText, x + 20, y + (height - 8) / 2, TEXT_COLOR);
            
            // 绘制占位符
            if (text.isEmpty() && !focused) {
                FontUtil.font18.drawString("搜索模块...", x + 20, y + (height - 8) / 2, DISABLED_COLOR);
            }
        }
        
        private void drawSearchIcon(int x, int y, int size, int color) {
            // 简单的搜索图标绘制
            float centerX = x + size / 2f;
            float centerY = y + size / 2f;
            float radius = size / 3f;
            
            // 绘制圆形
            drawCircle(centerX, centerY, radius, color);
            
            // 绘制手柄
            float handleX = centerX + radius * 0.7f;
            float handleY = centerY + radius * 0.7f;
            float handleLength = radius * 0.8f;
            
            RenderUtil.drawHorizontalLine(handleX, handleX + handleLength, handleY, color);
            RenderUtil.drawVerticalLine(handleX + handleLength, handleY, handleY + handleLength * 0.3f, color);
        }
        
        private void drawCircle(float centerX, float centerY, float radius, int color) {
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
        
        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
        
        public void setFocused(boolean focused) {
            this.focused = focused;
            if (focused) {
                cursorBlinkTime = System.currentTimeMillis();
            }
        }
    }
    
    // 分类面板内部类
    @Data
    private class CategoryPanel {
        private Category category;
        private int x, y, width, height;
        private boolean expanded = true;
        private List<Module> modules;
        private float expandAnimation = 1.0f;
        private long lastExpandTime;
        
        public CategoryPanel(Category category, int x, int y, int width, int height) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.modules = getModulesForCategory(category);
            this.lastExpandTime = System.currentTimeMillis();
        }
        
        public void draw(int mouseX, int mouseY) {
            boolean hovered = isMouseOver(mouseX, mouseY);
            boolean headerHovered = isHeaderHovered(mouseX, mouseY);
            
            // 更新展开动画
            updateExpandAnimation();
            
            // 计算实际显示高度
            int actualHeight = expanded ? height : (int)(25 + (height - 25) * expandAnimation);
            
            // 绘制面板背景
            int bgColor = hovered ? new Color(40, 55, 80, 220).getRGB() : PANEL_BACKGROUND;
            RenderUtil.drawRect(x, y, x + width, y + actualHeight, bgColor);
            
            // 绘制边框
            int borderColor = headerHovered ? HOVER_COLOR : PRIMARY_COLOR;
            RenderUtil.drawOutline(x, y, width, actualHeight, borderColor);
            
            // 绘制标题栏
            drawHeader(mouseX, mouseY);
            
            // 绘制模块列表
            if (expanded || expandAnimation > 0) {
                drawModules(mouseX, mouseY, actualHeight);
            }
            
            // 绘制装饰元素
            drawPanelDecorations(actualHeight);
        }
        
        private void updateExpandAnimation() {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastExpandTime) / 16.67f;
            lastExpandTime = currentTime;
            
            if (expanded && expandAnimation < 1.0f) {
                expandAnimation += deltaTime * 0.15f; // 加快展开速度
                if (expandAnimation > 1.0f) expandAnimation = 1.0f;
            } else if (!expanded && expandAnimation > 0.0f) {
                expandAnimation -= deltaTime * 0.15f; // 加快收起速度
                if (expandAnimation < 0.0f) expandAnimation = 0.0f;
            }
        }
        
        private void drawHeader(int mouseX, int mouseY) {
            boolean headerHovered = isHeaderHovered(mouseX, mouseY);
            
            // 标题栏背景
            int headerColor = headerHovered ? HOVER_COLOR : SECONDARY_COLOR;
            RenderUtil.drawRect(x, y, x + width, y + 25, headerColor);
            
            // 分类名称
            String categoryName = getCategoryDisplayName(category);
            FontUtil.font18.drawString(categoryName, x + 5, y + 5, TEXT_COLOR);
            
            // 展开/收起图标
            String expandIcon = expanded ? "▼" : "▶";
            int iconColor = headerHovered ? ACCENT_COLOR : TEXT_COLOR;
            FontUtil.font18.drawString(expandIcon, x + width - 15, y + 5, iconColor);
            
            // 模块数量
            String moduleCount = "(" + modules.size() + ")";
            FontUtil.font18.drawString(moduleCount, x + width - 35, y + 5, DISABLED_COLOR);
        }
        
        private void drawModules(int mouseX, int mouseY, int maxHeight) {
            int moduleY = y + 30;
            int moduleHeight = 20;
            int visibleHeight = (int) ((maxHeight - 30) * expandAnimation);

            for (Module module : modules) {
                // 检查是否在搜索范围内
                if (!searchText.isEmpty() && !module.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    continue;
                }

                int moduleX = x + 5;
                int moduleWidth = width - 10;

                if (moduleY + moduleHeight > y + 30 + visibleHeight) break;

                boolean moduleHovered = mouseX >= moduleX && mouseX <= moduleX + moduleWidth &&
                        mouseY >= moduleY && mouseY <= moduleY + moduleHeight;

                // 模块背景
                int moduleBgColor = module.isEnabled() ?
                        (moduleHovered ? HOVER_COLOR : ACCENT_COLOR) :
                        (moduleHovered ? new Color(80, 100, 120, 150).getRGB() : new Color(60, 80, 100, 100).getRGB());

                RenderUtil.drawRect(moduleX, moduleY, moduleX + moduleWidth, moduleY + moduleHeight, moduleBgColor);

                // 模块名称
                int textColor = module.isEnabled() ? TEXT_COLOR : DISABLED_COLOR;
                FontUtil.font18.drawString(module.getName(), moduleX + 5, moduleY + 5, textColor);

                // 状态指示器
                String status = module.isEnabled() ? "●" : "○";
                int statusColor = module.isEnabled() ? new Color(100, 255, 100).getRGB() : DISABLED_COLOR;
                FontUtil.font18.drawString(status, moduleX + moduleWidth - 15, moduleY + 5, statusColor);

                moduleY += moduleHeight + 2;
            }
        }
        
        private void drawPanelDecorations(int actualHeight) {
            // 绘制面板角落装饰
            int cornerSize = 15;
            int cornerColor = new Color(100, 150, 255, 60).getRGB();
            
            // 左上角
            RenderUtil.drawHorizontalLine(x, x + cornerSize, y, cornerColor);
            RenderUtil.drawVerticalLine(x, y, y + cornerSize, cornerColor);
            
            // 右上角
            RenderUtil.drawHorizontalLine(x + width - cornerSize, x + width, y, cornerColor);
            RenderUtil.drawVerticalLine(x + width, y, y + cornerSize, cornerColor);
        }
        
        public boolean isMouseOver(int mouseX, int mouseY) {
            // 计算实际显示高度
            int actualHeight = expanded ? height : (int)(25 + (height - 25) * expandAnimation);

            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + actualHeight;
        }

        public boolean isHeaderHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 25;
        }
        
        public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
            if (isHeaderHovered(mouseX, mouseY)) {
                expanded = !expanded;

                return;
            }
            
            // 处理模块点击
            if (expanded) {
                int moduleY = y + 30;
                int moduleHeight = 20;
                
                for (Module module : modules) {
                    if (!searchText.isEmpty() && !module.getName().toLowerCase().contains(searchText.toLowerCase())) {
                        continue;
                    }
                    
                    int moduleX = x + 5;
                    int moduleWidth = width - 10;
                    
                    if (mouseX >= moduleX && mouseX <= moduleX + moduleWidth &&
                        mouseY >= moduleY && mouseY <= moduleY + moduleHeight) {
                        if (mouseButton == 0) { // 左键切换模块
                            module.toggle();
                            
                            // 调试信息
                            if (cn.fpsboost.Client.isDev) {
                                System.out.printf("切换模块: %s -> %s%n", module.getName(), module.isEnabled() ? "启用" : "禁用");
                            }
                        }
                        break;
                    }
                    
                    moduleY += moduleHeight + 2;
                }
            }
        }
        
        public Module getModuleAt(int mouseX, int mouseY) {
            if (!expanded) return null;
            
            int moduleY = y + 30;
            int moduleHeight = 20;
            
            for (Module module : modules) {
                if (!searchText.isEmpty() && !module.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    continue;
                }
                
                int moduleX = x + 5;
                int moduleWidth = width - 10;
                
                if (mouseX >= moduleX && mouseX <= moduleX + moduleWidth &&
                    mouseY >= moduleY && mouseY <= moduleY + moduleHeight) {
                    return module;
                }
                
                moduleY += moduleHeight + 2;
            }
            return null;
        }
        
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
            // 当面板位置改变时，更新滚动范围
            if (ClickGUI.INSTANCE != null) {
                ClickGUI.INSTANCE.forceUpdateScrollRange();
            }
        }
    }
    
    private List<Module> getModulesForCategory(Category category) {
        return Client.moduleManager.getModules().values().stream()
                .filter(module -> module.getCategory() == category && module.isCanDisplay())
                .collect(Collectors.toList());
    }
    
    private String getCategoryDisplayName(Category category) {
        switch (category) {
            case MISC: return "杂项";
            case RENDER: return "渲染";
            case CLIENT: return "客户端";
            case DEV: return "开发";
            default: return category.name();
        }
    }
}