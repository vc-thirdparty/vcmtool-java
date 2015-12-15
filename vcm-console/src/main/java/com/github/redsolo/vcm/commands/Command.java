package com.github.redsolo.vcm.commands;


public interface Command {
	String getName();
	int execute(MainConfiguration mainConfiguration);
}
