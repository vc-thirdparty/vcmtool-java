package com.github.redsolo.vcm.commands;

import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.ComponentModel;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "List or modifies key words in meta data (component.dat)")
public class ModifyKeyWordsCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ModifyKeyWordsCommand.class);

	@Parameter(description = "add keywords (comma separated list of key/value pairs [key=value])", names = { "-a", "--add" }) private String addKeywords;

	@Parameter(description = "remove keywords (comma separated list of keys)", names = { "-r", "--remove" }) private String removeKeywordKeys;

	@Override
	public String getName() {
		return "keywords";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ComponentData componentData = model.getComponentData();
		ComponentModel componentModel = model.getComponentModel();

		boolean mapHasChanged = false;
		if (getRemoveKeywords() != null) {
			for (String key : StringUtils.split(getRemoveKeywords(), ',')) {
				if (componentModel != null) {
					componentModel.removePropertyValue(key);
				}
				componentData.getKeywords().remove(key);
				mapHasChanged = true;
			}
		}
		if (getAddKeywords() != null) {
			for (String keyValue : StringUtils.split(getAddKeywords(), ',')) {
				String[] strings = StringUtils.split(keyValue, '=');
				componentData.getKeywords().put(strings[0], strings[1]);
				if (componentModel != null) {
					componentModel.addPropertyValue(strings[0], strings[1]);
				}
				mapHasChanged = true;
			}
		}
		if (mapHasChanged) {
			boolean wasChanged = model.setComponentData(componentData, false);
			wasChanged = model.setComponentModel(componentModel, false) || wasChanged;
			if (wasChanged && !skipRevisionUpdate) {
				model.stepRevision();
			}
		}
		log.info(String.format("%s: %s", model.getFile().getName(), componentData.getKeywords()));
	}

	public String getAddKeywords() {
		return addKeywords;
	}

	public void setAddKeywords(String addKeywords) {
		this.addKeywords = addKeywords;
	}

	public String getRemoveKeywords() {
		return removeKeywordKeys;
	}

	public void setRemoveKeywords(String removeKeywords) {
		this.removeKeywordKeys = removeKeywords;
	}
}
