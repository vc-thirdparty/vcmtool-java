package com.github.redsolo.vcm.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "Creates a property (component.rsc)")
public class CreatePropertyCommand extends AbstractModelCollectionCommand {

	@Parameter(description = "name", names = {"-n", "--name"}, required = true)
	private String propertyName;
	@Parameter(description = "type (string, int, double, bool)", names = {"-t", "--type"}, required = true)
	private String propertyType;
	@Parameter(description = "value", names = {"-v", "--value"}, required = true)
	private String propertyValue;
	@Parameter(description = "allowed values", names = {"-a", "--allowed-values"})
	private String propertyValues;
	@Parameter(description = "settings that are comma separeted[EDITABLE_DISCONNECTED, EDITABLE_CONNECTED, etc]", names = {"-s", "--settings"})
	private String propertySetting;
	@Parameter(description = "group index (default is last variable + 1)", names = {"-g", "--group"})
	private int group;
	@Parameter(description = "double quantity (unclear what this means)", names = {"-q", "--quantity"})
	private String quantity;
	@Parameter(description = "double magnitude (unclear what this means)", names = {"-m", "--magnitude"})
	private int magnitude;
	@Parameter(description = "replace existing property (default is skipping creation)", names = {"--replace"})
	private boolean replaceExisting;

	private String[] allowedValues;
	private String[] settings;

	@Override
	public String getName() {
		return "create-property";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		super.validateParameters(mainConfiguration);
		if (!StringUtils.isEmpty(propertySetting)) {
			settings = StringUtils.split(propertySetting, ',');
		}
		if (!StringUtils.isEmpty(propertyValues)) {
			allowedValues = StringUtils.split(propertyValues, ',');
		}
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ModelResource resourceData = model.getResourceData();
		ModelResource variables = ResourceDataParser.getVariables(resourceData);

		Optional<ModelResource> existingVariable = variables.getResources().stream().filter(r -> r.getValue("Name").equals(propertyName)).findFirst();
		if (existingVariable.isPresent()) {
			if (replaceExisting) {
				variables.removeResource(existingVariable.get());
			}
			return;
		}

		ModelResource newVariable = new ModelResource("Variable", getVariableType());
		newVariable.setValue("Name", propertyName);
		newVariable.setValue("Value", getValueInCorrectType(newVariable, propertyValue));
		if (quantity != null) {
			newVariable.setValue("Quantity", quantity);
			newVariable.setValue("Magnitude", magnitude);
		}

		if (group == 0) {
			group = getNextGroup(variables);
		}
		newVariable.setValue("Group", group);

		ModelResource settingsResource = new ModelResource("Settings");
		if (settings != null && settings.length > 0) {
			for (String setting : settings) {
				settingsResource.setValue(setting, null);
			}
		}
		newVariable.addResource(settingsResource);

		if (isStepVariable()) {
			ModelResource steplist = new ModelResource("StepList");
			for (String value : allowedValues) {
				ModelResource step = new ModelResource("Step");
				step.setValue("Value", getValueInCorrectType(newVariable, value));
				step.setValue("Enabled", 1);
				steplist.addResource(step);
			}
			newVariable.addResource(steplist);
		}
		variables.addResource(newVariable);
		model.setResourceData(resourceData, !skipRevisionUpdate);
	}

	private String getVariableType() {
		String valueType = getValueType(propertyType);
		if (isStepVariable()) {
			return String.format("rTStepVariable<%s>", valueType);
		} else {
			return String.format("rTVariable<%s>", valueType);
		}
	}

	private boolean isStepVariable() {
		return allowedValues != null && allowedValues.length > 0;
	}

	void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	void setPropertyValues(String[] propertyValues) {
		allowedValues = propertyValues;
	}

	public void setPropertySetting(String[] propertySetting) {
		settings = propertySetting;
	}
}
