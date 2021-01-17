package com.github.h1ppyChick.modmenuext;

import com.github.h1ppyChick.modmenuext.util.Log;
import com.github.h1ppyChick.modmenuext.util.ModConfig;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ModMenuExtPreLaunch implements PreLaunchEntrypoint{
	private static Log LOG = new Log("ModMenuExtPreLaunch");
	@Override
	public void onPreLaunch() {
		LOG.enter("onPreLaunch");
		ModConfig.loadMods();
		LOG.exit("onPreLaunch");
	}

}
