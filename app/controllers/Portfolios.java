package controllers;

import com.google.gson.JsonObject;
import models.*;
import play.Play;
import play.mvc.With;

import java.util.*;

/**
 * Created by Enkh-Amgalan on 3/14/15.
 */
@With(Secure.class)
public class Portfolios extends CRUD {
    public static void blank(Long id) {
        User user = Users.getUser();
        List<PortfolioCategory> projectCategories = PortfolioCategory.findAll();
        List<PortfolioStage> stages = PortfolioStage.findAll();
        List<UserTeam> userTeams = UserTeam.find("contractor=false order by queue, name").fetch();
        if (id == null) {
            if (!user.isAdmin()) forbidden();
            render(projectCategories, stages, userTeams);
        } else {
            Portfolio portfolio = Portfolio.findById(id);
            if (!(user.isAdmin() || portfolio.owner.id.compareTo(user.id) == 0)) forbidden();
            render(portfolio, projectCategories, stages, userTeams);
        }
    }

    public static void list() {
        User user = Users.getUser();
        boolean admin = user.isAdmin();
        String qr = "SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE (f.owner.id = " + user.id + " OR ( p.id = f.project.id AND p.id = c.project.id AND c.user.id=" + user.id + ")) ";
        List<Portfolio> portfolios = Portfolio.find(qr).fetch();
        session.put("projectId", 0L);
        Long selProj = Users.pid();
        List<PortfolioStage> stages = PortfolioStage.findAll();
        List<PortfolioCategory> categories = PortfolioCategory.findAll();
        render(portfolios, selProj, admin, stages, categories);
    }

    public static void selectProject(Long id) {
        session.put("projectId", id);
        Project project = Project.findById(id);
        session.put("projectName", project.name);
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionDashboard) > 0) Dashboard.index();
        else blank(project.portfolio.id);
    }

    public static void create(Long portfolioId, Long selectStage, Long selectCategory, boolean statusRadio, String projectName,
                              Long selectOwner, String address, String description, String[] hariutsagchName, String[] hariutsagchTushaal,
                              String[] hariutsagchUtas, String mapLon, String mapLat, Date testStartDate, Date contractDate,
                              Date serviceStartDate, String[] amrahOdor, String[] amrahGarig,
                              String projectImageUrl, float x, float y, float w, float h, float w2, float h2) {
        User user = Users.getUser();
        PortfolioCategory category = PortfolioCategory.findById(selectCategory);
        PortfolioStage stage = PortfolioStage.findById(selectStage);
        ConvertToImage convertToImage = new ConvertToImage();
        float x_ratio = (1200 / w2);
        float y_ratio = (800 / h2);
        if (portfolioId == null) {
            if (!user.isAdmin()) forbidden();
            convertToImage.convertCrop(projectImageUrl, "jpg", (int) (x * x_ratio), (int) (y * y_ratio), (int) (w * x_ratio), (int) (h * y_ratio));
            Portfolio portfolio = new Portfolio();
            Project project = new Project();
            project.code = "";
            project.name = projectName;
            project.owner = Users.getUser();
            project.startDate = new Date();
            project.finishDate = project.startDate;
            project.duration = 1l;
            project.status = "STATUS_ACTIVE";
            project.depends = "";
            project.holidays = "";
            if (amrahOdor.length > 0) {
                for (int d = 0; d < amrahOdor.length; d++) {
                    project.holidays += amrahOdor[d] + "#";
                }
            }
            project.weekend = "";
            if (amrahGarig != null && amrahGarig.length > 0) {
                for (int d = 0; d < amrahGarig.length; d++) {
                    project.weekend += amrahGarig[d] + "#";
                }
            }
            project.startIsMilestone = false;
            project.owner = Users.getUser();
            project.folderStructures = new ArrayList<FolderStructure>();
            List<FolderBasic> folderBasics = FolderBasic.findAll();
            if (folderBasics.size() > 0) {
                FolderStructure folderStructureParent = null;
                FolderStructure folderStructureParent2 = null;
                for (FolderBasic basic : folderBasics) {
                    FolderStructure folderStructure = new FolderStructure();
                    folderStructure.queue = basic.queue;
                    folderStructure.folderType = basic.folderType;
                    folderStructure.folderColor = basic.folderColor;
                    folderStructure.name = basic.name;
                    if (basic.id.intValue() == 1 || folderStructure.name.equals("Үндсэн хавтас"))
                        folderStructure.systemFolder = true;
                    if (basic.parentFolder) {
                        if (basic.folderBasic == null) folderStructureParent = folderStructure;
                        else {
                            folderStructure.folderStructure = folderStructureParent;
                            if (basic.folderBasic != null && basic.folderBasic.parentFolder)
                                folderStructureParent2 = folderStructure;
                        }
                    } else {
                        if (basic.folderBasic.folderBasic == null)
                            folderStructure.folderStructure = folderStructureParent;
                        else folderStructure.folderStructure = folderStructureParent2;
                    }
                    folderStructure.project = project;
                    project.folderStructures.add(folderStructure);
                }
            } else {
                FolderStructure folderStructure = new FolderStructure();
                folderStructure.queue = 1L;
                folderStructure.folderType = FolderType.findById(1L);
                folderStructure.folderColor = FolderColor.findById(1L);
                folderStructure.name = "Үндсэн хавтас";
                folderStructure.systemFolder = true;
                folderStructure.project = project;
                project.folderStructures.add(folderStructure);
            }
            project.projectCounter = new ProjectCounter();
            project.create();
            portfolio.category = category;
            portfolio.stage = stage;
            portfolio.address = address;
            portfolio.description = description;
            portfolio.isActive = statusRadio;
            portfolio.contractDate = contractDate;
            portfolio.testStartDate = testStartDate;
            portfolio.serviceStartDate = serviceStartDate;
            portfolio.mapLat = mapLat;
            portfolio.mapLon = mapLon;
            portfolio.contacts = new ArrayList<PortfolioContact>();
            portfolio.project = project;
            portfolio.imageUrl = projectImageUrl;
            portfolio.owner = User.findById(selectOwner);
            portfolio.x = (int) x;
            portfolio.y = (int) y;
            portfolio.w = (int) w;
            portfolio.h = (int) h;
            if (CompanyConf.copy_orgChart) FunctionDatabase.createOrgChartFromCompanyOrg(project);
            portfolio.create();
            if (hariutsagchName != null && hariutsagchName.length > 0) {
                for (int i = 0; i < hariutsagchName.length; i++) {
                    PortfolioContact serviceUser = new PortfolioContact(hariutsagchName[i], hariutsagchTushaal[i], hariutsagchUtas[i], portfolio);
                    serviceUser.create();
                }
            }
        } else {
            Portfolio portfolio = Portfolio.findById(portfolioId);
            if (!(user.isAdmin() || portfolio.owner.id.compareTo(user.id) == 0)) forbidden();
            if (!projectImageUrl.equals(portfolio.imageUrl)) {
                Functions.deleteFileSingle(portfolio.imageUrl + ".jpg");
                Functions.deleteFileSingle(portfolio.imageUrl + "c.jpg");
                portfolio.imageUrl = projectImageUrl;
                convertToImage.convertCrop(projectImageUrl, "jpg", (int) (x * x_ratio), (int) (y * y_ratio), (int) (w * x_ratio), (int) (h * y_ratio));
            } else if (x != portfolio.x || y != portfolio.y) {
                Date now = new Date();
                java.io.File FromDir = new java.io.File(Play.applicationPath.getAbsolutePath() + portfolio.imageUrl + ".jpg");
                java.io.File FromDir2 = new java.io.File(Play.applicationPath.getAbsolutePath() + portfolio.imageUrl + "c.jpg");
                portfolio.imageUrl = Consts.uploadProjectPath + now.getTime();
                convertToImage.convertCrop(projectImageUrl, "jpg", (int) (x * x_ratio), (int) (y * y_ratio), (int) (w * x_ratio), (int) (h * y_ratio));
                java.io.File ToDir = new java.io.File(Play.applicationPath.getAbsolutePath() + portfolio.imageUrl + ".jpg");
                java.io.File ToDir2 = new java.io.File(Play.applicationPath.getAbsolutePath() + portfolio.imageUrl + "c.jpg");
                FromDir.renameTo(ToDir);
                FromDir2.renameTo(ToDir2);
                portfolio.x = (int) x;
                portfolio.y = (int) y;
                portfolio.w = (int) w;
                portfolio.h = (int) h;
            }
            if (!portfolio.project.name.equals(projectName)) {
                portfolio.project.name = projectName;
                portfolio.project._save();
            }
            portfolio.project.holidays = "";
            if (amrahOdor != null && amrahOdor.length > 0) {
                for (int d = 0; d < amrahOdor.length; d++) {
                    portfolio.project.holidays += amrahOdor[d] + "#";
                }
            }
            portfolio.project.weekend = "";
            if (amrahGarig != null && amrahGarig.length > 0) {
                for (int d = 0; d < amrahGarig.length; d++) {
                    portfolio.project.weekend += amrahGarig[d] + "#";
                }
            }
            portfolio.project._save();
            portfolio.category = category;
            portfolio.stage = stage;
            portfolio.address = address;
            portfolio.description = description;
            portfolio.isActive = statusRadio;
            portfolio.contractDate = contractDate;
            portfolio.testStartDate = testStartDate;
            portfolio.serviceStartDate = serviceStartDate;
            portfolio.mapLat = mapLat;
            portfolio.mapLon = mapLon;
            portfolio.category = category;
            portfolio.isActive = statusRadio;
            portfolio.owner = User.findById(selectOwner);
            portfolio._save();

            List<PortfolioContact> portfolioContacts = PortfolioContact.find("portfolio.id=?1", portfolio.id).fetch();
            for (PortfolioContact productD : portfolioContacts)
                productD._delete();
            if (hariutsagchName != null && hariutsagchName.length > 0) {
                for (int i = 0; i < hariutsagchName.length; i++) {
                    PortfolioContact serviceUser = new PortfolioContact(hariutsagchName[i], hariutsagchTushaal[i], hariutsagchUtas[i], portfolio);
                    serviceUser.create();
                }
            }
        }
        list();
    }

    public static void delete(Long id) {
        Portfolio portfolio = Portfolio.findById(id);
        User user = Users.getUser();
        if (!(user.isAdmin() || portfolio.owner.id.compareTo(user.id) == 0)) forbidden();
        try {
            Functions.deleteFileSingle(portfolio.imageUrl + "c.jpg");
            Functions.deleteFileSingle(portfolio.imageUrl + ".jpg");
            portfolio.project._delete();
            list();
        } catch (Exception e) {
            list();
        }
    }

    public static void settings() {
        User user = Users.getUser();
        if (!user.isAdmin()) forbidden();
        List<PortfolioStage> stages = PortfolioStage.findAll();
        List<PortfolioCategory> categories = PortfolioCategory.findAll();
        render(categories, stages);
    }

    public static void saveSettings(String[] stageName, String[] stageColor, Long[] stageId, String[] categoryName, Long[] categoryId,
                                    String[] factoryName, Long[] factoryId) {
        for (int i = 0; i < stageId.length; i++) {
            if (stageId[i] == 0) {
                PortfolioStage stage = new PortfolioStage(stageName[i], stageColor[i]);
                stage.create();
            } else {
                PortfolioStage stage = PortfolioStage.findById(stageId[i]);
                stage.name = stageName[i];
                stage.mapIcon = "/public/images/map/m-" + stageColor[i] + ".png";
                stage.colorClass = stageColor[i];
                stage.save();
            }
        }
        for (int i = 0; i < categoryId.length; i++) {
            if (categoryId[i] == 0) {
                PortfolioCategory category = new PortfolioCategory(categoryName[i]);
                category.create();
            } else {
                PortfolioCategory category = PortfolioCategory.findById(categoryId[i]);
                category.name = categoryName[i];
                category.save();
            }
        }
        list();
    }

    public static void deleteSettings(String type, Long id) {
        JsonObject jsonObject = new JsonObject();
        if (type.equals("stage")) {
            PortfolioStage stage = PortfolioStage.findById(id);
            if (stage.portfolios.size() > 0) {
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("message", "Төсөл харялагдаж байгаа тул устгах боломжгүй");
            } else {
                stage._delete();
                jsonObject.addProperty("type", 0);
                jsonObject.addProperty("message", "Устлаа");
            }
        } else if (type.equals("category")) {
            PortfolioCategory category = PortfolioCategory.findById(id);
            if (category.portfolios.size() > 0) {
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("message", "Төсөл харялагдаж байгаа тул устгах боломжгүй");
            } else {
                category._delete();
                jsonObject.addProperty("type", 0);
                jsonObject.addProperty("message", "Устлаа");
            }
        }
        renderJSON(jsonObject);
    }

    public static void filter(Long viewType, Long stageId, Long categoryId, String searchName, Date startDate, Date endDate) {
//        System.out.println("stageId: " + stageId);
//        System.out.println("viewType: " + viewType);
//        System.out.println("searchName: " + searchName);
//        System.out.println("categoryId: " + categoryId);
//        System.out.println(startDate);
//        System.out.println(endDate);
        User user = Users.getUser();
        String qr = "SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE (f.owner.id = " + user.id + " OR ( p.id=f.project.id AND p.id=c.project.id AND c.user.id=" + user.id + ")) ";
        if (stageId != 0l) {
            qr += " AND f.stage.id=" + stageId;
        }
        if (categoryId != 0l) {
            qr += " AND f.category.id=" + categoryId;
        }
        if (startDate != null && endDate != null) {
            qr += " AND " + "p.startDate >= '" + Consts.myDateFormat.format(startDate) +
                    "' AND p.startDate <= '" + Consts.myDateFormat.format(endDate) + "' ";
        }
        if (searchName != null && !searchName.equals("")) {
            qr += " AND p.name LIKE '%" + searchName + "%' ";
        }
        List<Portfolio> portfolios = Portfolio.find(qr).fetch();
        boolean admin = user.isAdmin();
        Date today = Functions.convertHourNull(new Date());
        Date nextWeek = Functions.addOrMinusDays(today, 7, true);
        Project project;
        for (Portfolio portfolio : portfolios) {
            project = portfolio.project;
            portfolio.allMeetings = Meeting.count("status.id=1L AND project.id=?1", project.id);
            portfolio.overdueMeetings = Meeting.count("status.id=1L AND project.id=?1 AND finishDate <?2", project.id, today);
            portfolio.nextWeekMeetings = Meeting.count("status.id=1L AND project.id=?1 AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek);
            portfolio.nNextWeekMeetings = Meeting.count("status.id=1L AND project.id=?1 AND finishDate >?2 ", project.id, nextWeek);
            portfolio.allRFIs = RFI.count("project.id =?1 AND status.status = 'Open'", project.id);
            portfolio.overdueRFIs = RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate <?2 ", project.id, today);
            portfolio.nextWeekRFIs = RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek);
            portfolio.nNextWeekRFIs = RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate >?2 ", project.id, nextWeek);
            portfolio.allPunchLists = PunchList.count("project.id =?1 AND status.status = 'NotResolved'", project.id);
            portfolio.overduePunchLists = PunchList.count("project.id =?1 AND status.status = 'NotResolved' AND dueDate <?2 ", project.id, today);
            portfolio.nextWeekPunchLists = PunchList.count("project.id =?1 AND status.status = 'NotResolved' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek);
            portfolio.nNextWeekPunchLists = PunchList.count("project.id =?1 AND status.status = 'NotResolved' AND dueDate >?2 ", project.id, nextWeek);
            portfolio.overdueSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND actualFinish!=null AND finishDate < actualFinish", project.id);
            portfolio.nextWeekSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek);
            portfolio.nNextWeekSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >?2 ", project.id, nextWeek);
            portfolio.allSchedule = (portfolio.overdueSchedule + portfolio.nextWeekSchedule + portfolio.nNextWeekSchedule);
        }

        render(portfolios, viewType, admin);
    }
}
