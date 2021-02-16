package org.zrnq;

import java.io.Serializable;

public class Questioner implements Serializable {
    public final String name;
    public final long id;
    public Questioner(String name, long id){
        this.name=name;
        this.id=id;
    }
    @Override
    public String toString(){
        return name+"("+id+")";
    }
}
