package com.github.h1ppyChick.modmenuext.gui;

import com.github.h1ppyChick.modmenuext.util.Log;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class ConfigApiImpl implements ModMenuApi {
	// Constants
	// Instance Variables
	private static Log LOG = new Log("ConfigApiImpl");
	// Methods
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		LOG.enter("getModConfigScreenFactory");
		ChildModsScreenFactory factory = new ChildModsScreenFactory();
		LOG.exit("getModConfigScreenFactory");
		return factory;
	}
}
