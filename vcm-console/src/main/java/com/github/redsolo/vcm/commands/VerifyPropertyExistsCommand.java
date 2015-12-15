package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "Verifies that certain properties exists in components")
public class VerifyPropertyExistsCommand  extends AbstractVerifyModelCommand {
	private static Logger log = Logger.getLogger(VerifyPropertyExistsCommand.class);

	@Parameter(description = "properties to verify (comma separated list)", names = { "-p", "--props" }) 
	private String verifyProperties;

	private Map<String, String> propNamesToVerify;

	@Override
	public String getName() {
		return "verify-propexists";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		if (verifyProperties != null) {
			propNamesToVerify = new HashMap<String, String>();
			for (String keyString : StringUtils.split(verifyProperties, ",")) {
				propNamesToVerify.put(keyString.toLowerCase(), keyString);
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
			    HashSet<String> missingProperties = new HashSet<String>(propNamesToVerify.keySet());
                missingProperties.removeAll(getPropertyNames(variables));
			    
				for (String propertyName : missingProperties) {
                    log.warn(String.format("Property '%s' does not exist in model %s", 
                            propNamesToVerify.get(propertyName), model.getFile()));
                    setVerificationFailed(true);        
				}
			}
		}
	}
	
	private Set<String> getPropertyNames(ModelResource variables)  {
	    HashSet<String> set = new HashSet<String>();
        for (ModelResource variable : variables.getResources()) {
            if (variable.getValue("Name") != null) {
                set.add(variable.getValue("Name").toString().toLowerCase());
            }
        }
        return set;
	}

	public String getVerifyProperties() {
		return verifyProperties;
	}

	public void setVerifyProperties(String verifyProperties) {
		this.verifyProperties = verifyProperties;
	}
}
