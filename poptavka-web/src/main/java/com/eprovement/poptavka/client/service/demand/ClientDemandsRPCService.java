package com.eprovement.poptavka.client.service.demand;

import com.eprovement.poptavka.domain.enums.OrderType;
import com.eprovement.poptavka.shared.domain.adminModule.OfferDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientProjectConversationDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientProjectDetail;
import com.eprovement.poptavka.shared.search.SearchModuleDataHolder;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martin Slavkovsky
 */
@RemoteServiceRelativePath(ClientDemandsRPCService.URL)
public interface ClientDemandsRPCService extends RemoteService {

    String URL = "service/clientdemandsmodule";

    //************************* CLIENT - My Demands ***************************/
    /**
     * Get all demand's count that has been created by client.
     * When new demand is created by client, will be involved here.
     * As Client: "All demands created by me."
     *
     * @param clientID - client's ID
     * @param filter - define searching criteria if any
     * @return count
     */
    long getClientProjectsCount(long clientID, SearchModuleDataHolder filter);

    /**
     * Get all demands that has been created by client.
     * When new demand is created by client, will be involved here.
     * As Client: "All demands created by me."
     *
     * @param start
     * @param maxResult
     * @param filter
     * @param orderColumns
     * @return list of demand's detail objects
     */
    List<ClientProjectDetail> getClientProjects(int start, int maxResult,
            SearchModuleDataHolder filter, Map<String, OrderType> orderColumns);

    /**
     * When supplier asks something about a demand of some client.
     * The conversation has more messages of course but I want count of threads.
     * As Client: "Questions made by suppliers to demands made by me." "How many suppliers
     * are asing something about a certain demand."
     *
     * @param clientID - client's ID
     * @param demandID - demand's ID
     * @param filter - define searching criteria if any
     * @return count
     */
    long getClientProjectConversationsCount(long clientID, long demandID,
            SearchModuleDataHolder filter);

    /**
     * When supplier asks something about a demand of some client.
     * The conversation has more messages of course but I want count of threads. As
     * Client: "Questions made by suppliers to demands made by me."
     * "How many suppliers are asing something about a certain demand."
     *
     * @param clientID - client's
     * @param demandID - demand's
     * @param start
     * @param maxResult
     * @param filter
     * @param orderColumns
     * @return
     */
    List<ClientProjectConversationDetail> getClientProjectConversations(long clientID, long demandID, int start,
            int maxResult, SearchModuleDataHolder filter, Map<String, OrderType> orderColumns);

    //************************* CLIENT - My Offers ****************************/
    /**
     * Get all demands where have been placed an offer by some supplier.
     * When supplier place an offer to client's demand, the demand will be involved here.
     * As Client: "Demands that have already an offer."
     *
     *
     * @param clientID
     * @param filter
     * @return
     */
    long getClientOffersCount(long clientID, SearchModuleDataHolder filter);

    /**
     * Get all demands where have been placed an offer by some supplier.
     * When supplier place an offer to client's demand, the demand will be involved here.
     * As Client: "Demands that have already an offer."
     *
     * @param clientID - client's ID
     * @param demandID - demands's ID
     * @param start
     * @param maxResult
     * @param filter
     * @param orderColumns
     * @return
     */
    List<FullDemandDetail> getClientOffersCount(long clientID, long demandID, int start,
            int maxResult, SearchModuleDataHolder filter, Map<String, OrderType> orderColumns);

    /**
     * Get all offers of given demand.
     * When supplier place an offer to client's demand, the offer will be involved here.
     * As Client: "How many suppliers placed an offers to a certain demand."
     *
     * @return offers count of given demand
     */
    long getClientDemandOffersCount(long clientID, long demandID, SearchModuleDataHolder filter);

    /**
     * Get all offers of given demand.
     * When supplier place an offer to client's demand, the offer will be involved here.
     * As Client: "How many suppliers placed an offers to a certain demand."
     *
     * @param clientID
     * @param demandID
     * @param start
     * @param maxResult
     * @param filter
     * @param orderColumns
     * @return
     */
    List<OfferDetail> getClientDemandOffers(long clientID, long demandID, int start,
            int maxResult, SearchModuleDataHolder filter, Map<String, OrderType> orderColumns);

    //******************** CLIENT - My Assigned Demands ***********************/
    /**
     * Get all offers that were accepted by client to solve a demand.
     * When client accept an offer, will be involved here.
     * As Client: "All offers that were accepted by me to solve my demand."
     *
     * @param clientID
     * @param filter
     * @return
     */
    long getClientAssignedDemandsCount(long clientID, SearchModuleDataHolder filter);

    /**
     * Get all offers that were accepted by client to solve a demand.
     * When client accept an offer, will be involved here.
     * As Client: "All offers that were accepted by me to solve my demand."
     *
     * @param clientID
     * @param start
     * @param maxResult
     * @param filter
     * @param orderColumns
     * @return
     */
    List<OfferDetail> getClientAssignedDemands(long clientID, int start, int maxResult,
            SearchModuleDataHolder filter, Map<String, OrderType> orderColumns);
}