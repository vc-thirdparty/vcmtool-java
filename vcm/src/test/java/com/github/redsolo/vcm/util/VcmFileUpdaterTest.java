package com.github.redsolo.vcm.util;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelTest;
import com.github.redsolo.vcm.util.VcmFileUpdater;

public class VcmFileUpdaterTest {

	@Test
	public void assertFilesAreAddedToVcm() throws Throwable {
		File vcmFile = ModelTest.getResourceFile();
		VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(vcmFile);
		File tempFile = ModelTest.getResourceFile("/component.dat");
		vcmFileUpdater.addFiles(tempFile);
		vcmFileUpdater.update(false);
		ComponentData componentData = new Model(vcmFile).getComponentData();
		assertThat(componentData.getRevision(), is((long)90));
	}
	
	@Test
	public void assertStreamsAreAddedToVcm() throws Throwable {
		File vcmFile = ModelTest.getResourceFile();
		VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(vcmFile);
		File tempFile = ModelTest.getResourceFile("/component.dat");

		ByteArrayInputStream stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile));
		vcmFileUpdater.addFile("component.dat", stream);
		vcmFileUpdater.update(false);
		ComponentData componentData = new Model(vcmFile).getComponentData();
		assertThat(componentData.getRevision(), is((long)90));
	}
    
    @Test
    public void assertBytArraysAreAddedToVcm() throws Throwable {
        File vcmFile = ModelTest.getResourceFile();
        VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(vcmFile);
        File tempFile = ModelTest.getResourceFile("/component.dat");
        vcmFileUpdater.addFile("component.dat", FileUtils.readFileToByteArray(tempFile));
        vcmFileUpdater.update(false);
        ComponentData componentData = new Model(vcmFile).getComponentData();
        assertThat(componentData.getRevision(), is((long)90));
    }
    
    @Test
    public void assertDateIsKept() throws Throwable {
        File vcmFile = ModelTest.getResourceFile();
        VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(vcmFile);
        File tempFile = ModelTest.getResourceFile("/component.dat");
        vcmFileUpdater.addFile("component.dat", FileUtils.readFileToByteArray(tempFile));
        vcmFileUpdater.update(false);
        
        assertThat(new Model(vcmFile).getLastModifiedTime(Model.COMPONENT_DAT), is(1369222058000l));
        assertThat(new Model(vcmFile).getLastModifiedTime(Model.COMPONENT_RSC), is(1386949374000L));
    }
    
    @Test
    public void assertDateIsUpdated() throws Throwable {
        File vcmFile = ModelTest.getResourceFile();
        VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(vcmFile);
        File tempFile = ModelTest.getResourceFile("/component.dat");
        vcmFileUpdater.addFile("component.dat", FileUtils.readFileToByteArray(tempFile));
        vcmFileUpdater.update(true);
        
        Model model = new Model(vcmFile);
        assertThat(model.getLastModifiedTime(Model.COMPONENT_DAT), is(greaterThan(1369222058000l)));
        assertThat(model.getLastModifiedTime(Model.COMPONENT_RSC), is(greaterThan(1386949374000L)));
    }
}
