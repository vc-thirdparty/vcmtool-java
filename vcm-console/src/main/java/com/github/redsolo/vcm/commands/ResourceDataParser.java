package com.github.redsolo.vcm.commands;

import java.util.ArrayList;
import java.util.List;

import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.model.Feature;

public class ResourceDataParser {

    private ResourceDataParser() {
    }

    public static ModelResource getVariables(ModelResource root) {
        for (ModelResource modelResource : root.getResources()) {
            ModelResource variableSpace = modelResource.getResource("VariableSpace");
            if (variableSpace != null) {
                return variableSpace;
            }
        }
        return null;
    }
    
    public static List<ModelResource> getPythonScripts(ModelResource root) {
        List<ModelResource> pythonScripts = new ArrayList<ModelResource>();
        for (ModelResource modelResource : root.getResources()) {
            
            for (ModelResource funcResource : modelResource.getResources("Functionality")) {
                if (funcResource.getName().equals("rPythonScript")) {
                    pythonScripts.add(funcResource);
                }
            }
        }
        return pythonScripts;
    }

    public static List<ModelResource> getInterfaces(ModelResource root) {
        List<ModelResource> interfaces = new ArrayList<ModelResource>();
        for (ModelResource modelResource : root.getResources()) {
            
            for (ModelResource funcResource : modelResource.getResources("Functionality")) {
                if (funcResource.getName().equals("rSimInterface") ||
                        funcResource.getName().equals("rSimDynamicInterface")) {
                    interfaces.add(funcResource);
                }
            }
        }
        return interfaces;
    }

    public static List<ModelResource> getNotes(ModelResource root) {
        List<ModelResource> interfaces = new ArrayList<ModelResource>();
        for (ModelResource modelResource : root.getResources()) {
            
            for (ModelResource funcResource : modelResource.getResources("Functionality")) {
                if (funcResource.getName().equals("rNote")) {
                    interfaces.add(funcResource);
                }
            }
        }
        return interfaces;
    }

    public static String getLocation(ModelResource root) {
        ModelResource locationResource = getLocationResource(root);
        if (locationResource != null) {
            return locationResource.getValue("Location").toString();
        }
        return null;
    }

    public static ModelResource getLocationResource(ModelResource root) {
        for (ModelResource modelResource : root.getResources()) {
            if (modelResource.hasValue("Location")) {
                return modelResource;
            }
        }
        return null;
    }

    public static ModelResource getNamedResource(ModelResource root, String name) {
        for (ModelResource modelResource : root.getResources()) {
            if (modelResource.getValue("Name") != null) {
                if (modelResource.getValue("Name").toString().equalsIgnoreCase(name)) {
                    return modelResource;
                }
            }
        }
        return null;
    }

    public static List<Feature> getFeatures(ModelResource root, String type) {
        List<Feature> features = new ArrayList<Feature>();
        for (ModelResource modelResource : root.getResources()) {

            if (modelResource.getType().equals("Feature") && modelResource.getName().equals(type)) {
                features.add(new Feature(modelResource));
            }
            features.addAll(getFeatures(modelResource, type));
        }
        return features;
    }
}
