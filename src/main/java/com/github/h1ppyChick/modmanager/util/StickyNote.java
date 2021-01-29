package com.github.h1ppyChick.modmanager.util;

import com.github.h1ppyChick.modmanager.ModManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
/******************************************************************************************
 *              STICKY NOTE
 * ---------------------------------------------------------------------------------------
 * Author:	h1ppychick
 * Since:	01/29/2021
 * Purpose:	This class makes adding messages to the UI easier.
 ****************************************************************************************/
public class StickyNote {
	/***************************************************
	 *              INSTANCE VARIABLES
	 **************************************************/
	private static Log LOG = new Log("ChildModsScreen");
	/***************************************************
	 *              METHODS
	 **************************************************/
	public static void addErrorMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModManager.TEXT_ERROR, key, args);
	}
	
	public static void addSuccessMsg(MinecraftClient client, String key,Object... args )
	{
		addClientMsg(client, ModManager.TEXT_SUCCESS, key, args);
	}
	
	public static void addClientMsg(MinecraftClient client, Text title, String key,Object... args )
	{
		ToastManager toastManager = client.getToastManager();
		Text msg = new TranslatableText(key, args);
		toastManager.add(SystemToast.create(client, SystemToast.Type.TUTORIAL_HINT, title, msg));
	}
	
}
