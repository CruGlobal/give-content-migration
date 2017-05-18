package org.cru.importer.bean;

public class ReferenceReplacement {

    private String selector;
    private String attribute;

    public ReferenceReplacement(String replacement) {
        int index = replacement.lastIndexOf(".");
        this.selector = replacement.substring(0, index);
        this.attribute = replacement.substring(index + 1);
    }
    
    public String getSelector() {
        return selector;
    }
    public String getAttribute() {
        return attribute;
    }
    
}
