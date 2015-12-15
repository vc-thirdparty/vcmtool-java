package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyItemsCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyItemsCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertItemsAreAdded() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyItemsCommand command = new ModifyItemsCommand();
		command.setAddItems("Name=newvalue");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getItems(), hasEntry("Name", "newvalue"));
	}
	
	@Test
	public void assertItemsAreRemoved() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyItemsCommand command = new ModifyItemsCommand();
		command.setRemoveItems("Name");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getItems(), not(hasKey("Name")));
	}
}
