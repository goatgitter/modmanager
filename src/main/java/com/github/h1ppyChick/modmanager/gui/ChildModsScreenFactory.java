package com.github.h1ppyChick.modmanager.gui;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.minecraft.client.gui.screen.Screen;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 */
public class ChildModsScreenFactory implements ConfigScreenFactory<ChildModsScreen>{
	// Instance Variables
	private ChildModsScreen _screen = null;
	// Constructor
	public ChildModsScreenFactory() {
	}
	
	@Override
	public ChildModsScreen create(Screen parent) {
		if (_screen == null)
		{
			_screen = new ChildModsScreen(parent);
		}
		return _screen;
	}

}
