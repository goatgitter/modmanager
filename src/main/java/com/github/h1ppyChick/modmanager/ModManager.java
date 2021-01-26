package com.github.h1ppyChick.modmanager;

import com.github.h1ppyChick.modmanager.util.Log;
import net.fabricmc.api.ModInitializer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 * See README.md
 */
public class ModManager implements ModInitializer {
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	public static final String MOD_ID = "modmanager";
	public static final String LOAD_JAR_DIR = "loadedJars/";
	public static final String CONFIG_DIR = "config/";
	public static final String MM_PARENT_KEY = "modmenu:parent";
	public static final String NEW_LIST_NAME = "newModList";
	
	public static final TranslatableText TEXT_SUCCESS = new TranslatableText(MOD_ID + ".success");
	public static final TranslatableText TEXT_ERROR = new TranslatableText(MOD_ID + ".error");
	public static final TranslatableText TEXT_WARNING = new TranslatableText(MOD_ID + ".warning");
	public static final TranslatableText TEXT_RESTART = new TranslatableText(MOD_ID + ".restart");
	public static final TranslatableText TEXT_NOT_IMPL = new TranslatableText(MOD_ID + ".notimpl");
	public static final TranslatableText TEXT_OPEN_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".open.tooltip");
	public static final TranslatableText TEXT_SAVE_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".save.tooltip");
	public static final TranslatableText TEXT_ADD_SUCCESS = new TranslatableText(ModManager.MOD_ID + ".add.success");
	public static final TranslatableText TEXT_ADD_ERROR = new TranslatableText(ModManager.MOD_ID + ".add.error");
	public static final TranslatableText TEXT_OPEN_ERROR = new TranslatableText(ModManager.MOD_ID + ".open.error");
	public static final TranslatableText TEXT_SAVE_SUCCESS = new TranslatableText(ModManager.MOD_ID + ".save.success");
	public static final TranslatableText TEXT_SAVE_ERROR = new TranslatableText(ModManager.MOD_ID + ".save.error");
	public static final TranslatableText TEXT_ADD_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".add.tooltip");
	public static final TranslatableText TEXT_EXPORT_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".export.tooltip");
	public static final TranslatableText TEXT_IMPORT_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".import.tooltip");
	
	public static final Identifier SAVE_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "save.png");
	public static final Identifier OPEN_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "open.png");
	public static final Identifier ADD_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "add.png");
	public static final Identifier EXPORT_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "export.png");
	public static final Identifier IMPORT_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "import.png");
	
	public static final int TOP_BTN_WIDTH = 19;
	public static final int TOP_BTN_HEIGHT = 13;
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("ModManager");
	@Override
	public void onInitialize() {
		LOG.enter("onInitialize");
		// No mod setup currently
		LOG.exit("onInitialize");
	}
}
