package com.pay.system.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** 配置文件资源读取 */
public class ConfigUtil {
	private String initConfigFileName = "init.properties";

	/** 配置文件的相对位置 */
	private String filePath = File.separator + "conf" + File.separator;

	private static ConfigUtil instanse;

	private Map<String, String> properties = new HashMap<String, String>();

	public synchronized static ConfigUtil getInstance() {
		if (instanse == null) {
			instanse = new ConfigUtil();
		}
		return instanse;
	}

	private ConfigUtil() {
		try {
			readFile(initConfigFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 获取当前程序的根目录 */
	public String getRootUrl() {
		URL url = ConfigUtil.class.getResource("/");
		if (url == null) {
			return System.getProperty("user.dir") + File.separator;
		}
		String path = url.getFile();
		String osName = System.getProperty("os.name");
		// windows路径特殊处理掉开头的/
		if (osName.startsWith("win") || osName.startsWith("Win")) {
			if (path.charAt(0) == '/') {
				path = path.substring(1);
			}
		}
		return path;
	}

	/** 获取web项目的根目录 */
	public String getWebRootUrl() {
		String url = getRootUrl().replace("classes", "").replace("WEB-INF", "");
		url = url.substring(0, url.length() - 2);
		return url;
	}

	/** 读取文件 */
	public File readFile(String configFileName) throws Exception {
		// String fileName = ConfigUtil.class.getResource(filePath).getFile();
		URL url = ConfigUtil.class.getResource("/" + configFileName);
		String fileUrl = null;
		if (url == null || url.toURI().getPath() == null) {
			url = ConfigUtil.class.getResource(filePath + configFileName);
			if (url == null || url.toURI().getPath() == null) {
				fileUrl = System.getProperty("user.dir") + filePath
						+ configFileName;
			}
			else {
				fileUrl = url.toURI().getPath();
			}
		}
		else {
			fileUrl = url.toURI().getPath();
		}
		File file = new File(fileUrl);
		FileInputStream input = new FileInputStream(file);
		Properties p = new Properties();
		p.load(input);
		Set<String> pNames = p.stringPropertyNames();
		if (pNames == null || pNames.size() == 0) {
			return file;
		}
		for (String pname : pNames) {
			properties.put(pname, p.getProperty(pname));
		}
		return file;
	}

	/** 获取参数值 */
	public String getStringValue(String key, String defaultValue) {
		String value = properties.get(key);
		if (null == value || value == null || value.trim().equals("")) {
			return defaultValue;
		}
		return value.trim();
	}

	/** 获取参数值 */
	public String getStringValue(String key) {
		return getStringValue(key, "");
	}
}
