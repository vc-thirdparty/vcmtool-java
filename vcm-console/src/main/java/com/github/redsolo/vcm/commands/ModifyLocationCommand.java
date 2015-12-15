package com.github.redsolo.vcm.commands;

import java.io.IOException;

import org.apache.log4j.Logger;

import net.lingala.zip4j.exception.ZipException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.RawValue;

@Parameters(commandDescription = "List or modifies location of model")
public class ModifyLocationCommand extends AbstractModelCollectionCommand {
    private static Logger log = Logger.getLogger(ModifyLocationCommand.class);

    @Override
    public String getName() {
        return "location";
    }
    
    @Parameter(description = "Z Position", names = { "-z" }) 
    private int positionZ = 0;
    
    @Parameter(description = "X Position", names = { "-x" }) 
    private int positionX = 0;
    
    @Parameter(description = "Y Position", names = { "-y" }) 
    private int positionY = 0;

    @Parameter(description = "Location", names = { "-l", "--location" }) 
    private String location;
    
    @Parameter(description = "Dry run", names = {"-d", "--dryRun"})
    private boolean dryRun = false;
    
    @Parameter(description = "List existing locations", names = {"--list"})
    private boolean listLocations = false;


    @Override
    protected void validateParameters(MainConfiguration mainConfiguration) {
        super.validateParameters(mainConfiguration);
        if (location == null) {
            location = String.format("1 0 0 0 0 1 0 0 0 0 1 0 %d %d %d 1", positionX, positionY, positionZ);
        }
        log.debug(String.format("Command will set location to '%s'", location));
    }    

    @Override
    protected void executeModel(Model model) throws IOException, ZipException {
        ModelResource resourceData = model.getResourceData();
        ModelResource locationResource = ResourceDataParser.getLocationResource(resourceData);
        if (locationResource == null) {
            throw new CommandExecutionException(5, String.format("Location could not be found in '%s'", model.getFile()));
        }
        String actualLocation = locationResource.getValue("Location").toString();
        if (!listLocations) {
            if (!actualLocation.equals(location)) {
                if (!dryRun) {
                    locationResource.setValue("Location", new RawValue(location));
                    model.setResourceData(resourceData, !skipRevisionUpdate);
                }
                actualLocation = location;
            }
        }
        log.info(String.format("%s:%s", model.getFile(), actualLocation));
    }

    public void setZPosition(int z) {
        positionZ = z;
    }
    public void setXPosition(int x) {
        positionX = x;
    }

    public void setLocation(String string) {
        location = string;
    }
}
