package controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Consts {

    public static final String detailedFormat = "yyyy-MM-dd HH:mm:ss";
    public static final String detailedFormat2 = "yyyy-MM-dd HH:mm";
    public static final SimpleDateFormat myDateFormat = new SimpleDateFormat(detailedFormat);
    public static final SimpleDateFormat myDateFormat2 = new SimpleDateFormat(detailedFormat2);
    public static final SimpleDateFormat dateFormatMD = new SimpleDateFormat("MM/dd");
    public static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("YYYY/MM/dd");
    public static final String dateFormat = "yyyy-MM-dd";
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
    public static int firstDayOfWeek = Calendar.MONDAY;
    public static final String uploadUserImagePath = "/public/userImages/";
    public static final String uploadDocPath = "/FileCenter/docPath/";
    public static final String uploadRFIAttachPath = "/FileCenter/RFIAttachPath/";
    public static final String uploadPunchListAttachPath = "/FileCenter/PunchListAttachPath/";
    public static final String uploadContractPath = "/FileCenter/Contracts/";
    public static final String uploadInventoryPath = "/FileCenter/Inventory/";
    public static final String uploadMeetingPath = "/FileCenter/Meetings/";
    public static final String uploadEventPath = "/FileCenter/Event/";
    public static final String uploadEventPostPath = "/FileCenter/EventPost/";
    public static final String uploadMeetingTopicPath = "/FileCenter/Meetings/Topic/";
    public static final String uploadDailyLogPath = "/FileCenter/DailyLog/";
    public static final String uploadProjectPath = "/FileCenter/ProjectImage/";
    public static final String uploadDrawingImagePath = "/FileCenter/DrawingImagePath/";
    public static final String uploadDrawingPDFPath = "/FileCenter/DrawingPDFPath/";
    public static final String uploadPostPath = "/FileCenter/PostPath/";
    public static final String uploadCoverImagePath = "/FileCenter/Dashboard/cover/";
    public static final String uploadDashboardPath = "/FileCenter/Dashboard/";
    public static final String uploadPhoneIosPath = "/FileCenter/PhoneIos/";
    public static final String uploadAktStyleImagePath = "/FileCenter/AktStyle/";
    public static final String uploadAktImagePath = "/FileCenter/Akt/";
    public static final String uploadAktPdfPath = "/FileCenter/Akt/pdf/";
    public static final String uploadEMailPath = "/FileCenter/Email/";
    public static final String uploadGalleryPath = "/FileCenter/Gallery/";

    public static final int maxDescriptionLength = 10000;
    public static final int maxRiskAssLength = 4000;
    public static final int maxDescriptionLength2 = 8000;

    public static final String imageFileExtensions = "gif,jpg,jpeg,png,bmp,tiff,GIF,JPG,JPEG,PNG,BMP,";
    public static final String imageFileType = "image";
    public static final String imageFileIcon = "/public/images/fileIcon/picture-icon.png";
    public static final String pdfFileExtensions = "pdf,PDF,";
    public static final String pdfFileIcon = "/public/images/fileIcon/pdf-icon.png";
    public static final String wordFileExtensions = "docx,doc,DOCX,DOC,";
    public static final String wordFileIcon = "/public/images/fileIcon/word-icon.png";
    public static final String excelFileExtensions = "xls,xlsx,XLS,XLSX,";
    public static final String excelFileIcon = "/public/images/fileIcon/excel-icon.png";
    public static final String powerPointFileExtensions = "ppt,pptx,PPT,PPTX,";
    public static final String powerPointFileIcon = "/public/images/fileIcon/powerpoint-icon.png";
    public static final String autoCadFileExtensions = "dwg,dxf,dgn,DWG,DXF,DGN";
    public static final String autoCadFileIcon = "/public/images/fileIcon/autocad-icon.png";
    public static final String videoFileExtensions = "avi,wmv,rm,rmvb,mp4,mpeg,mkv,AVI,WMV,RM,RMVB,MP4,MPEG,MKV,";
    public static final String videoFileIcon = "/public/images/fileIcon/video-icon.png";
    public static final String viewableVideoExtensions = "flv,swf,FLV,SWF,";
    public static final String viewableVideoType = "viewableVideo";
    public static final String viewableVideoIcon = "/public/images/fileIcon/viewable-video-icon.png";
    public static final String otherFileIcon = "/public/images/fileIcon/other-icon.png";

    public static final String permissionDashboard = "dashboard";
    public static final String permissionGantt = "gantt";
    public static final String permissionMyPlan = "myPlan";
    public static final String permissionDailyLog = "dailyLog";
    public static final String permissionMail = "mail";
    public static final String permissionRFI = "rfi";
    public static final String permissionGreatePunchList = "punchlist";
    public static final String permissionReport = "report";
    public static final String permissionMeeting = "meeting";
    public static final String permissionMonitorDrawing = "drawing";
    public static final String permissionGallery = "gallery";
    public static final String permissionFileShare = "file";
    public static final String permissionBudget = "budget";
    public static final String permissionContract = "contract";
    public static final String permissionInventory = "inventory";
    public static final String permissionInventoryOther = "inventory_other";
    public static final String permissionMap = "map";
    public static final String permissionForum = "forum";
    public static final String permissionAccount = "account";
    public static final String permissionAdmin = "admin";

    public static final boolean defugLog = false;
    public static final String permissionOrganization = "organization";

    public static final int MEETING_TOPIC = 1;
    public static final int DAILYLOG_WEATHER = 2;
    public static final int DAILYLOG_MATERIAL = 3;
    public static final int DAILYLOG_MANPOWER = 4;
    public static final int DAILYLOG_EQUIPMENT = 5;
    public static final int DAILYLOG_WASTE = 6;
    public static final int DAILYLOG_GUEST = 7;
    public static final int DAILYLOG_IDEA = 8;
    public static final int DAILYLOG_PROBLEM_NOTE = 9;
    public static final int DAILYLOG_DELIVERY = 10;
    public static final int DAILYLOG_SAFETY = 11;
    public static final int DAILYLOG_INSPECTION = 12;
    public static final int DAILYLOG_MATERIAL_LOSS = 13;
    public static final int DAILYLOG_TECHNICAL_PROBLEM = 14;
    public static final int DAILYLOG_WORK = 15;
    public static final int POST = 16;
    public static final int POST_COMMENT = 17;
    public static final int MY_PLAN = 18;
}
