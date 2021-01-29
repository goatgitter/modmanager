package com.github.h1ppyChick.modmanager.util;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.h1ppyChick.modmanager.ModManager;

/**
 * @author h1ppyChick
 * @since 08/11/2020
 *  This is a utility class for generating log messages to the log file.
 *  
 *  Psst ....
 *  The standard log file is "{yourUserName}\AppData\Roaming\.minecraft\logs\latest.log
 *  The debug log file is "{yourUserName}\AppData\Roaming\.minecraft\logs\debug.log
 *  	OR {yourModName}\run\logs\debug.log
 */
public class Log {
	// Constants
	private static final String ENTER_MSG ="Entering ";
	private static final String EXIT_MSG ="Exiting ";
	// Instance Variables (fields)
	private static final Logger LOG = LogManager.getFormatterLogger();
	private String prefixName = getClass().getSimpleName();
	// Constructors
	public Log()
	{
		
	}
	public Log(String prefixName)
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
		return "[" + ModManager.MOD_ID + "->" + prefixName + "] ";
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
	
	public void trace(String msg)
	{
		LOG.trace(getLogMsg(msg));
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
		LOG.trace(getMethodMsg(methodName) + ENTER_MSG + " method.");
	}
	
	public void enter(String methodName, String msg) {
		LOG.trace(getMethodMsg(methodName) + ENTER_MSG + " method -> " + msg);
	}
	
	public void enterClass()
	{
		LOG.trace(getLogMsg(ENTER_MSG + " Class"));
	}
	
	public void enterClass(String msg)
	{
		LOG.trace(getLogMsg(ENTER_MSG + " Class -> "+ msg));
	}
	
	public void exit(String methodName) {
		LOG.trace(getMethodMsg(methodName) + EXIT_MSG);
	}
	
	public void exit(String methodName, String msg) {
		LOG.trace(getMethodMsg(methodName) + EXIT_MSG + msg);
	}
	
	public void exitClass()
	{
		LOG.trace(getLogMsg(EXIT_MSG));
	}
	
	public void exitClass(String msg)
	{
		LOG.trace(getLogMsg(EXIT_MSG + msg));
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
