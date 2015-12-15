package com.github.redsolo.vcm.commands;

import java.io.IOException;

import org.apache.log4j.Logger;

import net.lingala.zip4j.exception.ZipException;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "List or modifies VCID (component.dat, component.rsc)")
public class ModifyVcidCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ModifyVcidCommand.class);

	@Parameter(description = "new vcid", names = { "-n"}) 
	private String newVcid;

	@Override
	public String getName() {
		return "vcid";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		if (newVcid != null) {
			ComponentData componentData = model.getComponentData();
			ModelResource resourceData = model.getResourceData();
			
			componentData.setVcid(newVcid);
			resourceData.getResource("Node").getResource("NodeClass").setValue("VCID", newVcid);

			if (model.setResourceData(resourceData, false) && model.setComponentData(componentData, false)) {
			    if (!skipRevisionUpdate) {
			        model.stepRevision();
			    }
			}
		}
		log.info(String.format("%s - '%s'", model.getFile(), model.getComponentData().getVcId()));
	}

	public void setNewVcid(String newVcid) {
		this.newVcid = newVcid;
	}
}
