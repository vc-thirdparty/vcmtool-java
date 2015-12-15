package com.github.redsolo.vcm.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.ComponentData;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

@Parameters(commandDescription = "Batch updates several files using a CSV file.")
public class BatchUpdateCommand implements Command {
    private static Logger log = Logger.getLogger(BatchUpdateCommand.class);

    private Pattern headerPattern = Pattern.compile(".+\\[(.+)\\]");
    
    @Parameter(description="component path (default is current path)", names={"-i", "--inputPath"})
    private String componentRootPath = System.getProperty("user.dir");
    
    @Parameter(description="dry run (do not update)", names={"-d", "--dryRun"})
    private boolean dryRun = false;
    
    @Parameter(description="ignore missing files", names={"--ignoreMissing"})
    private boolean ignoreMssingFiles = false;
    
    @Parameter(description="ignore read only files", names={"--ignoreReadOnly"})
    private boolean ignoreReadOnly = false;

    @Parameter(description = "[batch update files]", required=true) 
    private List<String> batchFiles;
    
    @Parameter(description="skip revision and last modified updating (true = revision and last modified will not be updated)", names={"--skip-revision-update"})
    protected boolean skipRevisionUpdate = false;

    @Override
    public String getName() {
        return "batchupdate";
    }
    
    @Override
    public int execute(MainConfiguration mainConfiguration) {
        
        try {
            // Validate all files first
            for (String batchFile : batchFiles) {
                log.trace(String.format("Validating batch file '%s'", batchFile));
                List<String> lines = FileUtils.readLines(new File(batchFile));
                
                String[] headers = StringUtils.split(lines.get(0), ";");            
                validateBatchFile(batchFile, lines, headers);
            }
            
            // Batch update all files
            for (String batchFile : batchFiles) {
                log.trace(String.format("Updating using batch file '%s'", batchFile));
                List<String> lines = FileUtils.readLines(new File(batchFile));
                String[] headers = StringUtils.split(lines.get(0), ";");            
                updateModelFiles(lines, ArrayUtils.remove(headers, 0));
            }            
        } catch (IOException e) {
            throw new CommandExecutionException(3, e);
        } catch (ZipException e) {
            throw new CommandExecutionException(2, e);
        }
        
        return 0;
    }

    private void updateModelFiles(List<String> lines, String[] headers) throws ZipException, IOException {
        for (String line : lines.subList(1, lines.size())) {
            if (!StringUtils.isBlank(line)) {
                String[] values = StringUtils.split(line, ";");
                File modelFile = getModelFile(values[0], dryRun);
                if (modelFile != null) {
                    Model model = new Model(modelFile);
                    log.trace(String.format("Reading component.dat '%s'", modelFile));
                    ComponentData componentData = model.getComponentData();
                    ModelResource resourceData = model.getResourceData();
                    List<String> updatedValues = new Vector<String>();
                    for (int i = 0; i < headers.length; i++) {
                        if (updateModel(headers[i], values[i+1], componentData, resourceData)) {
                            updatedValues.add(String.format("%s='%s'", headers[i], values[i+1]));
                        }
                    }
                    if (updatedValues.size() > 0) {
                        if (!dryRun) {
                            log.info(String.format("Updated '%s' : %s", modelFile, StringUtils.join(updatedValues, ", ")));
                            boolean componentDataWasUpdated = model.setComponentData(componentData, skipRevisionUpdate);
                            boolean resourceDataWasUpdated = model.setResourceData(resourceData, skipRevisionUpdate);
                            if (!skipRevisionUpdate && (componentDataWasUpdated || resourceDataWasUpdated)) {
                                model.stepRevision();
                            }
                        } else {
                            log.info(String.format("DryRun Updated '%s' : %s", modelFile, StringUtils.join(updatedValues, ", ")));
                        }
                    }
                }
            }
        }
    }

    private boolean updateModel(String header, String value, ComponentData componentData, ModelResource resourceData) throws ZipException, IOException {
        Matcher matcher = headerPattern.matcher(header);
        if (header.toLowerCase().startsWith("item")) {
            if (matcher.matches()) {
                if (!componentData.getItems().get(matcher.group(1)).equals(value)) {
                    componentData.getItems().put(matcher.group(1), value);
                    return true;
                }
            }
        } else if (header.toLowerCase().startsWith("keyword")) {
            if (matcher.matches()) {
                if (!componentData.getKeywords().get(matcher.group(1)).equals(value)) {
                    componentData.getKeywords().put(matcher.group(1), value);
                    return true;
                }
            }
            
        } else if (header.toLowerCase().startsWith("var")) {
            if (matcher.matches()) {
                for (ModelResource variable : ResourceDataParser.getVariables(resourceData).getResources()) {
                    if (variable.getValue("Name").equals(matcher.group(1))) {
                        if (!variable.getValue("Value").equals(value)) {
                            variable.setValue("Value", value);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void validateBatchFile(String batchFile, List<String> lines, String[] headers) {
        long headerCount = headers.length;
        for (String header : ArrayUtils.remove(headers, 0)) {
            log.trace(String.format("Validating header '%s'", header));
            if (!header.toLowerCase().startsWith("item[") && !header.toLowerCase().startsWith("keyword[")&& !header.toLowerCase().startsWith("var[")) {
                throw new CommandExecutionException(10, String.format("Unknown header '%s' in %s", header, batchFile));            
            }
            if (!headerPattern.matcher(header).matches()) {
                throw new CommandExecutionException(11, String.format("Invalid header '%s' in %s", header, batchFile));
            }
        }
        for (String line : lines.subList(1, lines.size())) {
            log.trace(String.format("Validating line '%s'", line));
            String[] values = StringUtils.split(line, ";");
            if (values.length != headerCount) {
                throw new CommandExecutionException(4, String.format("The CSV file contains a line with missing data '%s' in %s", line, batchFile));
            }
            File modelFile = getModelFile(values[0], dryRun || ignoreReadOnly);
            if (modelFile == null && !ignoreMssingFiles && !ignoreReadOnly) {
                throw new CommandExecutionException(5, String.format("VCM File '%s' could not be found.", values[0]));
            }
        }
    }

    private File getModelFile(String value, boolean ignoreReadOnly) {
        File modelFile = new File(value);
        if (!modelFile.isAbsolute()) {
            modelFile = FileUtils.getFile(componentRootPath, value);
        }
        if (!modelFile.exists()) {
            return null;
        }
        if (!modelFile.canWrite() && !ignoreReadOnly) {
            return null;
        }
        return modelFile;
    }

    public void setComponentRootPath(String path) {
        componentRootPath = path;
    }

    public void setFile(String path) {
        batchFiles = new Vector<String>();
        batchFiles.add(path);
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
    public void setIgnoreMssingFiles(boolean ignoreMssingFiles) {
        this.ignoreMssingFiles = ignoreMssingFiles;
    }
    public void setIgnoreReadOnly(boolean ignoreReadOnly) {
        this.ignoreReadOnly = ignoreReadOnly;
    }
}
