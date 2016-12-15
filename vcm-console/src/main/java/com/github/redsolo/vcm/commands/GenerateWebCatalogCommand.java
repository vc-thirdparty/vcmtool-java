package com.github.redsolo.vcm.commands;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.ToolManager;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.util.TargaReader;

@Parameters(commandDescription = "Generates the eCat for 2012 and 2014")
public class GenerateWebCatalogCommand extends DirectoryWalker<VcmFileData> implements Command {
	private static Logger log = Logger.getLogger(GenerateWebCatalogCommand.class);

	@Parameter(description="component path (default is current path)", names={"-i", "--inputPath"})
	private String componentRootPath = System.getProperty("user.dir");

	@Parameter(description="output path (default is current path)", names={"-o", "--outputPath"})
	private String outputRootPath = System.getProperty("user.dir");

	@Parameter(description="velocity template path (comma separated list)", names={"--template-paths"})
	private String velocityrTemplateDirectory = System.getProperty("user.dir");

	@Parameter(description="velocity index.html template filename (in template folder)", names={"--template-html"})
	private String templatePath;

	@Parameter(description="velocity components.xml template filename (in template folder)", names={"--template-components"})
	private String componentsTemplatePath;

	@Parameter(description="velocity sourcelist.xml template filename (in template folder)", names={"--template-sourcelist"})
	private String sourcelistTemplatePath;

	@Parameter(description="thumbnail width", names={"-tw", "--thumbnailWidth"})
	private int thumbnailWidth = 96;
	
	@Parameter(description="eCat name", names={"-en", "--ecat-name"})
	private String eCatName = "";
	@Parameter(description="eCat description", names={"-ed", "--ecat-description"})
	private String eCatDescription = "";
    @Parameter(description="eCat author", names={"-ea", "--ecat-author"})
    private String eCatAuthor = "";
    @Parameter(description="eCat url", names={"-eu", "--ecat-url"})
    private String eCatUrl = "";
    @Parameter(description="eCat guid", names={"-eg", "--ecat-guid"})
    private String eCatGuid = "";
	
	@Parameter(description="base url", names={"-u", "--baseURL"})
	private String baseUrl = "";
	
	private File outputRootFile;
	private Template velocityTemplate;
	private Template velocityComponentsTemplate;
	private Template velocitySourceListTemplate;

	private ToolManager velocityToolManager;

	public GenerateWebCatalogCommand() {
		super(FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE), -1);
	}
	
	@Override
	public String getName() {
		return "generateweb";
	}
	
	@Override
	public int execute(MainConfiguration configuration) {
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath,file");
		velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityEngine.setProperty("file.resource.loader.class", org.apache.velocity.runtime.resource.loader.FileResourceLoader.class.getName());
		velocityEngine.setProperty("file.resource.loader.cache", false);
		velocityEngine.setProperty("file.resource.loader.path", velocityrTemplateDirectory);

		velocityEngine.init();

		velocityToolManager = new ToolManager();
		velocityToolManager.configure("velocity-tools.xml");
		
		if (templatePath == null) {
			velocityTemplate = velocityEngine.getTemplate("webpage/index.vm" );
		} else {
			velocityTemplate = velocityEngine.getTemplate(templatePath);
		}

		if (componentsTemplatePath == null) {
			velocityComponentsTemplate = velocityEngine.getTemplate("xmlpage/components.xml");
		} else {
			velocityComponentsTemplate = velocityEngine.getTemplate(componentsTemplatePath);
		}

		if (sourcelistTemplatePath == null) {
			velocitySourceListTemplate = velocityEngine.getTemplate("xmlpage/sourcelist.xml");
		} else {
			velocitySourceListTemplate = velocityEngine.getTemplate(sourcelistTemplatePath);
		}

		List<VcmFileData> result = new ArrayList<VcmFileData>();
		try {
			outputRootFile = new File(outputRootPath);
			createWebStructure(result);
			createComponentsFile(result);
			createSourceListFile(result);
		} catch (IOException e) {
			throw new CommandExecutionException(-1, e);
		}

		return 0;
	}
	
	private void createWebStructure(List<VcmFileData> result) throws IOException {
		log.debug("Copying resource files");
		FileUtils.copyURLToFile(ClassLoader.getSystemResource("webpage/style.css"), new File(outputRootPath, "style.css"));
		FileUtils.copyURLToFile(ClassLoader.getSystemResource("webpage/up.jpg"), new File(outputRootPath, "up.jpg"));
		FileUtils.copyURLToFile(ClassLoader.getSystemResource("webpage/folder.jpg"), new File(outputRootPath, "folder.jpg"));
				
		walk(new File(componentRootPath), result);
	}
	
	private void createComponentsFile(List<VcmFileData> result) throws UnsupportedEncodingException, FileNotFoundException {
		log.debug("Creating components.xml");
		VelocityContext velocityContext = new VelocityContext(velocityToolManager.createContext());
		velocityContext.put("files", result);
        velocityContext.put("baseURL", baseUrl);	        
        velocityContext.put("author", eCatAuthor);        
        velocityContext.put("ecaturl", eCatUrl);	
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(new File(outputRootPath, "components.xml")), "UTF-8");
			velocityComponentsTemplate.merge(velocityContext, writer);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
	
	private void createSourceListFile(List<VcmFileData> result) throws IOException {
		log.debug("Creating sourcelist.xml");
		long totalSize = 0;
		for (VcmFileData vcmFileData : result) {
			totalSize += vcmFileData.getFileSize();
		}
		VelocityContext velocityContext = new VelocityContext(velocityToolManager.createContext());
		velocityContext.put("totalSize", totalSize);
        velocityContext.put("baseURL", baseUrl);		
        velocityContext.put("name", eCatName);		
        velocityContext.put("description", eCatDescription);        
        velocityContext.put("author", eCatAuthor);        
        velocityContext.put("ecaturl", eCatUrl);      
        velocityContext.put("guid", eCatGuid);
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(new File(outputRootPath, "sourcelist.xml")), "UTF-8");
			velocitySourceListTemplate.merge(velocityContext, writer);
		} finally {
			IOUtils.closeQuietly(writer);
		}		
	}
	
	@Override
	protected boolean handleDirectory(File directory, int depth, Collection<VcmFileData> results) throws IOException {
		try {
			if (directory.getCanonicalPath().equals(outputRootFile.getCanonicalPath())) {
				log.debug(String.format("Ignoring %s", directory.getCanonicalPath()));
				return false;
			}
			
			log.info(String.format("Indexing %s", directory.getCanonicalPath()));
			String relativeParentPaths = StringUtils.remove(directory.getCanonicalPath(), new File(componentRootPath).getCanonicalPath());
			String outputPath = outputRootPath + relativeParentPaths;
			File outputDirectory = new File(outputPath);
			if (!outputDirectory.exists()) {
				if (!outputDirectory.mkdir()) {
					throw new CommandExecutionException(2, String.format("Could not create the %s directory", outputDirectory));
				}
			}
			
			VelocityContext velocityContext = new VelocityContext(velocityToolManager.createContext());
	        velocityContext.put("baseURL", baseUrl);
	        velocityContext.put("folders", directory.listFiles((FileFilter) FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE)));
			velocityContext.put("parentFolders", getParentFolders(relativeParentPaths));
			List<VcmFileData> files = getFiles(directory, outputPath);
			velocityContext.put("files", files);
			if (depth == 0) {
				velocityContext.put("rootPath", ".");
			} else {
				velocityContext.put("rootPath", StringUtils.repeat("../", depth));
			}
			velocityContext.put("depth", depth);
	        
			OutputStreamWriter writer = null;
			try {
				writer = new OutputStreamWriter(new FileOutputStream(new File(outputDirectory, "index.htm")), "UTF-8");
		        velocityTemplate.merge(velocityContext, writer);
			} finally {
				IOUtils.closeQuietly(writer);
			}
	        
	        results.addAll(files);
			return true;
		} catch (ZipException e) {
			throw new CommandExecutionException(3, e);
		}

	}
	
	private List<ParentFolder> getParentFolders(String relativeParentPaths) {
		ArrayList<ParentFolder> list = new ArrayList<GenerateWebCatalogCommand.ParentFolder>();
		String[] parentFolderPaths = StringUtils.split(relativeParentPaths, File.separator);
		parentFolderPaths = ArrayUtils.add(parentFolderPaths, 0, "Home");
		for (int i = 0; i < parentFolderPaths.length - 1; i++) {
			list.add(new ParentFolder(parentFolderPaths[i], StringUtils.repeat("../", parentFolderPaths.length - i - 1)));
		}
		list.add(new ParentFolder(parentFolderPaths[parentFolderPaths.length - 1], "."));
		return list;
	}

	private List<VcmFileData> getFiles(File directory, String outputPath) throws IOException, ZipException {
		ArrayList<VcmFileData> list = new ArrayList<VcmFileData>();
		for (File file : directory.listFiles((FileFilter)
				FileFilterUtils.or(
						FileFilterUtils.suffixFileFilter("vcm", IOCase.INSENSITIVE),
						FileFilterUtils.suffixFileFilter("vcmx", IOCase.INSENSITIVE)))) {
			log.debug(String.format("Opening %s", file.getCanonicalPath()));
			File destFile = new File(outputPath, file.getName());
			String relativePath = destFile.getCanonicalPath().substring(new File(outputRootPath).getCanonicalPath().length() + 1);
			relativePath = StringUtils.replace(FilenameUtils.getPath(relativePath), "\\", "/");
			VcmFileData vcmFile = new VcmFileData(file, relativePath);
			list.add(vcmFile);
			log.debug(String.format("Generating thumbnail for %s", file.getCanonicalPath()));
			createThumbnail(vcmFile.getModel(), new File(outputPath, vcmFile.getThumbnailName()));
			log.debug(String.format("Copying %s", file.getCanonicalPath()));
			FileUtils.copyFile(file, destFile);
		}
		return list;
	}
	
	private void createThumbnail(Model model, File thumbnailFile) throws IOException, ZipException {
		InputStream thumbnailStream = null;
		try {
			thumbnailStream = model.getThumbnail();
			Image sourceImage = TargaReader.decode(IOUtils.toByteArray(thumbnailStream));
			Image thumbnail = sourceImage.getScaledInstance(thumbnailWidth, -1, Image.SCALE_SMOOTH);
			BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
			                                                    thumbnail.getHeight(null),
			                                                    BufferedImage.TYPE_INT_ARGB);
			bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
			ImageIO.write(bufferedThumbnail, "png", thumbnailFile);
		} finally {
			IOUtils.closeQuietly(thumbnailStream);
		}
	}

	/**
	 * Data class used by Velocity template
	 */
	public static class ParentFolder {
		private final String name;
		private final String path;
		public ParentFolder(String name, String path) {
			this.name = name;
			this.path = path;
		}
		public String getName() {
			return name;
		}
		public String getPath() {
			return path;
		}
	}
}
