package org.cresst.sb.irp.automation.adapter.student;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UpdateResponsesBuilderTest {
    UpdateResponsesBuilder updateResponsesBuilder;

    @Before
    public void setUp() throws Exception {
        updateResponsesBuilder = new UpdateResponsesBuilder();
    }

    @Test
    public void UpdateResponsesBuilder() {
        assertNotNull(updateResponsesBuilder);
    }

}
