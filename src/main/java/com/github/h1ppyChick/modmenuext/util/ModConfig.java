package com.github.h1ppyChick.modmenuext.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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

import com.github.h1ppyChick.modmenuext.ModMenuExt;
import com.github.h1ppyChick.modmenuext.gui.ChildModEntry;
import com.github.h1ppyChick.modmenuext.gui.Menu;
import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.discovery.ModCandidate;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.fabricmc.loader.metadata.EntrypointMetadata;

import net.fabricmc.loader.metadata.NestedJarEntry;

/**
 * @author h1ppyChic
 * 
 * I should probably refactor this...
 * This is class of helper methods for changing the mod configuration
 * 
 */
public class ModConfig {
	// Instance variables (fields)
	private static Log LOG = new Log("ModConfigUtil");
	private static FabricLoader fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();
	private static CombinedLoader cl = new CombinedLoader();
	
	// Methods
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
			Path loadListPath = cl.getLoadFile();
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
	public static void requestUnload(ModListEntry mod)
	{
		LOG.info("Requested unload of mod =>" + mod.getMetadata().getId() + ".");
		Path unloadListPath = cl.getUnLoadFile();
		Path loadlistPath = cl.getLoadFile();
		Path srcJarPath = cl.getModJarPath(mod);
		cl.addJarToFile(unloadListPath, srcJarPath);
		cl.removeJarFromFile(loadlistPath, srcJarPath);
		removeMod(mod);
		Menu.removeChildEntry(mod);
	}
	
	public static void requestLoad(ChildModEntry mod)
	{
		LOG.info("Requested load of mod =>" + mod.getMetadata().getId() + ".");
		Path unloadListPath = cl.getUnLoadFile();
		Path loadlistPath = cl.getLoadFile();
		Path srcJarPath = cl.getModJarPath(mod.getContainer());
		cl.addJarToFile(loadlistPath, srcJarPath);
		cl.removeJarFromFile(unloadListPath, srcJarPath);
		loadMods();
	}
	
	@SuppressWarnings("unchecked")
	public static void loadMods()
	{
		LOG.enter("loadMods");
		// Setup the menu config for the mods that have already been loaded.
//		ModMenu mm = new ModMenu();
//		mm.onInitializeClient();
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
	
	// Private methods
	
	private static void unZipLoadedJars(String jarFileName, String destDirName) throws IOException
	{
		JarFile jar = new JarFile(jarFileName);
		Enumeration<JarEntry> enumEntries = jar.entries();
		Path loadFilePath = cl.getLoadFile();
		while (enumEntries.hasMoreElements()) {
		    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
		    
		    if (file.getName().contains(ModMenuExt.LOAD_JAR_DIR) && file.getName().endsWith(".jar")) {
		    	String destFileName = destDirName + java.io.File.separator + file.getName().replace(ModMenuExt.LOAD_JAR_DIR, "");
		    	
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
			Map<String, ModCandidate> candidateMap = cl.getSelectedMods();
			LOG.info("Loading " + candidateMap.values().size() + " mods! => " + candidateMap.values().stream()
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
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			LOG.warn("Problem adding new mods");
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static void removeMod(ModListEntry mod)
	{
		try {
			Map<String, ModContainer> oldModMap = (Map<String, ModContainer>) FieldUtils.readDeclaredField(fl, "modMap", true);
			List<ModContainer> oldMods = (List<ModContainer>) FieldUtils.readDeclaredField(fl, "mods", true);
			Object oldGameDir = FieldUtils.readDeclaredField(fl, "gameDir", true);
			Object oldEntrypointStorage = FieldUtils.readDeclaredField(fl, "entrypointStorage", true);
			Map<String, List<?>> oldEntries = (Map<String, List<?>>)FieldUtils.readDeclaredField(oldEntrypointStorage, "entryMap", true);
			Map<String, LanguageAdapter> oldAdapterMap = (Map<String, LanguageAdapter>) FieldUtils.readDeclaredField(fl, "adapterMap", true);
			
			String thisModId = mod.getMetadata().getId();
			if (oldModMap.containsKey(thisModId))
			{
				ModContainer thisMod = oldModMap.get(thisModId);
				// Remove Entry points
				Map<String, List<?>> entriesToRemove = new HashMap<>();
				oldEntries.forEach((k, v) -> {
		            if(v.toString().contains(thisMod.getInfo().getId()))
		            {
		            	entriesToRemove.put(k, v);
		            }
				});
				
				entriesToRemove.forEach((k, v) -> {
		            if(v.toString().contains(thisMod.getInfo().getId()))
		            {
		            	oldEntries.remove(k, v);
		            }
				});

				for (String key : thisMod.getInfo().getEntrypointKeys()) {
					for (EntrypointMetadata value : thisMod.getInfo().getEntrypoints(key)) {
						oldEntries.remove(key, value);
					}
				}
				
				// Remove Lang Adapter
				for (Map.Entry<String, String> laEntry : thisMod.getInfo().getLanguageAdapterDefinitions().entrySet()) {
					if (oldAdapterMap.containsKey(laEntry.getKey())) {
						oldAdapterMap.remove(laEntry.getKey(), laEntry.getValue());
					}
				}
				
				oldModMap.remove(thisModId, thisMod);
				//addMod(candidate);
				oldMods.remove(thisMod);
			}
			
			FieldUtils.writeField(fl, "gameDir", oldGameDir, true);
			FieldUtils.writeField(fl, "modMap", oldModMap, true);
			FieldUtils.writeField(fl, "mods", oldMods, true);
			FieldUtils.writeField(fl, "adapterMap", oldAdapterMap, true);
			FieldUtils.writeField(oldEntrypointStorage, "entryMap", oldEntries, true);
			FieldUtils.writeField(fl, "entrypointStorage", oldEntrypointStorage, true);
			
		} catch (IllegalAccessException e) {
			LOG.warn("Problem removing mods");
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
		fl.loadAccessWideners();
		try {
			LOG.debug("Clearing init properties for Mixin Bootstrap");
			Object clearInit = null;
			GlobalProperties.put(GlobalProperties.Keys.INIT, clearInit);
			FieldUtils.writeStaticField(MixinBootstrap.class,  "initialised", false, true);
			FieldUtils.writeStaticField(MixinBootstrap.class,  "platform", null, true);
			
			LOG.debug("Calling mixin bootstrap");
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
	private static void addParentToMod(ModContainer childMod)
	{
		if (childMod != null)
		{
			// Add custom metadata to show the loaded mod as a child of many mods.
			ModMetadata mm = childMod.getMetadata();
			System.out.print(mm.getType());
			try {
				Map<String, CustomValue> cvUnmodifiableMap = (Map<String, CustomValue>) FieldUtils.readDeclaredField(mm, "customValues", true);
				Map<String, CustomValue> cvMap = new HashMap<String, CustomValue> (cvUnmodifiableMap);
				CustomValue cv = new CustomValueImpl.StringImpl(ModMenuExt.MOD_ID);
				cvMap.put(ModMenuExt.MM_PARENT_KEY, cv);
				FieldUtils.writeField(mm, "customValues", Collections.unmodifiableMap(cvMap), true);
			} catch (IllegalAccessException e) {
				LOG.warn("Problem adding parent to mod => " + childMod.getInfo().getId());
				e.printStackTrace();
			}
		}
		
	}
	
	private static boolean doesEntryExist(Map<String, List<?>> oldEntries, String stringVal)
	{
		boolean entryExists = false;
		for(Object entry: oldEntries.values())
		{
			if (entry.toString().equals(stringVal)) {
				entryExists = true;
				break;
			}
		}
		return entryExists;
	}
	
	private static boolean doesEntryExist(Map<String, List<?>> oldEntries, String key, String stringVal)
	{
		boolean entryExists = false;
		List<?> entries = oldEntries.get(key);
		if (entries != null)
		{
			for(Object entry: entries)
			{
				if (entry.toString().equals(stringVal)) {
					entryExists = true;
					break;
				}
			}
		}
		
		return entryExists;
	}
	@SuppressWarnings("unchecked")
	private static void updateLoaderFields(Map<String, ModContainer> oldModMap, List<ModContainer> oldMods, Map<String, LanguageAdapter> oldAdapterMap, Object oldGameDir,
			Object oldEntrypointStorage, Map<String, List<?>> oldEntries)
	{
		
		try {
			// Add parent tag to any mods added to this mod
			ModContainer modMenuExtMod = oldModMap.get(ModMenuExt.MOD_ID);
			for(NestedJarEntry nestedJar: modMenuExtMod.getInfo().getJars())
			{
				String jarFileName = cl.getNestedJarFileName(nestedJar);
				ModContainer nestedMod = cl.getModForJar(jarFileName, oldMods);
				String nestedModId = nestedMod.getInfo().getId();
				LOG.debug("Adding!-Nested mod => " + nestedModId);
				addParentToMod(nestedMod);
				//Menu.addChild(modMenuExtMod, nestedMod);
			}
			List<ModContainer> changedMods = (List<ModContainer>) FieldUtils.readDeclaredField(fl, "mods", true);
			// Add the new mod data
			for(ModContainer mod : changedMods)
			{
				String modId = mod.getMetadata().getId();
				LOG.debug("Checking changed mod id =>" + modId);
				// Don't add the fabric api again
				if (!cl.isRequiredMod(modId) && !oldModMap.containsKey(mod.getInfo().getId()))
				{
					LOG.debug("Adding!" + modId);
					addParentToMod(mod);
					//Menu.addChild(modMenuExtMod, mod);
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
					//oldEntrypointStorage = FieldUtils.readDeclaredField(fl, "entrypointStorage", true);
					
					for (String in : mod.getInfo().getOldInitializers()) {
						String stringVal = modId + "->" + in;
						boolean entryExists = doesEntryExist(oldEntries, stringVal);
						
						if (!entryExists)
						{
							String adapter = mod.getInfo().getOldStyleLanguageAdapter();
							Object[] addArgs = {mod, adapter, in};
							MethodUtils.invokeMethod(oldEntrypointStorage, true, "addDeprecated", addArgs);
						}
						
					}

					for (String key : mod.getInfo().getEntrypointKeys()) {
						for (EntrypointMetadata in : mod.getInfo().getEntrypoints(key)) {
							String stringVal = modId + "->(0.3.x)" + in.getValue();
							boolean entryExists = doesEntryExist(oldEntries, key, stringVal);
							if (!entryExists)
							{
								Object[] addArgs = {mod, key, in, oldAdapterMap};
								MethodUtils.invokeMethod(oldEntrypointStorage, true, "add", addArgs);
							}
						}
					}
				}
			}
			// Remove fabric mods from old map
			List<ModContainer> modsToRemove = new ArrayList<ModContainer>();
			for (ModContainer mod: oldMods)
			{
				String modId = mod.getMetadata().getId();
				if (cl.isRequiredMod(modId) && !modId.equals(CombinedLoader.BASE_MOD_ID))
				{
					modsToRemove.add(mod);
				}
			}
			oldMods.removeAll(modsToRemove);
			FieldUtils.writeField(fl, "gameDir", oldGameDir, true);
			FieldUtils.writeField(fl, "modMap", oldModMap, true);
			FieldUtils.writeField(fl, "mods", oldMods, true);
			FieldUtils.writeField(fl, "adapterMap", oldAdapterMap, true);
			FieldUtils.writeField(oldEntrypointStorage, "entryMap", oldEntries, true);
			FieldUtils.writeField(fl, "entrypointStorage", oldEntrypointStorage, true);
			
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException  e) {
			LOG.warn("Problem updating FabricLoader fields");
			e.printStackTrace();
		}
		
	}
	
	private static void extractLoadedMods()
	{
		Path jarDir = cl.getModsDir();
		Path jarPath = cl.getModJarPath(ModMenuExt.MOD_ID);
		try {
		if (Files.notExists(jarDir))
		{
			Files.createDirectory(jarDir);
		}
		LOG.debug("Unzipping mod JAR");
		unZipLoadedJars(jarPath.toString(), jarDir.toString());
		} catch (IOException e) {
			LOG.warn("Problem extracting LoadedMods");
			e.printStackTrace();
		}
	}
}
