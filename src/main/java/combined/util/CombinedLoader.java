package combined.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.discovery.ModResolutionException;
import net.fabricmc.loader.discovery.ModResolver;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.NestedJarEntry;

/**
 * @author h1ppyChick
 * I guess I should add some comments, huh?
 * 
 * This class contains many helper methods for loading the combined mods in a 
 * user-friendly way.
 *
 */
public class CombinedLoader {
	// Constants
	private static final String UNLOAD_LIST = "unloadlist.txt";
	private static final String LOAD_LIST = "loadlist.txt";
	private static final String COMBINED_MODS_DIR = "modmenuext\\";
	private static final String MODS_DIR = "mods\\";
	public static final Pattern FABRIC_PATTERN = Pattern.compile("^fabric-.*(-v\\d+)$");
	public final static String API_MOD_ID = "fabric-api-base";
	public final static String INDIGO_MOD_ID = "fabric-renderer-indigo";
	public final static String LOADER_MOD_ID = "fabricloader";
	public final static String FABRIC_MOD_ID = "fabric";
	public static final String BASE_MOD_ID = "minecraft";
	public static final String LOAD_CATCHER_MOD_ID = "loadcatcher";
	public static List<String> HIDDEN_MODS = Arrays.asList(API_MOD_ID, INDIGO_MOD_ID, LOADER_MOD_ID, FABRIC_MOD_ID, BASE_MOD_ID, LOAD_CATCHER_MOD_ID);
	private static final Logger LOG = LogManager.getFormatterLogger("CombinedLoader");
	// Instance variables (fields)
	private static FabricLoader fl;
	private static List<ModContainer> requiredMods = new ArrayList<ModContainer>();
	private static Map<String, ModContainer> requiredModsMap = new HashMap<>();
	
	// Constructor
	@SuppressWarnings("deprecation")
	public CombinedLoader() {
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

	// Methods
	public Path getModsDir() {
		Path baseModDir = getModsBaseDir();
		Path combinedModsDir = baseModDir.resolve(COMBINED_MODS_DIR);
		if(Files.notExists(combinedModsDir))
		{
			try {
				Files.createDirectories(combinedModsDir);
			} catch (IOException e) {
				LOG.warn("Could not create required directory => " + combinedModsDir + "."); 
				e.printStackTrace();
			}
		}
		return combinedModsDir;
	}
	
	public Path getModsBaseDir() {
		return fl.getGameDir().resolve(MODS_DIR);
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
		LOG.debug("Looking up mod id for jar file name =>" + fn +".");
		ModContainer jarMod = null;
		for (ModContainer mod: mods)
		{
			String modId = mod.getInfo().getId();
			if (!isRequiredMod(modId))
			{
				LOG.debug("Checking mod =>" + modId);
				if(fn.contains(modId))
				{
					String versionString = mod.getInfo().getVersion().getFriendlyString();
					LOG.debug("Mod version => " + versionString);
					if (fn.contains(versionString));
					{
						LOG.debug("Found " + jarFileName + " => " + modId);
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
	
	public Path getLoadFile()
	{
		return getModList(LOAD_LIST);
	}
	
	public Path getUnLoadFile()
	{
	    return getModList(UNLOAD_LIST);
	}
	
	public Path getModList(String fileName)
	{
		boolean isLoad = fileName.equals(LOAD_LIST);
		Path modListPath = getModsDir().resolve(fileName);
	    if (! Files.exists(modListPath)){
	    	LOG.info("Creating file " + modListPath.getFileName());
	        try {
				Files.createFile(modListPath);
				// Add all the required mods
				for(ModContainer mc: getRequiredMods())
				{
					addJarToFile(modListPath, mc);
				}
				if (isLoad)
				{
					// First time creating the file
					
					// Add all the JAR files in the mod dir.
					for (File file : getModsDir().toFile().listFiles()) {
						if (!file.isDirectory())
						{
							if (!file.getName().endsWith(".jar")) continue;
							String modJarName = file.getName();
							Path srcJarPath = getModsDir().resolve(modJarName);
							addJarToFile(modListPath, srcJarPath);
						}
					}
				}
			} catch (IOException e) {
				LOG.warn("Problem retrieving Mod List file => " + fileName);
				e.printStackTrace();
			}
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
	
	public boolean isModInLoadFile(Path listPath, Path jarPath) throws IOException
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
		return getModMap(UNLOAD_LIST);
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
		return getModMap(LOAD_LIST);
	}
}
