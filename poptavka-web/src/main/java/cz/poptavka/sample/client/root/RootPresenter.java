package cz.poptavka.sample.client.root;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
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

import cz.poptavka.sample.client.main.Storage;
import cz.poptavka.sample.client.main.common.LoadingPopup;
import cz.poptavka.sample.client.main.common.category.CategorySelectorPresenter;
import cz.poptavka.sample.client.main.common.locality.LocalitySelectorPresenter;
import cz.poptavka.sample.client.resources.StyleResource;
import cz.poptavka.sample.client.root.interfaces.IRootView;
import cz.poptavka.sample.client.root.interfaces.IRootView.IRootPresenter;
import cz.poptavka.sample.shared.domain.UserDetail;
import cz.poptavka.sample.shared.domain.UserDetail.Role;

@Presenter(view = RootView.class)
public class RootPresenter extends BasePresenter<IRootView, RootEventBus>
        implements IRootPresenter {

    private PopupPanel popup = null;
    private CategorySelectorPresenter categorySelector = null;
    private LocalitySelectorPresenter localitySelector = null;
    private UserDetail user = null;

    public void onSetMenu(IsWidget menu) {
        GWT.log("Menu widget set");
        view.setMenu(menu);
    }

    public void onSetSearchBar(IsWidget searchBar) {
        GWT.log("Search bar widget set");
        view.setSearchBar(searchBar);
    }

    public void onSetBody(IsWidget body) {
        GWT.log("Body widget set");
        view.setBody(body);
    }

    public void onSetFooter(IsWidget footer) {
        GWT.log("Footer widget set");
        view.setFooter(footer);
    }

    public void onSetHeader(IsWidget header) {
        GWT.log("Header widget set");
        view.setHeader(header);
    }

    public void onStart() {
        GWT.log("Root module loaded");
        eventBus.initHomeWelcomeModule(null);
        eventBus.initSearchModule(view.getSearchBar());
    }

    public void onNotFound() {
        eventBus.start();
        view.setBody(new Label("Page not found"));
    }

    public void onSetHomeBodyHolderWidget(IsWidget content) {
        view.setBody(content);
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
    public void onInitCategoryWidget(SimplePanel holderPanel) {
        if (categorySelector != null) {
            eventBus.removeHandler(categorySelector);
        }
        categorySelector = eventBus.addHandler(CategorySelectorPresenter.class);
        categorySelector.initCategoryWidget(holderPanel);
    }

    public void onInitLocalityWidget(SimplePanel holderPanel) {
        if (localitySelector != null) {
            eventBus.removeHandler(localitySelector);
        }
        localitySelector = eventBus.addHandler(LocalitySelectorPresenter.class);
        localitySelector.initLocalityWidget(holderPanel);
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

    public void onDisplayMenu() {
        eventBus.setPublicLayout();
        view.getMenu().getElement().getStyle().setDisplay(Display.BLOCK);
        view.getSearchBar().getElement().getStyle().setDisplay(Display.BLOCK);
    }

    public void onAtAccount() {
        GWT.log("Root Presenter AtAccount");
        Cookies.setCookie("user-presenter", "loaded");
        if (Storage.getUser() == null) {
            eventBus.loadingShow(Storage.MSGS.progressGetUserDetail());
            eventBus.getUser();
        } else {
            onSetUser(Storage.getUser());
        }
//        eventBus.setUserLayout();
        eventBus.setUserMenu();
        eventBus.initDemandModule(null, "welcome");
//        eventBus.initMessagesModule("displayGrid");
//        eventBus.initAdminModule(null);
    }

    /* For logging */
    public void onSetUser(UserDetail userDetail) {
        Storage.setUser(userDetail);
        //this should be removed and all references replaces by Storage calls
        user = userDetail;

        showDevelUserInfoPopupThatShouldBedeletedAfter();

//        eventBus.setUserLayout();
//        eventBus.setHomeBodyHolderWidget(new Label("TA CO"));
//        eventBus.setHeader(new Label("TA CO HEADER?"));
//        eventBus.setMenu(view);
//        eventBus.initDemandModule();

        eventBus.loadingHide();
    }

    // TODO delete for production
    private void showDevelUserInfoPopupThatShouldBedeletedAfter() {
        final DialogBox userInfoPanel = new DialogBox(false, false);
        userInfoPanel.setText("User Info Box");
        userInfoPanel.setWidth("200px");
        String br = "<br />";
        StringBuilder sb = new StringBuilder("<b>User Info:</b>" + br);
        user = Storage.getUser();
        sb.append("ID: " + user.getUserId() + br);

        sb.append("<i>-- user roles --</i>" + br);
        if (user.getRoleList().contains(Role.CLIENT)) {
            sb.append("<b><i>CLIENT</i></b>" + br);
            sb.append("ClientID: " + user.getClientId() + br);
            sb.append("Demand Count: " + user.getDemandsId().size() + br);
            sb.append("Demands Messages: " + "n/a" + " / " + "n/a" + br);
            sb.append("Demands Offers: " + "n/a" + " / " + "n/a" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getRoleList().contains(Role.SUPPLIER)) {
            sb.append("<b><i>SUPPLIER</i></b>" + br);
            sb.append("SupplierID: " + user.getSupplierId() + br);
            sb.append("Potentional Demands: " + "n/a" + " / " + "n/a" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getRoleList().contains(Role.PARTNER)) {
            sb.append("<b><i>PARTNER</i></b>" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getRoleList().contains(Role.OPERATOR)) {
            sb.append("<b><i>OPERATOR</i></b>" + br);
            sb.append("<i>-- -- -- --</i>" + br);
        }
        if (user.getRoleList().contains(Role.ADMIN)) {
            sb.append("<b><i>ADMIN</i></b>" + br);
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
}