package com.github.redsolo.vcm.commands;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Displays usage for a command")
public class HelpCommand implements Command {
	private static Logger log = Logger.getLogger(HelpCommand.class);

	private JCommander commander;

	@Parameter(description = "command_name", required=true, arity=1)
	private List<String> commandNames;

	public HelpCommand(JCommander commander) {
		this.commander = commander;
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public int execute(MainConfiguration mainConfiguration) {
		int retCode = 0;
		StringBuilder builder = new StringBuilder();
		if (commander.getCommands().containsKey(commandNames.get(0))) {
			commander.usage(commandNames.get(0), builder);
		} else {
			retCode = -1;
			builder.append("Unknown command, available commands are: ");
			builder.append(StringUtils.join(commander.getCommands().keySet(), ", "));
			builder.append("\n");
		}
		log.info(builder.toString());
		return retCode;
	}
}
