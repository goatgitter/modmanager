package com.github.h1ppyChick.modmenuext.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.github.h1ppyChick.modmenuext.ModMenuExt;
import com.github.h1ppyChick.modmenuext.util.CombinedLoader;
import com.github.h1ppyChick.modmenuext.util.Log;
import com.github.h1ppyChick.modmenuext.util.ModConfig;

import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ChildModsScreen extends TwoListsWidgetScreen{
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private final static String TITLE_ID = ModMenuExt.MOD_ID + ".config.screen.title";
	private static final Identifier ADD_BUTTON_LOCATION = new Identifier(ModMenuExt.MOD_ID, "add.png");
	private static final TranslatableText TEXT_ADD_TOOLTIP = new TranslatableText(ModMenuExt.MOD_ID + ".add.tooltip");
	private static final TranslatableText TEXT_ADD_DESC = new TranslatableText(ModMenuExt.MOD_ID + ".add.description");
	private static final TranslatableText TEXT_MOD_LIST = new TranslatableText(ModMenuExt.MOD_ID + ".modlist");
	
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("ChildModsScreen");
	private CombinedLoader cl = new CombinedLoader();
	private TextFieldWidget listNameInput;
	private int listNameInputX;
	private int listNameInputWidth;
	private int topRowItemHeight = 13;
	private int topRowHeight = 21;
	private int leftPaneX = 5;
	
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
		listNameInputWidth = paneWidth / 3;
		listNameInputX = rightPaneX + listNameInputWidth + 2;
		LiteralText availTitle = new LiteralText("Available Mods");	
		
		this.availableMods = new TwoListsWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, cl.getAvailableModList(), this, availableModList, availTitle,
				(TwoListsWidget.LoadListAction) widget  -> widget.setContainerList(cl.getAvailableModList()),
				(TwoListsWidget.ClickEntryAction) entry -> loadMod(entry)
		);
		
		this.availableMods.setLeftPos(leftPaneX);
		this.children.add(this.availableMods);
		
		LiteralText selectedTitle = new LiteralText("Selected Mods");
		this.selectedMods = new TwoListsWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, cl.getSelectedModList(false) , this, selectedModList, selectedTitle,
				(TwoListsWidget.LoadListAction) list -> list.setContainerList(cl.getSelectedModList(false)),
				(TwoListsWidget.ClickEntryAction) entry -> unloadMod(entry)
		);
		this.selectedMods.setLeftPos(this.width / 2 + 4);
		this.children.add(this.selectedMods);		
		drawDoneButton();
		drawAddButton();
		drawListNameInput();
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.availableMods.render(matrices, mouseX, mouseY, delta);
	    this.selectedMods.render(matrices, mouseX, mouseY, delta);
	    this.listNameInput.render(matrices, mouseX, mouseY, delta);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, this.title, this.width / 3 + 4, 8, 16777215);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, TEXT_ADD_DESC, leftPaneX, getTopRowY(), 16777215);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, TEXT_MOD_LIST, rightPaneX, getTopRowY(), 16777215);
		
		drawDoneButton();
		drawAddButton();
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		restartRequired = true;
		Path modsDirectory = cl.getModsDir();
		
		List<Path> mods = paths.stream()
				.filter(CombinedLoader::isFabricMod)
				.collect(Collectors.toList());

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.joining(", "));
		Path listFile = cl.getUnLoadFile();

		this.client.openScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
						cl.addJarToFile(listFile, path);
					} catch (IOException e) {
						LOG.warn(String.format("Failed to copy mod from {} to {}", path, modsDirectory.resolve(path.getFileName())));
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
		return paneY-10;
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
	 *              ADD BUTTON
	 **************************************************/
	/**
	 * Draws the Add Button at the correct position on the screen.
	 */
	private void drawAddButton()
	{
		int addBtnX =  listNameInputX + listNameInputWidth + 5;
		ButtonWidget addBtn = new ModMenuTexturedButtonWidget(addBtnX, getTopRowY(), topRowHeight, topRowItemHeight, 0, 0, ADD_BUTTON_LOCATION, topRowHeight, 42, button -> {
			addButtonClick();
		},TEXT_ADD_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, TEXT_ADD_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, TEXT_ADD_TOOLTIP, button.x, button.y);
			}
		}) {
		};
		this.addButton(addBtn);
	}
	
	/**
	 * Performs the Add Action when the button is clicked.
	 * Implementation is TBD, so just show a user message for now.
	 */
	private void addButtonClick()
	{
		SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_WARNING, ModMenuExt.TEXT_NOT_IMPL);
	}
	/***************************************************
	 *              LIST NAME INPUT BOX 
	 **************************************************/
	
	/**
	 * Draws the List Name Input Box at the correct position on the screen.
	 */
	private void drawListNameInput()
	{
		String listName = cl.getLoadFile().getFileName().toString();
		listName = listName.substring(0, listName.lastIndexOf(".txt"));
		Text listNameText = new LiteralText(listName);
		this.listNameInput = new TextFieldWidget(this.textRenderer, listNameInputX, getTopRowY(), listNameInputWidth, topRowItemHeight, this.listNameInput, listNameText);
		this.listNameInput.setText(listName);
		this.children.add(this.listNameInput);
		this.setInitialFocus(this.listNameInput);
	}
	
	/**
	 * Makes the cursor blink inside the list name input
	 */
	@Override
	public void tick() {
		this.listNameInput.tick();
	}
	
	/**
	 *   Method for retrieving the list name.
	 *   Will be used later.
	 * @return String - The List Name from the UI Input.
	 */
	public String getListNameInput() {
		return listNameInput.getText();
	}

}