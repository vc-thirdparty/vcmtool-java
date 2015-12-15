package com.github.redsolo.vcm.commands;


public class CommandExecutionException extends RuntimeException {

	private static final long serialVersionUID = 4463815244486084174L;
	
	private final int result;

	public CommandExecutionException(int result, Exception e) {
		super(e);
		this.result = result;		
	}

	public CommandExecutionException(int result, String message) {
		super(message);
		this.result = result;
	}

	public int getResult() {
		return result;
	}
}
