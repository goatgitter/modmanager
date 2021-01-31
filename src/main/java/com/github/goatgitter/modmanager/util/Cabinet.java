package com.github.goatgitter.modmanager.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.config.Props;

import net.minecraft.client.MinecraftClient;

/******************************************************************************************
 *              Cabinet
 * ---------------------------------------------------------------------------------------
 * @author	h1ppychick
 * @since	01/29/2021
 * @purpose	This class for working with Archive Files.
 ****************************************************************************************/

public class Cabinet {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("Cabinet");
	/***************************************************
	 *              METHODS
	 **************************************************/
	public static void retrieveConfigFile(String jarFileName) throws IOException
	{
		JarFile jar = new JarFile(jarFileName);
		Enumeration<JarEntry> enumEntries = jar.entries();
		while (enumEntries.hasMoreElements()) {
			java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
		    
			if (file.getName().contains(ModManager.CONFIG_DIR) && file.getName().endsWith(".properties")) {
		    	Path destFilePath = Props.getModConfigPath();
		    	File f = destFilePath.toFile();
		    	
		    	if (Files.notExists(destFilePath))
		    	{
		    		InputStream is = jar.getInputStream(file); 
				    FileOutputStream fos = new FileOutputStream(f);
				    while (is.available() > 0) {  
				        fos.write(is.read());
				    }
				    fos.close();
				    is.close();
		    	}
		    }
		}
		jar.close();
	}
	
	public static List<String> getAvailArchives()
	{
		List<String> availList = new ArrayList<String>();
		Path dirToCheck = Props.getModsDirPath();
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
	
	@SuppressWarnings("unchecked")
	public static boolean retreiveModList(String listName, MinecraftClient client)
	{
		boolean result = true;
		Path modListZipPath = Props.getModsDirPath().resolve(listName + ".zip");

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
				Path destFilePath = Props.getModsDirPath();
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
			Props.setSelectedModListName(listName, true);
			StickyNote.addImportMsg(client, ModManager.KEY_IMPORT_SUCCESS, listName);
		}
		return result;
	}
	
	public static boolean storeModList(String listName)
	{
		boolean result = true;
		Path modListPath = Props.getModsDirPath().resolve(listName + ".txt");
		Path modListZipPath = Props.getModsDirPath().resolve(listName + ".zip");
		
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
				if(modJarName.contains(Props.getModsDirPath().toString()) && modJarName.endsWith(".jar"))
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
}
