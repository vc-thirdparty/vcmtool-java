package com.github.redsolo.vcm.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.ModelResourceParser;
import com.github.redsolo.vcm.ModelResourceWriter;

@Parameters(commandDescription = "Export files from VCM")
public class ExportFilesCommand implements Command {
    private static Logger log = Logger.getLogger(ExportFilesCommand.class);

    private ModelResourceParser parser = new ModelResourceParser();
    private ModelResourceWriter writer = new ModelResourceWriter();

    @Parameter(description = "prefix to file name", names = { "--prefix" }) 
    private String prefixToFilename = "";
    
    @Parameter(description = "indent resource files (file needs to be text and a resource)", names = { "-i", "--indent" }) 
    private boolean indentResource;

    @Parameter(description = "vcm_file [files...]", required=true, variableArity=true)
    private List<String> filenames;

    @Parameter(description = "output folder", names = { "-o", "--output" }) 
    private String outputFolder = System.getProperty("user.dir");

    @Override
    public String getName() {
        return "export";
    }

    @Override
    public int execute(MainConfiguration mainConfiguration) {
        try {
            Model model = new Model(filenames.get(0));          
            log.debug(String.format("Target model '%s'", model.getFile()));
            for (int i = 1; i < filenames.size(); i++) {
                
                File outputFile = new File(outputFolder, String.format("%s%s", prefixToFilename, filenames.get(i)));
                InputStream input = null;
                OutputStream output = null;
                try {

                    input = model.getInputStream(filenames.get(i));
                    output = new FileOutputStream(outputFile);
                    log.debug(String.format("Exporting '%s' as '%s'", filenames.get(i), outputFile));
                    IOUtils.copy(input, output);
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                }
                
                if (indentResource && 
                        (StringUtils.endsWithIgnoreCase(filenames.get(i), ".rsc") ||
                         StringUtils.endsWithIgnoreCase(filenames.get(i), ".dat"))) {
                    String resourceText = FileUtils.readFileToString(outputFile);
                    ModelResource fileResource = parser.parse(resourceText);
                    Writer fileWriter = null;
                    try {
                        fileWriter = new FileWriter(outputFile);
                        writer.write(fileResource, fileWriter);
                    } finally {
                        IOUtils.closeQuietly(fileWriter);
                    }
                }
            }           
        } catch (IOException e) {
            throw new CommandExecutionException(3, e);
        } catch (ZipException e) {
            throw new CommandExecutionException(5, e);
        }
        
        return 0;
    }

}
