package org.cresst.sb.irp.automation.adapter.domain;

public enum AutomationPhase {
    INITIALIZATION("Initialization"),
    PRELOADING("Preloading"),
    SIMULATION("Student Simulation"),
    ANALYSIS("Analysis"),
    EXTRACTION("Preliminary TDS Report Extraction"),
    CLEANUP("Cleanup");

    private String name;

    AutomationPhase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
