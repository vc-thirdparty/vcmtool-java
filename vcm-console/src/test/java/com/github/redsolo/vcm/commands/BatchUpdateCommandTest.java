package com.github.redsolo.vcm.commands;

import static com.github.redsolo.vcm.Matchers.hasItemWithValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.commands.BatchUpdateCommand;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ResourceDataParser;
import com.github.redsolo.vcm.util.TestUtil;

public class BatchUpdateCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void assertFileIsUsed() throws Throwable {
        File modelFile = TestUtil.getResourceFile(folder.getRoot());
        File importFile = folder.newFile("items_update.csv");
        FileUtils.writeLines(importFile, Arrays.asList(
                "FileName;Item[Description];Item[Name];KeyWord[Manufacturer];Var[ACME::FunctionalCategory]", 
                String.format("%s;NewDescription;NewName;NewManufacturer;NewFunc", modelFile.getAbsolutePath())));

        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setFile(importFile.getAbsolutePath());
        command.setComponentRootPath(modelFile.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());
        
        Model model = new Model(modelFile);
        ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
        ComponentData componentData = model.getComponentData();
        
        assertThat(componentData.getKeywords(), hasEntry("Manufacturer", "NewManufacturer"));
        assertThat(componentData.getItems(), hasEntry("Name", "NewName"));
        assertThat(componentData.getItems(), hasEntry("Description", "NewDescription"));
        assertThat(variables.getResources(), hasItemWithValue("ACME::FunctionalCategory", "NewFunc"));
        assertThat(componentData.getRevision(), is(49l));
    }
    @Test
    public void assertMissingFileIsIgnored() throws Throwable {
        File modelFile = TestUtil.getResourceFile(folder.getRoot());
        File importFile = folder.newFile("items_update.csv");
        FileUtils.writeLines(importFile, Arrays.asList(
                "FileName;Item[Description];Item[Name];KeyWord[Manufacturer]", 
                "apapa.vcm;NewDescription;NewName;NewManufacturer"));

        BatchUpdateCommand command = new BatchUpdateCommand();
        command.setIgnoreMssingFiles(true);
        command.setFile(importFile.getAbsolutePath());
        command.setComponentRootPath(modelFile.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());
    }
}
