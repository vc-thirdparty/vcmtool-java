package com.github.redsolo.vcm.commands;

import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "Verifies that the component is saved into a [1,0,0,0][0,1,0,0][0,0,1,0][0,0,0,1] position")
public class VerifyZeroPositionCommand extends AbstractVerifyModelCommand implements Command {
	private static Logger log = Logger.getLogger(VerifyZeroPositionCommand.class);

	private static double[] expectedLocation = new double[]{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
	
	@Override
	public String getName() {
		return "verify-zeropos";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		String locationString = ResourceDataParser.getLocation(model.getResourceData());
		if (locationString != null) {
			
			String[] strValues = StringUtils.split(locationString, ' ');		
			double[] location = new double[strValues.length];
			for (int i = 0; i < strValues.length; i++) {
				try {
					location[i] = Double.parseDouble(strValues[i]);
					location[i] = Math.round(location[i] * 10000.0) / 10000.0;
				} catch (NumberFormatException nfe) {
					log.debug(String.format("Could not parse double value '%s'", strValues[i]));
				} 
			}
			
			if (!ArrayUtils.isEquals(location,  expectedLocation)) {
				if (mainConfiguration.isVerbose()) {
					log.warn(String.format("Model '%s' is not positioned in a zero position, expected='%s' actual='%s'", 
						model.getFile(), joinArray(expectedLocation, ' '), joinArray(location, ' ')));
				} else {
					log.warn(String.format("Model '%s' is not positioned in a zero position", model.getFile()));					
				}
				setVerificationFailed(true);
				
			}
		} else {
			log.warn(String.format("Could not find location in model '%s'", 
					model.getFile()));
			setVerificationFailed(true);
		}
	}
	
	private String joinArray(double[] values, char separator) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			builder.append(values[i]);
			if (i < values.length - 1) {
				builder.append(separator);
			}
		}
		return builder.toString();
	}
}
