package com.github.h1ppyChick.modmenuext;

import com.github.h1ppyChick.modmenuext.util.Log;
import net.fabricmc.api.ClientModInitializer;

public class ModMenuExtClient implements ClientModInitializer{
	// Instance Variables
	Log LOG = new Log("ModMenuExtClient");
	@Override
	public void onInitializeClient() {
		LOG.enter("onInitializeClient");
		LOG.exit("onInitializeClient");
	}
}
