package com.github.goatgitter.modmanager;

import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.ModConfig;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
/**
 * 
 * @author GoatGitter
 * @since 08/11/2020
 * 
 */
public class ModPreLaunch implements PreLaunchEntrypoint{
	private static Log LOG = new Log("ModPreLaunch");
	@Override
	public void onPreLaunch() {
		LOG.enter("onPreLaunch");
		try {
			ModConfig.loadMods();
		}
		catch(Exception e)
		{
			LOG.warn("Problem in onPreLaunch");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		LOG.exit("onPreLaunch");
	}

}
