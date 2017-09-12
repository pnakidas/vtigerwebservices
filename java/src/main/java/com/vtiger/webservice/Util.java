/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vtiger.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *
 * @author sidharth
 */
public class Util {

    public static String readStream(InputStream is)throws IOException{
        char[] buf = new char[1024];
        java.io.Reader reader = new InputStreamReader(is, "utf-8");
        StringBuilder sb = new StringBuilder();
        int n;
        while((n = reader.read(buf))!=-1){
            sb.append(buf, 0, n);
        }
        return sb.toString();
    }

    public static String join(String sep, List<String> list){
        StringBuilder sb = new StringBuilder();
        if(list.size()==0){
            return "";
        }
        sb.append(list.get(0));
        int n = list.size();
        for(int i = 1; i < n; i++){
            sb.append(sep);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    public static String md5(String data){
        try {
            MessageDigest m=MessageDigest.getInstance("MD5");
            m.update(data.getBytes());
            return new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("This system does not have the md5 algorithm");
        }
    }

}
