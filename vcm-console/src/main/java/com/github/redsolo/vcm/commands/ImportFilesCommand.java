package com.github.redsolo.vcm.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;

@Parameters(commandDescription = "Import files into VCM")
public class ImportFilesCommand implements Command {

	private static Logger log = Logger.getLogger(ImportFilesCommand.class);

	@Parameter(description = "strip from file name", names = { "--strip" }) 
	private String stripFromFilename;

	@Parameter(description = "vcm_file [files...]", required=true, variableArity=true)
	private List<String> filenames;
	
	@Override
	public String getName() {
		return "import";
	}

	@Override
	public int execute(MainConfiguration mainConfiguration) {
		try {
			Model model = new Model(filenames.get(0));			
			log.debug(String.format("Target model '%s'", model.getFile()));
			for (int i = 1; i < filenames.size(); i++) {
				File inputFile = new File(filenames.get(i));
				InputStream input = null;
				OutputStream output = null;
				try {
					String sourceFilename = inputFile.getName();
					String destinationFilename = StringUtils.remove(sourceFilename, stripFromFilename);
					log.debug(String.format("Importing '%s' as '%s'", sourceFilename, destinationFilename));
					input = new FileInputStream(inputFile);
					output = model.getOutputStream(destinationFilename);
					IOUtils.copy(input, output);
				} finally {
					IOUtils.closeQuietly(input);
					IOUtils.closeQuietly(output);
				}
			}			
		} catch (IOException e) {
			throw new CommandExecutionException(3, e);
		} catch (ZipException e) {
			throw new CommandExecutionException(5, e);
		}
		
		return 0;
	}

	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}

	public void setStrip(String stripFromFilename) {
		this.stripFromFilename = stripFromFilename;
	}
}
