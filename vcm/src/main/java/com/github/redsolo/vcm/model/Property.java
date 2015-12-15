package com.github.redsolo.vcm.model;

import com.github.redsolo.vcm.ModelResource;

public class Property {
    private static final String[] EMPRY_STRINGS = new String[0];
    private ModelResource resource;

    public Property(ModelResource resource) {
        this.resource = resource;
    }
    public boolean isVisible() {
        return hasSetting("VISIBLE");
    }
    public boolean isRebuildOnEdit() {
        return hasSetting("ON_EDIT_REBUILD");
    }
    public boolean isEditableWhenDisconnected() {
        return hasSetting("EDITABLE_DISCONNECTED");
    }
    public boolean isEditableVonnected() {
        return hasSetting("EDITABLE_CONNECTED");
    }
    private boolean hasSetting(String settingName) {
        ModelResource settings = resource.getResource("Settings");
        if (settings != null) {
            return settings.getValues().stream().anyMatch(nvp -> nvp.getName().equalsIgnoreCase(settingName));
        }
        return false;
    }
    public String[] getSet() {
        if (resource.getName().startsWith("rTStepVariable")) {
            ModelResource stepList = resource.getResource("StepList");            
            if (stepList != null) {               
                return stepList.getResources("Step").stream().map(s -> s.getValue("Value").toString()).toArray(String[]::new);
            }   
        }
        return EMPRY_STRINGS;
    }
    public String[] getRange() {
        if (resource.getName().startsWith("rTLimitVariable")) {
            Object max = resource.getValue("Max");
            Object min = resource.getValue("Min");          
            if (max != null && min != null) {
                return new String[]{min.toString(), max.toString()};        
            }
        }
        return EMPRY_STRINGS;
    }
    public String getName() {
        return resource.getValue("Name").toString();
    }
    public String getValue() {
        return resource.getValue("Value").toString();
    }
    public String toString() {
        return getValue();
    }
}