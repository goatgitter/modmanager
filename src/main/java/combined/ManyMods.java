package combined;

import combined.util.LogUtils;
import net.fabricmc.api.ModInitializer;
/**
 * 
 * @author H1ppyChick & Torphedo 
 * @since 08/11/2020
 * 
 * See README.md
 */
public class ManyMods implements ModInitializer {
	private static LogUtils LOG = new LogUtils("ManyMods");
	
	@Override
	public void onInitialize() {
		LOG.enter("onInitialize");
		// No mod setup currently
		LOG.exit("onInitialize");
	}
}
