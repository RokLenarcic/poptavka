<<<<<<< HEAD
<<<<<<< HEAD
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.server.service.homedemands;

import com.eprovement.poptavka.client.service.demand.HomeDemandsRPCService;
import com.eprovement.poptavka.domain.address.Locality;
import com.eprovement.poptavka.domain.common.ResultCriteria;
import com.eprovement.poptavka.domain.demand.Category;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.demand.DemandCategory;
import com.eprovement.poptavka.domain.demand.DemandLocality;
import com.eprovement.poptavka.domain.enums.OrderType;
import com.eprovement.poptavka.server.converter.Converter;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.service.address.LocalityService;
import com.eprovement.poptavka.service.common.TreeItemService;
import com.eprovement.poptavka.service.demand.CategoryService;
import com.eprovement.poptavka.service.demand.DemandService;
import com.eprovement.poptavka.service.fulltext.FulltextSearchService;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.domain.LocalityDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import com.eprovement.poptavka.shared.search.FilterItem;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.googlecode.genericdao.search.Search;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <b>HomeDemandsRPCServiceImpl</b> is RPC service for HomeDemands module. It
 * contains two methods: <ul> <li>@see #getDemandsCount(SearchModuleDataHolder
 * detail)</li> <li>@see #getDemands(int start, int count,
 * SearchDefinition searchDefinition, Map<String, OrderType> orderColumns)</li>
 * </ul> The retriving proces is optimized by using different backend methods
 * for different situations depending on given <b>SearchModuleDataHolder</b>
 * detail.
 *
 * @author Praso
 *
 * TODO Praso - Check which method are shared amongst more RPC services.
 * Probably Locality and categories are going to be used in more RPC. The best
 * idea is to make an parent class with locality/category methods and other RPC
 * will extend this class.
 *
 * TODO Praso - doplnit komentare k metodam a optimalizovat na stranke backendu
 */
@Configurable
public class HomeDemandsRPCServiceImpl extends AutoinjectingRemoteService implements HomeDemandsRPCService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HomeDemandsRPCServiceImpl.class);
    private GeneralService generalService;
    private DemandService demandService;
    private CategoryService categoryService;
    private LocalityService localityService;
    private TreeItemService treeItemService;
    private Converter<Demand, FullDemandDetail> demandConverter;
    private Converter<Category, CategoryDetail> categoryConverter;
    private Converter<ResultCriteria, SearchDefinition> criteriaConverter;
    private FulltextSearchService fulltextSearchService;

    // ***********************************************************************
    // Autowired methods
    // ***********************************************************************
    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setDemandService(DemandService demandService) {
        this.demandService = demandService;
    }

    @Autowired
    public void setTreeItemService(TreeItemService treeItemService) {
        this.treeItemService = treeItemService;
    }

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setLocalityService(LocalityService localityService) {
        this.localityService = localityService;
    }

    @Autowired
    public void setFulltextSearchService(FulltextSearchService fulltextSearchService) {
        this.fulltextSearchService = fulltextSearchService;
    }

    @Autowired
    public void setDemandConverter(
            @Qualifier("fullDemandConverter") Converter<Demand, FullDemandDetail> demandConverter) {
        this.demandConverter = demandConverter;
    }

    @Autowired
    public void setCategoryConverter(
            @Qualifier("categoryConverter") Converter<Category, CategoryDetail> categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    @Autowired
    public void setCriteriaConverter(
            @Qualifier("criteriaConverter") Converter<ResultCriteria, SearchDefinition> criteriaConverter) {
        this.criteriaConverter = criteriaConverter;
    }

    /**************************************************************************/
    /*  Categories                                                            */
    /**************************************************************************/
    @Override
    public CategoryDetail getCategory(long categoryID) throws RPCException {
        return categoryConverter.convertToTarget(categoryService.getById(categoryID));
    }

    /**************************************************************************/
    /*  Demands                                                               */
    /**************************************************************************/
    @Override
    public FullDemandDetail getDemand(long demandID) throws RPCException {
        return demandConverter.convertToTarget(demandService.getById(demandID));
    }

    // ***********************************************************************
    // Get filtered demands
    // ***********************************************************************
    /**
     * Method in general gets demands count according to given filter criteria
     * represented by SearchModuleDataHolder. If user don't specify attribute to
     * filter throught, there is no need to use generalService.Search for
     * retrieving data. Therefore different set of methods is used for
     * optimizing the proces.
     *
     * @param definition - define filter criteria
     * @return demands count
     * @throws RPCException
     */
    @Override
    public long getDemandsCount(SearchDefinition definition) throws RPCException {
        if (definition == null || definition.getFilter() == null) {
            return filterWithoutAttributesCount(definition);
        } else {
            if (!definition.getFilter().getSearchText().isEmpty()) {
                return fullTextSearchCount(definition.getFilter().getSearchText());
            } else if (definition.getFilter().getAttributes().isEmpty()) {
                return filterWithoutAttributesCount(definition);
            } else {
                return filterWithAttributesCount(definition);
            }
        }
    }

    /**
     * Method in general gets demands data according to given filter criteria
     * represented by start, maxResult, SearchModuleDataHolder, ordering. If
     * user don't specify attribute to filter throught, there is no need to use
     * generalService.Search for retrieving data. Therefore different set of
     * methods is used for optimizing the proces.
     *
     * @param definition search filters
     * @return list of demand details
     * @throws RPCException
     */
    @Override
    public List<FullDemandDetail> getDemands(SearchDefinition definition) throws RPCException {
        if (definition == null || definition.getFilter() == null) {
            return filterWithoutAttributes(definition);
        } else {
            if (!definition.getFilter().getSearchText().isEmpty()) {
                List<FullDemandDetail> demands = fullTextSearch(definition.getFilter().getSearchText());
                if (demands.size() < (definition.getFirstResult() + definition.getMaxResult())) {
                    return demands;
                } else {
                    return demands.subList(definition.getFirstResult(), definition.getMaxResult());
                }
            } else if (definition.getFilter().getAttributes().isEmpty()) {
                return filterWithoutAttributes(definition);
            } else {
                return filterWithAttributes(definition);
            }
        }
    }

    // ***********************************************************************
    // Get all demands
    // ***********************************************************************
    /**
     * This method is used when <b>no filtering</b> is required. Get all demands
     * count.
     *
     * @return all demands count
     */
    private Long getAllDemandsCount() {
        return demandService.getAllDemandsCount();
    }

    /**
     * This method is used when <b>no filtering</b> is required. Get demands
     * defined by range(start, maxResult) and ordering.
     *
     * @param searchDefinition search filters
     * @return list of demand details
     */
    private List<FullDemandDetail> getSortedDemands(SearchDefinition searchDefinition) {
        List<Demand> demands = demandService.getAll(criteriaConverter.convertToSource(searchDefinition));
        return demandConverter.convertToTargetList(demands);
    }

    // ***********************************************************************
    // Get category demands
    // ***********************************************************************
    /**
     * This mehtod is used when <b>category filtering</b> is required. Get
     * demands count of given categories.
     *
     * @param categories - define categories to filter throught
     * @return demands count of given categories
     */
    private Long getCategoryDemandsCount(List<CategoryDetail> categories) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : categories) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        return demandService.getDemandsCount(cats.toArray(new Category[cats.size()]));
    }

    /**
     * This method is used when <b>category filtering</b> is required. Get
     * demands data of given categories
     *
     * @param searchDefinition search filters
     * @return demand details list of given categories
     */
    private List<FullDemandDetail> getSortedCategoryDemands(SearchDefinition searchDefinition) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : searchDefinition.getFilter().getCategories()) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        return demandConverter.convertToTargetList(demandService.getDemands(
                criteriaConverter.convertToSource(searchDefinition),
                cats.toArray(new Category[cats.size()])));
    }

    // ***********************************************************************
    // Get locality demands
    // ***********************************************************************
    /**
     * This mehtod is used when <b>locality filtering</b> is required. Get
     * demands count of given localities.
     *
     * @param localities - define localities to filter through
     * @return demands count of given localities
     */
    private Long getLocalityDemandsCount(List<LocalityDetail> localities) {
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail locDetail : localities) {
            locs.add(localityService.getLocality(locDetail.getId()));
        }
        return demandService.getDemandsCount(locs.toArray(new Locality[locs.size()]));
    }

    /**
     * This method is used when <b>localities filtering</b> is required. Get
     * demands data of given localities
     *
     * @param searchDefinition search filters
     * @return demand details list of given localities
     */
    private List<FullDemandDetail> getSortedLocalityDemands(SearchDefinition searchDefinition) {
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail catDetail : searchDefinition.getFilter().getLocalities()) {
            locs.add(localityService.getLocality(catDetail.getId()));
        }
        return demandConverter.convertToTargetList(demandService.getDemands(
                criteriaConverter.convertToSource(searchDefinition),
                locs.toArray(new Locality[locs.size()])));
    }

    // ***********************************************************************
    // Get category locality demands
    // ***********************************************************************
    /**
     * This mehtod is used when both <b>category & locality filtering</b> is
     * required. Get demands count of given categories & localities.
     *
     * @param categories- define categories to filter throught
     * @param localities - define localities to filter throught
     * @return demands count of given categories & localities
     */
    private Long getCategoryLocalityDemandsCount(List<CategoryDetail> categories, List<LocalityDetail> localities) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : categories) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail locDetail : localities) {
            locs.add(localityService.getLocality(locDetail.getId()));
        }
        return demandService.getDemandsCount(cats, locs);
    }

    /**
     * This method is used when both <b>categories & localities filtering</b> is
     * required. Get demands data of given categories & localities
     *
     * @param searchDefinition search filters
     * @return demand details list of given categories & localities
     */
    private List<FullDemandDetail> getSortedCategoryLocalityDemands(SearchDefinition searchDefinition) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : searchDefinition.getFilter().getCategories()) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail catDetail : searchDefinition.getFilter().getLocalities()) {
            locs.add(localityService.getLocality(catDetail.getId()));
        }
        return demandConverter.convertToTargetList(demandService.getDemands(
                criteriaConverter.convertToSource(searchDefinition), cats, locs));
    }

    // ***********************************************************************
    // Get demands by Fulltext search
    // ***********************************************************************
    public long fullTextSearchCount(String searchText) throws RPCException {
        return this.fulltextSearchService.searchCount(Demand.class, Demand.DEMAND_FULLTEXT_FIELDS, searchText);
    }

    public List<FullDemandDetail> fullTextSearch(String searchText) throws RPCException {
        final List<Demand> foundDemands =
                this.fulltextSearchService.search(Demand.class, Demand.DEMAND_FULLTEXT_FIELDS, searchText);
        return demandConverter.convertToTargetList(foundDemands);
    }

    // ***********************************************************************
    // Fileter methods
    // ***********************************************************************
    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>no additional attributes filtering</b> is required,
     * therefore there is no need to use backend methods which use
     * <i>general.Search</i> for retrieving data.
     *
     * @param definition - define filtering criteria, which helps this method to
     * make decision
     * @return demands count
     */
    private long filterWithoutAttributesCount(SearchDefinition definition) {
        //null || 0 0
        if ((definition == null || definition.getFilter() == null)
                || (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty())) {
            return getAllDemandsCount();
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            return getCategoryDemandsCount(definition.getFilter().getCategories());
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getLocalityDemandsCount(definition.getFilter().getLocalities());
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getCategoryLocalityDemandsCount(
                    definition.getFilter().getCategories(),
                    definition.getFilter().getLocalities());
        }
        return -1L;
    }

    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>additional attributes filtering</b> is required, therefore
     * there is need to use backend methods which use <i>general.Search</i> for
     * retrieving data.
     *
     * @param definition - define filtering criteria, which helps this method to
     * make decision
     * @return demands count
     */
    private long filterWithAttributesCount(SearchDefinition definition) {
        //0 0
        if (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(definition);
            return (long) generalService.searchAndCount(search).getTotalCount();
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(definition);
            return (long) generalService.searchAndCount(search).getTotalCount();
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(definition);
            return (long) generalService.searchAndCount(search).getTotalCount();
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return generalService.searchAndCount(this.getFilter(definition)).getTotalCount()
                    + generalService.searchAndCount(this.getFilter(definition)).getTotalCount();
        }
        return -1L;
    }

    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>no additional attributes filtering</b> is required,
     * therefore there is no need to use backend methods which use
     * <i>general.Search</i> for retrieving data.
     *
     * @param definition - define filtering criteria, which helps this method to
     * make decision
     * @return demand detail list
     */
    private List<FullDemandDetail> filterWithoutAttributes(SearchDefinition definition) {
        //0 0
        if ((definition == null || definition.getFilter() == null)
                || (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty())) {
            return getSortedDemands(definition);
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            return getSortedCategoryDemands(definition);
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getSortedLocalityDemands(definition);
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getSortedCategoryLocalityDemands(definition);
        }
        return null;
    }

    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>additional attributes filtering</b> is required, therefore
     * there is need to use backend methods which use <i>general.Search</i> for
     * retrieving data.
     *
     * @param searchDefinition - define filtering criteria, which helps this method to make decision
     * @return demand detail list
     */
    private List<FullDemandDetail> filterWithAttributes(SearchDefinition searchDefinition) {
        //0 0
        if (searchDefinition.getFilter().getCategories().isEmpty()
                && searchDefinition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(searchDefinition);
            search.setFirstResult(searchDefinition.getFirstResult());
            search.setMaxResults(searchDefinition.getMaxResult());
            return demandConverter.convertToTargetList(this.generalService.search(search));
        }
        //1 0
        if (!searchDefinition.getFilter().getCategories().isEmpty()
                && searchDefinition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(searchDefinition);
            search.setFirstResult(searchDefinition.getFirstResult());
            search.setMaxResults(searchDefinition.getMaxResult());
            return this.createDemandDetailListCat(this.generalService.searchAndCount(search).getResult());
        }
        //0 1
        if (searchDefinition.getFilter().getCategories().isEmpty()
                && !searchDefinition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(searchDefinition);
            search.setFirstResult(searchDefinition.getFirstResult());
            search.setMaxResults(searchDefinition.getMaxResult());
            return this.createDemandDetailListLoc(this.generalService.searchAndCount(search).getResult());
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!searchDefinition.getFilter().getCategories().isEmpty()
                && !searchDefinition.getFilter().getLocalities().isEmpty()) {
            List<FullDemandDetail> demandsCat = this.createDemandDetailListCat(
                    this.generalService.searchAndCount(
                    this.getFilter(searchDefinition)).getResult());

            List<FullDemandDetail> demandsLoc = this.createDemandDetailListLoc(
                    this.generalService.searchAndCount(
                    this.getFilter(searchDefinition)).getResult());

            List<FullDemandDetail> demands = new ArrayList<FullDemandDetail>();
            for (FullDemandDetail demandCat : demandsCat) {
                if (demandsLoc.contains(demandCat)) {
                    demands.add(demandCat);
                }
            }
            return demands.subList(searchDefinition.getFirstResult(), searchDefinition.getMaxResult());
        }
        return null;
    }

    /**
     * This method create object <b>Search</b> from given
     * <b>SearchModuleDataHolder</b> provided by search module. This object
     * Search is then used in backend methods, which use generalService.Search
     * methods.
     */
    private Search getFilter(SearchDefinition definition) {
        Search search = null;
        String prefix = "";
        if (definition != null) {

            //if category filtering is required
            if (!definition.getFilter().getCategories().isEmpty()) {
                search = new Search(DemandCategory.class);
                prefix = "demand.";

                List<Category> allSubCategories = new ArrayList<Category>();
                for (CategoryDetail cat : definition.getFilter().getCategories()) {
                    allSubCategories.addAll(Arrays.asList(this.getAllSubcategories(cat.getId())));
                }
                search.addFilterIn("category", allSubCategories);
                //if locality filtering is required
            } else if (!definition.getFilter().getLocalities().isEmpty()) {
                search = new Search(DemandLocality.class);
                prefix = "demand.";
                List<Locality> allSubLocalities = new ArrayList<Locality>();
                for (LocalityDetail loc : definition.getFilter().getLocalities()) {
                    allSubLocalities.addAll(Arrays.asList(this.getAllSublocalities(loc.getId())));
                }
                search.addFilterIn("locality", allSubLocalities);
            } else {
                search = new Search(Demand.class);
            }
            //if attribute filtering is required
            for (FilterItem item : definition.getFilter().getAttributes()) {
                if (item.getItem().equals("type")) {
                    search.addFilterEqual(prefix + "type",
                            demandService.getDemandType(item.getValue().toString()));
                } else if (item.getItem().equals("createdDate")) {
                    //created date
                    Calendar calendarDate = Calendar.getInstance(); //today -> case 0
                    //Musi byt? ved to je list, vzdy bude nasetovany nie?
                    if (item.getValue() != null) {
                        switch (Integer.valueOf(item.getValue().toString())) {
                            case 1:
                                calendarDate.add(Calendar.DATE, -1);  //yesterday
                                break;
                            case 2:
                                calendarDate.add(Calendar.DATE, -7);  //last week
                                break;
                            case 3:
                                calendarDate.add(Calendar.MONTH, -1);  //last month
                                break;
                            default:
                                break;
                        }
                        if (Integer.valueOf(item.getValue().toString()) != 4) {
                            search.addFilterGreaterOrEqual(prefix + "createdDate",
                                    new Date(calendarDate.getTimeInMillis()));
                        }
                    }
                } else {
//                    this.filter(search, prefix, item);
                }
            }
        } else {
            search = new Search(Demand.class);
        }
        //if sorting is required
        if (definition.getOrderColumns() != null) {
            for (String item : definition.getOrderColumns().keySet()) {
                if (definition.getOrderColumns().get(item).getValue().equals(OrderType.ASC.getValue())) {
                    search.addSortAsc(prefix + item, true);
                } else {
                    search.addSortDesc(prefix + item, true);
                }
            }
        }
        return search;
    }

    // ***********************************************************************
    // Other methods
    // ***********************************************************************
    private List<FullDemandDetail> createDemandDetailListCat(Collection<DemandCategory> demands) {
        List<FullDemandDetail> fullDemandDetails = new ArrayList<FullDemandDetail>();
        for (DemandCategory demand : demands) {
            fullDemandDetails.add(demandConverter.convertToTarget(demand.getDemand()));
        }
        return fullDemandDetails;
    }

    private List<FullDemandDetail> createDemandDetailListLoc(Collection<DemandLocality> demands) {
        List<FullDemandDetail> fullDemandDetails = new ArrayList<FullDemandDetail>();
        for (DemandLocality demand : demands) {
            fullDemandDetails.add(demandConverter.convertToTarget(demand.getDemand()));
        }
        return fullDemandDetails;
    }

    // TODO Praso - toto je mozno duplikat na inych RPC
    private Category[] getAllSubcategories(long id) {
        final Category cat = this.generalService.find(Category.class, id);
        final List<Category> allSubCategories = this.treeItemService.getAllDescendants(cat, Category.class);
        allSubCategories.add(cat);
        return allSubCategories.toArray(new Category[allSubCategories.size()]);
    }

    private Locality[] getAllSublocalities(Long id) {
        final Locality loc = this.localityService.getLocality(id);
        final List<Locality> allSubLocalites = this.treeItemService.getAllDescendants(loc, Locality.class);
        allSubLocalites.add(loc);
        return allSubLocalites.toArray(new Locality[allSubLocalites.size()]);
    }
}
=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.server.service.homedemands;

import com.eprovement.poptavka.client.service.demand.HomeDemandsRPCService;
import com.eprovement.poptavka.domain.address.Locality;
import com.eprovement.poptavka.domain.common.ResultCriteria;
import com.eprovement.poptavka.domain.demand.Category;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.demand.DemandCategory;
import com.eprovement.poptavka.domain.demand.DemandLocality;
import com.eprovement.poptavka.domain.enums.OrderType;
import com.eprovement.poptavka.server.converter.Converter;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.service.address.LocalityService;
import com.eprovement.poptavka.service.common.TreeItemService;
import com.eprovement.poptavka.service.demand.CategoryService;
import com.eprovement.poptavka.service.demand.DemandService;
import com.eprovement.poptavka.service.fulltext.FulltextSearchService;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.domain.LocalityDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import com.eprovement.poptavka.shared.search.FilterItem;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <b>HomeDemandsRPCServiceImpl</b> is RPC service for HomeDemands module. It
 * contains two methods: <ul> <li>@see #getDemandsCount(SearchModuleDataHolder
 * detail)</li> <li>@see #getDemands(int start, int count,
 * SearchDefinition searchDefinition, Map<String, OrderType> orderColumns)</li>
 * </ul> The retriving proces is optimized by using different backend methods
 * for different situations depending on given <b>SearchModuleDataHolder</b>
 * detail.
 *
 * @author Praso
 *
 * TODO Praso - Check which method are shared amongst more RPC services.
 * Probably Locality and categories are going to be used in more RPC. The best
 * idea is to make an parent class with locality/category methods and other RPC
 * will extend this class.
 *
 * TODO Praso - doplnit komentare k metodam a optimalizovat na stranke backendu
 */
@Configurable
public class HomeDemandsRPCServiceImpl extends AutoinjectingRemoteService implements HomeDemandsRPCService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HomeDemandsRPCServiceImpl.class);
    private GeneralService generalService;
    private DemandService demandService;
    private CategoryService categoryService;
    private LocalityService localityService;
    private TreeItemService treeItemService;
    private Converter<Demand, FullDemandDetail> demandConverter;
    private Converter<Category, CategoryDetail> categoryConverter;
    private Converter<ResultCriteria, SearchDefinition> criteriaConverter;
    private Converter<Filter, FilterItem> filterConverter;
    private FulltextSearchService fulltextSearchService;

    // ***********************************************************************
    // Autowired methods
    // ***********************************************************************
    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setDemandService(DemandService demandService) {
        this.demandService = demandService;
    }

    @Autowired
    public void setTreeItemService(TreeItemService treeItemService) {
        this.treeItemService = treeItemService;
    }

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setLocalityService(LocalityService localityService) {
        this.localityService = localityService;
    }

    @Autowired
    public void setFulltextSearchService(FulltextSearchService fulltextSearchService) {
        this.fulltextSearchService = fulltextSearchService;
    }

    @Autowired
    public void setDemandConverter(
            @Qualifier("fullDemandConverter") Converter<Demand, FullDemandDetail> demandConverter) {
        this.demandConverter = demandConverter;
    }

    @Autowired
    public void setCategoryConverter(
            @Qualifier("categoryConverter") Converter<Category, CategoryDetail> categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    @Autowired
    public void setCriteriaConverter(
            @Qualifier("criteriaConverter") Converter<ResultCriteria, SearchDefinition> criteriaConverter) {
        this.criteriaConverter = criteriaConverter;
    }

    @Autowired
    public void setFilterConverter(
            @Qualifier("filterConverter") Converter<Filter, FilterItem> filterConverter) {
        this.filterConverter = filterConverter;
    }

    /**************************************************************************/
    /*  Categories                                                            */
    /**************************************************************************/
    @Override
    public CategoryDetail getCategory(long categoryID) throws RPCException {
        return categoryConverter.convertToTarget(categoryService.getById(categoryID));
    }

    /**************************************************************************/
    /*  Demands                                                               */
    /**************************************************************************/
    @Override
    public FullDemandDetail getDemand(long demandID) throws RPCException {
        return demandConverter.convertToTarget(demandService.getById(demandID));
    }

    // ***********************************************************************
    // Get filtered demands
    // ***********************************************************************
    /**
     * Method in general gets demands count according to given filter criteria
     * represented by SearchModuleDataHolder. If user don't specify attribute to
     * filter throught, there is no need to use generalService.Search for
     * retrieving data. Therefore different set of methods is used for
     * optimizing the proces.
     *
     * @param definition - define filter criteria
     * @return demands count
     * @throws RPCException
     */
    @Override
    public long getDemandsCount(SearchDefinition definition) throws RPCException {
        if (definition == null || definition.getFilter() == null) {
            return filterWithoutAttributesCount(definition);
        } else {
            if (!definition.getFilter().getSearchText().isEmpty()) {
                return fullTextSearchCount(definition.getFilter().getSearchText());
            } else if (definition.getFilter().getAttributes().isEmpty()) {
                return filterWithoutAttributesCount(definition);
            } else {
                return filterWithAttributesCount(definition);
            }
        }
    }

    /**
     * Method in general gets demands data according to given filter criteria
     * represented by start, maxResult, SearchModuleDataHolder, ordering. If
     * user don't specify attribute to filter throught, there is no need to use
     * generalService.Search for retrieving data. Therefore different set of
     * methods is used for optimizing the proces.
     *
     * @param definition search filters
     * @return list of demand details
     * @throws RPCException
     */
    @Override
    public List<FullDemandDetail> getDemands(SearchDefinition definition) throws RPCException {
        if (definition == null || definition.getFilter() == null) {
            return filterWithoutAttributes(definition);
        } else {
            if (!definition.getFilter().getSearchText().isEmpty()) {
                List<FullDemandDetail> demands = fullTextSearch(definition.getFilter().getSearchText());
                if (demands.size() < (definition.getFirstResult() + definition.getMaxResult())) {
                    return demands;
                } else {
                    return demands.subList(definition.getFirstResult(), definition.getMaxResult());
                }
            } else if (definition.getFilter().getAttributes().isEmpty()) {
                return filterWithoutAttributes(definition);
            } else {
                return filterWithAttributes(definition);
            }
        }
    }

    // ***********************************************************************
    // Get all demands
    // ***********************************************************************
    /**
     * This method is used when <b>no filtering</b> is required. Get all demands
     * count.
     *
     * @return all demands count
     */
    private Long getAllDemandsCount() {
        return demandService.getAllDemandsCount();
    }

    /**
     * This method is used when <b>no filtering</b> is required. Get demands
     * defined by range(start, maxResult) and ordering.
     *
     * @param searchDefinition search filters
     * @return list of demand details
     */
    private List<FullDemandDetail> getSortedDemands(SearchDefinition searchDefinition) {
        List<Demand> demands = demandService.getAll(criteriaConverter.convertToSource(searchDefinition));
        return demandConverter.convertToTargetList(demands);
    }

    // ***********************************************************************
    // Get category demands
    // ***********************************************************************
    /**
     * This mehtod is used when <b>category filtering</b> is required. Get
     * demands count of given categories.
     *
     * @param categories - define categories to filter throught
     * @return demands count of given categories
     */
    private Long getCategoryDemandsCount(List<CategoryDetail> categories) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : categories) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        return demandService.getDemandsCount(cats.toArray(new Category[cats.size()]));
    }

    /**
     * This method is used when <b>category filtering</b> is required. Get
     * demands data of given categories
     *
     * @param searchDefinition search filters
     * @return demand details list of given categories
     */
    private List<FullDemandDetail> getSortedCategoryDemands(SearchDefinition searchDefinition) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : searchDefinition.getFilter().getCategories()) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        return demandConverter.convertToTargetList(demandService.getDemands(
                criteriaConverter.convertToSource(searchDefinition),
                cats.toArray(new Category[cats.size()])));
    }

    // ***********************************************************************
    // Get locality demands
    // ***********************************************************************
    /**
     * This mehtod is used when <b>locality filtering</b> is required. Get
     * demands count of given localities.
     *
     * @param localities - define localities to filter through
     * @return demands count of given localities
     */
    private Long getLocalityDemandsCount(List<LocalityDetail> localities) {
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail locDetail : localities) {
            locs.add(localityService.getLocality(locDetail.getId()));
        }
        return demandService.getDemandsCount(locs.toArray(new Locality[locs.size()]));
    }

    /**
     * This method is used when <b>localities filtering</b> is required. Get
     * demands data of given localities
     *
     * @param searchDefinition search filters
     * @return demand details list of given localities
     */
    private List<FullDemandDetail> getSortedLocalityDemands(SearchDefinition searchDefinition) {
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail catDetail : searchDefinition.getFilter().getLocalities()) {
            locs.add(localityService.getLocality(catDetail.getId()));
        }
        return demandConverter.convertToTargetList(demandService.getDemands(
                criteriaConverter.convertToSource(searchDefinition),
                locs.toArray(new Locality[locs.size()])));
    }

    // ***********************************************************************
    // Get category locality demands
    // ***********************************************************************
    /**
     * This mehtod is used when both <b>category & locality filtering</b> is
     * required. Get demands count of given categories & localities.
     *
     * @param categories- define categories to filter throught
     * @param localities - define localities to filter throught
     * @return demands count of given categories & localities
     */
    private Long getCategoryLocalityDemandsCount(List<CategoryDetail> categories, List<LocalityDetail> localities) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : categories) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail locDetail : localities) {
            locs.add(localityService.getLocality(locDetail.getId()));
        }
        return demandService.getDemandsCount(cats, locs);
    }

    /**
     * This method is used when both <b>categories & localities filtering</b> is
     * required. Get demands data of given categories & localities
     *
     * @param searchDefinition search filters
     * @return demand details list of given categories & localities
     */
    private List<FullDemandDetail> getSortedCategoryLocalityDemands(SearchDefinition searchDefinition) {
        List<Category> cats = new ArrayList<Category>();
        for (CategoryDetail catDetail : searchDefinition.getFilter().getCategories()) {
            cats.add(categoryService.getById(catDetail.getId()));
        }
        List<Locality> locs = new ArrayList<Locality>();
        for (LocalityDetail catDetail : searchDefinition.getFilter().getLocalities()) {
            locs.add(localityService.getLocality(catDetail.getId()));
        }
        return demandConverter.convertToTargetList(demandService.getDemands(
                criteriaConverter.convertToSource(searchDefinition), cats, locs));
    }

    // ***********************************************************************
    // Get demands by Fulltext search
    // ***********************************************************************
    public long fullTextSearchCount(String searchText) throws RPCException {
        return this.fulltextSearchService.searchCount(Demand.class, Demand.DEMAND_FULLTEXT_FIELDS, searchText);
    }

    public List<FullDemandDetail> fullTextSearch(String searchText) throws RPCException {
        final List<Demand> foundDemands =
                this.fulltextSearchService.search(Demand.class, Demand.DEMAND_FULLTEXT_FIELDS, searchText);
        return demandConverter.convertToTargetList(foundDemands);
    }

    // ***********************************************************************
    // Fileter methods
    // ***********************************************************************
    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>no additional attributes filtering</b> is required,
     * therefore there is no need to use backend methods which use
     * <i>general.Search</i> for retrieving data.
     *
     * @param definition - define filtering criteria, which helps this method to
     * make decision
     * @return demands count
     */
    private long filterWithoutAttributesCount(SearchDefinition definition) {
        //null || 0 0
        if ((definition == null || definition.getFilter() == null)
                || (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty())) {
            return getAllDemandsCount();
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            return getCategoryDemandsCount(definition.getFilter().getCategories());
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getLocalityDemandsCount(definition.getFilter().getLocalities());
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getCategoryLocalityDemandsCount(
                    definition.getFilter().getCategories(),
                    definition.getFilter().getLocalities());
        }
        return -1L;
    }

    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>additional attributes filtering</b> is required, therefore
     * there is need to use backend methods which use <i>general.Search</i> for
     * retrieving data.
     *
     * @param definition - define filtering criteria, which helps this method to
     * make decision
     * @return demands count
     */
    private long filterWithAttributesCount(SearchDefinition definition) {
        //0 0
        if (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(definition);
            return (long) generalService.searchAndCount(search).getTotalCount();
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(definition);
            return (long) generalService.searchAndCount(search).getTotalCount();
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(definition);
            return (long) generalService.searchAndCount(search).getTotalCount();
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return generalService.searchAndCount(this.getFilter(definition)).getTotalCount()
                    + generalService.searchAndCount(this.getFilter(definition)).getTotalCount();
        }
        return -1L;
    }

    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>no additional attributes filtering</b> is required,
     * therefore there is no need to use backend methods which use
     * <i>general.Search</i> for retrieving data.
     *
     * @param definition - define filtering criteria, which helps this method to
     * make decision
     * @return demand detail list
     */
    private List<FullDemandDetail> filterWithoutAttributes(SearchDefinition definition) {
        //0 0
        if ((definition == null || definition.getFilter() == null)
                || (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty())) {
            return getSortedDemands(definition);
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            return getSortedCategoryDemands(definition);
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getSortedLocalityDemands(definition);
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            return getSortedCategoryLocalityDemands(definition);
        }
        return null;
    }

    /**
     * This method decide which backend method used to retrieve data. Method is
     * used when <b>additional attributes filtering</b> is required, therefore
     * there is need to use backend methods which use <i>general.Search</i> for
     * retrieving data.
     *
     * @param searchDefinition - define filtering criteria, which helps this method to make decision
     * @return demand detail list
     */
    private List<FullDemandDetail> filterWithAttributes(SearchDefinition searchDefinition) {
        //0 0
        if (searchDefinition.getFilter().getCategories().isEmpty()
                && searchDefinition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(searchDefinition);
            search.setFirstResult(searchDefinition.getFirstResult());
            search.setMaxResults(searchDefinition.getMaxResult());
            return demandConverter.convertToTargetList(this.generalService.search(search));
        }
        //1 0
        if (!searchDefinition.getFilter().getCategories().isEmpty()
                && searchDefinition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(searchDefinition);
            search.setFirstResult(searchDefinition.getFirstResult());
            search.setMaxResults(searchDefinition.getMaxResult());
            return this.createDemandDetailListCat(this.generalService.searchAndCount(search).getResult());
        }
        //0 1
        if (searchDefinition.getFilter().getCategories().isEmpty()
                && !searchDefinition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getFilter(searchDefinition);
            search.setFirstResult(searchDefinition.getFirstResult());
            search.setMaxResults(searchDefinition.getMaxResult());
            return this.createDemandDetailListLoc(this.generalService.searchAndCount(search).getResult());
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!searchDefinition.getFilter().getCategories().isEmpty()
                && !searchDefinition.getFilter().getLocalities().isEmpty()) {
            List<FullDemandDetail> demandsCat = this.createDemandDetailListCat(
                    this.generalService.searchAndCount(
                    this.getFilter(searchDefinition)).getResult());

            List<FullDemandDetail> demandsLoc = this.createDemandDetailListLoc(
                    this.generalService.searchAndCount(
                    this.getFilter(searchDefinition)).getResult());

            List<FullDemandDetail> demands = new ArrayList<FullDemandDetail>();
            for (FullDemandDetail demandCat : demandsCat) {
                if (demandsLoc.contains(demandCat)) {
                    demands.add(demandCat);
                }
            }
            return demands.subList(searchDefinition.getFirstResult(), searchDefinition.getMaxResult());
        }
        return null;
    }

    /**
     * This method create object <b>Search</b> from given
     * <b>SearchModuleDataHolder</b> provided by search module. This object
     * Search is then used in backend methods, which use generalService.Search
     * methods.
     */
    private Search getFilter(SearchDefinition definition) {
        Search search = null;
        String prefix = "";
        if (definition != null) {

            //if category filtering is required
            if (!definition.getFilter().getCategories().isEmpty()) {
                search = new Search(DemandCategory.class);
                prefix = "demand.";

                List<Category> allSubCategories = new ArrayList<Category>();
                for (CategoryDetail cat : definition.getFilter().getCategories()) {
                    allSubCategories.addAll(Arrays.asList(this.getAllSubcategories(cat.getId())));
                }
                search.addFilterIn("category", allSubCategories);
                //if locality filtering is required
            } else if (!definition.getFilter().getLocalities().isEmpty()) {
                search = new Search(DemandLocality.class);
                prefix = "demand.";
                List<Locality> allSubLocalities = new ArrayList<Locality>();
                for (LocalityDetail loc : definition.getFilter().getLocalities()) {
                    allSubLocalities.addAll(Arrays.asList(this.getAllSublocalities(loc.getId())));
                }
                search.addFilterIn("locality", allSubLocalities);
            } else {
                search = new Search(Demand.class);
            }
            //if attribute filtering is required
            for (FilterItem item : definition.getFilter().getAttributes()) {
                search.addFilter(filterConverter.convertToSource(item));
            }
        } else {
            search = new Search(Demand.class);
        }
        //if sorting is required
        if (definition.getOrderColumns() != null) {
            for (String item : definition.getOrderColumns().keySet()) {
                if (definition.getOrderColumns().get(item).getValue().equals(OrderType.ASC.getValue())) {
                    search.addSortAsc(prefix + item, true);
                } else {
                    search.addSortDesc(prefix + item, true);
                }
            }
        }
        return search;
    }

    // ***********************************************************************
    // Other methods
    // ***********************************************************************
    private List<FullDemandDetail> createDemandDetailListCat(Collection<DemandCategory> demands) {
        List<FullDemandDetail> fullDemandDetails = new ArrayList<FullDemandDetail>();
        for (DemandCategory demand : demands) {
            fullDemandDetails.add(demandConverter.convertToTarget(demand.getDemand()));
        }
        return fullDemandDetails;
    }

    private List<FullDemandDetail> createDemandDetailListLoc(Collection<DemandLocality> demands) {
        List<FullDemandDetail> fullDemandDetails = new ArrayList<FullDemandDetail>();
        for (DemandLocality demand : demands) {
            fullDemandDetails.add(demandConverter.convertToTarget(demand.getDemand()));
        }
        return fullDemandDetails;
    }

    // TODO Praso - toto je mozno duplikat na inych RPC
    private Category[] getAllSubcategories(long id) {
        final Category cat = this.generalService.find(Category.class, id);
        final List<Category> allSubCategories = this.treeItemService.getAllDescendants(cat, Category.class);
        allSubCategories.add(cat);
        return allSubCategories.toArray(new Category[allSubCategories.size()]);
    }

    private Locality[] getAllSublocalities(Long id) {
        final Locality loc = this.localityService.getLocality(id);
        final List<Locality> allSubLocalites = this.treeItemService.getAllDescendants(loc, Locality.class);
        allSubLocalites.add(loc);
        return allSubLocalites.toArray(new Locality[allSubLocalites.size()]);
    }
}
>>>>>>> Searching supplier fixed and simplified.
=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.server.service.homedemands;

import com.eprovement.poptavka.client.service.demand.HomeDemandsRPCService;
import com.eprovement.poptavka.domain.address.Locality;
import com.eprovement.poptavka.domain.common.ResultCriteria;
import com.eprovement.poptavka.domain.demand.Category;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.demand.DemandCategory;
import com.eprovement.poptavka.domain.demand.DemandLocality;
import com.eprovement.poptavka.domain.enums.OrderType;
import com.eprovement.poptavka.server.converter.Converter;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.service.address.LocalityService;
import com.eprovement.poptavka.service.common.TreeItemService;
import com.eprovement.poptavka.service.demand.CategoryService;
import com.eprovement.poptavka.service.demand.DemandService;
import com.eprovement.poptavka.service.fulltext.FulltextSearchService;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.domain.LocalityDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import com.eprovement.poptavka.shared.search.FilterItem;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.googlecode.genericdao.search.Field;
import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.Sort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <b>HomeDemandsRPCServiceImpl</b> is RPC service for HomeDemands module. It
 * contains two methods: <ul> <li>@see #getDemandsCount(SearchModuleDataHolder
 * detail)</li> <li>@see #getDemands(int start, int count,
 * SearchDefinition searchDefinition, Map<String, OrderType> orderColumns)</li>
 * </ul> The retriving proces is optimized by using different backend methods
 * for different situations depending on given <b>SearchModuleDataHolder</b>
 * detail.
 *
 * @author Praso
 *
 * TODO Praso - Check which method are shared amongst more RPC services.
 * Probably Locality and categories are going to be used in more RPC. The best
 * idea is to make an parent class with locality/category methods and other RPC
 * will extend this class.
 *
 * TODO Praso - doplnit komentare k metodam a optimalizovat na stranke backendu
 */
@Configurable
public class HomeDemandsRPCServiceImpl extends AutoinjectingRemoteService implements HomeDemandsRPCService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HomeDemandsRPCServiceImpl.class);
    private GeneralService generalService;
    private DemandService demandService;
    private CategoryService categoryService;
    private LocalityService localityService;
    private TreeItemService treeItemService;
    private Converter<Demand, FullDemandDetail> demandConverter;
    private Converter<Category, CategoryDetail> categoryConverter;
    private Converter<ResultCriteria, SearchDefinition> criteriaConverter;
    private Converter<Filter, FilterItem> filterConverter;
    private FulltextSearchService fulltextSearchService;

    // ***********************************************************************
    // Autowired methods
    // ***********************************************************************
    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setDemandService(DemandService demandService) {
        this.demandService = demandService;
    }

    @Autowired
    public void setTreeItemService(TreeItemService treeItemService) {
        this.treeItemService = treeItemService;
    }

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setLocalityService(LocalityService localityService) {
        this.localityService = localityService;
    }

    @Autowired
    public void setFulltextSearchService(FulltextSearchService fulltextSearchService) {
        this.fulltextSearchService = fulltextSearchService;
    }

    @Autowired
    public void setDemandConverter(
            @Qualifier("fullDemandConverter") Converter<Demand, FullDemandDetail> demandConverter) {
        this.demandConverter = demandConverter;
    }

    @Autowired
    public void setCategoryConverter(
            @Qualifier("categoryConverter") Converter<Category, CategoryDetail> categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    @Autowired
    public void setCriteriaConverter(
            @Qualifier("criteriaConverter") Converter<ResultCriteria, SearchDefinition> criteriaConverter) {
        this.criteriaConverter = criteriaConverter;
    }

    @Autowired
    public void setFilterConverter(
            @Qualifier("filterConverter") Converter<Filter, FilterItem> filterConverter) {
        this.filterConverter = filterConverter;
    }

    /**************************************************************************/
    /*  Categories                                                            */
    /**************************************************************************/
    @Override
    public CategoryDetail getCategory(long categoryID) throws RPCException {
        return categoryConverter.convertToTarget(categoryService.getById(categoryID));
    }

    /**************************************************************************/
    /*  Demands                                                               */
    /**************************************************************************/
    @Override
    public FullDemandDetail getDemand(long demandID) throws RPCException {
        return demandConverter.convertToTarget(demandService.getById(demandID));
    }

    // ***********************************************************************
    // Get filtered demands
    // ***********************************************************************
    /**
     * Method in general gets demands count according to given filter criteria
     * represented by SearchModuleDataHolder. If user don't specify attribute to
     * filter throught, there is no need to use generalService.Search for
     * retrieving data. Therefore different set of methods is used for
     * optimizing the proces.
     *
     * @param definition - define filter criteria
     * @return demands count
     * @throws RPCException
     */
    @Override
    public long getDemandsCount(SearchDefinition definition) throws RPCException {
        if (definition == null || definition.getFilter() == null) {
//            return filterWithoutAttributesCount(definition);
            //general
            return getDemandsGeneralCount();
        } else {
            //fulltext
            if (!definition.getFilter().getSearchText().isEmpty()) {
                return fullTextSearchCount(definition);
                //criteria search
            } else {
                return filterWithAttributesCount(definition);
            }
        }
    }

    /**
     * Method in general gets demands data according to given filter criteria
     * represented by start, maxResult, SearchModuleDataHolder, ordering. If
     * user don't specify attribute to filter throught, there is no need to use
     * generalService.Search for retrieving data. Therefore different set of
     * methods is used for optimizing the proces.
     *
     * @param definition search filters
     * @return list of demand details
     * @throws RPCException
     */
    @Override
    public List<FullDemandDetail> getDemands(SearchDefinition definition) throws RPCException {
        if (definition == null || definition.getFilter() == null) {
            //genera;
            return getDemandsGeneral(definition);
        } else {
            //fulltext
            if (!definition.getFilter().getSearchText().isEmpty()) {
                return fullTextSearch(definition);
                //criteria search
            } else {
                return filterWithAttributes(definition);
            }
        }
    }

    /**************************************************************************/
    /*  Get demands in generall                                             */
    /**************************************************************************/
    /**
     * Get all demand's count.
     *
     * @return demand's count
     */
    private Long getDemandsGeneralCount() {
        return demandService.getCount();
    }

    /**
     * Get demands limited by FirstResult and MaxResult attributes.
     *
     * @param definition holder for FirstResult and MaxResult attributes
     * @return demands
     */
    private List<FullDemandDetail> getDemandsGeneral(SearchDefinition definition) {
        Search search = new Search(Demand.class);
        search.setFirstResult(definition.getFirstResult());
        search.setMaxResults(definition.getMaxResult());
        for (String column : definition.getOrderColumns().keySet()) {
            if (definition.getOrderColumns().get(column) == OrderType.ASC) {
                search.addSort(Sort.asc(column));
            } else {
                search.addSort(Sort.desc(column));
            }
        }
        return demandConverter.convertToTargetList(generalService.search(search));
    }

    /**************************************************************************/
    /*  Get demands in fulltext search                                      */
    /**************************************************************************/
    /**
     * Get demands count by full text search.
     *
     * @param searchText - text to be searched
     * @return demands count
     * @throws RPCException
     */
    public long fullTextSearchCount(SearchDefinition definition) throws RPCException {
        return this.fulltextSearchService.searchCount(
                Demand.class, Demand.DEMAND_FULLTEXT_FIELDS, definition.getFilter().getSearchText());
    }

    /**
     * Get demands by full text search.
     *
     * @param searchText - text to be search
     * @return demands
     * @throws RPCException
     */
    public List<FullDemandDetail> fullTextSearch(SearchDefinition definition) throws RPCException {
        final List<Demand> foundDemands = this.fulltextSearchService.search(
                Demand.class, Demand.DEMAND_FULLTEXT_FIELDS, definition.getFilter().getSearchText());
        //TODO RELEASE Juraj - implementd SearchDefinition - first, max result
        if (foundDemands.size() < (definition.getFirstResult() + definition.getMaxResult())) {
            return demandConverter.convertToTargetList(foundDemands);
        } else {
            return demandConverter.convertToTargetList(
                    foundDemands.subList(definition.getFirstResult(), definition.getMaxResult()));
        }
    }

    /**************************************************************************/
    /*  Get demands in search by representer be seraching criteria          */
    /**************************************************************************/
    /**
     * This method decide which method is used to create Search object for retrieve counts.
     *
     * @param definition - define filtering criteria
     * @return demands count
     */
    private long filterWithAttributesCount(SearchDefinition definition) {
        //0 0
        if (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getDemandFilter(definition);
            search.addField("id", Field.OP_COUNT);
            search.setResultMode(Search.RESULT_SINGLE);
            return (Long) this.generalService.searchUnique(search);
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getCategoryFilter(definition);
            search.addField("id", Field.OP_COUNT);
            search.setResultMode(Search.RESULT_SINGLE);
            return (Long) generalService.searchUnique(search);
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getLocalityFilter(definition);
            search.addField("id", Field.OP_COUNT);
            search.setResultMode(Search.RESULT_SINGLE);
            return (Long) generalService.searchUnique(search);
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            Search searchCat = this.getCategoryFilter(definition);
            searchCat.addField("id", Field.OP_COUNT);
            searchCat.setResultMode(Search.RESULT_SINGLE);

            Search searchLoc = this.getLocalityFilter(definition);
            searchLoc.addField("id", Field.OP_COUNT);
            searchLoc.setResultMode(Search.RESULT_SINGLE);

            return (Long) generalService.searchUnique(searchCat)
                    + (Long) generalService.searchUnique(searchLoc);
        }
        return -1L;
    }

    /**
     * This method decide which method is used to create Search object for retrieve data.
     *
     * @param definition define filtering criteria
     * @return demands
     */
    private List<FullDemandDetail> filterWithAttributes(SearchDefinition definition) {
        //0 0
        if (definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getDemandFilter(definition);
            return demandConverter.convertToTargetList(this.generalService.search(search));
        }
        //1 0
        if (!definition.getFilter().getCategories().isEmpty()
                && definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getCategoryFilter(definition);
            return this.createDemandDetailListCat(this.generalService.search(search));
        }
        //0 1
        if (definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            Search search = this.getLocalityFilter(definition);
            return this.createDemandDetailListLoc(this.generalService.searchAndCount(search).getResult());
        }
        //1 1  --> perform join if filtering by category and locality was used
        if (!definition.getFilter().getCategories().isEmpty()
                && !definition.getFilter().getLocalities().isEmpty()) {
            List<FullDemandDetail> demandsCat = this.createDemandDetailListCat(
                    this.generalService.searchAndCount(
                    this.getCategoryFilter(definition))
                    .getResult());

            List<FullDemandDetail> demandsLoc = this.createDemandDetailListLoc(
                    this.generalService.searchAndCount(
                    this.getLocalityFilter(definition))
                    .getResult());

            List<FullDemandDetail> demands = new ArrayList<FullDemandDetail>();
            for (FullDemandDetail demandCat : demandsCat) {
                if (demandsLoc.contains(demandCat)) {
                    demands.add(demandCat);
                }
            }
            return demands;
        }
        return null;
    }

    /**************************************************************************/
    /*  Helper methods used in "Get demands in search" methods              */
    /**************************************************************************/
    /**
     * Create Search object for searching in categories and demand's attributes.
     *
     * @param definition - represents searching criteria
     * @return
     */
    private Search getCategoryFilter(SearchDefinition definition) {
        Search categorySearch = new Search(DemandCategory.class);
        categorySearch.setFirstResult(definition.getFirstResult());
        categorySearch.setMaxResults(definition.getMaxResult());

        List<Category> allSubCategories = new ArrayList<Category>();

        for (CategoryDetail cat : definition.getFilter().getCategories()) {
            allSubCategories = Arrays.asList(getAllSubCategories(cat.getId()));
        }
        categorySearch.addFilterIn("category", allSubCategories);

        if (!definition.getFilter().getAttributes().isEmpty()) {
            categorySearch.addFilterIn("demand", generalService.search(getDemandFilter(definition)));
        }

        return this.getSortSearch(categorySearch, definition.getOrderColumns(), "demand");
    }

    /**
     * Create Search object for searching in localities and demand's attributes.
     *
     * @param definition - represents searching criteria
     * @return
     */
    private Search getLocalityFilter(SearchDefinition definition) {
        Search localitySearch = new Search(DemandLocality.class);
        localitySearch.setFirstResult(definition.getFirstResult());
        localitySearch.setMaxResults(definition.getMaxResult());

        List<Locality> allSubLocalities = new ArrayList<Locality>();
        for (LocalityDetail loc : definition.getFilter().getLocalities()) {
            allSubLocalities = Arrays.asList(
                    this.getAllSublocalities(loc.getId()));
        }
        localitySearch.addFilterIn("locality", allSubLocalities);

        if (!definition.getFilter().getAttributes().isEmpty()) {
            localitySearch.addFilterIn("demand", generalService.search(getDemandFilter(definition)));
        }

        return this.getSortSearch(localitySearch, definition.getOrderColumns(), "demand");
    }

    /**
     * Create Search object for searching in demand's attributes.
     *
     * @param definition - represents searching criteria
     * @return
     */
    private Search getDemandFilter(SearchDefinition definition) {
        Search search = new Search(Demand.class);
        search.setFirstResult(definition.getFirstResult());
        search.setMaxResults(definition.getMaxResult());

        for (FilterItem item : definition.getFilter().getAttributes()) {
            search.addFilter(filterConverter.convertToSource(item));
        }
        return this.getSortSearch(search, definition.getOrderColumns(), "demand");
    }

    private Search getSortSearch(Search search, Map<String, OrderType> orderColumns, String prefix) {
        if (orderColumns != null) {
            for (String item : orderColumns.keySet()) {
                if (orderColumns.get(item).getValue().equals(OrderType.ASC.getValue())) {
                    search.addSortAsc(prefix + item, true);
                } else {
                    search.addSortDesc(prefix + item, true);
                }
            }
        }
        return search;
    }

    /**************************************************************************/
    /*  Helper methods - List convertions                                     */
    /**************************************************************************/
    private ArrayList<FullDemandDetail> createDemandDetailListCat(Collection<DemandCategory> demandsCat) {
        ArrayList<FullDemandDetail> userDetails = new ArrayList<FullDemandDetail>();
        for (DemandCategory demandCat : demandsCat) {
            userDetails.add(demandConverter.convertToTarget(demandCat.getDemand()));
        }
        return userDetails;
    }

    private ArrayList<FullDemandDetail> createDemandDetailListLoc(Collection<DemandLocality> demandsLoc) {
        ArrayList<FullDemandDetail> userDetails = new ArrayList<FullDemandDetail>();
        for (DemandLocality demandLoc : demandsLoc) {
            userDetails.add(demandConverter.convertToTarget(demandLoc.getDemand()));
        }
        return userDetails;
    }

    /**************************************************************************/
    /*  Helper methods - others                                               */
    /**************************************************************************/
    private Category[] getAllSubCategories(Long id) {
        final Category cat = this.generalService.find(Category.class, id);
        final List<Category> allSubCategories = this.treeItemService.getAllDescendants(cat, Category.class);
        allSubCategories.add(cat);
        return allSubCategories.toArray(new Category[allSubCategories.size()]);
    }

    private Locality[] getAllSublocalities(Long id) {
        final Locality loc = this.localityService.getLocality(id);
        final List<Locality> allSubLocalites = this.treeItemService.getAllDescendants(loc, Locality.class);
        allSubLocalites.add(loc);
        return allSubLocalites.toArray(new Locality[allSubLocalites.size()]);
    }
}
>>>>>>> Searching demands & suppliers fixed and simplified
