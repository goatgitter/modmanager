package com.github.h1ppyChick.modmanager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.h1ppyChick.modmanager.ModManager;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.discovery.ModResolutionException;
import net.fabricmc.loader.discovery.ModResolver;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.NestedJarEntry;
import net.minecraft.client.MinecraftClient;

/**
 * @author h1ppyChick
 * @since 08/11/2020
 * 
 * This class contains many helper methods for loading the mods in the mod list.
 *
 */
public class ModListLoader {
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private static final String KEY_SEL_LIST = "selectedModList";
	private static final String KEY_MOD_LISTS = "modLists";
	private static final String AVAIL_MODS_LIST = "availModList.txt";
	private static final String MODS_LIST_DIR = "modmanager\\";
	private static final String MODS_DIR = "mods\\";
	public static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	public final static String API_MOD_ID = "fabric-api-base";
	public final static String INDIGO_MOD_ID = "fabric-renderer-indigo";
	public final static String LOADER_MOD_ID = "fabricloader";
	public final static String FABRIC_MOD_ID = "fabric";
	public static final String BASE_MOD_ID = "minecraft";
	public static final String LOAD_CATCHER_MOD_ID = "loadcatcher";
	public static final String JRE = "java";
	public static List<String> HIDDEN_MODS = Arrays.asList(API_MOD_ID, INDIGO_MOD_ID, LOADER_MOD_ID, FABRIC_MOD_ID, BASE_MOD_ID, LOAD_CATCHER_MOD_ID, JRE);
	private static final Logger LOG = LogManager.getFormatterLogger("ModListLoader");
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static FabricLoader fl;
	private static List<ModContainer> requiredMods = new ArrayList<ModContainer>();
	private static Map<String, ModContainer> requiredModsMap = new HashMap<>();
	private static Properties props = null;
	
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	@SuppressWarnings("deprecation")
	public ModListLoader() {
		fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();	
		for(ModContainer mc: fl.getMods())
		{
			String modId = mc.getMetadata().getId();
			if (isRequiredMod(modId)) {
				requiredMods.add(mc);
				requiredModsMap.put(modId, mc);
			}
		}
		
	}

	/***************************************************
	 *              METHODS
	 **************************************************/
	
	/***************************************************
	 *              CONFIG FILE METHODS
	 **************************************************/
	private static Path getConfigDir() {
		return fl.getGameDir().normalize().resolve(ModManager.CONFIG_DIR);
	}
	public Path getConfigPath()
	{
		return getConfigDir().resolve(ModManager.MOD_ID + ".properties");
	}
	
	private File getConfigFile()
	{
		return getConfigPath().toFile();
	}
	
	private Properties getProps() {
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
	
	private void setPropVal(String key, String value)
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
	}
	
	public String getSelectedModListName()
	{
		return getProps().getProperty(KEY_SEL_LIST);
	}
	private String getModLists()
	{
		String propValue = getProps().getProperty(KEY_MOD_LISTS);
		if (propValue == null)
		{
			propValue = getSelectedModListName();
		}
		return propValue;
	}
	public List<String> getAllModLists()
	{
		String propValue = getProps().getProperty(KEY_MOD_LISTS);
		if (propValue == null)
		{
			propValue = getSelectedModListName();
		}
		return Arrays.asList(propValue.split(","));
	}
	
	private void setModLists(String modLists)
	{
		setPropVal(KEY_MOD_LISTS, modLists);
	}
	
	public boolean setSelectedModListName(String newName, boolean isNewFile)
	{
		boolean result = true;
		String oldName = getSelectedModListName();
		// If name has not changed, no updates to be saved.
		if (oldName.equals(newName)) return result;
		
		String modLists = getModLists();		
		Path oldFilePath = getSelectedModList();
		setPropVal(KEY_SEL_LIST, newName);
		Path newFilePath = getSelectedModList();
		
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
	
	private String getSelectedModListFileName()
	{
		return getSelectedModListName() + ".txt";
	}
	
	public Path getModsDir() {
		Path baseModDir = getModsBaseDir();
		Path modListDir = baseModDir.resolve(MODS_LIST_DIR);
		if(Files.notExists(modListDir))
		{
			try {
				Files.createDirectories(modListDir);
			} catch (IOException e) {
				LOG.warn("Could not create required directory => " + modListDir + "."); 
				e.printStackTrace();
			}
		}
		return modListDir;
	}
	
	public Path getModsBaseDir() {
		return fl.getGameDir().normalize().resolve(MODS_DIR);
	}

	@SuppressWarnings("deprecation")
	public ModContainer getMod(String id)
	{
		for(ModContainer mc : fl.getModContainers())
		{
			if(mc.getInfo().getId().equals(id))
			{
				return mc;
			}
		}
		return null;
	}

	
	public Path getModJarPath(ModContainer mc)
	{
		Path jarPath = null;
		if (mc != null)
		{
			
			try {
				jarPath = Paths.get(mc.getOriginUrl().toURI());
				if (Files.notExists(jarPath) || mc.getOriginUrl().toString().contains("nestedJarStore"))
				{
					return null;
				}
			} catch (URISyntaxException e) {
				LOG.warn("Invalid syntax for JAR " + mc.getMetadata().getId());
			}
		}
		return jarPath;
	}
	
	public ModContainer getModForJar(String jarFileName, List<ModContainer> mods)
	{
		String fn = jarFileName.toLowerCase();
		LOG.trace("Looking up mod id for jar file name =>" + fn +".");
		ModContainer jarMod = null;
		for (ModContainer mod: mods)
		{
			String modId = mod.getInfo().getId();
			if (!isRequiredMod(modId))
			{
				LOG.trace("Checking mod =>" + modId);
				if(fn.contains(modId))
				{
					String versionString = mod.getInfo().getVersion().getFriendlyString();
					LOG.trace("Mod version => " + versionString);
					if (fn.contains(versionString));
					{
						LOG.trace("Found " + jarFileName + " => " + modId);
						jarMod = mod;
						break;
					}
				}
			}
		}
			
		return jarMod;
	}
	
	public String getNestedJarFileName(NestedJarEntry nestedJar)
	{
		String fn = nestedJar.getFile();
		String returnName = fn.substring(fn.lastIndexOf("/") +1, fn.length());
		return returnName;
	}
	
	public Path getSelectedModList()
	{
		return getModList(getSelectedModListFileName());
	}
	
	public Path getAvailModListFile()
	{
	    return getModList(AVAIL_MODS_LIST);
	}
	
	// Update the avail mod list file when a new mod list is created.
	public void updateAvailModListFile()
	{
		getModList(AVAIL_MODS_LIST, true);
	}
	
	public Path getModList(String fileName)
	{
		return getModList(fileName, false);
	}
	public String getCurrentJarList()
	{
		Path selectedModsPath = getModsDir().resolve(getSelectedModListFileName());
		String currentJarList = "";
		try {
			currentJarList = FileUtils.readFileToString(selectedModsPath.toFile(), Charset.defaultCharset());
		} catch (IOException e) {
			LOG.warn("Problem retrieving current jar list");
			e.printStackTrace();
		}
		return currentJarList;
	}
	
	public Path getModList(String fileName, boolean refreshContents)
	{
		boolean isAvail = fileName.equals(AVAIL_MODS_LIST);
		Path modListPath = getModsDir().resolve(fileName);
		try {
			if (refreshContents)
			{
				Files.deleteIfExists(modListPath);
			}
		    if (! Files.exists(modListPath)){
	    	LOG.trace("Creating file " + modListPath.getFileName());
				Files.createFile(modListPath);
				// Add all the required mods
				for(ModContainer mc: getRequiredMods())
				{
					addJarToFile(modListPath, mc);
				}
				if (isAvail)
				{
					// Creating new file
					// Get the list of selected mods
					String currentJars = getCurrentJarList();
					// Add all the JAR files in the mod dir.
					for (File file : getModsDir().toFile().listFiles()) {
						if (!file.isDirectory())
						{
							if (!file.getName().endsWith(".jar")) continue;
							String modJarName = file.getName();
							Path srcJarPath = getModsDir().resolve(modJarName);
							// Check to see if the mod is in the selected list.
							if (!currentJars.contains(srcJarPath.toString()))
							{
								addJarToFile(modListPath, srcJarPath);
							}
						}
					}
				}
		    }
		} catch (IOException e) {
			LOG.warn("Problem retrieving Mod List file => " + fileName);
			e.printStackTrace();
		}
	    return modListPath;
	}
	
	public void addJarToFile(Path listPath, ModContainer mc) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		Path jarPath = getModJarPath(mc);
		if (jarPath != null)
		{
			if(Files.exists(jarPath))
			{
				String currentJarList = FileUtils.readFileToString(listPath.toFile(), Charset.defaultCharset());
				String jarFile = jarPath.toString();
				if (jarPath != null && !currentJarList.contains(jarFile))
				{
					sb.append(jarPath.toString() + System.lineSeparator());
				}
				Files.write(listPath, sb.toString().getBytes(), StandardOpenOption.APPEND);
			}
			
		}
	}
	
	public void addJarToFile(Path listPath, Path jarPath)
	{
		if (Files.exists(jarPath))
		{
			try {
				String currentJarList = FileUtils.readFileToString(listPath.toFile(), Charset.defaultCharset());
				// Remove extra .\ from path string.
				String jarFile = jarPath.toString().replace("run\\.", "run");
				if (jarPath != null && !currentJarList.contains(jarFile))
				{
					String line = jarFile + System.lineSeparator();
					Files.write(listPath, line.getBytes(), StandardOpenOption.APPEND);
				}
			} catch (IOException e) {
				LOG.warn("Problem adding jar to file !");
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void removeJarFromFile(Path listPath, Path jarPath)
	{
		if (Files.exists(jarPath))
		{
			try {
				String currentJarList = FileUtils.readFileToString(listPath.toFile(), Charset.defaultCharset());
				String jarFile = jarPath.toString();
				if (jarPath != null && currentJarList.contains(jarFile))
				{
					String newJarList = currentJarList.replace(jarFile + System.lineSeparator(), "");				
					Files.write(listPath, newJarList.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
				}
			} catch (IOException e) {
				LOG.warn("Problem removing jar from file!");
				e.printStackTrace();
			}
		}
	}
	
	public boolean isModInListFile(Path listPath, Path jarPath) throws IOException
	{
		boolean isModInLoadFile = false;
		if (jarPath != null && Files.exists(jarPath))
		{
			String currentJarList = FileUtils.readFileToString(listPath.toFile(), Charset.defaultCharset());
			String jarFile = jarPath.toString();
			if (jarPath != null && currentJarList.contains(jarFile))
			{
				isModInLoadFile = true;
			}
		}
		return isModInLoadFile;
	}
	
	public Path getModJarPath(String modId)
	{
		ModContainer mc = getMod(modId);
		return getModJarPath(mc);
	}
	
	public Path getModJarPath(ModListEntry mod)
	{
		ModContainer mc = getMod(mod.getMetadata().getId());
		return getModJarPath(mc);
	}
	
	
	public List<ModContainer> getRequiredMods()
	{
		return requiredMods;
	}
	
	public Map<String, ModContainer> getRequiredModsMap()
	{
		return requiredModsMap;
	}
	
	public boolean isRequiredMod(String modId)
	{
		boolean isRequiredMod = false;
		Matcher matcher = FABRIC_PATTERN.matcher(modId);
		isRequiredMod = matcher.matches() || HIDDEN_MODS.contains(modId);
		return isRequiredMod;
	}
	

	private Map<String, ModCandidate> getModMap(String fileName)
	{
		Map<String, ModCandidate> candidateMap = null;
		ModResolver resolver = new ModResolver();
		resolver.addCandidateFinder(new FileListModCandidateFinder(fileName));
		try {
			candidateMap = resolver.resolve(fl);
		} catch (ModResolutionException e) {
			LOG.warn("Problem getting loaded mods");
			e.printStackTrace();
		}
		return candidateMap;
	}
	
	public Map<String, ModCandidate> getAvailableMods()
	{
		return getModMap(AVAIL_MODS_LIST);
	}
	
	public List<ModContainer> getAvailableModList()
	{
		
		Map<String, ModCandidate> availModMap = getAvailableMods();		
		return getModList(availModMap, false);
	}
	
	public List<ModContainer> getSelectedModList(boolean includeLibs)
	{
		Map<String, ModCandidate> selectedModMap = getSelectedMods();
		return getModList(selectedModMap, includeLibs);
	}
	
	public List<ModContainer> getModList(Map<String, ModCandidate> modMap, boolean includeLibs)
	{
		List<ModContainer> mods = new ArrayList<>();
		if (modMap != null)
		{
			for (ModCandidate candidate: modMap.values())
			{
				LoaderModMetadata info = candidate.getInfo();
				URL originUrl = candidate.getOriginUrl();
				String modId = info.getId();
				if (!isRequiredMod(modId))
				{
					ModContainer container = new ModContainer(info, originUrl);
					boolean foundInLibs = ModMenu.LIBRARY_MODS.contains(modId);
					if ( (foundInLibs && includeLibs) || !foundInLibs)
					{
						mods.add(container);
					}
				}
			}
		}
		return mods;
	}
	
	
	public Map<String, ModCandidate> getSelectedMods()
	{
		return getModMap(getSelectedModListFileName());
	}
	
	public static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean exportModList(String listName)
	{
		boolean result = true;
		Path modListPath = getModList(listName + ".txt", false);
		Path modListZipPath = getModsDir().resolve(listName + ".zip");
		
		try
		{
			List<String> mods = Files.readAllLines(modListPath);
            FileOutputStream fos = new FileOutputStream(modListZipPath.toString());
            ZipOutputStream zos = new ZipOutputStream(fos);
            // Add the list text file
            zos.putNextEntry(new ZipEntry(modListPath.getFileName().toString()));
            byte[] bytes = Files.readAllBytes(modListPath);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            
			for (String modJarName : mods) {
				if(modJarName.contains(getModsDir().toString()) && modJarName.endsWith(".jar"))
				{
					File modFile = new File(modJarName);
					Path srcJarPath = modFile.toPath();
					if(Files.exists(srcJarPath) && Files.isRegularFile(srcJarPath))
					{
						zos.putNextEntry(new ZipEntry(srcJarPath.getFileName().toString()));
		                bytes = Files.readAllBytes(srcJarPath);
		                zos.write(bytes, 0, bytes.length);
		                zos.closeEntry();
					}
				}
			}
			zos.close();
			fos.close();
		} catch (Exception e) {
			result = false;
			LOG.warn("Could not add mod from list file => " + listName + "."); 
			e.printStackTrace();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public boolean importModList(String listName, MinecraftClient client)
	{
		boolean result = true;
		Path modListZipPath = getModsDir().resolve(listName + ".zip");

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(modListZipPath.toString());
		} catch (IOException e) {
			result = false;
			StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_ERROR_OPEN_ZIP, modListZipPath.toString());
			e.printStackTrace();
		}
		if (zipFile != null)
		{
			Enumeration<ZipEntry> enumEntries = (Enumeration<ZipEntry>) zipFile.entries();
			
			while( enumEntries.hasMoreElements())
			{
				ZipEntry zipEntry = enumEntries.nextElement();
				String zipFileName = zipEntry.getName();
				Path destFilePath = getModsDir();
		    	Path destZipFilePath = destFilePath.resolve(zipFileName);
				InputStream zipInputStream = null;
				try {
					zipInputStream = zipFile.getInputStream(zipEntry);
				} catch (IOException e) {
					result = false;
					StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_ERROR_OPEN_ZIP, zipEntry.getName(), listName);
					e.printStackTrace();
				}
				if (zipInputStream != null)
				{
					try {
						Files.copy(zipInputStream, destZipFilePath, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						result = false;
						StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_ERROR_EXTRACT_ZIP_ENTRY, zipEntry.getName(), listName, destZipFilePath.getParent().getFileName());
						e.printStackTrace();
					}
				}
				try {
					zipInputStream.close();
				} catch (IOException e) {
					result = false;
					StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_ERROR_CLOSE_ZIP_ENTRY, zipEntry.getName(), listName);
					e.printStackTrace();
				}
			}
			try {
				zipFile.close();
			} catch (IOException e) {
				result = false;
				StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_ERROR_CLOSE_ZIP, listName);
				e.printStackTrace();
			}
		}
		if (result)
		{
			setSelectedModListName(listName, true);
			StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_SUCCESS, listName);
		}
		return result;
	}
	
	public List<String> getAvailArchives()
	{
		List<String> availList = new ArrayList<String>();
		Path dirToCheck = getModsDir();
		File[] files = dirToCheck.toFile().listFiles();
		for (File f:files)
		{
			if (f.getName().endsWith(".zip"))
			{
				availList.add(f.getName());
			}
		}
		return availList;
	}
}
