package combined.util;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.discovery.ModCandidateFinder;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;

public class FileListModCandidateFinder implements ModCandidateFinder {
	private LogUtils LOG = new LogUtils("FileListModCandidateFinder");
	private CombinedLoader cl;
	private String loadList;

	public FileListModCandidateFinder(String loadList) 
	{
		this.cl = new CombinedLoader();
		this.loadList = loadList;
	}

	@Override
	public void findCandidates(FabricLoader loader, Consumer<URL> urlProposer) {
		try 
		{
			Path loadListPath = cl.getLoadFile(loadList);
			List<String> mods = Files.readAllLines(loadListPath);

			for (String modJarName : mods) {
				File modFile = new File(modJarName);
				Path srcJarPath = modFile.toPath();
				if(Files.exists(srcJarPath))
				{
					try {
						urlProposer.accept(UrlUtil.asUrl(modFile));
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
			LOG.warn("Unable to read mod file list =>" + loadList);
		}
	}
}

