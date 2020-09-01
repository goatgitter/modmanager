package combined;

import combined.util.Log;
import combined.util.ModConfig;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ManyModsPreLaunch implements PreLaunchEntrypoint{
	private static Log LOG = new Log("ManyModsPreLaunch");
	@Override
	public void onPreLaunch() {
		LOG.enter("onPreLaunch");
		ModConfig.loadMods();
		LOG.exit("onPreLaunch");
	}

}
