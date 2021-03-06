package com.eprovement.poptavka.service;

import com.eprovement.poptavka.base.integration.DBUnitIntegrationTest;
import com.eprovement.poptavka.base.integration.DataSet;
import com.eprovement.poptavka.domain.common.ResultCriteria;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.service.address.LocalityService;
import com.eprovement.poptavka.service.demand.CategoryService;
import com.eprovement.poptavka.service.demand.DemandService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Juraj Martinka
 *         Date: 24.4.11
 */
@DataSet(path = {
        "classpath:com/eprovement/poptavka/domain/register/RegisterDataSet.xml",
        "classpath:com/eprovement/poptavka/domain/address/LocalityDataSet.xml",
        "classpath:com/eprovement/poptavka/domain/demand/CategoryDataSet.xml",
        "classpath:com/eprovement/poptavka/domain/demand/RatingDataSet.xml",
        "classpath:com/eprovement/poptavka/domain/user/UsersDataSet.xml",
        "classpath:com/eprovement/poptavka/domain/demand/DemandDataSet.xml" },
        dtd = "classpath:test.dtd")
public class GenericServiceIntegrationTest extends DBUnitIntegrationTest {

    @Autowired
    private LocalityService localityService;

    @Autowired
    private DemandService demandService;

    @Autowired
    private CategoryService categoryService;


    //--------------------------------  METHODS FOR TESTING filtering based on <code>ResultCriteria</code> -------------
    @Test
    public void testGetAllWithNoCriteria() {
        // no restrictions
        final List<Demand> allDemands = this.demandService.getAll(ResultCriteria.EMPTY_CRITERIA);
        Assert.assertEquals(18, allDemands.size());
    }


    @Test
    public void testGetAllWithMaxResults() {
        // restrict number of demands - no ordering is guaranteed!
        final List<Demand> allDemandsMaxResults = this.demandService.getAll(new ResultCriteria.Builder()
                .maxResults(5)
                .build());
        Assert.assertEquals(5, allDemandsMaxResults.size());

    }

    @Test
    public void testGetAllWithFirstResultAndMaxResults() {
        // restrict number of demands - no ordering is guaranteed!
        final int maxDemands = 5;
        final int firstResult = 2;
        final List<Demand> allDemandsMaxResults = this.demandService.getAll(new ResultCriteria.Builder()
                .maxResults(maxDemands)
                .firstResult(firstResult)
                .build());
        Assert.assertEquals(maxDemands, allDemandsMaxResults.size());
    }


    @Test
    public void testGetAllWithFirstResultAndMaxResultsOrderBy() {
        // restrict number of demands - no ordering is guaranteed!
        final int maxDemands = 5;
        final int firstResult = 2;
        final List<Demand> allDemandsMaxResults = this.demandService.getAll(new ResultCriteria.Builder()
                .maxResults(maxDemands)
                .firstResult(firstResult)
                .orderByColumns(Arrays.asList("id"))
                .build());
        Assert.assertEquals(maxDemands, allDemandsMaxResults.size());
        checkDemandExistence(3L, allDemandsMaxResults);
        checkDemandExistence(4L, allDemandsMaxResults);
        checkDemandExistence(5L, allDemandsMaxResults);
        checkDemandExistence(6L, allDemandsMaxResults);
        checkDemandExistence(7L, allDemandsMaxResults);
        checkDemandNonExistence(2L, allDemandsMaxResults);
        checkDemandNonExistence(10L, allDemandsMaxResults);
    }


    @Test
    public void testGetAllWithFirstResultOrderyBy() {
        // restrict number of demands - no ordering is guaranteed!
        final int firstResult = 8;
        final List<Demand> allDemandsMaxResults = this.demandService.getAll(new ResultCriteria.Builder()
                .firstResult(firstResult)
                .orderByColumns(Arrays.asList("id"))
                .build());
        Assert.assertEquals(10, allDemandsMaxResults.size());
        checkDemandExistence(9L, allDemandsMaxResults);
        checkDemandExistence(10L, allDemandsMaxResults);
        checkDemandExistence(18L, allDemandsMaxResults);
        checkDemandExistence(21L, allDemandsMaxResults);
        checkDemandExistence(22L, allDemandsMaxResults);
        checkDemandExistence(23L, allDemandsMaxResults);
        checkDemandExistence(70L, allDemandsMaxResults);
        checkDemandExistence(80L, allDemandsMaxResults);
        checkDemandExistence(90L, allDemandsMaxResults);
    }

    @Test
    public void testGetAllWithFirstResultBeyondMaximum() {
        // restrict number of demands - no ordering is guaranteed!
        final int firstResult = 18;
        final List<Demand> allDemands = this.demandService.getAll(new ResultCriteria.Builder()
                .firstResult(firstResult)
                .orderByColumns(Arrays.asList("id"))
                .build());
        Assert.assertTrue(allDemands.isEmpty());
    }


    //----------------------------------  TESTING COUNTS -----------------------

    @Test
    public void testGetCount() {
        Assert.assertEquals("Locality count is incorrect.", 12,
                this.localityService.getCount());
        Assert.assertEquals("Category count is incorrect.", 17,
                this.categoryService.getCount());
        Assert.assertEquals("Demand count is incorrect.", 18,
                this.demandService.getCount());
    }


    //----------------------------------  HELPER METHODS ---------------------------------------------------------------

    private void checkDemandExistence(final Long id, Collection<Demand> allDemands) {
        Assert.assertTrue(CollectionUtils.exists(allDemands, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return id.equals(((Demand) object).getId());
            }
        }));
    }

    private void checkDemandNonExistence(final Long id, Collection<Demand> allDemands) {
        Assert.assertFalse(CollectionUtils.exists(allDemands, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return id.equals(((Demand) object).getId());
            }
        }));
    }
}
