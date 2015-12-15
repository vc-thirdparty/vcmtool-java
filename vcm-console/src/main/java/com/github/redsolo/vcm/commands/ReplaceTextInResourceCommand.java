package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.util.LineReader;

@Parameters(commandDescription = "Replaces any text in resource tree (component.rsc)")
public class ReplaceTextInResourceCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(ReplaceTextInResourceCommand.class);
	private Map<String, String> replacementMap;

	@Parameter(description = "replacement text (comma separated list of key=value pairs)", names = { "-r", "--replace" }, required=true) 
	private List<String> replacementtext;

	@Parameter(description = "only search and display found texst", names = { "-s", "--search-only"}) 
	private boolean searchOnly = false;
    
	@Override
	public String getName() {
		return "replace";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		replacementMap = new HashMap<String, String>();
		for (String keyValuePair : replacementtext) {
			String[] string = StringUtils.split(keyValuePair, "=");
			if (string.length != 2) {
				throw new ParameterException("Incorrect key value pair");
			}
			replacementMap.put(string[0], string[1]);
		}
	}
	
	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		log.debug("Reading from " + model.getFile());
		InputStream inputStream = model.getInputStream(Model.COMPONENT_RSC);
		try {
			List<String> lines = readLinesFromModel(inputStream);
			if (searchOnly) {
				displayLines(lines);
			} else {
			    if (modelNeedsToBeUpdated(lines)) {
			        writeLinesToModel(model, lines);
			        if (!skipRevisionUpdate) {
			            model.stepRevision();
			        }
			    }
			}
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	private List<String> readLinesFromModel(InputStream inputStream) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		LineReader reader = new LineReader(new InputStreamReader(inputStream, "US-ASCII"));
		String line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		inputStream.close();
		return lines;
	}
	
	private boolean modelNeedsToBeUpdated(List<String> lines) {
        for (String line : lines) {
            if (!line.equals(extracted(line))) {
                return true;
            }
        }
        return false;
    }
	
	private void displayLines(List<String> lines) {
		for (String line : lines) {
			extracted(line);
		}
	}

	private void writeLinesToModel(Model model, List<String> lines) throws ZipException, IOException {
		OutputStream outputStream = model.getOutputStream(Model.COMPONENT_RSC);
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(outputStream, "US-ASCII");
			for (String line : lines) {
				line = extracted(line);
		
				writer.write(line);
				writer.write('\n');
			}
			writer.flush();
			model.refresh();
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(outputStream);
		}
	}

	private String extracted(String line) {
		for (String key : replacementMap.keySet()) {
			if (line.contains(key)) {
                String replacedString = StringUtils.replace(line, key, replacementMap.get(key));
				log.debug(String.format("Replacing '%s' in line '%s' to '%s'", key, line, replacedString));
                line = replacedString;						
			}
		}
		return line;
	}

	public List<String> getReplacementtext() {
		return replacementtext;
	}

	public void setReplacementtext(List<String> replacementtext) {
		this.replacementtext = replacementtext;
	}
}
