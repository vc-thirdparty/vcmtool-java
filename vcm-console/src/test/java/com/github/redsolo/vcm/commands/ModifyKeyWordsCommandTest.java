package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyKeyWordsCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyKeyWordsCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertKeyWordsAreAdded() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyKeyWordsCommand command = new ModifyKeyWordsCommand();
		command.setAddKeywords("newkey=newvalue,otherkey=value");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getKeywords(), hasEntry("newkey", "newvalue"));
	}
	
	@Test
	public void assertKeyWordsAreRemoved() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyKeyWordsCommand command = new ModifyKeyWordsCommand();
		command.setRemoveKeywords("Manufacturer");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getKeywords(), not(hasEntry("Manufacturer", "ACME")));
	}
}
