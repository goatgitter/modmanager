package com.github.goatgitter.modmanager.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.Stencil;

import io.github.prospector.modmenu.mixin.EntryListWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class StringListWidget extends AlwaysSelectedEntryListWidget<StringEntry> {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private Log LOG = new Log("StringListWidget");
	protected ScreenBase parentScreen = null;	
	private List<String> _entryList = new ArrayList<String>();
	private Set<String> addedEntries = new HashSet<>();
	protected String selectedEntry = null;
	private boolean scrolling;
	protected final Text title;
	protected final StringListWidget.LoadListAction onLoadList;
	protected final StringListWidget.ClickEntryAction onClickEntry;
	protected TextFieldWidget listInput;
	protected int listInputX = 0;
	protected int listInputY = 0;
	protected int listInputWidth = 0;
	private int listInputHeight = 0;
	protected boolean isListVisible = true;

	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	public StringListWidget(MinecraftClient client, int left, int width, int height, 
			int y1, int y2, int entryHeight, List<String> widgetList, 
			ScreenBase parent, Text title, LoadListAction onLoadList, 
			ClickEntryAction onClickEntry, 
			String selectedEntry) {
		super(client, width, height, y1, y2, entryHeight);
		this.method_31322(false);
		this.setLeftPos(left);
	    this.centerListVertically = false;
	    this.title = title;
		this.parentScreen = parent;
		this.onLoadList = onLoadList;
		this.onClickEntry = onClickEntry;
		setList(widgetList);
		this.select(selectedEntry);	
		client.textRenderer.getClass();
		this.setRenderHeader(false, 0);
		
		listInputWidth = (this.width/3);
		listInputX = this.left + 50;
		listInputY = this.top + 6;
		listInputHeight = this.itemHeight-2;
		setScrollAmount(0);
		drawListInput();
		onLoadList();
	}
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	public interface LoadListAction {
		void onLoadList(StringListWidget widget);
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
		this.onClickEntry.onClickEntry(entry);
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
	}
	
	@Override
	protected boolean isSelectedItem(int index) {
		StringEntry selected = getSelected();
		return selected != null && selected.value.equals(getEntry(index).value);
	}
	
	public List<String> getValueList() {
		return _entryList;
	}
	
	public List<String> getAddedList() {
		Object[] addedObjects = addedEntries.toArray();
		String[] addedValues = Arrays.copyOf(addedObjects, addedObjects.length, String[].class);
		List<String> addedList =  Arrays.asList(addedValues);
		return addedList;
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

	public StringEntry getEntry(int index)
	{
		StringEntry m = super.getEntry(index);
		return (StringEntry) m;
	}
	
	protected boolean hasEntry(String value) {
		boolean hasEntry = false;
		hasEntry = _entryList.contains(value);
		hasEntry = hasEntry || addedEntries.contains(value);
		return hasEntry;
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
	protected void drawListInput()
	{
		Text listNameText = new LiteralText(selectedEntry);
		if (listInput == null)
		{
			this.listInput = new TextFieldWidget(this.client.textRenderer, listInputX, listInputY, listInputWidth, listInputHeight, this.listInput, listNameText);
		}
		parentScreen.setInitialFocus(listInput);
		parentScreen.addChild(listInput);
	}
	
	public TextFieldWidget getListInput()
	{
		return listInput;
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
		if (!_entryList.contains(entry.getDisplayValue()))
		{
			_entryList.add(entry.getDisplayValue());
		}
		int i = super.addEntry(entry);
		if (entry.getValue().equals(selectedEntry)) {
			setSelected(entry);
		}
		return i;
	}
	/***************************************************
	 *              RENDERING
	 **************************************************/
	
	protected void renderListLabel(MatrixStack matrices) {
		DrawableHelper.drawTextWithShadow(matrices, this.client.textRenderer, this.title, this.left+3, listInputY, 16777215);
	}
	
	@Override
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		this.bottom = this.top + this.height;
		Stencil.setColorBlack();
		Stencil.rectangle(matrices, this.left, this.right, this.top, this.bottom);
		// Add the label for the field
		renderListLabel(matrices);
		if (isListVisible)
		{
			renderListEntries(matrices,mouseX, mouseY, delta);
		}
		else
		{
			this.listInput.render(matrices, mouseX, mouseY, delta);
		}
	}

	protected void renderListEntries(MatrixStack matrices,int mouseX, int mouseY, float delta)
	{
		int boxTop = this.top;
		int boxRightX = getRowLeft() + getRowWidth() +1;
		int boxLeftX = getRowLeft();
		int boxBottom = this.top + this.getItemCount() * this.itemHeight;
		Stencil.setColorBlack();
		Stencil.rectangle(matrices, boxLeftX, boxRightX, boxTop, boxBottom);
		
		for (int index = 0; index < this.getItemCount(); ++index) {
			int entryTop = this.getRowTop(index) + 4;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= getScrollBottom()) {
				StringEntry entry = this.getEntry(index);
				int entryLeft = getEntryLeft();
				int entryRight = getEntryRight();
				entry.setSelectedItem(false);
				if (((EntryListWidgetAccessor) this).isRenderSelection() && this.isSelectedItem(index)) {
					entry.setSelectedItem(true);
				}
				this.bottom = Math.max(this.bottom, entryBottom);
				boolean isHovered = this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry);
				entry.render(matrices, index, entryLeft, entryRight, entryTop, entryBottom,  mouseX, mouseY, isHovered, delta);
			}
		}
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
				onClickEntry(entry);
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
	protected int getEntryLeft()
	{
		return getRowLeft();
	}
	
	protected int getEntryRight()
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
	
	protected int getScrollBottom()
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
		maxPos = this.getItemCount() * this.itemHeight + this.headerHeight;
		return maxPos + 4;
	}
}
