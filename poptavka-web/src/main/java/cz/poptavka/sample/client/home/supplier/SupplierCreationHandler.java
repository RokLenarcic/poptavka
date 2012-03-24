package cz.poptavka.sample.client.home.supplier;

import java.util.logging.Logger;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;
import cz.poptavka.sample.client.service.demand.SupplierCreationRPCServiceAsync;
import cz.poptavka.sample.shared.domain.UserDetail;

/**
 * Handler for RPC calls for localities & categories.
 *
 * @author Beho
 *
 */
@EventHandler
public class SupplierCreationHandler extends BaseEventHandler<SupplierCreationEventBus> {

    private SupplierCreationRPCServiceAsync supplierCreationService = null;
    private static final Logger LOGGER = Logger.getLogger("MainHandler");

    @Inject
    void setSupplierCreationRPCService(SupplierCreationRPCServiceAsync service) {
        supplierCreationService = service;
    }

    public void onRegisterSupplier(UserDetail newSupplier) {
        supplierCreationService.createNewSupplier(newSupplier, new AsyncCallback<UserDetail>() {

            @Override
            public void onFailure(Throwable arg0) {
                eventBus.loadingHide();
                Window.alert("Unexpected error occured! \n" + arg0.getMessage());
            }

            @Override
            public void onSuccess(UserDetail supplier) {
                // TODO forward to user/atAccount
                eventBus.loadingHide();
            }
        });
    }

    public void onCheckFreeEmail(String email) {
        supplierCreationService.checkFreeEmail(email, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable arg0) {
            }

            @Override
            public void onSuccess(Boolean result) {
                LOGGER.fine("result of compare " + result);
                eventBus.checkFreeEmailResponse(result);
                // eventBus.checkFreeEmailResponse();
            }
        });
    }
}
