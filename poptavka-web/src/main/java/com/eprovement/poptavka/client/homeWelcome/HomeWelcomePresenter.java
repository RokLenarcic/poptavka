package com.eprovement.poptavka.client.homeWelcome;

import com.eprovement.poptavka.client.common.security.SecuredAsyncCallback;
import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.homeWelcome.interfaces.IHomeWelcomeView;
import com.eprovement.poptavka.client.homeWelcome.interfaces.IHomeWelcomeView.IHomeWelcomePresenter;
import com.eprovement.poptavka.client.service.demand.SimpleRPCServiceAsync;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import java.util.ArrayList;

@Presenter(view = HomeWelcomeView.class)
public class HomeWelcomePresenter extends BasePresenter<IHomeWelcomeView, HomeWelcomeEventBus> implements
        IHomeWelcomePresenter {

    //columns number of root chategories in parent widget
    private static final int COLUMNS = 4;
    private SimpleRPCServiceAsync simpleService;


    @Inject
    void setSimpleService(SimpleRPCServiceAsync service) {
        simpleService = service;
    }

    /**************************************************************************/
    /* General Module events                                                  */
    /**************************************************************************/
    public void onStart() {
        //nothing
    }

    public void onForward() {
        eventBus.setUpSearchBar(null);
        //Martin Temporary commented - not to load categories at application startup
        //eventBus.getRootCategories();
    }

    /**************************************************************************/
    /* Navigation events                                                      */
    /**************************************************************************/
    public void onGoToHomeWelcomeModule(SearchModuleDataHolder searchDataHolder) {
    }

    @Override
    public void bind() {
        view.getCategorySelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                CategoryDetail selected = (CategoryDetail) view.getCategorySelectionModel().getSelectedObject();

                if (selected != null) {
                    SearchModuleDataHolder searchDataHolder = new SearchModuleDataHolder();
                    searchDataHolder.getCategories().add(selected);
                    eventBus.goToHomeDemandsModule(searchDataHolder, Constants.HOME_DEMANDS_BY_WELCOME);
                }
            }
        });

        view.getCreateDemandButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // do something
                simpleService.getData(new SecuredAsyncCallback<String>(eventBus) {

                    public void onServiceFailure(Throwable caught) {
                        // Show the RPC error message to the user
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.center();
                        dialogBox.setModal(true);
                        dialogBox.setGlassEnabled(true);
                        dialogBox.setAutoHideEnabled(true);

                        dialogBox.setText("Remote Procedure Call - Failure");
                        dialogBox.show();
                    }

                    public void onSuccess(String result) {
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.center();
                        dialogBox.setModal(true);
                        dialogBox.setGlassEnabled(true);
                        dialogBox.setAutoHideEnabled(true);

                        dialogBox.setText(result);
                        dialogBox.show();
                    }
                });
            }
        });

        view.getSecuredButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // do something
                simpleService.getSecuredData(new SecuredAsyncCallback<String>(eventBus) {

                    public void onServiceFailure(Throwable caught) {
                        // Show the RPC error message to the user
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.center();
                        dialogBox.setModal(true);
                        dialogBox.setGlassEnabled(true);
                        dialogBox.setAutoHideEnabled(true);

                        dialogBox.setText("Remote Procedure Call - Failure");
                        dialogBox.show();
                    }

                    public void onSuccess(String result) {
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.center();
                        dialogBox.setModal(true);
                        dialogBox.setGlassEnabled(true);
                        dialogBox.setAutoHideEnabled(true);

                        dialogBox.setText(result);
                        dialogBox.show();
                    }
                });
            }
        });

        view.getSendUsEmailButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.sendUsEmail(Constants.SUBJECT_GENERAL_QUESTION, "");

            }
        });

    }

    /**************************************************************************/
    /* Business events handled by presenter                                   */
    /**************************************************************************/
    /**
     * Methods displays root categories. These categories are divided into cellLists (columns),
     * where number of columns depends on constant: COLUMNS.
     * @param rootCategories - root categories to be displayed
     */
    public void onDisplayCategories(ArrayList<CategoryDetail> rootCategories) {
        view.displayCategories(COLUMNS, rootCategories);
    }

    /**************************************************************************/
    /* Business events handled by eventbus or RPC                             */
    /**************************************************************************/
}
