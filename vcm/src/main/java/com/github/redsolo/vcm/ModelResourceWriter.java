package com.github.redsolo.vcm;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

public class ModelResourceWriter {

	public void write(ModelResource resource, Writer writer) throws IOException {
		write(resource, writer, "");
		writer.flush();
	}
	
	private void write(ModelResource resource, Writer writer, String indent) throws IOException {
		for (NameValuePair nameValuePair : resource.getValues()) {
			Object obj = nameValuePair.getValue();
			if (obj instanceof ModelResource) {
				ModelResource childResource = ((ModelResource)obj);
				if (childResource.getName() == null) {
					writer.write(String.format("%s%s\n", indent, childResource.getType()));
				} else {
				    if (StringUtils.isNumeric(childResource.getName())) {
                        writer.write(String.format("%s%s %s\n", indent, childResource.getType(), childResource.getName()));				        
				    } else {
				        writer.write(String.format("%s%s \"%s\"\n", indent, childResource.getType(), childResource.getName()));
				    }
				}
				writer.write(String.format("%s{\n", indent));
				write(childResource, writer, String.format("%s  ", indent));
				writer.write(String.format("%s}\n", indent));
			} else {
				writer.write(String.format("%s%s%s\n", indent, nameValuePair.getName(), convertType(obj)));
			}
		}
	} 

	private String convertType(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof String) {
			return String.format(" \"%s\"", (String) value);
		}
		return String.format(" %s", value.toString());
	}
}
