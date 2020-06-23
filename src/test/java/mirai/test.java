package mirai;

import com.zjs.Util;

import javax.swing.*;
import java.awt.*;

public class test {
    public static void main(String[] args){
        JFrame frame=new JFrame();
        Util.generateHelpImage();
        JPanel p=new JPanel(){
            @Override
            public void paint(Graphics g){
                super.paint(g);
                g.drawImage(Util.helpImage,50,50,this);
            }
        };
        frame.add(p);
        frame.setVisible(true);
    }
}
