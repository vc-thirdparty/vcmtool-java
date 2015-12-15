package com.github.redsolo.vcm;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.redsolo.vcm.util.LineReader;

public class ModelResourceParser {
	private static final Pattern NAME_REGEX = Pattern.compile("\\s*(\\w+)\\s*(.*)");

	private static final Pattern KEY_VALUE_REGEX = Pattern.compile("\\s*(\\w+)\\s(.+)", Pattern.DOTALL);
	private static final Pattern KEY_REGEX = Pattern.compile("\\s*([\"\\w]+)\\s*");
	private static final Pattern STRING_VALUE_REGEX = Pattern.compile("\"(.*)\"", Pattern.DOTALL);
	private static final Pattern DOUBLE_VALUE_REGEX = Pattern.compile("(\\d+\\.\\d+)");
	private static final Pattern INT_VALUE_REGEX = Pattern.compile("(\\d+)");
	
	
	public ModelResource parse(String string) throws IOException {
		return parse(new StringReader(string));
	}
	
	public ModelResource parse(Reader reader) throws IOException {
		LineReader bufferedReader = new LineReader(reader);		
		return parseModelResource(bufferedReader);
	}

	private ModelResource parseModelResource(LineReader bufferedReader) throws IOException {
		ModelResource resource = new ModelResource();
		String lastLine = null;		
		String line = bufferedReader.readLine();
		while (line != null) {
			
			while (line.endsWith("\\")) {
				line = String.format("%s%s", StringUtils.removeEnd(line, "\\"), bufferedReader.readLine());
			}
			
			if (line.trim().equals("{")) {
				ModelResource childResource = parseModelResource(bufferedReader);				
				Matcher matcher = NAME_REGEX.matcher(lastLine.trim());
				if (matcher.matches()) {
					childResource.setType(matcher.group(1));
					if (!StringUtils.isEmpty(matcher.group(2))) {
					    if (matcher.group(2).startsWith("\"")) {
					        childResource.setName(matcher.group(2).substring(1, matcher.group(2).length() - 1));
					    } else {
					        childResource.setName(matcher.group(2));
					    }
					}
					resource.addResource(childResource);
				}
			} else if (line.trim().equals("}")) {
				break;
			} else {
				if (lastLine != null) {
					line = addValueToResource(resource, lastLine, line);
				}
			}

			lastLine = line;
			line = bufferedReader.readLine();
		}
		
		if (lastLine != null) {
			addValueToResource(resource, lastLine, line);
		}
		
		return resource;
	}

	private String addValueToResource(ModelResource resource, String lastLine, String line) throws IOException {
		Matcher matcher = KEY_VALUE_REGEX.matcher(lastLine);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String group = matcher.group(2);
			resource.putValue(name, convertValue(group));
		} else {
			matcher = KEY_REGEX.matcher(lastLine);
			if (matcher.matches()) {
				resource.putValue(matcher.group(1), null);
			}
		}
		return line;
	}
	
	private Object convertValue(String valueString) {
		Matcher matcher = STRING_VALUE_REGEX.matcher(valueString);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		matcher = DOUBLE_VALUE_REGEX.matcher(valueString);
		if (matcher.matches()) {
			return Double.parseDouble(matcher.group(1));
		}
		matcher = INT_VALUE_REGEX.matcher(valueString);
		if (matcher.matches()) {
			return Long.parseLong(matcher.group(1));
		}
		return new RawValue(valueString);
	}	
}
