package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.VerifyPropertyValuesCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class VerifyPropertyValuesCommandTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertPropertyIsVerified() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		
		VerifyPropertyValuesCommand command = new VerifyPropertyValuesCommand();
		command.setVerifyProperties("ACME::Platform=X86");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		int value = command.execute(new MainConfiguration());
		
		assertThat(value, is(not(0)));
	}

	@Test
	public void assertEmptyPropertyCanBeRed() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		
		VerifyPropertyValuesCommand command = new VerifyPropertyValuesCommand();
		command.setVerifyProperties("ACME::Empty=");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		int value = command.execute(new MainConfiguration());
		
		assertThat(value, is(0));
	}
}
