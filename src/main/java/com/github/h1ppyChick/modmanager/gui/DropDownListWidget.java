package com.github.h1ppyChick.modmanager.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.github.h1ppyChick.modmanager.ModManager;
import com.github.h1ppyChick.modmanager.util.Log;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import io.github.prospector.modmenu.mixin.EntryListWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class DropDownListWidget extends AlwaysSelectedEntryListWidget<StringEntry> {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("DropDownListWidget");
	private TwoListsWidgetScreen parentScreen = null;	
	private List<String> _entryList = null;
	private Set<String> addedEntries = new HashSet<>();
	private String selectedEntry = null;
	private boolean scrolling;
	private final Text title;
	protected final DropDownListWidget.LoadListAction onLoadList;
	protected final DropDownListWidget.ClickEntryAction onClickEntry;
	protected final DropDownListWidget.OpenListAction onOpenList;
	protected final DropDownListWidget.SaveListAction onSaveList;
	protected final DropDownListWidget.AddEntryAction onAddEntry;
	protected final DropDownListWidget.ExportListAction onExportList;
	protected final DropDownListWidget.ImportListAction onImportList;
	private ButtonWidget openBtn;
	private boolean isListOpen = false;
	private TextFieldWidget listInput;
	private int listInputX = 0;
	private int listInputY = 0;
	private int listInputWidth = 0;
	private int listInputHeight = 0;
	private int openBtnX = 0;
	private int saveBtnX = 0;
	private int addBtnX = 0;
	private int exportBtnX = 0;
	private int importBtnX = 0;
	private int topBtnY = 0;
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public DropDownListWidget(MinecraftClient client, int left, int width, int height, 
			int y1, int y2, int entryHeight, List<String> widgetList, 
			TwoListsWidgetScreen parent, Text title, LoadListAction onLoadList, 
			ClickEntryAction onClickEntry, OpenListAction onOpenList,
			SaveListAction onSaveList, AddEntryAction onAddEntry,
			ExportListAction onExportList,ImportListAction onImportList,
			String selectedEntry) {
		super(client, width, height, y1, y2, entryHeight);
		this.method_31322(false);
		this.setLeftPos(left);
	    this.centerListVertically = false;
	    this.title = title;
		this.parentScreen = parent;
		this.onLoadList = onLoadList;
		this.onOpenList = onOpenList;
		this.onClickEntry = onClickEntry;
		this.onSaveList = onSaveList;
		this.onAddEntry = onAddEntry;
		this.onExportList = onExportList;
		this.onImportList = onImportList;
		this.select(selectedEntry);	
		client.textRenderer.getClass();
		this.setRenderHeader(false, 0);
		
		listInputWidth = (this.width/3);
		listInputX = this.left + 50;
		listInputY = this.top + 6;
		listInputHeight = this.itemHeight-2;
		topBtnY = listInputY - 2;
		setScrollAmount(0);
		drawListInput();
		drawButtons();
		onLoadList();
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	public interface LoadListAction {
		void onLoadList(DropDownListWidget widget);
	}
	
	public void onLoadList() {
		this.clearEntries();
		addedEntries.clear();
		this.onLoadList.onLoadList(this);
		for (String entry: _entryList)
		{
			this.addEntry(new StringEntry(entry));
		}
	}
	
	public void onClickEntry(StringEntry entry) {
		if (isListOpen)
		{
			this.onClickEntry.onClickEntry(entry);
		}
		isListOpen = false;
	}

	public interface ClickEntryAction {
		void onClickEntry(StringEntry entry);
	}
	
	@Override
	protected boolean isFocused() {
		return parentScreen.getFocused() == this;
	}
	
	public void select(String value) {
		StringEntry selEntry = new StringEntry(value);
		this.setSelected(selEntry);
	}
	
	@Override
	public void setSelected(StringEntry entry) {
		super.setSelected(entry);
		selectedEntry = entry.value;
		if (listInput != null)
		{
			listInput.setText(selectedEntry);
			onClickEntry(entry);
			isListOpen = false;
			drawListInput();
			drawButtons();
		}
	}
	
	@Override
	protected boolean isSelectedItem(int index) {
		StringEntry selected = getSelected();
		return selected != null && selected.value.equals(getEntry(index).value);
	}
	
	public List<String> getValueList() {
		return _entryList;
	}

	public void setList(List<String> theList) {
		this._entryList = theList;
	}

	public boolean removeEntry(StringEntry entry) {
		addedEntries.remove(entry.getValue());
		return super.removeEntry(entry);
	}
	public String getSelectedValue()
	{
	    return this.getSelected().value;
	}
	
	public String getCurrentValue()
	{
		String currentText = this.listInput.getText();
		StringEntry currentEntry = getSelected();
		if (!currentText.equals(currentEntry.getValue()))
		{
			add(currentText);
			select(currentText);
			removeEntry(currentEntry);
		}
	    return getSelectedValue();
	}
	
	public StringEntry getEntry(int index)
	{
		StringEntry m = super.getEntry(index);
		return (StringEntry) m;
	}
	
	protected StringEntry remove(int index) {
		StringEntry entry = getEntry(index);
		addedEntries.remove(entry.getValue());
		return super.remove(index);
	}
	
	@Override
	protected int getRowTop(int index) {
		return this.top - (int)this.getScrollAmount() + index * this.itemHeight;
	}
	
	/***************************************************
	 *              LIST INPUT
	 **************************************************/
	private void drawListInput()
	{
		Text listNameText = new LiteralText(selectedEntry);
		if (listInput == null)
		{
			this.listInput = new TextFieldWidget(this.client.textRenderer, listInputX, listInputY, listInputWidth, listInputHeight, this.listInput, listNameText);
			Predicate<String> noSpecialChars = (s -> s.matches("^[a-zA-Z0-9-_]*$"));
			listInput.setTextPredicate(noSpecialChars);
		}
		this.listInput.setText(selectedEntry);
		listInput.visible = !isListOpen;
		if (listInput.visible)
		{
			parentScreen.setInitialFocus(listInput);
			parentScreen.addChild(listInput);
		}
	}
	
	public TextFieldWidget getListInput()
	{
		return listInput;
	}
	
	/***************************************************
	 *              BUTTONS
	 **************************************************/
	private void drawButtons()
	{
		drawOpenButton();
		drawSaveButton();
		drawAddButton();
		drawExportButton();
		drawImportButton();
	}
	/***************************************************
	 *              OPEN BUTTON
	 **************************************************/
	/**
	 * Draws the Open Button at the correct position on the screen.
	 */
	private void drawOpenButton()
	{
		if (openBtn == null)
		{
			openBtnX =  listInputX + listInputWidth;
			openBtn = new ModMenuTexturedButtonWidget(openBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.OPEN_BUTTON_LOCATION, this.height, 42, 
					button -> {this.onOpenList(this);},
					ModManager.TEXT_OPEN_TOOLTIP, 
					(buttonWidget, matrices, mouseX, mouseY) -> {
						ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
						if (button.isJustHovered()) {
							parentScreen.renderTooltip(matrices, ModManager.TEXT_OPEN_TOOLTIP, mouseX, mouseY);
						} else if (button.isFocusedButNotHovered()) {
							parentScreen.renderTooltip(matrices, ModManager.TEXT_OPEN_TOOLTIP, button.x, button.y);
						}
				}) {
			};
			
		}
		openBtn.visible = !isListOpen;
		if (openBtn.visible)
		{
			parentScreen.addButton(openBtn);
		}
	}
	
	public interface OpenListAction {
		void onOpenList(DropDownListWidget widget);
	}
	
	public void onOpenList(DropDownListWidget widget) {
		isListOpen = !isListOpen;
		openBtn.visible = !isListOpen;
		if (openBtn.visible)
		{
			parentScreen.addButton(openBtn);
		}
		else
		{
			this.onOpenList.onOpenList(widget);
		}
	}
	
	public boolean isListOpen()
	{
		return isListOpen;
	}
	/***************************************************
	 *              SAVE BUTTON
	 **************************************************/
	/**
	 * Draws the Save Button at the correct position on the screen.
	 */
	private void drawSaveButton()
	{
		saveBtnX =  openBtnX + ModManager.TOP_BTN_WIDTH;
		ButtonWidget saveBtn = new ModMenuTexturedButtonWidget(saveBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.SAVE_BUTTON_LOCATION, this.height, 42, button -> {
			this.onSaveList(this);
		},ModManager.TEXT_SAVE_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_SAVE_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_SAVE_TOOLTIP, button.x, button.y);
			}
		}) {
		};
		parentScreen.addButton(saveBtn);
	}
	
	public interface SaveListAction {
		void onSaveList(DropDownListWidget widget);
	}
	
	public void onSaveList(DropDownListWidget widget) {
		this.onSaveList.onSaveList(widget);
	}
	/***************************************************
	 *              ADD BUTTON
	 **************************************************/
	/**
	 * Draws the Add Button at the correct position on the screen.
	 */
	private void drawAddButton()
	{
		addBtnX =  saveBtnX + ModManager.TOP_BTN_WIDTH;
		ButtonWidget addBtn = new ModMenuTexturedButtonWidget(addBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.ADD_BUTTON_LOCATION, this.height, 42, button -> {
			this.onAddEntry(this);
		},ModManager.TEXT_ADD_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_ADD_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_ADD_TOOLTIP, button.x, button.y);
			}
		}) {
		};
		parentScreen.addButton(addBtn);
	}
	public interface AddEntryAction {
		void onAddEntry(DropDownListWidget widget);
	}
	
	public void onAddEntry(DropDownListWidget widget) {
		this.onAddEntry.onAddEntry(widget);
	}
	
	public void add(String value) {
		StringEntry newEntry = new StringEntry(value);
		this.addEntry(newEntry);
	}
	
	public int addEntry(StringEntry entry) {
		if (addedEntries.contains(entry.getValue())) {
			return 0;
		}
		addedEntries.add(entry.getValue());
		int i = super.addEntry(entry);
		if (entry.getValue().equals(selectedEntry)) {
			setSelected(entry);
		}
		return i;
	}
	
    /***************************************************
	 *              EXPORT BUTTON
	 **************************************************/
	/**
	 * Draws the Export Button at the correct position on the screen.
	 */
	private void drawExportButton()
	{
		exportBtnX =  addBtnX + ModManager.TOP_BTN_WIDTH;
		ButtonWidget exportBtn = new ModMenuTexturedButtonWidget(exportBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.EXPORT_BUTTON_LOCATION, this.height, 42, button -> {
			this.onExportList(this);
		},ModManager.TEXT_EXPORT_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_EXPORT_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_EXPORT_TOOLTIP, button.x, button.y);
			}
		}) {
		};
		parentScreen.addButton(exportBtn);
	}
	public interface ExportListAction {
		void onExportList(DropDownListWidget widget);
	}
	
	public void onExportList(DropDownListWidget widget) {
		this.onExportList.onExportList(widget);
	}
    /***************************************************
	 *              IMPORT BUTTON
	 **************************************************/
	/**
	 * Draws the Import Button at the correct position on the screen.
	 */
	private void drawImportButton()
	{
		importBtnX =  exportBtnX + ModManager.TOP_BTN_WIDTH;
		ButtonWidget importBtn = new ModMenuTexturedButtonWidget(importBtnX, topBtnY, this.height, ModManager.TOP_BTN_HEIGHT, 0, 0, ModManager.IMPORT_BUTTON_LOCATION, this.height, 42, button -> {
			this.onImportList(this);
		},ModManager.TEXT_IMPORT_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_IMPORT_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				parentScreen.renderTooltip(matrices, ModManager.TEXT_IMPORT_TOOLTIP, button.x, button.y);
			}
		}) {
		};
		parentScreen.addButton(importBtn);
	}
	public interface ImportListAction {
		void onImportList(DropDownListWidget widget);
	}
	
	public void onImportList(DropDownListWidget widget) {
		this.onImportList.onImportList(widget);
	}
	/***************************************************
	 *              RENDERING
	 **************************************************/
	private void drawBackgroundBox(Matrix4f matrix, BufferBuilder buffer, Tessellator tessellator, int leftX, int rightX, int topY, int bottomY)
	{
		float zero = 0.0F;
		// Paint a black box for given coordinates.
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.color4f(zero, zero, zero, 1.0F);
		buffer.begin(7, VertexFormats.POSITION);
		buffer.vertex(matrix, leftX, bottomY, 0.0F).next();
		buffer.vertex(matrix, rightX, bottomY, 0.0F).next();
		buffer.vertex(matrix, rightX, topY, 0.0F).next();
		buffer.vertex(matrix, leftX, topY, 0.0F).next();
		tessellator.draw();
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}
	
	private void renderListLabel(MatrixStack matrices, int x, int y,int rowWidth, int rowHeight) {
		DrawableHelper.drawTextWithShadow(matrices, this.client.textRenderer, this.title, x, y, 16777215);
	}
	
	@Override
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder buffer = tessellator_1.getBuffer();
		int entryHeight = this.itemHeight;
		int rowWidth = this.getRowWidth();
		this.bottom = this.top + this.height;
		int boxTop = this.top;
		int boxRightX = this.right;
		int boxLeftX = this.left;
		Matrix4f matrix = matrices.peek().getModel();
		int boxBottom = this.bottom;
		drawBackgroundBox(matrix, buffer, tessellator_1, boxLeftX, boxRightX, boxTop, boxBottom);
		
		
		// Add the label for the field to the left of the drop down list.
		renderListLabel(matrices,this.left+3,listInputY, rowWidth, entryHeight);
		if (!isListOpen)
		{
			listInput.visible = true;
		}
		else
		{
			listInput.visible = false;
			boxRightX = getRowLeft() + getRowWidth() +1;
			boxLeftX = getRowLeft();
			boxBottom = this.top + itemCount * this.itemHeight;
			drawBackgroundBox(matrix, buffer, tessellator_1, boxLeftX, boxRightX, boxTop, boxBottom);
			
			for (int index = 0; index < itemCount; ++index) {
				int entryTop = this.getRowTop(index) + 4;
				int entryBottom = this.getRowTop(index) + this.itemHeight;
				if (entryBottom >= this.top && entryTop <= getScrollBottom()) {
					StringEntry entry = this.getEntry(index);
					int entryLeft;
					int entryRight;
					if (((EntryListWidgetAccessor) this).isRenderSelection() && this.isSelectedItem(index)) {
						entryLeft = getEntryLeft() - 1;
						entryRight = getEntryRight() +1;
						RenderSystem.disableTexture();
						//Green
						RenderSystem.color4f(0,1,0,1);
						buffer.begin(7, VertexFormats.POSITION);
						buffer.vertex(matrix, entryLeft, entryBottom + 1, 0.0F).next();
						buffer.vertex(matrix, entryRight, entryBottom + 1, 0.0F).next();
						buffer.vertex(matrix, entryRight, entryTop - 1, 0.0F).next();
						buffer.vertex(matrix, entryLeft, entryTop - 1, 0.0F).next();
						tessellator_1.draw();
						RenderSystem.enableTexture();
					}
					entryLeft = getEntryLeft();
					entryRight = getEntryRight();
					this.bottom = Math.max(this.bottom, entryBottom);
					boolean isHovered = this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry);
					if (isHovered)
					{
						entryLeft = getEntryLeft() - 1;
						entryRight = getEntryRight() +1;
						RenderSystem.disableTexture();
						//Blue
						RenderSystem.color4f(0,0,1,1);
						buffer.begin(7, VertexFormats.POSITION);
						buffer.vertex(matrix, entryLeft, entryBottom + 1, 0.0F).next();
						buffer.vertex(matrix, entryRight, entryBottom + 1, 0.0F).next();
						buffer.vertex(matrix, entryRight, entryTop - 1, 0.0F).next();
						buffer.vertex(matrix, entryLeft, entryTop - 1, 0.0F).next();
						tessellator_1.draw();
						RenderSystem.enableTexture();
					}
					entryLeft = getEntryLeft();
					entryRight = getEntryRight();
					entry.render(matrices, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, isHovered, delta);
				}
			}
		}
		this.listInput.render(matrices, mouseX, mouseY, delta);
		drawButtons();
	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPositionX() && double_1 < (double) (this.getScrollbarPositionX() + 6);
	}
	@Override
	 public boolean isMouseOver(double mouseX, double mouseY) {
		// Break up expression to make it more readable.
		boolean isMouseBelowTop = mouseY >= (double)this.top;
		boolean isMouseAboveBottom = mouseY <= (double)this.bottom;
		boolean isMouseBeyondLeft = mouseX >= (double)this.left;
		boolean isMouseBeforeRight = mouseX <= (double)this.right;
		
	    return  isMouseBelowTop && isMouseAboveBottom && isMouseBeyondLeft && isMouseBeforeRight ;
	}
	
	 public boolean isMouseOverEntry(double mouseX, double mouseY) {
		// Break up expression to make it more readable.
		boolean isMouseBelowTop = mouseY >= (double)this.top;
		boolean isMouseAboveBottom = mouseY <= (double)this.bottom;
		boolean isMouseBeyondLeft = mouseX >= (double)getEntryLeft();
		boolean isMouseBeforeRight = mouseX <= (double)getEntryRight();
		
	    return  isMouseBelowTop && isMouseAboveBottom && isMouseBeyondLeft && isMouseBeforeRight ;
	}
	
	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (!this.isMouseOver(double_1, double_2)) {
			return false;
		} else {
			StringEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
				setSelected(entry);
				return true;
			} else if (int_1 == 0) {
				this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
				return true;
			}
			return this.scrolling;
		}
	}
	
	public final StringEntry getEntryAtPos(double x, double y) {
		int posInList = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount();
		// Index is Zero Based!
		int index = MathHelper.floor((posInList / this.itemHeight));
		// Break up the expression to make it readable/debuggable!
		boolean isMouseBeforeScrollbar = x < (double) this.getScrollbarPositionX();
		boolean isMouseAfterEntryLeft = x >= (double) getEntryLeft();
		boolean isMouseBeforeEntryRight = x <= (double) getEntryRight();
		boolean isIndexPositive = index >= 0;
		boolean isPosInListPositive = posInList >= 0;
		// Item Count is not zero-based!
		boolean isIndexInList = index < this.getItemCount();
		
		return isMouseBeforeScrollbar && isMouseAfterEntryLeft && isMouseBeforeEntryRight 
				&& isIndexPositive && isPosInListPositive && isIndexInList
				? (StringEntry) this.children().get(index) 
				: null;
	}
	private int getEntryLeft()
	{
		return getRowLeft();
	}
	
	private int getEntryRight()
	{
		return getRowLeft() + getRowWidth();
	}
	@Override
	protected int getScrollbarPositionX() {
		return this.right - 6;
	}
	
	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - getScrollBottom());
		if (denominator <= 0 && parentScreen != null) {
			parentScreen.updateScrollPercent(0);
		} else {
			parentScreen.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - getScrollBottom()));
		}
	}
	
	@Override
	public int getMaxScroll() {
      return Math.max(0, this.getMaxPosition() - getScrollBottom());
	}
	
	private int getScrollBottom()
	{
		return parentScreen.height - 20;
	}
	@Override
	public int getRowWidth() {
		return listInputWidth;
	}

	@Override
	public int getRowLeft() {
		return listInputX;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.top;
	}

	public Screen getParent() {
		return parentScreen;
	}

	@Override
	protected int getMaxPosition() {
		int maxPos = 0;
		if (isListOpen)
		{
			maxPos = this.getItemCount() * this.itemHeight + this.headerHeight;

		}
		else
		{
			maxPos = this.itemHeight + this.headerHeight;
		}
		return maxPos + 4;
	}
}
