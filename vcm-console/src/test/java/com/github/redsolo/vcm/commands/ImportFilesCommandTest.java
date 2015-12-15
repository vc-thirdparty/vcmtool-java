package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.commands.ImportFilesCommand;
import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.util.TestUtil;

public class ImportFilesCommandTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertTagsAreRemoved() throws Throwable {
		File tempFile = File.createTempFile("vcm", ".dat");
		tempFile.deleteOnExit();
		File tempFileTwo = File.createTempFile("vcm", ".dat");
		tempFileTwo.deleteOnExit();
		
		File file = TestUtil.getResourceFile(folder.getRoot());
		ImportFilesCommand command = new ImportFilesCommand();
		command.setFilenames(Arrays.asList(new String[]{ file.getCanonicalPath(), tempFile.getCanonicalPath(), tempFileTwo.getCanonicalPath() }));
		command.execute(new MainConfiguration());
		
		assertThat(new Model(file).listContent(), hasItem(tempFile.getName()));
		assertThat(new Model(file).listContent(), hasItem(tempFileTwo.getName()));
	}
	
	@Test
	public void assertStripingFileName() throws Throwable {
		File tempFile = File.createTempFile("beam_vcm", ".dat");
		tempFile.deleteOnExit();
		
		File file = TestUtil.getResourceFile(folder.getRoot());
		ImportFilesCommand command = new ImportFilesCommand();
		command.setStrip("beam_");
		command.setFilenames(Arrays.asList(new String[]{ file.getCanonicalPath(), tempFile.getCanonicalPath() }));
		command.execute(new MainConfiguration());
		
		assertThat(new Model(file).listContent(), hasItem(StringUtils.remove(tempFile.getName(), "beam_")));
	}

}
