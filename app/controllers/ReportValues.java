package controllers;

import com.google.gson.JsonArray;
import models.PunchList_Status;
import models.RFI_Status;

import java.util.List;

/**
 * Created by enkhamgalan on 3/14/15.
 */
public class ReportValues {

    public List<RFI_Status> rfi_status;
    public List<PunchList_Status> punchList_status;
    public List<Long> manPowerDate;
    public float countSumRFI = 0;
    public float countSumPunch = 0;
    public int totalManpower = 0;
    public int totalManHour = 0;
    public JsonArray projPerDay = new JsonArray();
    public JsonArray projPerMust = new JsonArray();
    public JsonArray projPerName = new JsonArray();
}
