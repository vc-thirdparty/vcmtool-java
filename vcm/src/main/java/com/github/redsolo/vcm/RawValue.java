package com.github.redsolo.vcm;

public class RawValue {

	private String valueString;

	public RawValue(String valueString) {
		this.valueString = valueString;
	}

	@Override
	public String toString() {
		return valueString;
	}
}
