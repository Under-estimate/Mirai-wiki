package org.zrnq;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class PluginImageHolder {
    public static final PluginImageHolder INSTANCE = getInstance();
    private HashMap<String,Object> data;
    private PluginImageHolder(){

    }
    private static PluginImageHolder getInstance() {
        PluginImageHolder holder = new PluginImageHolder();
        holder.data = new HashMap<>();
        holder.readImages();
        return holder;
    }
    public void putImage(String name, byte[] byteArray){
        data.put(name + "A",byteArray);
        data.put(name + "B",toBufferedImage(byteArray));
    }
    @SuppressWarnings("unused")
    public void putImage(String name, BufferedImage bi){
        data.put(name + "A",toByteArray(bi));
        data.put(name + "B",bi);
    }
    public BufferedImage getBufferedImage(String name){
        return (BufferedImage) data.get(name + "B");
    }
    public byte[] getByteArray(String name){
        return (byte[]) data.get(name + "A");
    }
    private void readImages(){
        putImage("bg",Util.readPackageResource("bg.png"));
        putImage("missing",Util.readPackageResource("missing.png"));
    }
    public static BufferedImage toBufferedImage(byte[] byteArray){
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            return ImageIO.read(bis);
        }catch (Exception e){
            R.logger.error("无法将给定的byte数组转换为BufferedImage",e);
            return null;
        }
    }
    public static byte[] toByteArray(BufferedImage bi){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", bos);
            return bos.toByteArray();
        }catch (Exception e){
            R.logger.error("无法将给定的BufferedImage转换为byte数组",e);
            return null;
        }
    }
}
