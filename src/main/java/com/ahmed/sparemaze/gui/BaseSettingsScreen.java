package com.ahmed.sparemaze.gui;

import com.ahmed.sparemaze.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseSettingsScreen extends Screen {
    protected final Screen parent;
    protected SettingsListWidget listWidget;

    protected BaseSettingsScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    protected void init() {
        int topHeaderHeight = 32;
        int bottomFooterHeight = 36;
        int listHeight = Math.max(40, this.height - topHeaderHeight - bottomFooterHeight);

        this.listWidget = new SettingsListWidget(this.client, this.width, listHeight, topHeaderHeight, 24);
        buildEntries();
        this.addDrawableChild(this.listWidget);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
            .dimensions(this.width / 2 - 100, this.height - 27, 200, 20)
            .build());
    }

    protected abstract void buildEntries();

    protected void addFullWidthButton(Text label, ButtonWidget.PressAction action) {
        ButtonWidget btn = ButtonWidget.builder(label, action)
            .dimensions(this.width / 2 - 155, 0, 310, 20)
            .build();
        this.listWidget.addSingleEntry(btn);
    }

    protected void addPairButtons(Text leftLabel, ButtonWidget.PressAction leftAction, Text rightLabel, ButtonWidget.PressAction rightAction) {
        ButtonWidget left = ButtonWidget.builder(leftLabel, leftAction)
            .dimensions(this.width / 2 - 155, 0, 150, 20)
            .build();
        ButtonWidget right = ButtonWidget.builder(rightLabel, rightAction)
            .dimensions(this.width / 2 + 5, 0, 150, 20)
            .build();
        this.listWidget.addDualEntry(left, right);
    }

    protected void addBooleanToggle(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        ButtonWidget toggle = ButtonWidget.builder(formatToggleText(label, getter.get()), btn -> {
            boolean next = !getter.get();
            setter.accept(next);
            ConfigManager.save();
            btn.setMessage(formatToggleText(label, next));
        }).dimensions(this.width / 2 - 155, 0, 310, 20).build();
        this.listWidget.addSingleEntry(toggle);
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
        this.listWidget.addSingleEntry(slider);
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

    protected static class SettingsListWidget extends ElementListWidget<SettingsListWidget.RowEntry> {
        public SettingsListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
        }

        public void addSingleEntry(ClickableWidget widget) {
            this.addEntry(new RowEntry(List.of(widget)));
        }

        public void addDualEntry(ClickableWidget left, ClickableWidget right) {
            this.addEntry(new RowEntry(List.of(left, right)));
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        protected static class RowEntry extends ElementListWidget.Entry<RowEntry> {
            private final List<ClickableWidget> widgets;

            public RowEntry(List<ClickableWidget> widgets) {
                this.widgets = widgets;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if (widgets.size() == 1) {
                    ClickableWidget w = widgets.get(0);
                    w.setX(x);
                    w.setY(y);
                    w.setWidth(entryWidth);
                    w.render(context, mouseX, mouseY, tickDelta);
                } else if (widgets.size() == 2) {
                    ClickableWidget left = widgets.get(0);
                    ClickableWidget right = widgets.get(1);
                    int half = (entryWidth - 10) / 2;
                    left.setX(x);
                    left.setY(y);
                    left.setWidth(half);
                    right.setX(x + half + 10);
                    right.setY(y);
                    right.setWidth(half);
                    left.render(context, mouseX, mouseY, tickDelta);
                    right.render(context, mouseX, mouseY, tickDelta);
                }
            }

            @Override
            public List<? extends Element> children() {
                return widgets;
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return widgets;
            }
        }
    }
}
