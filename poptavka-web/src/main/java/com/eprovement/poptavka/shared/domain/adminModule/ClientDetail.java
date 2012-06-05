package com.eprovement.poptavka.shared.domain.adminModule;

import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.user.Client;
import com.eprovement.poptavka.domain.user.Supplier;
import com.eprovement.poptavka.domain.user.Verification;
import com.eprovement.poptavka.shared.domain.UserDetail;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents full detail of domain object <b>Client</b> used in <i>Administration Module</i>.
 * Contains 2 static methods:  1. creating detail object
 *                             2. updating domain object
 *
 * @author Martin Slavkovsky
 *
 */
public class ClientDetail implements Serializable {

    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = -530982467233195456L;
    private Long id;
    private int overalRating = -1;
    private UserDetail userDetail = null;
    private String verification = null;
    private List<Long> supplierBlackListIds = null;
    private List<Long> demandsIds = null;

    /** for serialization. **/
    public ClientDetail() {
    }

    public ClientDetail(ClientDetail role) {
        this.updateWholeClient(role);
    }

    //---------------------------- GETTERS AND SETTERS --------------------
    public void updateWholeClient(ClientDetail detail) {
        id = detail.getId();
        overalRating = detail.getOveralRating();
        verification = detail.getVerification();
        userDetail = detail.getUserDetail();
        supplierBlackListIds = detail.getSupplierBlackListIds();
        demandsIds = detail.getDemandsIds();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public List<Long> getDemandsIds() {
        return demandsIds;
    }

    public void setDemandsIds(List<Long> demandsIds) {
        this.demandsIds = demandsIds;
    }

    public int getOveralRating() {
        return overalRating;
    }

    public void setOveralRating(int overalRating) {
        this.overalRating = overalRating;
    }

    public List<Long> getSupplierBlackListIds() {
        return supplierBlackListIds;
    }

    public void setSupplierBlackListIds(List<Long> supplierBlackListIds) {
        this.supplierBlackListIds = supplierBlackListIds;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    @Override
    public String toString() {
        return "\nGlobal Client Detail Info:"
                + "\n    ClientId=" + Long.toString(id)
                + "\n    OveralRating=" + Integer.valueOf(overalRating)
                + "\n    UserDetail=" + userDetail.toString()
                + "\n    SupplierBlackListIds=" + supplierBlackListIds.toString()
                + "\n    DemandsIds=" + demandsIds.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClientDetail other = (ClientDetail) obj;
        if (this.id != other.getId()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}