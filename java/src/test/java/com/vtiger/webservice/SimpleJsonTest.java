/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vtiger.webservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author sidharth
 */
public class SimpleJsonTest extends TestCase {
    
    public SimpleJsonTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of read method, of class SimpleJson.
     */
    public void testRead() {
        System.out.println("read");
        SimpleJson json = new SimpleJson();

        assertEquals("hello\n\r\t\f\b/ world",
                json.read("\"hello\\n\\r\\t\\f\\b\\/ world\""));
        assertEquals("'Hello there\nhow d\\'you do\\\\'", json.read("\"'Hello there\\nhow d\\\\'you do\\\\\\\\'\""));

        assertEquals(123L, json.read("123"));
        assertEquals(-123L, json.read("-123"));
        assertEquals(15.5, json.read("15.5"));
        assertEquals(10E10, json.read("10E10"));

        List<?> l1 = Arrays.asList(1L, 2L, 3L, Arrays.asList("hello"));
        assertEquals(l1, json.read("[1,2,3, [\"hello\"]"));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1L);
        map.put("b", 4L);
        assertEquals(map, json.read("{\"a\":1,\"b\":4}"));

    }

    /**
     * Test of write method, of class SimpleJson.
     */
    public void testWrite() {
        System.out.println("write");
        SimpleJson json = new SimpleJson();
        assertEquals("\"Hello\\nWorld\"", json.write("Hello\nWorld"));
        assertEquals("1234", json.write(1234));
        assertEquals("[1, 2, 3, [true, false]]", json.write(Arrays.asList(1, 2, 3, Arrays.asList(true, false))));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1L);
        assertEquals("{\"a\" : 1}", json.write(map));
    }

}
