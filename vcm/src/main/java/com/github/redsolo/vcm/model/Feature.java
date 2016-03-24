package com.github.redsolo.vcm.model;

import com.github.redsolo.vcm.ModelResource;

public class Feature {
    private final ModelResource resource;

    public Feature(ModelResource resource) {
        this.resource = resource;
    }

    public String getName() {
        return resource.getValue("Name").toString();
    }
    public boolean isOnDemandLoad() {
        return resource.getValue("OnDemandLoad").equals(1l);
    }
}
