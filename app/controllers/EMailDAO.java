package controllers;

import controllers.EMailClass.EmailContentData;
import controllers.EMailClass.EmailData;
import controllers.EMailClass.MailboxMetaData;
import org.apache.commons.io.IOUtils;
import play.Play;

import javax.mail.*;
import java.io.*;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by enkhamgalan on 8/1/15.
 */
public class EMailDAO implements Serializable {

    private Store store;
    private String protocol;
    private String mailServer;
    private int port;
    private String userName;
    private String password;

    public EMailDAO(String protocol, String mailServer, int port,
                    String userName, String password) {
        this.protocol = protocol;
        this.mailServer = mailServer;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public void connect() throws MessagingException {
        // login to the IMAP server
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);
//      session.setDebug(true);
        store = session.getStore(protocol);
        store.connect(mailServer, port, userName, password);
    }

    public List<MailboxMetaData> getFolders() throws MessagingException,
            UnsupportedEncodingException {
        List<Folder> folders = Arrays.asList(store.getDefaultFolder().list());
        List<MailboxMetaData> mailboxes = new ArrayList<MailboxMetaData>();
        addToList(mailboxes, folders);
        return mailboxes;
    }

    private void addToList(List<MailboxMetaData> mailboxes, List<Folder> folders)
            throws MessagingException, UnsupportedEncodingException {
        if (folders == null) {
            return;
        }
        for (Folder folder : folders) {
            MailboxMetaData mailbox = new MailboxMetaData();
            mailbox.id = getFullName(folder);
            mailbox.text = folder.getName();
            folder.getUnreadMessageCount();
            mailboxes.add(mailbox);
            addToList(mailbox.children, Arrays.asList(folder.list()));
        }
    }

    public String getFullName(Folder folder) throws MessagingException,
            UnsupportedEncodingException {
        String folderName = URLEncoder.encode(folder.getName(), "UTF-8");
        while (folder.getParent() != null) {
            folder = folder.getParent();
            folderName = URLEncoder.encode(folder.getName(), "UTF-8") + "/" + folderName;
        }
        return folderName.substring(1);
    }

    public void close() throws MessagingException {
        store.close();
    }

    public boolean isConnected() {
        return store != null && store.isConnected();
    }

    public Folder createFolder(String name) throws MessagingException {

        Folder folder = store.getFolder(name);
        try {
            if (!folder.exists()) {
                folder.create(Folder.HOLDS_MESSAGES);
            }
        } catch (FolderNotFoundException e) {
            folder.create(Folder.HOLDS_MESSAGES);
        }
        return folder;
    }

    public void deleteMessage(Message message, String trashMailbox)
            throws MessagingException {
        Folder folder = message.getFolder();
        // If the folder is already open, close it?
        if (folder.isOpen()) {
            folder.close(true);
        }
        folder.open(Folder.READ_WRITE);
        if (trashMailbox != null) {
            Folder deletedFolder = store.getFolder(trashMailbox);
            if (!deletedFolder.exists()) {
                deletedFolder.create(Folder.HOLDS_MESSAGES);
            }
            deletedFolder.open(Folder.READ_WRITE);
            if (!trashMailbox.equals(folder.getName())) {
                folder.copyMessages(new Message[]{message}, deletedFolder);
                message.setFlag(Flags.Flag.SEEN, true);
            }
        }
        message.setFlag(Flags.Flag.DELETED, true);
        folder.expunge();
    }

    public Folder getDefaultFolder() throws MessagingException {

        return store.getDefaultFolder();
    }

    public Folder getFolder(String name) throws MessagingException {
        return store.getFolder(name);
    }

    public Message[] getMessages(Folder folder, int start, int end)
            throws MessagingException {
        final int numberOfEmail = folder.getMessageCount();
        int s = (numberOfEmail - end);
        int e = (numberOfEmail - start);
        if (s == 0) s = 1;
        if (e == 0) e = 1;
        return folder.getMessages(s, e);
    }

    public Message getMessage(String[] folderNames, int mode, int messageId)
            throws MessagingException {
        Folder folder = openFolder(folderNames, mode);
        return folder.getMessage(messageId);
    }

    public Folder openFolder(String folderName, int mode)
            throws MessagingException {
        Folder folder = getFolder(folderName);
        if (!folder.isOpen()) {
            folder.open(mode);
        }
        return folder;
    }

    public Folder openFolder(String[] folderNames, int mode) throws MessagingException {
        List<String> folders = new ArrayList<String>(Arrays.asList(folderNames));
        folders.remove(0);

        Folder folder = getFolder(folderNames[0]);
        for (String f : folders) {
            folder = folder.getFolder(f);
        }
        if (!folder.isOpen()) {
            folder.open(mode);
        }

        return folder;
    }

    public Message findMessage(Folder folder, int messageId)
            throws MessagingException {
        System.out.println("messageIdmessageIdmessageIdmessageIdmessageIdmessageIdmessageIdmessageId = " + messageId + "=>" + folder.getMessageCount());
        return folder.getMessage(messageId);
    }

    public EmailData findEmail(Folder folder, int messageId)
            throws MessagingException, IOException {
        // TODO: Should be somewhere else
        Message message = findMessage(folder, messageId);
        return new EmailData(message, false);
    }

    public EmailContentData getContent(EmailContentData contentData, Message message, Long uid, boolean createAttach) throws MessagingException,
            IOException {
        return getContent(contentData, message.getFolder().getName(), message.getMessageNumber(), message, "text/html", uid, createAttach);
    }

    public EmailContentData getContent(EmailContentData contentData, Message message, String contentType, Long uid, boolean createAttach) throws MessagingException,
            IOException {
        return getContent(contentData, message.getFolder().getName(), message.getMessageNumber(), message, contentType, uid, createAttach);
    }

    /**
     * Return the content as text
     *
     * @param message
     * @param contentType
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public EmailContentData getContent(EmailContentData contentData, String mailbox, int messageId, Part message, String contentType, Long uid, boolean createAttach) throws MessagingException,
            IOException {
        if (contentData == null) contentData = new EmailContentData();
        if (message.getContentType().startsWith(contentType)) {
            contentData.body = (String) message.getContent();
            return contentData;
        } else if (message.getContent() != null && message.getContent() instanceof Multipart) {
            Multipart part = (Multipart) message.getContent();
            contentData.body = "";
            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);
                if (Message.INLINE.equals(bodyPart.getDisposition())) {
                    String extension = bodyPart.getContentType().replaceAll(";.*", "");
                    extension = extension.substring(extension.indexOf("/") + 1);
                    if (Consts.imageFileExtensions.contains(extension)) {
                        String contentId = bodyPart.getHeader("Content-Id")[0];
                        contentId = contentId.substring(1, contentId.length() - 1);
                        try {
                            Date currentDate = new Date();
                            createAttachFile(bodyPart.getDataHandler().getInputStream(), Consts.uploadEMailPath + uid + "/", currentDate.getTime() + "." + extension);
                            contentData.body = contentData.body.replace("cid:" + contentId, Consts.uploadEMailPath + uid + "/" + currentDate.getTime() + "." + extension);
                        } catch (Exception e) {
                            System.out.println("Emails INLINE Img: " + e);
                        }
                    }
//                    bodyPart.getDataHandler().getInputStream();
                } else if (Message.ATTACHMENT.equals(bodyPart.getDisposition())) {
                    String attData, filename;
                    filename = URLEncoder.encode(bodyPart.getFileName(), "UTF-8");
                    String[] length = filename.split("\\.");
                    filename = "";
                    String extension = length[length.length - 1];
                    for (int fi = 0; fi < length.length - 1; fi++) filename += length[fi];
                    if (!createAttach) {
                        if (Consts.imageFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.imageFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.wordFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.wordFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.excelFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.excelFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.powerPointFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.powerPointFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.autoCadFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.autoCadFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.pdfFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.pdfFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.videoFileExtensions.contains(extension))
                            attData = "<img src='" + Consts.videoFileIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else if (Consts.viewableVideoExtensions.contains(extension))
                            attData = "<img src='" + Consts.viewableVideoIcon + "' class='localContentType' style='width:29px;height:29px'>";
                        else attData = "<i class=fa fa-paperclip pull-left fa-2x'</i>";
                        contentData.attach += "<div class='media attmedia'>" + attData +
                                "<div class='media-body'><div><a href='#' folder='" + mailbox + "' mid='" + messageId + "' filename='" +
                                filename + "' extension='" + extension + "' filenameOrg='" + URLEncoder.encode(bodyPart.getFileName(), "UTF-8") + "' class='strong text-regular'>" + filename + "." + extension + "<img style='display:none' src='/public/images/loading.gif'></a></div>" +
                                "<span>" + getAttachentSize(bodyPart.getSize()) + "</span><div class='clearfix'></div></div></div>";
                    } else { // Drafts, Reply, Forward
                        String pathIcon;
                        if (Consts.imageFileExtensions.contains(extension)) pathIcon = Consts.imageFileIcon;
                        else if (Consts.wordFileExtensions.contains(extension)) pathIcon = Consts.wordFileIcon;
                        else if (Consts.excelFileExtensions.contains(extension)) pathIcon = Consts.excelFileIcon;
                        else if (Consts.powerPointFileExtensions.contains(extension))
                            pathIcon = Consts.powerPointFileIcon;
                        else if (Consts.autoCadFileExtensions.contains(extension)) pathIcon = Consts.autoCadFileIcon;
                        else if (Consts.pdfFileExtensions.contains(extension)) pathIcon = Consts.pdfFileIcon;
                        else if (Consts.videoFileExtensions.contains(extension)) pathIcon = Consts.videoFileIcon;
                        else if (Consts.viewableVideoExtensions.contains(extension))
                            pathIcon = Consts.viewableVideoIcon;
                        else pathIcon = Consts.otherFileIcon;
                        try {
                            String absolutePath = Consts.uploadEMailPath + uid + "/" + filename;
                            contentData.attach += "<li ><span ><img src = '" + pathIcon + "' class='imgIcon' " +
                                    "filedir = '" + absolutePath + "' filename = '" + filename + "' extension = '" + extension + "' " +
                                    "filesize = '" + createAttachFile(bodyPart.getDataHandler().getInputStream(), Consts.uploadEMailPath + uid + "/", filename + "." + extension) + "' nowupload = '1' >" +
                                    "<img height = 12 class='deleteUploadedImage' width = '12' " +
                                    "src = '/public/images/delete-image.png' filedir = '" + absolutePath + "' extension = '" + extension + "'></span ></li >";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else
                    contentData.body += getContent(contentData, mailbox, messageId, bodyPart, contentType, uid, createAttach).body;
            }
            return contentData;
        }
        if (message.getContent() != null && message.getContent() instanceof Part) {
            if (!Message.ATTACHMENT.equals(message.getDisposition())) {
                return getContent(contentData, mailbox, messageId, (Part) message.getContent(), contentType, uid, createAttach);
            }
        }

        return contentData;
    }

    public String createAttachFile(InputStream is, String absolutePath, String filename) throws Exception {
        File file = new File(Play.applicationPath.getAbsolutePath() + absolutePath);
        if (!file.exists()) {
            if (!file.mkdirs()) throw new Exception("Unable to create upload dir");
        }
        absolutePath += filename;
        file = new File(Play.applicationPath.getAbsolutePath() + absolutePath);
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(is, fos);
        fos.close();
        return Functions.getFloatFormat((float) file.length() / 1024, 2);
    }

    public InputStream getAttachInputStream(Part message, String filename) throws MessagingException,
            IOException {
        if (message.getContent() != null && message.getContent() instanceof Multipart) {
            Multipart part = (Multipart) message.getContent();
            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);
                if (Message.ATTACHMENT.equals(bodyPart.getDisposition())) {
                    if (URLEncoder.encode(bodyPart.getFileName(), "UTF-8").equals(filename))
                        return bodyPart.getDataHandler().getInputStream();
                }
            }
        }
        return null;
    }


    public byte[] getContent(Part message, String cid, String contentType) throws MessagingException,
            IOException {

        if (message.getContent() != null && message.getContent() instanceof Multipart) {
            Multipart part = (Multipart) message.getContent();
            for (int i = 0; i < part.getCount(); i++) {
                BodyPart bodyPart = part.getBodyPart(i);
                if (Message.ATTACHMENT.equals(bodyPart.getDisposition())) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(bodyPart.getDataHandler().getInputStream(), out);
                    return out.toByteArray();
                }
            }
        }
        return null;
    }

    public String getAttachentSize(long sizeInBytes) {
        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(1);
        double SPACE_KB = 1024;
        double SPACE_MB = 1024 * SPACE_KB;
        double SPACE_GB = 1024 * SPACE_MB;
        if (sizeInBytes < SPACE_MB) {
            return nf.format(sizeInBytes / SPACE_KB) + " KB";
        } else if (sizeInBytes < SPACE_GB) {
            return nf.format(sizeInBytes / SPACE_MB) + " MB";
        }
        return "0";
    }
}
