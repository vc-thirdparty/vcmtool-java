package com.github.redsolo.vcm.commands;

import com.beust.jcommander.Parameter;

public class MainConfiguration {
	@Parameter(names = { "-v" }, description = "verbose logging")
	private boolean verbose;

	@Parameter(names = { "-vv" }, description = "very verbose logging")
	private boolean veryVerbose;
	
	@Parameter(names = { "-q" }, description = "quiet")
	private boolean quiet;
	
	public boolean isQuiet() {
		return quiet;
	}
	
	public boolean isVerbose() {
		return !quiet && (verbose || veryVerbose);
	}
	
	public boolean isVeryVerbose() {
		return !quiet && veryVerbose;
	}

	@Override
	public String toString() {
		return String.format("MainConfiguration [verbose=%s, quiet=%s, veryVerbose=%s]", verbose, quiet, veryVerbose);
	}
}
