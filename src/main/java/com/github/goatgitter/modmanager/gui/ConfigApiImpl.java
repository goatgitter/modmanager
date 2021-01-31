package com.github.goatgitter.modmanager.gui;

import com.github.goatgitter.modmanager.util.Log;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
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
