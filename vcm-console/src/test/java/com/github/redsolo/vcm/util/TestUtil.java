package com.github.redsolo.vcm.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class TestUtil {

	public static File getResourceFile(File root) throws IOException {
		return getResourceFile(root, "/Idler.vcm");
	}
	
	public static File getVcmxResourceFile(File root) throws IOException {
		return getResourceFile(root, "/Idler.vcmx");
	}
	
	public static File getResourceFile(File root, String string) throws IOException {
		File temp = new File(root, StringUtils.removeStart(string, "/"));
		FileUtils.copyURLToFile(TestUtil.class.getResource(string), temp);
		return temp;
	}
}
