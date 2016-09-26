package org.cresst.sb.irp.automation.adapter.student.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PageContentsTest {

    private PageContents pageContents;

    @Before
    public void setUp() throws Exception {
        pageContents = new PageContents();
    }

    @Test
    public void PageContents() {
        assertNotNull(pageContents);
    }

}
