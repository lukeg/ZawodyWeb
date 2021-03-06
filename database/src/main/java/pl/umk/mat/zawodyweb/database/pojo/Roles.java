/*
 * Copyright (c) 2009-2015, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.database.pojo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <p>
 * Pojo mapping TABLE public.roles</p>
 *
 * <p>
 * Generated at Fri May 08 19:01:00 CEST 2009</p>
 *
 * @author Salto-db Generator v1.1 / EJB3
 *
 */
@Entity
@Table(name = "roles", schema = "public")
@SuppressWarnings("serial")
public class Roles implements Serializable {

    /**
     * Attribute id.
     */
    private Integer id;

    /**
     * Attribute name.
     */
    private String name;

    /**
     * Attribute edituser.
     */
    private Boolean edituser;

    /**
     * Attribute addcontest.
     */
    private Boolean addcontest;

    /**
     * Attribute editcontest.
     */
    private Boolean editcontest;

    /**
     * Attribute delcontest.
     */
    private Boolean delcontest;

    /**
     * Attribute addseries.
     */
    private Boolean addseries;

    /**
     * Attribute editseries.
     */
    private Boolean editseries;

    /**
     * Attribute delseries.
     */
    private Boolean delseries;

    /**
     * Attribute addproblem.
     */
    private Boolean addproblem;

    /**
     * Attribute editproblem.
     */
    private Boolean editproblem;

    /**
     * Attribute delproblem.
     */
    private Boolean delproblem;

    /**
     * Attribute canrate.
     */
    private Boolean canrate;

    /**
     * Attribute contestant.
     */
    private Boolean contestant;

    /**
     * @return id
     */
    @Basic
    @Id
    @GeneratedValue
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    /**
     * @param id new value for id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return name
     */
    @Basic
    @Column(name = "name", length = 40)
    public String getName() {
        return name;
    }

    /**
     * @param name new value for name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return addcontest
     */
    @Basic
    @Column(name = "edituser")
    public Boolean getEdituser() {
        return edituser;
    }

    /**
     * @param addcontest new value for addcontest
     */
    public void setEdituser(Boolean edituser) {
        this.edituser = edituser;
    }

    /**
     * @return addcontest
     */
    @Basic
    @Column(name = "addcontest")
    public Boolean getAddcontest() {
        return addcontest;
    }

    /**
     * @param addcontest new value for addcontest
     */
    public void setAddcontest(Boolean addcontest) {
        this.addcontest = addcontest;
    }

    /**
     * @return editcontest
     */
    @Basic
    @Column(name = "editcontest")
    public Boolean getEditcontest() {
        return editcontest;
    }

    /**
     * @param editcontest new value for editcontest
     */
    public void setEditcontest(Boolean editcontest) {
        this.editcontest = editcontest;
    }

    /**
     * @return delcontest
     */
    @Basic
    @Column(name = "delcontest")
    public Boolean getDelcontest() {
        return delcontest;
    }

    /**
     * @param delcontest new value for delcontest
     */
    public void setDelcontest(Boolean delcontest) {
        this.delcontest = delcontest;
    }

    /**
     * @return addseries
     */
    @Basic
    @Column(name = "addseries")
    public Boolean getAddseries() {
        return addseries;
    }

    /**
     * @param addseries new value for addseries
     */
    public void setAddseries(Boolean addseries) {
        this.addseries = addseries;
    }

    /**
     * @return editseries
     */
    @Basic
    @Column(name = "editseries")
    public Boolean getEditseries() {
        return editseries;
    }

    /**
     * @param editseries new value for editseries
     */
    public void setEditseries(Boolean editseries) {
        this.editseries = editseries;
    }

    /**
     * @return delseries
     */
    @Basic
    @Column(name = "delseries")
    public Boolean getDelseries() {
        return delseries;
    }

    /**
     * @param delseries new value for delseries
     */
    public void setDelseries(Boolean delseries) {
        this.delseries = delseries;
    }

    /**
     * @return addproblem
     */
    @Basic
    @Column(name = "addproblem")
    public Boolean getAddproblem() {
        return addproblem;
    }

    /**
     * @param addproblem new value for addproblem
     */
    public void setAddproblem(Boolean addproblem) {
        this.addproblem = addproblem;
    }

    /**
     * @return editproblem
     */
    @Basic
    @Column(name = "editproblem")
    public Boolean getEditproblem() {
        return editproblem;
    }

    /**
     * @param editproblem new value for editproblem
     */
    public void setEditproblem(Boolean editproblem) {
        this.editproblem = editproblem;
    }

    /**
     * @return delproblem
     */
    @Basic
    @Column(name = "delproblem")
    public Boolean getDelproblem() {
        return delproblem;
    }

    /**
     * @param delproblem new value for delproblem
     */
    public void setDelproblem(Boolean delproblem) {
        this.delproblem = delproblem;
    }

    /**
     * @return canrate
     */
    @Basic
    @Column(name = "canrate")
    public Boolean getCanrate() {
        return canrate;
    }

    /**
     * @param canrate new value for canrate
     */
    public void setCanrate(Boolean canrate) {
        this.canrate = canrate;
    }

    /**
     * @return contestant
     */
    @Basic
    @Column(name = "contestant")
    public Boolean getContestant() {
        return contestant;
    }

    /**
     * @param contestant new value for contestant
     */
    public void setContestant(Boolean contestant) {
        this.contestant = contestant;
    }
}
