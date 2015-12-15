package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "List or modifies meta data (component.dat)")
public class ModifyItemsCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ModifyItemsCommand.class);

	@Parameter(description = "add items (comma separated key-value list)", names = { "-a", "--add" }) 
	private String addItems;

	@Parameter(description = "remove items (comma separated list)", names = { "-r", "--remove" }) 
	private String removeItems;

	@Parameter(description = "list items", names = { "-l", "--list" }) 
	private boolean listItems;

	private List<String> itemNamesToRemove;
	private Map<String, String> itemNamesToAdd;

	@Override
	public String getName() {
		return "items";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		if (removeItems != null) {
			itemNamesToRemove = new ArrayList<String>();
			for (String string : Arrays.asList(StringUtils.split(removeItems, ","))) {
				itemNamesToRemove.add(string.toLowerCase());
			}
		}
		
		if (addItems != null) {
			itemNamesToAdd = new HashMap<String, String>();
			for (String keyValueString : StringUtils.split(addItems, ",")) {
				String[] stringArray = StringUtils.split(keyValueString, '=');
				if (stringArray.length != 2) {
					throw new CommandExecutionException(2, "");
				} else {
					itemNamesToAdd.put(stringArray[0], stringArray[1]);
				}
			}
		}
	}
	
	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		listItems = listItems || (itemNamesToRemove == null && itemNamesToAdd == null);
		ComponentData componentData = model.getComponentData();
		Map<String, String> items = componentData.getItems();
		
		boolean itemsWereChanged = false;
		if (itemNamesToRemove != null) {
			for (String key : items.keySet().toArray(new String[items.size()])) {
				if (itemNamesToRemove.contains(key.toLowerCase())) {
					items.remove(key);
					itemsWereChanged = true;
				}
			}
		}
		
		if (itemNamesToAdd != null) {
			for (Map.Entry<String, String> entry : itemNamesToAdd.entrySet()) {
				items.put(entry.getKey(), entry.getValue());
				itemsWereChanged = true;
			}
		}

		if (listItems) {
			log.info(String.format("%s: %s", model.getFile().getName(), items));
		}
		if (itemsWereChanged) {
			model.setComponentData(componentData, !skipRevisionUpdate);
		}
	}

	public void setAddItems(String addItems) {
		this.addItems = addItems;
	}
	public void setRemoveItems(String removeItems) {
		this.removeItems = removeItems;
	}
}
