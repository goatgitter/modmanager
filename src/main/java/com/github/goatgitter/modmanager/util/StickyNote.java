package com.github.goatgitter.modmanager.util;

import com.github.goatgitter.modmanager.ModManager;
import com.github.goatgitter.modmanager.gui.ModToast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
/******************************************************************************************
 *              STICKY NOTE
 * ---------------------------------------------------------------------------------------
 * @author	goatgitter
 * @since	01/29/2021
 * @purpose	This class makes adding messages to the UI easier.  Using the 
 * 			ModToast class to make sure all messages are visible to user.
 ****************************************************************************************/
public class StickyNote {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("StickyNote");
	private static ModToast userMsg = null;
	
	/***************************************************
	 *              METHODS
	 **************************************************/
	public static void logErrorMsg(Log theLog, String key,Object... args )
	{
		logClientMsg(theLog, ModToast.Type.ERROR, key, args);
	}
	
	public static void addErrorMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModToast.Type.ERROR, key, args);
	}
	
	public static void showErrorMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModToast.Type.ERROR, key, args);
		showClientMsg(client);
	}
	
	public static void addSuccessMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModToast.Type.SUCCESS, key, args);
	}
	
	public static void showSuccessMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModToast.Type.SUCCESS, key, args);
		showClientMsg(client);
	}
	
	public static void addImportMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModToast.Type.IMPORT, key, args);
	}
	
	public static void addClientMsg(MinecraftClient client, ModToast.Type type, String key,Object... args )
	{
		
		Text msg = new TranslatableText(key, args);
		Text title = null;
		switch(type)
		{
			case IMPORT:
				title = ModManager.TEXT_IMPORT_TOOLTIP;
				break;
			case SUCCESS:
				title = ModManager.TEXT_SUCCESS;
				break;
			default:
				title = ModManager.TEXT_ERROR;
				break;
		}
		if (userMsg == null)
		{
			userMsg = ModToast.create(client, type, title, msg);
		}
		else
		{
			userMsg.addTextToList(client, msg);
		}
		logClientMsg(LOG,type, key, args);
	}
	
	public static void logClientMsg(Log theLog, ModToast.Type type, String key,Object... args )
	{
		String title = null;
	    String msg = I18n.translate(key, args);
	    String logMsg = null;
		switch(type)
		{
			case IMPORT:
				title = I18n.translate(ModManager.KEY_IMPORT_TOOLTIP);
				logMsg = title + " => " + msg;
				theLog.debug(logMsg);
				break;
			case SUCCESS:
				title = I18n.translate(ModManager.KEY_SUCCESS);
				logMsg = title + " => " + msg;
				theLog.debug(logMsg);
				break;
			default:
				title = I18n.translate(ModManager.KEY_ERROR);				
				logMsg = title + " => " + msg;
				theLog.warn(logMsg);
				break;
		}
		
	}
	
	public static void showClientMsg(MinecraftClient client)
	{
		if (userMsg != null)
		{
			ToastManager toastManager = client.getToastManager();
			toastManager.add(userMsg);
			userMsg = null;
		}
	}

	
}
