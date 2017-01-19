package org.cresst.sb.irp.automation.adapter.dao;

import java.util.Date;
import java.util.Map;

public interface TestOpportunityRepository {

    /**
     * Gets the number of times a Student (testee) record appears in the TestOpportunity database
     *
     * @param startTimeOfSimulation Optional. Specifies the start time to return data from.
     * @return A mapping of student identifier (from TDS database) to number of times it appears
     */
    Map<Integer, Integer> getStudentToTestCount(Date startTimeOfSimulation);
}
