package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "Verifies property values")
public class VerifyPropertyValuesCommand  extends AbstractVerifyModelCommand {
	private static Logger log = Logger.getLogger(VerifyPropertyValuesCommand.class);

	private Pattern keyValuePattern = Pattern.compile("(.+)=(.*)"); 
	
	@Parameter(description = "properties to verify (comma separated key-value list)", names = { "-p", "--props" }) 
	private String verifyProperties;

	private HashMap<String, String> propNamesToVerify;

	@Override
	public String getName() {
		return "verify-propvalues";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		if (verifyProperties != null) {
			propNamesToVerify = new HashMap<String, String>();
			for (String keyValueString : StringUtils.split(verifyProperties, ",")) {
				Matcher matcher = keyValuePattern.matcher(keyValueString);
				if (matcher.matches() && matcher.groupCount() == 2) {
					propNamesToVerify.put(matcher.group(1).toLowerCase(), matcher.group(2));
				} else {
					throw new CommandExecutionException(2, String.format("Can not parse the name and value from '%s'", keyValueString));					
				}
			}
		}
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
		if (variables == null) {
			log.warn(String.format("VariableSpace node not found in file '%s'", model.getFile()));
		} else {
			if (propNamesToVerify != null) {
				for (ModelResource variable : variables.getResources()) {
					if (variable.getValue("Name") != null) {
						String variableName = variable.getValue("Name").toString().toLowerCase();
						if (propNamesToVerify.containsKey(variableName)) {
							String actualValue = variable.getValue("Value").toString();
							String expectedValue = propNamesToVerify.get(variableName);
							if (! expectedValue.equals(actualValue)) {
								log.warn(String.format("Invalid property value in model %s for property '%s', expected value='%s', actual value='%s'", 
										model.getFile(), variable.getValue("Name"), expectedValue, actualValue));
								setVerificationFailed(true);
							}
						}
					}
				}
			}
		}
	}

	public String getVerifyProperties() {
		return verifyProperties;
	}

	public void setVerifyProperties(String verifyProperties) {
		this.verifyProperties = verifyProperties;
	}
}
