package com.github.redsolo.vcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Model resource that is a data model representation of a component.dat or component.rsc file
 */
public class ModelResource {

	private String name;
	private String type;
	private List<NameValuePair> values = new ArrayList<NameValuePair>();

	public ModelResource() {
	}
	
	public ModelResource(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public ModelResource(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public ModelResource setName(String name) {
		this.name = name;
		return this;
	}

	public String getType() {
		return type;
	}

	public ModelResource setType(String type) {
		this.type = type;
		return this;
	}
	
	public ModelResource putValue(String string, Object obj) {
		values.add(new NameValuePair(string, obj));
		return this;
	}

	/**
	 * Replaces the value of the named resource, if the resource does not exist it will add it to the end of the list of resources 
	 * @param name the name of resource
	 * @param value the new value for the resource
	 * @return the current modelresource, builder pattern
	 */
	public ModelResource setValue(String name, Object value) {
		for (int index = 0; index < values.size(); index++) {
			NameValuePair nameValuePair = values.get(index);
			if (nameValuePair.getName() != null && nameValuePair.getName().equals(name)) {
				values.set(index, new NameValuePair(nameValuePair.getName(), value));
				return this;
			}
		}
		values.add(new NameValuePair(name, value));
		return this;
	}

	public boolean hasValue(String string) {
		for (NameValuePair value : values) {
			if (value.getName() != null && value.getName().equals(string)) {
				return true;
			}
		}
		return false;
	}

	public Object getValue(String string) {
		for (NameValuePair value : values) {
			if (value.getName() != null && value.getName().equals(string)) {
				return value.getValue();
			}
		}
		return null;
	}
	
	public Collection<NameValuePair> getValues() {
		return values;
	}
	
	public ModelResource addResource(ModelResource childResource) {
		values.add(new NameValuePair(name, childResource));
		return this;
	}

	public void removeResource(ModelResource variable) {
		for (NameValuePair pair : values) {
			if (pair.getValue() instanceof ModelResource) {
				ModelResource resource = (ModelResource) pair.getValue();
				if (resource.equals(variable)) {
					values.remove(pair);
					break;
				}
			}
		}
	}

	/**
	 * Returns the resource with the specified name
	 * @param type
	 * @return the resource; null if it is not found
	 */
	public ModelResource getResource(String type) {
		for (NameValuePair resourceValue : values) {
			Object obj = resourceValue.getValue();
			if (obj instanceof ModelResource) {
				ModelResource childResource = (ModelResource) obj;
				if (childResource.getType().equals(type)) {
					return childResource;
				}
			}
		}
		return null;
	}

	public ModelResource getResource(String type, String name) {
		for (NameValuePair resourceValue : values) {
			Object obj = resourceValue.getValue();
			if (obj instanceof ModelResource) {
				ModelResource childResource = (ModelResource) obj;
				if (childResource.getType().equals(type)) {
					if (name == null || name.equals(childResource.getName())) {
						return childResource;
					}
				}
			}
		}
		return null;
	}

	public List<ModelResource> getResources(String type) {
		ArrayList<ModelResource> list = new ArrayList<ModelResource>();
		for (NameValuePair resourceValue : values) {
			Object obj = resourceValue.getValue();
			if (obj instanceof ModelResource) {
				ModelResource childResource = (ModelResource) obj;
				if (type == null || childResource.getType().equals(type)) {
					list.add(childResource);
				}
			}
		}
		return list;	
	}
	
	public List<ModelResource> getResources() {
		return getResources(null);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("type=");
		builder.append(type);
		builder.append(", name=");
		builder.append(name);
		builder.append(", values=[");
		builder.append(StringUtils.join(values, ','));
		builder.append("]");
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}
