package org.cru.importer.bean;

public class ProcessMessage {
    
    public static final String START = "started";
    public static final String RUNNING = "running";
    public static final String FINISH = "finished";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";
    public static final String NOT_MODIFIED = "notModified";
    public static final String IGNORED = "ignored";
    public static final String ERROR = "error";
    
    private String type;

    private String description;
    
    public ProcessMessage(String type, String description) {
        super();
        this.type = type;
        this.description = description;
    }
    
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "{\"type\":\"" + type + "\",\"description\":\"" + description + "\"}";
    }
    
    public static ProcessMessage createCreatedMessage(String description) {
        return new ProcessMessage(CREATED, description);
    }
    
    public static ProcessMessage createModifiedMessage(String description) {
        return new ProcessMessage(MODIFIED, description);
    }
    
    public static ProcessMessage createNotModifiedMessage(String description) {
        return new ProcessMessage(NOT_MODIFIED, description);
    }
    
    public static ProcessMessage createIgnoredMessage(String description) {
        return new ProcessMessage(IGNORED, description);
    }
    
    public static ProcessMessage createErrorMessage(String description) {
        return new ProcessMessage(ERROR, description);
    }
    
    public static ProcessMessage createStartMessage() {
        return new ProcessMessage(START, "");
    }
    
    public static ProcessMessage createFinishMessage(int errors) {
        return new ProcessMessage(FINISH, String.valueOf(errors));
    }
    
    public static ProcessMessage createRunningMessage() {
        return new ProcessMessage(RUNNING, "");
    }
    
}
