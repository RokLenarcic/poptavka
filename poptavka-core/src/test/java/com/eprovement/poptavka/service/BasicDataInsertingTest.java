package com.eprovement.poptavka.service;

import com.eprovement.poptavka.domain.enums.DemandTypeType;
import com.eprovement.poptavka.base.RealDbTest;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.enums.DemandStatus;
import com.eprovement.poptavka.domain.settings.Settings;
import com.eprovement.poptavka.domain.user.BusinessUserData;
import com.eprovement.poptavka.domain.user.Client;
import com.eprovement.poptavka.domain.user.Supplier;
import com.eprovement.poptavka.service.address.LocalityService;
import com.eprovement.poptavka.service.demand.DemandService;
import com.eprovement.poptavka.service.user.ClientService;
import com.eprovement.poptavka.service.user.SupplierService;
import com.eprovement.poptavka.util.date.DateUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * Test class which can connect to the real db and create some basic data.
 * <p>
 * All methods must be marked as Ignored by default! If someone wants to insert test data then he must
 * remove @Ignore annotation at particular method manually and run the test method.
 * Then the @Ignore has to be set on that method again.
 *
 * @author Juraj Martinka
 *         Date: 9.5.11
 */
public class BasicDataInsertingTest extends RealDbTest {


    private static final String JIHOMORAVSKY_KRAJ_CODE = "CZ064";
    @Autowired
    private ClientService clientService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private DemandService demandService;

    @Autowired
    private LocalityService localityService;




    // This test should be ignored by default. It should be run only after creating of clean database to fill
    // basic data.
    @Ignore
    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    // DO NOT ROLLBACK! We really do want to store some sample data into the database
    @Rollback(false)
    public void createTestDemands() {


        final Demand demand = new Demand();
        demand.setTitle("Title poptavka");
        demand.setType(this.demandService.getDemandType(DemandTypeType.NORMAL.getValue()));
        final BigDecimal price = BigDecimal.valueOf(10000);
        demand.setPrice(price);
        demand.setMaxSuppliers(20);
        demand.setMinRating(99);
        demand.setStatus(DemandStatus.NEW);
        final Date endDate = DateUtils.parseDate("2011-05-01");
        demand.setEndDate(endDate);
        final Date validTo = DateUtils.parseDate("2011-06-01");
        demand.setValidTo(validTo);


        final Client newClient = new Client();
        newClient.getBusinessUser().setEmail("test@poptavam.com");
        final String clientSurname = "Client";
        newClient.getBusinessUser().setBusinessUserData(
                new BusinessUserData.Builder().personFirstName("Test").personLastName(clientSurname).build());
        newClient.getBusinessUser().setSettings(new Settings());
        this.clientService.create(newClient);

        demand.setClient(clientService.getById(newClient.getId()));
        demandService.create(demand);

        final Demand createdDemand = this.demandService.getById(demand.getId());
        Assert.assertNotNull(createdDemand);
        Assert.assertEquals(price, createdDemand.getPrice());
        Assert.assertEquals(DemandStatus.NEW, createdDemand.getStatus());
        Assert.assertEquals(validTo, createdDemand.getValidTo());
        Assert.assertEquals(clientSurname,
                createdDemand.getClient().getBusinessUser().getBusinessUserData().getPersonLastName());
    }


    @Ignore
    @Test
    @Transactional(propagation = Propagation.REQUIRED)
    // DO NOT ROLLBACK! We really do want to store some sample data into the database
    @Rollback(false)
    public void createTestSuppliers() {
        final Supplier newSupplier = new Supplier();

        // set basic data
        newSupplier.getBusinessUser().setEmail("janko.hrasko@poptavam.com");
        final BusinessUserData newBusinessUserData = new BusinessUserData();
        newBusinessUserData.setPersonFirstName("Janko");
        newBusinessUserData.setPersonLastName("Hrasko");
        newBusinessUserData.setCompanyName("JankoHrasko s.r.o");
        newBusinessUserData.setPhone("+420725648999");
        newSupplier.getBusinessUser().setBusinessUserData(newBusinessUserData);

        newSupplier.setCertified(false);

//        // set supplier locality
//        final Locality locality = localityService.getLocality(JIHOMORAVSKY_KRAJ_CODE);
//        Preconditions.checkState(locality != null, "Locality with code [" + JIHOMORAVSKY_KRAJ_CODE + "] must exist!");
//        newSupplier.setLocalities(Arrays.asList(locality));

        this.supplierService.create(newSupplier);
    }
}
