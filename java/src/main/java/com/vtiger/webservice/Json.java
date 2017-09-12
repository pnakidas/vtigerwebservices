/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vtiger.webservice;

/**
 *
 * @author sidharth
 */
public interface Json {
    /**
     * Read the contents of a string and build a json object.
     *
     * @param input
     * @return
     */
    public Object read(String input);
    public String write(Object input);
}
