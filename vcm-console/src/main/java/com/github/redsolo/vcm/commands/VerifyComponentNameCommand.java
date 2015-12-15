package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "Verifies that component names matches a regex")
public class VerifyComponentNameCommand extends AbstractVerifyModelCommand implements Command {
    private static Logger log = Logger.getLogger(VerifyComponentNameCommand.class);
    
    @Override
    public String getName() {
        return "verify-compname";
    }

    @Parameter(description = "regular expression to use for verification", names = { "-r", "--regex" }) 
    private String regex = "^[\\w\\s\\d-_\\(\\)/]+$";
    private Pattern regexPattern;

    @Override
    protected void validateParameters(MainConfiguration mainConfiguration) {
        regexPattern = Pattern.compile(regex);
    }
    
    @Override
    protected void executeModel(Model model) throws IOException, ZipException {
        ModelResource modelResource = model.getResourceData().getResource("Node");
        String componentName = (String) modelResource.getValue("Name");
        if (!regexPattern.matcher(componentName).matches()) {
            log.warn(String.format("Invalid component name '%s' in model '%s'", 
                    componentName, model.getFile()));
            setVerificationFailed(true);
        }
    }
}
