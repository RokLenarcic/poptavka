package com.eprovement.poptavka.client.service.demand;

import com.eprovement.poptavka.shared.domain.RatingDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientDashboardDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientDemandConversationDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientDemandDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.domain.message.UnreadMessagesDetail;
import com.eprovement.poptavka.shared.domain.offer.ClientOfferedDemandOffersDetail;
import com.eprovement.poptavka.shared.exceptions.ApplicationSecurityException;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

/**
 *
 * @author Martin Slavkovsky
 */
@RemoteServiceRelativePath(ClientDemandsModuleRPCService.URL)
public interface ClientDemandsModuleRPCService extends RemoteService {

    String URL = "service/clientdemandsmodule";

    /**************************************************************************/
    /* Table getter methods                                                   */
    /**************************************************************************/
    //************************* CLIENT - My Demands ***************************/
    /**
     * Get all demand's count that has been created by client.
     * When new demand is created by client, will be involved here.
     * As Client: "All demands created by me."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param filter - define searching criteria if any
     * @return count
     */
    int getClientDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get all demands that has been created by client.
     * When new demand is created by client, will be involved here.
     * As Client: "All demands created by me."
     *
     * @param userId
     * @param searchDefinition
     * @return list of demand's detail objects
     */
    List<ClientDemandDetail> getClientDemands(long userId, SearchDefinition searchDefinition)
        throws RPCException, ApplicationSecurityException;

    /**
     * When supplier asks something about a demand of some client.
     * The conversation has more messages of course but I want count of threads.
     * As Client: "Questions made by suppliers to demands made by me." "How many suppliers
     * are asing something about a certain demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param demandID - demand's ID
     * @param filter - define searching criteria if any
     * @return count
     */
    int getClientDemandConversationsCount(long userId, long demandID,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * When supplier asks something about a demand of some client.
     * The conversation has more messages of course but I want count of threads. As
     * Client: "Questions made by suppliers to demands made by me."
     * "How many suppliers are asing something about a certain demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param demandID - demand's
     * @param searchDefinition
     * @return
     */
    List<ClientDemandConversationDetail> getClientDemandConversations(long userId, long demandID,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    //************************* CLIENT - My Offers ****************************/
    /**
     * Get all demands where have been placed an offer by some supplier.
     * When supplier place an offer to client's demand, the demand will be involved here.
     * As Client: "Demands that have already an offer."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param filter
     * @return
     */
    int getClientOfferedDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get all demands where have been placed an offer by some supplier.
     * When supplier place an offer to client's demand, the demand will be involved here.
     * As Client: "Demands that have already an offer."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param demandID - demands's ID
     * @param searchDefinition
     * @return
     */
    List<ClientDemandDetail> getClientOfferedDemands(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get all suppliers and their offers of given demand.
     * When supplier place an offer to client's demand, the offer will be involved here.
     * As Client: "How many suppliers placed an offers to a certain demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @return offers count of given demand
     */
    int getClientOfferedDemandOffersCount(long userId, long demandID,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get all suppliers and their offers of given demand.
     * When supplier place an offer to client's demand, the offer will be involved here.
     * As Client: "How many suppliers placed an offers to a certain demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param demandID
     * @param searchDefinition
     * @return
     */
    List<ClientOfferedDemandOffersDetail> getClientOfferedDemandOffers(long userId, long demandID, long threadRootId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    //******************** CLIENT - My Assigned Demands ***********************/
    /**
     * Get all offers that were accepted by client to solve a demand.
     * When client accept an offer, will be involved here.
     * As Client: "All offers that were accepted by me to solve my demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param filter
     * @return
     */
    int getClientAssignedDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get all offers that were accepted by client to solve a demand.
     * When client accept an offer, will be involved here.
     * As Client: "All offers that were accepted by me to solve my demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different
     *               If userId represents some different user than client, exception will be thrown
     * @param searchDefinition
     * @return
     */
    List<ClientOfferedDemandOffersDetail> getClientAssignedDemands(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    //******************** CLIENT - My Closed Demands *************************/
    /**
     * Get all closed demands.
     * When client accept supplier's work, demand is closed and stored to demand's history.
     *
     * @param userId user's id
     * @param filter
     * @return
     */
    int getClientClosedDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get all closed demands.
     * When client accept supplier's work, demand is closed and stored to demand's history.
     *
     * @param userId user's id
     * @param searchDefinition
     * @return
     */
    List<ClientOfferedDemandOffersDetail> getClientClosedDemands(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    //******************** CLIENT - My Ratings ********************************/
    /**
     * Get ratings of my closed demands.
     *
     * @param userId user's id
     * @param searchDefinition
     * @return
     */
    int getClientRatingsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**
     * Get ratings of my all closed demands.
     *
     * @param userId user's id
     * @param searchDefinition
     * @return
     */
    List<RatingDetail> getClientRatings(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException;

    /**************************************************************************/
    /* Setter methods                                                         */
    /**************************************************************************/
    void acceptOffer(long offerId) throws RPCException, ApplicationSecurityException;

    /**************************************************************************/
    /* Messages methods                                                       */
    /**************************************************************************/
    UnreadMessagesDetail updateUnreadMessagesCount() throws RPCException, ApplicationSecurityException;

    /**************************************************************************/
    /* CRUD operation of demand                                               */
    /**************************************************************************/
    FullDemandDetail updateDemand(long demandId, FullDemandDetail updatedDemand) throws
            RPCException, ApplicationSecurityException;

    FullDemandDetail requestDeleteDemand(long demandId) throws RPCException, ApplicationSecurityException;

    /**
     * Client enters a new feedback for Supplier with respect to given demand and close that demand.
     *
     * @param demandID of Demand to which this feedback is connected
     * @param supplierRating integer number that will be assigned to supplier
     * @param supplierMessage comment that will be assigned to supplier
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    void closeDemandAndEnterFeedbackForSupplier(long demandID, long offerID, Integer supplierRating,
            String supplierMessage) throws RPCException;

    /**
     * Load all data to construct ClientDashboardDetail. Data such as number of unread messages for particular sections
     * will be retrieved.
     *
     * @param userId of Client for which dashboard object will be created
     * @return clientDashboardDetail
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    ClientDashboardDetail getClientDashboardDetail(long userId) throws RPCException, ApplicationSecurityException;

    /**
     * Substract credit from Supplier after Client closes project. Credit can be substracted even when Supplier has no
     * sufficient requested amount available. In such case, credit will be negative and Supplier cannot send
     * further offers, therefore he must recharge his credit.
     * @param offerId
     * @param credits
     * @return
     * @throws RPCException
     * @throws ApplicationSecurityException
    */
    Boolean substractCredit(long offerId, int credits) throws RPCException, ApplicationSecurityException;
}
