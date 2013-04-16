package com.eprovement.poptavka.client.homedemands;

import com.eprovement.poptavka.client.common.session.Constants;
import com.eprovement.poptavka.client.common.session.Storage;
import com.eprovement.poptavka.client.homesuppliers.TreeItem;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.mvp4g.client.annotation.History;
import com.mvp4g.client.annotation.History.HistoryConverterType;
import com.mvp4g.client.history.HistoryConverter;
import java.util.LinkedList;

/**
 * History converter class. Handles history for HomeDemandsModule.
 *
 * @author slavkovsky.martin
 */
@History(type = HistoryConverterType.DEFAULT, name = "demands")
public class HomeDemandsHistoryConverter implements HistoryConverter<HomeDemandsEventBus> {

    private static final String HOME = "home";
    private static final String USER = "user";
    private static final String VALUE_SEPARATOR = "=";
    private static final String ITEM_SEPARATOR = ";";
    private static final String LIST_BRACKET_LEFT = "(";
    private static final String LIST_BRACKET_RIGHT = ")";
    private static final String LIST_ITEM_SEPARATOR = ",";
    private static final String LIST_ITEM_IDX_SEPARATOR = ":";

    public String onCreateTokenForHistory(SearchModuleDataHolder searchDataHolder,
            LinkedList<TreeItem> openedHierarchy, int page, FullDemandDetail demandDetail) {
        StringBuilder token = new StringBuilder();
        //Location
        token.append(Storage.getUser() == null ? HOME : USER);
        token.append(ITEM_SEPARATOR);
        //Category
        token.append("tree");
        token.append(VALUE_SEPARATOR);
        token.append(convertCateoryMapToToken(openedHierarchy));
        //Page
        token.append(ITEM_SEPARATOR);
        token.append("page");
        token.append(VALUE_SEPARATOR);
        token.append(page);
        //Supplier
        token.append(ITEM_SEPARATOR);
        token.append("supId");
        token.append(VALUE_SEPARATOR);
        token.append(demandDetail == null ? "-1" : demandDetail.getDemandId());
        return token.toString();
    }

    /**
     * Called either when browser action <b>back</b> or <b>forward</b> is evocated,
     * or by clicking on <b>hyperlink</b> with set token.
     *
     * @param methodName - name of the called method
     * @param param - string behind '?' in url (module/method?param).
     *                URL is created by createTokenForHistory method in HomeDemandsHistoryConverter class.
     * @param eventBus - HomeDemandsEventBus
     */
    @Override
    public void convertFromToken(String methodName, String param, HomeDemandsEventBus eventBus) {
        if (param.startsWith(USER)) {
            eventBus.setHistoryStoredForNextOne(false);
            eventBus.loginFromSession(Constants.SKIP);
        }
        Storage.setCalledDueToHistory(true);
        param = param.substring(6, param.length());
        //When back & forward events -> don't need to call goToHomeSupplierModule
        // - it would create new universalAsyncTable, ...
        // - just use what is already created - events will fire appropiate actions
        //parse param
        String[] params = param.split(ITEM_SEPARATOR);
        LinkedList<TreeItem> tree = convertCategoryTokenToMap(params[0].split(VALUE_SEPARATOR)[1]);
        if (tree.isEmpty()) {
            eventBus.setModuleByHistory(null, tree, null,
                    Integer.valueOf(params[1].split(VALUE_SEPARATOR)[1]),
                    Long.valueOf(params[2].split(VALUE_SEPARATOR)[1]));
        } else {
            eventBus.getCategoryAndSetModuleByHistory(
                    null, tree, tree.getLast().getCategoryId(),
                    Integer.valueOf(params[1].split(VALUE_SEPARATOR)[1]),
                    Long.valueOf(params[2].split(VALUE_SEPARATOR)[1]));
        }
    }

    private LinkedList<TreeItem> convertCategoryTokenToMap(String token) {
        LinkedList<TreeItem> tree = new LinkedList<TreeItem>();
        StringBuilder str = new StringBuilder(token);
        str.deleteCharAt(0);
        str.deleteCharAt(str.length() - 1);
        if (!str.toString().isEmpty()) {
            String[] items = str.toString().split(LIST_ITEM_SEPARATOR);
            int level = 1;
            for (String item : items) {
                tree.add(
                        new TreeItem(Long.valueOf(item.split(LIST_ITEM_IDX_SEPARATOR)[0]),
                        Long.valueOf(item.split(LIST_ITEM_IDX_SEPARATOR)[0]),
                        level, Integer.valueOf(item.split(":")[1])));
                level++;
            }
        }
        return tree;
    }

    private String convertCateoryMapToToken(LinkedList<TreeItem> openedHierarcy) {
        StringBuilder str = new StringBuilder();
        str.append(LIST_BRACKET_LEFT);
        if (openedHierarcy != null && !openedHierarcy.isEmpty()) {
            for (TreeItem item : openedHierarcy) {
                str.append(item.getCategoryId());
                str.append(LIST_ITEM_IDX_SEPARATOR);
                str.append(item.getIndex());
                str.append(LIST_ITEM_SEPARATOR);
            }
            str.deleteCharAt(str.length() - 1);
        }
        str.append(LIST_BRACKET_RIGHT);
        return str.toString();
    }

    @Override
    public boolean isCrawlable() {
        return false;
    }
}
