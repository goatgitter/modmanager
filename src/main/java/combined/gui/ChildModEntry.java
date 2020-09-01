package combined.gui;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.mojang.blaze3d.systems.RenderSystem;

import combined.util.Log;
import combined.util.ModConfig;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModListWidget;
import io.github.prospector.modmenu.util.BadgeRenderer;
import io.github.prospector.modmenu.util.HardcodedUtil;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

public class ChildModEntry extends ModListEntry{
	// Constants
	private static final Identifier RESOURCE_PACKS_TEXTURE = new Identifier("textures/gui/resource_packs.png");
	private static final String ELLIPSIS = "...";
	// Instance Variables
	Log LOG = new Log("ChildModEntry");
	private boolean isHoveringIcon = false;
	protected final TwoListsWidget manyModsList;

	public ChildModEntry(ModContainer container, ModListWidget list, TwoListsWidget manyModsList) {
		super(container, list);
		this.manyModsList = manyModsList;
	}
	

	public ModContainer getContainer()
	{
		return (ModContainer) this.container;
	}
	
	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		int rowWidth = entryWidth - getXOffset();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawIcon(matrices, x, y);
		
		Text name = HardcodedUtil.formatFabricModuleName(metadata.getName());
		StringVisitable trimmedName = (StringVisitable) name;
		int maxNameWidth = rowWidth - 32 - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			StringVisitable ellipsis = StringVisitable.plain(ELLIPSIS);
			trimmedName = StringVisitable.concat(font.trimToWidth(name, maxNameWidth - font.getWidth(ELLIPSIS)), ellipsis);
		}
		font.draw(matrices, Language.getInstance().reorder(trimmedName), x + 32 + 3, y + 1, 0xFFFFFF);
		new BadgeRenderer(x + 32 + 3 + font.getWidth(name) + 2, y, x + rowWidth, container, list.getParent()).draw(matrices, mouseX, mouseY);
		String version = StringUtils.isNotEmpty(metadata.getVersion().getFriendlyString()) ? "v" + metadata.getVersion().getFriendlyString() + " " : "";
		String description = version + metadata.getDescription();
		if (description.isEmpty() && HardcodedUtil.getHardcodedDescriptions().containsKey(metadata.getId())) {
			description = HardcodedUtil.getHardcodedDescription(metadata.getId());
		}
		RenderUtils.drawWrappedString(matrices, description, (x + 32 + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - 32 - 7, 2, 0x808080);
		if (hovered)
		{
			this.isHoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + 32 && mouseY >= y - 1 && mouseY <= y - 1 + 32;
			if (isHoveringIcon) {
				addLoadArrow(matrices, x, y, mouseX, mouseY);
			}
		}
		
	}
	
	public void addLoadArrow(MatrixStack matrices, int x, int y, int mouseX, int mouseY)
	{
		RenderSystem.enableBlend();
		this.client.getTextureManager().bindTexture(RESOURCE_PACKS_TEXTURE);
		DrawableHelper.fill(matrices, x-2, y, x + 32, y + 32, 0xA0909090);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		int i = mouseX - x;
        int j = mouseY - y;
		if (this.canTurnOff())
		{
			if (i < 16) {
                DrawableHelper.drawTexture(matrices, x, y, 32.0F, 32.0F, 32, 32, 256, 256);
             } else {
                DrawableHelper.drawTexture(matrices, x, y, 32.0F, 0.0F, 32, 32, 256, 256);
             }
		}
		else
		{
			if (i < 32) {
                DrawableHelper.drawTexture(matrices, x, y, 0.0F, 32.0F, 32, 32, 256, 256);
             } else {
                DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 256, 256);
             }
		}
		RenderSystem.disableBlend();
	}
	
	private NativeImageBackedTexture createIcon() {
		try {
			net.fabricmc.loader.api.ModContainer modMenu = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID).orElseThrow(IllegalAccessError::new);
			Path path = modMenu.getPath("assets/" + ModMenu.MOD_ID + "/grey_fabric_icon.png");
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				NativeImageBackedTexture tex = new NativeImageBackedTexture(image);
				return tex;
			}

		} catch (Throwable t) {
			LOG.warn("Problem creating icon");
			t.printStackTrace();
			return null;
		}
	}
	
	public void drawIcon(MatrixStack matrices, int x, int y) {
		RenderSystem.enableTexture();
		bindIconTexture();
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(matrices, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
		RenderSystem.disableBlend();
	}
	
	@Override
	public void bindIconTexture() {
		NativeImageBackedTexture icon = this.createIcon();
		if (icon != null && icon.getImage() != null) {
			this.client.getTextureManager().registerTexture(this.iconLocation, icon);
		} else {
			this.iconLocation = UNKNOWN_ICON;
		}

		this.client.getTextureManager().bindTexture(this.iconLocation);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int i) {
		if (isHoveringIcon) {
			manyModsList.onClickEntry(this);
		}
		return super.mouseClicked(mouseX, mouseY, i);
	}
	
	public boolean canTurnOff()
	{
		return ModConfig.canTurnOff(this);
	}
	
	public String getModVersion()
	{
		return ModConfig.getModVersion(this);
	}
	
	public boolean isConfigurable()
	{
		return ModConfig.isConfigurable(this);
	}
	
	public String getModId()
	{
		return ModConfig.getModId(this);
	}
}
