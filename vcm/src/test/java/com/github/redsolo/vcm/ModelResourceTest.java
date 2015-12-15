package com.github.redsolo.vcm;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.redsolo.vcm.ModelResource;


public class ModelResourceTest {

	@Test
	public void assertResourceHasNamedValues() {
		ModelResource resource = new ModelResource();
		resource.putValue("key", 3);
		assertThat(resource.getValue("key"), is((Object) 3));
	}
	
	@Test
	public void assertResourceHasNamedChildren() {
		ModelResource childResource = new ModelResource("named");
		ModelResource resource = new ModelResource();
		resource.addResource(childResource);
		assertThat(resource.getResource("named"), is(childResource));
	}
	
	@Test
	public void assertEquals() {
		ModelResource resource = new ModelResource().addResource(new ModelResource("child", "sven"));
		ModelResource resourceTwo = new ModelResource().addResource(new ModelResource("child", "sven"));
		assertThat(resource.equals(resourceTwo), is(true));
		assertThat(resourceTwo.equals(resource), is(true));
	}
	
	@Test
	public void assertHashCode() {
		ModelResource resource = new ModelResource().addResource(new ModelResource("child", "sven"));
		ModelResource resourceTwo = new ModelResource().addResource(new ModelResource("child", "sven"));
		assertThat(resource.hashCode(), is(resourceTwo.hashCode()));
		assertThat(resourceTwo.hashCode(), is(resource.hashCode()));
	}
	
	@Test
	public void assertGetResourcesReturnChildren() {
		ModelResource childOne = new ModelResource("type1");
		ModelResource childTwo = new ModelResource("type2");
		ModelResource resource = new ModelResource();
		resource.addResource(childOne);
		resource.addResource(childTwo);
		assertThat(resource.getResources(), contains(childOne, childTwo));
	}
	
	@Test
	public void assertGetResourcesWithTypeReturnChildren() {
		ModelResource childOne = new ModelResource("typeA", "named");
		ModelResource childTwo = new ModelResource("typeB", "namedTwo");
		ModelResource resource = new ModelResource();
		resource.addResource(childOne);
		resource.addResource(childTwo);
		assertThat(resource.getResources(), contains(childOne, childTwo));
		assertThat(resource.getResources("typeB"), contains(childTwo));
		assertThat(resource.getResource("typeB", "namedTwo"), is(childTwo));
	}

	@Test
	public void assertChildResourceCanBeRemoved() {
		ModelResource childResource = new ModelResource("named");
		ModelResource resource = new ModelResource();
		resource.addResource(childResource);
		assertThat(resource.getResources(), hasItem(childResource));
		resource.removeResource(childResource);
		assertThat(resource.getResources(), not(hasItem(childResource)));
	}
	
	@Test
	public void assertSetValueUpdatesNameValuePair() {
		ModelResource resource = new ModelResource().putValue("named", "value");
		resource.setValue("named", "othervalue");
		assertThat(resource.getValue("named").toString(), is("othervalue"));
	}
}
