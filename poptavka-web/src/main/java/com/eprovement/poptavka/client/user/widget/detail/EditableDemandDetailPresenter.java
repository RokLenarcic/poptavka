/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.client.user.widget.detail;

import com.eprovement.poptavka.client.common.ChangeMonitor;
import com.eprovement.poptavka.client.common.ListChangeMonitor;
import com.eprovement.poptavka.client.common.category.CategoryCell;
import com.eprovement.poptavka.client.common.category.CategorySelectorView;
import com.eprovement.poptavka.client.common.locality.LocalitySelectorView;
import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.root.RootEventBus;
import com.eprovement.poptavka.client.user.widget.detail.EditableDemandDetailPresenter.IEditableDemandDetailView;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.domain.ChangeDetail;
import com.eprovement.poptavka.shared.domain.LocalityDetail;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.LazyPresenter;
import com.mvp4g.client.view.LazyView;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mato
 */
@Presenter(view = EditableDemandDetailView.class, multiple = true)
public class EditableDemandDetailPresenter extends LazyPresenter<IEditableDemandDetailView, RootEventBus> {

    public interface IEditableDemandDetailView extends LazyView, IsWidget {

        long getDemandId();

        PopupPanel getSelectorWidgetPopup();

        Button getEditCatBtn();

        Button getEditLocBtn();

        ArrayList<CategoryDetail> getCategories();

        ArrayList<LocalityDetail> getLocalities();

        void setLocalities(List<LocalityDetail> localities);

        void setCategories(List<CategoryDetail> categories);

        void setChangeHandler(ChangeHandler changeHandler);

        void setListChangeHandler(ChangeHandler handler);

        boolean isValid();

        void setFieldEnables(boolean enable);

        void resetFields();

        void revertFields();
    }
    //history of changes
    private ArrayList<ChangeDetail> updatedFields = new ArrayList<ChangeDetail>();

    public void initEditableDemandDetail() {
    }

    @Override
    public void bindView() {
        view.getEditCatBtn().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.initCategoryWidget(
                        view.getSelectorWidgetPopup(),
                        Constants.WITH_CHECK_BOXES_ONLY_ON_LEAFS,
                        CategoryCell.DISPLAY_COUNT_DISABLED,
                        view.getCategories());
                view.getSelectorWidgetPopup().center();
            }
        });
        view.getEditLocBtn().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.initLocalityWidget(
                        view.getSelectorWidgetPopup(),
                        Constants.WITH_CHECK_BOXES,
                        CategoryCell.DISPLAY_COUNT_DISABLED,
                        view.getLocalities());
                view.getSelectorWidgetPopup().center();
            }
        });
        view.getSelectorWidgetPopup().addCloseHandler(
                new CloseHandler<PopupPanel>() {
                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        if (view.getSelectorWidgetPopup()
                                .getWidget() instanceof CategorySelectorView) {
                            view.setCategories(
                                    ((CategorySelectorView) view.getSelectorWidgetPopup().getWidget())
                                    .getCellListDataProvider().getList());
                        } else if (view.getSelectorWidgetPopup()
                                .getWidget() instanceof LocalitySelectorView) {
                            view.setLocalities(
                                    ((LocalitySelectorView) view.getSelectorWidgetPopup().getWidget())
                                    .getCellListDataProvider().getList());
                        }
                    }
                });
        view.setChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                ChangeMonitor source = (ChangeMonitor) event.getSource();
                source.getChangeDetail().setValue(source.getValue());
                if (source.isModified()) {
                    //if contains already - remove before adding new
                    if (updatedFields.contains(source.getChangeDetail())) {
                        updatedFields.remove(source.getChangeDetail());
                    }
                    updatedFields.add(source.getChangeDetail());
                } else {
                    updatedFields.remove(source.getChangeDetail());
                }
            }
        });
        view.setListChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                ListChangeMonitor source = (ListChangeMonitor) event.getSource();
                source.getChangeDetail().setValue(source.getValue());
                if (source.isModified()) {
                    //if contains already - remove before adding new
                    if (updatedFields.contains(source.getChangeDetail())) {
                        updatedFields.remove(source.getChangeDetail());
                    }
                    updatedFields.add(source.getChangeDetail());
                } else {
                    updatedFields.remove(source.getChangeDetail());
                }
            }
        });
    }

    public void onResponseUpdateDemand(Boolean result) {
        if (result) {
            Window.alert("Successfully updated");
        }
    }

    public ArrayList<ChangeDetail> getUpdatedFields() {
        return updatedFields;
    }
}
