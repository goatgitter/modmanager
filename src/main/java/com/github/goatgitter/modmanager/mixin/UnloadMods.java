package com.github.goatgitter.modmanager.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.ModConfig;
import com.github.goatgitter.modmanager.util.StickyNote;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.prospector.modmenu.gui.ModListEntry;
import io.github.prospector.modmenu.gui.ModMenuTexturedButtonWidget;
import io.github.prospector.modmenu.gui.ModsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
@Mixin(ModsScreen.class)
public class UnloadMods extends Screen{
	private static final Log LOG = new Log("UnloadMods");
	private int paneY = 48;
	private int buttonHeight = 20;
	private int buttonWidth = 32;
	private int canvasHeight = 64;
	private ModListEntry selected;
	private static final TranslatableText TEXT_UNLOAD_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".unload.tooltip");
	private static final TranslatableText TEXT_REFRESH_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".refresh.tooltip");
	private static final Identifier UNLOAD_BTN_IMG = new Identifier(ModManager.MOD_ID, "button.png");
	private static final Identifier REFRESH_BTN_IMG = new Identifier(ModManager.MOD_ID, "refresh.png");
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
			LOG.trace("Requested unloading of mod " + selected);
			ModConfig.requestUnload(selected);
			StickyNote.showSuccessMsg(client, ModManager.KEY_RESTART);
		},
		TEXT_UNLOAD_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> 
		{
			// Cast to the correct type to access the isJustHovered method.
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, TEXT_UNLOAD_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, TEXT_UNLOAD_TOOLTIP, button.x, button.y);
			}
		}) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				visible = ModConfig.canTurnOff(selected);
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
			LOG.trace("Requested mod reload");
			
		},
		TEXT_REFRESH_TOOLTIP, (buttonWidget, matrices, mouseX, mouseY) -> {
			// Cast to the correct type to access the isJustHovered method.
			ModMenuTexturedButtonWidget button = (ModMenuTexturedButtonWidget) buttonWidget;
			if (button.isJustHovered()) {
				this.renderTooltip(matrices, TEXT_REFRESH_TOOLTIP, mouseX, mouseY);
			} else if (button.isFocusedButNotHovered()) {
				this.renderTooltip(matrices, TEXT_REFRESH_TOOLTIP, button.x, button.y);
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
		if (ModConfig.isConfigurable(selected))
		{
			newX -= 24;
		}
		return newX;		
	}
}
