package com.github.goatgitter.modmanager.gui;

import java.util.List;
import java.util.Objects;

import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.Stencil;

import io.github.prospector.modmenu.mixin.EntryListWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class TwoStringListsWidget extends StringListWidget {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("StringListWidget");
	private boolean isOnRightSide = false;

	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public TwoStringListsWidget(MinecraftClient client, int left, int width, int height, int y1, int y2,
			int entryHeight, List<String> widgetList, ScreenBase parent, Text title, LoadListAction onLoadList,
			ClickEntryAction onClickEntry, String selectedEntry) {
		super(client, left, width, height, y1, y2, entryHeight, widgetList, parent, title, onLoadList, onClickEntry,
				selectedEntry);
		listInputWidth = this.width;
		listInputX = this.left;
		listInputY = this.top;
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	public void setOnRightSide()
	{
		this.isOnRightSide = true;
	}

	/***************************************************
	 *              RENDERING
	 **************************************************/

	@Override
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		this.bottom = this.top + this.height;
		Stencil.setColorBlack();
		Stencil.rectangle(matrices, this.left, this.right, this.top, this.bottom);
		renderListEntries(matrices,mouseX, mouseY, delta);
	}

	protected void renderListEntries(MatrixStack matrices,int mouseX, int mouseY, float delta)
	{
		int boxTop = this.top;
		int boxRightX = getRowLeft() + getRowWidth() +1;
		int boxLeftX = getRowLeft();
		int boxBottom = this.top + this.getItemCount() * this.itemHeight;
		Stencil.setColorBlack();
		Stencil.rectangle(matrices, boxLeftX, boxRightX, boxTop, boxBottom);
		
		for (int index = 0; index < this.getItemCount(); ++index) {
			int entryTop = this.getRowTop(index) + 4;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= getScrollBottom()) {
				StringEntry entry = this.getEntry(index);
				int entryLeft = getEntryLeft();
				int entryRight = getEntryRight();
				entry.setSelectedItem(false);
				if (((EntryListWidgetAccessor) this).isRenderSelection() && this.isSelectedItem(index)) {
					entry.setSelectedItem(true);
				}
				this.bottom = Math.max(this.bottom, entryBottom);
				boolean isHovered = this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry);
				entry.setOnRightSide(isOnRightSide);
				entry.setRenderArrow(true);
				entry.render(matrices, index, entryLeft, entryRight, entryTop, entryBottom,  mouseX, mouseY, isHovered, delta);
			}
		}
	}

	
}
