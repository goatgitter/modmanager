package com.github.goatgitter.modmanager;

import com.github.goatgitter.modmanager.util.Log;
import net.fabricmc.api.ClientModInitializer;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class ModClient implements ClientModInitializer{
	// Instance Variables
	Log LOG = new Log("ModClient");
	@Override
	public void onInitializeClient() {
		LOG.enter("onInitializeClient");
		LOG.exit("onInitializeClient");
	}
}
