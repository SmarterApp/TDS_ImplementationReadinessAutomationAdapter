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

    @Test
    public void parsed_correctly() {
        assertEquals("187", pageItem.getBankKey());
        assertEquals("2700", pageItem.getItemKey());
        assertEquals("ELA", pageItem.getSubject());
        assertEquals("11", pageItem.getGrade());
        assertEquals("ER", pageItem.getFormat());
        assertEquals(false, pageItem.isMarked());
        assertEquals(false, pageItem.isDisabled());
        assertEquals(false, pageItem.isPrintable());
        assertEquals(false, pageItem.isPrinted());
        assertEquals("PlainText", pageItem.getResponseType());
        assertEquals("1", pageItem.getPosition());
        assertEquals("1", pageItem.getPositionOnPage());
        assertEquals("v3JA11SEqACI7Q0NGyE1q6UqjmoTRNjHGS4ZjHNH%2BSWjRq4hbjTCn50qOgZ2hCLw2HXUuB3kOIfLY%2FqE4ZkaqrEIIuvZdJ6xytbFDt1n3X8bWrQpQY2bs0RVZNR%2Bk8Xk", pageItem.getFilePath());
    }

}
