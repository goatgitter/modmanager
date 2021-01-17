package com.github.h1ppyChick.modmenuext.gui;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.minecraft.client.gui.screen.Screen;

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
