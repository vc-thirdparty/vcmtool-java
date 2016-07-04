package com.github.redsolo.vcm.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

public abstract class AbstractModelCollectionCommand implements Command {
	private static Logger log = Logger.getLogger(AbstractModelCollectionCommand.class);

	@Parameter(description = "recursive", names = {"-R"})
	private boolean recursive = false;

	@Parameter(description = "wildcards (defaults to *.vcm)")
	private List<String> wildcards;

	@Parameter(description = "component path (default is current path)", names = {"-i", "--inputPath"})
	private String componentRootPath = System.getProperty("user.dir");

	@Parameter(description = "exlude files using regular expression (supports multiple arguments)", names = {"-e", "--excludeRegex"})
	private List<String> excludeRegexStrings;
	private List<Pattern> excludeRegexs;

	@Parameter(description = "include layout VCM files", names = {"--include-layouts"})
	protected boolean includeLayouts = false;

	@Parameter(description = "exclude component VCM files", names = {"--exclude-components"})
	protected boolean excludeComponents = false;

	@Parameter(description = "exclude read only files", names = {"-ro", "--exclude-readonly"})
	private boolean excludeReadOnly = false;

	@Parameter(description = "skip revision and last modified updating (true = revision and last modified will not be updated)", names = {"--skip-revision-update"})
	protected boolean skipRevisionUpdate = false;

	protected MainConfiguration mainConfiguration;

	protected Collection<Model> getModels() throws ZipException {
		IOFileFilter fileFilter;
		if (wildcards == null) {
			fileFilter = new WildcardFileFilter("*.vcm", IOCase.INSENSITIVE);
		} else {
			ArrayList<IOFileFilter> fileFilters = new ArrayList<IOFileFilter>();
			for (String filename : wildcards) {
				fileFilters.add(new WildcardFileFilter(filename, IOCase.INSENSITIVE));
			}
			fileFilter = FileFilterUtils.or(fileFilters.toArray(new IOFileFilter[fileFilters.size()]));
		}

		IOFileFilter directoryFilter = recursive ? TrueFileFilter.INSTANCE : null;

		Collection<Model> models = new ArrayList<Model>();
		for (File file : FileUtils.listFiles(new File(componentRootPath), fileFilter, directoryFilter)) {
			Pattern matchingRegex = null;
			for (Pattern regex : excludeRegexs) {
				if (regex.matcher(file.getPath()).find()) {
					matchingRegex = regex;
					break;
				}
			}
			if (matchingRegex == null) {
				models.add(new Model(file));
			} else {
				log.trace(String.format("Ignoring %s as it matches regex '%s'", file.getAbsolutePath(), matchingRegex.pattern()));
			}
		}
		return models;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public void setWildcards(List<String> wildcards) {
		this.wildcards = wildcards;
	}

	public void setComponentRootPath(String componentRootPath) {
		this.componentRootPath = componentRootPath;
	}

	protected void postProcess() {
	}

	protected void preProcess() {
	}

	protected abstract void executeModel(Model model) throws IOException, ZipException;

	protected void executeModels(Collection<Model> models) throws IOException, ZipException {
		preProcess();
		for (Model model : models) {
			if (excludeReadOnly && !model.getFile().canWrite()) {
				log.trace(String.format("Ignoring %s as it is read only", model.getFile().getAbsolutePath()));
			} else {
				if ((model.isLayout() && includeLayouts) || (model.isComponent() && !excludeComponents)) {
					log.trace(String.format("Processing %s", model.getFile().getAbsolutePath()));
					executeModel(model);
				} else {
					log.trace(String.format("Ignoring %s as it is a %s", model.getFile().getAbsolutePath(), (model.isLayout() ? "layout" : "component")));
				}
			}
		}
		postProcess();
	}

	@Override
	public int execute(MainConfiguration mainConfiguration) {
		this.mainConfiguration = mainConfiguration;
		excludeRegexs = new ArrayList<Pattern>();
		if (excludeRegexStrings != null && excludeRegexStrings.size() > 0) {
			for (String regexString : excludeRegexStrings) {
				excludeRegexs.add(Pattern.compile(regexString, Pattern.CASE_INSENSITIVE));
			}
		}
		validateParameters(mainConfiguration);
		try {
			executeModels(getModels());
		} catch (ZipException e) {
			throw new CommandExecutionException(3, e);
		} catch (IOException e) {
			throw new CommandExecutionException(4, e);
		}
		return 0;
	}

	protected void validateParameters(MainConfiguration mainConfiguration) {
	}

	public String getComponentRootPath() {
		return componentRootPath;
	}

	protected String getValueType(String type) {
		if (type.equalsIgnoreCase("string")) {
			return "rString";
		}
		if (type.equalsIgnoreCase("double")) {
			return "rDouble";
		}
		if (type.equalsIgnoreCase("bool")) {
			return "rBool";
		}
		if (type.equalsIgnoreCase("int")) {
			return "rInt";
		}
		throw new CommandExecutionException(5, "Unknown property type");
	}

	protected Object getValueInCorrectType(ModelResource variable, String value) throws ZipException, IOException {
		if (variable.getName().contains("rBool")) {
			return Boolean.parseBoolean(value) ? 1 : 0;
		}
		if (variable.getName().contains("rInt")) {
			return Integer.parseInt(value);
		}
		if (variable.getName().contains("rDouble")) {
			return Double.parseDouble(value);
		}
		return value;
	}

	protected int getNextGroup(ModelResource variables) {
		int highestGroupIndex = 0;
		for (ModelResource variable : variables.getResources()) {
			Object groupValue = variable.getValue("Group");
			if (groupValue != null) {
				highestGroupIndex = Math.max(highestGroupIndex, Integer.parseInt(groupValue.toString()));
			}
		}

		return highestGroupIndex + 1;
	}

	protected String getRelativePath(File file) {
		return String.format(".%s", StringUtils.remove(file.getAbsolutePath(), new File(componentRootPath).getAbsolutePath()));
	}
}
