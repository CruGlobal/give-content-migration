package org.cru.importer.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component(
    metatype = true,
    label = "Give importer - ResultsCollectorImpl",
    description = "Collect results across the import process"
)
@Service
public class ResultsCollectorImpl extends Observable implements ResultsCollector {
	
	private List<ProcessMessage> cachedMessages;
	private int errors;

	public ResultsCollectorImpl() {
		cachedMessages =  new LinkedList<ProcessMessage>();
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
	
    public void addFinishMessage() {
        super.setChanged();
        super.notifyObservers(ProcessMessage.createFinishMessage(errors));
        this.errors = 0;
    }
    
    public void addWarning(String message) {
        addMessage(ProcessMessage.createWarningMessage(message));
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

}
