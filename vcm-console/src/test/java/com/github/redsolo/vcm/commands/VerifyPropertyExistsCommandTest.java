package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.commands.MainConfiguration;
import com.github.redsolo.vcm.commands.VerifyPropertyExistsCommand;
import com.github.redsolo.vcm.util.TestUtil;

public class VerifyPropertyExistsCommandTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertPropertyExists() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		
		VerifyPropertyExistsCommand command = new VerifyPropertyExistsCommand();
		command.setVerifyProperties("ACME::Platform");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		int value = command.execute(new MainConfiguration());
		
		assertThat(value, is(0));
	}

	@Test
	public void assertPropertyDoesNotExists() throws Throwable {
		File file = TestUtil.getResourceFile(folder.getRoot());
		
		VerifyPropertyExistsCommand command = new VerifyPropertyExistsCommand();
		command.setVerifyProperties("ACME::NotFoundAtAll");
		command.setComponentRootPath(file.getParentFile().getCanonicalPath());
		int value = command.execute(new MainConfiguration());
		
		assertThat(value, is(not(0)));
	}
}
