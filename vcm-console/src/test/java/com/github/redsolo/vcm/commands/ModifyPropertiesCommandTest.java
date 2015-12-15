package com.github.redsolo.vcm.commands;

import static com.github.redsolo.vcm.Matchers.hasItemWithName;
import static com.github.redsolo.vcm.Matchers.hasItemWithValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyPropertiesCommand;
import com.github.redsolo.vcm.commands.ResourceDataParser;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyPropertiesCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertPropertyIsRemoved() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModelResource variables = ResourceDataParser.getVariables(new Model(file).getResourceData());
		assertThat(variables.getResources(), hasItemWithName("ACME::PLATFORM"));
		
		ModifyPropertiesCommand command = new ModifyPropertiesCommand();
		command.setRemoveProps("ACME::Platform");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		variables = ResourceDataParser.getVariables(new Model(file).getResourceData());
		assertThat(variables.getResources(), not(hasItemWithName("ACME::PLATFORM")));
	}
	
	@Test
	public void assertPropertiesAreUpdated() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ModifyPropertiesCommand command = new ModifyPropertiesCommand();
		command.setUpdateProps("ACME::PLATFORM=NEWVALUE");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		Model model = new Model(file);
		ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
		assertThat(variables.getResources(), hasItemWithValue("ACME::PLATFORM", "NEWVALUE"));
	}
    
    @Test
    public void assertEmptyingPropertiesAreUpdated() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModifyPropertiesCommand command = new ModifyPropertiesCommand();
        command.setUpdateProps("ACME::PLATFORM=");
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());
        
        Model model = new Model(file);
        ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
        assertThat(variables.getResources(), hasItemWithValue("ACME::PLATFORM", ""));
    }
    
    @Test
    public void assertNewPropertiesAreAdded() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModifyPropertiesCommand command = new ModifyPropertiesCommand();
        command.setAddProps("ACME::NEWONE=Apa");
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());
        
        Model model = new Model(file);
        ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
        assertThat(variables.getResources(), hasItemWithValue("ACME::NEWONE", "Apa"));
    }
}
