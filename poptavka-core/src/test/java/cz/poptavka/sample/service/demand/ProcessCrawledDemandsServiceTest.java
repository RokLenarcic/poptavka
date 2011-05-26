package cz.poptavka.sample.service.demand;

import cz.poptavka.sample.base.integration.DBUnitBaseTest;
import cz.poptavka.sample.base.integration.DataSet;
import cz.poptavka.sample.domain.demand.Demand;
import cz.poptavka.sample.domain.demand.DemandType;
import cz.poptavka.sample.util.messaging.demand.TestingDemand;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test of processing of incoming crawled demands.
 * <p>
 *     Must extend {@link DBUnitBaseTest} because it required some basic data to be presented when storing crawled
 *     demands - e.g. required {@link cz.poptavka.sample.domain.demand.DemandType}.
 * @author Juraj Martinka
 *         Date: 16.5.11
 */
@DataSet(path = {
        "classpath:cz/poptavka/sample/domain/address/LocalityDataSet.xml",
        "classpath:cz/poptavka/sample/domain/demand/CategoryDataSet.xml",
        "classpath:cz/poptavka/sample/domain/demand/DemandDataSet.xml" },
        dtd = "classpath:test.dtd")
public class ProcessCrawledDemandsServiceTest extends DBUnitBaseTest {

    @Autowired
    private ProcessCrawledDemandsService processCrawledDemandsService;

    @Autowired
    private DemandService demandService;


    @Test
    public void testProcessCrawledDemands() throws Exception {
        this.processCrawledDemandsService.processCrawledDemands(TestingDemand.generateDemands());

        // find and check the processed demands

        final Demand demandExample = new Demand();
        demandExample.setTitle(TestingDemand.TEST_DEMAND_1_TITLE);
        final Demand testDemand1 = this.demandService.findByExample(demandExample).get(0);

        Assert.assertNotNull(testDemand1.getClient());
        Assert.assertEquals(TestingDemand.TEST_DEMAND_1_EMAIL, testDemand1.getClient().getBusinessUser().getEmail());
        Assert.assertEquals(DemandType.Type.NORMAL, testDemand1.getType().getType());
    }
}