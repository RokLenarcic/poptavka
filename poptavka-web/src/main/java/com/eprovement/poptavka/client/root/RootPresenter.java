package com.eprovement.poptavka.client.root;

import com.eprovement.poptavka.client.common.LoadingPopup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import com.eprovement.poptavka.client.common.session.Storage;
import com.eprovement.poptavka.client.common.address.AddressSelectorPresenter;
import com.eprovement.poptavka.client.common.category.CategorySelectorPresenter;
import com.eprovement.poptavka.client.common.locality.LocalitySelectorPresenter;
import com.eprovement.poptavka.client.resources.StyleResource;
import com.eprovement.poptavka.client.root.interfaces.IRootView;
import com.eprovement.poptavka.client.root.interfaces.IRootView.IRootPresenter;
import com.eprovement.poptavka.client.user.widget.DetailsWrapperPresenter;
import com.eprovement.poptavka.shared.domain.BusinessUserDetail;
import com.eprovement.poptavka.shared.domain.BusinessUserDetail.BusinessRole;
import com.eprovement.poptavka.shared.domain.UserDetail;
import com.eprovement.poptavka.shared.domain.adminModule.AccessRoleDetail;

@Presenter(view = RootView.class)
public class RootPresenter extends BasePresenter<IRootView, RootEventBus>
        implements IRootPresenter {

    private PopupPanel popup = null;
    private CategorySelectorPresenter categorySelector = null;
    private LocalitySelectorPresenter localitySelector = null;
    private AddressSelectorPresenter addressSelector = null;

    /**************************************************************************/
    /* Layout events.                                                         */
    /**************************************************************************/
    public void onSetHeader(IsWidget header) {
        GWT.log("Header widget set");
        view.setHeader(header);
    }

    public void onSetMenu(IsWidget menu) {
        GWT.log("Menu widget set");
        view.setMenu(menu);
    }

    public void onSetSearchBar(IsWidget searchBar) {
        GWT.log("Search bar widget set");
        view.setSearchBar(searchBar);
    }

    public void onSetUpSearchBar(IsWidget searchView, boolean cat, boolean loc, boolean advBtn) {
        GWT.log("Search bar widget set up");
//        view.setUpSearchBar(searchView, cat, loc, advBtn);
    }

    public void onSetBody(IsWidget body) {
        GWT.log("Body widget set");
        view.setBody(body);
    }

    public void onSetFooter(IsWidget footer) {
        GWT.log("Footer widget set");
        view.setFooter(footer);
    }

    /**************************************************************************/
    /* General Module events                                                  */
    /**************************************************************************/
    /**
     * When your application starts, you may want to automatically fire an event
     * so that actions needed at first can occur.
     */
    public void onStart() {
        GWT.log("Root presenter loaded");
        eventBus.atHome();
        eventBus.goToSearchModule();
        eventBus.goToHomeWelcomeModule(null);
    }

    /**
     * This event will be called in case an error occurs while loading the
     * ChildModule code.
     *
     * @param reason - An object may be fired for the event used in case of
     * error but the type of this object must be compatible with
     * java.lang.Throwable. In this case, the error returned by the
     * RunAsync object is passed to the event.
     */
    public void onErrorOnLoad(Throwable reason) {
        // TODO praso doplnit. Napriklad poslat email s notifikaciou o chybe
    }

    /**
     * This event will be called before starting to load the ChildModule code.
     * You can for example decide to display a wait popup.
     */
    public void onBeforeLoad() {
        view.setWaitVisible(true);
    }

    /**
     * This event will be called after the code is done loading.
     * You can for example decide to hide a wait popup.
     */
    public void onAfterLoad() {
        view.setWaitVisible(false);
    }

    /**************************************************************************/
    /* Navigation events                                                      */
    /**************************************************************************/
    public void onAtAccount() {
        GWT.log("User has logged in and his user data are about to be retrieved");
        //Po prihlaseni je v Storage ulozeny len UserDetail prihlaseneho uzivatela.
        //Pre dalsiu pracu budeme potrebovat BusinessUserDetail, takze ho ziskaj.
        if (Storage.getUser() instanceof UserDetail) {
            // TODO praso - zakomentovane kvoli refaktorinu na standarny wait loading
            // cez onBefore, onAfter eventy v Root module. Potom to bude treba znovu
            // upravit aby sa volal cakacia smycka aj pri logovani.
            eventBus.loadingShow(Storage.MSGS.progressGetUserDetail());
            eventBus.getUser(Storage.getUser().getUserId());
        }
    }

    public void onAtHome() {
        // TODO praso - logout request was moved to loginpopuppresenter. Delete these commented lines
//        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
//                URL.encode("/poptavka/j_spring_security_logout"));
//        builder.setHeader("Content-type", "application/x-www-form-urlencoded");
//        //remove user from session management to force user input login information
//        Storage.setUser(null);
//        try {
//            Request request = builder.sendRequest(null,
//                    new RequestCallback() {
//
//                        public void onError(Request request,
//                                Throwable exception) {
//                        }
//
//                        public void onResponseReceived(Request request,
//                                Response response) {
//                        }
//                    });
//        } catch (RequestException e) {
//            // Couldn't connect to server
//            GWT.log("exception during login");
//        }
//        GWT.log("User has logged out");
    }

    /**************************************************************************/
    /* Business events handled by presenter                                   */
    /**************************************************************************/
    public void onNotFound() {
        eventBus.start();
        view.setBody(new Label("Page not found"));
    }

    public void onLoadingShow(String loadingMessage) {
        if (!(popup == null)) {
            LoadingPopup popupContent = (LoadingPopup) popup.getWidget();
            popupContent.setMessage(loadingMessage);
        } else {
            createLoadingPopup(loadingMessage);
        }
    }

    public void onLoadingHide() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    public void onInitDemandAdvForm(SimplePanel holderWidget) {
    }

    public void onLoadingShowWithAnchor(String loadingMessage, Widget anchor) {
        if (popup != null) {
            LoadingPopup popupContent = (LoadingPopup) popup.getWidget();
            popupContent.setMessage(loadingMessage);
        } else {
            createLoadingPopup(loadingMessage, anchor);
        }
    }

    /** multiple presenters handling methods **/
    public void onInitCategoryWidget(SimplePanel holderPanel, int checkboxes) {
        if (categorySelector != null) {
            eventBus.removeHandler(categorySelector);
        }
        categorySelector = eventBus.addHandler(CategorySelectorPresenter.class);
        categorySelector.initCategoryWidget(holderPanel, checkboxes);
    }

    public void onInitLocalityWidget(SimplePanel holderPanel) {
        if (localitySelector != null) {
            eventBus.removeHandler(localitySelector);
        }
        localitySelector = eventBus.addHandler(LocalitySelectorPresenter.class);
        localitySelector.initLocalityWidget(holderPanel);
    }

    public void onInitAddressWidget(SimplePanel holderPanel) {
        if (addressSelector != null) {
            eventBus.removeHandler(localitySelector);
        }
        addressSelector = eventBus.addHandler(AddressSelectorPresenter.class);
        addressSelector.initAddressWidget(holderPanel);
    }

    public void onInitDemandBasicForm(SimplePanel holderWidget) {
    }
    private static final int OFFSET_X = 60;
    private static final int OFFSET_Y = 35;

    private void createLoadingPopup(String loadingMessage, Widget anchor) {
        popup = new PopupPanel(false, false);
        popup.setStylePrimaryName(StyleResource.INSTANCE.common().loadingPopup());
        popup.setWidget(new LoadingPopup(loadingMessage));
        int top = anchor.getAbsoluteTop() + (anchor.getOffsetHeight() / 2);
        int left = anchor.getAbsoluteLeft() + (anchor.getOffsetWidth() / 2)
                - OFFSET_X;
        popup.showRelativeTo(anchor);
        GWT.log("AbsoluteLeft: " + anchor.getAbsoluteLeft() + " OffsetWidth: "
                + (anchor.getOffsetWidth()));
        GWT.log("AbsoluteTop: " + anchor.getAbsoluteTop() + " Offsetheight: "
                + (anchor.getOffsetHeight()));

        GWT.log("L: " + left + " T: " + top);

        popup.show();
    }

    private void createLoadingPopup(String loadingMessage) {
        popup = new PopupPanel(false, false);
        popup.setStylePrimaryName(StyleResource.INSTANCE.common().loadingPopup());
        popup.setWidget(new LoadingPopup(loadingMessage));
        popup.setPopupPosition((Window.getClientWidth() / 2) - OFFSET_X,
                (Window.getClientHeight() / 2) - OFFSET_Y);
        popup.show();
    }

    // TODO delete for production
    private void showDevelUserInfoPopupThatShouldBedeletedAfter() {
        final DialogBox userInfoPanel = new DialogBox(false, false);
        userInfoPanel.setText("User Info Box");
        userInfoPanel.setWidth("200px");
        String br = "<br />";
        StringBuilder sb = new StringBuilder("<b>User Info:</b>" + br);
        BusinessUserDetail user = (BusinessUserDetail) Storage.getUser();
        sb.append("ID: " + user.getUserId() + br);

        sb.append("<i>-- user roles --</i>" + br);
        if (user.getBusinessRoles().contains(BusinessRole.CLIENT)) {
            sb.append("<b><i>BusinessRole: CLIENT</i></b>" + br);
            sb.append("ClientID: " + user.getClientId() + br);
            sb.append("Demands Messages: " + "n/a" + " / " + "n/a" + br);
            sb.append("Demands Offers: " + "n/a" + " / " + "n/a" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getBusinessRoles().contains(BusinessRole.SUPPLIER)) {
            sb.append("<b><i>BusinessRole: SUPPLIER</i></b>" + br);
            sb.append("SupplierID: " + user.getSupplierId() + br);
            sb.append("Potentional Demands: " + "n/a" + " / " + "n/a" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getBusinessRoles().contains(BusinessRole.PARTNER)) {
            sb.append("<b><i>BusinessRole: PARTNER</i></b>" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getAccessRoles().contains(new AccessRoleDetail("user"))) {
            sb.append("<b><i>AccessRole: USER</i></b>" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getAccessRoles().contains(new AccessRoleDetail("admin"))) {
            sb.append("<b><i>AccessRole: ADMIN</i></b>" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        sb.append("Messages: " + "n/a" + " / " + "n/a" + br);

        HTML content = new HTML(sb.toString());
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                userInfoPanel.hide();
            }
        });
        FlowPanel m = new FlowPanel();
        m.add(content);
        m.add(closeButton);
        userInfoPanel.add(m);
        userInfoPanel.setPopupPosition(Window.getClientWidth() - 200, 20);
        userInfoPanel.show();
    }

    /**
     * Request RootEventBus to create DetailWrapperPresenter.
     */
    public void onRequestDetailWrapperPresenter() {
        DetailsWrapperPresenter detailSection = eventBus.addHandler(DetailsWrapperPresenter.class);
        eventBus.responseDetailWrapperPresenter(detailSection);
    }
}
