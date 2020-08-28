package combined.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.discovery.ModResolutionException;
import net.fabricmc.loader.discovery.ModResolver;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.fabricmc.loader.metadata.ModMetadataV1;
import net.fabricmc.loader.metadata.ModMetadataV1.CustomValueContainer;

public class ModConfigUtil {
	private static boolean startedManyModLoad = false;
	private static LogUtils LOG = new LogUtils("ModConfigUtil");	
	private static final String UNLOAD_LIST = "unloadlist.txt";
	private static final String LOAD_LIST = "loadlist.txt";
	public static final String MOD_ID = "manymods";
	private static final String LOAD_JAR_DIR = "loadedJars/";
	private static final String MM_PARENT_KEY = "modmenu:parent";
	private static FabricLoader fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();
	private static CombinedLoader cl = new CombinedLoader();
	public static ModMetadata getMetadata(ModListEntry mod)
	{
		ModMetadata mmd = null;
		if (mod !=null) {
			mmd = mod.getMetadata();
		}
		return mmd;
	}
	public static String getModId(ModListEntry mod)
	{
		String modId = "";		
		ModMetadata mmd = getMetadata(mod);
		if (mmd != null)
		{
			modId = mmd.getId();
		}
		return modId;
	}
	public static String getModVersion(ModListEntry mod)
	{
		String modVersion = "";		
		ModMetadata mmd = getMetadata(mod);
		if (mmd != null)
		{
			modVersion = mmd.getVersion().getFriendlyString();
		}
		return modVersion;
	}
	public static boolean isConfigurable(ModListEntry mod)
	{
		boolean isConfigurable = true;
		String modid = getModId(mod);
		isConfigurable = (ModMenu.hasConfigScreenFactory(modid) || ModMenu.hasLegacyConfigScreenTask(modid));
		return isConfigurable;
	}
	
	public static boolean canTurnOff(ModListEntry mod)
	{
		boolean canTurnOff = true;
		String modId = getModId(mod);
		if (cl.isRequiredMod(modId)) 
		{
			canTurnOff = false;
		}
		else
		{
			Path srcJarPath = cl.getModJarPath(mod);
			Path loadListPath = cl.getLoadFile(LOAD_LIST);
			try {
				if (!cl.isModInLoadFile(loadListPath, srcJarPath))
					canTurnOff=false;
			} catch (IOException e) {
				LOG.warn("Problem determining if mod can be turned off");
				e.printStackTrace();
			}
		}
		
		return canTurnOff;
	}
	
	
	public static Path getModsDir()
	{
		return cl.getModsDir();
	}
	
	public static Path getPath(Path root, String dirName) throws IOException
	{
		Path dirPath = root.resolve(dirName);
		if (! Files.exists(dirPath)){
	        Files.createDirectory(dirPath);
	    }
		return dirPath;
	}
	
	public static Path getCombinedModsDir() throws IOException
	{
		return cl.getModsDir();
	}
	public static void requestUnload(ModListEntry mod) throws IOException
	{
		Path unloadListPath = cl.getUnLoadFile(UNLOAD_LIST);
		Path loadlistPath = cl.getLoadFile(LOAD_LIST);
		Path srcJarPath = cl.getModJarPath(mod);
		cl.addJarToFile(unloadListPath, srcJarPath);
		cl.removeJarFromFile(loadlistPath, srcJarPath);
	}
	
	private static void unZipLoadedJars(String jarFileName, String destDirName) throws IOException
	{
		JarFile jar = new JarFile(jarFileName);
		Enumeration<JarEntry> enumEntries = jar.entries();
		Path loadFilePath = cl.getLoadFile(LOAD_LIST);
		while (enumEntries.hasMoreElements()) {
		    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
		    
		    if (file.getName().contains(LOAD_JAR_DIR) && file.getName().endsWith(".jar")) {
		    	String destFileName = destDirName + java.io.File.separator + file.getName().replace(LOAD_JAR_DIR, "");
		    	
		    	java.io.File f = new java.io.File(destFileName);
		    	Path destFilePath = f.toPath();
		    	if (Files.notExists(destFilePath))
		    	{
		    		java.io.InputStream is = jar.getInputStream(file); 
				    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
				    while (is.available() > 0) {  
				        fos.write(is.read());
				    }
				    fos.close();
				    is.close();
				    cl.addJarToFile(loadFilePath, destFilePath);
		    	}
		    }
		}
		jar.close();
	}
	
	private static void addNewMods(Map<String, ModContainer> oldModMap, Object oldEntrypointStorage)
	{
		try {
			
			ModResolver resolver = new ModResolver();
			resolver.addCandidateFinder(new FileListModCandidateFinder(LOAD_LIST));
			Map<String, ModCandidate> candidateMap = resolver.resolve(fl);
			LOG.info("Loading " + candidateMap.values().size() + candidateMap.values().stream()
					.map(info -> String.format("%s@%s", info.getInfo().getId(), info.getInfo().getVersion().getFriendlyString()))
					.collect(Collectors.joining(", ")));
			FieldUtils.writeField(fl, "modMap", new HashMap<>(), true);
			FieldUtils.writeField(fl, "mods", new ArrayList<>(), true);
			FieldUtils.writeField(fl, "adapterMap", new HashMap<>(), true);
			FieldUtils.writeField(oldEntrypointStorage, "entryMap", new HashMap<>(), true);
			FieldUtils.writeField(fl, "entrypointStorage", oldEntrypointStorage, true);
			for (ModCandidate candidate : candidateMap.values()) {
				if (!oldModMap.containsKey(candidate.getInfo().getId()))
				{
					//addMod(candidate);
					Object[] addArgs = {candidate};
					MethodUtils.invokeMethod(fl, true, "addMod", addArgs);
				}
			}
		} catch (IllegalAccessException | ModResolutionException | NoSuchMethodException | InvocationTargetException e) {
			LOG.info("Problem adding new mods");
			e.printStackTrace();
		}
		
	}
	
	private static void initMixins()
	{
		try {
			FieldUtils.writeField(fl, "frozen", false, true);
		} catch (IllegalAccessException e) {
			LOG.warn("Problem updating frozen field");
			e.printStackTrace();
		}
		fl.freeze();
		fl.getAccessWidener().loadFromMods();
		try {
			LOG.info("Clearing init properties for Mixin Bootstrap");
			Object clearInit = null;
			GlobalProperties.put(GlobalProperties.Keys.INIT, clearInit);
			FieldUtils.writeStaticField(MixinBootstrap.class,  "initialised", false, true);
			FieldUtils.writeStaticField(MixinBootstrap.class,  "platform", null, true);
			
			LOG.info("Calling mixin bootstrap");
			MixinBootstrap.init();
		}
		catch(Exception except) {
			LOG.warn("Unable to init mixin boot strap");
			except.printStackTrace();
		}
			
		try
		{
			Constructor<FabricMixinBootstrap> c = FabricMixinBootstrap.class.getDeclaredConstructor();
			c.setAccessible(true);
			FabricMixinBootstrap fmb = c.newInstance();
			FieldUtils.writeField(fmb, "initialized", false, true);
			LOG.debug("-------------------------------------------------");
			LOG.debug("             START Mixin BootStrap             ");
			FabricMixinBootstrap.init(fl.getEnvironmentType(), fl);
			finishMixinBootstrapping();
			LOG.debug("             END Mixin BootStrap                 ");
			LOG.debug("-------------------------------------------------");
		} catch (IllegalAccessException | NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException e) {
			LOG.warn("Problem initalizing Mixins");
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static void updateLoaderFields(Map<String, ModContainer> oldModMap, List<ModContainer> oldMods, Map<String, LanguageAdapter> oldAdapterMap, Object oldGameDir,
			Object oldEntrypointStorage, Map<String, List<?>> oldEntries)
	{
		
		try {
			List<ModContainer> changedMods = (List<ModContainer>) FieldUtils.readDeclaredField(fl, "mods", true);
			for(ModContainer mod : changedMods)
			{
				String modId = mod.getMetadata().getId();
				LOG.debug("Checking changed mod id =>" + modId);
				// Don't add the fabric api again
				if (!cl.isRequiredMod(modId) && !oldModMap.containsKey(mod.getInfo().getId()))
				{
					// Add custom metadata to show the loaded mod as a child of many mods.
					ModMetadataV1 mm = (ModMetadataV1) mod.getMetadata();
					CustomValueContainer cvc = (CustomValueContainer) FieldUtils.readDeclaredField(mm, "custom", true);
					Map<String, CustomValue> cvUnmodifiableMap = (Map<String, CustomValue>) FieldUtils.readDeclaredField(cvc, "customValues", true);
					Map<String, CustomValue> cvMap = new HashMap<String, CustomValue> (cvUnmodifiableMap);
					CustomValue cv = new CustomValueImpl.StringImpl(MOD_ID);
					cvMap.put(MM_PARENT_KEY, cv);
					FieldUtils.writeField(cvc, "customValues", Collections.unmodifiableMap(cvMap), true);
					FieldUtils.writeField(mm, "custom", cvc, true);
					
					LOG.debug("Adding!" + modId);
					oldModMap.put(modId, mod);
					oldMods.add(mod);
					
					// Language Adapters
					Map<String, LanguageAdapter> changedAdapters = (Map<String, LanguageAdapter>) FieldUtils.readDeclaredField(fl, "adapterMap", true);
					changedAdapters.forEach((k, v) -> 
					{
			            if(!oldAdapterMap.containsKey(k))
			            {
			            	oldAdapterMap.put(k, v);
			            }
			        });
					
					// Entry Points
					oldEntrypointStorage = FieldUtils.readDeclaredField(fl, "entrypointStorage", true);
					Map<String, List<?>> changedEntryMap = (Map<String, List<?>>) FieldUtils.readDeclaredField(oldEntrypointStorage, "entryMap", true);
					changedEntryMap.forEach((k, v) -> {
			            if(v.toString().contains(mod.getInfo().getId()))
			            {
			            	oldEntries.put(k, v);								
			            }
			        });
				}
			}
			
			FieldUtils.writeField(fl, "gameDir", oldGameDir, true);
			FieldUtils.writeField(fl, "modMap", oldModMap, true);
			FieldUtils.writeField(fl, "mods", oldMods, true);
			FieldUtils.writeField(fl, "adapterMap", oldAdapterMap, true);
			FieldUtils.writeField(oldEntrypointStorage, "entryMap", oldEntries, true);
			FieldUtils.writeField(fl, "entrypointStorage", oldEntrypointStorage, true);
			
		} catch (IllegalAccessException e) {
			LOG.warn("Problem updating FabricLoader fields");
			e.printStackTrace();
		}
		
	}
	
	private static void extractLoadedMods()
	{
		Path jarDir = cl.getModsDir();
		Path jarPath = cl.getModJarPath(MOD_ID);
		try {
		if (Files.notExists(jarDir))
		{
			Files.createDirectory(jarDir);
		}
		LOG.info("Unzipping mod JAR");
		unZipLoadedJars(jarPath.toString(), jarDir.toString());
		} catch (IOException e) {
			LOG.info("Problem extracting LoadedMods");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void loadMods() throws IOException, ModResolutionException
	{
		LOG.enter("loadMods");
		
		// Prevent an infinite loop
		if (!startedManyModLoad)
		{
			startedManyModLoad = true;
			extractLoadedMods();
			try {
				Map<String, ModContainer> oldModMap = (Map<String, ModContainer>) FieldUtils.readDeclaredField(fl, "modMap", true);
				List<ModContainer> oldMods = (List<ModContainer>) FieldUtils.readDeclaredField(fl, "mods", true);
				Object oldGameDir = FieldUtils.readDeclaredField(fl, "gameDir", true);
				Object oldEntrypointStorage = FieldUtils.readDeclaredField(fl, "entrypointStorage", true);
				Map<String, List<?>> oldEntries = (Map<String, List<?>>)FieldUtils.readDeclaredField(oldEntrypointStorage, "entryMap", true);
				Map<String, LanguageAdapter> oldAdapterMap = (Map<String, LanguageAdapter>) FieldUtils.readDeclaredField(fl, "adapterMap", true);
						
				addNewMods(oldModMap, oldEntrypointStorage);
				initMixins();
				
				updateLoaderFields(oldModMap, oldMods, oldAdapterMap, oldGameDir,
						oldEntrypointStorage, oldEntries);
			} catch (IllegalAccessException e1) {
				LOG.warn("How is this possible?");
				e1.printStackTrace();
			}
			
		}
		
		LOG.exit("loadMods");
	}
	
	public static void finishMixinBootstrapping()
	{
		try {
			Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
			m.setAccessible(true);
			m.invoke(null, MixinEnvironment.Phase.INIT);
			m.invoke(null, MixinEnvironment.Phase.DEFAULT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
