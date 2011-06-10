package cz.poptavka.sample.client.home.demands;

import java.util.logging.Logger;

import com.mvp4g.client.annotation.History;
import com.mvp4g.client.annotation.History.HistoryConverterType;
import com.mvp4g.client.history.HistoryConverter;

@History(type = HistoryConverterType.NONE, name = "displayDemandsHistoryConverter")
public class DisplayDemandsHistoryConverter implements HistoryConverter<DemandsEventBus> {

    private static final Logger LOGGER = Logger.getLogger(DisplayDemandsHistoryConverter.class.getName());

    @Override
    public void convertFromToken(String historyName, String param, DemandsEventBus eventBus) {
        eventBus.setHistoryStoredForNextOne(false);
        eventBus.atDemands();

//        if (historyName.equals("atAttachment")) {
//            eventBus.atAttachment();
//        }
//        if (historyName.equals("atLogin")) {
//            eventBus.atLogin();
//        }
//        if (historyName.equals("atRegisterSupplier")) {
//            eventBus.atRegisterSupplier();
//        }
    }

    @Override
    public boolean isCrawlable() {
        // TODO Auto-generated method stub
        return true;
    }

}
