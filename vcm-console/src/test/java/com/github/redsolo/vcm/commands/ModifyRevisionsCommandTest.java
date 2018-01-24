package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.Matchers.is;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.ComponentModel;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyRevisionsCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyRevisionsCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertVersionIsUpdated() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyRevisionsCommand command = new ModifyRevisionsCommand();
		command.setRevision("2014.23.2");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getValues(), hasEntry("DetailedRevision", (Object)"2014.23.2.48"));
	}
	
	@Test
	public void assertVersionIsUpdatedWithNextGen() throws Throwable {
		File file = TestUtil.getVcmxResourceFile(folder.getRoot());
		ModifyRevisionsCommand command = new ModifyRevisionsCommand();
		command.setRevision("2014.23.2");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());

		ComponentModel componentModel = new Model(file).getComponentModel();
		assertThat(componentModel.getDetailedRevision(), is("2014.23.2.200"));
	}

	@Test
	public void assertVersionWithWildCharIsUpdated() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyRevisionsCommand command = new ModifyRevisionsCommand();
		command.setRevision("*.23.*");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentData componentData = new Model(file).getComponentData();
		assertThat(componentData.getValues(), hasEntry("DetailedRevision", (Object)"2012.23.0.48"));
	}

	@Test
	public void assertVersionWithWildCharIsUpdatedWithNextGenFile() throws Throwable {
		File file = TestUtil.getVcmxResourceFile(folder.getRoot());
		ModifyRevisionsCommand command = new ModifyRevisionsCommand();
		command.setRevision("*.23.*");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		ComponentModel componentModel = new Model(file).getComponentModel();
		assertThat(componentModel.getDetailedRevision(), is("4.23.4.200"));
	}
}
