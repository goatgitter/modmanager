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
	public static final String MODS_DIR = "mods\\";
	public static final String MODS_LIST_DIR = MOD_ID + "\\";
	public static final String MM_PARENT_KEY = "modmenu:parent";
	public static final String NEW_LIST_NAME = "newModList";
	
	// Translation keys
	// ERRORS
	public static final String KEY_IMPORT_ERROR = ModManager.MOD_ID + ".import.error";
	public static final String KEY_IMPORT_ERROR_OPEN_ZIP = ModManager.MOD_ID + ".import.error.open.zip";
	public static final String KEY_IMPORT_ERROR_OPEN_ZIP_ENTRY = ModManager.MOD_ID + ".import.error.open.zip.entry";
	public static final String KEY_IMPORT_ERROR_EXTRACT_ZIP_ENTRY = ModManager.MOD_ID + ".import.error.extract.entry";
	public static final String KEY_IMPORT_ERROR_CLOSE_ZIP = ModManager.MOD_ID + ".import.error.close.zip";
	public static final String KEY_IMPORT_ERROR_CLOSE_ZIP_ENTRY = ModManager.MOD_ID + ".import.error.close.zip.entry";
	public static final String KEY_COPY_ERROR = ModManager.MOD_ID + ".copy.error";
	public static final String KEY_OPEN_ERROR = ModManager.MOD_ID + ".open.error";
	public static final String KEY_SAVE_ERROR = ModManager.MOD_ID + ".save.error";
	public static final String KEY_ADD_ERROR = ModManager.MOD_ID + ".add.error";
	public static final String KEY_EXPORT_ERROR = ModManager.MOD_ID + ".export.error";
	
	// Success
	public static final String KEY_IMPORT_SUCCESS = ModManager.MOD_ID + ".import.success";
	public static final String KEY_DROP_SUCCESS_1 = "modmenu.dropSuccessful.line1";
	public static final String KEY_DROP_SUCCESS_2 = "modmenu.dropSuccessful.line2";
	public static final String KEY_SAVE_SUCCESS = ModManager.MOD_ID + ".save.success";
	public static final String KEY_ADD_SUCCESS = ModManager.MOD_ID + ".add.success";
	public static final String KEY_EXPORT_SUCCESS = ModManager.MOD_ID + ".export.success";
	public static final String KEY_RESTART = MOD_ID + ".restart";
	
	// Translatable Texts
	public static final TranslatableText TEXT_SUCCESS = new TranslatableText(MOD_ID + ".success");
	public static final TranslatableText TEXT_ERROR = new TranslatableText(MOD_ID + ".error");
	public static final TranslatableText TEXT_WARNING = new TranslatableText(MOD_ID + ".warning");
	public static final TranslatableText TEXT_NOT_IMPL = new TranslatableText(MOD_ID + ".notimpl");
	public static final TranslatableText TEXT_OPEN_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".open.tooltip");
	public static final TranslatableText TEXT_SAVE_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".save.tooltip");
	public static final TranslatableText TEXT_ADD_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".add.tooltip");
	public static final TranslatableText TEXT_EXPORT_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".export.tooltip");
	public static final TranslatableText TEXT_IMPORT_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".import.tooltip");
	public static final TranslatableText TEXT_DIR_UP_TOOLTIP = new TranslatableText(ModManager.MOD_ID + ".dirup.tooltip");
	
	public static final Identifier SAVE_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "save.png");
	public static final Identifier OPEN_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "open.png");
	public static final Identifier ADD_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "add.png");
	public static final Identifier EXPORT_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "export.png");
	public static final Identifier IMPORT_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "import.png");
	public static final Identifier UP_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "up.png");
	public static final Identifier ARROW_BUTTON_LOCATION = new Identifier(ModManager.MOD_ID, "arrows.png");
	public static final Identifier RESOURCE_PACKS_TEXTURE = new Identifier("textures/gui/resource_packs.png");
	
	public static final int TOP_BTN_WIDTH = 19;
	public static final int TOP_BTN_HEIGHT = 13;
	public static final int TOP_ENTRY_HEIGHT = 15;
	public static final int LEFT_PANE_X = 5;
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
