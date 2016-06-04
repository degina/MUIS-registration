package controllers;

import models.Akt;
import models.FileShare;
import models.FolderStructure;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.mvc.Controller;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Calendar;
import java.util.Date;

import sun.misc.BASE64Encoder;

public class FileUploader extends Controller {
    public static void uploadFile(String uploadPath, String filename) throws Exception {
        String extension = "null";
        String FileNameOrig = "";
        boolean success = true;
        String filesize = "0";
        if (request.isNew) {
            FileOutputStream moveTo = null;
            try {
                String[] length = filename.split("\\.");
                for (int fi = 0; fi < length.length - 1; fi++) FileNameOrig += length[fi];
                extension = length[length.length - 1];
                Date currentDate = new Date();
                filename = currentDate.getTime() + "";
                InputStream data = request.body;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                uploadPath = uploadPath + calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/";
                String absolutePath = Functions.cleanUrl(Play.applicationPath.getAbsolutePath() + "/" + uploadPath);

                File f = new File(absolutePath);
                if (!f.exists()) {
                    if (!f.mkdirs()) throw new Exception("Unable to create upload dir");
                }
                absolutePath = Functions.cleanUrl(absolutePath + "/" + filename + "." + extension);
                File copy = new File(absolutePath);
                moveTo = new FileOutputStream(copy);

                IOUtils.copy(data, moveTo);
                moveTo.close();
                filesize = Functions.getFloatFormat((float) copy.length() / 1024 / 1024, 2);
                if (isImage(extension)) {        //to small
                    ConvertToImage convertToImage = new ConvertToImage();
                    if (!uploadPath.startsWith("/FolderShare/"))
                        convertToImage.convert(uploadPath + "/" + filename, extension, 400, 400, "none", true);
                }
                uploadPath += filename;
            } catch (Exception ex) {
                ex.printStackTrace();
                if (moveTo != null)
                    moveTo.close();
                success = false;
            }
        }
        renderJSON("{success: " + success + ",filedir:'" + uploadPath + "',filename:'" + FileNameOrig + "',extension:'" + extension + "',filesize:'" + filesize + "'}");
    }

    public static boolean isImage(String ext) {
        return (Consts.imageFileExtensions.contains(ext));
    }

    public static void deleteUploadFile(String fileDir, String extension) {
        try {
            File file = new File(Play.applicationPath.getAbsolutePath() + fileDir + "." + extension);
            if (file.exists()) file.delete();
            if (!fileDir.startsWith("/FolderShare/") && isImage(extension)) {
                file = new File(Play.applicationPath.getAbsolutePath() + fileDir + "t.jpg");
                if (file.exists()) file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadFileCustom(String filename, String uploadPath, String ratio) throws Exception {
        String uploadedFileName, fileexen;
        int rw, rh;
        rw = Integer.parseInt(ratio.split("x")[0]);
        rh = Integer.parseInt(ratio.split("x")[1]);
        boolean success = true;
        if (request.isNew) {
            FileOutputStream moveTo = null;
            try {
                InputStream data = request.body;
                String[] length = filename.split("\\.");
                fileexen = length[length.length - 1];
                Date currentDate = new Date();
                filename = currentDate.getTime() + "";
                uploadedFileName = Play.applicationPath.getAbsolutePath() + uploadPath + filename + "." + fileexen;
                moveTo = new FileOutputStream(new File(uploadedFileName));
                IOUtils.copy(data, moveTo);
                moveTo.close();
                ConvertToImage convertToImage = new ConvertToImage();
                if (!convertToImage.convert(uploadPath + filename, fileexen, rw, rh, "none", false))
                    new File(Play.applicationPath.getAbsolutePath() + uploadPath + filename + "." + fileexen).delete();
                if (!fileexen.endsWith("jpg"))
                    new File(Play.applicationPath.getAbsolutePath() + uploadPath + filename + "." + fileexen).delete();
            } catch (Exception ex) {
                success = false;
                ex.printStackTrace();
                if (moveTo != null)
                    moveTo.close();
            }
        }
        renderJSON("{success: " + success + ",filedir:'" + uploadPath + "',filename:'" + filename + "'}");
    }

    public static String decodeToImage(String path, String imageString) {
        BufferedImage image;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            path = path + calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/";
            File f = new File(Play.applicationPath.getAbsolutePath() + "/" + path);
            if (!f.exists()) {
                if (!f.mkdirs()) throw new Exception("Unable to create upload dir");
            }
            path += calendar.getTimeInMillis() + ".png";
            ImageIO.write(image, "png", new File(Play.applicationPath.getAbsolutePath() + "/" + path));
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void uploadPDF(String pdf, Long id, int ftype, String name) {
        String extension = "pdf";
        String filename = "";
        String uploadPath = "/FolderShare/";
        String dir = "";
        FileOutputStream moveTo = null;
        try {
            //System.out.println("uploadpdf2");
            Date currentDate = new Date();
            filename = currentDate.getTime() + "";

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            uploadPath = uploadPath + calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/";
            dir = uploadPath + filename;
            String absolutePath = Functions.cleanUrl(Play.applicationPath.getAbsolutePath() + "/" + uploadPath);
            //System.out.println("uploadpdf3");
            File f = new File(absolutePath);
            if (!f.exists()) {
                if (!f.mkdirs()) throw new Exception("Unable to create upload dir");
            }
            absolutePath = Functions.cleanUrl(absolutePath + "/" + filename + "." + extension);
            moveTo = new FileOutputStream(new File(absolutePath));
            //System.out.println("uploadpdf4");
            //decode base64 to pdf
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] decodedBytes = decoder.decodeBuffer(pdf);

            //IOUtils.copy(data, moveTo);
            moveTo.write(decodedBytes);
            moveTo.close();
            //System.out.println("uploadpdf5");

            Akt akt = Akt.findById(id);
            // huuchin pdf-g delete hiij bn
            File delfile = new File(Play.applicationPath.getAbsolutePath() + akt.dir + "." + "pdf");
            if (delfile.exists()) delfile.delete();

            akt.dir = dir;
            akt.save();
            // System.out.println("uploadpdf6");

            FolderStructure folderStructure = FolderStructure.findById(akt.folderStructure.id);

            FileShare file;
            if (ftype == 2 || akt.fileShare == null) file = new FileShare();
            else file = FileShare.findById(akt.fileShare.id);
            file.folderStructure = folderStructure;
            file.ftype = 0L;
            file.dir = akt.dir;
            file.name = name + "_" + akt.queue;
            file.extension = extension;
            file.fileSize = f.length() + "";
            file.description = "";
            file.sendUserList = "";
            file.akt = akt;

            if (ftype == 2 || akt.fileShare == null) {
                file.uploader = akt.uploader;
                file.create();
                akt.fileShare = file;
                akt.save();
            } else {
                file.uploader = akt.uploader;
                file.save();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String encodeToString(BufferedImage image) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    public static String encodeToBase64(String path) {
        ConvertToImage convertToImage = new ConvertToImage();
        return "data:image/png;base64," + encodeToString(convertToImage.convertFileToBufferedImage(path));
    }
}
