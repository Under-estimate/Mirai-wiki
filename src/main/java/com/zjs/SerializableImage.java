package com.zjs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 因为BufferedImage不可序列化,故创建了这个能够保存BufferedImage的工具类.
 * */
public class SerializableImage implements Externalizable {
    BufferedImage bi;
    public SerializableImage(BufferedImage bi){
        this.bi=bi;
    }
    public SerializableImage(){}
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        ImageIO.write(bi,"jpg",outputStream);
        byte[] data=outputStream.toByteArray();
        out.writeObject(data);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte[] data=(byte[])in.readObject();
        ByteArrayInputStream inputStream=new ByteArrayInputStream(data);
        bi=ImageIO.read(inputStream);
    }
}
