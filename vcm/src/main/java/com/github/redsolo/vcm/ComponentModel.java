package com.github.redsolo.vcm;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ComponentModel {

	private Document document;
	private boolean isChanged;
	
	public ComponentModel(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public Map<String, String> getProperties() {
		HashMap<String, String> map = new HashMap<String,String>();
		NodeList list = getElement("Properties").getElementsByTagName("Property");
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			map.put(element.getAttribute("name"), element.getTextContent());
		}
		return map;
	}
	
	public String getVcid() {
		return getPropertyValue("VCID");
	}
	public void setVcid(String vcid) {
		setPropertyValue("VCID", vcid);
	}

	public String getDetailedRevision() {
		return getPropertyValue("DetailedRevision");
	}
	public void setDetailedRevision(String revision) {
		setPropertyValue("DetailedRevision", revision);
	}
	
	public String[] getTags() {
		return StringUtils.split(getPropertyValue("Tags"), ';');
	}
	public void setTags(String[] tags) {
		setPropertyValue("Tags", StringUtils.join(tags, ';'));
	}
	
	public int getRevision() {
		return Integer.parseInt(getPropertyValue("Revision"));
	}
	public void setRevision(int revision) {
		setPropertyValue("Revision", String.format("%d", revision));
	}
		
	public void setModelUrl(String modelUrl) {
		setElementValue("ModelUrl", modelUrl);		
	}
	public String getModelUrl() {
		return getElementValue("ModelUrl");
	}
	public String getLogoImageUrl() {
		return getElementValue("ModelUrl");
	}
	public void setLogoImageUrl(String logoImageUrl) {
		setElementValue("ModelUrl", logoImageUrl);
	}
	public String getPreviewImageUrl() {
		return getElementValue("PreviewImageUrl");
	}
	public void setPreviewImageUrl(String previewImageUrl) {
		setElementValue("PreviewImageUrl", previewImageUrl);
	}
	public String getThumbnailImageUrl() {
		return getElementValue("ThumbnailImageUrl");
	}
	public void setThumbnailImageUrl(String thumbnailImageUrl) {
		setElementValue("ThumbnailImageUrl", thumbnailImageUrl);
	}

	private void setElementValue(String name, String value) {
		if (!getElement(name).equals(value)) {
			getElement(name).setTextContent(value);
			setChanged(true);
		}
	}
	private String getElementValue(String name) {
		return getElement(name).getTextContent();
	}
	private Element getElement(String name) {
		NodeList list = document.getDocumentElement().getElementsByTagName(name);
		if (list.getLength() == 0) {
			throw new IllegalArgumentException(String.format("Unknown element name '%s'", name));
		}
		if (list.getLength() > 1) {
			throw new IllegalArgumentException(String.format("More than one element named '%s'", name));
		}
		return (Element) list.item(0);
	}

	public void setPropertyValue(String name, String value) {
		if (!getPropertyElement(name).equals(value)){
			getPropertyElement(name).setTextContent(value);
			setChanged(true);
		}
	}
	public void removePropertyValue(String name) {
		Element element = getPropertyElement(name);
		if (element != null) {
			element.getParentNode().removeChild(element);
			setChanged(true);
		}
	}
	public void addPropertyValue(String name, String value) {
		Element element = getPropertyElement(name, false);
		if (element == null) {
			Element properties = getElement("Properties");
			Element newElement = document.createElement("Property");
			newElement.setAttribute("name", name);
			newElement.setTextContent(value);
			properties.appendChild(newElement);
		} else {
			element.setTextContent(value);			
		}
		setChanged(true);
	}
	public String getPropertyValue(String name) {
		return getPropertyElement(name).getTextContent();
	}	

	private Element getPropertyElement(String name) {
		return getPropertyElement(name, true);
	}
	private Element getPropertyElement(String name, boolean raiseIfNotFound) {
		NodeList list = getElement("Properties").getElementsByTagName("Property");
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			if (element.getAttribute("name").equals(name)){
				return element;
			}
		}
		if (raiseIfNotFound) {
			throw new IllegalArgumentException(String.format("Unknown property named '%s'", name));
		}
		return null;		
	}

	boolean isChanged() {
		return isChanged;
	}

	void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
}
