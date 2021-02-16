package org.zrnq;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

/**
 * 因为BufferedImage不可序列化,故创建了这个能够保存BufferedImage的工具类.
 * */
public class SerializableImage implements Externalizable {
    private int imageId=-1;
    private transient File storage;
    private static final Object lock = new Object();
    public SerializableImage(String url){
        synchronized (lock){
            imageId=PluginData.INSTANCE.getImageIdPointer();
            PluginData.INSTANCE.setImageIdPointer(imageId+1);
        }
        storage = Wiki.INSTANCE.resolveDataFile("images\\"+imageId+".png");
        try{
            URL u=new URL(url);
            BufferedImage bi = ImageIO.read(u);
            ImageIO.write(bi, "png", storage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public SerializableImage() {

    }
    @Nullable
    public static File getImage(int id){
        File image = Wiki.INSTANCE.resolveDataFile("images\\"+id+".png");
        if(image.exists())
            return image;
        else
            return null;
    }
    public BufferedImage getImage(){
        File img = getImage(imageId);
        if(img==null){
            R.logger.warning("找不到id为"+imageId+"的图片，使用丢失图片材质。");
            return PluginImageHolder.INSTANCE.getBufferedImage("missing");
        }
        try{
            return ImageIO.read(img);
        }catch (Exception e){
            R.logger.error("读取图片失败:"+img.getAbsolutePath()+"，使用丢失图片材质。");
            return PluginImageHolder.INSTANCE.getBufferedImage("missing");
        }
    }
    public int getImageId(){
        return imageId;
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(imageId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        imageId=(int)in.readObject();
        storage = Wiki.INSTANCE.resolveDataFile("images\\"+imageId+".png");
    }
}
