package com.github.redsolo.vcm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ComponentModelTest {

	@Test
	public void assertParsingWorks() throws Throwable {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><VcModel xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1\" xmlns=\"http://schemas.visualcomponents.com/2017/01/component/componentxml\">  <Properties>    <Property name=\"VCID\">619b15fe-5dfb-4836-82e1-de44e63e42c3</Property>  </Properties>  <ModelUrl>component.rsc</ModelUrl></VcModel>")));
		ComponentModel model = new ComponentModel(doc);
		assertThat(model.getPropertyValue("VCID"), is(equalTo("619b15fe-5dfb-4836-82e1-de44e63e42c3")));
		assertThat(model.getModelUrl(), is(equalTo("component.rsc")));
	}
	
	@Test
	public void assertSetProperty() throws Throwable {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><VcModel xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1\" xmlns=\"http://schemas.visualcomponents.com/2017/01/component/componentxml\">  <Properties>    <Property name=\"VCID\">619b15fe-5dfb-4836-82e1-de44e63e42c3</Property>  </Properties>  <ModelUrl>component.rsc</ModelUrl></VcModel>")));
		ComponentModel model = new ComponentModel(doc);
		assertThat(model.isChanged(), is(false));
		model.setVcid("196f9842-f467-4c2c-b60a-2138737f5b3e");
		assertThat(model.isChanged(), is(true));
		assertThat(new ComponentModel(doc).getVcid(), is(equalTo("196f9842-f467-4c2c-b60a-2138737f5b3e")));
		assertThat(model.getModelUrl(), is(equalTo("component.rsc")));
	}
}
