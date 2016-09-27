package org.cresst.sb.irp.automation.adapter.student.data;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class PageItemTest {


    private PageItem pageItem;

    @Before
    public void setUp() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("example_page_item.xml").getFile());
        pageItem = new PageItem(file);
    }

    @Test
    public void PageItem() {
        assertNotNull(pageItem);
    }

}
