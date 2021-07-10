/*
 * Copyright (C), eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.client.root;

import com.eprovement.poptavka.client.catLocSelector.CatLocSelectorModule;
import com.eprovement.poptavka.client.catLocSelector.others.CatLocSelectorBuilder;
import com.eprovement.poptavka.client.addressSelector.AddressSelectorModule;
import com.eprovement.poptavka.client.common.LoadingPopupPresenter;
import com.eprovement.poptavka.client.common.actionBox.ActionBoxModule;
import com.eprovement.poptavka.client.login.LoginModule;
import com.eprovement.poptavka.client.search.SearchModule;
import com.eprovement.poptavka.client.common.userRegistration.UserRegistrationModule;
import com.eprovement.poptavka.client.infoWidgets.InfoWidgetsModule;
import com.eprovement.poptavka.client.home.createDemand.DemandCreationModule;
import com.eprovement.poptavka.client.home.createSupplier.SupplierCreationModule;
import com.eprovement.poptavka.client.homeWelcome.HomeWelcomeModule;
import com.eprovement.poptavka.client.homedemands.HomeDemandsModule;
import com.eprovement.poptavka.client.homesuppliers.HomeSuppliersModule;
import com.eprovement.poptavka.client.detail.DetailModule;
import com.eprovement.poptavka.client.detail.DetailModuleBuilder;
import com.eprovement.poptavka.client.home.createDemand.interfaces.IDemandCreationModule;
import com.eprovement.poptavka.client.home.createSupplier.interfaces.ISupplierCreationModule;
import com.eprovement.poptavka.client.root.footer.FooterPresenter;
import com.eprovement.poptavka.client.root.header.HeaderPresenter;
import com.eprovement.poptavka.client.root.header.menu.MenuPresenter;
import com.eprovement.poptavka.client.root.interfaces.HandleResizeEvent;
import com.eprovement.poptavka.client.root.interfaces.IRootModule;
import com.eprovement.poptavka.client.root.toolbar.ToolbarPresenter;
import com.eprovement.poptavka.client.root.unsubscribe.UnsubscribePresenter;
import com.eprovement.poptavka.client.serviceSelector.ServiceSelectorModule;
import com.eprovement.poptavka.client.user.admin.AdminModule;
import com.eprovement.poptavka.client.user.admin.interfaces.IAdminModule;
import com.eprovement.poptavka.client.user.clientdemands.ClientDemandsModule;
import com.eprovement.poptavka.client.user.messages.MessagesModule;
import com.eprovement.poptavka.client.user.settings.SettingsModule;
import com.eprovement.poptavka.client.user.supplierdemands.SupplierDemandsModule;
import com.eprovement.poptavka.client.user.widget.creditAnnouncer.CreditStatusAnnouncerPresenter;
import com.eprovement.poptavka.client.user.widget.grid.UniversalAsyncGrid;
import com.eprovement.poptavka.shared.domain.AddressDetail;
import com.eprovement.poptavka.shared.domain.BusinessUserDetail;
import com.eprovement.poptavka.shared.selectors.catLocSelector.ICatLocDetail;
import com.eprovement.poptavka.shared.domain.ServiceDetail;
import com.eprovement.poptavka.shared.domain.message.UnreadMessagesDetail;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.annotation.Debug;
import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.InitHistory;
import com.mvp4g.client.annotation.NotFoundHistory;
import com.mvp4g.client.annotation.Start;
import com.mvp4g.client.annotation.module.AfterLoadChildModule;
import com.mvp4g.client.annotation.module.BeforeLoadChildModule;
import com.mvp4g.client.annotation.module.ChildModule;
import com.mvp4g.client.annotation.module.ChildModules;
import com.mvp4g.client.annotation.module.DisplayChildModuleView;
import com.mvp4g.client.annotation.module.LoadChildModuleError;
import com.mvp4g.client.event.EventBusWithLookup;
import java.util.List;

/**
 * RootEventBus servers all events for foot module. This is the starting
 * EventBus that handled the very first event in the app.
 *
 * Root Module countains all child modules, and all initial presenters like
 * Header, HomeMenu, UserMenu, SearchPanel, HomeBody, UserBody, Footer.
 *
 * Specification:
 * Wireframe: http://www.webgres.cz/axure/
 *
 * @author Beho, Ivan Vlcek, Martin Slavkovsky
 */
@Events(startPresenter = RootPresenter.class, historyOnStart = true)
@Debug(logLevel = Debug.LogLevel.DETAILED)
@ChildModules({
    @ChildModule(moduleClass = HomeWelcomeModule.class, async = false, autoDisplay = true),
    @ChildModule(moduleClass = SearchModule.class, async = false, autoDisplay = true),
    @ChildModule(moduleClass = DemandCreationModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = SupplierCreationModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = HomeDemandsModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = HomeSuppliersModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = ClientDemandsModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = SupplierDemandsModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = MessagesModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = SettingsModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = InfoWidgetsModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = LoginModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = AdminModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = CatLocSelectorModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = UserRegistrationModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = ServiceSelectorModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = AddressSelectorModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = ActionBoxModule.class, async = true, autoDisplay = false),
    @ChildModule(moduleClass = DetailModule.class, async = true, autoDisplay = false) })
public interface RootEventBus extends EventBusWithLookup,
    ISupplierCreationModule.Gateway,
    IDemandCreationModule.Gateway {

    /**
     * When your application starts, you may want to automatically fire an event
     * so that actions needed at first can occur.
     */
    @Start
    @InitHistory
    @Event(handlers = {RootPresenter.class, HeaderPresenter.class, ToolbarPresenter.class, MenuPresenter.class })
    void start();

    /**************************************************************************/
    /* Layout events.                                                         */
    /**************************************************************************/
    @Event(handlers = RootPresenter.class)
    void setHeader(IsWidget header);

    /*
     * <b><i>Note:</i></b>
     * AutodisplayChildModules function automatically sets all listed child
     * modules' views to body using setBody method.
     */
    @DisplayChildModuleView(HomeWelcomeModule.class)
    @Event(handlers = RootPresenter.class)
    void setBody(IsWidget body);

    @Event(handlers = HeaderPresenter.class)
    void setMenu(IsWidget menu);

    @DisplayChildModuleView(SearchModule.class)
    @Event(handlers = HeaderPresenter.class)
    void setSearchBar(IsWidget searchBar);

    @Event(handlers = RootPresenter.class)
    void openMenu();

    @Event(handlers = RootPresenter.class)
    void closeMenu();

    @Event(handlers = ToolbarPresenter.class)
    void openDetail();

    @Event(handlers = ToolbarPresenter.class)
    void closeSubMenu();

    @Event(handlers = RootPresenter.class)
    void setToolbar(IsWidget toolbar);

    @Event(handlers = ToolbarPresenter.class)
    void setToolbarContent(String title, Widget toolbarContent);

    @Event(handlers = ToolbarPresenter.class)
    void toolbarRefresh();

    @Event(handlers = FooterPresenter.class)
    void setFooter(SimplePanel footerPanel);

    @Event(handlers = CreditStatusAnnouncerPresenter.class)
    void initCreditAnnouncer(SimplePanel creditAnnouncerPanel);

    @Event(broadcastTo = HandleResizeEvent.class, passive = true)
    void resize(int actualWidth);

    /**************************************************************************/
    /* Footer section                                                         */
    /**************************************************************************/
    @Event(handlers = FooterPresenter.class)
    void displayAboutUs();

    @Event(handlers = FooterPresenter.class)
    void displayFaq();

    @Event(handlers = FooterPresenter.class)
    void displayPrivacyPolicy();

    @Event(handlers = FooterPresenter.class)
    void displayTermsAndConditions();

    /**************************************************************************/
    /* History events.                                                        */
    /**************************************************************************/
    @Event(historyConverter = RootHistoryConverter.class, name = IRootModule.CUSTOM_TOKEN)
    void createCustomToken(String param);

    @Event(historyConverter = RootHistoryConverter.class, name = IRootModule.UNSUBSCRIBE_TOKEN)
    void createUnsubscribeToken();

    @Event(historyConverter = RootHistoryConverter.class, name = IRootModule.PAYMENT_TOKEN)
    void createPaymentToken();

    @Event(historyConverter = RootHistoryConverter.class, name = IRootModule.LOGIN_TOKEN)
    void createLoginToken();

    /**************************************************************************/
    /* Unsubscribe                                                            */
    /**************************************************************************/
    @Event(handlers = UnsubscribePresenter.class)
    void initUnsubscribe(String password);

    @Event(handlers = RootHandler.class)
    void unsubscribeUser(String password);

    @Event(handlers = UnsubscribePresenter.class)
    void responseUnsubscribe(Boolean result);

    /**************************************************************************/
    /* Navigation events - Home menu control section                          */
    /**************************************************************************/
    @Event(forwardToModules = HomeWelcomeModule.class)
    void goToHomeWelcomeModule();

    @Event(forwardToModules = HomeDemandsModule.class)
    void goToHomeDemandsModule(SearchModuleDataHolder filter);

    @Event(forwardToModules = HomeDemandsModule.class)
    void goToHomeDemandsModuleFromWelcome(int categoryIdx, ICatLocDetail category);

    @Event(forwardToModules = HomeSuppliersModule.class)
    void goToHomeSuppliersModule(SearchModuleDataHolder filter);

    @Override
    @Event(forwardToModules = SupplierCreationModule.class)
    void goToCreateSupplierModule();

    @Override
    @Event(forwardToModules = DemandCreationModule.class)
    void goToCreateDemandModule();

    /**************************************************************************/
    /* Navigation events - User menu control section                          */
    /**************************************************************************/
    @Event(forwardToModules = ClientDemandsModule.class)
    void goToClientDemandsModule(SearchModuleDataHolder filter, int loadWidget);

    @Event(forwardToModules = SupplierDemandsModule.class)
    void goToSupplierDemandsModule(SearchModuleDataHolder filter, int loadWidget);

    /**
     * @param filter - provided by search module
     */
    @Event(forwardToModules = MessagesModule.class)
    void goToMessagesModule(SearchModuleDataHolder filter, int loadWidget);

    @Event(forwardToModules = SettingsModule.class)
    void goToSettingsModule();

    @Event(forwardToModules = AdminModule.class)
    void goToAdminModule(SearchModuleDataHolder filter, IAdminModule.AdminWidget loadWidget);

    /**************************************************************************/
    /* Info widgets module                                                    */
    /**************************************************************************/
    /**
     * Forward error handling to Error Module that will display proper messages to user.
     *
     * @param errorResponseCode - HTTP error response code if available, otherwise use value 0
     * @param errorId - errorId if available, otherwise use null
     */
    @Event(forwardToModules = InfoWidgetsModule.class)
    void displayError(int errorResponseCode, String errorId);

    /**
     * Contact us popup will appear. Parameters will be forwared to method fillContactUsValues().
     *
     * @param subject - predefined subject i.e. report issue that was invoked by user from Error Module
     * @param errorId - the error ID what was genereated for reported issue
     */
    @Event(forwardToModules = InfoWidgetsModule.class)
    void sendUsEmail(int subject, String errorId);

    @Event(forwardToModules = InfoWidgetsModule.class)
    void showThankYouPopup(SafeHtml message, Timer timer);

    @Event(forwardToModules = InfoWidgetsModule.class)
    void showAlertPopup(String message);

    @Event(forwardToModules = InfoWidgetsModule.class)
    void showTermsAndConditionsPopup();

    /**************************************************************************/
    /* Navigation events - Other control sections                             */
    /**************************************************************************/
    /**
     * Logout usera prechadza vzdy cez tuto metodu. Nastavuje sa menu, hlavicka
     * a defaultny modul po prihlaseni.
     */
    @Event(handlers = {HeaderPresenter.class, RootPresenter.class, MenuPresenter.class })
    void atHome();

    /**
     * Login usera prechadza vzdy cez tuto metodu. Nastavuje sa menu, hlavicka,
     * cookies a defaultny modul po odhlaseni
     */
    @Event(handlers = {HeaderPresenter.class, RootPresenter.class, MenuPresenter.class })
    void atAccount();

    @Event(handlers = HeaderPresenter.class)
    void setUpdatedUnreadMessagesCount(UnreadMessagesDetail numberOfMessages);

    /**
     * This event will be called in case an error occurs while loading the
     * ChildModule code.
     *
     * @param reason - An object may be fired for the event used in case of
     * error but the type of this object must be compatible with
     * java.lang.Throwable. In this case, the error returned by the
     * RunAsync object is passed to the event.
     */
    @LoadChildModuleError
    @Event(handlers = RootPresenter.class)
    void errorOnLoad(Throwable reason);

    /**
     * This event will be called before starting to load the ChildModule code.
     * You can for example decide to display a wait popup.
     *
     * Vytvara to vela volani, pretoze mame vela modulov. Trebarz len pri loadovani
     * Welcomu sa to vola 3x - Start app, HomeWelcome, SearchModule,
     * Skusit zakomentovanat, mozno to nebudeme potrebovat kvoli zrychleniu aplikacie
     */
    @BeforeLoadChildModule
    @Event(handlers = RootPresenter.class)
    void beforeLoad();

    /**
     * This event will be called after the code is done loading.
     * You can for example decide to hide a wait popup.
     *
     * Zatial zakomentovane. Mozno to nebudeme potrebovat kvoli zrychleniu aplikacie
     */
    @AfterLoadChildModule
    @Event(handlers = RootPresenter.class)
    void afterLoad();

    /**************************************************************************/
    /* Parent events - no events for RootModule                               */
    /**************************************************************************/
    /**************************************************************************/
    /* Business events handled by Presenters.                                 */
    /**************************************************************************/
    /**
     * When URL token can't be recognized this method gets invoked.
     */
    @NotFoundHistory
    @Event(handlers = RootPresenter.class)
    void notFound();

    /**************************************************************************/
    /* Business events handled by Login module                                */
    /**************************************************************************/
    @Event(forwardToModules = LoginModule.class)
    void login(int widgetToLoad);

    /**
     * This method populates Storage i.e. our custom GWT session object with UserDetail.
     * A secured RPC service is invoked so this method can be called only if user is logged in and he opened our
     * website in new browser tab, which obviously starts the whole app from the begining. If user is not logged in
     * the RPC service will cause the initiation of loginPopupView via SecuredAsyncCallback.
     */
    @Event(forwardToModules = LoginModule.class)
    void loginFromSession(int widgetToLoad);

    @Event(forwardToModules = LoginModule.class)
    void autoLogin(String email, String password, int widgetToLoad);

    @Event(forwardToModules = LoginModule.class)
    void logout(int widgetToLoad);

    /**************************************************************************/
    /* LOADING.                                                               */
    /**************************************************************************/
    @Event(handlers = LoadingPopupPresenter.class)
    void loadingShow(String loadingMessage);

    @Event(handlers = LoadingPopupPresenter.class)
    void loadingHide();

    /**************************************************************************/
    /* SEARCH MODULE.                                                         */
    /**************************************************************************/
    @Event(forwardToModules = SearchModule.class)
    void goToSearchModule();

    @Event(forwardToModules = SearchModule.class)
    void goToAdvancedSearch();

    @Event(forwardToModules = SearchModule.class)
    void resetSearchBar(Widget newAttributeSearchWidget);

    /**************************************************************************/
    /* CATEGORY SELECTOR MODULE.                                              */
    /**************************************************************************/
    /** CategorySelection section. **/
    @Event(forwardToModules = CatLocSelectorModule.class)
    void initCatLocSelector(SimplePanel embedToWidget, CatLocSelectorBuilder builder);

    @Event(forwardToModules = CatLocSelectorModule.class)
    void fillCatLocs(List<ICatLocDetail> selectedCategories, int instanceId);

    @Event(forwardToModules = CatLocSelectorModule.class)
    void setCatLocs(List<ICatLocDetail> categories, int instanceId);

    @Event(forwardToModules = CatLocSelectorModule.class)
    void requestHierarchy(int selectorType, ICatLocDetail category, int instanceId);

    @Event(forwardToModules = CatLocSelectorModule.class)
    void redrawCatLocSelectorGrid(int instanceId);

    /**************************************************************************/
    /* ADDRESS SELECTOR MODULE.                                               */
    /**************************************************************************/
    @Event(forwardToModules = AddressSelectorModule.class)
    void initAddressSelector(SimplePanel embedToWidget);

    @Event(forwardToModules = AddressSelectorModule.class)
    void fillAddresses(List<AddressDetail> addresses);

    @Event(forwardToModules = AddressSelectorModule.class)
    void setAddresses(List<AddressDetail> addresses);

    /**************************************************************************/
    /* SERVICE SELECTOR MODULE.                                               */
    /**************************************************************************/
    @Event(forwardToModules = ServiceSelectorModule.class)
    void initServicesWidget(SimplePanel embedToWidget);

    @Event(forwardToModules = ServiceSelectorModule.class)
    void initServicesWidget2(SimplePanel embedToWidget, String infoLabel);

    @Event(forwardToModules = ServiceSelectorModule.class)
    void fillServices(List<ServiceDetail> services);

    @Event(forwardToModules = ServiceSelectorModule.class)
    void selectService(ServiceDetail service);

    @Event(forwardToModules = ServiceSelectorModule.class)
    void requestCreateUserService(long userId, ServiceDetail serviceDetail);

    /**************************************************************************/
    /* Business events handled by Handlers.                                   */
    /**************************************************************************/
    /**************************************************************************/
    /* Business events handled by UserMenuPresenter --- USER MENU             */
    /**************************************************************************/
    @Event(handlers = MenuPresenter.class)
    void menuStyleChange(int loadedModule);


    /**************************************************************************/
    /* Action box module                                                      */
    /**************************************************************************/
    @Event(forwardToModules = ActionBoxModule.class)
    void initActionBox(SimplePanel holderWidget, UniversalAsyncGrid grid);

    /**
     * Send/Response method pair
     * Update message read status.
     * @param messageToSend
     */
    @Event(forwardToModules = ActionBoxModule.class)
    void requestReadStatusUpdate(List<Long> userMessageIds, boolean isRead);

    @Event(forwardToModules = ActionBoxModule.class)
    void responseReadStatusUpdate();

    /**
     * Send/Response method pair
     * Update message star status.
     * @param messageToSend
     */
    @Event(forwardToModules = ActionBoxModule.class)
    void requestStarStatusUpdate(List<Long> userMessageIdList, boolean newStatus);

    @Event(forwardToModules = ActionBoxModule.class)
    void responseStarStatusUpdate();

    /**
     * Send status message i.e. when user clicks on action buttons like Accept Offer, Finish Offer, Close Demand etc.
     * @param statusMessageBody representing status message text
     */
    @Event(forwardToModules = DetailModule.class)
    void sendStatusMessage(String statusMessageBody);

    /**************************************************************************/
    /* User Registration Module                                               */
    /**************************************************************************/
    @Event(forwardToModules = UserRegistrationModule.class)
    void initUserRegistration(SimplePanel holderWidget);

    @Event(forwardToModules = UserRegistrationModule.class)
    void fillBusinessUserDetail(BusinessUserDetail userDetail);

    /**************************************************************************/
    /* Detail module business events.                                         */
    /**************************************************************************/
    @Event(forwardToModules = DetailModule.class)
    void initDetailSection(SimplePanel detailSection);

    @Event(forwardToModules = DetailModule.class)
    void buildDetailSectionTabs(DetailModuleBuilder builder);

    @Event(forwardToModules = DetailModule.class)
    void displayAdvertisement();

    @Event(forwardToModules = DetailModule.class)
    void setCustomWidget(int tabIndex, Widget customWidget);

    @Event(forwardToModules = DetailModule.class)
    void setCustomSelectionHandler(SelectionHandler selectionHandler);

    @Event(forwardToModules = DetailModule.class)
    void allowSendingOffer();

    @Event(forwardToModules = DetailModule.class)
    void registerQuestionSubmitHandler(ClickHandler handler);

    /**************************************************************************/
    /* Detail module business events.                                         */
    /**************************************************************************/
    @Event(handlers = RootHandler.class)
    void requestCreditCount(long userId);

    @Event(handlers = {CreditStatusAnnouncerPresenter.class, HeaderPresenter.class })
    void responseCreditCount(Integer credit);

    /**
     * Create professional and create project user registration tab height
     * handlers.
    */
    @Event(forwardToModules = {DemandCreationModule.class, SupplierCreationModule.class }, passive = true)
    void setUserRegistrationHeight(boolean company);

    @Event(forwardToModules = UserRegistrationModule.class)
    void checkCompanySelected();

    void nastyIfs() {
      if (true) {
        if (false) {
          if (true) {
            if (false) {
              if (true) {
                if (false) {
                }
              }
            }
          }
        }
      }
    }
}
