package models;

import controllers.CRUD;
import controllers.Functions;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Khuubuu on 1/12/2015.
 */
@Entity(name = "tb_contract")
public class Contract extends Model {
    @Required
    public String name;

    @Required
    public String companyName;

    @ManyToOne
    public ContractStatus status;

    @ManyToOne
    public ContractCategory category;

    @ManyToOne
    public ContractPayment payment;

    @Required
    public String title;

    @Required
    public String negj;

    public Date approvedDate;

    public Date reviewDate;

    public Date endDate;

    public Date startDate;

    public Long notifyMeBeforeEndDay;

    public Long notifyMeBeforeReview;

    public Date insertedDate;


    public String acceptedA;

    public String acceptedB;

    public String acceptedC;

    public String acceptedTA;

    public String acceptedTB;

    public String acceptedTC;


    @Column(length = 65535)
    public String notes;

    @Required
    @ManyToOne
    public User owner;

    @CRUD.Hidden
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    public List<ReminderModel> reminders;

    @CRUD.Hidden
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    public List<ContractWarrantyItem> warrantyItems;

    @CRUD.Hidden
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    public List<ContractAttachment> contractAttachments;

    @CRUD.Hidden
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    public List<ContractFinancial> contractFinancials;

    @CRUD.Hidden
    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL)
    public ContractTotalAmount totalAmount;

    public String toString() {
        return this.name;
    }

    public String getCompanyNameMin(int length) {
        if (length < companyName.length()) {
            return this.companyName.substring(0, length) + "...";
        }
        return this.companyName;
    }

    public String getTitleMin(int length) {
        if (length < title.length()) {
            return this.title.substring(0, length) + "...";
        }
        return this.title;
    }

    public ContractFinancial getInitialDeposit() {
        return ContractFinancial.find("financialType=?1 and contract.id=?2", "InitialDeposit", this.id).first();
    }

    public ContractFinancial getFinalPayment() {
        return ContractFinancial.find("financialType=?1 and contract.id=?2", "Final", this.id).first();
    }

    public List<ContractFinancial> getProgressPayment() {
        return ContractFinancial.find("financialType=?1 and contract.id=?2", "ProgressPayment", this.id).fetch();
    }

    public List<ContractFinancial> getRetention() {
        return ContractFinancial.find("financialType=?1 and contract.id=?2", "Retention", this.id).fetch();
    }

    public List<ContractWarrantyItem> getWarrantyItems() {
        return ContractWarrantyItem.find("contract.id=?1",this.id).fetch();
    }
}
