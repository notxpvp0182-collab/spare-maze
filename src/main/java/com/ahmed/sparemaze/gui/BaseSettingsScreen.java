package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseSettingsScreen extends Screen {
    protected final Screen parent;
    private final List<RowData> rows = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private static final int HEADER_HEIGHT = 34;
    private static final int FOOTER_HEIGHT = 36;
    private static final int ROW_HEIGHT = 24;

    protected BaseSettingsScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.rows.clear();
        buildEntries();

        for (RowData row : rows) {
            this.addDrawableChild(row.left);
            if (row.right != null) {
                this.addDrawableChild(row.right);
            }
        }

        int visibleHeight = Math.max(24, this.height - HEADER_HEIGHT - FOOTER_HEIGHT);
        int totalContentHeight = rows.size() * ROW_HEIGHT;
        this.maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0, this.maxScroll);

        updateRowPositions();

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
            .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
            .build());
    }

    protected abstract void buildEntries();

    private void updateRowPositions() {
        int centerX = this.width / 2;
        int minVisibleY = HEADER_HEIGHT;
        int maxVisibleY = this.height - FOOTER_HEIGHT - 20;

        for (int i = 0; i < rows.size(); i++) {
            int y = HEADER_HEIGHT + (i * ROW_HEIGHT) - scrollOffset;
            boolean visible = (y >= minVisibleY - 4) && (y <= maxVisibleY + 4);
            RowData row = rows.get(i);

            if (row.right == null) {
                row.left.setX(centerX - 155);
                row.left.setY(y);
                row.left.setWidth(310);
                row.left.visible = visible;
            } else {
                row.left.setX(centerX - 155);
                row.left.setY(y);
                row.left.setWidth(150);
                row.left.visible = visible;

                row.right.setX(centerX + 5);
                row.right.setY(y);
                row.right.setWidth(150);
                row.right.visible = visible;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.maxScroll > 0) {
            this.scrollOffset = MathHelper.clamp(this.scrollOffset - (int) (verticalAmount * 16), 0, this.maxScroll);
            updateRowPositions();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected void addFullWidthButton(Text label, ButtonWidget.PressAction action) {
        ButtonWidget btn = ButtonWidget.builder(label, action)
            .dimensions(this.width / 2 - 155, 0, 310, 20)
            .build();
        this.rows.add(new RowData(btn, null));
    }

    protected void addPairButtons(Text leftLabel, ButtonWidget.PressAction leftAction, Text rightLabel, ButtonWidget.PressAction rightAction) {
        ButtonWidget left = ButtonWidget.builder(leftLabel, leftAction)
            .dimensions(this.width / 2 - 155, 0, 150, 20)
            .build();
        ButtonWidget right = ButtonWidget.builder(rightLabel, rightAction)
            .dimensions(this.width / 2 + 5, 0, 150, 20)
            .build();
        this.rows.add(new RowData(left, right));
    }

    protected void addBooleanToggle(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        ButtonWidget toggle = ButtonWidget.builder(formatToggleText(label, getter.get()), btn -> {
            boolean next = !getter.get();
            setter.accept(next);
            ConfigManager.save();
            btn.setMessage(formatToggleText(label, next));
        }).dimensions(this.width / 2 - 155, 0, 310, 20).build();
        this.rows.add(new RowData(toggle, null));
    }

    protected void addIntSlider(String label, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter) {
        double initialNormalized = MathHelper.clamp((double) (getter.get() - min) / (double) (max - min), 0.0, 1.0);
        SliderWidget slider = new SliderWidget(this.width / 2 - 155, 0, 310, 20, Text.literal(label + ": " + getter.get()), initialNormalized) {
            @Override
            protected void updateMessage() {
                int val = min + (int) Math.round(this.value * (max - min));
                this.setMessage(Text.literal(label + ": " + val));
            }

            @Override
            protected void applyValue() {
                int val = min + (int) Math.round(this.value * (max - min));
                setter.accept(val);
                ConfigManager.save();
            }
        };
        this.rows.add(new RowData(slider, null));
    }

    private Text formatToggleText(String label, boolean enabled) {
        return Text.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void close() {
        ConfigManager.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private record RowData(ClickableWidget left, ClickableWidget right) {}
}
