package org.cresst.sb.irp.automation.adapter.engine.task;

import java.util.List;
import java.util.Set;

import org.cresst.sb.irp.automation.adapter.art.ArtStudent;

public class AutomationPreloadResults {
    private Set<String> irpTestKeys;
    private List<ArtStudent> artStudents;

    public AutomationPreloadResults(Set<String> irpTestKeys, List<ArtStudent> artStudents) {
        this.irpTestKeys = irpTestKeys;
        this.artStudents = artStudents;
    }

    public Set<String> getIrpTestKeys() {
        return irpTestKeys;
    }

    public void setIrpTestKeys(Set<String> irpTestKeys) {
        this.irpTestKeys = irpTestKeys;
    }

    public List<ArtStudent> getArtStudents() {
        return artStudents;
    }

    public void setArtStudents(List<ArtStudent> artStudents) {
        this.artStudents = artStudents;
    }

}
