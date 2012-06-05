package com.eprovement.poptavka.client.user;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocalizableMessages;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.mvp4g.client.annotation.Presenter;

import com.mvp4g.client.presenter.BasePresenter;

import com.eprovement.poptavka.client.main.Storage;
import com.eprovement.poptavka.client.user.IUserMenuView.IUserMenuPresenter;
import com.eprovement.poptavka.shared.domain.UserDetail;
import com.eprovement.poptavka.shared.domain.UserDetail.Role;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.domain.message.MessageDetail;
import com.eprovement.poptavka.shared.domain.message.OfferMessageDetail;
import com.eprovement.poptavka.shared.domain.type.ViewType;

/**
 * Main presenter for User account.
 *
 * @author Beho
 *
 */
@Presenter(view = UserView.class)
public class UserPresenter extends BasePresenter<IUserMenuView, UserEventBus> implements IUserMenuPresenter {

    @Override
    public void bind() {
        view.getDemandsButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
//                eventBus.initDemandModule();
            }
        });
        view.getMessagesButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
//                eventBus.initMessagesModule("displayGrid");
            }
        });
        view.getSettingsButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
//                eventBus.initSettings();
            }
        });
        view.getAdministrationButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
//                eventBus.initAdminModule(null);
            }
        });

    }
    private static final Logger LOGGER = Logger.getLogger("UserPresenter");
    private static final LocalizableMessages MSGS = GWT.create(LocalizableMessages.class);

    private UserDetail user = null;
    private String eventMarkedToFire;
    private boolean fireMarkedEvent = false;

    // initial user call after login
    // while developing we can enter this url even without login operation
    public void onAtAccount() {
//        bindView();
        GWT.log("User Presenter at account");
        Cookies.setCookie("user-presenter", "loaded");
//        eventBus.initSearchModule(view.getSearchPanel());
        if (Storage.getUser() == null) {
            eventBus.loadingShow(MSGS.progressGetUserDetail());
            eventBus.getUser();
        } else {
            onSetUser(Storage.getUser());
        }
        view.asWidget().getElement().getStyle().setDisplay(Display.BLOCK);
    }

    // setting of user
    // init of demandLayout tab
    //TODO need revision and clean-up
    public void onSetUser(UserDetail userDetail) {
        Storage.setUser(userDetail);
        //this should be removed and all references replaces by Storage calls
        user = userDetail;

        showDevelUserInfoPopupThatShouldBedeletedAfter();

//        eventBus.setUserLayout();
//        eventBus.setHomeBodyHolderWidget(new Label("TA CO"));
//        eventBus.setHeader(new Label("TA CO HEADER?"));
        eventBus.setMenu(view);
//        eventBus.initDemandModule();

        eventBus.loadingHide();
    }

    public void onFireMarkedEvent() {
        LOGGER.fine("Fire Marked Event");
        if (fireMarkedEvent) {

            LOGGER.fine("    Firing event: " + eventMarkedToFire);
            fireMarkedEvent = false;
            eventBus.dispatch(eventMarkedToFire);
        } else {
            //TODO delete
            LOGGER.fine("There's nothing to fire");
        }
    }

    public void onSetUserInteface(StyleInterface widget) {
        // TODO
        //set interface according to user roles
        if (user.getRoleList().contains(Role.CLIENT)) {
            widget.setRoleInterface(Role.CLIENT);
        }
        if (user.getRoleList().contains(Role.SUPPLIER)) {
            widget.setRoleInterface(Role.SUPPLIER);
        }
    }

//    public void onSetTabWidget(Widget tabBody) {
//        view.setBodyDemand(tabBody);
//    }
//
//    public void onSetTabAdminWidget(Widget tabBody) {
//        view.setBodyAdmin(tabBody);
//    }
//
//    public void onSetTabMessagesWidget(Widget tabBody) {
//        view.setBodyMessages(tabBody);
//    }
//
//    public void onSetTabSettingsWidget(Widget tabBody) {
//        view.setBodySettings(tabBody);
//    }
    /** REQUESTs && RESPONSEs. **/
    public void onRequestClientId(FullDemandDetail newDemand) {
        // TODO refactor this method to call just demand
        if (user.getRoleList().contains(Role.CLIENT)) {
            newDemand.setClientId(user.getClientId());
            eventBus.createDemand(newDemand, user.getClientId());
        } else {
            accessDenied();
        }
    }

    public void onMarkEventToLoad(String historyName) {
        fireMarkedEvent = true;
        this.eventMarkedToFire = historyName;
    }

    public void onClearUserOnUnload() {
        if (user != null) {
            this.user = null;
        } else {
            accessDenied();
        }
//        view.asWidget().getElement().getStyle().setDisplay(Display.NONE);
        eventBus.start();
    }

    public void onRequestClientDemands() {
        eventBus.getClientDemands(user.getUserId(), 0);
    }

    public void onRequestDemandsWithConversationInfo() {
        eventBus.getClientDemandWithConversations(user.getUserId(), user.getClientId());
    }

    public void onRequestPotentialDemands() {
        if (user.getRoleList().contains(Role.SUPPLIER)) {
            eventBus.getPotentialDemands(user.getUserId());
        } else {
            accessDenied();
        }
    }

    public void onRequestClientOfferDemands() {
        if (user.getRoleList().contains(Role.CLIENT)) {
            eventBus.getClientDemandsWithOffers(user.getUserId());
        } else {
            accessDenied();
        }
    }

    /** messaging subsection. **/
    public void onRequestPotentialDemandConversation(long messageId, long userMessageId) {
        if (user.getRoleList().contains(Role.SUPPLIER)) {
            eventBus.getPotentialDemandConversation(messageId, user.getUserId(), userMessageId);
        } else {
            accessDenied();
        }
    }

    public void onBubbleMessageSending(MessageDetail messageToSend, ViewType viewType) {
        messageToSend.setSenderId(user.getUserId());
        eventBus.sendMessageToPotentialDemand(messageToSend, viewType);
    }

    public void onBubbleOfferSending(OfferMessageDetail offerToSend) {
        if (user.getRoleList().contains(Role.SUPPLIER)) {
            offerToSend.setSenderId(user.getUserId());
            offerToSend.setSupplierId(user.getSupplierId());
            eventBus.sendDemandOffer(offerToSend);
        } else {
            accessDenied();
        }
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

    // TODO devel warning poput, in prod, you wont see others options
    private void accessDenied() {
        eventBus.loadingHide();
        PopupPanel p = new PopupPanel(true);
        p.setWidget(new HTML("You cannot access this funcionality.<br />You are logged as user, "
                + "who does not support this"));
        p.center();
        p.show();
    }
}