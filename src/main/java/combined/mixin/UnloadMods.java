package combined.mixin;

import java.io.IOException;
import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import combined.util.CombinedLoader;
import combined.util.LogUtils;
import combined.util.ModConfigUtil;
import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import io.github.prospector.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@Mixin(ModsScreen.class)
public class UnloadMods extends Screen{
	private static final LogUtils LOG = new LogUtils("UnloadMods");
	private static final String MOD_ID = "manymods";
	private int paneY = 48;
	private int buttonHeight = 20;
	private int buttonWidth = 32;
	private int canvasHeight = 64;
	private ModListEntry selected;
	private static final TranslatableText UNLOAD_TOOLTIP_TEXT = new TranslatableText(MOD_ID + ".mixinBtnTooltip");
	private static final TranslatableText REFRESH_TOOLTIP_TEXT = new TranslatableText(MOD_ID + ".mixinBtnTooltip");
	private static final TranslatableText ERROR = new TranslatableText(MOD_ID + ".error");
	private static final TranslatableText UNLOAD_ERROR_DESC = new TranslatableText(MOD_ID + ".unload.error.desc");
	private static final TranslatableText SUCCESS = new TranslatableText(MOD_ID + ".success");
	private static final TranslatableText UNLOAD_SUCCESS_DESC = new TranslatableText(MOD_ID + ".unload.success.desc");
	private static final Identifier UNLOAD_BTN_IMG = new Identifier(MOD_ID, "button.png");
	private static final Identifier REFRESH_BTN_IMG = new Identifier(MOD_ID, "refresh.png");
	private CombinedLoader cl = new CombinedLoader();
	
	public UnloadMods(Text title) {
		super(title);
	}
	@Inject(at = @At("RETURN"), method = "init()V")
	public void drawMenuButton(CallbackInfo info) {	
		LOG.enter("drawMenuButton");
		ButtonWidget unloadBtn = getUnloadButton();
		this.addButton(unloadBtn);
		// Coming soon
		//ButtonWidget refreshBtn = getRefreshButton();
		//this.addButton(refreshBtn);
		LOG.exit("drawMenuButton");
	}
	
	private ButtonWidget getUnloadButton()
	{
		// Create the unload mixin button with a lambda
		ButtonWidget mixinButton = new ModMenuTexturedButtonWidget(getButtonX(1), paneY, buttonWidth, buttonHeight, 0, 0, UNLOAD_BTN_IMG, buttonWidth, canvasHeight, button -> 
		{
			boolean success = true;
			LOG.debug("Requested unloading of mod " + selected);
			try {
				ModConfigUtil.requestUnload(selected);
			
			} catch (IOException e) {
				Path src = cl.getModJarPath(selected);
				
				LOG.warn("Failed to copy mod from " + src );
				e.printStackTrace();
				SystemToast.add(client.getToastManager(), SystemToast.Type.PACK_COPY_FAILURE, ERROR, UNLOAD_ERROR_DESC);
				success = false;
			}
			
			if (success)
			{
				SystemToast.add(client.getToastManager(), SystemToast.Type.TUTORIAL_HINT, SUCCESS, UNLOAD_SUCCESS_DESC);
			}
		},
				UNLOAD_TOOLTIP_TEXT, (buttonWidget, matrices, mouseX, mouseY) -> {
			// Cast to the correct type to access the isJustHovered method.
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, UNLOAD_TOOLTIP_TEXT, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, UNLOAD_TOOLTIP_TEXT, button.x, button.y);
			}
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				visible = ModConfigUtil.canTurnOff(selected);
				this.x = getButtonX(1);
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.color4f(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
		return mixinButton;
	}
	@SuppressWarnings("unused")
	private ButtonWidget getRefreshButton()
	{
		// Create the unload mixin button with a lambda
		ButtonWidget mixinButton = new ModMenuTexturedButtonWidget(getButtonX(2), paneY, buttonWidth, buttonHeight, 0, 0, REFRESH_BTN_IMG, buttonWidth, canvasHeight, button -> 
		{
			boolean success = true;
			LOG.info("Requested mod reload");
			
		},
		REFRESH_TOOLTIP_TEXT, (buttonWidget, matrices, mouseX, mouseY) -> {
			// Cast to the correct type to access the isJustHovered method.
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, REFRESH_TOOLTIP_TEXT, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, REFRESH_TOOLTIP_TEXT, button.x, button.y);
			}
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				this.x = getButtonX(2);
				super.render(matrices, mouseX, mouseY, delta);
			}

			@Override
			public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				RenderSystem.color4f(1, 1, 1, 1f);
				super.renderButton(matrices, mouseX, mouseY, delta);
			}
		};
		return mixinButton;
	}
	// Figure out where to add the unload Button
	private int getButtonX(int addingBtnNum)
	{
		// Figure out where to add the mixin Button
		int newX = (width - (buttonWidth * addingBtnNum) ) - 2;
		if (ModConfigUtil.isConfigurable(selected))
		{
			newX -= 24;
		}
		return newX;		
	}
}
