package com.github.redsolo.vcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;
import com.github.redsolo.vcm.commands.*;

public class App {
	private static final String PROGRAM_NAME = "vcm-console";

	private static Logger log = Logger.getLogger(App.class);
	
	private List<Command> commands = new ArrayList<Command>();

	public App() {
		commands.add(new GenerateWebCatalogCommand());
		commands.add(new GrepDataCommand());
		commands.add(new ImportFilesCommand());
		commands.add(new CreatePropertyCommand());
		commands.add(new ModifyTagsCommand());
		commands.add(new ModifyKeyWordsCommand());
		commands.add(new ModifyPropertiesCommand());
		commands.add(new ModifyVcidCommand());
		commands.add(new ModifyItemsCommand());
        commands.add(new ModifyRevisionsCommand());
        commands.add(new ModifyLocationCommand());
        commands.add(new VerifyPropertyValuesCommand());
        commands.add(new VerifyPropertyExistsCommand());
        commands.add(new VerifyComponentNameCommand());
		commands.add(new VerifyKeyWordsCommand());
		commands.add(new VerifyPropertyNamesCommand());
		commands.add(new VerifyZeroPositionCommand());
		commands.add(new VerifyPythonCommand());
		commands.add(new VerifyOnDemandLoadIsEnabledCommand());
        commands.add(new SearchTextInResourceCommand());
        commands.add(new ReplaceTextInResourceCommand());
        commands.add(new ReplaceSnippetInResourceCommand());
        commands.add(new BatchUpdateCommand());
        commands.add(new ExportFilesCommand());
        commands.add(new ListNodesCommand());
	}

	public void addCommand(Command command) {
		commands.add(command);
	}

	private Command getCommand(String[] args, JCommander commander, MainConfiguration configuration) {
		commander.setProgramName(PROGRAM_NAME);

		HelpCommand helpCommand = new HelpCommand(commander);
		commander.addCommand(helpCommand.getName(), helpCommand);

		commands.sort(new CommandSorter());
		for (Command command : commands) {
			commander.addCommand(command.getName(), command);
		}
		
		commander.parse(args);

		Logger toolLogger = LogManager.getLogger("com.github.redsolo.vcm");
		if (configuration.isQuiet()) {
			toolLogger.setLevel(Level.WARN);
		} else if (configuration.isVeryVerbose()) {
            toolLogger.setLevel(Level.ALL);
        } else if (configuration.isVerbose()) {
			toolLogger.setLevel(Level.DEBUG);
		} 
		
		if (StringUtils.isBlank(commander.getParsedCommand())) {
			throw new ParameterException("No command specified");
		} else {
			for (Command command : commands) {
				if (command.getName().equals(commander.getParsedCommand())) {
					return command;
				}
			}
		}
		throw new ParameterException("No command specified");
	}

	public int parseCommands(String[] args) {
		MainConfiguration configuration = new MainConfiguration();
		JCommander commander = new JCommander(configuration);
		commands.add(new HelpCommand(commander));
		try {
			Command command = getCommand(args, commander, configuration);
			return command.execute(configuration);
		} catch (MissingCommandException e) {
			log.info(e.getMessage());
			log.info("");
			log.info(String.format("Available commands are: %s.", StringUtils.join(commander.getCommands().keySet(), ", ")));
			return -1;
		} catch (ParameterException e) {
			log.info(e.getMessage());
			log.info("");
			if (StringUtils.isBlank(commander.getParsedCommand())) {
				usage(commander);
			} else {
				commander.usage(commander.getParsedCommand());
			}
			return -1;
		} catch (CommandExecutionException e) {
			if (configuration.isVerbose()) {
				log.error("Command raised an exception.", e);
			} else {
				log.error(e.getMessage());
			}
			return e.getResult();
		} catch (Exception e) {
			if (configuration.isVerbose()) {
				log.error("Command raised an exception.", e);
			} else {
				log.error(StringUtils.defaultString(e.getMessage(), String.format("Unknown error ('%s')", e.getClass().getSimpleName())));
			}
			return -1;
		}
	}
	
	private void usage(JCommander commander) {
		int longestCommandNameLength = 0;
		for (Command command : commands) {
			longestCommandNameLength = Math.max(longestCommandNameLength, command.getName().length()); 
		}
		longestCommandNameLength += 2;
		
		
		log.info(String.format("Usage: %s [options] [command] [command options]", PROGRAM_NAME));
		log.info("  Options:");
		for (ParameterDescription parameter : commander.getParameters()) {
			log.info(String.format("    %s %s (Default: %s)", StringUtils.rightPad(parameter.getNames(), longestCommandNameLength), parameter.getDescription(), parameter.getDefault()));
		}
		log.info("\n  Commands:");
		for (Command command : commands) {
			String commandName = command.getName();
			log.info(String.format("    %s %s", StringUtils.rightPad(commandName, longestCommandNameLength), StringUtils.defaultString(commander.getCommandDescription(commandName))));
		}
		log.info("");
	}
	
	public static void main(String[] args) {
		System.exit(new App().parseCommands(args));
	}

	private static class CommandSorter implements Comparator<Command> {
		@Override
		public int compare(Command arg0, Command arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	}
}
