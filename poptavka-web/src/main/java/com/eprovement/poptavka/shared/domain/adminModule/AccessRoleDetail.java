package com.eprovement.poptavka.shared.domain.adminModule;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;

/**
 * Represents full detail of domain object <b>AccessRole</b> used in
 * <i>Administration Module</i>. Contains 2 static methods: 1. creating detail
 * object 2. updating domain object
 *
 * @author Martin Slavkovsky
 *
 */
public class AccessRoleDetail implements IsSerializable {

    private Long id;
    private String name;
    private String description;
    private String code;
    private ArrayList<PermissionDetail> permissions;

    /**
     * for serialization. *
     */
    public AccessRoleDetail() {
    }

    public AccessRoleDetail(String roleCode) {
        code = roleCode;
    }

    public AccessRoleDetail(AccessRoleDetail role) {
        this.updateWholeAccessRole(role);
    }

    //---------------------------- GETTERS AND SETTERS --------------------
    public void updateWholeAccessRole(AccessRoleDetail roleDetail) {
        id = roleDetail.getId();
        name = roleDetail.getName();
        description = roleDetail.getDescription();
        code = roleDetail.getCode();
        permissions = roleDetail.getPermissions();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ArrayList<PermissionDetail> getPermissions() {
        return permissions;
    }

    public void setPermissions(ArrayList<PermissionDetail> permissions) {
        this.permissions = permissions;
//        Collections.sort(this.permissions);
    }

    @Override
    public String toString() {
        return "\nGlobal AccessRole Detail Info:"
                + "\n    AccessRoleId=" + Long.toString(id)
                + "\n    Name=" + name
                + "\n    Code=" + code
                + "\n    Description=" + description
                + "\n    Permissions=" + permissions.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AccessRoleDetail other = (AccessRoleDetail) obj;
        if (this.code.equalsIgnoreCase(other.getCode())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
