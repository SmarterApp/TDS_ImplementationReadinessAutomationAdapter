package org.cresst.sb.irp.automation.adapter.student.data;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PageContentsTest {

    private PageContents pageContents;

    @Before
    public void setUp() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("getPageContent_sample_response.xml").getFile());
        pageContents = new PageContents(file, 1, "8e03f3ed-3c08-4556-9e80-46a165a017b8");
    }

    @Test
    public void PageContents() {
        assertNotNull(pageContents);
    }

    @Test
    public void parsed_correctly() {
        assertEquals("8e03f3ed-3c08-4556-9e80-46a165a017b8", pageContents.getPageKey());
        assertEquals("G-187-3759-1", pageContents.getGroupId());
        assertEquals("SBAC-IRP-Perf-S1-ELA-11", pageContents.getSegmentId());
        assertEquals("21", pageContents.getLayout());
        assertEquals("ENU", pageContents.getLanguage());
    }
}
