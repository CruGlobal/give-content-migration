package org.cru.importer.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class ResultsCollector extends Observable {
	
	private List<ProcessMessage> cachedMessages;
	private boolean isRunning;
	private int errors;

	public ResultsCollector() {
		cachedMessages =  new LinkedList<ProcessMessage>();
		isRunning = false;
		errors = 0;
	}
	
	public void addCreatedPage(String page) {
	    addMessage(ProcessMessage.createCreatedMessage(page));
	}
	
    public void addModifiedPage(String page) {
        addMessage(ProcessMessage.createModifiedMessage(page));
	}

	public void addNotModifiedPage(String page) {
	    addMessage(ProcessMessage.createNotModifiedMessage(page));
	}
	
	public void addError(String page) {
	    errors++;
	    addMessage(ProcessMessage.createErrorMessage(page));
	}

	public void addIgnoredPages(String page) {
	    addMessage(ProcessMessage.createIgnoredMessage(page));
	}
	
    private void addMessage(ProcessMessage message) {
        if (super.countObservers() > 0) {
            super.setChanged();
            super.notifyObservers(message);
        } else {
            cachedMessages.add(message);
        }
    }
    
    public List<ProcessMessage> getCachedMessages() {
        return cachedMessages;
    }

    public void clearCachedMessages() {
        cachedMessages.clear();
    }

    public void stopRunning() {
        this.isRunning = false;
        super.setChanged();
        super.notifyObservers(ProcessMessage.createFinishMessage(errors));
        this.errors = 0;
    }
    
    public synchronized boolean checkRunning() {
        if (isRunning) {
            return true;
        } else {
            isRunning = true;
            return false;
        }
    }

}
