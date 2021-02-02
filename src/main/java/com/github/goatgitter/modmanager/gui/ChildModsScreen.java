package com.github.goatgitter.modmanager.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.config.Props;
import com.github.goatgitter.modmanager.util.Cabinet;
import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.ModConfig;
import com.github.goatgitter.modmanager.util.ModListLoader;
import com.github.goatgitter.modmanager.util.StickyNote;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class ChildModsScreen extends TwoListsWidgetScreen{
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private final static String TITLE_ID = ModManager.MOD_ID + ".config.screen.title";
	private static final TranslatableText TEXT_ADD_DESC = new TranslatableText(ModManager.MOD_ID + ".add.description");
	private static final TranslatableText TEXT_MOD_LIST = new TranslatableText(ModManager.MOD_ID + ".modlist");
	
	private static final int LIST_NAME_INPUT_HEIGHT = 14;
	
	
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
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;
		listNameInputWidth = paneWidth;
		listNameInputX = rightPaneX-4;
		LiteralText availTitle = new LiteralText("Available Mods");	
		
		this.availableMods = new TwoListsWidget(this.client, paneWidth, this.height, getTop(), getBottom(), 36, modListLoader.getAvailableModList(), this, availableModList, availTitle,
				(TwoListsWidget.LoadListAction) widget  -> widget.setContainerList(modListLoader.getAvailableModList()),
				(TwoListsWidget.ClickEntryAction) entry -> loadMod(entry)
		);
		
		this.availableMods.setLeftPos(ModManager.LEFT_PANE_X);
		this.children.add(this.availableMods);
		
		LiteralText selectedTitle = new LiteralText("Selected Mods");
		this.selectedMods = new TwoListsWidget(this.client, paneWidth, this.height, getTop() + LIST_NAME_INPUT_HEIGHT, getBottom(), 36, modListLoader.getSelectedModList(false) , this, selectedModList, selectedTitle,
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
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, TEXT_ADD_DESC, ModManager.LEFT_PANE_X, getTopRowY(), 16777215);
		
		drawDoneButton();
		this.modsList.getListInput().render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		restartRequired = true;
		Path modsDirectory = Props.getModsDirPath();
		
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
						StickyNote.addErrorMsg(client, ModManager.KEY_COPY_ERROR, path, destPath);
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					this.availableModList.reloadFilters();
					StickyNote.addSuccessMsg(client, ModManager.KEY_DROP_SUCCESS_1);
					StickyNote.addSuccessMsg(client, ModManager.KEY_DROP_SUCCESS_2);
				}
				StickyNote.showClientMsg(client);
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
	
	/***************************************************
	 *              LIST NAME INPUT BOX 
	 **************************************************/
	
	/**
	 * Draws the List Name Input Box at the correct position on the screen.
	 */
	private void drawListNameInput()
	{
		String listName = Props.getSelectedModListName();
		this.modsList = new DropDownListWidget(this.client, listNameInputX, listNameInputWidth, ModManager.TOP_ROW_HEIGHT, getTopRowY(), ModManager.TOP_ROW_HEIGHT, LIST_NAME_INPUT_HEIGHT, Props.getAllModLists(), this, TEXT_MOD_LIST,
				(DropDownListWidget.LoadListAction) widget  -> widget.setList(Props.getAllModLists()),
				(DropDownListWidget.ClickEntryAction) entry -> onClickEntry(entry),
				(DropDownListWidget.OpenListAction) widget -> onOpenList(widget),
				(DropDownListWidget.SaveListAction) widget -> saveButtonClick(widget),
				(DropDownListWidget.AddEntryAction) widget -> addButtonClick(widget),
				(DropDownListWidget.ExportListAction) widget -> onExportList(widget),
				(DropDownListWidget.ImportListAction) widget -> onImportList(widget),
				listName
		);
		this.children.add(modsList);
	}
	private void onClickEntry(StringEntry entry)
	{
		boolean result = Props.setSelectedModListName(modsList.getSelectedValue(), false);
		if (result)
		{
			restartRequired = true;
			selectedMods.onLoadList();
			modListLoader.updateAvailModListFile();
			availableMods.onLoadList();
		}
		else
		{
			StickyNote.showErrorMsg(client, ModManager.KEY_OPEN_ERROR);
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
		boolean result = Props.setSelectedModListName(getListNameInput(), false);
		if (result)
		{
			restartRequired = true;
			modsList.onLoadList();
			StickyNote.showSuccessMsg(client, ModManager.KEY_SAVE_SUCCESS);
		}
		else
		{
			StickyNote.showErrorMsg(client, ModManager.KEY_SAVE_ERROR);
		}
	}
	
	/**
	 * Performs the Add Action when the button is clicked.
	 */
	private void addButtonClick(DropDownListWidget widget)
	{
		boolean result = Props.setSelectedModListName(ModManager.NEW_LIST_NAME, true);
		if (result)
		{
			restartRequired = true;
			modsList.add(ModManager.NEW_LIST_NAME);
			modsList.select(ModManager.NEW_LIST_NAME);
			modsList.listInput.setText(ModManager.NEW_LIST_NAME);
			selectedMods.onNewList();
			modListLoader.updateAvailModListFile();
			availableMods.onLoadList();
			StickyNote.showSuccessMsg(client, ModManager.KEY_ADD_SUCCESS);
		}
		else
		{
			StickyNote.showErrorMsg(client, ModManager.KEY_ADD_ERROR);
		}
	}
	/**
	 * Performs the Export List Action when the button is clicked.
	 */
	private void onExportList(DropDownListWidget widget)
	{
		boolean result = Cabinet.storeModList(modsList.getSelectedValue(), client);
		if (result)
		{
			StickyNote.showSuccessMsg(client, ModManager.KEY_EXPORT_SUCCESS);
		}
		else
		{
			StickyNote.showErrorMsg(client, ModManager.KEY_EXPORT_ERROR);
		}
	}

	/**
	 * Performs the Import List Action when the button is clicked.
	 */
	private void onImportList(DropDownListWidget widget)
	{
		FilePickerScreen picker = new FilePickerScreen((ScreenBase) this, 
				(FilePickerScreen.ClickDoneButtonAction) selectedList -> onClickDoneButton(selectedList),
				Props.getModsDirPath(),
				(FilePickerScreen.LoadFileListAction) (directoryPath, fileListWidget) -> onLoadFileList(directoryPath, fileListWidget)
		);
		
		if (picker != null) {
			client.openScreen(picker);
		}
	}
	
	/**
	 * Performs the Load File List Action.
	 */
	private void onLoadFileList(Path directoryPath, TwoStringListsWidget widget)
	{
		List<String> fileList = Cabinet.getAvailArchives(directoryPath);
		widget.setList(fileList);
		widget.onLoadList();
	}
	
	/**
	 * Performs the Done Button Click Action.
	 */
	private void onClickDoneButton(List<String> selectedList)
	{
		for(String fileName: selectedList)
		{
			boolean result = Cabinet.retreiveModList(fileName, client, modListLoader);
			if (result)
			{
				modsList.onLoadList();
				modListLoader.updateAvailModListFile();
				availableMods.onLoadList();
				selectedMods.onLoadList();
			}
		}
		StickyNote.showClientMsg(client);
	}
	
	/**
	 * Performs the Mouse Button Click Action.
	 */
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