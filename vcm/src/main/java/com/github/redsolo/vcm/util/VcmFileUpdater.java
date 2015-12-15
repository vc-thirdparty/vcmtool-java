package com.github.redsolo.vcm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Zip file updater that will create a vcm file that can be read by 3DRealize.
 * 3DRealize has problems reading zip file entries that have the extended file header set.
 * 
 * Solution copied from: http://sipostamas.wordpress.com/2008/05/13/creating-compatible-zip-file-from-java-to-net-and-silverlight-2b1/
 * Discussion: https://java.net/projects/truezip/lists/users/archive/2013-05/message/17
 */
public class VcmFileUpdater {

	private List<ByteArrayFile> byteArrayFiles = new ArrayList<VcmFileUpdater.ByteArrayFile>();
	private List<File> filesToUpdate = new ArrayList<File>();
	private List<String> filenamesToUpdate = new ArrayList<String>();
	private final File sourceFile;
	private final File destinationFile;
	private boolean needCompress = true;
	private int compressionLevel = 8;
    
    public VcmFileUpdater(File sourceFile) throws IOException {
        this.sourceFile = sourceFile;
        destinationFile = File.createTempFile("vcm-temp", "zip");
        destinationFile.deleteOnExit();
    }
    
	public void addFiles(File... files) {
		for (File file : files) {
			filesToUpdate.add(file);
			filenamesToUpdate.add(file.getName());
		}
	}

	public void addFile(String filename, InputStream stream) throws IOException {
		byteArrayFiles.add(new ByteArrayFile(filename, stream));
		filenamesToUpdate.add(filename);
	}

	public void addFile(String filename, byte[] content) {
		filenamesToUpdate.add(filename);
		byteArrayFiles.add(new ByteArrayFile(filename, content));
	}
    public void update(boolean updateTimestamp) throws IOException, ZipException {
		ZipOutputStream zipOut = createOutputStream();

		ZipFile zipFile = new ZipFile(sourceFile);
		zipOut.setComment(zipFile.getComment());
		for (Object fileHeaderObj : zipFile.getFileHeaders()) {
			FileHeader fileHeader = (FileHeader) fileHeaderObj;
			if (!filenamesToUpdate.contains(fileHeader.getFileName())) {
				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
				ZipInputStream zipInputStream = zipFile.getInputStream(fileHeader);
				IOUtils.copyLarge(zipInputStream, arrayOutputStream);
				IOUtils.closeQuietly(zipInputStream);
				long fileTimestamp = DosTimeToEpochConverter.convert(fileHeader.getLastModFileTime());
	            if (updateTimestamp) {
	                fileTimestamp = System.currentTimeMillis();
	            }
                writeToZipFile(zipOut, fileHeader.getFileName(), arrayOutputStream.toByteArray(), fileTimestamp);
			}
		}
		
		for (File file : filesToUpdate) {
		    long fileTimestamp = getFileTimestamp(zipFile, file.getName());
            if (updateTimestamp) {
                fileTimestamp = System.currentTimeMillis();
            }
            writeToZipFile(zipOut, file.getName(), FileUtils.readFileToByteArray(file), fileTimestamp);		
		}
		
		for (ByteArrayFile content : byteArrayFiles) {
			long fileTimestamp = getFileTimestamp(zipFile, content.filename);
            if (updateTimestamp) {
                fileTimestamp = System.currentTimeMillis();
            }
            writeToZipFile(zipOut, content.filename, content.fileContent, fileTimestamp);
		}
		
		closeOutputStream(zipOut);		
		FileUtils.copyFile(destinationFile, sourceFile);
	}
	
	private long getFileTimestamp(ZipFile zipFile, String filename) throws ZipException {
        long fileTimestamp = System.currentTimeMillis();
        FileHeader fileHeader = zipFile.getFileHeader(filename);
        if (fileHeader != null) {
            fileTimestamp = DosTimeToEpochConverter.convert(fileHeader.getLastModFileTime());
        }
        return fileTimestamp;
	}

	private void writeToZipFile(ZipOutputStream zipOut, String filename, byte[] content, long fileTimestamp) throws IOException {
		ZipEntry entry = createZipEntry(content, filename, fileTimestamp);
		zipOut.putNextEntry(entry);
		zipOut.write(content);
		zipOut.closeEntry();
	}

	private ZipOutputStream createOutputStream() throws FileNotFoundException {
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destinationFile));
		zipOut.setMethod(ZipOutputStream.DEFLATED);
		zipOut.setMethod(needCompress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
		zipOut.setLevel(compressionLevel);
		return zipOut;
	}
	
	private void closeOutputStream(ZipOutputStream zipOut) throws IOException{
		zipOut.finish();
		zipOut.close();
	}
	
	private ZipEntry createZipEntry(byte[] content, String fileName, long fileTimestamp) throws IOException {
		final ZipEntry zipEntry = new ZipEntry(fileName);
		if (needCompress) {
			// Compress to null, to determine compressed size and crc
			final NullOutputStream baos = new NullOutputStream();
			final ZipOutputStream zipOut = new ZipOutputStream(baos);
			try {
				zipOut.setMethod(needCompress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
				zipOut.setLevel(compressionLevel);
				zipOut.putNextEntry(zipEntry);
				zipOut.write(content);
				zipOut.closeEntry();
				zipOut.finish();
			} finally {
				IOUtils.closeQuietly(zipOut);
			}
		}
		else {
			// No compress, just store
			final CRC32 crc = new CRC32();
			crc.update(content);
			zipEntry.setCrc(crc.getValue());
			zipEntry.setCompressedSize(content.length);
			zipEntry.setSize(content.length);
		}
		zipEntry.setTime(fileTimestamp);
		return zipEntry;
	}
	
	private static class NullOutputStream extends OutputStream {
		public NullOutputStream() {
			super();
		}

		@Override
		public void write(final int b) {
			// DO NOTHING
		}
	}
	
	public static class ByteArrayFile {
		private final String filename;
		private final byte[] fileContent;

		public ByteArrayFile(String filename, InputStream stream) throws IOException {
			this.filename = filename;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.copy(stream, outputStream);
			this.fileContent = outputStream.toByteArray();
		}

		public ByteArrayFile(String filename, byte[] content) {
			this.filename = filename;
			this.fileContent = Arrays.copyOf(content, content.length);
		}
	}
}
