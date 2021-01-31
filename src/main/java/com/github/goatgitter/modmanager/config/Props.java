package com.github.goatgitter.modmanager.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.util.Log;

import net.fabricmc.loader.FabricLoader;

/******************************************************************************************
 *              Props
 * ---------------------------------------------------------------------------------------
 * @author	h1ppychick
 * @since	01/29/2021
 * @purpose	This class for working with the Mod Configuration Properties.
 ****************************************************************************************/

public class Props {
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private static final String KEY_SEL_LIST = "selectedModList";
	private static final String KEY_MOD_LISTS = "modLists";
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("Props");
	private static Properties props = null;
	private static Path gameConfigDirPath = null;
	private static Path modConfigFilePath = null;
	private static File modConfigFile = null;
	private static Path gameModsDirPath = null;
	private static Path modsDirPath = null;
	private static Path selectedModListPath = null;
	/***************************************************
	 *              METHODS
	 **************************************************/
	public static Path getGameModsPath() {
		if (gameModsDirPath == null)
		{
			FabricLoader fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();	
			gameModsDirPath = fl.getGameDir().normalize().resolve(ModManager.MODS_DIR);
		}
		return gameModsDirPath;
	}
	
	public static Path getGameConfigPath() {
		if (gameConfigDirPath == null)
		{
			FabricLoader fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();	
			gameConfigDirPath = fl.getGameDir().normalize().resolve(ModManager.CONFIG_DIR);
		}
		return gameConfigDirPath;
	}
	
	public static Path getModConfigPath()
	{
		if (modConfigFilePath == null)
		{
			modConfigFilePath = getGameConfigPath().resolve(ModManager.MOD_ID + ".properties");
		}
		return modConfigFilePath;
	}
	
	public static Path getModsDirPath() {
		if (modsDirPath == null)
		{
			modsDirPath = getGameModsPath().resolve(ModManager.MODS_LIST_DIR);
			if(Files.notExists(modsDirPath))
			{
				try {
					Files.createDirectories(modsDirPath);
				} catch (IOException e) {
					LOG.warn("Could not create required directory => " + modsDirPath + "."); 
					e.printStackTrace();
				}
			}
		}
		return modsDirPath;
	}
	
	public static File getConfigFile()
	{
		if (modConfigFile == null)
		{
			modConfigFile = getModConfigPath().toFile();
		}
		return modConfigFile;
	}
	
	public static Properties getProps() {
		if (props == null)
		{
		    props = new Properties();
		    File configFile = getConfigFile();
		    try {
		    	FileReader reader = new FileReader(configFile);
				props.load(reader);
			} catch (IOException e) {
				LOG.warn("Could not read property file => " + configFile.getName() + "."); 
				e.printStackTrace();
			}
		}
		return props;
	}
	
	public static void setPropVal(String key, String value)
	{
		File configFile = getConfigFile();
		getProps().setProperty(key, value);
	    FileWriter writer;
		try {
			writer = new FileWriter(configFile);
			getProps().store(writer, key);
		    writer.close();
		} catch (IOException e) {
			LOG.warn("Could not write property file => " + configFile.getName() + "."); 
			e.printStackTrace();
		}
		// Force a refresh by resetting props to null after an update.
		props = null;
	}
	
	public static String getSelectedModListName()
	{
		return getProps().getProperty(KEY_SEL_LIST);
	}
	
	public static Path getSelectedModListPath()
	{
		if (selectedModListPath == null)
		{
			selectedModListPath = getModsDirPath().resolve(getSelectedModListFileName());
		}
		return selectedModListPath;
	}
	
	public static String getModLists()
	{
		String propValue = getProps().getProperty(KEY_MOD_LISTS);
		if (propValue == null)
		{
			propValue = getSelectedModListName();
		}
		return propValue;
	}
	
	public static List<String> getAllModLists()
	{
		String propValue = getModLists();
		return Arrays.asList(propValue.split(","));
	}
	
	public static void setModLists(String modLists)
	{
		setPropVal(KEY_MOD_LISTS, modLists);
	}
	
	public static boolean setSelectedModListName(String newName, boolean isNewFile)
	{
		boolean result = true;
		String oldName = getSelectedModListName();
		// If name has not changed, no updates to be saved.
		if (oldName.equals(newName)) return result;
		
		String modLists = getModLists();		
		Path oldFilePath = getSelectedModListPath();
		setPropVal(KEY_SEL_LIST, newName);
		Path newFilePath = getSelectedModListPath();
		
		try {			
			if (isNewFile)
			{
				if(!modLists.contains(newName))
				{
					modLists = modLists + "," + newName;
				}
			}
			else
			{
				if(!modLists.contains(newName))
				{
					Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
					
					modLists = modLists.replace(oldName, newName);
				}
			}
		} catch (IOException e) {
			result = false;
			// Set the prop value back in case of error.
			setPropVal(KEY_SEL_LIST, oldName);
			LOG.warn("Could not rename mod list file to => " + newName + "."); 
			e.printStackTrace();
		}
		setModLists(modLists);
		return result;
	}
	
	public static String getSelectedModListFileName()
	{
		return getSelectedModListName() + ".txt";
	}
}
