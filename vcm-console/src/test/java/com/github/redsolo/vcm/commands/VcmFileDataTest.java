package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.util.TestUtil;

public class VcmFileDataTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void assertIsDeprecatedIsReadThroughTags() throws Throwable {
		File copyFile = folder.newFile();
		
		FileUtils.copyFile(TestUtil.getResourceFile(folder.getRoot()), copyFile);
		assertThat(new VcmFileData(TestUtil.getResourceFile(folder.getRoot()),  null).getIsDeprecated(), is(false));
		
		Model model = new Model(copyFile);
		ComponentData componentData = model.getComponentData();
		componentData.setTags(new String[]{"IsDeprecated"});		
		model.setComponentData(componentData, false);
		assertThat(new VcmFileData(copyFile,  null).getIsDeprecated(), is(true));
	}
	@Test
	public void assertIsDeprecatedIsReadThroughKeywords() throws Throwable {
		File copyFile = folder.newFile();
		
		FileUtils.copyFile(TestUtil.getResourceFile(folder.getRoot()), copyFile);
		assertThat(new VcmFileData(TestUtil.getResourceFile(folder.getRoot()),  null).getIsDeprecated(), is(false));
		
		Model model = new Model(copyFile);
		ComponentData componentData = model.getComponentData();
		componentData.getKeywords().put("IsDeprecated", "True");	
		model.setComponentData(componentData, false);
		assertThat(new VcmFileData(copyFile,  null).getIsDeprecated(), is(true));
	}
}
