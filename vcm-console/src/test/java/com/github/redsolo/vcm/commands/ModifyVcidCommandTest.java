package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyVcidCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyVcidCommandTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void assertVcidIsUpdated() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyVcidCommand command = new ModifyVcidCommand();
		String expectedVcid = "19a08d4c-fd77-4fe8-8f61-a6eb4189f521";
		command.setNewVcid(expectedVcid);
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		Model model = new Model(file);
		assertThat(model.getComponentData().getVcId(), is(expectedVcid));
		assertThat((String)model.getResourceData().getResource("Node").getResource("NodeClass").getValue("VCID"), 
				is(expectedVcid));
		assertThat(model.getComponentData().getRevision(), is(49l));
	}
}
