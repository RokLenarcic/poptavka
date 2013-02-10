package com.eprovement.poptavka.client.common.search;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class SearchModuleView extends Composite implements SearchModulePresenter.SearchModuleInterface {

    /**************************************************************************/
    /* UiBinder                                                               */
    /**************************************************************************/
    private static SearchModulViewUiBinder uiBinder = GWT.create(SearchModulViewUiBinder.class);

    interface SearchModulViewUiBinder extends UiBinder<Widget, SearchModuleView> {
    }
    /**************************************************************************/
    /* Attributes                                                             */
    /**************************************************************************/
    /** UiBinder attributes. **/
    @UiField Button searchBtn, advSearchBtn;
    @UiField TextBox searchContent;
    @UiField PopupPanel popupPanel;
    @UiField AdvanceSearchContentView advanceSearchContentView;

    /**************************************************************************/
    /* Initialization                                                         */
    /**************************************************************************/
    @Override
    public void createView() {
        initWidget(uiBinder.createAndBindUi(this));

        popupPanel.setAutoHideEnabled(true);
        popupPanel.setAnimationEnabled(true);
        //Aby sa nam nezobrazoval taky ramcek (popup bez widgetu) pri starte modulu
        //Musi to byt takto? Neda sa to urobit krajsie? (len hide nefunguje)
        popupPanel.show();
        popupPanel.hide();
    }

    /**************************************************************************/
    /* Setters                                                                */
    /**************************************************************************/
    @Override
    public void setAttributeSelectorWidget(IsWidget attributeSearchViewWidget) {
        //If attributeSearchViewWidget is not null -> current view searching is available
        //Therefore set widget to popup
        advanceSearchContentView.getAttributeSelectorPanel().setWidget(attributeSearchViewWidget);

        advanceSearchContentView.addCustomItemToSearchWhatBox(attributeSearchViewWidget != null);

    }

    /**************************************************************************/
    /* Getters                                                                */
    /**************************************************************************/
    // Search Bar items
    @Override
    public TextBox getSearchContent() {
        return searchContent;
    }

    @Override
    public Button getSearchBtn() {
        return searchBtn;
    }

    @Override
    public Button getAdvSearchBtn() {
        return advSearchBtn;
    }

    // Layouts & Panels
    @Override
    public PopupPanel getPopupPanel() {
        return popupPanel;
    }

    @Override
    public AdvanceSearchContentView getAdvanceSearchContentView() {
        return advanceSearchContentView;
    }

    @Override
    public Widget getWidgetView() {
        return this;
    }

    /**************************************************************************/
    /* UiHandlers                                                             */
    /**************************************************************************/
    /**
     * Display popup when advance button popup is clicked.
     */
    @UiHandler("advSearchBtn")
    public void advSearchBtnClickHandler(ClickEvent event) {
        int left = searchContent.getElement().getAbsoluteLeft() - 553;
        int top = searchContent.getElement().getAbsoluteTop() + 75;
        popupPanel.setSize("1030px", "500px");
        popupPanel.setPopupPosition(left, top);
        popupPanel.show();
    }
}