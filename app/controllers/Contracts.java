package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import controllers.MyClass.ContractChart;
import models.*;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@With(Secure.class)
@Check(Consts.permissionContract)
public class Contracts extends CRUD {

    public static void saveStatus(String[] status, String[] category, String[] payment, String[] statusColor,
                                  Long[] statusId, Long[] categoryId, Long[] paymentId) {
        if (status != null) {
            for (int i = 0; i < status.length; i++) {
                ContractStatus status1 = ContractStatus.findById(statusId[i]);
                status1.status = status[i];
                status1.colorClass = statusColor[i];
                status1._save();
            }
        }
        if (category != null) {
            for (int i = 0; i < category.length; i++) {
                ContractCategory cat = ContractCategory.findById(categoryId[i]);
                cat.category = category[i];
                cat._save();
            }
        }
        if (payment != null) {
            for (int i = 0; i < payment.length; i++) {
                ContractPayment pay = ContractPayment.findById(paymentId[i]);
                pay.payment = payment[i];
                pay._save();
            }
        }
        list();
    }

    public static void deleteStatus(Long id, int type) {
        if (type == 1) {
            ContractStatus contractStatusNew = ContractStatus.findById(id);
            contractStatusNew._delete();
        } else if (type == 2) {
            ContractCategory contractCategory = ContractCategory.findById(id);
            contractCategory._delete();
        } else if (type == 3) {
            ContractPayment contractPayment = ContractPayment.findById(id);
            contractPayment._delete();
        }
    }

    public static void createStatus(int type) {
        JsonObject statusObj = new JsonObject();
        if (type == 1) {
            ContractStatus contractStatusNew = new ContractStatus();
            contractStatusNew.create();
            statusObj.addProperty("id", contractStatusNew.id);
            statusObj.addProperty("type", type);
        } else if (type == 2) {
            ContractCategory contractCategory = new ContractCategory();
            contractCategory.create();
            statusObj.addProperty("id", contractCategory.id);
            statusObj.addProperty("type", type);
        } else if (type == 3) {
            ContractPayment contractPayment = new ContractPayment();
            contractPayment.create();
            statusObj.addProperty("id", contractPayment.id);
            statusObj.addProperty("type", type);
        }
        renderJSON(statusObj);
    }

    public static void list() {
        List<Contract> contracts = Contract.find("order by approvedDate").fetch(1, 16);
        Long maxSize = Contract.count();
        int CurrentPageNumber = 1;
        Long MaxPageNumber = maxSize / 16;
        if (maxSize % 16 != 0) MaxPageNumber++;
        List<ContractStatus> statuses = ContractStatus.findAll();
        List<ContractPayment> payments = ContractPayment.findAll();
        List<ContractCategory> categories = ContractCategory.findAll();
        List<ContractFinancial> financialsTotal = ContractFinancial.find("financialType=?1", "ContractAmount").fetch();
        render(contracts, statuses, categories, payments, financialsTotal, CurrentPageNumber, MaxPageNumber);
    }

    public static void chart() {
        List<Contract> contracts = Contract.findAll();
        List<ContractStatus> statuses = ContractStatus.findAll();
        List<ContractPayment> payments = ContractPayment.findAll();
        List<ContractCategory> categories = ContractCategory.findAll();
        List<ContractFinancial> financialsTotal = ContractFinancial.find("financialType=?", "ContractAmount").fetch();

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.YEAR, -2);
        calendar.set(Calendar.MONTH, 12);
        List<ContractChart> contractCharts = new ArrayList<ContractChart>();

        for (int i = 0; i < 24; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            Date lastDayOfMonthBottom = calendar.getTime();
            calendar.setTime(lastDayOfMonthBottom);
            calendar.add(Calendar.MONTH, 2);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);
            Date lastDayOfMonthTop = calendar.getTime();

            //System.out.println("lastDayOfMonthBottom: " + lastDayOfMonthBottom);
            //System.out.println("lastDayOfMonthTop: " + lastDayOfMonthTop);
            List<Contract> contractList = Contract.find("approvedDate >=?1 and approvedDate <?2 ", lastDayOfMonthBottom, lastDayOfMonthTop).fetch();
            float totalAmount = 0.00f;
            for (Contract cont : contractList) {
                totalAmount += (cont.totalAmount.amount == null) ? 0.00f : cont.totalAmount.amount;
            }
            totalAmount = totalAmount / 1000000;
            contractCharts.add(new ContractChart(lastDayOfMonthTop, contractList.size(), totalAmount));
            calendar.add(Calendar.MONTH, 1);
        }
        int catSize = categories.size();
        for (ContractCategory category : categories)
            category.percent = (float) (category.contracts.size() * 100 / catSize);
        catSize = statuses.size();
        for (ContractStatus status : statuses)
            status.percent = (float) (status.contracts.size() * 100 / catSize);
        catSize = payments.size();
        for (ContractPayment payment : payments)
            payment.percent = (float) (payment.contracts.size() * 100 / catSize);
        render(contracts, statuses, categories, payments, financialsTotal, contractCharts);
    }

    public static void listFilter(String filterStatus, String filterContractor, String filterCategory,
                                  String filterStartPrice, String filterEndPrice, String filterSearchName,
                                  Date filterStartDate, Date filterEndDate, String filterPayment, int CurrentPageNumber) {
        List<Contract> contracts;
        String qr = "1=1";
        if (!filterStatus.equals("All")) {
            Long filter1Id = Long.valueOf(filterStatus);
            qr += " AND status.id=" + filter1Id;
        }
        if (filterContractor != null)
            if (!filterContractor.equals("All"))
                qr += " AND companyName='" + filterContractor + "'";

        if (!filterCategory.equals("All"))
            qr += " AND category.id='" + filterCategory + "'";

        if (filterStartPrice != null)
            if (!filterStartPrice.equals("All"))
                qr += " AND " + "totalAmount.amount > " + StoSplitLong(filterStartPrice) + "  and totalAmount.amount < " + StoSplitLong(filterEndPrice) + "";

        if (filterSearchName != null)
            if (!filterSearchName.equals(""))
                qr += " AND companyName LIKE '%" + filterSearchName + "%' OR title LIKE '%" + filterSearchName + "%' OR name LIKE '%" + filterSearchName + "%'";

        if (filterStartDate != null)
            qr += " AND " + "reviewDate > '" + Consts.myDateFormat.format(filterStartDate) +
                        "' AND reviewDate < '" + Consts.myDateFormat.format(filterEndDate) + "'";

        if (filterPayment != null && !filterPayment.equals("All") && !filterPayment.equals(""))
            qr += " AND payment.id='" + filterPayment + "'";

        Long maxSize = Contract.count(qr);
        Long MaxPageNumber = maxSize / 16;
        if (maxSize % 16 != 0) MaxPageNumber++;
        contracts = Contract.find(qr).fetch(CurrentPageNumber, 16);
        render(contracts, CurrentPageNumber, MaxPageNumber);
    }

    public static void blank(Long id) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionContract) != 3) forbidden();
        List<ContractStatus> statuses = ContractStatus.findAll();
        List<ContractCategory> categories = ContractCategory.findAll();
        List<ContractPayment> payments = ContractPayment.findAll();
        List<User> users = User.findAll();
        Contract contract = null;
        if (id != null && id != 0)
            contract = Contract.findById(id);
        render(statuses, categories, payments, users, contract);
    }

    public static void settings() {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionContract) != 3) forbidden();
        List<ContractStatus> statuses = ContractStatus.findAll();
        List<ContractCategory> categories = ContractCategory.findAll();
        List<ContractPayment> payments = ContractPayment.findAll();
        List<UserPermissionRelation> permissionRelations = UserPermissionRelation.find("permissionType.permission.alias=?1", Consts.permissionContract).fetch();
        render(statuses, categories, payments, permissionRelations);
    }

    public static void deleteContract(Long id) {
        //System.out.println("id: " + id);
        Contract contract = Contract.findById(id);
        for (ContractAttachment contractAttachment : contract.contractAttachments)
            Functions.deleteUploadFile(contractAttachment.dir, contractAttachment.extension);
        contract._delete();
        list();
    }

    public static void show(Long id) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionContract) != 3) forbidden();
        Contract contract = Contract.findById(id);
        ContractFinancial initialDeposit = contract.getInitialDeposit();
        ContractFinancial finalTolbor = contract.getFinalPayment();
        List<ContractFinancial> progressPayment = contract.getProgressPayment();
        List<ContractFinancial> retention = contract.getRetention();
        List<ContractWarrantyItem> warrantyItems = contract.getWarrantyItems();
        int permissionType = Users.getUser().getUserPermissionType(Consts.permissionContract);

        render(contract, initialDeposit, finalTolbor, progressPayment, retention, warrantyItems, permissionType);
    }

    public static void create(Long contractId, String contractName, String contractCompanyName, Long status, Long category,
                              String title, String acceptedA, String acceptedB, String acceptedC,
                              String acceptedTA, String acceptedTB, String acceptedTC, Long paymentMode,
                              String contractAmount, Float contractAmountHuvi, String initialDeposit, Date contractAmountDate,
                              Date initialDepositDate, Float initialDepositHuvi,
                              String[] progressPayment, Date[] progressPaymentDate, Float[] progressPaymentHuvi,
                              String finalUld, Date finalDate, Float FinalHuvi, String[] retention,
                              Date[] retentionDate, Float[] retentionHuvi, Date startDate, Date reviewDate, Date endDate, Date approvedDate,
                              String notifyMe1, String notifyMe2, String[] warrantyItem, int[] warrantyItemDugaar, Date[] warrantyItemstartDate,
                              Date[] warrantyItemendDate, String[] warrantyItemnotifyMe,
                              String editor1, String[] filename, String[] filedir, String[] extension,
                              Float [] filesize,boolean tolborNegj, boolean isDraft) {
        //System.out.println("InitialDepositHuvi :" + initialDepositHuvi);
        Contract contract;
        if (contractId != null)
            contract = Contract.findById(contractId);
        else
            contract = new Contract();
        contract.name = contractName;
        ContractStatus contractStatus;
        if (isDraft)
            contractStatus = ContractStatus.findById(3L);
        else
            contractStatus = ContractStatus.findById(status);

        ContractCategory contractCategory = ContractCategory.findById(category);
        ContractPayment contractPayment = ContractPayment.findById(paymentMode);

        contract.title = title;
        contract.companyName = contractCompanyName;
        contract.status = contractStatus;
        contract.category = contractCategory;
        contract.payment = contractPayment;
        contract.acceptedA = acceptedA;
        contract.acceptedB = acceptedB;
        contract.acceptedC = acceptedC;
        contract.acceptedTA = acceptedTA;
        contract.acceptedTB = acceptedTB;
        contract.acceptedTC = acceptedTC;

        contract.notifyMeBeforeEndDay = !notifyMe1.equals("") ? Long.parseLong(notifyMe1) : null;
        contract.notifyMeBeforeReview = !notifyMe2.equals("") ? Long.parseLong(notifyMe2) : null;
        contract.endDate = endDate;
        contract.startDate = startDate;
        contract.reviewDate = reviewDate;
        contract.approvedDate = approvedDate;
        contract.negj = tolborNegj ? "₮" : "$";
        contract.notes = editor1;
        contract.owner = Users.getUser();

        if (contractId == null) {
            contract.insertedDate = new Date();
            contract.create();
        }
        else
            contract.save();
        //System.out.println("notifyMe1: " + notifyMe1);
        if (notifyMe1 != null && !notifyMe1.equals("")) {
            ReminderModel reminder = new ReminderModel();
            reminder.mainType = "contract";
            reminder.subType = "endOfDay";
            reminder.title = "гэрээний хугцаа дуусахад " + notifyMe1 + " хоног үлдлээ";
            reminder.count = Long.parseLong(notifyMe1);
            reminder.reminderDate = Functions.addOrMinusDays(endDate, Integer.parseInt(notifyMe1), false);
            reminder.contract = contract;
            reminder.reminderUsers = new ArrayList<ReminderUser>();
            reminder.addUser(Users.getUser());
            reminder.create();
        }
        if (notifyMe2 != null && !notifyMe2.equals("")) {
            ReminderModel reminder = new ReminderModel();
            reminder.mainType = "contract";
            reminder.subType = "reviewOfDay";
            reminder.title = "гэрээг дүгнэхэд " + notifyMe2 + " хоног үлдлээ";
            reminder.count = Long.parseLong(notifyMe2);
            reminder.reminderDate = Functions.addOrMinusDays(reviewDate, Integer.parseInt(notifyMe2), false);
            reminder.contract = contract;
            reminder.reminderUsers = new ArrayList<ReminderUser>();
            reminder.addUser(Users.getUser());
            reminder.create();
        }
        if (contractId != null) {
            for (ContractFinancial financial : contract.contractFinancials)
                financial.delete();
            for (ContractWarrantyItem warrantyItem1 : contract.warrantyItems)
                warrantyItem1.delete();
            for (ContractAttachment attachment : contract.contractAttachments)
                attachment.delete();
            contract.totalAmount._delete();
        }
        ContractTotalAmount totalAmount = new ContractTotalAmount(StoSplitLong(contractAmount), contractAmountDate, contractAmountHuvi, contract);
        totalAmount.create();
        ContractFinancial finalUldegdel = new ContractFinancial(StoSplitLong(finalUld), finalDate, FinalHuvi, "Final", contract);
        finalUldegdel.create();
        if (initialDeposit != null && !initialDeposit.equals("")) {
            ContractFinancial initialDepositFinancial = new ContractFinancial(StoSplitLong(initialDeposit), initialDepositDate, initialDepositHuvi, "InitialDeposit", contract);
            initialDepositFinancial.create();
        }

        if (progressPayment != null)
        for (int i = 0; i < progressPayment.length; i++) {
            if ((progressPayment[i] != null) && (!progressPayment[i].equals(""))) {
                ContractFinancial contractFinancialProgress = (new ContractFinancial(StoSplitLong(progressPayment[i]), progressPaymentDate[i]
                        , progressPaymentHuvi[i], "ProgressPayment", contract));
                contractFinancialProgress.create();
            }
        }
        if (retention != null)
            for (int i = 0; i < retention.length; i++) {
                if ((retention[i] != null) && (!retention[i].equals(""))) {
                    //System.out.println("retention:  " + retention[i]);
                    //System.out.println("retention urt:  " + retention.length);
                    ContractFinancial contractFinancialRetentions = new ContractFinancial(StoSplitLong(retention[i]), retentionDate[i], retentionHuvi[i], "Retention", contract);
                    contractFinancialRetentions.create();
                    contract.contractFinancials.add(contractFinancialRetentions);
                }
            }
        if (warrantyItem != null) {
            for (int i = 0; i < warrantyItem.length; i++) {
                if (warrantyItem[i] != null && !warrantyItem[i].equals("")) {
                    ContractWarrantyItem contractWarrantyItem = new ContractWarrantyItem(warrantyItem[i], (long) warrantyItemDugaar[i], warrantyItemstartDate[i],
                            warrantyItemendDate[i], contract);
                    contractWarrantyItem.create();
                    if (warrantyItemnotifyMe[i] != null) {
                        ReminderModel reminder = new ReminderModel();
                        reminder.mainType = "contract";
                        reminder.subType = "warrantyItemOfDay";
                        reminder.title = "гэрээний \"" + warrantyItem[i] + "\" баталгаат зүйлийн хугацаа дуусахад " + warrantyItemnotifyMe[i] + " хоног үлдлээ";
                        reminder.count = Long.parseLong(warrantyItemnotifyMe[i]);
                        reminder.reminderDate = Functions.addOrMinusDays(warrantyItemendDate[i], Integer.parseInt(warrantyItemnotifyMe[i]), false);
                        reminder.contract = contract;
                        reminder.reminderUsers = new ArrayList<ReminderUser>();
                        reminder.addUser(Users.getUser());
                        reminder.create();
                    }
                }
            }
        }
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                if (filename[i] != null) {
                    ContractAttachment attachment = new ContractAttachment(filename[i], filedir[i],
                            extension[i],filesize[i], contract);
                    attachment.create();
                }
            }
        }
        show(contract.id);
    }

    public static Long StoSplitLong(String str) {
        if (str != null && !str.equals(""))
            return Long.parseLong(str.replaceAll("'", ""));
        else
            return null;
    }
}
