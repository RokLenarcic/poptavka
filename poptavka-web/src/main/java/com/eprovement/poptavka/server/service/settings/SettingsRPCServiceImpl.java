package com.eprovement.poptavka.server.service.settings;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gwt.core.client.GWT;
import com.googlecode.genericdao.search.Search;

import com.eprovement.poptavka.client.service.demand.SettingsRPCService;
import com.eprovement.poptavka.domain.address.Address;
import com.eprovement.poptavka.domain.address.Locality;
import com.eprovement.poptavka.domain.demand.Category;
import com.eprovement.poptavka.domain.user.BusinessUser;
import com.eprovement.poptavka.domain.user.BusinessUserRole;
import com.eprovement.poptavka.domain.user.Client;
import com.eprovement.poptavka.domain.user.Supplier;
import com.eprovement.poptavka.domain.user.User;
import com.eprovement.poptavka.server.converter.Converter;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.shared.domain.AddressDetail;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.domain.LocalityDetail;
import com.eprovement.poptavka.shared.domain.SupplierDetail;
import com.eprovement.poptavka.shared.domain.settings.SettingsDetail;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import org.springframework.beans.factory.annotation.Qualifier;

@Component(SettingsRPCService.URL)
public class SettingsRPCServiceImpl extends AutoinjectingRemoteService
        implements SettingsRPCService {

    private static final long serialVersionUID = 113266708108432157L;
    private GeneralService generalService;
    private Converter<Locality, LocalityDetail> localityConverter;
    private Converter<Category, CategoryDetail> categoryConverter;

    @Autowired
    public void setLocalityConverter(
            @Qualifier("localityConverter") Converter<Locality, LocalityDetail> localityConverter) {
        this.localityConverter = localityConverter;
    }

    @Autowired
    public void setCategoryConverter(
            @Qualifier("categoryConverter") Converter<Category, CategoryDetail> categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    //TODO Nahradit konverterom???
    @Override
    public SettingsDetail getUserSettings(long userId) throws RPCException {
        GWT.log("Getting user settings for user:" + userId);
        final BusinessUser user = (BusinessUser) generalService.searchUnique(new Search(User.class).addFilterEqual("id",
                userId));

        SettingsDetail settingsDetail = new SettingsDetail();

        List<BusinessUserRole> roles = user.getBusinessUserRoles();
        for (BusinessUserRole role : roles) {
            if (role instanceof Client) {
                settingsDetail.setEmail(role.getBusinessUser().getEmail());
                Client client = (Client) role;
                if (client.getOveralRating() != null) {
                    settingsDetail.setClientRating(client.getOveralRating());
                }

            }
            if (role instanceof Supplier) {
                Supplier supplier = (Supplier) role;
                SupplierDetail supplierDetail = new SupplierDetail();
                if (supplier.getOveralRating() != null) {
                    supplierDetail.setOverallRating(supplier.getOveralRating());
                }
                supplierDetail.setLocalities(localityConverter.convertToTargetList(supplier.getLocalities()));
                supplierDetail.setCategories(categoryConverter.convertToTargetList(supplier.getCategories()));
                settingsDetail.setSupplier(supplierDetail);
            }

        }
        settingsDetail.setFirstName(user.getBusinessUserData().getPersonFirstName());
        settingsDetail.setLastName(user.getBusinessUserData().getPersonLastName());
        settingsDetail.setPhone(user.getBusinessUserData().getPhone());
        settingsDetail.setIdentificationNumber(user.getBusinessUserData().getIdentificationNumber());
        settingsDetail.setCompanyName(user.getBusinessUserData().getCompanyName());
        settingsDetail.setDescription(user.getBusinessUserData().getDescription());
        settingsDetail.setTaxId(user.getBusinessUserData().getTaxId());
        List<AddressDetail> addresses = new ArrayList<AddressDetail>();
        for (Address address : user.getAddresses()) {
            AddressDetail detail = new AddressDetail();
            if (address.getCity() != null) {
                detail.setCity(address.getCity().getName());
            }
            detail.setStreet(address.getStreet());
            detail.setZipCode(address.getZipCode());
            addresses.add(detail);
        }
        settingsDetail.setAddresses(addresses);
        GWT.log("User settings get:" + settingsDetail.getFirstName());
        return settingsDetail;
    }

    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }
}
