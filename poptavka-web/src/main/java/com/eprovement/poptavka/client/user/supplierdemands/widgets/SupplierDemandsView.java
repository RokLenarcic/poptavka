package com.eprovement.poptavka.client.user.supplierdemands.widgets;

import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.common.session.Storage;
import com.eprovement.poptavka.client.user.widget.grid.UniversalTableWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SupplierDemandsView extends Composite
        implements SupplierDemandsPresenter.SupplierDemandsLayoutInterface {

    private static SupplierDemandsLayoutViewUiBinder uiBinder =
            GWT.create(SupplierDemandsLayoutViewUiBinder.class);

    interface SupplierDemandsLayoutViewUiBinder extends UiBinder<Widget, SupplierDemandsView> {
    }
    /**************************************************************************/
    /* PotentialDemand table Attributes                                       */
    /**************************************************************************/
    //table definition
    @UiField(provided = true)
    UniversalTableWidget tableWidget;
    /**************************************************************************/
    /* Attrinbutes                                                            */
    /**************************************************************************/
    //detail WrapperPanel
    @UiField
    SimplePanel detailPanel;

    /**************************************************************************/
    /* Initialization                                                         */
    /**************************************************************************/
    @Override
    public void createView() {
        //load custom grid cssStyle
        Storage.RSCS.grid().ensureInjected();

        tableWidget = new UniversalTableWidget(Constants.SUPPLIER_POTENTIAL_DEMANDS);
        tableWidget.getTableNameLabel().setText(Storage.MSGS.supplierPotentialDemandsTableTitle());

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**************************************************************************/
    /* Getters                                                                */
    /**************************************************************************/
    //Table
    @Override
    public UniversalTableWidget getTableWidget() {
        return tableWidget;
    }

    @Override
    public SimplePanel getDetailPanel() {
        return detailPanel;
    }

    @Override
    public IsWidget getWidgetView() {
        return this;
    }
}
