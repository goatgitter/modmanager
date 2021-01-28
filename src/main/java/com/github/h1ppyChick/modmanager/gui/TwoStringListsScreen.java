package com.github.h1ppyChick.modmanager.gui;

import java.util.ArrayList;

import com.github.h1ppyChick.modmanager.gui.StringListWidget.LoadListAction;
import com.github.h1ppyChick.modmanager.util.Log;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public abstract class TwoStringListsScreen extends ScreenBase {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("TwoStringListsScreen");
	protected TwoStringListsWidget availableList;
	protected TwoStringListsWidget selectedList;
	protected double scrollPercent = 0;
	protected boolean restartRequired = false;
	LiteralText selectedTitle = new LiteralText("Selected");
	LiteralText availableTitle = new LiteralText("Available");
	private LoadListAction onLoadAvailList;
	private String currentAvailEntry;
	private LoadListAction onLoadSelectedList;
	private String currentSelectedEntry;
	protected ScreenBase previousScreen;
	
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	protected TwoStringListsScreen(Text title) {
		super(title);
	}
	
	public TwoStringListsScreen(ScreenBase previousScreen, String titleId, 
			LoadListAction onLoadAvailList, String currentAvailEntry,
			LoadListAction onLoadSelectedList, String currentSelectedEntry) {
		super(previousScreen, titleId);
		this.previousScreen = previousScreen;
		this.onLoadAvailList = onLoadAvailList;
		this.currentAvailEntry = currentAvailEntry;
		this.onLoadSelectedList = onLoadSelectedList;
		this.currentSelectedEntry = currentSelectedEntry;
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	@Override
	public void init() {
		LOG.enter("init");
		int paneLeft = 5;
		int entryHeight = 15;
		int paneHeight = this.height - 20;
		availableList = new TwoStringListsWidget(this.client, paneLeft, previousScreen.paneWidth, paneHeight, 
				previousScreen.paneY + getY1Offset(), this.height + getY2Offset(), entryHeight, 
				new ArrayList<String>(), previousScreen, availableTitle, 
				onLoadAvailList, 
				(StringListWidget.ClickEntryAction) entry -> onClickAvailEntry(entry), 
				currentAvailEntry);
		selectedList = new TwoStringListsWidget(this.client, previousScreen.rightPaneX, previousScreen.paneWidth, paneHeight, 
				previousScreen.paneY + getY1Offset(), this.height + getY2Offset(), entryHeight, 
				new ArrayList<String>(), previousScreen, selectedTitle,
				onLoadSelectedList,
				(StringListWidget.ClickEntryAction) entry -> onClickSelEntry(entry), 
				currentSelectedEntry);
		selectedList.setOnRightSide();
		LOG.exit("init");
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.availableList.render(matrices, mouseX, mouseY, delta);
	    this.selectedList.render(matrices, mouseX, mouseY, delta);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, this.title, this.width / 3 + 10, 8, 16777215);
		
		super.render(matrices, mouseX, mouseY, delta);
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
	
	public void onClickAvailEntry(StringEntry entry)
	{
		if (availableList.hasEntry(entry.value))
		{
			availableList.removeEntry(entry);
			selectedList.addEntry(entry);
		}
	}
	
	public void onClickSelEntry(StringEntry entry)
	{
		if(selectedList.hasEntry(entry.value))
		{
			selectedList.removeEntry(entry);
			availableList.addEntry(entry);
		}
	}
}
