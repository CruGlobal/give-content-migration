package org.cru.importer.bean;

import java.util.List;
import java.util.Observer;

public interface ResultsCollector {

    void addCreatedPage(String page);

    void addModifiedPage(String page);

    void addNotModifiedPage(String page);

    void addError(String page);

    void addIgnoredPages(String page);

    void addFinishMessage();

    List<ProcessMessage> getCachedMessages();

    void clearCachedMessages();
    
    void addObserver(Observer o);
    
    void deleteObserver(Observer o);

    void addWarning(String message);

}