/*
 * Copyright (C) 2011, eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.shared.domain;

import com.eprovement.poptavka.client.common.validation.Email;
import com.eprovement.poptavka.client.common.validation.Extended;
import com.eprovement.poptavka.shared.domain.adminModule.AccessRoleDetail;
import java.util.ArrayList;
import com.google.gwt.user.client.rpc.IsSerializable;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Detail object encapsulating all information provided when user is logged on.
 *
 * @see com.eprovement.poptavka.client.service.demand.UserRPCService
 * @see com.eprovement.poptavka.service.user.LoginService
 */
public class UserDetail implements IsSerializable {

    /**
     * Logged user must always has non-null id set!
     */
    private long userId;

    @NotBlank(message = "{emailNotBlank}")
    @Email(message = "{patternEmail}", groups = Extended.class)
    @Size(max = 255, message = "{emailSize}", groups = Extended.class)
    private String email;

    @NotBlank(message = "{passwordNotBlank}")
    @Size(min = 5, max = 255, message = "{passwordSize}", groups = Extended.class)
    private String password;

    private ArrayList<AccessRoleDetail> accessRoles;

    /**
     * Required for GWT
     */
    public UserDetail() {
        //for serialization
    }

    public UserDetail(long userId, String email, ArrayList<AccessRoleDetail> accessRoles) {
        this.userId = userId;
        this.email = email;
        this.accessRoles = accessRoles;
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<AccessRoleDetail> getAccessRoles() {
        return accessRoles;
    }

    public void setAccessRoles(ArrayList<AccessRoleDetail> accessRoles) {
        this.accessRoles = accessRoles;
    }
}
