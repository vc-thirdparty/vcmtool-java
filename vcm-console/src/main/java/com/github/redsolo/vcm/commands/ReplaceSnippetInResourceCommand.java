package com.github.redsolo.vcm.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

import net.lingala.zip4j.exception.ZipException;

@Parameters(commandDescription = "Replaces text snippets in python scripts (component.rsc)")
public class ReplaceSnippetInResourceCommand  extends AbstractModelCollectionCommand {
    private static Logger log = Logger.getLogger(ReplaceSnippetInResourceCommand.class);
    private Pattern startPythonPattern = Pattern.compile(".*PythonSnippet Start\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
    private Pattern stopPythonPattern = Pattern.compile(".*PythonSnippet Stop", Pattern.CASE_INSENSITIVE);
    
    @DynamicParameter(names = {"-s", "--snippet"}, description = "Multiple snippets can be defined as this [snippet name]=[file path]. Example: -s CodeOne=snippet.txt, will replace snippet 'CodeOne' with the contents from the 'snippet.txt'")
    private HashMap<String, String> snippetsParameter = new HashMap<String, String>();
    private HashMap<String, File> snippets = new HashMap<String, File>(); 
    
    @Override
    public String getName() {
        return "snippet";
    }

    @Override
    protected void validateParameters(MainConfiguration mainConfiguration) {
        for (String key : snippetsParameter.keySet()) {
            File file = new File(snippetsParameter.get(key));
            if (!file.exists()) {
                throw new CommandExecutionException(2, String.format("Can not find the file for snippet code '%s'", key));
            }
            addSnippet(key.toLowerCase(), file);
        }
    }

    @Override
    protected void executeModel(Model model) throws IOException, ZipException {
        boolean modelNeedsToBeUpdated = false;
        ModelResource resourceData = model.getResourceData();
        for (ModelResource pythonResource : ResourceDataParser.getPythonScripts(resourceData)) {
            String originalScript = (String) pythonResource.getValue("Script");
            String newScript = insertSnippets(originalScript, startPythonPattern, stopPythonPattern);
            pythonResource.setValue("Script", newScript);
            modelNeedsToBeUpdated |= !StringUtils.equals(originalScript, newScript);
        }
        if (modelNeedsToBeUpdated) {
            log.info(String.format("Snippet(s) replaced in file '%s'", model.getFile()));
            model.setResourceData(resourceData, !skipRevisionUpdate);
            model.refresh();
        }
    }

    private String insertSnippets(String value, Pattern startPattern, Pattern stopPattern) throws IOException {
        StringBuilder builder = new StringBuilder();
        boolean isInsideSnippet = false;
        for (String line : StringUtils.split(value, '\r')) {
            Matcher match = startPattern.matcher(line);
            if (match.matches()) {
                builder.append(line).append('\r');
                String snippetName = match.group(1);
                File snippetFile = snippets.get(snippetName.toLowerCase());
                if (snippetFile != null) {
                    isInsideSnippet = true;                    
                    builder.append("\\n");
                    builder.append(readSnippet(snippetFile));
                    builder.append("\r");
                    log.debug(String.format("Replacing snippet '%s' with content from '%s'", snippetName, snippetFile));
                } else {
                    log.debug(String.format("Ignoring snippet '%s'", snippetName));                    
                }
            } else if (stopPattern.matcher(line).matches()) {
                builder.append(line).append('\r');
                isInsideSnippet = false;
            } else if (!isInsideSnippet) {
                builder.append(line).append('\r');
            }
        }
        return builder.toString().trim();
    }

    private String readSnippet(File snippetFile) throws IOException {
        return StringUtils.replace(StringUtils.replace(StringUtils.trim(FileUtils.readFileToString(snippetFile)), "\n", "\\n"),"\"", "\\\"");
    }

    void addSnippet(String key, File snippetFile) {
       snippets.put(key.toLowerCase(), snippetFile);
    }

    public HashMap<String, String> getSnippetsParameter() {
        return snippetsParameter;
    }

    public void setSnippetsParameter(HashMap<String, String> snippetsParameter) {
        this.snippetsParameter = snippetsParameter;
    }
}
