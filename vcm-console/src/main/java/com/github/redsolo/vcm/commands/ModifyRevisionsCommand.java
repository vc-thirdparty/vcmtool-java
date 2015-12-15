package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import net.lingala.zip4j.exception.ZipException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "List or modifies detailed revision (component.dat)")
public class ModifyRevisionsCommand extends AbstractModelCollectionCommand implements Command {
	private static Logger log = Logger.getLogger(ModifyRevisionsCommand.class);
	private static final Pattern REVISION_PATTERN = Pattern.compile("([\\d*]+).([\\d*]+).([\\d*]+)");

	@Parameter(description = "new revision number ('dddd.d.d'), a '*' is replaced with old value", names = { "-r", "--revision" }) 
	private String newRevision;
	private String[] splitNewRevision;

	@Override
	public String getName() {
		return "revisions";
	}
	
	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		if (newRevision != null) {
			Matcher matcher = REVISION_PATTERN.matcher(newRevision);
			if (matcher.matches())  {
				splitNewRevision = new String[3];
				for (int i = 0; i < 3; i++) {
					if (!matcher.group(i + 1).equals("*")) {
						splitNewRevision[i] = matcher.group(i + 1);
					}
				}
			} else {
				throw new ParameterException("The new revision must contain 3 digits");
			}
		}
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ComponentData componentData = model.getComponentData();
		if (newRevision != null) {
			String newRevision = getNewRevision((String)componentData.getValues().get("DetailedRevision")); 
			componentData.getValues().put("DetailedRevision", newRevision);
			model.setComponentData(componentData, false);
		}
		log.info(String.format("%s: %s", model.getFile(), componentData.getValues().get("DetailedRevision")));
	}

	private String getNewRevision(String oldDetailedRevision) {
		String[] oldRevisions = StringUtils.split(oldDetailedRevision, '.');
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			builder.append(splitNewRevision[i] == null ? oldRevisions[i] : splitNewRevision[i]);
			builder.append('.');
		}
		builder.append(oldRevisions[3]);
		return builder.toString();
	}

	public void setRevision(String revision) {
		this.newRevision = revision;
	}
}
