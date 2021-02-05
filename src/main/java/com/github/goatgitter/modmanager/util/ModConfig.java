package com.github.goatgitter.modmanager.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.config.Props;
import com.github.goatgitter.modmanager.gui.ChildModEntry;
import com.github.goatgitter.modmanager.gui.Menu;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.gui.ModListEntry;
import net.fabricmc.accesswidener.AccessWidener;
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
 * @author goatgitter
 * @since 08/11/2020
 * 
 * This is class of helper methods for reading and writing mod configurations.
 * 
 */
public class ModConfig {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("ModConfig");
	private static FabricLoader fl = (FabricLoader) net.fabricmc.loader.api.FabricLoader.getInstance();
	private static ModListLoader modListLoader = new ModListLoader();
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	
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
		if (modListLoader.isRequiredMod(modId)) 
		{
			canTurnOff = false;
		}
		else
		{
			Path srcJarPath = modListLoader.getModJarPath(mod);
			Path selectedListPath = modListLoader.getSelectedModList();
			try {
				if (!modListLoader.isModInListFile(selectedListPath, srcJarPath))
					canTurnOff=false;
			} catch (IOException e) {
				LOG.warn("Problem determining if mod can be turned off");
				e.printStackTrace();
			}
		}
		
		return canTurnOff;
	}
	
	public static void requestUnload(ModListEntry mod)
	{
		LOG.trace("Requested unload of mod =>" + mod.getMetadata().getId() + ".");
		Path availListPath = modListLoader.getAvailModListFile();
		Path selectedListPath = modListLoader.getSelectedModList();
		Path srcJarPath = modListLoader.getModJarPath(mod);
		modListLoader.addJarToFile(availListPath, srcJarPath);
		modListLoader.removeJarFromFile(selectedListPath, srcJarPath);
		removeMod(mod);
		Menu.removeChildEntry(mod);
	}
	
	public static void requestLoad(ChildModEntry mod)
	{
		LOG.trace("Requested load of mod =>" + mod.getMetadata().getId() + ".");
		Path availListPath = modListLoader.getAvailModListFile();
		Path selectedListPath = modListLoader.getSelectedModList();
		Path srcJarPath = modListLoader.getModJarPath(mod.getContainer());
		modListLoader.addJarToFile(selectedListPath, srcJarPath);
		modListLoader.removeJarFromFile(availListPath, srcJarPath);
		loadMods();
	}
	
	@SuppressWarnings("unchecked")
	public static void loadMods()
	{
		LOG.enter("loadMods");
		// Setup the menu config for the mods that have already been loaded.
		retrieveRequiredFilesForLoad();
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
	private static void addNewMods(Map<String, ModContainer> oldModMap, Object oldEntrypointStorage)
	{
		try {
			Map<String, ModCandidate> candidateMap = modListLoader.getSelectedMods();
			if (candidateMap == null) return;
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
			AccessWidener oldAw = (AccessWidener)FieldUtils.readDeclaredField(fl, "accessWidener", true);
			String oldNamespace = (String)FieldUtils.readDeclaredField(oldAw, "namespace", true);
			FieldUtils.writeField(oldAw, "namespace", null, true);
			FieldUtils.writeField(fl, "accessWidener", oldAw, true);
			fl.freeze();
			modListLoader.loadAccessWideners();
			FieldUtils.writeField(oldAw, "namespace", oldNamespace, true);
			FieldUtils.writeField(fl, "accessWidener", oldAw, true);
		} catch (IllegalAccessException e) {
			LOG.warn("Problem updating frozen field");
			e.printStackTrace();
		}
		
		try {
			
			LOG.trace("Clearing init properties for Mixin Bootstrap");
			Object clearInit = null;
			GlobalProperties.put(GlobalProperties.Keys.INIT, clearInit);
			FieldUtils.writeStaticField(MixinBootstrap.class,  "initialised", false, true);
			FieldUtils.writeStaticField(MixinBootstrap.class,  "platform", null, true);
			
			LOG.trace("Calling mixin bootstrap");
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
			
			LOG.trace("-------------------------------------------------");
			LOG.trace("             START Mixin BootStrap             ");
			FabricMixinBootstrap.init(fl.getEnvironmentType(), fl);
			finishMixinBootstrapping();
			LOG.trace("             END Mixin BootStrap                 ");
			LOG.trace("-------------------------------------------------");
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
				CustomValue cv = new CustomValueImpl.StringImpl(ModManager.MOD_ID);
				cvMap.put(ModManager.MM_PARENT_KEY, cv);
				FieldUtils.writeField(mm, "customValues", Collections.unmodifiableMap(cvMap), true);
			} catch (IllegalAccessException e) {
				LOG.warn("Problem adding parent to mod => " + childMod.getInfo().getId());
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void updateLoaderFields(Map<String, ModContainer> oldModMap, List<ModContainer> oldMods, Map<String, LanguageAdapter> oldAdapterMap, Object oldGameDir,
			Object oldEntrypointStorage, Map<String, List<?>> oldEntries)
	{
		
		try {
			// Add parent tag to any mods added to this mod
			ModContainer modMenuExtMod = oldModMap.get(ModManager.MOD_ID);
			for(NestedJarEntry nestedJar: modMenuExtMod.getInfo().getJars())
			{
				String jarFileName = modListLoader.getNestedJarFileName(nestedJar);
				ModContainer nestedMod = modListLoader.getModForJar(jarFileName, oldMods);
				String nestedModId = nestedMod.getInfo().getId();
				LOG.trace("Adding!-Nested mod => " + nestedModId);
				addParentToMod(nestedMod);
			}
			List<ModContainer> changedMods = (List<ModContainer>) FieldUtils.readDeclaredField(fl, "mods", true);
			// Add the new mod data
			for(ModContainer mod : changedMods)
			{
				String modId = mod.getMetadata().getId();
				LOG.trace("Checking changed mod id =>" + modId);
				// Don't add the fabric api again
				if (!modListLoader.isRequiredMod(modId) && !oldModMap.containsKey(mod.getInfo().getId()))
				{
					LOG.trace("Adding!" + modId);
					addParentToMod(mod);
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
					for (String key : mod.getInfo().getEntrypointKeys()) {
						Map<String, List<?>> newEntries = (Map<String, List<?>>)FieldUtils.readDeclaredField(oldEntrypointStorage, "entryMap", true);
						
						List<?> newKeyEntries = newEntries.get(key);
						List<?> oldKeyEntries = oldEntries.get(key);
						if(oldKeyEntries == null && newKeyEntries != null)
						{
							oldEntries.put(key, newKeyEntries);
						}
						else if (oldKeyEntries != null && newKeyEntries != null)
						{
							List<?> combinedEntries = Stream.concat(newKeyEntries.stream(),oldKeyEntries.stream())
									.distinct()
									.collect(Collectors.toList());
							oldEntries.put(key, combinedEntries);
						}
					}
				}
			}
			// Remove fabric mods from old map
			List<ModContainer> modsToRemove = new ArrayList<ModContainer>();
			for (ModContainer mod: oldMods)
			{
				String modId = mod.getMetadata().getId();
				if (modListLoader.isRequiredMod(modId) && !modId.equals(ModListLoader.BASE_MOD_ID))
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
			
		} catch (IllegalAccessException e) {
			LOG.warn("Problem updating FabricLoader fields");
			e.printStackTrace();
		}
		
	}
	
	private static void retrieveRequiredFilesForLoad()
	{
		Path jarDir = Props.getModsDirPath();
		Path jarPath = modListLoader.getModJarPath(ModManager.MOD_ID);
		try {
		if (Files.notExists(jarDir))
		{
			Files.createDirectory(jarDir);
		}
		Cabinet.retrieveConfigFile(jarPath.toString());
		} catch (IOException e) {
			LOG.warn("Problem extracting config file");
			e.printStackTrace();
		}
	}
}
