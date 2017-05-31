package com.github.redsolo.vcm.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FilenameUtils;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;

public class VcmFileData {
	private final File file;
	private final ComponentData componentData;
	private Model model;
	private final String modelType;
	private String relativePath;
    private long lastModifiedTime;

	public VcmFileData(File file, String relativePath) throws IOException, ZipException {
		this.file = file;
		this.relativePath = relativePath;
		model = new Model(file);
		componentData = model.getComponentData();
		lastModifiedTime = model.getLastModifiedTime(Model.COMPONENT_DAT);
		
        if (model.isComponent()) {
            modelType = "Component";
        } else if (model.isLayout()) {
            modelType = "Layout";
        } else {        
            throw new UnsupportedOperationException("Unkonwn model type");
        }
	}
	public Model getModel() {
		return model;
	}
	public File getFile() {
		return file;
	}
	public String getName() {
		return file.getName();
	}
	public String getThumbnailName() {
		return FilenameUtils.removeExtension(file.getName()) + ".png";
	}
	public ComponentData getComponentData() {
		return componentData;
	}
	public long getFileSize() {
		return file.length();
	}
	public String getFileSizeStr() {
		boolean si = true;
		long bytes = file.length();
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	public long getLastModified() {	    
		return lastModifiedTime;
	}
	public String getLastModifiedStr() {
		return String.format("%tF %<tT", new Date(lastModifiedTime));
	}
	public String getRelativePath() {
		return relativePath;
	}
	public String getModelType() {
	    return modelType;
	}
	public boolean getIsDeprecated() {
	    return Arrays.stream(componentData.getTags()).anyMatch(s -> s.equalsIgnoreCase("IsDeprecated")) ||
	    		"True".equalsIgnoreCase(componentData.getKeyword("IsDeprecated")); 
	}
}
