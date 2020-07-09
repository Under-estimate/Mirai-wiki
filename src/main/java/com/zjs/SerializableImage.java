package com.zjs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 因为BufferedImage不可序列化,故创建了这个能够保存BufferedImage的工具类.
 * */
public class SerializableImage implements Externalizable {
    int imageId=-1;
    private static int idCounter=-1;
    private static final File idc=new File("plugins\\Wiki\\images\\idc.IMPORTANT");
    public SerializableImage(BufferedImage bi){
        if(idCounter==-1){
            if(!idc.exists())
                idCounter=0;
            else{
                try{
                    BufferedReader br=new BufferedReader(new FileReader(idc));
                    idCounter=Integer.parseInt(br.readLine());
                    br.close();
                }catch (Exception e){
                    Util.logger.error("Failed to read image IDCounter",e);
                }
            }
        }
        imageId=idCounter++;
        writeIdc();
        writeImg(bi);
    }
    public SerializableImage() {
    }
    private void writeImg(BufferedImage im){
        try {
            ImageIO.write(im, "png", new File("plugins\\Wiki\\images\\" + imageId + ".png"));
        }catch (Exception e){
            Util.logger.error("Failed to write image",e);
        }
    }
    private static void writeIdc(){
        try{
            FileWriter fw=new FileWriter(idc);
            fw.write(Integer.toString(idCounter));
            fw.flush();
            fw.close();
        }catch (Exception e){
            Util.logger.error("Failed to write image IDCounter.",e);
        }
    }
    public BufferedImage getImage(){
        try{
            return ImageIO.read(new File("plugins\\Wiki\\images\\" + imageId + ".png"));
        }catch (Exception e){
            Util.logger.error("Failed to read image:"+imageId + ".png, using missing texture.",e);
            BufferedImage bi=new BufferedImage(150,50,BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g=(Graphics2D) bi.getGraphics();
            g.setColor(Color.black);
            g.fillRect(0,0,150,50);
            g.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
            g.setColor(Color.red);
            g.drawString("Image Not Found",10,25);
            return bi;
        }
    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(imageId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        imageId=(int)in.readObject();
    }
}
