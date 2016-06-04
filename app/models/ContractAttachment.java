package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Khuubuu on 1/13/2015.
 */
@Entity(name = "tb_contract_attachment")
public class ContractAttachment extends ModelAttach {

    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @Required
    @ManyToOne
    @CRUD.Hidden
    public Contract contract;

    public ContractAttachment(String name,String dir,String extension,Float filesize,Contract contract){
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.contract = contract;
    }
}
