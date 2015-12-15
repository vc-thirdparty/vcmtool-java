package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.ToolManager;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.model.Property;

import net.lingala.zip4j.exception.ZipException;

@Parameters(commandDescription = "Greps data from component.dat or resource.dat")
public class GrepDataCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ModifyItemsCommand.class);
	
	private ToolManager velocityToolManager;
	private VelocityEngine velocityEngine;
	
	@Parameter(description = "data string; variables = '$var[\"Visible\"]', values = '$value[\"VcId\"]', notes = '$note[\"InitList\"]', items = '$item[\"Name\"], tags, tag = '$tag[\"Name\"]', keyword = '$keyword[\"Manufacturer\"] - Add ! after $ to make something optional", names = { "-g", "--grep" }) 
	private String grepData;

    @Parameter(description = "Separator to use when displaying multiple values", names = { "-ls", "--list-seperator" }) 
    private char separator = ',';
    
    @Parameter(description = "Removes duplicate lines", names={"-u", "--unique"})
    private boolean displayOnlyUnique;

    private ArrayList<String> grepOutput;
    
	@Override
	public String getName() {
		return "grep";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		log.trace(String.format("Evaluating %s", model.getFile()));
		StringWriter writer = new StringWriter();
		VelocityContext context = new VelocityContext(velocityToolManager.createContext());
        context.put("var", getVariables(ResourceDataParser.getVariables(model.getResourceData())));
		context.put("note", getNotes(ResourceDataParser.getNotes(model.getResourceData())));
		context.put("value", model.getComponentData().getValues());
		context.put("item", model.getComponentData().getItems());
        context.put("keyword", model.getComponentData().getKeywords());
        context.put("tags", StringUtils.join(model.getComponentData().getTags(), ","));
        context.put("tag", model.getComponentData().getTags());
		context.put("file", model.getFile());
		try {
			velocityEngine.evaluate(context, writer, "vcm", grepData);
            String string = writer.toString();
			grepOutput.add(writer.toString());
			if (!StringUtils.isEmpty(string) && !displayOnlyUnique) {
			    log.info(string);			
			}
		} catch (VelocityException ve) {
			log.debug(String.format("Exception thrown for %s, '%s'", model.getFile(), ve.getMessage()));
		}
    }

    @Override
    protected void postProcess() {
        if (displayOnlyUnique) {
            grepOutput.stream().distinct().filter(s -> s.length() > 0).forEach(s -> log.info(s));
        }
    }
    @Override
    protected void preProcess() {
        grepOutput = new ArrayList<String>();
    }
    
    private Map<String, Object> getVariables(ModelResource variables) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (ModelResource variable : variables.getResources()) {
            if (variable.getValue("Value") != null) {
                map.put(variable.getValue("Name").toString(), new Property(variable));               
            }
        }
        return map;
    }
    
    private Map<String, String> getNotes(List<ModelResource> notes) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (ModelResource note : notes) {
            if (note.getValue("Note") != null) {
                String value = note.getValue("Note").toString();
                value = StringUtils.remove(value, '\r');
                map.put(note.getValue("Name").toString(), value);               
            }
        }
        return map;
    }

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		velocityEngine = new VelocityEngine();

		velocityEngine.setProperty("runtime.references.strict", true);
		velocityEngine.init();
		velocityToolManager = new ToolManager();
		velocityToolManager.configure("velocity-tools.xml");

		velocityToolManager.setVelocityEngine(velocityEngine);
	}
}
