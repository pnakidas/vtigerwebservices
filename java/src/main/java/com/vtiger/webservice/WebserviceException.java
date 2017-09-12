/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vtiger.webservice;

import java.net.MalformedURLException;

/**
 *
 * @author sidharth
 */
public class WebserviceException extends Exception{

    WebserviceException(String string, Throwable ex) {
        super(string, ex);
    }

    WebserviceException(String string) {
        super(string);
    }

}
