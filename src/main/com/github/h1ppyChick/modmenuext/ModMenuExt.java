package combined;

import combined.util.Log;
import net.fabricmc.api.ModInitializer;
import net.minecraft.text.TranslatableText;
/**
 * 
 * @author H1ppyChick
 * @since 08/11/2020
 * 
 * See README.md
 */
public class ModMenuExt implements ModInitializer {
	private static Log LOG = new Log("ModMenuExt");
	// Constants
	public static final String MOD_ID = "modmenuext";
	public static final String LOAD_JAR_DIR = "loadedJars/";
	public static final String MM_PARENT_KEY = "modmenu:parent";
	public static final TranslatableText TEXT_SUCCESS = new TranslatableText(MOD_ID + ".success");
	public static final TranslatableText TEXT_ERROR = new TranslatableText(MOD_ID + ".error");
	public static final TranslatableText TEXT_RESTART = new TranslatableText(MOD_ID + ".restart");
	
	@Override
	public void onInitialize() {
		LOG.enter("onInitialize");
		// No mod setup currently
		LOG.exit("onInitialize");
	}
}
