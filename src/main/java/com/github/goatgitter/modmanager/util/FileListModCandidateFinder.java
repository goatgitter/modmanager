package com.github.goatgitter.modmanager.util;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.discovery.ModCandidateFinder;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class FileListModCandidateFinder implements ModCandidateFinder {
	private Log LOG = new Log("FileListModCandidateFinder");
	private ModListLoader modListLoader;
	private String modListFile;

	public FileListModCandidateFinder(String selectedList) 
	{
		this.modListLoader = new ModListLoader();
		this.modListFile = selectedList;
	}

	@Override
	public void findCandidates(FabricLoader loader, BiConsumer<URL, Boolean> urlProposer)
	{
		Path selectedListPath = modListLoader.getModList(modListFile);
		List<String> mods = Notebook.getAsList(selectedListPath);

		for (String modJarName : mods) {
			File modFile = new File(modJarName);
			Path srcJarPath = modFile.toPath();
			if(Files.exists(srcJarPath))
			{
				try {
					urlProposer.accept(UrlUtil.asUrl(modFile), false);
				} catch (UrlConversionException e) {
					throw new RuntimeException("Failed to convert URL for mod '" + modJarName + "'!", e);
				}
			}
			else
			{
				LOG.trace("JAR File did not exist =>" + modJarName);
			}
		}
	}
}

