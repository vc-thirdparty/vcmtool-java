package com.github.redsolo.vcm;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.github.redsolo.vcm.ModelResource;

public class Matchers {
	
	public static Matcher<List<ModelResource>> hasItemWithName(String name) {
		return new HasItemWithValueMatcher(name, null);
	}
	public static Matcher<List<ModelResource>> hasItemWithValue(String name, String value) {
		return new HasItemWithValueMatcher(name, value);
	}
	public static class HasItemWithValueMatcher extends TypeSafeMatcher<List<ModelResource>> {
		private String needleName;
		private String needleValue;

		public HasItemWithValueMatcher(String name, String value) {
			this.needleName = name;
			this.needleValue = value;
		}
		@Override
		public void describeTo(Description description) {
			if (needleValue == null) {
				description.appendText(String.format("a model resource with a value named='%s'", needleName));
			} else {
				description.appendText(String.format("a model resource with a key-value-pair named='%s' and value='%s'", needleName, needleValue));
			}
		}
		@Override
		protected boolean matchesSafely(List<ModelResource> modelResources) {
			for (ModelResource modelResource : modelResources) {
				Object obj = modelResource.getValue("Name");
				if (obj instanceof String) {
					String name = (String) obj;
					if (needleName.equalsIgnoreCase(name)) {
						obj = modelResource.getValue("Value");
						if (needleValue == null || needleValue.equals(obj)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	public static Matcher<ModelResource> withName(String name) {
		return new HasValueWithNameMatcher(name, null);
	}
	public static Matcher<ModelResource> withValue(String name, String value) {
		return new HasValueWithNameMatcher(name, value);
	}
	public static class HasValueWithNameMatcher extends TypeSafeMatcher<ModelResource> {
		private String needleName;
		private String needleValue;

		public HasValueWithNameMatcher(String name, String value) {
			this.needleName = name;
			this.needleValue = value;
		}
		@Override
		public void describeTo(Description description) {
			if (needleValue == null) {
				description.appendText(String.format("a model resource with a value named='%s'", needleName));
			} else {
				description.appendText(String.format("a model resource with a key-value-pair named='%s' and value='%s'", needleName, needleValue));
			}
		}
		@Override
		protected boolean matchesSafely(ModelResource modelResource) {
			
			Object obj = modelResource.getValue("Name");
			if (obj instanceof String) {
				String name = (String) obj;
				if (needleName.equalsIgnoreCase(name)) {
					obj = modelResource.getValue("Value");
					if (needleValue == null || needleValue.equals(obj)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
