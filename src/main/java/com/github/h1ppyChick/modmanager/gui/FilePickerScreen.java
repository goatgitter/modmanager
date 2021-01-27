package com.github.h1ppyChick.modmanager.gui;

import com.github.h1ppyChick.modmanager.util.Log;

import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class FilePickerScreen extends ScreenBase {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("TwoListsWidgetScreen");
	protected TwoListsWidget availableMods;
	protected TwoListsWidget selectedMods;
	protected ModListEntry selected;
	protected double scrollPercent = 0;
	protected ModsScreen modsScreen;
	protected ModListWidget availableList;
	protected ModListWidget selectedList;
	protected int paneY;
	protected int paneWidth;
	protected int rightPaneX;
	protected boolean restartRequired = false;
	
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	protected FilePickerScreen(Text title) {
		super(title);
	}
	
	public FilePickerScreen(Screen previousScreen, String titleId) {
		super(previousScreen, titleId);
		this.modsScreen = (ModsScreen) previousScreen;
		availableList = new ModListWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, "", this.availableList, modsScreen);
		selectedList = new ModListWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, "", this.selectedList, modsScreen);
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	public ModListEntry getSelectedEntry() {
		return selected;
	}
	
	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
		}
	}
	
	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		if (selectedList.isMouseOver(double_1, double_2)) {
			return this.selectedList.mouseScrolled(double_1, double_2, double_3);
		}
		if (availableList.isMouseOver(double_1, double_2)) {
			return this.availableList.mouseScrolled(double_1, double_2, double_3);
		}
		return false;
	}

	@Override
	protected void closeScreen()
	{
		super.closeScreen();
		this.selectedList.close();
		this.availableList.close();
		this.selected = null;
		
	}
}
