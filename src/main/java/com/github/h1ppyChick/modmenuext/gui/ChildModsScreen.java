package com.github.h1ppyChick.modmenuext.gui;

import com.github.h1ppyChick.modmenuext.ModMenuExt;
import com.github.h1ppyChick.modmenuext.util.CombinedLoader;
import com.github.h1ppyChick.modmenuext.util.Log;
import com.github.h1ppyChick.modmenuext.util.ModConfig;

import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ChildModsScreen extends TwoListsWidgetScreen{
	// Constants
	private final static String TITLE_ID = ModMenuExt.MOD_ID + ".config.screen.title";
	private static final Identifier ADD_BUTTON_LOCATION = new Identifier(ModMenuExt.MOD_ID, "add.png");
	private static final TranslatableText TEXT_ADD_TOOLTIP = new TranslatableText(ModMenuExt.MOD_ID + ".add.tooltip");
	// Instance Variables
	private Log LOG = new Log("ChildModsScreen");
	private CombinedLoader cl = new CombinedLoader();
	
	// Constructors
	public ChildModsScreen(Screen previousScreen) {
		super(previousScreen, TITLE_ID);
	}
	
	// Methods
	@Override
	public void init() {
		LOG.enter("init");
		addItemsToScreen();
		LOG.exit("init");
	}
	
	private void doneButtonClick()
	{
		this.onClose();
	}
	
	private void addButtonClick()
	{
		SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, ModMenuExt.TEXT_WARNING, ModMenuExt.TEXT_NOT_IMPL);
		
	}
	
	private void addItemsToScreen()
	{
		paneY = 28;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;
		LiteralText availTitle = new LiteralText("Available Mods");	
		
		this.availableMods = new TwoListsWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, cl.getAvailableModList(), this, availableModList, availTitle,
				(TwoListsWidget.LoadListAction) widget  -> widget.setContainerList(cl.getAvailableModList()),
				(TwoListsWidget.ClickEntryAction) entry -> {
					ModConfig.requestLoad(entry);
				}
		);
		
		this.availableMods.setLeftPos(0);
		this.children.add(this.availableMods);
		
		LiteralText selectedTitle = new LiteralText("Selected Mods");
		this.selectedMods = new TwoListsWidget(this.client, paneWidth, this.height, paneY + getY1Offset(), this.height + getY2Offset(), 36, cl.getSelectedModList(false) , this, selectedModList, selectedTitle,
				(TwoListsWidget.LoadListAction) list -> list.setContainerList(cl.getSelectedModList(false)),
				(TwoListsWidget.ClickEntryAction) entry -> ModConfig.requestUnload(entry)
		);
		this.selectedMods.setLeftPos(this.width / 2 + 4);
		this.children.add(this.selectedMods);
		drawDoneButton();
		drawAddButton();
	}
	
	private void drawDoneButton()
	{
		this.addButton(new ButtonWidget(this.width / 3 + 4, this.height - 28, 150, 20, ScreenTexts.DONE, button -> doneButtonClick()));
	}
	private void drawAddButton()
	{
		ButtonWidget addBtn = new ModMenuTexturedButtonWidget(paneWidth - 24, paneY-10, 21, 13, 0, 0, ADD_BUTTON_LOCATION, 21, 42, button -> {
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
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.availableMods.render(matrices, mouseX, mouseY, delta);
	    this.selectedMods.render(matrices, mouseX, mouseY, delta);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, this.title, this.width / 3 + 4, 8, 16777215);
		drawDoneButton();
		drawAddButton();
		super.render(matrices, mouseX, mouseY, delta);
	}
	

}