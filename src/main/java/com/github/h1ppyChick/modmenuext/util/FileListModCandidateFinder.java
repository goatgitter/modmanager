package com.github.h1ppyChick.modmenuext.util;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.discovery.ModCandidateFinder;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;

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
	public void findCandidates(FabricLoader loader, BiConsumer<URL, Boolean> urlProposer){
		try 
		{
			Path selectedListPath = modListLoader.getModList(modListFile);
			List<String> mods = Files.readAllLines(selectedListPath);

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
					LOG.info("JAR File did not exist =>" + modJarName);
				}
			}
			
		} catch (IOException e1) {
			LOG.warn("Unable to read mod file list =>" + modListFile);
		}
	}
}

