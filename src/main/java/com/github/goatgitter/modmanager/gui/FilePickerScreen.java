package com.github.goatgitter.modmanager.gui;

import java.util.List;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.config.Props;
import com.github.goatgitter.modmanager.gui.StringListWidget.LoadListAction;
import com.github.goatgitter.modmanager.util.Log;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
/**
 * 
 * @author GoatGitter
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
	private FileListWidget availFileListWidget = null;	
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
		availFileListWidget = new FileListWidget(this.client,  
				ModManager.LEFT_PANE_X, 
				previousScreen.paneWidth, 
				this.height, 
				getTop(), 
				getBottom(), 
				36, 
				availableList.getValueList(), 
				this, 
				availableTitle, 
				availableList.onLoadList, 
				(StringListWidget.ClickEntryAction) entry -> availableList.onClickEntry(entry), 
				(FileListWidget.MoveDirectoryUpAction) widget -> onMoveDirectoryUp(widget), 
				"");
		availFileListWidget.setDirectoryPath(Props.getModsDirPath());
		this.addChild(availFileListWidget);
		LOG.exit("init");
	}
	
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		availFileListWidget.render(matrices, mouseX, mouseY, delta);
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
	
	public void onMoveDirectoryUp(FileListWidget widget) {
		
	}
}
