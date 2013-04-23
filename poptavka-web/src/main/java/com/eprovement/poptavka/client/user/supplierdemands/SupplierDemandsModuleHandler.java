package com.eprovement.poptavka.client.user.supplierdemands;

import com.eprovement.poptavka.client.common.security.SecuredAsyncCallback;
import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.common.session.Storage;
import com.eprovement.poptavka.client.service.demand.SupplierDemandsModuleRPCServiceAsync;
import com.eprovement.poptavka.client.user.widget.grid.UniversalAsyncGrid;
import com.eprovement.poptavka.shared.domain.DemandRatingsDetail;
import com.eprovement.poptavka.shared.domain.adminModule.OfferDetail;
import com.eprovement.poptavka.shared.domain.message.UnreadMessagesDetail;
import com.eprovement.poptavka.shared.domain.offer.SupplierOffersDetail;
import com.eprovement.poptavka.shared.domain.supplierdemands.SupplierDashboardDetail;
import com.eprovement.poptavka.shared.domain.supplierdemands.SupplierPotentialDemandDetail;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;
import java.util.List;

@EventHandler
public class SupplierDemandsModuleHandler extends BaseEventHandler<SupplierDemandsModuleEventBus> {

    @Inject
    private SupplierDemandsModuleRPCServiceAsync supplierDemandsService = null;

    //*************************************************************************/
    // Overriden methods of IEventBusData interface.                          */
    //*************************************************************************/
    public void onGetDataCount(final UniversalAsyncGrid grid, SearchDefinition searchDefinition) {
        switch (Storage.getCurrentlyLoadedView()) {
            case Constants.SUPPLIER_POTENTIAL_DEMANDS:
                getSupplierPotentialDemandsCount(grid, searchDefinition);
                break;
            case Constants.SUPPLIER_OFFERS:
                getSupplierOffersCount(grid, searchDefinition);
                break;
            case Constants.SUPPLIER_ASSIGNED_DEMANDS:
                getSupplierAssignedDemandsCount(grid, searchDefinition);
                break;
            case Constants.SUPPLIER_CLOSED_DEMANDS:
                getSupplierClosedDemandsCount(grid, searchDefinition);
                break;
            case Constants.SUPPLIER_RATINGS:
                getSupplierRatingsCount(grid, searchDefinition);
                break;
            default:
                break;
        }
    }

    public void onGetData(SearchDefinition searchDefinition) {
        switch (Storage.getCurrentlyLoadedView()) {
            case Constants.SUPPLIER_POTENTIAL_DEMANDS:
                getSupplierPotentialDemands(searchDefinition);
                break;
            case Constants.SUPPLIER_OFFERS:
                getSupplierOffers(searchDefinition);
                break;
            case Constants.SUPPLIER_ASSIGNED_DEMANDS:
                getSupplierAssignedDemands(searchDefinition);
                break;
            case Constants.SUPPLIER_CLOSED_DEMANDS:
                getSupplierClosedDemands(searchDefinition);
                break;
            case Constants.SUPPLIER_RATINGS:
                getSupplierRatings(searchDefinition);
                break;
            default:
                break;
        }
    }

    //*************************************************************************/
    // Retrieving methods - SUPPLIER WELCOME DASHBOARD                        */
    //*************************************************************************/
    public void onGetSupplierDashboardDetail() {
        supplierDemandsService.getSupplierDashboardDetail(Storage.getUser().getUserId(), Storage.getSupplierId(),
                new SecuredAsyncCallback<SupplierDashboardDetail>(eventBus) {

                    @Override
                    public void onSuccess(SupplierDashboardDetail result) {
                        eventBus.loadSupplierDashboardDetail(result);
                    }
                });
    }


    //*************************************************************************/
    // Retrieving methods                                                     */
    //*************************************************************************/

    private void getSupplierPotentialDemandsCount(final UniversalAsyncGrid grid, SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierPotentialDemandsCount(Storage.getUser().getUserId(), searchDefinition,
                new SecuredAsyncCallback<Integer>(eventBus) {

                    @Override
                    public void onSuccess(Integer result) {
                        grid.getDataProvider().updateRowCount(result, true);
                    }
                });
    }

    private void getSupplierPotentialDemands(SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierPotentialDemands(
                Storage.getUser().getUserId(), Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<List<SupplierPotentialDemandDetail>>(eventBus) {
                    @Override
                    public void onSuccess(List<SupplierPotentialDemandDetail> result) {
                        eventBus.displaySupplierDemands(result);
                    }
                });
    }

    private void getSupplierOffersCount(final UniversalAsyncGrid grid, SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierOffersCount(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<Integer>(eventBus) {
                    @Override
                    public void onSuccess(Integer result) {
                        grid.getDataProvider().updateRowCount(result, true);
                    }
                });
    }

    private void getSupplierOffers(SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierOffers(
                Storage.getSupplierId(), Storage.getUser().getUserId(), searchDefinition,
                new SecuredAsyncCallback<List<SupplierOffersDetail>>(eventBus) {
                    @Override
                    public void onSuccess(List<SupplierOffersDetail> result) {
                        eventBus.displaySupplierOffers(result);
                    }
                });
    }

    private void getSupplierAssignedDemandsCount(final UniversalAsyncGrid grid, SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierAssignedDemandsCount(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<Integer>(eventBus) {
                    @Override
                    public void onSuccess(Integer result) {
                        grid.getDataProvider().updateRowCount(result, true);
                    }
                });
    }

    private void getSupplierAssignedDemands(SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierAssignedDemands(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<List<SupplierOffersDetail>>(eventBus) {
                    @Override
                    public void onSuccess(List<SupplierOffersDetail> result) {
                        eventBus.displaySupplierAssignedDemands(result);
                    }
                });
    }

    private void getSupplierClosedDemandsCount(final UniversalAsyncGrid grid, SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierClosedDemandsCount(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<Integer>(eventBus) {
                    @Override
                    public void onSuccess(Integer result) {
                        grid.getDataProvider().updateRowCount(result, true);
                    }
                });
    }

    private void getSupplierClosedDemands(SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierClosedDemands(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<List<SupplierOffersDetail>>(eventBus) {
                    @Override
                    public void onSuccess(List<SupplierOffersDetail> result) {
                        eventBus.displaySupplierAssignedDemands(result);
                    }
                });
    }

    private void getSupplierRatingsCount(final UniversalAsyncGrid grid, SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierRatingsCount(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<Integer>(eventBus) {
                    @Override
                    public void onSuccess(Integer result) {
                        grid.getDataProvider().updateRowCount(result, true);
                    }
                });
    }

    private void getSupplierRatings(SearchDefinition searchDefinition) {
        supplierDemandsService.getSupplierRatings(
                Storage.getSupplierId(), searchDefinition,
                new SecuredAsyncCallback<List<DemandRatingsDetail>>(eventBus) {
                    @Override
                    public void onSuccess(List<DemandRatingsDetail> result) {
                        eventBus.displaySupplierRatings(result);
                    }
                });
    }

    /**************************************************************************/
    /* Other                                                                  */
    /**************************************************************************/
    public void onRequestFinishAndRateClient(final long demandID, final long offerID,
            final Integer rating, final String message) {
        supplierDemandsService.finishOfferAndEnterFeedbackForClient(demandID, offerID, rating, message,
                new SecuredAsyncCallback<Void>(eventBus) {
                    @Override
                    public void onSuccess(Void result) {
                        eventBus.sendStatusMessage(Storage.MSGS.finishedOfferMessage());
                        eventBus.responseFeedback();
                    }
                });
    }

    //request? better would be update

    public void onRequestEditOffer(long id) {
        //TODO LATER ivlcek: maybe we will not enable this functionality. Consider removing
    }

    public void onUpdateUnreadMessagesCount() {
        supplierDemandsService.updateUnreadMessagesCount(new SecuredAsyncCallback<UnreadMessagesDetail>(eventBus) {
            @Override
            public void onSuccess(UnreadMessagesDetail result) {
                eventBus.setUpdatedUnreadMessagesCount(result);
            }
        });
    }

    public void onUpdateOfferStatus(OfferDetail offerDetail) {
        //TODO LATER ivlcek: consider removing. Supplier shouldn't change his offer status
    }
}
