package com.github.h1ppyChick.modmanager.util;

import java.util.Set;

import com.google.common.collect.LinkedListMultimap;

import net.fabricmc.loader.api.ModContainer;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 */
public class Conditional {

	public static void add(Set<String> set, String id)
	{
		if (!set.contains(id))
		{
			set.add(id);
		}
	}

	public static void add(LinkedListMultimap<ModContainer, ModContainer> map, ModContainer parent, ModContainer mod)
	{
		if(!map.containsEntry(parent, mod))
		{
			map.put(parent, mod);
		}
	}

}
