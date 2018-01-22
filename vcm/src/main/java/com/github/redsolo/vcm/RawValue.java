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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((valueString == null) ? 0 : valueString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RawValue other = (RawValue) obj;
		if (valueString == null) {
			if (other.valueString != null)
				return false;
		} else if (!valueString.equals(other.valueString))
			return false;
		return true;
	}
}
