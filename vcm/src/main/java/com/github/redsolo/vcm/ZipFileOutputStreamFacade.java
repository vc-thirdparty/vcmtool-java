package com.github.redsolo.vcm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.github.redsolo.vcm.util.VcmFileUpdater;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

class ZipFileOutputStreamFacade extends OutputStream {
	private final String filename;
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final ZipFile zipFile;
	
	public ZipFileOutputStreamFacade(ZipFile zipFile, String filename) {
		this.zipFile = zipFile;
		this.filename = filename;
	}

	@Override
	public void write(int arg0) throws IOException {
		outputStream.write(arg0);
	}

	@Override
	public void close() throws IOException {
		outputStream.flush();
		VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(zipFile.getFile());
		vcmFileUpdater.addFile(filename, outputStream.toByteArray());
		try {
			vcmFileUpdater.update(false);
		} catch (ZipException e) {
			throw new RuntimeException(String.format("Error when adding/updating '%s' in VCM file '%s'", filename, zipFile.getFile()), e);
		}
	}

	@Override
	public void flush() throws IOException {
		outputStream.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputStream.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		outputStream.write(b);
	}
}