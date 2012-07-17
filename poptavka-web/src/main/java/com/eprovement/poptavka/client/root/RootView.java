package com.eprovement.poptavka.client.root;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.eprovement.poptavka.client.common.search.SearchModuleView;
import com.eprovement.poptavka.client.resources.StyleResource;
import com.eprovement.poptavka.client.root.interfaces.IRootView;
import com.eprovement.poptavka.client.root.interfaces.IRootView.IRootPresenter;

public class RootView extends ReverseCompositeView<IRootPresenter> implements
        IRootView {

    private static RootViewUiBinder uiBinder = GWT.create(RootViewUiBinder.class);
    @UiField
    SimplePanel header, body, menu, searchBar, footer;
    private PopupPanel wait = new PopupPanel();

    interface RootViewUiBinder extends UiBinder<Widget, RootView> {
    }

    public RootView() {

        // TODO praso - otestovat na online poptavke ci sa zobrazuje tato loading show/hide hlaska
        wait.add(new Label("Wait until requested module code is downloaded from server."));
        initWidget(uiBinder.createAndBindUi(this));
        /* Tato metoda, zaisti, ze sa nacíta CSS styl. Bez nej by sa styl nahral az pri prepnuti do
         * dalsieho modulu.
         */
        StyleResource.INSTANCE.layout().ensureInjected();

    }

    @Override
    public void setMenu(IsWidget menu) {
        GWT.log("Menu widget view set");
        this.menu.setWidget(menu);

    }

    @Override
    public void setSearchBar(IsWidget searchBar) {
        GWT.log("Search bar widget view set");
        this.searchBar.setWidget(searchBar);

    }

    @Override
    public void setBody(IsWidget body) {
        GWT.log("Body widget view set");
        this.body.setWidget(body);

    }

    @Override
    public void setFooter(IsWidget footer) {
        GWT.log("Footer widget view set");
        this.footer.setWidget(footer);

    }

    @Override
    public void setHeader(IsWidget header) {
        GWT.log("Header widget view set");
        this.header.setWidget(header);

    }

    @Override
    public void setWaitVisible(boolean visible) {
        if (visible) {
            GWT.log("Show loading popup");
            wait.setPopupPosition(body.getAbsoluteLeft(), body.getAbsoluteTop());
            wait.setPixelSize(body.getOffsetWidth(), body.getOffsetHeight());
            wait.show();
        } else {
            GWT.log("Hide loading popup");
            wait.hide();
        }
    }

    /**
     * Sets given advance search view to popup window and set search bar enables
     * (categories, localities, advance button).
     * @param loadedWidget
     */
    @Override
    public void setUpSearchBar(IsWidget searchView, boolean cat, boolean loc, boolean advBtn) {
        SearchModuleView searchBarView = (SearchModuleView) searchBar.getWidget();
        searchBarView.setAttributeSelector(searchView);
        searchBarView.setSearchBarEnables(cat, loc, advBtn);
    }
}
