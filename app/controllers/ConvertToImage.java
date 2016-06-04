package controllers;

import play.Play;
import sun.misc.BASE64Decoder;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: enkhamgalan
 * Date: 12/15/14
 * Time: 10:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConvertToImage extends JPanel {

    public boolean convert(String path, String fileExtension, int w, int h, String type, boolean tumb) {
        Image image;
        int x, y, q = 1;
        try {
            try {
                path = path.substring(1, path.length());
                File file = new File(path + "." + fileExtension);
                image = ImageIO.read(file);
            } catch (Exception eo) {
                System.out.println(eo);
                return false;
            }
            int w2, h2, ww, hh;
            w2 = image.getWidth(this);
            h2 = image.getHeight(this);
            if (type.equals("ratio")) {
                float rw = w2 / w;
                float rh = h2 / h;
                if (rh > rw) rh = rw;
                w = (int) (rh * w);
                h = (int) (rh * h);
            } else if (type.equals("rect")) {
                if (w2 < h2) {
                    w = w2;
                    h = w2;
                } else {
                    w = h2;
                    h = h2;
                }
            }
            BufferedImage bit = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D bigt = bit.createGraphics();
            bigt.setBackground(Color.white);
            bigt.clearRect(0, 0, w, h);

            hh = (h2 * w) / w2;
            ww = (w2 * h) / h2;
            if (hh <= h) {
                y = (h - hh) / 2;
                x = 0;
                h2 = hh;
                w2 = w;
            } else {
                x = (w - ww) / 2;
                y = 0;
                h2 = h;
                w2 = ww;
            }
            bigt.drawImage(image, x, y, w2, h2, this);
            bit.flush();
            Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(q);   // an integer between 0 and 1
            if (tumb) path += "t.jpg";
            else path += ".jpg";
            File selectedfile = new File(path);
            IIOImage imageio = new IIOImage(bit, null, null);
            FileImageOutputStream output = new FileImageOutputStream(selectedfile);
            writer.setOutput(output);
            writer.write(null, imageio, iwp);
            writer.dispose();
            output.close();
        } catch (Exception ef) {
            System.out.println("Save Image error:" + ef);
            return false;
        }
        return true;
    }

    public boolean convertCrop(String path, String fileExtension, int x, int y, int w, int h) {
        Image image = null;
        int q = 1;
        try {
            try {
                path = path.substring(1, path.length());
                File file = new File(path + "." + fileExtension);
                image = ImageIO.read(file);
            } catch (Exception eo) {
                System.out.println(eo);
                return false;
            }
            int w2, h2;
            w2 = image.getWidth(this);
            h2 = image.getHeight(this);
//            System.out.println("x: " + x + " y: " + y + " w: " + w + " h: " + h + " w2: " + w2 + " h2: " + h2);
            BufferedImage bit = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_RGB);
            Graphics2D bigt = bit.createGraphics();
            bigt.setBackground(Color.white);
            bigt.clearRect(0, 0, w2, h2);
            bigt.drawImage(image, 0, 0, w2, h2, this);
            bit.flush();
            bit = bit.getSubimage(x, y, w, h);
            Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(q);   // an integer between 0 and 1
            path += "c.jpg";
            File selectedfile = new File(path);
            IIOImage imageio = new IIOImage(bit, null, null);
            FileImageOutputStream output = new FileImageOutputStream(selectedfile);
            writer.setOutput(output);
            writer.write(null, imageio, iwp);
            writer.dispose();
            output.close();
        } catch (Exception ef) {
            System.out.println("Save Image error:" + ef);
            return false;
        }
        return true;
    }

    public boolean convertRectEllipse(String path, String fileExtension, int x, int y, int w, int h) {
        Image image;
        try {
            try {
                path = path.substring(1, path.length());
                File file = new File(path + "." + fileExtension);
                image = ImageIO.read(file);
            } catch (Exception eo) {
                System.out.println(eo);
                return false;
            }
            BufferedImage bit = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bit.createGraphics();
            path += ".png";
            g2.clip(new Ellipse2D.Double(0, 0, w, h));
            g2.drawImage(image, -x, -y, null);
            g2.dispose();
            File selectedfile = new File(path);
            ImageIO.write(bit, "PNG", selectedfile);
        } catch (Exception ef) {
            System.out.println("Save Image error:" + ef);
            return false;
        }
        return true;
    }

    public String getSizeForDrawing(String path) {
        Image image;
        try {
            try {
                path = path.substring(1, path.length());
                File file = new File(path);
                image = ImageIO.read(file);
                return "width = '" + String.valueOf(image.getWidth(this)) + "' height='" + String.valueOf(image.getHeight(this)) + "'";
            } catch (Exception eo) {
                System.out.println(eo);
                return "error";
            }
        } catch (Exception ef) {
            System.out.println("Save Image error:" + ef);
            return "error";
        }
    }

    public BufferedImage convertFileToBufferedImage(String path) {
        String absolutePath = Functions.cleanUrl(Play.applicationPath.getAbsolutePath() + "/" + path);
        BufferedImage newImage = null;
        try {
            BufferedImage in = ImageIO.read(new File(absolutePath));

            newImage = new BufferedImage(
                    in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();
        } catch (Exception e) {
            System.err.print(e);
        }
        return newImage;
    }
}
