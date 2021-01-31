package com.github.goatgitter.modmanager.gui;
import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.util.Stencil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class StringEntry extends AlwaysSelectedEntryListWidget.Entry<StringEntry> {
	protected final MinecraftClient client;
	protected final String value;
	private boolean isOnRightSide = false;
	private boolean isSelectedItem = false;
	private boolean isRenderArrow = false;

	
	public StringEntry(String val) {
		this.value = val;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void render(MatrixStack matrices, int index, int entryLeft, int entryRight, int entryTop,  int entryBottom,  int mouseX, int mouseY, boolean isHovered, float delta) {
		if (isSelectedItem) {
			Stencil.setColorGreen();
			Stencil.rectangle(matrices, entryLeft - 1, entryRight + 1, entryTop - 1, entryBottom + 1);
		}
		else if (isHovered)
		{
			Stencil.setColorBlue();
			Stencil.rectangle(matrices, entryLeft - 1, entryRight + 1, entryTop - 1, entryBottom + 1);
			if (isRenderArrow)
			{
				addArrow(matrices, entryTop-1, entryLeft -1, entryRight +1, entryBottom+1, mouseX, mouseY);
			}
		}
		
		entryLeft += getXOffset();
		if (isHovered && isOnRightSide)
		{
			entryLeft += entryBottom - entryTop;
		}
		entryTop += getYOffset();
		Text valueText = new LiteralText(value);
	    DrawableHelper.drawTextWithShadow(matrices, this.client.textRenderer, valueText, entryLeft, entryTop, 16777215);
	}
	
	public void addArrow(MatrixStack matrices, int entryTop, int entryLeft, int entryRight, int entryBottom, int mouseX, int mouseY)
	{
		int arrowSize = 13;
		int arrowLeft = entryLeft;
		int arrowRight = arrowLeft + arrowSize;
		int arrowTop = entryTop;
		int arrowBottom = arrowTop + arrowSize;
		int regionTop = arrowSize;
		int regionLeft = arrowSize;
		if (!isOnRightSide)
		{
			arrowLeft = entryRight - arrowSize;
			arrowRight = entryRight;
			regionTop = 0;
		}
		
		this.client.getTextureManager().bindTexture(ModManager.ARROW_BUTTON_LOCATION);
		Stencil.texturedRectangle(matrices, arrowLeft, arrowRight, arrowTop, arrowBottom, regionLeft, regionTop, arrowSize, arrowSize, 13, 26);
	}
	
	public int getXOffset() {
		return 4;
	}
	public int getYOffset() {
		return 1;
	}
	
	public String getValue()
	{
		return this.value;
	}

	public boolean isOnRightSide() {
		return isOnRightSide;
	}

	public void setOnRightSide(boolean isOnRightSide) {
		this.isOnRightSide = isOnRightSide;
	}

	public boolean isSelectedItem() {
		return isSelectedItem;
	}

	public void setSelectedItem(boolean isSelectedItem) {
		this.isSelectedItem = isSelectedItem;
	}
	
	public boolean isRenderArrow() {
		return isRenderArrow;
	}

	public void setRenderArrow(boolean isRenderArrow) {
		this.isRenderArrow = isRenderArrow;
	}

}
