package com.lvt4j.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class TProps {

	private Properties props = new Properties();
	private File propsFile;

	public TProps(String propsFilePath) {
		try {
			propsFile = new File(propsFilePath);
			if (!propsFile.exists()) {
				propsFile.createNewFile();
			}
			props.load(new FileInputStream(propsFile));
		} catch (Exception e) {
			TLog.e("On load props", e);
		}
	}
	public TProps(File propsFile) {
		try {
			this.propsFile = propsFile;
			if (!propsFile.exists()) {
				propsFile.createNewFile();
			}
			props.load(new FileInputStream(propsFile));
		} catch (Exception e) {
			TLog.e("On load props", e);
		}
	}
	public void setProp(String key, Object prop) {
		props.setProperty(key, String.valueOf(prop));
		try {
			props.store(new FileOutputStream(propsFile), "");
		} catch (Exception e) {
			TLog.e("On set prop", e);
		}
	}

	public <E> E getProp(String key, Class<E> cls) {
		try {
			return cls.getConstructor(String.class).newInstance(
					props.getProperty(key));
		} catch (Exception e) {
			TLog.e("On getProp", e);
			return null;
		}
	}

	public Boolean getBit(String key) {
		return getBit(key, false);
	}

	public Boolean getBit(String key, boolean defaultProp) {
		try {
			if (props.containsKey(key)) {
				return new Boolean(props.getProperty(key));
			} else {
				setProp(key, defaultProp);
			}
		} catch (Exception e) {
			TLog.e("On getBool", e);
		}
		return defaultProp;
	}

	public Short getShort(String key) {
		return getShort(key, (short) 0);
	}

	public Short getShort(String key, short defaultProp) {
		try {
			if (props.containsKey(key)) {
				return new Short(props.getProperty(key));
			}
		} catch (Exception e) {
			TLog.e("On getShort", e);
		}
		return defaultProp;
	}

	public Integer getInt(String key) {
		return getInt(key, 0);
	}

	public Integer getInt(String key, int defaultProp) {
		try {
			if (props.containsKey(key)) {
				return new Integer(props.getProperty(key));
			}
		} catch (Exception e) {
			TLog.e("On getInt", e);
		}
		return defaultProp;
	}

	public Long getLong(String key) {
		return getLong(key, 0);
	}

	public Long getLong(String key, long defaultProp) {
		try {
			if (props.containsKey(key)) {
				return new Long(props.getProperty(key));
			}
		} catch (Exception e) {
			TLog.e("On getLong", e);
		}
		return defaultProp;
	}

	public Double getDouble(String key) {
		return getDouble(key, 0);
	}

	public Double getDouble(String key, double defaultProp) {
		try {
			if (props.containsKey(key)) {
				return new Double(props.getProperty(key));
			}
		} catch (Exception e) {
			TLog.e("On getDouble", e);
		}
		return defaultProp;
	}

	public Float getFloat(String key) {
		return getFloat(key, 0);
	}

	public Float getFloat(String key, float defaultProp) {
		try {
			if (props.containsKey(key)) {
				return new Float(props.getProperty(key));
			}
		} catch (Exception e) {
			TLog.e("On getFloat", e);
		}
		return defaultProp;
	}

	public String getString(String key) {
		return getString(key, "");
	}

	public String getString(String key, String defaultProp) {
		try {
			if (props.containsKey(key)) {
				return props.getProperty(key);
			}
		} catch (Exception e) {
			TLog.e("On getString", e);
		}
		return defaultProp;
	}
}
