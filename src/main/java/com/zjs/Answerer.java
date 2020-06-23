package com.zjs;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

public class Answerer implements Serializable {
    public String name;
    public long id;
    public long time=System.currentTimeMillis();
    public boolean accepted=false;
    public String text;
    public ArrayList<SerializableImage> images=new ArrayList<>();
    @Override
    public String toString(){
        return "回答者:"+name+"("+id+")\r\n" +
                "回答时间:"+Util.parseTime(time)+"\r\n" +
                "此回答解决了提问者的问题:"+accepted+"\r\n\r\n" +
                text;
    }
}
