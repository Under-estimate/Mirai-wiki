package com.zjs;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * 提供用户对应用的自定义设置。
 * */
public class Config {
    public JsonObject data;

    public static final String QUESTION_NOTIFICATION="enable-question-notification";
    public static final String JOIN_NOTIFICATION="enable-join-notification";
    public static final String GROUP_BLACKLIST="group-blacklist";
    public static final String MEMBER_BLACKLIST="member-blacklist";
    public static final String RESULT_BACKGROUND="result-bg-image";
    public static final String HELP_BACKGROUND="help-bg-image";
    public static final String RESULT_BACKGROUND_OFFSET="result-bg-image-offset";
    public static final String HELP_BACKGROUND_OFFSET="help-bg-image-offset";

    public static final String CONFIG_COMMENT=
            "//所有以\"//\"开头的行都会被忽略\r\n" +
            "//自定义设置帮助:\r\n" +
            "//"+QUESTION_NOTIFICATION+":[true/false]设置是否在检测到群成员提出问题之后2分钟内无人发言时提醒使用本插件。\r\n" +
            "//"+JOIN_NOTIFICATION+":[true/false]设置是否在有新成员加群时发送欢迎信息并提醒使用本插件。\r\n" +
            "//"+GROUP_BLACKLIST+":[Array]设置忽略特定群的消息，将群ID写在方括号内，使用英文逗号隔开。\r\n" +
            "//"+MEMBER_BLACKLIST+":[Array]设置忽略特定群成员的消息，和"+QUESTION_NOTIFICATION+"一同使用，该成员不会触发提醒机制，但该成员发送的命令仍会被处理，适用于过滤机器人自动回复。\r\n" +
            "//"+RESULT_BACKGROUND+":[String]设置问题列表的背景图片，推荐大小:宽度≥700像素，高度≥1100像素。图片应放在plugins文件夹下与Wiki同名文件夹中，此处填写\"default\"表示使用默认图片，或填写你的图片文件名(包括拓展名)。\r\n" +
            "//"+RESULT_BACKGROUND_OFFSET+":[(x,y)]设置问题列表图片偏移，即图片左上角在生成的问题列表中的位置。\r\n" +
            "//"+HELP_BACKGROUND+":[String]设置帮助界面的背景图片，推荐大小:宽度≥1020像素，高度≥425像素。其他说明与"+RESULT_BACKGROUND+"相同。\r\n" +
            "//"+HELP_BACKGROUND_OFFSET+":[(x,y)]设置帮助界面背景图片偏移。\r\n";


    public Config(){
        data=new JsonObject();
        data.addProperty(QUESTION_NOTIFICATION,true);
        data.addProperty(JOIN_NOTIFICATION,true);
        data.add(GROUP_BLACKLIST,new JsonArray());
        data.add(MEMBER_BLACKLIST,new JsonArray());
        data.addProperty(RESULT_BACKGROUND,"default");
        data.addProperty(RESULT_BACKGROUND_OFFSET,"(0,0)");
        data.addProperty(HELP_BACKGROUND,"default");
        data.addProperty(HELP_BACKGROUND_OFFSET,"(0,0)");
    }
    public Config(File save){
        this();
        JsonObject temp=data;
        try {
            BufferedReader br = new BufferedReader(new FileReader(save));
            StringBuilder sb=new StringBuilder();
            String line;
            while((line=br.readLine())!=null)
                if(!line.startsWith("//"))
                    sb.append(line);
            data=JsonParser.parseString(sb.toString()).getAsJsonObject();
        }catch (Exception e){
            Util.logger.error("Failed to load config file, using default config...",e);
            data=temp;
        }
    }

    public void save(File destination){
        try {
            FileWriter fw = new FileWriter(destination);
            fw.write(CONFIG_COMMENT);
            fw.write(data.toString().replace(",\"",",\r\n\"").replace("{","{\r\n").replace("}","\r\n}"));
            fw.flush();
            fw.close();
        }catch (Exception e){
            Util.logger.error(e);
        }
    }

    public Object getAs(String key){
        if(!data.has(key))
            return null;
        if(key.equals(QUESTION_NOTIFICATION)||key.equals(JOIN_NOTIFICATION))
            return data.get(key).getAsBoolean();
        if(key.equals(GROUP_BLACKLIST)||key.equals(MEMBER_BLACKLIST))
            return data.get(key).getAsJsonArray();
        if(key.equals(RESULT_BACKGROUND)||key.equals(HELP_BACKGROUND))
            return data.get(key).getAsString();
        if(key.equals(RESULT_BACKGROUND_OFFSET)||key.equals(HELP_BACKGROUND_OFFSET)){
            String s=data.get(key).getAsString();
            s=s.substring(1,s.length()-1);
            String[] pos=s.split(",");
            Dimension d=new Dimension(Integer.parseInt(pos[0]),Integer.parseInt(pos[1]));
            return d;
        }
        return null;
    }
}
