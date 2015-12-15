package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ModifyLocationCommand;
import com.github.redsolo.vcm.commands.ResourceDataParser;
import com.github.redsolo.vcm.util.TestUtil;

public class ModifyLocationCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void assertXAndZPositionIsSet() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModifyLocationCommand command = new ModifyLocationCommand();
        command.setXPosition(700);
        command.setZPosition(900);
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());
        
        Model model = new Model(file);
        assertThat(ResourceDataParser.getLocation(model.getResourceData()), is("1 0 0 0 0 1 0 0 0 0 1 0 700 0 900 1"));
    }

    @Test
    public void assertLocationIsSet() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        ModifyLocationCommand command = new ModifyLocationCommand();
        command.setLocation("1 0 0 0 0 1 0 0 0 0 1 0 0 0 900 1");
        command.setComponentRootPath(file.getParentFile().getCanonicalPath());
        command.execute(new MainConfiguration());
        
        Model model = new Model(file);
        assertThat(ResourceDataParser.getLocation(model.getResourceData()), is("1 0 0 0 0 1 0 0 0 0 1 0 0 0 900 1"));
    }
}
