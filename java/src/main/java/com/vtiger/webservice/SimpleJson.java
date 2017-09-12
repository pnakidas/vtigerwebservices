/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vtiger.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple lightweight json parser and generator;
 * @author sidharth
 */
public class SimpleJson implements Json{
    static enum TokenType {number, string, punctuation,
        constant, space, something};
    static class Reader{

        class Token{
            TokenType type;
            String value;
            
            @Override
            public String toString(){
                return "Token(type:"+type+", value:"+value+")";
            }
        }


        Pattern tokenRe=Pattern.compile(
                "(-?\\d+(?:[.]\\d+)?(?:[eE][+-]?\\d+)?)|"+
                "\"((?:[^\\\\]|\\\\.)*?)\"|"+
                "(\\[|]|\\{|}|,|:)|"+
                "(true|false|null)|"+
                "(\\s+)|(.)");


        Token current;
        boolean next(){
            while(match.find()){
                for(int i=0; i<TokenType.values().length;i++){
                    String val = match.group(i+1);
                    if(val != null){
                        current = new Token();
                        current.type = TokenType.values()[i];
                        current.value = val;
                        break;
                    }
                }
                if(current.type==TokenType.something){
                    throw new RuntimeException("Invalid json object");
                }
                if(current.type!=TokenType.space){
                    return true;
                }
            }
            return false;
        }

        Matcher match;
        public Object read(String input){
            match = tokenRe.matcher(input);
            if(next()){
                return readExpr();
            }else{
                throw new RuntimeException("There was no content");
            }
        }

        private Object readExpr(){
            switch(current.type){
                case string:
                    return readString();
                case number:
                    return readNumber();
                case punctuation:
                    if(current.value.equals("[")){
                        return readArray();
                    }else if(current.value.equals("{")){
                        return readObject();
                    }else{
                        throw new RuntimeException("Unexpected symbol");
                    }
                case constant:
                    if(current.value.equals("true")){
                        return true;
                    }else if(current.value.equals("false")){
                        return false;
                    }else if(current.value.equals("null")){
                        return null;
                    }else{
                        throw new RuntimeException("Should never come here");
                    }
                default:
                    throw new RuntimeException("Should never come here");
            }
        }

        private Object readString(){
            Map<String, String> conv = new HashMap<String, String>();
            conv.put("b", "\b");
            conv.put("f", "\f");
            conv.put("n", "\n");
            conv.put("r", "\r");
            conv.put("t", "\t");
            conv.put("\\", "\\\\");
            Pattern stringRe = Pattern.compile("[\\\\](.)");
            Matcher m = stringRe.matcher(current.value);
            StringBuffer sb = new StringBuffer();
            while(m.find()){
                String c = m.group(1);
                c = conv.containsKey(c)?conv.get(c):c;
                m.appendReplacement(sb, c);
            }
            m.appendTail(sb);
            return sb.toString();
        }

        private Object readNumber(){
            boolean isDouble = Pattern.compile("[.]|e|E").matcher(current.value).find();
            if(isDouble){
                return Double.valueOf(current.value);
            }else{
                return Long.valueOf(current.value);
            }
        }

        private Object readArray(){
            next();
            if(current.value.equals("]")){
                return Collections.EMPTY_LIST;
            }
            List<Object> list = new ArrayList<Object>();
            while(true){
                list.add(readExpr());
                next();
                if(!current.value.equals(",")){
                    break;
                }
                next();
            }
            if(current.value.equals("]")){
                return list;
            }else{
                throw new RuntimeException("Malformed list expression");
            }
        }

        private Object readObject(){
            next();
            if(current.value.equals("}")){
                return Collections.EMPTY_MAP;
            }
            Map<String, Object> obj = new HashMap<String, Object>();
            while(true){
                String key = (String)readExpr();
                next();
                match(":");

                obj.put(key, readExpr());
                next();
                if(!current.value.equals(",")){
                    break;
                }
                next();
            }
            if(current.value.equals("}")){
                return obj;
            }else{
                System.out.println("Current object = "+current);
                throw new RuntimeException("Malformed list expression");
            }
        }

        private void match(String value){
            if(!current.value.equals(value)){
                throw new RuntimeException("Was expecting "+value+"but got "+current.value);
            }
            next();

        }
    }

    static class Writer{
        public String write(Object input){
            if(input instanceof String){
                return writeString((String)input);
            }else if(input instanceof Number){
                return ((Number)input).toString();
            }else if(input instanceof List){
                return writeArray((List)input);
            }else if(input instanceof Map){
                return writeObject((Map)input);
            }else if(input instanceof Boolean){
                return ((Boolean)input)?"true":"false";
            }else if(input==null){
                return "null";
            }else{
                throw new RuntimeException("Unexpected unput value");
            }
        }

        private String writeString(String input){
            Map<String, String> conv = new HashMap<String, String>();
            conv.put("\b", "b");
            conv.put("\f", "f");
            conv.put("\n", "n");
            conv.put("\r", "r");
            conv.put("\t", "t");

            Pattern specialsRe = Pattern.compile("[\b\f\n\r\t]");

            StringBuffer sb = new StringBuffer();
            
            Matcher m = specialsRe.matcher(input);
            while(m.find()){
                m.appendReplacement(sb, "\\\\"+conv.get(m.group()));
            }
            m.appendTail(sb);
            return "\""+sb.toString()+"\"";
        }

        private String writeArray(List list) {
            List<String> parts = new ArrayList<String>();
            for(Object obj : list){
                parts.add(write(obj));
            }
            return "["+join(", ", parts)+"]";
        }

        private String writeObject(Map<String, Object> map) {
            List<String>parts = new ArrayList<String>();
            for(Map.Entry<String, Object> entry:map.entrySet()){
                String key = writeString(entry.getKey());
                String value = write(entry.getValue());
                parts.add(key+" : "+value);
            }
            return "{"+join(", ", parts)+"}";
        }

        private String join(String sep, List<String> list){
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
    }

    public Object read(String input) {
        return new Reader().read(input);
    }

    public String write(Object input) {
        return new Writer().write(input);
    }

}
