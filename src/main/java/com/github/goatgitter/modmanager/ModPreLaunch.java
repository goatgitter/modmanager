package com.github.goatgitter.modmanager;

import com.github.goatgitter.modmanager.util.Log;
import com.github.goatgitter.modmanager.util.ModConfig;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.gui.FabricGuiEntry;
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
		catch(Exception exception)
		{
			LOG.warn("Problem in onPreLaunch");
			exception.printStackTrace();
			FabricGuiEntry.displayCriticalError(exception, true);
		}
		LOG.exit("onPreLaunch");
	}

}
