package com.github.h1ppyChick.modmenuext.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.github.h1ppyChick.modmenuext.ModMenuExt;
import com.github.h1ppyChick.modmenuext.util.Log;
import com.github.h1ppyChick.modmenuext.util.ModConfig;
import com.github.h1ppyChick.modmenuext.util.ModListLoader;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class ChildModsScreen extends TwoListsWidgetScreen{
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private final static String TITLE_ID = ModMenuExt.MOD_ID + ".config.screen.title";
	private static final TranslatableText TEXT_ADD_DESC = new TranslatableText(ModMenuExt.MOD_ID + ".add.description");
	private static final TranslatableText TEXT_MOD_LIST = new TranslatableText(ModMenuExt.MOD_ID + ".modlist");
	private static final int TOP_ROW_HEIGHT = 20;
	private static final int LIST_NAME_INPUT_HEIGHT = 14;
	private static final int LEFT_PANE_X = 5;
	
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("ChildModsScreen");
	private ModListLoader modListLoader = new ModListLoader();
	private int listNameInputX;
	private int listNameInputWidth;
	
	private DropDownListWidget modsList;
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public ChildModsScreen(Screen previousScreen) {
		super(previousScreen, TITLE_ID);
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	@Override
	public void init() {
		LOG.enter("init");
		addItemsToScreen();
		LOG.exit("init");
	}
	
	private void addItemsToScreen()
	{
		paneY = 28;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;
		listNameInputWidth = paneWidth;
		listNameInputX = rightPaneX-4;
		LiteralText availTitle = new LiteralText("Available Mods");	
		
		this.availableMods = new TwoListsWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, modListLoader.getAvailableModList(), this, availableModList, availTitle,
				(TwoListsWidget.LoadListAction) widget  -> widget.setContainerList(modListLoader.getAvailableModList()),
				(TwoListsWidget.ClickEntryAction) entry -> loadMod(entry)
		);
		
		this.availableMods.setLeftPos(LEFT_PANE_X);
		this.children.add(this.availableMods);
		
		LiteralText selectedTitle = new LiteralText("Selected Mods");
		this.selectedMods = new TwoListsWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, modListLoader.getSelectedModList(false) , this, selectedModList, selectedTitle,
				(TwoListsWidget.LoadListAction) list -> list.setContainerList(modListLoader.getSelectedModList(false)),
				(TwoListsWidget.ClickEntryAction) entry -> unloadMod(entry)
		);
		this.selectedMods.setLeftPos(this.width / 2 + 4);
		this.children.add(this.selectedMods);
		drawDoneButton();
		drawListNameInput();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.availableMods.render(matrices, mouseX, mouseY, delta);
	    this.selectedMods.render(matrices, mouseX, mouseY, delta);
	    this.modsList.render(matrices, mouseX, mouseY, delta);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, this.title, this.width / 3 + 4, 8, 16777215);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, TEXT_ADD_DESC, LEFT_PANE_X, getTopRowY(), 16777215);
		
		drawDoneButton();
		this.modsList.getListInput().render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		restartRequired = true;
		Path modsDirectory = modListLoader.getModsDir();
		
		List<Path> mods = paths.stream()
				.filter(ModListLoader::isFabricMod)
				.collect(Collectors.toList());

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.joining(", "));
		Path listFile = modListLoader.getAvailModListFile();

		this.client.openScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					Path destPath = modsDirectory.resolve(path.getFileName());
					try {
						Files.copy(path, destPath);
						modListLoader.addJarToFile(listFile, destPath);
					} catch (IOException e) {
						LOG.warn(String.format("Failed to copy mod from {} to {}", path, destPath));
						SystemToast.addPackCopyFailure(client, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					this.availableModList.reloadFilters();
					SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, new TranslatableText("modmenu.dropSuccessful.line1"), new TranslatableText("modmenu.dropSuccessful.line2"));
				}
			}
			this.client.openScreen(this);
		}, new TranslatableText("modmenu.dropConfirm"), new LiteralText(modList)));
	}
	
	private void loadMod(ChildModEntry mod) {
		restartRequired = true;
		ModConfig.requestLoad(mod);
	}
	
	private void unloadMod(ChildModEntry mod) {
		restartRequired = true;
		ModConfig.requestUnload(mod);
	}
	
	private int getTopRowY()
	{
		return paneY-8;
	}
	
	/***************************************************
	 *              DONE BUTTON
	 **************************************************/
	/**
	 * Draws the Done Button at the correct position on the screen.
	 */
	private void drawDoneButton()
	{
		this.addButton(new ButtonWidget(this.width / 3 + 4, this.height - 28, 150, 20, ScreenTexts.DONE, button -> doneButtonClick()));
	}
	/**
	 * Performs the Done Action when the button is clicked.
	 * Closes the screen, with restart option, if required.
	 * {@link #onClose()}
	 */
	private void doneButtonClick()
	{
		this.onClose();
	}
	
	
	/***************************************************
	 *              LIST NAME INPUT BOX 
	 **************************************************/
	
	/**
	 * Draws the List Name Input Box at the correct position on the screen.
	 */
	private void drawListNameInput()
	{
		String listName = modListLoader.getSelectedModListName();
		this.modsList = new DropDownListWidget(this.client, listNameInputX, listNameInputWidth, TOP_ROW_HEIGHT, getTopRowY(), TOP_ROW_HEIGHT, LIST_NAME_INPUT_HEIGHT, modListLoader.getAllModLists(), this, TEXT_MOD_LIST,
				(DropDownListWidget.LoadListAction) widget  -> widget.setList(modListLoader.getAllModLists()),
				(DropDownListWidget.ClickEntryAction) entry -> onClickEntry(entry),
				(DropDownListWidget.OpenListAction) widget -> onOpenList(widget),
				(DropDownListWidget.SaveListAction) widget -> saveButtonClick(widget),
				(DropDownListWidget.AddEntryAction) widget -> addButtonClick(widget),
				listName
		);
		this.children.add(modsList);
	}
	private void onClickEntry(StringEntry entry)
	{
		boolean result = modListLoader.setSelectedModListName(modsList.getSelectedValue(), false);
		if (result)
		{
			restartRequired = true;
			selectedMods.onLoadList();
			modListLoader.updateAvailModListFile();
			availableMods.onLoadList();
		}
		else
		{
			SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_ERROR, ModMenuExt.TEXT_OPEN_ERROR);
		}
	}
	
	private void onOpenList(DropDownListWidget widget)
	{
		selectedMods.onNewList();
		modListLoader.updateAvailModListFile();
		availableMods.onLoadList();
	}
	
	/**
	 * Performs the Save Action when the button is clicked.
	 */
	private void saveButtonClick(DropDownListWidget widget)
	{
		boolean result = modListLoader.setSelectedModListName(getListNameInput(), false);
		if (result)
		{
			restartRequired = true;
			SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_SUCCESS, ModMenuExt.TEXT_SAVE_SUCCESS);
		}
		else
		{
			SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_ERROR, ModMenuExt.TEXT_SAVE_ERROR);
		}
	}
	
	/**
	 * Performs the Add Action when the button is clicked.
	 */
	private void addButtonClick(DropDownListWidget widget)
	{
		boolean result = modListLoader.setSelectedModListName(ModMenuExt.NEW_LIST_NAME, true);
		if (result)
		{
			restartRequired = true;
			this.modsList.add(ModMenuExt.NEW_LIST_NAME);
			this.modsList.select(ModMenuExt.NEW_LIST_NAME);
			selectedMods.onNewList();
			modListLoader.updateAvailModListFile();
			availableMods.onLoadList();
			SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_SUCCESS, ModMenuExt.TEXT_ADD_SUCCESS);
		}
		else
		{
			SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_ERROR, ModMenuExt.TEXT_ADD_ERROR);
		}
	}
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (modsList.isListOpen() &&  modsList.isMouseOverEntry(mouseX, mouseY))
		{
			this.modsList.mouseClicked(mouseX, mouseY, button);
			return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	/**
	 * Makes the cursor blink inside the list name input
	 */
	@Override
	public void tick() {
		this.modsList.getListInput().tick();
	}
	
	/**
	 *   Method for retrieving the list name.
	 * @return String - The List Name from the UI Input.
	 */
	private String getListNameInput() {
		return modsList.getCurrentValue();
	}

}