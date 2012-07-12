/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.client.user.clientdemands.widgets;

import com.eprovement.poptavka.client.main.Constants;
import com.eprovement.poptavka.client.main.Storage;
import com.eprovement.poptavka.client.user.clientdemands.ClientDemandsEventBus;
import com.eprovement.poptavka.client.user.widget.DevelDetailWrapperPresenter;
import com.eprovement.poptavka.client.user.widget.grid.UniversalAsyncGrid;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientProjectConversationDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientProjectDetail;
import com.eprovement.poptavka.shared.domain.message.MessageDetail;
import com.eprovement.poptavka.shared.domain.type.ViewType;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.LazyPresenter;
import com.mvp4g.client.view.LazyView;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Presenter(view = ClientProjectsView.class)
public class ClientProjectsPresenter
        extends LazyPresenter<ClientProjectsPresenter.ClientProjectsLayoutInterface, ClientDemandsEventBus> {

    public interface ClientProjectsLayoutInterface extends LazyView, IsWidget {

        // Columns
        Header getCheckHeader();

        Column<ClientProjectConversationDetail, Boolean> getCheckColumn();

        Column<ClientProjectConversationDetail, Boolean> getStarColumn();

        Column<ClientProjectConversationDetail, String> getSupplierNameColumn();

        Column<ClientProjectConversationDetail, String> getBodyPreviewColumn();

        Column<ClientProjectConversationDetail, String> getDateColumn();

        // Buttons
        Button getReadBtn();

        Button getStarBtn();

        Button getUnreadBtn();

        Button getUnstarBtn();

        Button getBackBtn();

        // Others
        UniversalAsyncGrid<ClientProjectDetail> getDemandGrid();

        UniversalAsyncGrid<ClientProjectConversationDetail> getConversationGrid();

        int getDemandPageSize();

        int getConversationPageSize();

        List<Long> getSelectedIdList();

        Set<ClientProjectConversationDetail> getSelectedMessageList();

        SimplePanel getWrapperPanel();

        IsWidget getWidgetView();

        // Setters
        void setConversationTableVisible(boolean visible);

        void setDemandTitleLabel(String text);
    }
    /**************************************************************************/
    /* Attributes                                                             */
    /**************************************************************************/
    //viewType
    private ViewType type = ViewType.EDITABLE;
    private DevelDetailWrapperPresenter detailSection = null;
    private SearchModuleDataHolder searchDataHolder;
    //attrribute preventing repeated loading of demand detail, when clicked on the same demand
    private long lastOpenedProjectConversation = -1;

    /**************************************************************************/
    /* General Module events                                                  */
    /**************************************************************************/
    public void onStart() {
        // nothing
    }

    public void onForward() {
        // nothing
    }

    /**************************************************************************/
    /* Bind actions                                                           */
    /**************************************************************************/
    @Override
    public void bindView() {
        // Selection Handlers
        addDemandTableSelectionHandler();
        // Field Updaters
        addCheckHeaderUpdater();
        addStarColumnFieldUpdater();
        addTextColumnFieldUpdaters();
        // Buttons Actions
        addBackButtonHandler();
        addReadButtonHandler();
        addUnreadButtonHandler();
        addStarButtonHandler();
        addUnstarButtonHandler();
    }

    /**************************************************************************/
    /* Navigation events */
    /**************************************************************************/
    public void onInitClientProjects(SearchModuleDataHolder filter) {
        Storage.setCurrentlyLoadedView(Constants.DEMANDS_CLIENT_PROJECTS);
        searchDataHolder = filter;
        view.getDemandGrid().getDataCount(eventBus, searchDataHolder);

        view.getWidgetView().asWidget().setStyleName(Storage.RSCS.common().userContent());
        eventBus.displayView(view.getWidgetView());
        //init wrapper widget
        if (detailSection == null) {
            detailSection = eventBus.addHandler(DevelDetailWrapperPresenter.class);
            detailSection.initDetailWrapper(view.getWrapperPanel(), type);
        }
    }

    /**************************************************************************/
    /* Business events handled by presenter */
    /**************************************************************************/
    /**
     * DEVEL METHOD
     *
     * Used for JRebel correct refresh. It is called from DemandModulePresenter, when removing instance of
     * SupplierListPresenter. it has to remove it's detailWrapper first.
     */
    public void develRemoveDetailWrapper() {
        detailSection.develRemoveReplyWidget();
        eventBus.removeHandler(detailSection);
    }

    /**
     * Response method for onInitSupplierList()
     * @param data
     */
    public void onDisplayClientsProjects(List<ClientProjectDetail> data) {
        GWT.log("++ onResponseClientsProjects");

        view.getDemandGrid().updateRowData(data);
    }

    public void onDisplayClientsProjectConversations(List<ClientProjectConversationDetail> data) {
        GWT.log("++ onResponseClientsProjects");

        view.getConversationGrid().updateRowData(data);
    }

    /**
     * New data are fetched from db.
     *
     * @param demandId ID for demand detail
     * @param messageId ID for demand related conversation
     * @param userMessageId ID for demand related conversation
     */
    public void displayDetailContent(Long demandId, long messageId, Long userMessageId) {
        //TODO
        //copy role check from old implementation
        //
        //

        //can be solved by enum in future or can be accesed from storage class
        detailSection.showLoading(DevelDetailWrapperPresenter.DETAIL);
        eventBus.requestDemandDetail(demandId, type);

        //add conversation loading events and so on
        detailSection.showLoading(DevelDetailWrapperPresenter.CHAT);
        eventBus.requestChatForSupplierList(messageId, userMessageId, Storage.getUser().getUserId());

        //init default replyWidget
        //it is initalized now, because we do not need to have it visible before first demand selection
        detailSection.initReplyWidget();
    }

    public void onSendMessageResponse(MessageDetail sentMessage, ViewType handlingType) {
        //neccessary check for method to be executed only in appropriate presenter
        if (type.equals(handlingType)) {
            detailSection.addConversationMessage(sentMessage);
        }
    }

    /**************************************************************************/
    /* Business events handled by eventbus or RPC                             */
    /**************************************************************************/
    /**************************************************************************/
    /* Bind View helper methods                                               */
    /**************************************************************************/
    // Field Updaters
    public void addCheckHeaderUpdater() {
        view.getCheckHeader().setUpdater(new ValueUpdater<Boolean>() {

            @Override
            public void update(Boolean value) {
                List<ClientProjectConversationDetail> rows = view.getConversationGrid().getVisibleItems();
                for (ClientProjectConversationDetail row : rows) {
                    ((MultiSelectionModel) view.getConversationGrid().getSelectionModel()).setSelected(row, value);
                }
            }
        });
    }

    public void addStarColumnFieldUpdater() {
        view.getStarColumn().setFieldUpdater(new FieldUpdater<ClientProjectConversationDetail, Boolean>() {

            @Override
            public void update(int index, ClientProjectConversationDetail object, Boolean value) {
//              TableDisplay obj = (TableDisplay) object;
                object.setStarred(!value);
                view.getConversationGrid().redraw();
                Long[] item = new Long[]{object.getUserMessageId()};
                eventBus.requestStarStatusUpdate(Arrays.asList(item), !value);
            }
        });
    }

    public void addTextColumnFieldUpdaters() {
        FieldUpdater textFieldUpdater = new FieldUpdater<ClientProjectConversationDetail, String>() {

            @Override
            public void update(int index, ClientProjectConversationDetail object, String value) {
                //Neviem ci porovnamam so spravnym atributoms
                if (lastOpenedProjectConversation != object.getUserMessageId()) {
                    //DisplayDetail
                }
            }
        };
        view.getSupplierNameColumn().setFieldUpdater(textFieldUpdater);
        view.getBodyPreviewColumn().setFieldUpdater(textFieldUpdater);
        view.getDateColumn().setFieldUpdater(textFieldUpdater);
    }

    // Buttons
    private void addBackButtonHandler() {
        view.getBackBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                view.getDemandGrid().getSelectionModel().setSelected(
                        (ClientProjectDetail) ((SingleSelectionModel)
                        view.getDemandGrid().getSelectionModel()).getSelectedObject(), false);
                view.setConversationTableVisible(false);
            }
        });
    }

    private void addReadButtonHandler() {
        view.getReadBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.requestStarStatusUpdate(view.getSelectedIdList(), true);
            }
        });
    }

    private void addUnreadButtonHandler() {
        view.getUnreadBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.requestStarStatusUpdate(view.getSelectedIdList(), false);
            }
        });
    }

    private void addStarButtonHandler() {
        view.getStarBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.requestStarStatusUpdate(view.getSelectedIdList(), true);
            }
        });

    }

    private void addUnstarButtonHandler() {
        view.getUnstarBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.requestStarStatusUpdate(view.getSelectedIdList(), false);
            }
        });
    }

    //SelectionHandlers
    private void addDemandTableSelectionHandler() {
        view.getDemandGrid().getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Storage.setCurrentlyLoadedView(Constants.DEMANDS_CLIENT_PROJECT_CONVERSATIONS);
                ClientProjectDetail selected = (ClientProjectDetail) ((SingleSelectionModel)
                        view.getDemandGrid().getSelectionModel()).getSelectedObject();
                if (selected != null) {
                    selected.setRead(true);
                    Storage.setDemandId(selected.getDemandId());
                    view.setDemandTitleLabel(selected.getDemandTitle());
                    view.setConversationTableVisible(true);
                    view.getConversationGrid().getDataCount(eventBus, null);
                }
            }
        });
    }
}