/*
 * Copyright (C), eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.client.user.supplierdemands.widgets;

import com.eprovement.poptavka.client.user.widget.grid.UniversalAsyncGridBuilder;
import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.detail.DetailModuleBuilder;
import com.eprovement.poptavka.client.user.widget.grid.UniversalAsyncGrid;
import com.eprovement.poptavka.shared.domain.RatingDetail;
import com.eprovement.poptavka.shared.domain.demand.DemandField;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.eprovement.poptavka.shared.search.SortPair;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.mvp4g.client.annotation.Presenter;
import java.util.Arrays;

/**
 * Part of SupplierDemands module widget.
 * Displays supplier's ratings.
 *
 * @author Martin Slavkovsky
 */
@Presenter(view = AbstractSupplierView.class)
public class SupplierRatingsPresenter extends AbstractSupplierPresenter {

    /**************************************************************************/
    /* Bind actions                                                           */
    /**************************************************************************/
    /**
     * Binds table selection handler. Inits detail section.
     */
    @Override
    public void bindView() {
        view.getTable().getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (view.getTable().getSelectionModel() != null
                    && ((SingleSelectionModel) view.getTable().getSelectionModel()).getSelectedObject() != null) {
                    initDetailSection((RatingDetail) ((SingleSelectionModel) view.getTable()
                                        .getSelectionModel()).getSelectedObject());
                }
            }
        });
    }

    /**************************************************************************/
    /* Navigation events */
    /**************************************************************************/
    /**
     * Creates SupplierRatings widget.
     * @param filter - search criteria
     */
    public void onInitSupplierRatings(SearchModuleDataHolder filter) {
        super.initAbstractPresenter(filter, Constants.SUPPLIER_RATINGS);

        eventBus.resetSearchBar(new Label("Supplier's ratings attibure's selector will be here."));
    }

    /**************************************************************************/
    /* Bind View helper methods                                               */
    /**************************************************************************/
    /**
     * Inits detail section.
     * @param selectedDetail - rating detail
     */
    private void initDetailSection(RatingDetail selectedDetail) {
        eventBus.buildDetailSectionTabs(new DetailModuleBuilder.Builder()
            .addRatingTab(selectedDetail.getDemandId())
            .selectTab(DetailModuleBuilder.RATING_TAB)
            .build());
    }

    /**
     * Creates supplier ratings table using UniversalGridFactory.
     * @return table
     */
    @Override
    UniversalAsyncGrid initTable() {
        return new UniversalAsyncGridBuilder<RatingDetail>()
            .addColumnDemandTitle(null)
            .addColumnPrice(null)
            .addSelectionModel(new SingleSelectionModel(), RatingDetail.KEY_PROVIDER)
            .addDefaultSort(Arrays.asList(SortPair.desc(DemandField.CREATED)))
            .build();
    }
}