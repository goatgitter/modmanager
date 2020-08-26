package combined;

import java.io.IOException;

import combined.util.LogUtils;
import combined.util.ModConfigUtil;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.discovery.ModResolutionException;

public class ManyModsPreLaunch implements PreLaunchEntrypoint{
	private static LogUtils LOG = new LogUtils("ManyModsPreLaunch");
	@Override
	public void onPreLaunch() {
		LOG.enter("onPreLaunch");
		try {
			ModConfigUtil.loadMods();
		} catch (IOException | ModResolutionException e) {
			LOG.warn("Problem loading mods");
			e.printStackTrace();
		}
		LOG.exit("onPreLaunch");
	}

}
