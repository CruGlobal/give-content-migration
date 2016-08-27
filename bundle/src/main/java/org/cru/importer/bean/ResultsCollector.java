package org.cru.importer.bean;

import java.util.LinkedList;
import java.util.List;

public class ResultsCollector {
	
	private List<String> createdPages;
	private List<String> modifiedPages;
	private List<String> notModifiedPages;
	private List<String> ignoredPages;
	private List<String> errors;

	public ResultsCollector() {
		createdPages = new LinkedList<String>();
		modifiedPages = new LinkedList<String>();
		notModifiedPages = new LinkedList<String>();
		errors = new LinkedList<String>();
		ignoredPages =  new LinkedList<String>();
	}
	
	public void addCreatedPage(String page) {
		createdPages.add(page);
	}
	
	public void addModifiedPage(String page) {
		modifiedPages.add(page);
	}

	public void addNotModifiedPage(String page) {
		notModifiedPages.add(page);
	}
	
	public void addError(String page) {
		errors.add(page);
	}

	public void addIgnoredPages(String page) {
		ignoredPages.add(page);
	}

	public List<String> getCreatedPages() {
		return createdPages;
	}

	public List<String> getModifiedPages() {
		return modifiedPages;
	}

	public List<String> getNotModifiedPages() {
		return notModifiedPages;
	}

	public List<String> getErrors() {
		return errors;
	}

	public List<String> getIgnoredPages() {
		return ignoredPages;
	}

}
