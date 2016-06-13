package com.github.redsolo.vcm.commands;

import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

@Parameters(commandDescription = "Verifies python scripts conforms to standards (space instead of tab, etc)")
public class VerifyPythonCommand extends AbstractVerifyModelCommand implements Command {
	private static Logger log = Logger.getLogger(VerifyKeyWordsCommand.class);

	@Override
	public String getName() {
		return "verify-python";
	}

	@Override
	protected void executeModel(Model model) throws IOException, ZipException {
		for (ModelResource pythonResource : ResourceDataParser.getPythonScripts(model.getResourceData())) {
			String originalScript = (String) pythonResource.getValue("Script");
			int rowIndex=1;
			for (String line : StringUtils.splitByWholeSeparator(originalScript, "\\n")) {
				if (StringUtils.startsWith(StringUtils.remove(line, ' '), "\t")) {
					log.warn(String.format("Tab char is detected in indentation at line %d of script '%s' in file %s", rowIndex, pythonResource.getValue("Name"), model.getFile()));
					setVerificationFailed(true);
				}
				/*System.out.println("line='"+line +"'");
				Matcher matcher = indentRegex.matcher(line);
				if (matcher.matches()) {
					System.out.println("matched");
					if (matcher.group(1).contains("\t")) {
						log.warn(String.format("Tab char is detected in indentation at line %d of script '%s' in file %s", rowIndex, pythonResource.getValue("Name"), model.getFile()));
						setVerificationFailed(true);
					}
				}*/
				rowIndex++;
			}
		}
	}
}
