package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.util.LineReader;

@Parameters(commandDescription = "Searches for any text in resource tree (component.rsc)")
public class SearchTextInResourceCommand extends AbstractModelCollectionCommand {
	private static Logger log = Logger.getLogger(SearchTextInResourceCommand.class);

    @Parameter(description = "only search and display found text", names = { "-s", "--search-only"}, required=true) 
    private List<String> searchtext;

    @Parameter(description = "prints the name of the file with the match", names = { "-l", "--file-list-only"}) 
    private boolean displayFilenamesOnly = false;
    
    @Parameter(description = "print a match count of matching lines for each file", names = { "-c", "--count"}) 
    private boolean countMatches = false;
    
	@Override
	public String getName() {
		return "search";
	}
	
	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		log.debug("Reading from " + model.getFile());
		InputStream inputStream = model.getInputStream(Model.COMPONENT_RSC);
		try {
			displayLines(readLinesFromModel(inputStream), getRelativePath(model.getFile()));
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

	private void displayLines(List<String> lines, String filename) {
	    List<String> matchingLines = new ArrayList<String>();
		for (String line : lines) {
		    for (String key : searchtext) {
	            if (line.contains(key)) {
	                matchingLines.add(line);
	            }
	        }
		}
		if (matchingLines.size() > 0) {
		    if (displayFilenamesOnly) {
		        log.info(String.format("%s", filename));
		    } else if (countMatches) {
                log.info(String.format("%s, %d", filename, matchingLines.size()));
            } else {
                for (String line : matchingLines) {
                    log.info(String.format("%s - '%s'", filename, line));
                }
            }
        } 
	}
}
