package com.github.h1ppyChick.modmanager;

import com.github.h1ppyChick.modmanager.util.Log;
import com.github.h1ppyChick.modmanager.util.ModConfig;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 */
public class ModPreLaunch implements PreLaunchEntrypoint{
	private static Log LOG = new Log("ModPreLaunch");
	@Override
	public void onPreLaunch() {
		LOG.enter("onPreLaunch");
		ModConfig.loadMods();
		LOG.exit("onPreLaunch");
	}

}
