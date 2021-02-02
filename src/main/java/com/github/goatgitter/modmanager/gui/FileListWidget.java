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
	private static Log LOG = new Log("FileListWidget");
	protected final FileListWidget.MoveDirectoryUpAction onMoveDirectoryUp;
	private ButtonWidget moveDirUpBtn;
	private boolean isRootDir = false;
	private int textRight = 0;
	private LiteralText directoryPathText = null;
	private Path directoryPath = null;
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public FileListWidget(MinecraftClient client, int left, int width, int height, 
			int y1, int y2, int entryHeight, List<String> widgetList, 
			ScreenBase parent, Text title, 
			ClickEntryAction onClickEntry, MoveDirectoryUpAction onMoveDirectoryUp,
			String selectedEntry,
			Path directoryPath) {
		super(client, left, width, height,y1, y2, entryHeight, widgetList, 
				parent,title, 
				(StringListWidget.LoadListAction) widget  -> onLoadList(widget),
				onClickEntry, selectedEntry);
		LOG.enter("FileListWidget");
		this.method_31322(false);
		this.setLeftPos(left);
	    this.centerListVertically = false;
		this.onMoveDirectoryUp = onMoveDirectoryUp;
		setDirectoryPath(directoryPath);
		LOG.exit("FileListWidget");
	}

	/***************************************************
	 *              METHODS
	 **************************************************/	
	// Do nothing extra when a string list is loaded.  Handled by onLoadFileList
	public static void onLoadList(StringListWidget widget)
	{
		LOG.debug("onLoadList");
	}
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
		if (moveDirUpBtn != null)
		{
			parentScreen.removeButton(moveDirUpBtn);
		}
		if (!isRootDir())
		{
			moveDirUpBtn = new ModMenuTexturedButtonWidget(textRight, getTop(), ModManager.TOP_BTN_WIDTH, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.UP_BUTTON_LOCATION, ModManager.TOP_BTN_WIDTH, 42, 
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
			parentScreen.addButton(moveDirUpBtn);
		}
	}
	
	public interface MoveDirectoryUpAction {
		void onMoveDirectoryUp(FileListWidget widget);
	}
	
	public void onMoveDirectoryUp(FileListWidget widget) {
		if (!isRootDir())
		{
			setDirectoryPath(directoryPath.getParent());
			setIsRootDir(directoryPath.getParent() == null);
			this.onMoveDirectoryUp.onMoveDirectoryUp(widget);
		}
	}
	
	private int getXOffset()
	{
		return 2;
	}
	
	private int getYOffset()
	{
		return -1 * ModManager.TOP_BTN_HEIGHT;
	}
	
	/***************************************************
	 *              RENDERING
	 **************************************************/
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		Stencil.setColorBlack();
		Stencil.rectangle(matrices, getLeft() - 2, textRight, getTop() - 1, getBottom());
		DrawableHelper.drawTextWithShadow(matrices, client.textRenderer, getDirectoryPathText(), getLeft(), getTop(), 16777215);
		drawButtons();
	}
	
	/***************************************************
	 *              PROPERTY ACCESSORS
	 **************************************************/
	public boolean isRootDir()
	{
		return isRootDir;
	}
	
	public void setIsRootDir(boolean newValue)
	{
		isRootDir = newValue;
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
	
	public LiteralText getDirectoryPathText() {
		return directoryPathText;
	}

	public void setDirectoryPathText(LiteralText directoryPathText) {
		this.directoryPathText = directoryPathText;
	}
	
	public void setDirectoryPath(Path directoryPath) {
		this.directoryPath = directoryPath;
		this.directoryPathText = new LiteralText(directoryPath.toString());
		this.textRight = client.textRenderer.getWidth(getDirectoryPathText()) + 9;
	}
	
	public Path getDirectoryPath()
	{
		return this.directoryPath;
	}
}
