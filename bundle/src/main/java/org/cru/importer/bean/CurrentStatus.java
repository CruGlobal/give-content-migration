package org.cru.importer.bean;

public class CurrentStatus {
	
	private boolean isRunning;
	private Thread currentProcess;

	public CurrentStatus() {
		isRunning = false;
	}
	
    public void stopRunning() {
        this.isRunning = false;
    }
    
    public synchronized boolean checkRunning() {
        if (isRunning) {
            return true;
        } else {
            isRunning = true;
            return false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Thread getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(Thread currentProcess) {
        this.currentProcess = currentProcess;
    }

}
