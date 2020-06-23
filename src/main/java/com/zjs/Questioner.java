package com.zjs;

import java.io.Serializable;

public class Questioner implements Serializable {
    public String name;
    public long id;
    @Override
    public String toString(){
        return name+"("+id+")";
    }
}
