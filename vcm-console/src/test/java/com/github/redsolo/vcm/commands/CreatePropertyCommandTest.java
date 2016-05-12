package com.github.redsolo.vcm.commands;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.util.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import static com.github.redsolo.vcm.Matchers.hasItemWithName;
import static com.github.redsolo.vcm.Matchers.hasItemWithValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CreatePropertyCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void assertPropertyIsCreated() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModelResource variables = ResourceDataParser.getVariables(new Model(file).getResourceData());
        assertThat(variables.getResources(), not(hasItemWithName("FlexLink::PalletSize")));

        CreatePropertyCommand command = new CreatePropertyCommand();
        command.setPropertyName("FlexLink::PalletSize");
        command.setPropertyType("string");
        command.setPropertyValue("120");
        command.setPropertySetting(new String[]{"EDITABLE_DISCONNECTED"});
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());

        Model model = new Model(file);
        variables = ResourceDataParser.getVariables(model.getResourceData());
        assertThat(variables.getResources(), hasItemWithName("FlexLink::PalletSize"));
        assertThat(variables.getResources(), hasItemWithValue("FlexLink::PalletSize", "120"));
        ModelResource variable = variables.getResources().stream().filter(v -> v.getValue("Name").equals("FlexLink::PalletSize")).findFirst().get();
        assertThat(variable.getResource("Settings").hasValue("EDITABLE_DISCONNECTED"), is(true));
    }
    @Test
    public void assertSetPropertyIsCreated() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModelResource variables = ResourceDataParser.getVariables(new Model(file).getResourceData());
        assertThat(variables.getResources(), not(hasItemWithName("FlexLink::PalletSize")));

        CreatePropertyCommand command = new CreatePropertyCommand();
        command.setPropertyName("FlexLink::PalletSize");
        command.setPropertyType("string");
        command.setPropertyValue("120");
        command.setPropertyValues(new String[]{"120", "240"});
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());

        Model model = new Model(file);
        variables = ResourceDataParser.getVariables(model.getResourceData());
        ModelResource variable = variables.getResources().stream().filter(v -> v.getValue("Name").equals("FlexLink::PalletSize")).findFirst().get();
        assertThat(variable.getValues().stream().filter(v -> v.getName().equals("Group")).findFirst().isPresent(), is(true));
        assertThat(variable.getResources().stream().filter(r -> r.getType().equals("Settings")).findFirst().isPresent(), is(true));
        ModelResource stepList = variable.getResource("StepList");
        assertThat(stepList, is(notNullValue()));
        assertThat(stepList.getResources().size(), is(2));
        Optional<ModelResource> step = stepList.getResources().stream().filter(v -> v.getType().equalsIgnoreCase("Step") && v.getValue("Value").equals("240")).findFirst();
        assertThat(step.isPresent(), is(true));
    }

    @Test
    public void assertSetIntPropertyIsCreated() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModelResource variables = ResourceDataParser.getVariables(new Model(file).getResourceData());
        assertThat(variables.getResources(), not(hasItemWithName("FlexLink::PalletSize")));

        CreatePropertyCommand command = new CreatePropertyCommand();
        command.setPropertyName("FlexLink::PalletSize");
        command.setPropertyType("int");
        command.setPropertyValue("120");
        command.setPropertyValues(new String[]{"120", "240"});
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());

        Model model = new Model(file);
        variables = ResourceDataParser.getVariables(model.getResourceData());
        ModelResource variable = variables.getResources().stream().filter(v -> v.getValue("Name").equals("FlexLink::PalletSize")).findFirst().get();
        ModelResource stepList = variable.getResource("StepList");
        assertThat(stepList, is(notNullValue()));
        assertThat(stepList.getResources().size(), is(2));
        Optional<ModelResource> step = stepList.getResources().stream().filter(v -> v.getType().equalsIgnoreCase("Step") && (long)v.getValue("Value") == 240).findFirst();
        assertThat(step.isPresent(), is(true));
    }
}