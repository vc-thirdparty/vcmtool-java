package com.github.redsolo.vcm.commands;

import static com.github.redsolo.vcm.Matchers.hasItemWithValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ReplaceTextInResourceCommand;
import com.github.redsolo.vcm.commands.ResourceDataParser;
import com.github.redsolo.vcm.util.TestUtil;

public class ReplaceTextInResourceCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertPropertiesAreUpdated() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		ReplaceTextInResourceCommand command = new ReplaceTextInResourceCommand();
		command.setReplacementtext(Arrays.asList(new String[]{"ACME::PLATFORM=ACME::PlatformS"}));
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		Model model = new Model(file);
		ModelResource variables = ResourceDataParser.getVariables(model.getResourceData());
        assertThat(variables.getResources(), hasItemWithValue("ACME::PlatformS", "Y12"));
        assertThat(model.getComponentData().getRevision(), is(49l));
	}
}
