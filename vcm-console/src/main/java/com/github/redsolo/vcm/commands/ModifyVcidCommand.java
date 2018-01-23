package com.github.redsolo.vcm.commands;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.ComponentModel;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.RawValue;

import net.lingala.zip4j.exception.ZipException;

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
			ComponentModel componentModel = model.getComponentModel();
			
			componentData.setVcid(newVcid);
			resourceData.getResource("Node").getResource("NodeClass").setValue("VCID", newVcid);
			resourceData.getResource("Node").setValue("VCID", new RawValue(newVcid));
			if (componentModel != null) {
				componentModel.setVcid(newVcid);
			}
			
			if (model.setResourceData(resourceData, false) 
					|| model.setComponentData(componentData, false)
					|| model.setComponentModel(componentModel, false)) {
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
