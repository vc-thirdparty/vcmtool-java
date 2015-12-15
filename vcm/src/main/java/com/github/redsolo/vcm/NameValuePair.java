package com.github.redsolo.vcm;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class NameValuePair {

	private Object value;
	private String name;
	
	public NameValuePair(Object value) {
		this.value = value;			
	}
	public NameValuePair(String name, Object value) {
		this.name = name;
		this.value = value;			
	}
	
	public Object getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("={");
		builder.append(value);
		builder.append("}");
		return builder.toString();
	}	
}
