/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */
package io.github.zeroaicy.aide.utils;
import com.aide.ui.util.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import io.github.zeroaicy.util.IOUtils;

public class PropertiesConfiguration extends Configuration<PropertiesConfiguration> {

	private static PropertiesConfiguration singleton;

	/**
	 * 单例
	 */
	public static synchronized PropertiesConfiguration getSingleton() {
		if (singleton == null) {
			singleton = new PropertiesConfiguration(true);
		}
		return singleton;
	}

	@Override
	public PropertiesConfiguration makeConfiguration(String propertiesPath) {
		return new PropertiesConfiguration(propertiesPath);
	}

	final boolean isSingleton;
	public PropertiesConfiguration(boolean isSingleton) {
		this.isSingleton = isSingleton;
		this.properties = null;
	}


	public final Properties properties;
	public PropertiesConfiguration(String propertiesPath) {
		this.isSingleton = false;
		this.properties = new Properties();

		File propertiesFile = new File(propertiesPath);
		if( !propertiesFile.isFile()){
			return;
		}
		

		FileInputStream propertiesFileInputStream = null;
		try {
			propertiesFileInputStream = new FileInputStream(propertiesFile);
			this.properties.load(propertiesFileInputStream);
		}
		catch (Throwable  e) {
			
		}finally{
			IOUtils.close(propertiesFileInputStream);
		}
	}


	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
	}
}
