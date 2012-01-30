package cz.poptavka.sample.client.root.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import cz.poptavka.sample.client.root.RootEventBus;
import cz.poptavka.sample.client.root.interfaces.IMenuView;
import cz.poptavka.sample.client.root.interfaces.IMenuView.IMenuPresenter;

@Presenter(view = MenuView.class)
public class MenuPresenter extends BasePresenter<IMenuView, RootEventBus>
        implements IMenuPresenter {

    public void onStart() {
        bindView();
        GWT.log("Menu module loaded");
        eventBus.setMenu(view);
    }

    public void bindView() {
        GWT.log("Binding menu view");
        view.getDemandsButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                eventBus.initHomeDemandsModule(null, "home");
            }
        });
        view.getSuppliersButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                eventBus.initHomeSuppliersModule(null, "home");
            }
        });
        view.getCreateSupplierButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                eventBus.goToCreateSupplier();
            }
        });
        view.getCreateDemandButton().addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                eventBus.goToCreateDemand("home");
            }
        });

    }

    public void onSetHomeMenu() {
        bindView();
        GWT.log("set menu after log out");
        eventBus.setMenu(view);
    }
}
