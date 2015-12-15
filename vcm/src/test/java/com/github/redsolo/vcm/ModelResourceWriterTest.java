package com.github.redsolo.vcm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.ModelResourceParser;
import com.github.redsolo.vcm.ModelResourceWriter;
import com.github.redsolo.vcm.RawValue;

public class ModelResourceWriterTest {

	@Test
	public void assertCanWriteSimpleValue() throws Throwable {
		ModelResource resource = new ModelResource().putValue("key", "value").putValue("key2", 12);
		StringWriter writer = new StringWriter();
		new ModelResourceWriter().write(resource, writer);
		assertThat(writer.toString(), is("key \"value\"\nkey2 12\n"));
	}

	@Test
	public void assertCanWriteChildResource() throws Throwable {
		ModelResource resource = new ModelResource().putValue("key", "value").addResource(new ModelResource("Item", "Name").putValue("key", 12)).putValue("key2", "value2");
		StringWriter writer = new StringWriter();
		new ModelResourceWriter().write(resource, writer);
		assertEquals(writer.toString(), "key \"value\"\nItem \"Name\"\n{\n  key 12\n}\nkey2 \"value2\"\n");
	}

    @Test
    public void assertCanWriteChildWithIntegerName() throws Throwable {
        ModelResource resource = new ModelResource().addResource(new ModelResource("InternalProxy", "2").putValue("key", 12));
        StringWriter writer = new StringWriter();
        new ModelResourceWriter().write(resource, writer);
        assertEquals(writer.toString(), "InternalProxy 2\n{\n  key 12\n}\n");
    }

	@Test
	public void assertCanWriteSettings() throws Throwable {
		ModelResource resource = new ModelResource().putValue("key", "").putValue("key2", new RawValue("val")).putValue("key3", null);
		StringWriter writer = new StringWriter();
		new ModelResourceWriter().write(resource, writer);
		assertThat(writer.toString(), is("key \"\"\nkey2 val\nkey3\n"));
	}

	@Test
	public void assertReaderAndWriterCreatesSameData() throws Throwable {
		String sourceString = FileUtils.readFileToString(new File(getClass().getResource("/component.dat").toURI()));
		ModelResource sourceResource = new ModelResourceParser().parse(sourceString);
		StringWriter stringWriter = new StringWriter();
		new ModelResourceWriter().write(sourceResource, stringWriter);
		assertEquals(sourceString, stringWriter.toString());
	}
}
