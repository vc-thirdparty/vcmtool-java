package com.github.redsolo.vcm.commands;

import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;

import org.apache.log4j.Logger;

import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "Verifies property names does not start or end with space")
public class VerifyPropertyNamesCommand extends AbstractVerifyModelCommand implements Command {
	private static Logger log = Logger.getLogger(VerifyPropertyNamesCommand.class);

	@Override
	public String getName() {
		return "verify-propnames";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
		if (variables == null) {
			log.warn(String.format("VariableSpace node not found in file '%s'", model.getFile()));
		} else {
			for (ModelResource variable : variables.getResources()) {
				String variableName = (String) variable.getValue("Name");
				if (!variableName.trim().equals(variableName)) {

					log.warn(String.format("Invalid property name '%s' in model '%s'", 
							variableName, model.getFile()));
					setVerificationFailed(true);
				}
			}
		}
	}
}
