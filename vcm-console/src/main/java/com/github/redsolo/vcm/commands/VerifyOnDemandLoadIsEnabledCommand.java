package com.github.redsolo.vcm.commands;

import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import net.lingala.zip4j.exception.ZipException;
import org.apache.log4j.Logger;

import java.io.IOException;

@Parameters(commandDescription = "Verifies that all geo Features have OnDemandLoad set to 1")
public class VerifyOnDemandLoadIsEnabledCommand extends AbstractVerifyModelCommand implements Command {
    private static Logger log = Logger.getLogger(VerifyOnDemandLoadIsEnabledCommand.class);

    @Override
    protected void executeModel(Model model) throws IOException, ZipException {
        ResourceDataParser.getFeatures(model.getResourceData(), "rGeoFeature").stream().filter(f -> !f.isOnDemandLoad()).forEach(f -> {
            log.warn(String.format("Feature '%s' in file '%s' does not have OnDemandLoad = 1", f.getName(), model.getFile()));
            setVerificationFailed(true);
        });
    }

    @Override
    public String getName() {
        return "verify-ondemandload";
    }
}
