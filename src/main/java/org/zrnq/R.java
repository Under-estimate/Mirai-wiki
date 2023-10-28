package org.zrnq;

import net.mamoe.mirai.utils.MiraiLogger;

import java.awt.*;
import java.io.File;
import java.util.HashMap;

public class R {
    public static final String name="MiraiWiki";
    public static final String version ="2.0.4";
    public static MiraiLogger logger;
    public static final HashMap<Long,HashMap<Long,Session>> sessions=new HashMap<>();
    public static final Font F = new Font("Microsoft YaHei",Font.PLAIN,30);
    public static final Color hover = new Color(0,0,0,100);
    public static final Color skyBlue = new Color(0, 100, 255);
    public static final Color orange = new Color(255,100,0);
    public static void systemCheck(){
        if(!System.getProperty("os.name").toLowerCase().contains("win"))
            logger.warning("检测到正在非Windows系统上运行，请安装字体\"Microsoft YaHei\"以便汉字能够正常显示。");
        GraphicsEnvironment ge=GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] f=ge.getAllFonts();
        for(Font temp:f){
            String name = temp.getName();
            if(Util.matchesAny(name,"微软雅黑","msyh","Microsoft YaHei"))return;
        }
        logger.error("没有找到字体\"Microsoft YaHei\"，汉字可能不会被正确显示。");
    }
    public static void initResources(){
        File imageFolder = Wiki.INSTANCE.resolveDataFile("images\\");
        if(!imageFolder.exists())
            if(!imageFolder.mkdirs())
                logger.error("创建图片文件夹失败。");
    }
}
