package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.model.Property;

@Parameters(commandDescription = "List or modifies properties (component.rsc)")
public class ModifyPropertiesCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ModifyPropertiesCommand.class);

	private Pattern keyValuePattern = Pattern.compile("(.+)=(.*)"); 
	
	@Parameter(description = "remove properties (comma separated list)", names = { "-r", "--remove" }) 
	private String removeProps;

    @Parameter(description = "update properties data (comma separated list with key=value pairs)", names = { "-u", "--update" }) 
    private String updateProperties;
    
    @Parameter(description = "add properties data (comma separated list with key=value pairs); if property exists it will be updated", names = { "-a", "--add" }) 
    private String addProperties;

    @Parameter(description = "type when adding new properties (string, double, bool, int)", names = { "-t", "--type" }) 
    private String propertyType = "string";

	@Parameter(description = "list properties", names = { "-l", "--list" }) 
	private boolean listProperties;
	@Parameter(description = "list properties with all information such as range, set", names = { "-ll", "--list-all-info" }) 
    private boolean listAllMetainformation;

    private Map<String, String> propNamesToUpdate;
    private Map<String, String> propNamesToAdd;

	private List<String> propNamesToRemove;

	@Override
	public String getName() {
		return "props";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		if (removeProps != null) {
			propNamesToRemove = new ArrayList<String>();
			for (String string : Arrays.asList(StringUtils.split(removeProps, ","))) {
				propNamesToRemove.add(string.toLowerCase());
			}
		}
        
        if (updateProperties != null) {
            propNamesToUpdate = new HashMap<String, String>();
            for (String keyValueString : StringUtils.split(updateProperties, ",")) {
                Matcher matcher = keyValuePattern.matcher(keyValueString);
                if (matcher.matches() && matcher.groupCount() == 2) {
                    propNamesToUpdate.put(matcher.group(1), matcher.group(2));
                } else {
                    throw new CommandExecutionException(2, String.format("Can not parse the name and value from '%s'", keyValueString));                    
                }
            }
        }
        
        if (addProperties != null) {
            propNamesToAdd = new HashMap<String, String>();
            for (String keyValueString : StringUtils.split(addProperties, ",")) {
                Matcher matcher = keyValuePattern.matcher(keyValueString);
                if (matcher.matches() && matcher.groupCount() == 2) {
                    propNamesToAdd.put(matcher.group(1), matcher.group(2));
                } else {
                    throw new CommandExecutionException(2, String.format("Can not parse the name and value from '%s'", keyValueString));                    
                }
            }
        }
		
		if (propertyType != null) {
		    getValueType(propertyType);
		}
	}
	
	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		listProperties = listProperties || (propNamesToRemove == null && propNamesToUpdate == null && propNamesToAdd == null);
		ModelResource resourceData = model.getResourceData();
		ModelResource variables = ResourceDataParser.getVariables(resourceData);
		if (variables == null) {
			log.warn(String.format("VariableSpace node not found in file '%s'", model.getFile()));
		} else {

			if (propNamesToRemove != null) {
				for (ModelResource variable : variables.getResources()) {
					if (variable.getValue("Name") != null) {
						String name = variable.getValue("Name").toString().toLowerCase();
						if (propNamesToRemove.contains(name)) {
							variables.removeResource(variable);
						}
					}
				}
			}
            
            if (propNamesToUpdate != null) {                
                for (String propertyName : propNamesToUpdate.keySet()) {
                    ModelResource variable = ResourceDataParser.getNamedResource(variables, propertyName);
                    if (variable != null) {
                        variable.setValue("Value", getValueInCorrectType(model, variable, propNamesToUpdate.get(propertyName)));
                    }
                }
            }
            
            if (propNamesToAdd != null) {                
                for (String propertyName : propNamesToAdd.keySet()) {
                    ModelResource variable = ResourceDataParser.getNamedResource(variables, propertyName);
                    if (variable == null) {
                        variable = new ModelResource("Variable", String.format("rTVariable<%s>", getValueType(propertyType)));
                        variable.setValue("Name", propertyName);
                        variable.setValue("Group", getNextGroup(variables));
                        ModelResource settingsResource = new ModelResource("Settings");
                        variable.addResource(settingsResource);
                        variables.addResource(variable);
                    }
                    variable.setValue("Value", getValueInCorrectType(model, variable, propNamesToAdd.get(propertyName)));
                }
            }
		}
		if (listProperties) {
			log.info(model.getFile());
			for (ModelResource variable : variables.getResources()) {
			    Property property = new Property(variable);
			    StringBuilder builder = new StringBuilder();
			    if (listAllMetainformation) {
	                builder.append(String.format("%s; ", property.getName()));
	                String[] set = property.getSet();
                    if (set.length > 0){
                        builder.append(String.format("[%s]", StringUtils.join(set,',')));
	                }
	                String[] range = property.getRange();
	                if (range.length > 0) {
    	                builder.append(String.format("; %s..%s", range[0], range[1]));               
    	            } else {
                        builder.append("; ");
    	            }
    	            builder.append(String.format("; '%s'", variable.getValue("Value")));
			    } else {
	                builder.append(String.format("%s=%s", property.getName(), property.getValue()));			        
			    }
                log.info(builder.toString());
			}
		}
		model.setResourceData(resourceData, !skipRevisionUpdate);
	}

	private int getNextGroup(ModelResource variables) {
	    int highestGroupIndex = 0;
	    for (ModelResource variable : variables.getResources()) {
	        Object groupValue = variable.getValue("Group");
	        if (groupValue != null) {
	            highestGroupIndex = Math.max(highestGroupIndex, Integer.parseInt(groupValue.toString()));
	        }
	    }

        return highestGroupIndex + 1;
    }

    public void setRemoveProps(String removeProps) {
		this.removeProps = removeProps;
	}

	public void setUpdateProps(String updateProperties) {
		this.updateProperties = updateProperties;
	}

    public void setAddProps(String addProperties) {
        this.addProperties = addProperties;
    }
	
	private String getValueType(String type) {
        if (type.equalsIgnoreCase("string")) {
            return "rString";
        }
        if (type.equalsIgnoreCase("double")) {
            return "rDouble";
        }
        if (type.equalsIgnoreCase("bool")) {
            return "rBool";
        }
        if (type.equalsIgnoreCase("int")) {
            return "rInt";
        }
	    throw new CommandExecutionException(5, "Unknown property type");
	}
	
	private Object getValueInCorrectType(Model model, ModelResource variable, String value) throws ZipException, IOException {
        if (variable.getName().contains("rBool")) {
            return Boolean.parseBoolean(value) ? 1 : 0;
        }
        if (variable.getName().contains("rInt")) {
            return Integer.parseInt(value);
        }
        if (variable.getName().contains("rDouble")) {
            return Double.parseDouble(value);
        }
        return replaceData(model, value);
	}
	
	private String replaceData(Model model, String value) throws ZipException, IOException {
        if (value.contains("${component.VcId}")) {
            value = StringUtils.replace(value, "${component.VcId}", model.getComponentData().getVcId());
        }
        if (value.contains("${component.DetailedRevision}")) {
            value = StringUtils.replace(value, "${component.DetailedRevision}", model.getComponentData().getDetailedRevision());
        }
        if (value.contains("${component.Revision}")) {
            value = StringUtils.replace(value, "${component.Revision}", Long.toString(model.getComponentData().getRevision()));
        }
	    return value;
	}
}
