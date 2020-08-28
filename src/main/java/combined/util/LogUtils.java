package combined.util;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author h1ppyChick
 *  This is a utility class for generating log messages to the log file.
 *  
 *  Psst ....
 *  The standard log file is "{yourUserName}\AppData\Roaming\.minecraft\logs\latest.log
 *  The debug log file is "{yourUserName}\AppData\Roaming\.minecraft\logs\debug.log
 *  	OR {yourModName}\run\logs\debug.log
 */
public class LogUtils {
	// Constants
	private static final String ENTER_MSG ="Entering ";
	private static final String EXIT_MSG ="Exiting ";
	public static final String MOD_ID = "manymods";
	// Instance Variables (fields)
	private static final Logger LOG = LogManager.getFormatterLogger();
	private String prefixName = getClass().getSimpleName();
	// Constructors
	public LogUtils()
	{
		
	}
	public LogUtils(String prefixName)
	{
		setPrefixName(prefixName);
		enterClass();
	}
	
	// Methods
	public void setPrefixName(String name)
	{
		prefixName = name;
	}
	
	public String getLogMsg(String msg)
	{
		return getPrefixMsg() + msg;
	}
	
	public String getPrefixMsg()
	{
		return "[" + MOD_ID + "->" + prefixName + "] ";
	}
	
	public void info(String msg)
	{
		LOG.info(getLogMsg(msg));
	}
	public void debug(String msg)
	{
		LOG.debug(getLogMsg(msg));
	}
	
	public void error(String msg)
	{
		LOG.error(getLogMsg(msg));
	}
	
	public void warn(String msg)
	{
		LOG.warn(getLogMsg(msg));
	}
	
	public Logger getLog()
	{
		return LOG;
	}
	
	public String getMethodMsg(String methodName)
	{
		return getLogMsg("- [" + methodName + "] : ");
	}
	
	public void enter(String methodName) {
		LOG.debug(getMethodMsg(methodName) + ENTER_MSG + " method.");
	}
	
	public void enter(String methodName, String msg) {
		LOG.debug(getMethodMsg(methodName) + ENTER_MSG + " method -> " + msg);
	}
	
	public void enterClass()
	{
		LOG.debug(getLogMsg(ENTER_MSG + " Class"));
	}
	
	public void enterClass(String msg)
	{
		LOG.debug(getLogMsg(ENTER_MSG + " Class -> "+ msg));
	}
	
	public void exit(String methodName) {
		LOG.debug(getMethodMsg(methodName) + EXIT_MSG);
	}
	
	public void exit(String methodName, String msg) {
		LOG.debug(getMethodMsg(methodName) + EXIT_MSG + msg);
	}
	
	public void exitClass()
	{
		LOG.debug(getLogMsg(EXIT_MSG));
	}
	
	public void exitClass(String msg)
	{
		LOG.debug(getLogMsg(EXIT_MSG + msg));
	}
	
	
	/**
	 * This is really helpful when you are trying to figure out the field descriptors
	 * for mixin classes.
	 * @param classType - The type you want to know stuff about.
	 */
	public void logClassMethodInfo(Class<?> classType)
	{
		for(Method m : classType.getMethods())
		  {
			  
			  LOG.info("methodName =>" + m.getName());
			  
			  int paramNum = 0;
			  for(Class<?> cls : m.getParameterTypes())
			  {
				  paramNum ++;
				  LOG.info("    paramNum =>" + paramNum);
				  LOG.info("    getTypeName =>" + cls.getTypeName());
				  LOG.info("    getName => "+ cls.getName());
				  LOG.info("    getCannocialName => " + cls.getCanonicalName());
			  }
		  }
	}
}
