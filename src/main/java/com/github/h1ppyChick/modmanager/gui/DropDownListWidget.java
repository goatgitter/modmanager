package com.github.h1ppyChick.modmanager.gui;

import java.util.List;
import java.util.function.Predicate;

import com.github.h1ppyChick.modmanager.ModManager;
import com.github.h1ppyChick.modmanager.util.Log;

import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class DropDownListWidget extends StringListWidget {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("DropDownListWidget");
	protected final DropDownListWidget.OpenListAction onOpenList;
	protected final DropDownListWidget.SaveListAction onSaveList;
	protected final DropDownListWidget.AddEntryAction onAddEntry;
	protected final DropDownListWidget.ExportListAction onExportList;
	protected final DropDownListWidget.ImportListAction onImportList;
	private ButtonWidget openBtn;
	private ButtonWidget saveBtn;
	private ButtonWidget addBtn;
	private ButtonWidget exportBtn;
	private ButtonWidget importBtn;
	private boolean isListOpen = false;

	private int openBtnX = 0;
	private int saveBtnX = 0;
	private int addBtnX = 0;
	private int exportBtnX = 0;
	private int importBtnX = 0;
	private int topBtnY = 0;
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public DropDownListWidget(MinecraftClient client, int left, int width, int height, 
			int y1, int y2, int entryHeight, List<String> widgetList, 
			TwoListsWidgetScreen parent, Text title, LoadListAction onLoadList, 
			ClickEntryAction onClickEntry, OpenListAction onOpenList,
			SaveListAction onSaveList, AddEntryAction onAddEntry,
			ExportListAction onExportList,ImportListAction onImportList,
			String selectedEntry) {
		super(client, left, width, height,y1, y2, entryHeight, widgetList, 
				parent,title, onLoadList,onClickEntry, selectedEntry);
		this.method_31322(false);
		this.setLeftPos(left);
	    this.centerListVertically = false;
		this.onOpenList = onOpenList;
		this.onSaveList = onSaveList;
		this.onAddEntry = onAddEntry;
		this.onExportList = onExportList;
		this.onImportList = onImportList;
		drawButtons();
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/	
	@Override
	public void setSelected(StringEntry entry) {
		super.setSelected(entry);
		if (listInput != null)
		{
			listInput.setText(selectedEntry);
			if (isListOpen)
			{
				onClickEntry(entry);
				isListOpen = false;
				drawListInput();
				drawOpenButton();
			}
		}
	}
	
	public String getCurrentValue()
	{
		String currentText = this.listInput.getText();
		StringEntry currentEntry = getSelected();
		if (!currentText.equals(currentEntry.getValue()))
		{
			add(currentText);
			select(currentText);
			removeEntry(currentEntry);
		}
	    return getSelectedValue();
	}

	/***************************************************
	 *              LIST INPUT
	 **************************************************/
	@Override
	protected void drawListInput()
	{
		super.drawListInput();
		Predicate<String> noSpecialChars = (s -> s.matches("^[a-zA-Z0-9-_]*$"));
		listInput.setTextPredicate(noSpecialChars);
		
		this.listInput.setText(selectedEntry);
		listInput.visible = !isListOpen;
	}
	
	/***************************************************
	 *              BUTTONS
	 **************************************************/
	protected void drawButtons()
	{
		topBtnY = listInputY - 2;
		drawOpenButton();
		drawSaveButton();
		drawAddButton();
		drawExportButton();
		drawImportButton();
	}
	/***************************************************
	 *              OPEN BUTTON
	 **************************************************/
	/**
	 * Draws the Open Button at the correct position on the screen.
	 */
	private void drawOpenButton()
	{
		if (openBtn == null)
		{
			openBtnX =  listInputX + listInputWidth;
			openBtn = new ModMenuTexturedButtonWidget(openBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.OPEN_BUTTON_LOCATION, this.height, 42, 
					button -> {this.onOpenList(this);},
					ModManager.TEXT_OPEN_TOOLTIP, 
					(buttonWidget, matrices, mouseX, mouseY) -> {
						ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
						if (button.isJustHovered()) {
							parentScreen.renderTooltip(matrices, ModManager.TEXT_OPEN_TOOLTIP, mouseX, mouseY);
						} else if (button.isFocusedButNotHovered()) {
							parentScreen.renderTooltip(matrices, ModManager.TEXT_OPEN_TOOLTIP, button.x, button.y);
						}
				}) {
			};
			
		}
		openBtn.visible = !isListOpen;
		if (openBtn.visible)
		{
			parentScreen.addButton(openBtn);
		}
	}
	
	public interface OpenListAction {
		void onOpenList(DropDownListWidget widget);
	}
	
	public void onOpenList(DropDownListWidget widget) {
		isListOpen = !isListOpen;
		openBtn.visible = !isListOpen;
		if (openBtn.visible)
		{
			parentScreen.addButton(openBtn);
		}
		else
		{
			this.onOpenList.onOpenList(widget);
		}
	}
	
	public boolean isListOpen()
	{
		return isListOpen;
	}
	/***************************************************
	 *              SAVE BUTTON
	 **************************************************/
	/**
	 * Draws the Save Button at the correct position on the screen.
	 */
	private void drawSaveButton()
	{
		if (saveBtn == null)
		{
			saveBtnX =  openBtnX + ModManager.TOP_BTN_WIDTH;
			saveBtn = new ModMenuTexturedButtonWidget(saveBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.SAVE_BUTTON_LOCATION, this.height, 42, button -> {
				this.onSaveList(this);
			},ModManager.TEXT_SAVE_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
				ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
				if (button.isJustHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_SAVE_TOOLTIP, mouseX, mouseY);
				} else if (button.isFocusedButNotHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_SAVE_TOOLTIP, button.x, button.y);
				}
			}) {
			};
			parentScreen.addButton(saveBtn);
		}
	}
	
	public interface SaveListAction {
		void onSaveList(DropDownListWidget widget);
	}
	
	public void onSaveList(DropDownListWidget widget) {
		this.onSaveList.onSaveList(widget);
	}
	/***************************************************
	 *              ADD BUTTON
	 **************************************************/
	/**
	 * Draws the Add Button at the correct position on the screen.
	 */
	private void drawAddButton()
	{
		if (addBtn == null)
		{
			addBtnX =  saveBtnX + ModManager.TOP_BTN_WIDTH;
			addBtn = new ModMenuTexturedButtonWidget(addBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.ADD_BUTTON_LOCATION, this.height, 42, button -> {
				this.onAddEntry(this);
			},ModManager.TEXT_ADD_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
				ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
				if (button.isJustHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_ADD_TOOLTIP, mouseX, mouseY);
				} else if (button.isFocusedButNotHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_ADD_TOOLTIP, button.x, button.y);
				}
			}) {
			};
			parentScreen.addButton(addBtn);
		}
	}
	public interface AddEntryAction {
		void onAddEntry(DropDownListWidget widget);
	}
	
	public void onAddEntry(DropDownListWidget widget) {
		this.onAddEntry.onAddEntry(widget);
	}
	
    /***************************************************
	 *              EXPORT BUTTON
	 **************************************************/
	/**
	 * Draws the Export Button at the correct position on the screen.
	 */
	private void drawExportButton()
	{
		if (exportBtn == null)
		{
			exportBtnX =  addBtnX + ModManager.TOP_BTN_WIDTH;
			exportBtn = new ModMenuTexturedButtonWidget(exportBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.EXPORT_BUTTON_LOCATION, this.height, 42, button -> {
				this.onExportList(this);
			},ModManager.TEXT_EXPORT_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
				ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
				if (button.isJustHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_EXPORT_TOOLTIP, mouseX, mouseY);
				} else if (button.isFocusedButNotHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_EXPORT_TOOLTIP, button.x, button.y);
				}
			}) {
			};
			parentScreen.addButton(exportBtn);
		}
	}
	public interface ExportListAction {
		void onExportList(DropDownListWidget widget);
	}
	
	public void onExportList(DropDownListWidget widget) {
		this.onExportList.onExportList(widget);
	}
    /***************************************************
	 *              IMPORT BUTTON
	 **************************************************/
	/**
	 * Draws the Import Button at the correct position on the screen.
	 */
	private void drawImportButton()
	{
		if (importBtn == null)
		{
			importBtnX =  exportBtnX + ModManager.TOP_BTN_WIDTH;
			importBtn = new ModMenuTexturedButtonWidget(importBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.IMPORT_BUTTON_LOCATION, this.height, 42, button -> {
				this.onImportList(this);
			},ModManager.TEXT_IMPORT_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
				ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
				if (button.isJustHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_IMPORT_TOOLTIP, mouseX, mouseY);
				} else if (button.isFocusedButNotHovered()) {
					parentScreen.renderTooltip(matrices, ModManager.TEXT_IMPORT_TOOLTIP, button.x, button.y);
				}
			}) {
			};
			parentScreen.addButton(importBtn);
		}
	}
	public interface ImportListAction {
		void onImportList(DropDownListWidget widget);
	}
	
	public void onImportList(DropDownListWidget widget) {
		this.onImportList.onImportList(widget);
	}
	/***************************************************
	 *              RENDERING
	 **************************************************/

	@Override
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		if (!isListOpen)
		{
			listInput.visible = true;
			isListVisible = false;
		}
		else
		{
			listInput.visible = false;
			isListVisible = true;
		}
		super.renderList(matrices, x, y, mouseX, mouseY, delta);
		drawButtons();
	}

	@Override
	protected int getMaxPosition() {
		int maxPos = 0;
		if (isListOpen)
		{
			maxPos = this.getItemCount() * this.itemHeight + this.headerHeight;

		}
		else
		{
			maxPos = this.itemHeight + this.headerHeight;
		}
		return maxPos + 4;
	}
}
