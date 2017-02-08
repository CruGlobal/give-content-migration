package org.cru.importer.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class ResultsCollector extends Observable {
	
	private List<String> cachedMessages;
	
	private boolean isRunning;

	public ResultsCollector() {
		cachedMessages =  new LinkedList<String>();
		isRunning = false;
	}
	
	public void addCreatedPage(String page) {
	    addMessage("Created Resource: " + page);
	}
	
    public void addModifiedPage(String page) {
        addMessage("Modified Resource: " + page);
	}

	public void addNotModifiedPage(String page) {
	    addMessage("Not Modified Resource: " + page);
	}
	
	public void addError(String page) {
	    addMessage("Error: " + page);
	}

	public void addIgnoredPages(String page) {
	    addMessage("Ignored Resource: " + page);
	}
	
    private void addMessage(String message) {
        if (super.countObservers() > 0) {
            super.setChanged();
            super.notifyObservers(message);
        } else {
            cachedMessages.add(message);
        }
    }
    
    public List<String> getCachedMessages() {
        return cachedMessages;
    }

    public void clearCachedMessages() {
        cachedMessages.clear();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
        super.setChanged();
        super.notifyObservers(isRunning);
    }

}
