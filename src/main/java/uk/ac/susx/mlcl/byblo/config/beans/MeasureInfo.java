/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.config.beans;

import uk.ac.susx.mlcl.byblo.measures.Measure;

/**
 *
 * @author hamish
 */
public class MeasureInfo {

    private String name;

    private Class<? extends Measure> type;

    private String description;

    public MeasureInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends Measure> getType() {
        return type;
    }

    public void setType(Class<? extends Measure> type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{"
                + "name=" + name
                + ", type=" + type
                + ", description=" + description
                + '}';
    }
}
