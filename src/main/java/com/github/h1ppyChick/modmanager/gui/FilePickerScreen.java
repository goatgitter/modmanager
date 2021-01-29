package com.github.h1ppyChick.modmanager.gui;

import java.util.List;

import com.github.h1ppyChick.modmanager.ModManager;
import com.github.h1ppyChick.modmanager.gui.StringListWidget.LoadListAction;
import com.github.h1ppyChick.modmanager.util.Log;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 */
public class FilePickerScreen extends TwoStringListsScreen {
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private final static String TITLE_ID = ModManager.MOD_ID + ".filepicker.title";

	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("FilePickerScreen");
	private ClickDoneButtonAction onClickDoneButton;
	
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	protected FilePickerScreen(Text title) {
		super(title);
	}
	
	public FilePickerScreen(ScreenBase previousScreen, LoadListAction onLoadAvailList, 
			LoadListAction onLoadSelectedList,
			ClickDoneButtonAction onClickDoneButton) {
		super(previousScreen, TITLE_ID, onLoadAvailList, 
				"",
				onLoadSelectedList,
				"");
		this.onClickDoneButton = onClickDoneButton;
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	@Override
	public void init() {
		LOG.enter("init");
		super.init();
		drawDoneButton();
		this.addChild(availableList);
		this.addChild(selectedList);
		LOG.exit("init");
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
	
	public interface ClickDoneButtonAction {
		void onClickDoneButton(List<String> selectedList);
	}
	
	@Override
	protected void doneButtonClick()
	{
		this.onClickDoneButton.onClickDoneButton(selectedList.getAddedList());
		this.onClose();
	}
}
