package com.github.h1ppyChick.modmanager;

import com.github.h1ppyChick.modmanager.util.Log;
import net.fabricmc.api.ClientModInitializer;

public class ModClient implements ClientModInitializer{
	// Instance Variables
	Log LOG = new Log("ModClient");
	@Override
	public void onInitializeClient() {
		LOG.enter("onInitializeClient");
		LOG.exit("onInitializeClient");
	}
}
