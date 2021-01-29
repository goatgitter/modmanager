package com.github.h1ppyChick.modmanager.gui;

import com.github.h1ppyChick.modmanager.util.Log;

import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 */
public abstract class TwoListsWidgetScreen extends ScreenBase {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("TwoListsWidgetScreen");
	protected TwoListsWidget availableMods;
	protected TwoListsWidget selectedMods;
	protected ModListEntry selected;
	protected double scrollPercent = 0;
	protected ModsScreen modsScreen;
	protected ModListWidget availableModList;
	protected ModListWidget selectedModList;
	protected boolean restartRequired = false;
	
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	protected TwoListsWidgetScreen(Text title) {
		super(title);
	}
	
	public TwoListsWidgetScreen(Screen previousScreen, String titleId) {
		super(previousScreen, titleId);
		this.modsScreen = (ModsScreen) previousScreen;
		availableModList = new ModListWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, "", this.availableModList, modsScreen);
		selectedModList = new ModListWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, "", this.selectedModList, modsScreen);
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
		if (selectedModList.isMouseOver(double_1, double_2)) {
			return this.selectedModList.mouseScrolled(double_1, double_2, double_3);
		}
		if (availableModList.isMouseOver(double_1, double_2)) {
			return this.availableModList.mouseScrolled(double_1, double_2, double_3);
		}
		return false;
	}

	@Override
	protected void closeScreen()
	{
		super.closeScreen();
		this.selectedModList.close();
		this.availableModList.close();
		this.selected = null;
		
	}
}
