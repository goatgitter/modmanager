package com.github.goatgitter.modmanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.goatgitter.modmanager.config.Props;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.discovery.ModResolutionException;
import net.fabricmc.loader.discovery.ModResolver;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.NestedJarEntry;

/**
 * @author goatgitter
 * @since 08/11/2020
 * 
 * This class contains many helper methods for loading the mods 
 * in the mod list.
 *
 */
public class ModListLoader {
	/***************************************************
	 *              CONSTANTS
	 **************************************************/
	private static final String AVAIL_MODS_LIST = "availModList.txt";
	public static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	public final static String API_MOD_ID = "fabric-api-base";
	public final static String INDIGO_MOD_ID = "fabric-renderer-indigo";
	public final static String LOADER_MOD_ID = "fabricloader";
	public final static String FABRIC_MOD_ID = "fabric";
	public static final String BASE_MOD_ID = "minecraft";
	public static final String LOAD_CATCHER_MOD_ID = "loadcatcher";
	public static final String JRE = "java";
	public static List<String> HIDDEN_MODS = Arrays.asList(API_MOD_ID, INDIGO_MOD_ID, LOADER_MOD_ID, FABRIC_MOD_ID, BASE_MOD_ID, LOAD_CATCHER_MOD_ID, JRE);
	private static final Logger LOGGER = LogManager.getFormatterLogger("ModListLoader");
	private static Log LOG = new Log("ModListLoader");
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static FabricLoader fl;
	private static List<ModContainer> requiredMods = new ArrayList<ModContainer>();
	private static Map<String, ModContainer> requiredModsMap = new HashMap<>();
	
	
	/***************************************************
	 *              CONSTRUCTORS
	 **************************************************/
	@SuppressWarnings("deprecation")
	public ModListLoader() {
		LOG.enter("ModListLoader");
		fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();	
		for(ModContainer mc: fl.getMods())
		{
			String modId = mc.getMetadata().getId();
			if (isRequiredMod(modId)) {
				requiredMods.add(mc);
				requiredModsMap.put(modId, mc);
			}
		}
		LOG.exit("ModListLoader");
	}

	/***************************************************
	 *              METHODS
	 **************************************************/
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
				LOGGER.warn("Invalid syntax for JAR " + mc.getMetadata().getId());
			}
		}
		return jarPath;
	}
	
	public ModContainer getModForJar(String jarFileName, List<ModContainer> mods)
	{
		String fn = jarFileName.toLowerCase();
		LOGGER.trace("Looking up mod id for jar file name =>" + fn +".");
		ModContainer jarMod = null;
		for (ModContainer mod: mods)
		{
			String modId = mod.getInfo().getId();
			if (!isRequiredMod(modId))
			{
				LOGGER.trace("Checking mod =>" + modId);
				if(fn.contains(modId))
				{
					String versionString = mod.getInfo().getVersion().getFriendlyString();
					LOGGER.trace("Mod version => " + versionString);
					if (fn.contains(versionString));
					{
						LOGGER.trace("Found " + jarFileName + " => " + modId);
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
		return getModList(Props.getSelectedModListFileName());
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
		Path selectedModsPath = Props.getModsDirPath().normalize().resolve(Props.getSelectedModListFileName());
		String currentJarList = Notebook.get(selectedModsPath);
		return currentJarList;
	}
	
	public Path getModList(String fileName, boolean refreshContents)
	{
		boolean isAvail = fileName.equals(AVAIL_MODS_LIST);
		Path modListPath = Props.getModsDirPath().normalize().resolve(fileName);
		try {
			if (refreshContents)
			{
				Files.deleteIfExists(modListPath);
			}
		    if (! Files.exists(modListPath)){
	    	LOGGER.trace("Creating file " + modListPath.getFileName());
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
					for (File file : Props.getModsDirPath().toFile().listFiles()) {
						if (!file.isDirectory())
						{
							if (!file.getName().endsWith(".jar")) continue;
							String modJarName = file.getName();
							Path srcJarPath = Props.getModsDirPath().resolve(modJarName);
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
			LOGGER.warn("Problem retrieving Mod List file => " + fileName);
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
				String currentJarList = Notebook.get(listPath);
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
				String currentJarList = Notebook.get(listPath);
				String jarFile = jarPath.normalize().toString();
				if (jarPath != null && !currentJarList.contains(jarFile))
				{
					String line = jarFile + System.lineSeparator();
					Files.write(listPath, line.getBytes(), StandardOpenOption.APPEND);
				}
			} catch (IOException e) {
				LOGGER.warn("Problem adding jar to file !");
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void removeJarFromFile(Path listPath, Path jarPath)
	{
		if (Files.exists(jarPath))
		{
			try {
				String currentJarList = Notebook.get(listPath);
				String jarFile = jarPath.normalize().toString();
				if (jarPath != null && currentJarList.contains(jarFile))
				{
					String newJarList = currentJarList.replace(jarFile + System.lineSeparator(), "");				
					Files.write(listPath, newJarList.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
				}
			} catch (IOException e) {
				LOGGER.warn("Problem removing jar from file!");
				e.printStackTrace();
			}
		}
	}
	
	public boolean isModInListFile(Path listPath, Path jarPath) throws IOException
	{
		boolean isModInLoadFile = false;
		if (jarPath != null && Files.exists(jarPath))
		{
			String currentJarList = Notebook.get(listPath);
			String jarFile = jarPath.normalize().toString();
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
	
	public boolean isLibraryMod(LoaderModMetadata metadata)
	{
		boolean isLibraryMod = false;
		isLibraryMod = (metadata.containsCustomValue("modmenu:api") 
						&& metadata.getCustomValue("modmenu:api").getAsBoolean()) ;
		isLibraryMod = isLibraryMod || (metadata.containsCustomValue("fabric-loom:generated") 
						&& metadata.getCustomValue("fabric-loom:generated").getAsBoolean());
		isLibraryMod = isLibraryMod || isRequiredMod(metadata.getId());
		return isLibraryMod;
	}

	private Map<String, ModCandidate> getModMap(String fileName)
	{
		Map<String, ModCandidate> candidateMap = null;
		ModResolver resolver = new ModResolver();
		resolver.addCandidateFinder(new FileListModCandidateFinder(fileName));
		try {
			candidateMap = resolver.resolve(fl);
		} catch (ModResolutionException e) {
			LOGGER.warn("Problem getting loaded mods");
			e.printStackTrace();
		}
		
		// Add Library Mods to the Library Map
		for (ModCandidate modCandidate: candidateMap.values())
		{
			LoaderModMetadata metadata = modCandidate.getInfo();
			boolean isLibrary = isLibraryMod(metadata);
			if (isLibrary) {
				ModMenu.LIBRARY_MODS.add(metadata.getId());
			}
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
		return getModMap(Props.getSelectedModListFileName());
	}
	
	public static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void loadAccessWideners() {
		AccessWidenerReader accessWidenerReader = new AccessWidenerReader(fl.getAccessWidener());
		for (net.fabricmc.loader.api.ModContainer modContainer : fl.getAllMods()) {
			LoaderModMetadata modMetadata = (LoaderModMetadata) modContainer.getMetadata();
			String accessWidener = modMetadata.getAccessWidener();

			if (accessWidener != null) {
				Path path = modContainer.getPath(accessWidener);

				try (BufferedReader reader = Files.newBufferedReader(path)) {
					
					accessWidenerReader.read(reader, "intermediary");
				} catch (Exception e) {
					throw new RuntimeException("Failed to read accessWidener file from mod " + modMetadata.getId(), e);
				}
			}
		}
	}
}
