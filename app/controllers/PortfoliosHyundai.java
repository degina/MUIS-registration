package controllers;

import com.google.gson.JsonObject;
import models.*;
import play.Play;
import play.mvc.With;

import java.util.*;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@With(Secure.class)
public class PortfoliosHyundai extends CRUD {
    public static void blank(Long id) {
        User user = Users.getUser();
        List<PortfolioHyundaiStage> portfolioStages = PortfolioHyundaiStage.findAll();
        List<PortfolioHyundaiLocation> portfolioLocations = PortfolioHyundaiLocation.findAll();
        List<PortfolioHyundaiProduct> portfolioProducts = PortfolioHyundaiProduct.findAll();
        List<PortfolioHyundaiFactory> portfolioFactories = PortfolioHyundaiFactory.findAll();
        List<UserTeam> userTeams = UserTeam.find("contractor=false order by queue, name").fetch();
        if (id == null) {
            if (!user.isAdmin()) forbidden();
            render(portfolioStages, portfolioLocations, userTeams, portfolioProducts, portfolioFactories);
        } else {
            Portfolio portfolio = Portfolio.findById(id);
            PortfolioHyundai portfolioHyundai = portfolio.portfolioHyundai;
            if (!(user.isAdmin() || portfolio.owner.id.compareTo(user.id) == 0)) forbidden();
            render(portfolio, portfolioHyundai, portfolioLocations, portfolioStages, userTeams, portfolioProducts, portfolioFactories);
        }
    }

    public static void list() {
        User user = Users.getUser();
        boolean admin = user.isAdmin();
        String qr = "SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE (f.owner.id = " + user.id + " OR ( p.id=f.project.id AND p.id=c.project.id AND c.user.id=" + user.id + ")) ";
        List<Portfolio> portfolios = Portfolio.find(qr).fetch();
        session.put("projectId", 0L);
        Long selProj = Users.pid();
        List<PortfolioHyundaiLocation> locations = PortfolioHyundaiLocation.findAll();
        List<PortfolioHyundaiStage> stages = PortfolioHyundaiStage.findAll();

        render(portfolios, selProj, admin, locations, stages);
    }

    public static void selectProject(Long id) {
        session.put("projectId", id);
        Project project = Project.findById(id);
        session.put("projectName", project.name);
        show(project.portfolio.id);
    }

    public static void create(Long portfolioId, Long selectStatus, String projectName, String projectZoruilalt, String projectAddress, Long selectOwner,
                              Date projectStartDate, Date projectFinishDate, String projectZahialagch, String projectZahialagchToloologch, String zahialagchTushaal,
                              Long selectLocationId, String zahialagchUtas, String ugsraltHariutsagch, String ugsraltHariutsagchTushaal, String ugsraltHariutsagchUtas,
                              String borluulaltName, String uildveriinDugaar, Long uildverId, Date gereeHiisen, Date zuragBatalsan, Date uildverlelEhelsen,
                              Date uildverlelDuussan, Date gaaliDeerIrsen, Date teeverleltHiisen, Date talbaiDeerIrsen, String[] ugsrahBag, String[] ugsrahAhlagch, Date ugsrahStartDate,
                              Date ugsrahEndDate, Long[] productName, String[] productRate, String[] productAmount, String[] hariutsagchName, String[] hariutsagchTushaal, String[] hariutsagchUtas,
                              String ashiglagch, String ashiglaltHariutsagchBag, String ashiglaltHariutsagchAhlagch, String mapLon, String mapLat,
                              String tusgaiTemdeglel, Date ashiglaltGereeHiisen, Date ashiglaltEhelsen, Date ashiglaltDuusah, Date ashiglaltMagdlal,
                              String projectImageUrl, float x, float y, float w, float h, float w2, float h2) {
        PortfolioHyundaiStage stage = PortfolioHyundaiStage.findById(selectStatus);
        PortfolioHyundaiLocation location = PortfolioHyundaiLocation.findById(selectLocationId);
        PortfolioHyundaiFactory factory = PortfolioHyundaiFactory.findById(uildverId);
        ConvertToImage convertToImage = new ConvertToImage();
        float x_ratio = (1200 / w2);
        float y_ratio = (800 / h2);
        User user = Users.getUser();
        if (portfolioId == null) {
            if (!user.isAdmin()) forbidden();
            convertToImage.convertCrop(projectImageUrl, "jpg", (int) (x * x_ratio), (int) (y * y_ratio), (int) (w * x_ratio), (int) (h * y_ratio));
            PortfolioHyundai portfolioHyundai = new PortfolioHyundai();
            Portfolio portfolio = new Portfolio();
            Project project = new Project();
            project.code = "";
            project.name = projectName;
            project.owner = Users.getUser();
            project.startDate = projectStartDate;
            project.finishDate = projectFinishDate;
            project.duration = 1l;
            project.status = "STATUS_ACTIVE";
            project.depends = "";
            project.weekend = CompanyConf.weekend;
            project.holidays = CompanyConf.holidays;
            project.startIsMilestone = false;
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

            portfolio.project = project;
            portfolio.address = projectAddress;
            portfolio.mapLat = mapLat;
            portfolio.mapLon = mapLon;
            portfolio.isActive = true;
            portfolio.owner = User.findById(selectOwner);
            portfolio.imageUrl = projectImageUrl;
            portfolio.x = (int) x;
            portfolio.y = (int) y;
            portfolio.w = (int) w;
            portfolio.h = (int) h;
            portfolio.create();

            portfolioHyundai.portfolio = portfolio;
            portfolioHyundai.location = location;
            portfolioHyundai.stage = stage;
            portfolioHyundai.zoruilalt = projectZoruilalt;
            portfolioHyundai.zahialagch = projectZahialagch;
            portfolioHyundai.zahialagchToloologch = projectZahialagchToloologch;
            portfolioHyundai.zahialagchTushaal = zahialagchTushaal;
            portfolioHyundai.zahialagchUtas = zahialagchUtas;
            portfolioHyundai.hariutsagch = ugsraltHariutsagch;
            portfolioHyundai.hariutsagchTushaal = ugsraltHariutsagchTushaal;
            portfolioHyundai.hariutsagchUtas = ugsraltHariutsagchUtas;
            portfolioHyundai.borluulaltName = borluulaltName;
            portfolioHyundai.uildveriinDugaar = uildveriinDugaar;
            portfolioHyundai.uildverlelDuussan = uildverlelDuussan;
            portfolioHyundai.factory = factory;
            portfolioHyundai.gereeHiisen = gereeHiisen;
            portfolioHyundai.zuragBatalsan = zuragBatalsan;
            portfolioHyundai.uildverlelEhelsen = uildverlelEhelsen;
            portfolioHyundai.teeverleltHiisen = teeverleltHiisen;
            portfolioHyundai.gaaliDeerIrsen = gaaliDeerIrsen;
            portfolioHyundai.talbaiDeerIrsen = talbaiDeerIrsen;
            portfolioHyundai.ugsrahStartDate = ugsrahStartDate;
            portfolioHyundai.ugsrahEndDate = ugsrahEndDate;
            portfolioHyundai.ashiglagch = ashiglagch;
            portfolioHyundai.ashiglaltBag = ashiglaltHariutsagchBag;
            portfolioHyundai.ashiglaltAhlagch = ashiglaltHariutsagchAhlagch;
            portfolioHyundai.tusgaiTemdeglel = tusgaiTemdeglel;
            portfolioHyundai.ashiglaltGereeHiisen = ashiglaltGereeHiisen;
            portfolioHyundai.ashiglaltEhelsen = ashiglaltEhelsen;
            portfolioHyundai.ashiglaltDuusah = ashiglaltDuusah;
            portfolioHyundai.ashiglaltMagdlal = ashiglaltMagdlal;
            portfolioHyundai.create();

            if (productName != null && productName.length > 0) {
                for (int i = 0; i < productName.length; i++) {
                    PortfolioHyundaiProduct portfolioProduct = PortfolioHyundaiProduct.findById(productName[i]);
                    PortfolioHyundaiProductList portfolioProductList = new PortfolioHyundaiProductList(portfolioProduct, productRate[i], productAmount[i], portfolioHyundai);
                    portfolioProductList.create();
                }
            }
            if (hariutsagchName != null && hariutsagchName.length > 0) {
                for (int i = 0; i < hariutsagchName.length; i++) {
//                    System.out.println("id+ " + hariutsagchName[i]);
                    PortfolioHyundaiServiceUser serviceUser = new PortfolioHyundaiServiceUser(hariutsagchName[i], hariutsagchTushaal[i], hariutsagchUtas[i], portfolioHyundai);
                    serviceUser.create();
                }
            }
            if (ugsrahBag != null && ugsrahBag.length > 0) {
                for (int i = 0; i < ugsrahBag.length; i++) {
//                    System.out.println("id+ " + ugsrahBag[i]);
                    PortfolioHyundaiInstallTeam installTeam = new PortfolioHyundaiInstallTeam(ugsrahBag[i], ugsrahAhlagch[i], portfolioHyundai);
                    installTeam.create();
                }
            }

        } else {
            Portfolio portfolio = Portfolio.findById(portfolioId);
            PortfolioHyundai portfolioHyundai = portfolio.portfolioHyundai;
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
            portfolio.address = projectAddress;
            portfolio.mapLat = mapLat;
            portfolio.mapLon = mapLon;
            portfolio.owner = User.findById(selectOwner);
            portfolio._save();

            portfolioHyundai.location = location;
            portfolioHyundai.stage = stage;
            portfolioHyundai.zoruilalt = projectZoruilalt;
            portfolioHyundai.zahialagch = projectZahialagch;
            portfolioHyundai.zahialagchToloologch = projectZahialagchToloologch;
            portfolioHyundai.zahialagchTushaal = zahialagchTushaal;
            portfolioHyundai.zahialagchUtas = zahialagchUtas;
            portfolioHyundai.hariutsagch = ugsraltHariutsagch;
            portfolioHyundai.hariutsagchTushaal = ugsraltHariutsagchTushaal;
            portfolioHyundai.hariutsagchUtas = ugsraltHariutsagchUtas;
            portfolioHyundai.borluulaltName = borluulaltName;
            portfolioHyundai.uildveriinDugaar = uildveriinDugaar;
            portfolioHyundai.uildverlelDuussan = uildverlelDuussan;
            portfolioHyundai.factory = factory;
            portfolioHyundai.gereeHiisen = gereeHiisen;
            portfolioHyundai.zuragBatalsan = zuragBatalsan;
            portfolioHyundai.uildverlelEhelsen = uildverlelEhelsen;
            portfolioHyundai.teeverleltHiisen = teeverleltHiisen;
            portfolioHyundai.gaaliDeerIrsen = gaaliDeerIrsen;
            portfolioHyundai.talbaiDeerIrsen = talbaiDeerIrsen;
            portfolioHyundai.ugsrahStartDate = ugsrahStartDate;
            portfolioHyundai.ugsrahEndDate = ugsrahEndDate;
            portfolioHyundai.ashiglagch = ashiglagch;
            portfolioHyundai.ashiglaltBag = ashiglaltHariutsagchBag;
            portfolioHyundai.ashiglaltAhlagch = ashiglaltHariutsagchAhlagch;
            portfolioHyundai.tusgaiTemdeglel = tusgaiTemdeglel;
            portfolioHyundai.ashiglaltGereeHiisen = ashiglaltGereeHiisen;
            portfolioHyundai.ashiglaltEhelsen = ashiglaltEhelsen;
            portfolioHyundai.ashiglaltDuusah = ashiglaltDuusah;
            portfolioHyundai.ashiglaltMagdlal = ashiglaltMagdlal;
            portfolioHyundai._save();

            List<PortfolioHyundaiProductList> portfolioProductLists = PortfolioHyundaiProductList.find("portfolioHyundai.id=?1", portfolio.id).fetch();
            for (PortfolioHyundaiProductList productD : portfolioProductLists)
                productD._delete();
            if (productName != null && productName.length > 0) {
                for (int i = 0; i < productName.length; i++) {
                    PortfolioHyundaiProduct portfolioProduct = PortfolioHyundaiProduct.findById(productName[i]);
                    PortfolioHyundaiProductList portfolioProductList = new PortfolioHyundaiProductList(portfolioProduct, productRate[i], productAmount[i], portfolioHyundai);
                    portfolioProductList.create();
                }
            }
            List<PortfolioHyundaiServiceUser> portfolioServiceUsers = PortfolioHyundaiServiceUser.find("portfolioHyundai.id=?1", portfolioHyundai.id).fetch();
            for (PortfolioHyundaiServiceUser productD : portfolioServiceUsers)
                productD._delete();
            if (hariutsagchName != null && hariutsagchName.length > 0) {
                for (int i = 0; i < hariutsagchName.length; i++) {
                    PortfolioHyundaiServiceUser serviceUser = new PortfolioHyundaiServiceUser(hariutsagchName[i], hariutsagchTushaal[i], hariutsagchUtas[i], portfolioHyundai);
                    serviceUser.create();
                }
            }
            List<PortfolioHyundaiInstallTeam> installTeams = PortfolioHyundaiInstallTeam.find("portfolioHyundai.id=?1", portfolioHyundai.id).fetch();
            for (PortfolioHyundaiInstallTeam installTeam : installTeams)
                installTeam._delete();
            if (ugsrahBag != null && ugsrahBag.length > 0) {
                for (int i = 0; i < ugsrahBag.length; i++) {
//                    System.out.println("id+ " + ugsrahBag[i]);
                    PortfolioHyundaiInstallTeam installTeam = new PortfolioHyundaiInstallTeam(ugsrahBag[i], ugsrahAhlagch[i], portfolioHyundai);
                    installTeam.create();
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
            portfolio.project._delete();
            list();
        } catch (Exception e) {
            list();
        }
    }


    public static void settings() {
        User user = Users.getUser();
        if (!user.isAdmin()) forbidden();
        List<PortfolioHyundaiLocation> locations = PortfolioHyundaiLocation.findAll();
        List<PortfolioHyundaiProduct> products = PortfolioHyundaiProduct.findAll();
        List<PortfolioHyundaiFactory> factories = PortfolioHyundaiFactory.findAll();
        List<PortfolioHyundaiStage> stages = PortfolioHyundaiStage.findAll();
        render(locations, products, factories, stages);
    }

    public static void saveSettings(String[] locationName, Long[] locationId, String[] productName, Long[] productId,
                                    String[] factoryName, Long[] factoryId, String[] stageName, String[] stageColor, Long[] stageId) {
        for (int i = 0; i < locationId.length; i++) {
            if (locationId[i] == 0) {
                PortfolioHyundaiLocation location = new PortfolioHyundaiLocation(locationName[i]);
                location.create();
            } else {
                PortfolioHyundaiLocation location = PortfolioHyundaiLocation.findById(locationId[i]);
                location.name = locationName[i];
                location.save();
            }
        }
        for (int i = 0; i < productId.length; i++) {
            if (productId[i] == 0) {
                PortfolioHyundaiProduct product = new PortfolioHyundaiProduct(productName[i]);
                product.create();
            } else {
                PortfolioHyundaiProduct product = PortfolioHyundaiProduct.findById(productId[i]);
                product.name = productName[i];
                product.save();
            }
        }
        for (int i = 0; i < factoryId.length; i++) {
            if (factoryId[i] == 0) {
                PortfolioHyundaiFactory factory = new PortfolioHyundaiFactory(factoryName[i]);
                factory.create();
            } else {
                PortfolioHyundaiFactory factory = PortfolioHyundaiFactory.findById(factoryId[i]);
                factory.name = factoryName[i];
                factory.save();
            }
        }
        for (int i = 0; i < stageId.length; i++) {
            if (stageId[i] == 0) {
                PortfolioHyundaiStage stage = new PortfolioHyundaiStage(stageName[i], stageColor[i]);
                stage.create();
            } else {
                PortfolioHyundaiStage stage = PortfolioHyundaiStage.findById(stageId[i]);
                stage.name = stageName[i];
                stage.mapIcon = "/public/images/map/m-" + stageColor[i] + ".png";
                stage.colorClass = stageColor[i];
                stage.save();
            }
        }
        list();
    }

    public static void deleteSettings(String type, Long id) {

        JsonObject jsonObject = new JsonObject();
        if (type.equals("location")) {
            PortfolioHyundaiLocation location = PortfolioHyundaiLocation.findById(id);
            location._delete();
            if (location.portfolioHyundais.size() > 0) {
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("message", "Төсөл харялагдаж байгаа тул устгах боломжгүй");
            } else {
                location._delete();
                jsonObject.addProperty("type", 0);
                jsonObject.addProperty("message", "Устлаа");
            }
        } else if (type.equals("factory")) {
            PortfolioHyundaiFactory factory = PortfolioHyundaiFactory.findById(id);

            if (factory.portfolioHyundais.size() > 0) {
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("message", "Төсөл харялагдаж байгаа тул устгах боломжгүй");
            } else {
                factory._delete();
                jsonObject.addProperty("type", 0);
                jsonObject.addProperty("message", "Устлаа");
            }
        } else if (type.equals("product")) {
            PortfolioHyundaiProduct product = PortfolioHyundaiProduct.findById(id);
            if (product.productList.size() > 0) {
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("message", "Бүтээгдэхүүн харялагдаж байгаа тул устгах боломжгүй");
            } else {
                product._delete();
                jsonObject.addProperty("type", 0);
                jsonObject.addProperty("message", "Устлаа");
            }
        } else if (type.equals("stage")) {
            PortfolioHyundaiStage hyundaiStage = PortfolioHyundaiStage.findById(id);
            if (hyundaiStage.portfolioHyundais.size() > 0) {
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("message", "Бүтээгдэхүүн харялагдаж байгаа тул устгах боломжгүй");
            } else {
                hyundaiStage._delete();
                jsonObject.addProperty("type", 0);
                jsonObject.addProperty("message", "Устлаа");
            }
        }
        renderJSON(jsonObject);

    }

    public static void filter(Long viewType, Long statusId, Long locationId, String searchName, Date startDate, Date endDate) {
        User user = Users.getUser();
        String qr = "SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE (f.owner.id = " + user.id + " OR ( p.id=f.project.id AND p.id=c.project.id AND c.user.id=" + user.id + ")) ";
        if (statusId != 0l) {
            qr += " AND f.portfolioHyundai.stage.id=" + statusId;
        }
        if (locationId != 0l) {
            qr += " AND f.portfolioHyundai.location.id=" + locationId;
        }
        if (startDate != null && endDate != null) {
            qr += " AND  " + "f.portfolioHyundai.gereeHiisen >= '" + Consts.myDateFormat.format(startDate) +
                    "' AND f.portfolioHyundai.gereeHiisen <= '" + Consts.myDateFormat.format(endDate) + "'";
        }
        if (searchName != null && !searchName.equals("")) {
            qr += " AND p.name LIKE '%" + searchName + "%'";
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
            portfolio.allPunchLists = PunchList.count("project.id =?1 AND status.status = 'Open'", project.id);
            portfolio.overduePunchLists = PunchList.count("project.id =?1 AND status.status = 'Open' AND dueDate <?2 ", project.id, today);
            portfolio.nextWeekPunchLists = PunchList.count("project.id =?1 AND status.status = 'Open' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek);
            portfolio.nNextWeekPunchLists = PunchList.count("project.id =?1 AND status.status = 'Open' AND dueDate >?2 ", project.id, nextWeek);
            portfolio.overdueSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND actualFinish!=null AND finishDate < actualFinish", project.id);
            portfolio.nextWeekSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek);
            portfolio.nNextWeekSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >?2 ", project.id, nextWeek);
            portfolio.allSchedule = (portfolio.overdueSchedule + portfolio.nextWeekSchedule + portfolio.nNextWeekSchedule);

        }
        render(portfolios, viewType, admin);
    }

    public static void show(Long id) {
        Date today = Functions.convertHourNull(new Date());
        Date nextWeek = Functions.addOrMinusDays(today, 7, true);
        Portfolio portfolio = Portfolio.findById(id);
        PortfolioHyundai portfolioHyundai = portfolio.portfolioHyundai;
        Project project = portfolio.project;
        portfolio.allMeetings = Meeting.count("status.id=1L AND project.id=?1", project.id);
        portfolio.overdueMeetings = Meeting.count("status.id=1L AND project.id=?1 AND finishDate <?2", project.id, today);
        portfolio.nextWeekMeetings = Meeting.count("status.id=1L AND project.id=?1 AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek);
        portfolio.nNextWeekMeetings = Meeting.count("status.id=1L AND project.id=?1 AND finishDate >?2 ", project.id, nextWeek);
        portfolio.allRFIs = RFI.count("project.id =?1 AND status.status = 'Open'", portfolio.id);
        portfolio.overdueRFIs = RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate <?2 ", project.id, today);
        portfolio.nextWeekRFIs = RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek);
        portfolio.nNextWeekRFIs = RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate >?2 ", project.id, nextWeek);
        portfolio.allPunchLists = PunchList.count("project.id =?1 AND status.status = 'Open'", project.id);
        portfolio.overduePunchLists = PunchList.count("project.id =?1 AND status.status = 'Open' AND dueDate <?2 ", project.id, today);
        portfolio.nextWeekPunchLists = PunchList.count("project.id =?1 AND status.status = 'Open' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek);
        portfolio.nNextWeekPunchLists = PunchList.count("project.id =?1 AND status.status = 'Open' AND dueDate >?2 ", project.id, nextWeek);
        portfolio.overdueSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND actualFinish!=null AND finishDate < actualFinish", project.id);
        portfolio.nextWeekSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek);
        portfolio.nNextWeekSchedule = Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >?2 ", project.id, nextWeek);
        portfolio.allSchedule = (portfolio.overdueSchedule + portfolio.nextWeekSchedule + portfolio.nNextWeekSchedule);
        render(portfolio, portfolioHyundai);
    }
}
