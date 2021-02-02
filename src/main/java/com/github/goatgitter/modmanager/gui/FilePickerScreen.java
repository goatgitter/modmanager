package com.github.goatgitter.modmanager.gui;

import java.nio.file.Path;
import java.util.List;

import com.github.goatgitter.modmanager.ModManager;
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
	private static Log LOG = new Log("FilePickerScreen");
	private ClickDoneButtonAction onClickDoneButton;
	private LoadFileListAction onLoadFileList;
	private FileListWidget availFileListWidget = null;
	private Path directoryPath = null;
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	protected FilePickerScreen(Text title) {
		super(title);
	}
	
	public FilePickerScreen(ScreenBase previousScreen, 
			ClickDoneButtonAction onClickDoneButton, 
			Path directoryPath,
			LoadFileListAction onLoadFileList) {
		super(previousScreen, TITLE_ID, (StringListWidget.LoadListAction) widget  -> onLoadList(widget), 
				"",
				(StringListWidget.LoadListAction) widget  -> onLoadList(widget),
				"");
		this.onClickDoneButton = onClickDoneButton;
		this.directoryPath = directoryPath;
		this.onLoadFileList = onLoadFileList;
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
				(StringListWidget.ClickEntryAction) entry -> availableList.onClickEntry(entry), 
				(FileListWidget.MoveDirectoryUpAction) widget -> onMoveDirectoryUp(widget), 
				"",
				directoryPath);
		this.addChild(availFileListWidget);
		this.onLoadFileList();
		LOG.exit("init");
	}
	
	// Do nothing extra when a string list is loaded.  Handled by onLoadFileList
	public static void onLoadList(StringListWidget widget)
	{
		LOG.trace("onLoadList");
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
	
	public interface LoadFileListAction {
		void onLoadFileList(Path directoryPath, TwoStringListsWidget widget);
	}
	
	protected void onLoadFileList()
	{
		Path directoryPath = availFileListWidget.getDirectoryPath();
		TwoStringListsWidget widget = availableList;
		this.onLoadFileList.onLoadFileList(directoryPath, widget);
	}
	
	public void onMoveDirectoryUp(FileListWidget widget) {
		onLoadFileList();
	}

	public Path getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(Path directoryPath) {
		this.directoryPath = directoryPath;
		availFileListWidget.setDirectoryPath(directoryPath);
	}
}
