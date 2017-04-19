package com.nituv.lbi.inf;

//import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

//import org.apache.commons.configuration.PropertiesConfiguration;

public class Config {
	public static PropertiesConfiguration config;
//	static {
//		setup();
//	}
	
	private static void setup()
	{
		try {
			config = new PropertiesConfiguration("config/lbi-config.properties");
		} catch (Exception ex) {
			Lg.lgr.error("error loading config file",ex);
		}
	}
	
	public static PropertiesConfiguration getConfig() {
		if (config==null) {
			setup();
		}
		return config;
	}
}
