package combined.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;

import combined.util.CombinedLoader;
import combined.util.Log;
import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.mixin.EntryListWidgetAccessor;
import io.github.prospector.modmenu.util.HardcodedUtil;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class TwoListsWidget extends AlwaysSelectedEntryListWidget<ModListEntry> {
	// Instance Variables
	private Log LOG = new Log("TwoListsWidget");
	private TwoListsWidgetScreen parentScreen = null;	
	private List<ModContainer> _containerList = null;
	private Set<ModContainer> addedMods = new HashSet<>();
	private String selectedModId = null;
	private boolean scrolling;
	private ModListWidget widget;
	private final Text title;
	protected final TwoListsWidget.LoadListAction onLoadList;
	protected final TwoListsWidget.ClickEntryAction onClickEntry;
	
	// Constructors
	public TwoListsWidget(MinecraftClient client, int width, int height, int y1, int y2, int entryHeight, List<ModContainer> widgetList, TwoListsWidgetScreen parent, ModListWidget widget, Text title, LoadListAction onLoadList, ClickEntryAction onClickEntry) {
		super(client, width, height, y1, y2, entryHeight);
	    this.centerListVertically = false;
	    this.title = title;
		this.parentScreen = parent;
		this.widget = widget;
		this.onLoadList = onLoadList;
		this.onClickEntry = onClickEntry;
		onLoadList();
		setScrollAmount(parentScreen.getScrollPercent() * Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		client.textRenderer.getClass();
		this.setRenderHeader(true, (int)(9.0F * 1.5F));
	}
	
	// Methods
   protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {
	      Text text = (new LiteralText("")).append(this.title).formatted(Formatting.UNDERLINE, Formatting.BOLD);
	      this.client.textRenderer.draw(matrices, (Text)text, (float)(x + this.width / 2 - this.client.textRenderer.getWidth((StringVisitable)text) / 2), (float)Math.min(this.top + 3, y), 16777215);
	}
	
   public void onLoadList() {
	   this.clearEntries();
		addedMods.clear();
		this.onLoadList.onLoadList(this);
		for (ModContainer container: _containerList)
		{
			this.addEntry(new ChildModEntry(container, widget, this));
		}
   }
   
   public void onClickEntry(ChildModEntry entry) {
		this.onClickEntry.onClickEntry(entry);
		parentScreen.availableMods.onLoadList();
		parentScreen.selectedMods.onLoadList();
   }
   
   public interface LoadListAction {
	      void onLoadList(TwoListsWidget widget);
   }
   
   public interface ClickEntryAction {
	      void onClickEntry(ChildModEntry entry);
   }
	
	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4));
		if (denominator <= 0 && parentScreen != null) {
			parentScreen.updateScrollPercent(0);
		} else {
			parentScreen.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)));
		}
	}
	
	@Override
	protected boolean isFocused() {
		return parentScreen.getFocused() == this;
	}
	
	public void select(ChildModEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			ModMetadata metadata = entry.getMetadata();
			NarratorManager.INSTANCE.narrate(new TranslatableText("narrator.select", HardcodedUtil.formatFabricModuleName(metadata.getName())).getString());
		}
	}
	
	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		selectedModId = entry.getMetadata().getId();
		parentScreen.updateSelectedEntry(getSelected());
	}
	
	@Override
	protected boolean isSelectedItem(int index) {
		ModListEntry selected = getSelected();
		return selected != null && selected.getMetadata().getId().equals(getEntry(index).getMetadata().getId());
	}
	
	public int addEntry(ChildModEntry entry) {
		if (addedMods.contains(entry.getContainer())) {
			return 0;
		}
		addedMods.add(entry.getContainer());
		int i = super.addEntry(entry);
		if (entry.getMetadata().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
	}

	public List<ModContainer> getContainerList() {
		return _containerList;
	}

	public void setContainerList(List<ModContainer> containerList) {
		this._containerList = containerList;
	}

	public boolean removeEntry(ChildModEntry entry) {
		addedMods.remove(entry.getContainer());
		return super.removeEntry(entry);
	}

	public ChildModEntry getEntry(int index)
	{
		ModListEntry m = super.getEntry(index);
		return (ChildModEntry) m;
	}
	
	protected ModListEntry remove(int index) {
		ChildModEntry entry = getEntry(index);
		addedMods.remove(entry.getContainer());
		return super.remove(index);
	}
	
	
	@Override
	protected void renderList(MatrixStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
		int itemCount = this.getItemCount();
		Tessellator tessellator_1 = Tessellator.getInstance();
		BufferBuilder buffer = tessellator_1.getBuffer();

		for (int index = 0; index < itemCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.top && entryTop <= this.bottom) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (((EntryListWidgetAccessor) this).isRenderSelection() && this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = x + rowWidth + 2;
					RenderSystem.disableTexture();
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.color4f(float_2, float_2, float_2, 1.0F);
					Matrix4f matrix = matrices.peek().getModel();
					buffer.begin(7, VertexFormats.POSITION);
					buffer.vertex(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F).next();
					buffer.vertex(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F).next();
					buffer.vertex(matrix, selectionRight, entryTop - 2, 0.0F).next();
					buffer.vertex(matrix, entryLeft, entryTop - 2, 0.0F).next();
					tessellator_1.draw();
					RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					buffer.begin(7, VertexFormats.POSITION);
					buffer.vertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F).next();
					buffer.vertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F).next();
					buffer.vertex(matrix, selectionRight - 1, entryTop - 1, 0.0F).next();
					buffer.vertex(matrix, entryLeft + 1, entryTop - 1, 0.0F).next();
					tessellator_1.draw();
					RenderSystem.enableTexture();
				}

				entryLeft = this.getRowLeft();
				boolean isHovered = this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry);
				entry.render(matrices, index, entryTop, entryLeft, rowWidth, entryHeight, mouseX, mouseY, isHovered, delta);
				if (isHovered)
				{
					String modId = entry.getMetadata().getId();
					parentScreen.setTooltip(modId);
					Text text = new LiteralText(modId);
					parentScreen.renderTooltip(matrices, text, mouseX, mouseY);
				}
			}
		}

	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPositionX() && double_1 < (double) (this.getScrollbarPositionX() + 6);
	}
	
	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (!this.isMouseOver(double_1, double_2)) {
			return false;
		} else {
			ChildModEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
				if (entry.mouseClicked(double_1, double_2, int_1)) {
					setSelected(entry);
					return true;
				}
			} else if (int_1 == 0) {
				this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScrollAmount() - 4);
				return true;
			}
			return this.scrolling;
		}
	}
	
	public final ChildModEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.top) - this.headerHeight + (int) this.getScrollAmount() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPositionX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ? (ChildModEntry) this.children().get(index) : null;
	}
	
	@Override
	protected int getScrollbarPositionX() {
		return this.right - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4)) > 0 ? 18 : 12);
	}

	@Override
	protected int getRowLeft() {
		return left + 6;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.top;
	}

	public TwoListsWidgetScreen getParent() {
		return parentScreen;
	}

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCount() {
		return children().size();
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : children()) {
			if (set.contains(c.getMetadata().getId())) {
				count++;
			}
		}
		return count;
	}
	
	public Set<ModContainer> getCurrentModSet() {
		return addedMods;
	}

}
