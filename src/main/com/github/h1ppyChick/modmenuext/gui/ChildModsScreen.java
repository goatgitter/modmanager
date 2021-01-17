package combined.gui;

import combined.ModMenuExt;
import combined.util.CombinedLoader;
import combined.util.Log;
import combined.util.ModConfig;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class ChildModsScreen extends TwoListsWidgetScreen{
	// Constants
	private final static String TITLE_ID = ModMenuExt.MOD_ID + ".config.screen.title";
	
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
	
	private void addItemsToScreen()
	{
		paneY = 28;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;
		this.addButton(new ButtonWidget(this.width / 3 + 4, this.height - 28, 150, 20, ScreenTexts.DONE, button -> doneButtonClick()));
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
		addDoneButton();
	}
	
	private void addDoneButton()
	{
		this.addButton(new ButtonWidget(this.width / 3 + 4, this.height - 28, 150, 20, ScreenTexts.DONE, button -> doneButtonClick()));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.availableMods.render(matrices, mouseX, mouseY, delta);
	    this.selectedMods.render(matrices, mouseX, mouseY, delta);
		DrawableHelper.drawTextWithShadow(matrices, this.textRenderer, this.title, this.width / 3 + 4, 8, 16777215);
		addDoneButton();
		super.render(matrices, mouseX, mouseY, delta);
	}
	

}