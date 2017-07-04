package com.github.redsolo.vcm;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@SuppressWarnings("unchecked")
public class ModelTest {

	public static File getResourceFile() throws IOException {
		return getResourceFile("/Idler.vcm");
	}
	
	public static File getResourceFile(String string) throws IOException {
		File tempDirectory = FileUtils.getTempDirectory();
		tempDirectory.deleteOnExit();
		File temp = new File(tempDirectory, StringUtils.removeStart(string, "/"));
		FileUtils.copyURLToFile(ModelTest.class.getResource(string), temp);
		return temp;
	}

	@Test
	public void assertVcmFileCanListContent() throws Throwable {
		Model vcm = new Model(getResourceFile("/Idler.vcm"));
		Matcher<Iterable<String>> matcher = hasItems(is("component.rsc"), is("component_icon.tga"));
		assertThat(vcm.listContent(), matcher);
	}

	@Test
	public void assertThumbNailCanBeRead() throws Throwable {
		Model vcm = new Model(getResourceFile("/Idler.vcm"));
		InputStream thumbnail = null;
		try {
			thumbnail = vcm.getThumbnail();
			assertThat(thumbnail, is(notNullValue()));
		} finally {
			IOUtils.closeQuietly(thumbnail);
		}
	}

	@Test
	public void assertComponentDataIsRead() throws Throwable {
		Model vcm = new Model(getResourceFile("/Idler.vcm"));
		ComponentData componentData = vcm.getComponentData();
		assertThat(componentData.getVcId(), is("421fb14f-42c4-4b66-9a19-8d1ed0fce9d7"));
	}

	@Test
	public void assertResourcetDataIsRead() throws Throwable {
		Model vcm = new Model(getResourceFile("/Idler.vcm"));
		vcm.getResourceData();
	}

	@Test
	public void assertComponentDataCanBeWritten() throws Throwable {
		Model vcm = new Model(getResourceFile("/Idler.vcm"));
		ComponentData componentData = vcm.getComponentData();
		componentData.setTags(new String[]{"Testing", "Ok"});
		vcm.setComponentData(componentData, false);
		assertThat(new Model(vcm.getFile()).getComponentData().getTags(), is(new String[]{"Testing", "Ok"}));
	}

	@Test
	public void assertOutputStreamIsWrittenToFile() throws Throwable {
		// Given a vcm file
		// When a file is written using outputstream
		// Then verify that the file exists in the file
		Model vcm = new Model(getResourceFile("/Idler.vcm"));

		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			outputStream = vcm.getOutputStream("new_file.txt");
			IOUtils.write("This is a test", outputStream);
			outputStream.close();
			assertThat(vcm.listContent(), hasItem("new_file.txt"));
			
			inputStream = vcm.getInputStream("new_file.txt");
			List<String> lines = IOUtils.readLines(inputStream);
			assertThat(lines, hasItem("This is a test"));
		} finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
		}		
	}

	@Test
	public void assertZipFileOnlyIsUpdatedWhenChangingComponentDat() throws Throwable {
		Model vcm = new Model(getResourceFile("/Idler.vcm"));
		long lastModified = vcm.getFile().lastModified();
		
		ComponentData componentData = vcm.getComponentData();
		vcm.setComponentData(componentData, false);
		
		assertThat(vcm.getFile().lastModified(), is(lastModified));
	}

    @Test
    public void assertZipFileOnlyIsUpdatedWhenChangedComponentRsc() throws Throwable {
        Model vcm = new Model(getResourceFile("/Idler.vcm"));
        long lastModified = vcm.getFile().lastModified();
        
        ModelResource resourceData = vcm.getResourceData();
        vcm.setResourceData(resourceData, false);
        
        assertThat(vcm.getFile().lastModified(), is(lastModified));
    }

    @Test
    public void assertLayoutWithoutComponentResourceFileIsLayout() throws Throwable {
        Model vcm = new Model(getResourceFile("/Idler_layout.vcm"));        
        assertThat(vcm.isLayout(), is(true));        
        assertThat(vcm.isComponent(), is(false));
        
        vcm = new Model(getResourceFile("/Idler.vcm"));        
        assertThat(vcm.isLayout(), is(false));        
        assertThat(vcm.isComponent(), is(true));
    }

    @Test
    public void assertResourcetDataIsReadForComponents() throws Throwable {
        Model vcm = new Model(getResourceFile("/Idler.vcm"));
        assertThat(vcm.getResourceData(), is(not(nullValue())));
    }

    @Test
    public void assertResourcetDataIsReadForLayouts() throws Throwable {
        Model vcm = new Model(getResourceFile("/Idler_layout.vcm"));
        assertThat(vcm.getResourceData(), is(not(nullValue())));
    }
    
    @Test
    public void assertTimeStampIsntUpdated() throws Throwable {
        File resourceFile = getResourceFile("/Idler.vcm");
        long lastModified = resourceFile.lastModified();
        Delay(1000);

        Model vcm = new Model(resourceFile);
        ComponentData componentData = vcm.getComponentData();
        componentData.setVcid("18a768ae-0f31-4476-852d-6f8c099ad3ab");
        vcm.setComponentData(componentData, false);

        assertThat(resourceFile.lastModified(), is(lastModified));
    }
    
    @Test
    public void assertLastModifiedOnFileIsCorrect() throws Throwable {
        File resourceFile = getResourceFile("/Idler.vcm");
        Model vcm = new Model(resourceFile);
        assertThat(vcm.getLastModifiedTime("component.dat"), is(1369222058000l));
    }
    
    @Test
    public void assertRevisionIsUpdatedWhenSavingComponentFile() throws Throwable {
        File resourceFile = getResourceFile("/Idler.vcm");
        long lastModified = resourceFile.lastModified();
        Delay(1000);
        
        Model vcm = new Model(resourceFile);
        ComponentData componentData = vcm.getComponentData();
        componentData.setVcid("18a768ae-0f31-4476-852d-6f8c099ad3ab");
        vcm.setComponentData(componentData, true);

        Model newVcm = new Model(resourceFile);
        assertThat(newVcm.getComponentData().getRevision(), is(48l));
        assertThat(resourceFile.lastModified(), is(greaterThan(lastModified)));
    }
    
    @Test
    public void assertRevisionIsUpdatedWhenSavingResourceFile() throws Throwable {
        File resourceFile = getResourceFile("/Idler.vcm");
        long lastModified = resourceFile.lastModified();
        Delay(1000);

        Model vcm = new Model(resourceFile);
        ModelResource resourceData = vcm.getResourceData();
        ModelResource nodeResource = resourceData.getResource("Node");
        nodeResource.setValue("Name", "NewIdler");
        vcm.setResourceData(resourceData, true);
 
        Model newVcm = new Model(resourceFile);
        assertThat((String)newVcm.getResourceData().getResource("Node").getValue("Name"), is("NewIdler"));
        assertThat(newVcm.getComponentData().getRevision(), is(48l));
        assertThat(resourceFile.lastModified(), is(greaterThan(lastModified)));
        assertThat(vcm.getLastModifiedTime("component.dat"), is(greaterThan(1369222058000l)));
    }
    
    private void Delay(long millis){
    	try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
