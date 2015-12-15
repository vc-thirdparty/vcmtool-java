package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "List or modifies tags in meta data (component.dat)")
public class ModifyTagsCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ModifyTagsCommand.class);

	@Parameter(description = "add tags (comma separated list)", names = { "-a", "--add" }) 
	private String addTags;

	@Parameter(description = "remove tags (comma separated list)", names = { "-r", "--remove" }) 
	private String removeTags;

	@Parameter(description = "clear ALL tags before adding new", names = {"-c", "--clear"})
	private boolean clearTags = false;

	@Override
	public String getName() {
		return "tags";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ComponentData componentData = model.getComponentData();

		Set<String> tags = new LinkedHashSet<String>(Arrays.asList(componentData.getTags()));
		if (removeTags != null) {
			for (String tag : StringUtils.split(removeTags, ',')) {
				tags.remove(tag);
			}
		}
		if (clearTags) {
			tags.clear();
		}
		if (addTags != null) {
			for (String tag : StringUtils.split(addTags, ',')) {
				tags.add(tag);
			}
		}
		String[] newTagsArray = tags.toArray(new String[tags.size()]);
		if (!Arrays.equals(newTagsArray, componentData.getTags())) {
			componentData.setTags(newTagsArray);
			model.setComponentData(componentData, !skipRevisionUpdate);
		}
		log.info(String.format("%s: %s", model.getFile().getName(), StringUtils.join(componentData.getTags(), ",")));
	}

	public void setAddTags(String addTags) {
		this.addTags = addTags;
	}

	public void setRemoveTags(String removeTags) {
		this.removeTags = removeTags;
	}

	public void setClearTags(boolean clearTags) {
		this.clearTags = clearTags;
	}
}
