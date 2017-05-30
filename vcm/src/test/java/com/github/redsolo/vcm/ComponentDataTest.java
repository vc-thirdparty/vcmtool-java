package com.github.redsolo.vcm;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.ModelResource;

public class ComponentDataTest {

	@Test
	public void assertEqualsWorks() {
		ComponentData componentDataOne = new ComponentData(new ModelResource().putValue("VcId", "59527d14-9901-4796-8c21-d2395af7b454"));
		ComponentData componentDataTwo = new ComponentData(new ModelResource().putValue("VcId", "59527d14-9901-4796-8c21-d2395af7b454"));
		ComponentData componentDataThree = new ComponentData(new ModelResource().putValue("VcId", "APA"));
		assertThat(componentDataOne, is(equalTo(componentDataTwo)));
		assertThat(componentDataTwo, is(equalTo(componentDataOne)));
		assertThat(componentDataOne, not(is(equalTo(componentDataThree))));
		assertThat(componentDataThree, not(is(equalTo(componentDataOne))));
	}
	
	@Test
	public void assertVcIdIsRetrieved() {
		ComponentData componentData = new ComponentData(new ModelResource().putValue("VcId", "59527d14-9901-4796-8c21-d2395af7b454"));
		assertThat(componentData.getVcId(), is("59527d14-9901-4796-8c21-d2395af7b454"));
	}
	
	@Test
	public void assertRevisionIsRetrieved() {
		ComponentData componentData = new ComponentData(new ModelResource().putValue("Revision", 12l));
		assertThat(componentData.getRevision(), is((long)12));
	}
	
	@Test
	public void assertStringRevisionIsRetrieved() {
		ComponentData componentData = new ComponentData(new ModelResource().putValue("Revision", "12"));
		assertThat(componentData.getRevision(), is((long)12));
	}
    
    @Test
    public void assertItemsAreRetrieved() {
        ModelResource itemResource = new ModelResource("Item").putValue("Name", "Idler");
        ComponentData componentData = new ComponentData(new ModelResource().addResource(new ModelResource("Items").addResource(itemResource)));
        assertThat(componentData.getName(), is("Idler"));
        assertThat(componentData.getItem("Name"), is("Idler"));
    }
    
    @Test
    public void assertItemsWithEmptyValueAreIgnored() {
        ModelResource itemResource = new ModelResource("Item").putValue("Name", "Idler").putValue("PreviewIcon", "thumbnail.tga").putValue("Logo", "");
        ModelResource extraItemResource = new ModelResource("Item").putValue("Name", "Idler - Components").putValue("PreviewIcon", "").putValue("Logo", "logotype.tga");
        ComponentData componentData = new ComponentData(new ModelResource().addResource(new ModelResource("Items").addResource(itemResource).addResource(extraItemResource)));
        assertThat(componentData.getName(), is("Idler"));
        assertThat(componentData.getItem("Name"), is("Idler"));
        assertThat(componentData.getItem("PreviewIcon"), is("thumbnail.tga"));
        assertThat(componentData.getItem("Logo"), is("logotype.tga"));
    }

	@Test
	public void assertKeyWordsAreRetrieved() {
		ModelResource keywordOneResource = new ModelResource("Keyword").putValue("Key", "Manufacturer").putValue("Value", "ACME");
		ModelResource keywordTwoResource = new ModelResource("Keyword").putValue("Key", "Type").putValue("Value", "Accessories");
		ModelResource modelResource = new ModelResource().addResource(new ModelResource("KeywordMap").addResource(keywordOneResource).addResource(keywordTwoResource));
		ComponentData componentData = new ComponentData(modelResource);
		assertThat(componentData.getKeyword("Manufacturer"), is("ACME"));
		assertThat(componentData.getKeyword("Type"), is("Accessories"));
	}
	
	@Test
	public void assertTagsAreSplit() {
		ModelResource resource = new ModelResource().putValue("Tags", "ACME;X85;Conveyors");
		ComponentData componentData = new ComponentData(resource);
		assertThat(componentData.getTags(), arrayContaining("ACME", "X85", "Conveyors"));
	}
	
	@Test
	public void assertRetrievedModelResourceHasSameValues() {
		ModelResource resource = new ModelResource().putValue("Tags", "ACME;X85;Conveyors");
		ComponentData componentData = new ComponentData(resource);
		assertThat(componentData.getResource(), is(equalTo(resource)));
	}
	
	@Test
	public void assertRetrievedModelResourceHasSameResources() {
		ModelResource resource = new ModelResource().putValue("Tags", "ACME;X85;Conveyors");
		ModelResource keywordOneResource = new ModelResource("Keyword").putValue("Key", "Manufacturer").putValue("Value", "ACME");
		resource.addResource(new ModelResource("KeywordMap").addResource(keywordOneResource));
		assertEquals(new ComponentData(resource).getResource(), resource);
	}
	
	@Test
	public void assertSetTagsIsCopied() {
		ModelResource resource = new ModelResource().putValue("Tags", "ACME;X85;Conveyors");
		ComponentData componentData = new ComponentData(resource);
		componentData.setTags(new String[] {"ACME", "X86"});		
		assertThat(componentData.getResource(), is(equalTo(new ModelResource().putValue("Tags", "ACME;X86"))));
	}
    
    @Test
    public void assertIncreasingRevision() {
        ModelResource resource = new ModelResource().putValue("Tags", "ACME;X85;Conveyors").putValue("Revision", 36l).putValue("DetailedRevision", "2014.1.2.36");
        ComponentData componentData = new ComponentData(resource);
        assertThat(componentData.getRevision(), is(equalTo(36l)));
        assertThat(componentData.getDetailedRevision(), is(equalTo("2014.1.2.36")));
        
        componentData.setRevision(37);
        assertThat(componentData.getRevision(), is(equalTo(37l)));
        assertThat(componentData.getDetailedRevision(), is(equalTo("2014.1.2.37")));

        componentData.stepRevision();
        assertThat(componentData.getRevision(), is(equalTo(38l)));
        assertThat(componentData.getDetailedRevision(), is(equalTo("2014.1.2.38")));
    }
}
