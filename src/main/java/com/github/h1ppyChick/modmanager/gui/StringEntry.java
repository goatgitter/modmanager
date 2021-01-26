package com.github.h1ppyChick.modmanager.gui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class StringEntry extends AlwaysSelectedEntryListWidget.Entry<StringEntry> {
	protected final MinecraftClient client;
	protected final String value;

	public StringEntry(String val) {
		this.value = val;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
		x += getXOffset();
		y += getYOffset();
		Text valueText = new LiteralText(value);
	    DrawableHelper.drawTextWithShadow(matrices, this.client.textRenderer, valueText, x, y, 16777215);
	}
	public static int getXOffset() {
		return 4;
	}
	public static int getYOffset() {
		return 1;
	}
	
	public String getValue()
	{
		return this.value;
	}
}
