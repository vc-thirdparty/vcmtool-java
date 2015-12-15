package com.github.redsolo.vcm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Every VCM file has a component.dat file that has some values about the
 * component.
 */
public class ComponentData {

	public static final String REVISION = "Revision";
    public static final String DETAILED_REVISION = "DetailedRevision";

	private Map<String, Object> valuesMap = new LinkedHashMap<String, Object>();
	private Map<String, String> itemsMap = new LinkedHashMap<String, String>();
	private Map<String, String> keywordsMap = new LinkedHashMap<String, String>();

	private final String modelResourceName;
	private final String modelResourceType;
	
	public ComponentData(ModelResource modelResource) {
		modelResourceName = modelResource.getName();
		modelResourceType = modelResource.getType();
		
		for (NameValuePair pair : modelResource.getValues()) {
			if (!(pair.getValue() instanceof ModelResource)) {
				valuesMap.put(pair.getName(), pair.getValue());
			}
		}

		if (modelResource.getResource("Items") != null) {
			for (ModelResource itemResource : modelResource.getResource("Items").getResources()) {
				for (NameValuePair pair : itemResource.getValues()) {
					if (!(pair.getValue() instanceof ModelResource)) {
					    if (!itemsMap.containsKey(pair.getName()) || StringUtils.isBlank(itemsMap.get(pair.getName()))) {
					        itemsMap.put(pair.getName(), pair.getValue().toString());
					    }
					}
				}
			}
		}
		if (modelResource.getResource("KeywordMap") != null) {
			for (ModelResource keyWordResource : modelResource.getResource("KeywordMap").getResources()) {
				keywordsMap.put((String) keyWordResource.getValue("Key"), keyWordResource.getValue("Value").toString());
			}
		}
	}

	public long getRevision() {
		return (Long) valuesMap.get(REVISION);
	}

	public void setRevision(long newRevision) {
        valuesMap.put(REVISION, newRevision);
        String detailedRevision = getDetailedRevision();
        int lastDigitPos = StringUtils.lastIndexOf(detailedRevision, ".");
        detailedRevision = String.format("%s.%d", detailedRevision.substring(0,  lastDigitPos), newRevision);
        valuesMap.put(DETAILED_REVISION, detailedRevision);
    }
    
    public void stepRevision() {
        setRevision(getRevision() + 1);
    }

    public String getDetailedRevision() {
        return (String) valuesMap.get(DETAILED_REVISION);
    }

	public String getVcId() {
		return (String) valuesMap.get("VcId");
	}

	public void setVcid(String vcid) {		
		valuesMap.put("VcId", vcid);
	}

	public String getName() {
		return itemsMap.get("Name");
	}

	public String getDescription() {
		return itemsMap.get("Description");
	}

	public String getIcon() {
		return itemsMap.get("Icon");
	}

	public String getItem(String key) {
		return itemsMap.get(key);
	}

	public String getKeyword(String key) {
		return keywordsMap.get(key);
	}

	public Map<String, String> getItems() {
		return itemsMap;
	}

	public Map<String, String> getKeywords() {
		return keywordsMap;
	}

	public Map<String, Object> getValues() {
		return valuesMap;
	}

	public String[] getTags() {
		if (valuesMap.containsKey("Tags") ) {
			return StringUtils.split((String) valuesMap.get("Tags"), ';');			
		} else {
			return new String[]{""};
		}
	}

	public void setTags(String[] tags) {
		valuesMap.put("Tags", StringUtils.join(tags, ';'));
	}

	public ModelResource getResource() {
		ModelResource resource = new ModelResource(modelResourceType, modelResourceName);
		for (String key : valuesMap.keySet()) {
			resource.putValue(key, valuesMap.get(key));
		}
		if (itemsMap.size() > 0) {
			ModelResource itemsResource = new ModelResource("Items");
			ModelResource itemResource = new ModelResource("Item");
			resource.addResource(itemsResource);
			itemsResource.addResource(itemResource);
			for (Entry<String, String> entry : itemsMap.entrySet()) {
				itemResource.putValue(entry.getKey(), entry.getValue());
			}
		}
		if (keywordsMap.size() > 0) {
			ModelResource keywordMapResource = new ModelResource("KeywordMap");
			resource.addResource(keywordMapResource);
			for (Entry<String, String> entry : keywordsMap.entrySet()) {
				ModelResource keywordResource = new ModelResource("Keyword");
				keywordMapResource.addResource(keywordResource);
				keywordResource.putValue("Key", entry.getKey());
				keywordResource.putValue("Value", entry.getValue());
			}
		}
		return resource;
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}
