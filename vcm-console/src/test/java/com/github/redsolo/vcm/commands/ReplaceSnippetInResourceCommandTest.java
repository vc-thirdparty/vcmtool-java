package com.github.redsolo.vcm.commands;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.ReplaceSnippetInResourceCommand;
import com.github.redsolo.vcm.commands.ResourceDataParser;
import com.github.redsolo.vcm.util.TestUtil;

public class ReplaceSnippetInResourceCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertPropertiesAreUpdated() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot(), "/SnippetIdler.vcm");
		File snippetFile = folder.newFile("snippet.txt");
		FileUtils.writeStringToFile(snippetFile, "#This is a Snippet\r\nkey=\"flag\"");
		ReplaceSnippetInResourceCommand command = new ReplaceSnippetInResourceCommand();
		command.addSnippet("SnippetOne", snippetFile);
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		command.execute(new MainConfiguration());
		
		Model model = new Model(file);
		ModelResource pythonScriptResource = ResourceDataParser.getPythonScripts(model.getResourceData()).get(0);
		String pythonScript = (String)pythonScriptResource.getValue("Script");
        assertThat(pythonScript, not(containsString("Do not see this")));
        assertThat(pythonScript, containsString("#This is a Snippet\r\\nkey=\\\"flag\\\""));
	}
}
