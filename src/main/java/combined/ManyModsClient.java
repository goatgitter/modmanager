package combined;

import combined.util.Log;
import net.fabricmc.api.ClientModInitializer;

public class ManyModsClient implements ClientModInitializer{
	// Instance Variables
	Log LOG = new Log("ManyModsClient");
	@Override
	public void onInitializeClient() {
		LOG.enter("onInitializeClient");
		LOG.exit("onInitializeClient");
	}
}
