package controllers.MyClass;

import java.util.Date;

/**
 * Created by Enkh-Amgalan on 2/26/15.
 */
public class ContractChart {
    public int number;
    public Date date;
    public float amount;

    public ContractChart(Date date, int number, float amount) {
        this.number = number;
        this.date = date;
        this.amount = amount;
    }
}
