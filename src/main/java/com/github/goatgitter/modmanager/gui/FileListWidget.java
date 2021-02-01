package com.github.goatgitter.modmanager.gui;

import java.nio.file.Path;
import java.util.List;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.Stencil;

import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class FileListWidget extends TwoStringListsWidget {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("FileListWidget");
	protected final FileListWidget.MoveDirectoryUpAction onMoveDirectoryUp;
	private ButtonWidget moveDirUpBtn;
	private boolean isRootDir = false;
	private int textRight = 0;
	private LiteralText directoryPathText = null;
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public FileListWidget(MinecraftClient client, int left, int width, int height, 
			int y1, int y2, int entryHeight, List<String> widgetList, 
			ScreenBase parent, Text title, LoadListAction onLoadList, 
			ClickEntryAction onClickEntry, MoveDirectoryUpAction onMoveDirectoryUp,
			String selectedEntry) {
		super(client, left, width, height,y1, y2, entryHeight, widgetList, 
				parent,title, onLoadList,onClickEntry, selectedEntry);
		LOG.enter("FileListWidget");
		this.method_31322(false);
		this.setLeftPos(left);
	    this.centerListVertically = false;
		this.onMoveDirectoryUp = onMoveDirectoryUp;
		LOG.exit("FileListWidget");
	}

	/***************************************************
	 *              METHODS
	 **************************************************/	

	/***************************************************
	 *              BUTTONS
	 **************************************************/
	protected void drawButtons()
	{
		drawMoveDirectoryUpButton();
	}
	/***************************************************
	 *              MOVE DIRECTORY UP BUTTON
	 **************************************************/
	/**
	 * Draws the Move Directory Up Button at the correct position on the screen.
	 */
	private void drawMoveDirectoryUpButton()
	{
		if (moveDirUpBtn == null)
		{
			int left =  textRight;
			int top = getTop();	
			moveDirUpBtn = new ModMenuTexturedButtonWidget(left, top, ModManager.TOP_BTN_WIDTH, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.UP_BUTTON_LOCATION, ModManager.TOP_BTN_WIDTH, 42, 
					button -> {this.onMoveDirectoryUp(this);},
					ModManager.TEXT_DIR_UP_TOOLTIP, 
					(buttonWidget, matrices, mouseX, mouseY) -> {
						ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
						if (button.isJustHovered()) {
							parentScreen.renderTooltip(matrices, ModManager.TEXT_DIR_UP_TOOLTIP, mouseX, mouseY);
						} else if (button.isFocusedButNotHovered()) {
							parentScreen.renderTooltip(matrices, ModManager.TEXT_DIR_UP_TOOLTIP, button.x, button.y);
						}
				}) {
			};
			
		}
		moveDirUpBtn.visible = !isRootDir;
		if (moveDirUpBtn.visible)
		{
			parentScreen.addButton(moveDirUpBtn);
		}
	}
	
	public interface MoveDirectoryUpAction {
		void onMoveDirectoryUp(FileListWidget widget);
	}
	
	public void onMoveDirectoryUp(FileListWidget widget) {
		
		moveDirUpBtn.visible = !isRootDir;
		if (moveDirUpBtn.visible)
		{
			parentScreen.addButton(moveDirUpBtn);
		}
		else
		{
			this.onMoveDirectoryUp.onMoveDirectoryUp(widget);
		}
	}
	
	public boolean isRootDir()
	{
		return isRootDir;
	}
	
	private int getXOffset()
	{
		return 2;
	}
	
	private int getYOffset()
	{
		return -1 * ModManager.TOP_BTN_HEIGHT;
	}
	
	@Override
	public int getTop()
	{
		return parentScreen.getTop() + getYOffset();
	}
	
	public int getLeft()
	{
		return ModManager.LEFT_PANE_X + getXOffset();
	}
	
	public int getBottom()
	{
		return getTop() + ModManager.TOP_BTN_HEIGHT;
	}
	/***************************************************
	 *              RENDERING
	 **************************************************/
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		LiteralText text = getDirectoryPathText();
		int textLeft = getLeft();
		int textTop = getTop();
		int boxBottom = getBottom();
		textRight = client.textRenderer.getWidth(text) + 9;
		Stencil.setColorBlack();
		Stencil.rectangle(matrices, textLeft - 2, textRight, textTop - 1, boxBottom);
		DrawableHelper.drawTextWithShadow(matrices, client.textRenderer, text, textLeft, textTop, 16777215);
		drawButtons();
	}

	public LiteralText getDirectoryPathText() {
		return directoryPathText;
	}

	public void setDirectoryPathText(LiteralText directoryPathText) {
		this.directoryPathText = directoryPathText;
	}
	
	public void setDirectoryPath(Path directoryPath) {
		this.directoryPathText = new LiteralText(directoryPath.toString());
	}
}
