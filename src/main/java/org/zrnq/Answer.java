package org.zrnq;

import java.io.Serializable;
import java.util.ArrayList;

public class Answer implements Serializable {
    public String name;
    public long id;
    public final long time=System.currentTimeMillis();
    public boolean accepted=false;
    public String text;
    public final ArrayList<SerializableImage> images=new ArrayList<>();
    public String getStatusBarText(){
        return "回答者: "+name
                +"    时间: "+Util.parseTime(time)
                +(accepted?"[已被采纳]":"");
    }
}
