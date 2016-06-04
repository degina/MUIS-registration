package controllers;

import play.jobs.Job;
import play.jobs.On;

import java.util.List;

/**
 * Created by enkhamgalan on 3/15/15.
 */
@On("0 0 3 * * ?")
public class LiveService extends Job {
    //    @On("second minut hour,hour * * ?")
//    @Every("5s")

    public void doJob() {

    }

    public float setCompletedPercent(float value) {
//        if (unit == null) {
//            return value;
//        } else {
//            List<TaskPlan> planList = TaskPlan.find("task.id=?", this.id).fetch();
//            float summa = 0, totVal = 0;
//            for (TaskPlan plan : planList) {
//                summa += plan.plan;
//                if (plan.value != null) totVal += plan.value;
//            }
//            return (totVal * 100) / summa;
//        }
        return 100;
    }
}
