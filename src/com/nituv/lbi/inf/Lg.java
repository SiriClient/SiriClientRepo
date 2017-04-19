package com.nituv.lbi.inf;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Lg {
	static {
		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		File file = new File("config/log4j2.xml");
		 
		// this will force a reconfiguration
		context.setConfigLocation(file.toURI());
	}
	public static final Logger lgr = LogManager.getLogger("Lbi"); 
}
