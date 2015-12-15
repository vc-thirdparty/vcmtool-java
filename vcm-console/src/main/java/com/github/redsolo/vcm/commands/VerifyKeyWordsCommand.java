package com.github.redsolo.vcm.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "Verifies property names does not start or end with space")
public class VerifyKeyWordsCommand extends AbstractVerifyModelCommand implements Command {
	private static Logger log = Logger.getLogger(VerifyKeyWordsCommand.class);

	private Pattern keyValuePattern = Pattern.compile("(.+)=(.*)"); 

	@Parameter(description = "keywords to verify (comma separated key-value list)", names = { "-k", "--keywords" }) 
	private String verifyKeywords;

	private HashMap<String, String> keywordsToVerify;

	@Override
	public String getName() {
		return "verify-keywords";
	}

	@Override
	protected void validateParameters(MainConfiguration mainConfiguration) {
		if (verifyKeywords != null) {
			keywordsToVerify = new HashMap<String, String>();
			for (String keyValueString : StringUtils.split(verifyKeywords, ",")) {
				Matcher matcher = keyValuePattern.matcher(keyValueString);
				if (matcher.matches() && matcher.groupCount() == 2) {
					keywordsToVerify.put(matcher.group(1).toLowerCase(), matcher.group(2));
				} else {
					throw new CommandExecutionException(2, String.format("Can not parse the name and value from '%s'", keyValueString));					
				}
			}
		}
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		ComponentData componentData = model.getComponentData();
		Map<String, String> keywords = componentData.getKeywords();
		for (String keyValue : keywords.keySet()) {
			String key = keyValue.toLowerCase();
			if (keywordsToVerify.containsKey(key)) {
				String expectedValue = keywordsToVerify.get(key);
				String actualValue = keywords.get(keyValue);
				if (! expectedValue.equals(actualValue)) {
					log.warn(String.format("Invalid keyword in model %s for keyword '%s', expected value='%s', actual value='%s'", 
							model.getFile(), key, expectedValue, actualValue));
					setVerificationFailed(true);
				}
			}
		}
	}

	public void setVerifyProperties(String string) {
		this.verifyKeywords = string;
	}
}
