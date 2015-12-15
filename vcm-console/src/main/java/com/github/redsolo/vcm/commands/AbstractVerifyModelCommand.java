package com.github.redsolo.vcm.commands;

public abstract class AbstractVerifyModelCommand extends AbstractModelCollectionCommand {

	private boolean verificationFailed;

	@Override
	public int execute(MainConfiguration mainConfiguration) {
		int value = super.execute(mainConfiguration);
		return (value == 0 ? (hasVerificationFailed() ? 1 : 0) : value);
	}

	public boolean hasVerificationFailed() {
		return verificationFailed;
	}

	public void setVerificationFailed(boolean verificationFailed) {
		this.verificationFailed = verificationFailed;
	}
}
