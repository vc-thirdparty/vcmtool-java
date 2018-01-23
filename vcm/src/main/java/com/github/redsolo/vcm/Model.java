package com.github.redsolo.vcm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.github.redsolo.vcm.util.DosTimeToEpochConverter;
import com.github.redsolo.vcm.util.VcmFileUpdater;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class Model {
    public static final String COMPONENT_RSC = "component.rsc";
    public static final String COMPONENT_DAT = "component.dat";
    public static final String MODEL_XML = "model.xml";
    public static final String LAYOUT_RSC = "layout.rsc";

    private static Logger log = Logger.getLogger(Model.class);
    
    private final long zipFileLastModified;
	private ZipFile zipFile;
	
	private ComponentData componentData;
	private long componentDataHashcode;
	
	private ModelResource resourceData;
	private long resourceDataHashcode;
	
	private ComponentModel componentModel;
	private TransformerFactory transformerFactory;
	private DocumentBuilderFactory documentBuilderFactory;

	public Model(File file) throws ZipException {
		zipFile = new ZipFile(file);
		zipFileLastModified = file.lastModified();
	}
	
	public Model(String path) throws ZipException {
		zipFile = new ZipFile(path);
		zipFileLastModified = zipFile.getFile().lastModified();
	}

	public File getFile() {
		return zipFile.getFile();
	}
	
	public List<String> listContent() throws ZipException {
		ArrayList<String> list = new ArrayList<String>();
		for (Object fileheaderObj : zipFile.getFileHeaders()) {
			list.add(((FileHeader)fileheaderObj).getFileName());
		}
		return list;
	}
	
	public InputStream getInputStream(String filename) throws ZipException {
	    log.trace(String.format("Opening input stream to %s", filename));
		FileHeader fileHeader = zipFile.getFileHeader(filename);
		if (fileHeader == null) {
			return null;
		}
		return zipFile.getInputStream(fileHeader);
	}
	
	public OutputStream getOutputStream(String filename) throws ZipException {
        log.trace(String.format("Opening output stream to %s", filename));
		return new ZipFileOutputStreamFacade(zipFile, filename);
	}
	
	public InputStream getThumbnail() throws ZipException, IOException {
		return getInputStream((String) getComponentData().getItem("PreviewIcon"));
	}

    private String getResourceDataFilename() throws ZipException {
        return isComponent() ? COMPONENT_RSC : LAYOUT_RSC;
    }
	
	public ModelResource getResourceData()throws ZipException, IOException {
		if (resourceData == null) {
			resourceData = readFileAsResource(getResourceDataFilename());
			resourceDataHashcode = resourceData.hashCode();
		}
		return resourceData;
	}
	
	public boolean setResourceData(ModelResource newResourceData, boolean stepRevision) throws IOException, ZipException {
		if (newResourceData.hashCode() != resourceDataHashcode) {
			this.resourceData = newResourceData;
			resourceDataHashcode = resourceData.hashCode();
			writeFileAsResource(COMPONENT_RSC, newResourceData, false);
			
			if (stepRevision) {
			    stepRevision();
			}
			return true;
		}
		return false;
	}
		
	public ComponentData getComponentData() throws ZipException, IOException {
		if (componentData == null) {
			componentData = new ComponentData(readFileAsResource(COMPONENT_DAT));
			componentDataHashcode = componentData.hashCode();
		}
		return componentData;
	}

	public boolean setComponentData(ComponentData newComponentData, boolean stepRevision) throws IOException, ZipException {
		if (newComponentData.hashCode() != componentDataHashcode) 
		{
			this.componentData = newComponentData;
			if (stepRevision) {
			    componentData.stepRevision();
			}
			componentDataHashcode = componentData.hashCode();
			writeFileAsResource(COMPONENT_DAT, componentData.getResource(), stepRevision);
			return true;
		}
		return false;
	}

	public ComponentModel getComponentModel() throws ZipException, IOException {
		if (componentModel == null) {
			InputStream stream = null;
			try {
				stream = getInputStream(MODEL_XML);
				if (stream != null) {
					Document document = getDocumentBuilderFactory().newDocumentBuilder().parse(stream);
					componentModel = new ComponentModel(document);					
				}
			} catch (SAXException | ParserConfigurationException e) {
				throw new IOException("Problem loading Model.xml", e);
			} finally {
				IOUtils.closeQuietly(stream);
			}
		}
		return componentModel;
	}
	
	public boolean setComponentModel(ComponentModel newModel, boolean stepRevision) throws ZipException, IOException {
		if (newModel == null) return false;
		if (newModel.isChanged()) 
		{
			this.componentModel = newModel;
			if (stepRevision) {
				stepRevision();
			}
			OutputStream stream = null;
			try {
				stream = getOutputStream(MODEL_XML);				
				TransformerFactory transformerFactory = getTransformerFactory();
				Transformer xformer = transformerFactory.newTransformer();
			    xformer.transform(new DOMSource(componentModel.getDocument()), new StreamResult(stream));
			} catch (TransformerFactoryConfigurationError | TransformerException e) {
				throw new IOException("Problem saving Model.xml", e);
			} finally {
				IOUtils.closeQuietly(stream);
			}
			componentModel.setChanged(false);
			return true;
		}
		return false;
	}
	
	public long getLastModifiedTime(String filename) throws ZipException {	    
	    return DosTimeToEpochConverter.convert(zipFile.getFileHeader(filename).getLastModFileTime());
	}

	private ModelResource readFileAsResource(String filename) throws ZipException, IOException {
		Reader reader = null;
		try {
			reader = new InputStreamReader(zipFile.getInputStream(zipFile.getFileHeader(filename)), "US-ASCII");
			return new ModelResourceParser().parse(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

    private void writeFileAsResource(String filename, ModelResource modelResource, boolean updateFileDate) throws ZipException, IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
    		new ModelResourceWriter().write(modelResource, new OutputStreamWriter(outputStream, "US-ASCII"));
    		VcmFileUpdater vcmFileUpdater = new VcmFileUpdater(zipFile.getFile());
    		vcmFileUpdater.addFile(filename, outputStream.toByteArray());
    		vcmFileUpdater.update(updateFileDate);
    		refresh();
    		if (!updateFileDate) {
    		    zipFile.getFile().setLastModified(zipFileLastModified);
    		}
		} finally  {
		    IOUtils.closeQuietly(outputStream);
		}
	}
    
    public void refresh() throws ZipException {
        zipFile = new ZipFile(zipFile.getFile());
    }

    public boolean isComponent() throws ZipException {
        return zipFile.getFileHeader(COMPONENT_RSC) != null;
    }

    public boolean isLayout() throws ZipException {
        return zipFile.getFileHeader(LAYOUT_RSC) != null;
    }

    public void stepRevision() throws ZipException, IOException {
        ComponentData revisionComponentData = getComponentData();
        revisionComponentData.stepRevision();
        componentDataHashcode = componentData.hashCode();
        writeFileAsResource(COMPONENT_DAT, componentData.getResource(), true);
    }

	private TransformerFactory getTransformerFactory() throws TransformerFactoryConfigurationError {
		if (transformerFactory == null) {
			transformerFactory = TransformerFactory.newInstance();
		}
		return transformerFactory;
	}
	private DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory == null) {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		}
		return documentBuilderFactory;
	}
}
