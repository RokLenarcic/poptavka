/*
 * Copyright (C), eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.client.homedemands;

import com.eprovement.poptavka.client.catLocSelector.others.CatLocSelectorBuilder;
import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.common.session.Storage;
import com.eprovement.poptavka.client.detail.DetailModuleBuilder;
import com.eprovement.poptavka.client.homedemands.HomeDemandsPresenter.HomeDemandsViewInterface;
import com.eprovement.poptavka.client.root.toolbar.ProvidesToolbar;
import com.eprovement.poptavka.client.service.demand.CatLocSelectorRPCServiceAsync;
import com.eprovement.poptavka.client.user.widget.grid.UniversalAsyncGrid;
import com.eprovement.poptavka.shared.selectors.catLocSelector.ICatLocDetail;
import com.eprovement.poptavka.shared.domain.demand.LesserDemandDetail;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.history.NavigationConfirmationInterface;
import com.mvp4g.client.history.NavigationEventCommand;
import com.mvp4g.client.presenter.LazyPresenter;
import com.mvp4g.client.view.LazyView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This presenter handles main actions for HomeDemands module such as list of all Demands etc.
 * Data retrieving handle categorySelectionHandler.
 * All changes to CellTree, Table range (Pager), Table Selection is stored to history.
 *
 * @author praso, Martin Slavkovsky
 */
@Presenter(view = HomeDemandsView.class)
public class HomeDemandsPresenter
    extends LazyPresenter<HomeDemandsViewInterface, HomeDemandsEventBus>
    implements NavigationConfirmationInterface {

    /**************************************************************************/
    /* View interface                                                         */
    /**************************************************************************/
    public interface HomeDemandsViewInterface extends LazyView, IsWidget, ProvidesToolbar {

        //Table
        UniversalAsyncGrid<LesserDemandDetail> getDataGrid();

        SimplePager getPager();

        //Filter
        Label getFilterLabel();

        Button getFilterClearBtn();

        //Other
        SimplePanel getCategoryTreePanel();

        SimplePanel getDetailPanel();

        SimplePanel getFooterPanel();

    }
    /**************************************************************************/
    /* Attributes                                                             */
    /**************************************************************************/
    /** Class attributes. **/
    private SearchModuleDataHolder searchDataHolder;
    private ICatLocDetail selectedCategory;
    /** Flag attributes. **/
    private long selectedDemandId = -1;
    private long selectedDemandIdByHistory = -1;
    private boolean calledFromHistory;
    private boolean sameCategorySelection;
    private boolean fromWelcome;
    private int pageFromToken = 0;
    private SelectionChangeEvent.Handler handler = new SelectionChangeEvent.Handler() {
        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
            //Get selected item
            List<ICatLocDetail> selectedCategories = new ArrayList<ICatLocDetail>();
            eventBus.fillCatLocs(selectedCategories, builder.getInstanceId());
            //if new selection was made, need to set searchDataHolder = null before creating
            //token for new selection made in categorySelection.onSelected.
            if (!selectedCategories.isEmpty()) {
                searchDataHolder = null;
                //Tree browser supports olny single selection model, therefore can use get(0).
                selectedCategory = selectedCategories.get(0);
                selectCategoryChangeHandlerInner(selectedCategory);
            }
        }
    };
    private CatLocSelectorBuilder builder = new CatLocSelectorBuilder.Builder(Constants.HOME_DEMANDS_MODULE)
        .initCategorySelector()
        .initSelectorTreeBrowser()
        .displayCountOfDemands()
        .addSelectionHandler(handler)
        .build();

    /**************************************************************************/
    /* RPC Service                                                            */
    /* Need RPC service defined here because we need to pass it to CellTree   */
    /* and then to CategoryDataProvider                                       */
    /**************************************************************************/
    @Inject
    private CatLocSelectorRPCServiceAsync categoryService;

    public CatLocSelectorRPCServiceAsync getCategoryService() {
        return categoryService;
    }

    /**************************************************************************/
    /* General Module events                                                  */
    /**************************************************************************/
    public void onStart() {
        eventBus.initCatLocSelector(view.getCategoryTreePanel(), builder);
    }

    /**
     * Sets body and toolbar, footer.
     * Inits Detail module and category selector.
     */
    public void onForward() {
        eventBus.setBody(view);
        eventBus.setToolbarContent("Categories", view.getToolbarContent());
        eventBus.setFooter(view.getFooterPanel());
        eventBus.menuStyleChange(Constants.HOME_DEMANDS_MODULE);
        eventBus.initDetailSection(view.getDetailPanel());
    }

    @Override
    public void confirm(NavigationEventCommand event) {
        // nothing by default
    }

    /**************************************************************************/
    /* Navigation events                                                      */
    /**************************************************************************/
    /**
     * Main Navigation method called either by default application startup or by searching mechanism.
     * @param filter - if searching is needed, this object holds conditions to do so.
     *                               - it's also used as pointer to differ root and child sections
     */
    public void onGoToHomeDemandsModule(SearchModuleDataHolder filter) {
        defaultCommonInit(filter);
        //Get data
        view.getDataGrid().getDataCount(eventBus, new SearchDefinition(filter));
    }

    /**
     * Category index must be passed as argument because at the time we want to
     * select selected category, no categories are loaded from backend yet.
     * @param categoryIdx
     * @param category
     */
    public void onGoToHomeDemandsModuleFromWelcome(final int categoryIdx, ICatLocDetail category) {
        fromWelcome = true;
        Storage.setCurrentlyLoadedView(Constants.HOME_DEMANDS_BY_WELCOME);
        SearchModuleDataHolder filter = SearchModuleDataHolder.getSearchModuleDataHolder();
        filter.setCategories(Arrays.asList(category));
        defaultCommonInit(filter);
        //Get data - category selector tree will take care of data retrieving according to selected category
        eventBus.requestHierarchy(
            CatLocSelectorBuilder.SELECTOR_TYPE_CATEGORIES, category, builder.getInstanceId());

    }

    /**
     * History Navigation method called either when application started from URL
     * or called by back & forward browser actions.
     *
     * @param historyTree - CellTree's open nodes hierarchy stored and parsed from URL.
     * @param categoryDetail - Retrieved CategoryDetail object according to category ID stored and parsed from URL.
     * @param page - Table's page stored and parsed from URL
     * @param demandId - Demand's ID stored and parsed from URL
     */
    public void onGoToHomeDemandsModuleByHistory(SearchModuleDataHolder filterHolder,
        ICatLocDetail categoryDetail, int page, long demandId) {
        defaultCommonInit(filterHolder);
        //Restore tree opened nodes
        if (categoryDetail == null) {
            //if no category selection -> no selection model selection -> no data retrieving -> manually ask for data
            view.getDataGrid().getDataCount(eventBus, new SearchDefinition(filterHolder));
        } else {
            //if category selection -> select and display in tree
            eventBus.requestHierarchy(
                CatLocSelectorBuilder.SELECTOR_TYPE_CATEGORIES, categoryDetail, builder.getInstanceId());
        }
        //Restore table page
        this.calledFromHistory = true;
        this.pageFromToken = page;
        this.selectedDemandIdByHistory = demandId;
    }

    private void defaultCommonInit(SearchModuleDataHolder filter) {
        if (filter == null) {
            eventBus.resetSearchBar(null);
        }
        //flags initialization
        //----------------------------------------------------------------------
        calledFromHistory = false;

        //widget initialization
        //----------------------------------------------------------------------
        createTokenForHistory();
        deselectGridSelection();
        setFilteringLabel(filter);
    }

    /**************************************************************************/
    /* Restoring methods                                                      */
    /**************************************************************************/
    /**
     * Set currentlyLoadedView, filterLabel, searchDataHolder according to given filter.
     *
     * @param filterHolder
     * - if null - hides filterLabel and sets currentlyLoadedView to Constants.HOME_SUPPLIERS_BY_DEFAULT
     * - if not null - display filterLabel and sets currentlyLoadedView to Constants.HOME_SUPPLIERS_BY_SEARCH
     */
    private void setFilteringLabel(SearchModuleDataHolder filterHolder) {
        //FILTER
        //----------------------------------------------------------------------
        if (filterHolder == null) {
            Storage.setCurrentlyLoadedView(Constants.HOME_DEMANDS_BY_DEFAULT);
            view.getFilterLabel().setTitle("");
            view.getFilterLabel().setVisible(false);
            view.getFilterClearBtn().setVisible(false);
            eventBus.resetSearchBar(null);
        } else {
            Storage.setCurrentlyLoadedView(Constants.HOME_DEMANDS_BY_SEARCH);
            view.getFilterLabel().setTitle(filterHolder.toString());
            view.getFilterLabel().setVisible(true);
            view.getFilterClearBtn().setVisible(true);
        }
        searchDataHolder = filterHolder;
    }

    /**************************************************************************/
    /* Bind events                                                            */
    /**************************************************************************/
    /**
     * Events description.
     * - DataGrid.RangeChange
     *        - creates token when table page was changed
     *        - invoked when table page changed
     * - DataGrid.Selection
     *        - display details according to table selection
     * - CellTree.Selection (see onForward)
     *        - according to selection retrieve data to table
     *        - invoked when selecting category detail object in category selection model.
     *          CategorySelectionModel is bind to CellTree
     */
    @Override
    public void bindView() {
        dataGridRangeChangeHandler();
        dataGridSelectioChangeHandler();
        view.getFilterClearBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                eventBus.goToHomeDemandsModule(null);
            }
        });
    }

    /**************************************************************************/
    /* Bind events - helper methods                                           */
    /**************************************************************************/
    /**
     * Handle table range change by creating token for new range/page.
     */
    private void dataGridRangeChangeHandler() {
        view.getDataGrid().addRangeChangeHandler(new RangeChangeEvent.Handler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                createTokenForHistory();
            }
        });
    }

    /**
     * Is called either from by selecting SelectionCategoryModel or when reestablishing history.
     * @param selected
     */
    private void selectCategoryChangeHandlerInner(ICatLocDetail selected) {
        //Table selection
        //----------------------------------------------------------------------
        //If new category is selected, already selected demand won't match -> deselect
        //Deselecting table object is needed despite table data provider has changed,
        //because selection model holds its selected object independent to data provider.
        deselectGridSelection();

        //Set pager
        //----------------------------------------------------------------------
        //If any changed has been made, pager must be set to right page. Setting different
        //page that is already selected fires dataGrid.RangeChangeEvent that handles creating token.
        //In some scenarious we don't want to create another token, therefore differ two cases:
        //1) If setting page is called from history, cancel any attemts to create new token
        if (calledFromHistory) {
            //if actual page differs to history page, pager.setPage fires dataGrid.RangeChangeEvent
            //that handles creating token -> cancel creating new token in this scenario
            if (view.getPager().getPage() != pageFromToken) {
                eventBus.setHistoryStoredForNextOne(false);
            }

            //If same category is selected -> setPage must fire getData on dataProvider
            //If other category is selected -> setPage need not fire getData on dataProvider,
            //because new data will be retrieved therefore there is no point to do that
            //(we don't need to get data on page 0 on actual data because new
            //data will be retrieved)
            if (!sameCategorySelection) {
                view.getDataGrid().cancelRangeChangedEvent();
            }
            calledFromHistory = false;
            view.getPager().setPage(pageFromToken);
        } else {
            //2) If setting page is called by user, make sure to create new token
            //If user selected new category, pager must be reset to 0 and again:
            //If same page is set -> setPage won't fire getData on dataProvider
            //If different page is set -> setPage to 0 will fire getData on dataProvider,
            //therefore cancel rangeChangeEvent because new data will be retrieved
            //(we don't need to get data on page 0 on actual data because new
            //data will be retrieved)
            if (view.getPager().getPage() != 0) {
                view.getDataGrid().cancelRangeChangedEvent();
            }
            view.getPager().setPage(0);
            createTokenForHistory();
        }

        //Retireve data if needed
        //----------------------------------------------------------------------
        if (!sameCategorySelection) {
            if (selected == null) {
                //Retrieve data
                view.getDataGrid().getDataCount(eventBus, new SearchDefinition(searchDataHolder));
            } else {
                //cancel filtering if user selected category from celltree
                if (!fromWelcome) {
                    fromWelcome = false;
                    view.getFilterLabel().setVisible(false);
                    view.getFilterClearBtn().setVisible(false);
                }
                //Set selected category as filter and pass it to filter through that category
                SearchModuleDataHolder filterHolder = SearchModuleDataHolder.getSearchModuleDataHolder();
                filterHolder.getCategories().add(selected);
                //Retrieve data
                view.getDataGrid().getDataCount(eventBus, new SearchDefinition(
                    0, view.getPager().getPageSize(), filterHolder, view.getDataGrid().getSort().getSortOrder()));
            }
        }

        //Reset flags
        //----------------------------------------------------------------------
        sameCategorySelection = false;
    }

    /**
     * Display demand detail in detail view when selected by user.
     */
    private void dataGridSelectioChangeHandler() {
        view.getDataGrid().getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                //get selected demand
                LesserDemandDetail selected =
                    (LesserDemandDetail) ((SingleSelectionModel) view.getDataGrid().getSelectionModel())
                    .getSelectedObject();

                if (selected != null) {
                    selectedDemandId = selected.getDemandId();
                    //retrieve demand detail info and display it
                    eventBus.buildDetailSectionTabs(
                        new DetailModuleBuilder.Builder()
                            .addDemandTab(selectedDemandId)
                            .selectTab(DetailModuleBuilder.DEMAND_DETAIL_TAB)
                            .build());
                    eventBus.openDetail();
                    //create token for this selection
                    createTokenForHistory();
                }
            }
        });
    }

    /**************************************************************************/
    /* Additional methods                                                     */
    /**************************************************************************/
    /**
     * Used to select table item. When demand id is parsed from token, demand
     * object is retrieved and then selected.
     * @param demandDetail
     */
    public void onDisplayDemandDetail(LesserDemandDetail lesserDemandDetail) {
        view.getDataGrid().getSelectionModel().setSelected(lesserDemandDetail, true);
    }

    /**
     * Display demands of selected category.
     * @param list
     */
    public void onResponseGetData() {
        //If demand must be selected, get its detail and select in selectionModel
        if (selectedDemandId != -1) {
            eventBus.getDemand(selectedDemandId);
        } else if (selectedDemandIdByHistory != -1) {
            eventBus.getDemand(selectedDemandIdByHistory);
        }
    }

    /**
     * Recalculate table height if resize event occurs.
     * Usually paddings or margins changes on smaller resolutions.
     * @param actualWidth
     */
    public void onResize(int actualWidth) {
        view.getDataGrid().resize(actualWidth);
    }

    /**************************************************************************/
    /* Helper methods                                                         */
    /**************************************************************************/
    /**
     * Cancel table selection and hide supplier detail.
     */
    private void deselectGridSelection() {
        SingleSelectionModel selectionModel = (SingleSelectionModel) view.getDataGrid().getSelectionModel();
        LesserDemandDetail demand = (LesserDemandDetail) (selectionModel).getSelectedObject();
        if (demand != null) {
            selectionModel.setSelected(demand, false);
        }
        selectedDemandId = -1L;
        eventBus.displayAdvertisement();
    }

    /**
     * Creates token for history according to CellTree's open nodes hierarchy, table page, table selection.
     * @param setStorageHistoryPointerValue
     */
    private void createTokenForHistory() {
        LesserDemandDetail demand =
            (LesserDemandDetail) ((SingleSelectionModel) view.getDataGrid().getSelectionModel())
            .getSelectedObject();

        eventBus.createTokenForHistory(searchDataHolder, selectedCategory, view.getPager().getPage(), demand);
    }


demandvoid nastyIfs() {
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
