package com.github.goatgitter.modmanager.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.goatgitter.modmanager.ModManager;
/******************************************************************************************
 *              Notebook
 * ---------------------------------------------------------------------------------------
 * @author	goatgitter
 * @since	02/04/2021
 * @purpose	This class for reading, writing, and organizing Files.
 ****************************************************************************************/

public class Notebook {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("Cabinet");
	/***************************************************
	 *              METHODS
	 **************************************************/
	public static String get(Path filePath)
	{
		String fileContents = "";
		try {
			File theFile = new File(filePath.normalize().toUri());
			fileContents = FileUtils.readFileToString(theFile, "UTF-8");
		} catch (IOException e) {
			StickyNote.logErrorMsg(LOG, ModManager.KEY_NOTEBOOK_ERROR_GET, filePath.getFileName());
			e.printStackTrace();
		}
		return fileContents;
	}
	
	public static List<String> getAsList(Path filePath)
	{
		List<String> contents = new ArrayList<String>();
		try {
			contents = Files.readAllLines(filePath.normalize(), Charset.defaultCharset());
		} catch (IOException e) {
			StickyNote.logErrorMsg(LOG, ModManager.KEY_NOTEBOOK_ERROR_GET, filePath.getFileName());
			e.printStackTrace();
		}
		
		return contents;
	}
}
