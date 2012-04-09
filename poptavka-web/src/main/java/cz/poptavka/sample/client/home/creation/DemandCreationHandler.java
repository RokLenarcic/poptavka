package cz.poptavka.sample.client.home.creation;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

import cz.poptavka.sample.client.main.errorDialog.ErrorDialogPopupView;
import cz.poptavka.sample.client.service.demand.DemandCreationRPCServiceAsync;
import cz.poptavka.sample.client.service.demand.UserRPCServiceAsync;
import cz.poptavka.sample.shared.domain.LoggedUserDetail;
import cz.poptavka.sample.shared.domain.UserDetail;
import cz.poptavka.sample.shared.domain.demand.FullDemandDetail;
import cz.poptavka.sample.shared.exceptions.CommonException;

/**
 * Handler for RPC calls for DemandCreationModule
 *
 * @author Beho
 *
 */
@EventHandler
public class DemandCreationHandler extends BaseEventHandler<DemandCreationEventBus> {

    private static final Logger LOGGER = Logger.getLogger("MainHandler");

    private DemandCreationRPCServiceAsync demandCreationService = null;
    private UserRPCServiceAsync userRpcService;

    private ErrorDialogPopupView errorDialog;

    @Inject
    void setDemandCreationModuleRPCServiceAsync(DemandCreationRPCServiceAsync service) {
        demandCreationService = service;
    }

    @Inject
    void setUserRpcService(UserRPCServiceAsync userRpcService) {
        this.userRpcService = userRpcService;
    }

    /**
     * Verify identity of user, if exists in the system.
     * If so, new demand is created.
     *
     * @param client existing user detail
     */
    public void onVerifyExistingClient(final UserDetail client) {
        LOGGER.fine("verify start");
        userRpcService.loginUser(client, new AsyncCallback<LoggedUserDetail>() {
            @Override
            public void onFailure(Throwable loginException) {
                LOGGER.info("login error:" + loginException.getMessage());
                eventBus.loadingHide();
                eventBus.loginError();
            }

            @Override
            public void onSuccess(final LoggedUserDetail loggedUser) {
                userRpcService.getUserById(loggedUser.getUserId(), new AsyncCallback<UserDetail>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO
                        if (caught instanceof CommonException) {
                            CommonException commonException = (CommonException) caught;
                            errorDialog = new ErrorDialogPopupView();
                            errorDialog.show(commonException.getSymbol());
                        }
                        throw new IllegalStateException("Cannot get business user for user id="
                                + loggedUser.getUserId());
                    }

                    @Override
                    public void onSuccess(UserDetail businessUserDetail) {
                        eventBus.prepareNewDemandForNewClient(businessUserDetail);
                    }
                });
            }
        });
    }

    /**
     * Method registers new client and afterwards creates new demand.
     *
     * @param client newly created client
     */
    public void onRegisterNewClient(UserDetail client) {
        demandCreationService.createNewClient(client, new AsyncCallback<UserDetail>() {

            @Override
            public void onFailure(Throwable arg0) {
                if (arg0 instanceof CommonException) {
                    CommonException commonException = (CommonException) arg0;
                    errorDialog = new ErrorDialogPopupView();
                    errorDialog.show(commonException.getSymbol());
                }
            }

            @Override
            public void onSuccess(UserDetail client) {
                if (client.getClientId() != -1) {
                    eventBus.prepareNewDemandForNewClient(client);
                }
            }
        });
    }

    /**
     * Creates new demand.
     *
     * @param detail
     *            front-end entity of demand
     * @param clientId
     *            client id
     */
    public void onCreateDemand(FullDemandDetail detail, Long clientId) {
        demandCreationService.createNewDemand(detail, clientId,
                new AsyncCallback<FullDemandDetail>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.loadingHide();
                        if (caught instanceof CommonException) {
                            CommonException commonException = (CommonException) caught;
                            errorDialog = new ErrorDialogPopupView();
                            errorDialog.show(commonException.getSymbol());
                        }
                    }

                    @Override
                    public void onSuccess(FullDemandDetail result) {
                        // signal event
                        eventBus.loadingHide();
                        // TODO forward to user/atAccount
//                        eventBus.addNewDemand(result);
                    }
                });
        LOGGER.info("submitting new demand");
    }

    public void onCheckFreeEmail(String email) {
        userRpcService.checkFreeEmail(email, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof CommonException) {
                    CommonException commonException = (CommonException) caught;
                    errorDialog = new ErrorDialogPopupView();
                    errorDialog.show(commonException.getSymbol());
                }
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