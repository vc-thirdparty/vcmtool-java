package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyTagsCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyTagsCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertTagsAreRemoved() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyTagsCommand command = new ModifyTagsCommand();
		command.setWildcards(Arrays.asList(new String[]{"Idler.vcm"}));
		command.setAddTags("newtag");
		command.setRemoveTags("ACME");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getTags(), hasItemInArray("newtag"));
		assertThat(componentData.getTags(), not(hasItemInArray("ACME")));
	}
	
	@Test
	public void assertTagsAreCleared() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyTagsCommand command = new ModifyTagsCommand();
		command.setWildcards(Arrays.asList(new String[]{"Idler.vcm"}));
		command.setClearTags(true);
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getTags(), is(emptyArray()));
	}
}
