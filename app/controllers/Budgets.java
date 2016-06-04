package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import models.*;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/28/15.
 */
@With(Secure.class)
@Check(Consts.permissionBudget)
public class Budgets extends CRUD {
    public static void chartView() {
        Budget budget;
        Date currentMonth = new Date();
        currentMonth = Functions.convertDayNull(currentMonth);
        Date nextMonth = Functions.addOrMinusMonth(currentMonth, 1, true);
        budget = Budget.find("date>=?1 and date<?2", currentMonth, nextMonth).first();
        List<BudgetOpexDetail> budgetOpexDetail = BudgetOpexDetail.find("opex.id=?1", budget.opex.id).fetch();

        JsonArray opexChartData = new JsonArray();
        for (BudgetOpexDetail opexDetail:budgetOpexDetail){
            JsonObject obj = new JsonObject();
            obj.addProperty("label",opexDetail.name);
            obj.addProperty("value",opexDetail.actual);
            opexChartData.add(obj);
        }
        //  //System.out.println(chartData.toString());
        render(budget,opexChartData);

    }

    public static void showNetSalesDetails(Long budgetId, int type) {
        List<BudgetActivity> budgetActivities = BudgetActivity.find("budget.id=?1", budgetId).fetch();
        if (type == 3) {
            List<BudgetOpexDetail> budgetOpexDetails = BudgetOpexDetail.find("opex.budget.id=?1", budgetId).fetch();
            render(budgetOpexDetails, type);
        }
        render(budgetActivities, type);
    }

    public static void showMonthBudget(Long month) {
        Date currentMonth = new Date();
        Budget budget;
        currentMonth.setTime(month);
        currentMonth = Functions.convertDayNull(currentMonth);
        Date nextMonth = Functions.addOrMinusMonth(currentMonth, 1, true);
        budget = Budget.find("date>=?1 and date<?2", currentMonth, nextMonth).first();
        render(budget);
    }

    public static void blank(int year, int month) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionBudget) != 3) forbidden();
        String[] temps = {"Wage", "Bonus", "SocialSecurity", "Allowance", "Communication", "BusinessTrip"};
        String[] tempsNotEmpls = {"VehicleExpenses", "RentalExpense", "Depreciation", "OfficeSupply", "Training", "Safety", "EntertainmentSportActivities",
                "BusinessMeeting", "Others"};
        Budget budget;
        Date currentMonth;
        if (year == 0) {
            currentMonth = new Date();
        } else {
            currentMonth = new Date((year - 1900), (month - 1), 1);
        }
//        //System.out.println("date: " + currentMonth);
        currentMonth = Functions.convertDayNull(currentMonth);
        Date nextMonth = Functions.addOrMinusMonth(currentMonth, 1, true);
        budget = Budget.find("date>=?1 and date<?2", currentMonth, nextMonth).first();
        if (budget == null) {
            budget = new Budget();
            budget.date = currentMonth;
            budget.netSales = new BudgetNetSales(budget);
            budget.cogs = new BudgetCogs(budget);
            budget.grossMargin = new BudgetGrossMargin(budget);
            budget.opex = new BudgetOpex(budget);
            budget.balance = new BudgetBalance(budget);
            budget.create();
            render(budget, temps, tempsNotEmpls);
        } else {
            List<BudgetOpexDetail> budgetOpexDetailEmps = BudgetOpexDetail.find("opex.id=?1 and isEmpRelated=?2", budget.opex.id, true).fetch();
            List<BudgetOpexDetail> budgetOpexDetailNotEmps = BudgetOpexDetail.find("opex.id=?1 and isEmpRelated=?2", budget.opex.id, false).fetch();
            render(budget, budgetOpexDetailEmps, budgetOpexDetailNotEmps, temps, tempsNotEmpls);
        }
    }

    public static void createActivity(Long budgetId, String name, String acSale, String buSale, String acCogs, String buCogs,
                                      String acProfit, String buProfit, String description) {
        Budget budget = Budget.findById(budgetId);
        BudgetActivity activity = new BudgetActivity(name, splitLong(acSale), splitLong(buSale), splitLong(acCogs), splitLong(buCogs),
                splitMinusLong(acProfit), splitMinusLong(buProfit), description, budget);
        activity.create();

        budget.netSales.totalActual += activity.salesActual;
        budget.netSales.totalBudget += activity.salesBudget;
        budget.cogs.totalActual += activity.cogsActual;
        budget.cogs.totalBudget += activity.cogsBudget;
        budget.grossMargin.totalActual += activity.profitActual;
        budget.grossMargin.totalBudget += activity.profitBudget;
        budget._save();
        calculateTotalIncome(budgetId);
        render(activity, budget);
    }

    public static void deleteActivity(Long budgetId, Long activityId) {
        Budget budget = Budget.findById(budgetId);
        BudgetActivity activity = BudgetActivity.findById(activityId);

        budget.netSales.totalActual -= activity.salesActual;
        budget.netSales.totalBudget -= activity.salesBudget;
        budget.cogs.totalActual -= activity.cogsActual;
        budget.cogs.totalBudget -= activity.cogsBudget;
        budget.grossMargin.totalActual -= activity.profitActual;
        budget.grossMargin.totalBudget -= activity.profitBudget;
        budget._save();
        calculateTotalIncome(budgetId);
        activity._delete();
        render(budget);
    }

    public static void createOpex(Long budgetOpexId, String name, String actual, String budgetS, boolean isEmpRelated, String description) {
        //System.out.println("actual: " + actual);
        BudgetOpex budgetOpex = BudgetOpex.findById(budgetOpexId);
        BudgetOpexDetail opexDetail = new BudgetOpexDetail(name, isEmpRelated, splitLong(actual), splitLong(budgetS), description, budgetOpex);
        opexDetail.create();
        //System.out.println("budgetOpex.employeeActual: " + budgetOpex.employeeActual);
        //System.out.println("opexDetail.actual: " + opexDetail.actual);
        if (isEmpRelated) {
            budgetOpex.employeeActual += opexDetail.actual;
            budgetOpex.employeeBudget += opexDetail.budget;
        }
        budgetOpex.totalActual += opexDetail.actual;
        budgetOpex.totalBudget += opexDetail.budget;
        calculateTotalOpex(budgetOpex.budget.id);
        budgetOpex._save();
        Budget budget = budgetOpex.budget;
        render(opexDetail, budget);
    }

    public static void deleteOpex(Long budgetOpexId, Long opexDetailId) {
        BudgetOpex budgetOpex = BudgetOpex.findById(budgetOpexId);
        BudgetOpexDetail opexDetail = BudgetOpexDetail.findById(opexDetailId);
        if (opexDetail.isEmpRelated) {
            budgetOpex.employeeActual -= opexDetail.actual;
            budgetOpex.employeeBudget -= opexDetail.budget;
        }
        budgetOpex.totalActual -= opexDetail.actual;
        budgetOpex.totalBudget -= opexDetail.budget;
        budgetOpex.variance = budgetOpex.totalActual - budgetOpex.totalBudget;
        budgetOpex.variancePercent = (budgetOpex.totalBudget == 0) ? ((float) budgetOpex.variance) : ((float) budgetOpex.variance * 100 / (float) budgetOpex.totalBudget);
        budgetOpex._save();
        calculateTotalOpex(budgetOpex.budget.id);
        opexDetail._delete();
        Budget budget = budgetOpex.budget;
        render(budgetOpex, budget);
    }

    public static void createCash(Long budgetId, String name) {
        Budget budget = Budget.findById(budgetId);
        BudgetCash budgetCash = new BudgetCash(budget, name);
        budgetCash.create();
        render(budgetCash);
    }

    public static void createCashSub(Long cashId, String name, String inflow, String outflow) {
        BudgetCash budgetCash = BudgetCash.findById(cashId);
        BudgetSubContractor subContractor = new BudgetSubContractor(name, splitLong(inflow), splitLong(outflow), budgetCash);
        subContractor.netflow = subContractor.inflow - subContractor.outflow;
        subContractor.create();
        budgetCash.inflow += subContractor.inflow;
        budgetCash.outflow += subContractor.outflow;
        budgetCash.netflow += subContractor.netflow;
        budgetCash._save();
        render(subContractor, budgetCash);
    }

    public static void deleteCashSub(Long subContractorId) {
        BudgetSubContractor subContractor = BudgetSubContractor.findById(subContractorId);
        BudgetCash budgetCash = subContractor.cash;
        budgetCash.inflow -= subContractor.inflow;
        budgetCash.outflow -= subContractor.outflow;
        budgetCash.netflow -= subContractor.netflow;
        budgetCash._save();
        subContractor._delete();
        render(budgetCash);
    }

    public static void deleteCash(Long cashId) {
        BudgetCash budgetCash = BudgetCash.findById(cashId);
        budgetCash._delete();
    }

    public static void saveResult(Long budgetId, String operatingProfitActual, String operatingProfitBudget, String operatingProfitDescription,
                                  String otherExpensesActual, String otherExpensesBudget, String otherExpensesDescription,
                                  String otherIncomeActual, String otherIncomeBudget, String otherIncomeDescription,
                                  String taxActual, String taxBudget, String taxDescription, String netGrossMarginDescription, String opexTotalDescription,
                                  String netSalesDescription, String netCogsDescription, String ebitdaDescription, String netIncomeDescription) {
        Budget budget = Budget.findById(budgetId);
        budget.operatingProfitActual = splitMinusLong(operatingProfitActual);
        budget.operatingProfitBudget = splitMinusLong(operatingProfitBudget);
        budget.operatingProfitDescription = operatingProfitDescription;
        budget.otherExpensesActual = splitMinusLong(otherExpensesActual);
        budget.otherExpensesBudget = splitMinusLong(otherExpensesBudget);
        budget.otherExpensesDescription = otherExpensesDescription;
        budget.otherIncomeActual = splitMinusLong(otherIncomeActual);
        budget.otherIncomeBudget = splitMinusLong(otherIncomeBudget);
        budget.otherIncomeDescription = otherIncomeDescription;
        budget.taxActual = splitMinusLong(taxActual);
        budget.taxBudget = splitMinusLong(taxBudget);
        budget.taxDescription = taxDescription;

        budget.otherIncomeVariance = budget.otherIncomeBudget - budget.otherIncomeActual;
        budget.otherIncomeVariancePercent = (budget.otherIncomeBudget == 0) ? 0f : ((float) budget.otherIncomeActual * 100 / (float) budget.otherIncomeBudget);
        budget.otherExpensesVariance = budget.otherExpensesBudget - budget.otherExpensesActual;
        budget.otherExpensesVariancePercent = (budget.otherExpensesBudget == 0) ? 0f : ((float) budget.otherExpensesActual * 100 / (float) budget.otherExpensesBudget);
        budget.taxBeforeProfitVariance = budget.taxBeforeProfitBudget - budget.taxBeforeProfitActual;
        budget.taxBeforeProfitVariancePercent = (budget.taxBeforeProfitBudget == 0) ? 0f : ((float) budget.taxBeforeProfitActual * 100 / (float) budget.taxBeforeProfitBudget);
        budget.taxVariance = budget.taxBudget - budget.taxActual;
        budget.taxVariancePercent = (budget.taxBudget == 0) ? 0f : ((float) budget.taxActual * 100 / (float) budget.taxBudget);
        budget.netIncomeVariance = budget.netIncomeBudget - budget.netIncomeActual;
        budget.netIncomeVariancePercent = (budget.netIncomeBudget == 0) ? 0f : ((float) budget.netIncomeActual * 100 / (float) budget.netIncomeBudget);
        budget.ebitdaVariance = budget.ebitdaBudget - budget.ebitdaActual;
        budget.ebitdaVariancePercent = (budget.ebitdaBudget == 0) ? 0f : ((float) budget.ebitdaActual * 100 / (float) budget.ebitdaBudget);

        budget.ebitdaDescription = ebitdaDescription;
        budget.netIncomeDescription = netIncomeDescription;
        budget._save();
        budget.grossMargin.description = netGrossMarginDescription;
        budget.grossMargin._save();
        budget.opex.description = opexTotalDescription;
        budget.opex._save();
        budget.netSales.description = netSalesDescription;
        budget.netSales._save();
        budget.cogs.description = netCogsDescription;
        budget.cogs._save();
        calculateTotal(budgetId);
        render(budget);
    }

    public static void saveBalance(Long budgetId, String cashEquivalents, String currentLiabilities, String accountsReceivables, String taxesPayAbles, String taxRecoverAbles, String inventory,
                                   String loans, String unearnedSales, String supplyMaterial, String nonCurrentPayAbles, String prepayments, String tangibleAssets,
                                   String ordinaryShares, String intangibleAssets, String incomeReporting, String investments, String retainedEarnings,
                                   String otherNonCurrentAssets, String balanceDescription) {
        Budget budget = Budget.findById(budgetId);
        BudgetBalance balance = budget.balance;
        balance.saveBudgetBalance(splitLong(cashEquivalents), splitLong(currentLiabilities), splitLong(accountsReceivables), splitLong(taxesPayAbles), splitLong(taxRecoverAbles), splitLong(inventory),
                splitLong(loans), splitLong(unearnedSales), splitLong(supplyMaterial), splitLong(nonCurrentPayAbles), splitLong(prepayments),
                splitLong(tangibleAssets), splitLong(ordinaryShares), splitLong(intangibleAssets), splitLong(incomeReporting), splitLong(investments), splitLong(retainedEarnings),
                splitLong(otherNonCurrentAssets), balanceDescription, budget);
        balance._save();
        balance.currentAssets = balance.cashEquivalents + balance.accountsReceivables + balance.taxRecoverAbles + balance.inventory + balance.supplyMaterial + balance.prepayments;
        balance.nonCurrentAssets = balance.tangibleAssets + balance.intangibleAssets + balance.investments + balance.otherNonCurrentAssets;
        balance.totalLiabilities = balance.currentLiabilities + balance.taxesPayAbles + balance.unearnedSales + balance.loans + balance.nonCurrentPayAbles;
        balance.totalEquity = balance.ordinaryShares + balance.incomeReporting + balance.retainedEarnings;
        balance.totalLiabilitiesAndEquity = balance.totalLiabilities + balance.totalEquity;
        balance.totalAssets = balance.currentAssets + balance.nonCurrentAssets;
        balance._save();
        render(balance);

    }

    public static boolean calculateTotalIncome(Long budgetId) {
        Budget budget = Budget.findById(budgetId);
        budget.netSales.variance = budget.netSales.totalBudget - budget.netSales.totalActual;

        budget.netSales.variancePercent = (budget.netSales.totalBudget == 0) ? 0f : ((float) budget.netSales.totalActual * 100 / (float) budget.netSales.totalBudget);
        budget.cogs.variance = budget.cogs.totalBudget - budget.cogs.totalActual;
        budget.cogs.variancePercent = (budget.cogs.totalBudget == 0) ? 0f : ((float) budget.cogs.totalActual * 100 / (float) budget.cogs.totalBudget);
        budget.grossMargin.variance = budget.grossMargin.totalBudget - budget.grossMargin.totalActual;
        budget.grossMargin.variancePercent = (budget.grossMargin.totalBudget == 0) ? 0f : ((float) budget.grossMargin.totalActual * 100 / (float) budget.grossMargin.totalBudget);
        //System.out.println("budget.grossMargin.totalActual " + budget.grossMargin.totalActual);
        //System.out.println("budget.netSales.totalActual " + budget.netSales.totalActual);
        budget._save();
        calculateTotal(budgetId);
        return true;
    }

    public static boolean calculateTotalOpex(Long budgetId) {
        Budget budget = Budget.findById(budgetId);
        budget.opex.variance = (budget.opex.totalBudget - budget.opex.totalActual);
        budget.opex.variancePercent = (budget.opex.totalBudget == 0) ? 0f : ((float) budget.opex.totalActual * 100 / (float) budget.opex.totalBudget);
        budget.opex._save();
        calculateTotal(budget.opex.budget.id);
        return true;
    }

    public static boolean calculateTotal(Long budgetId) {
        Budget budget = Budget.findById(budgetId);
        budget.taxBeforeProfitActual = budget.operatingProfitActual + budget.grossMargin.totalActual + budget.otherIncomeActual - budget.opex.totalActual;
        budget.taxBeforeProfitBudget = budget.operatingProfitBudget + budget.grossMargin.totalBudget + budget.otherIncomeBudget - budget.opex.totalBudget;
        budget.netIncomeActual = budget.taxBeforeProfitActual - budget.taxActual;
        budget.netIncomeBudget = budget.taxBeforeProfitBudget - budget.taxBudget;
        budget.ebitdaActual = budget.netIncomeActual + budget.taxActual;
        budget.ebitdaBudget = budget.netIncomeBudget + budget.taxBudget;
        budget._save();
        return true;
    }

    public static Long splitLong(String str) {
        if (str != null && !str.equals(""))
            return Long.parseLong(str.replaceAll("'", ""));
        else
            return 0L;
    }

    public static Long splitMinusLong(String str) {
        if (str != null && !str.equals(""))
            if (str.charAt(0) == '(') {
                str = str.substring(1, str.length() - 1);
                Long minus = Long.parseLong(str.replaceAll("'", ""));
                return (minus * -1);
            } else
                return Long.parseLong(str.replaceAll("'", ""));
        else
            return 0L;
    }
}
