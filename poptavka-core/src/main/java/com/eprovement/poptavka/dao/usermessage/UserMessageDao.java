/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.dao.usermessage;

import com.eprovement.poptavka.dao.GenericDao;
import com.eprovement.poptavka.dao.message.MessageFilter;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.enums.DemandStatus;
import com.eprovement.poptavka.domain.message.ClientConversation;
import com.eprovement.poptavka.domain.message.Message;
import com.eprovement.poptavka.domain.message.UserMessage;
import com.eprovement.poptavka.domain.offer.OfferState;
import com.eprovement.poptavka.domain.user.BusinessUser;
import com.eprovement.poptavka.domain.user.User;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ivan.vlcek
 */
public interface UserMessageDao extends GenericDao<UserMessage> {

    /**
     * Returns a userMessage of given message. UserMessage stores attributes like
     * read, starred
     *
     * @param message
     * @param messageFilter
     * @return
     */
    List<UserMessage> getUserMessages(List<Message> messages,
            User user, MessageFilter messageFilter);

    /**
     * Returns a userMessage to a given message and user.
     * UserMessage stores attributes like isRead, isStared
     *
     * @param message
     * @param messageFilter
     * @return
     */
    UserMessage getUserMessage(Message message, User user);

    /**
     * Gets the user's inbox
     * @param user the user whose inbox to get
     * @return list of <code>UserMesage</code> of the mesages in the user's
     * inbox
     */
    List<UserMessage> getInbox(User user);

    /**
     * Gets the user's outbox
     * @param user the user whose inbox to get
     * @return list of <code>UserMesage</code> of the mesages in the user's
     * outbox
     */
    List<UserMessage> getSentItems(User user);

    /**
     * Gets all the user's valid potential demand messages count
     * @param user the supplier to retreive the demand messages for
     * @return count of <code>UserMesage</code> of the potential demand mesages
     */
    long getPotentialDemandsCount(BusinessUser user);

    /**
     * Gets all the user's valid potential demand messages
     * @param user the supplier to retreive the demand messages for
     * @return list of <code>UserMesage</code> of the potential demand mesages
     */
    List<UserMessage> getPotentialDemands(BusinessUser user);

    Long getAdminNewDemandsCount();
    List<Demand> getAdminNewDemands(int start, int limit);
    /**
     * Gets all admin's assigned demand messages count.
     * @param adminId the admin to retrieve the demand messages for
     * @param status of demand to be filtered
     * @return count of <code>UserMesage</code> of the admin assigned demand mesages
     */
    long getAdminConversationsWithDemandStatusCount(long adminId, DemandStatus status);

    /**
     * Gets all admin's assigned demand messages.
     * @param adminId the admin to retrieve the demand messages for
     * @param status of demand to be filtered
     * @return list of <code>UserMesage</code> of the admin assigned demand mesages
     */
    Map<UserMessage, Integer> getAdminConversationsWithDemandStatus(long adminId, DemandStatus status);

    /**
     * Retrieves a map of the latest <code>UserMessage</code>s in each of the given
     * supplier's conversations along with the counts of messages in each conversation
     * that are accessible to the supplier where any offer has been made
     *
     * @param user the supplier whose conversations to get
     * @return map of the latest <code>UserMessage</code>s and number of
     * messages in each conversation
     */
    Map<UserMessage, Integer> getSupplierConversationsWithoutOffer(BusinessUser user);

    /**
     * Retrieves a map of the latest <code>UserMessage</code>s in each of the given
     * supplier's conversations along with the counts of messages in each conversation
     * that are accessible to the supplier where an offer has been made
     *
     * @param user the supplier whose conversations to get
     * @return map of the latest <code>UserMessage</code>s and number of
     * messages in each conversation
     */
    Map<UserMessage, Integer> getSupplierConversationsWithOffer(BusinessUser user);

    /** Retrieves the count of supplier's conversations where an offer has been
     * made
     *
     * @param user the supplier whose conversations to get
     * @return number of supplier conversations where an offer has been made
     */
    long getSupplierConversationsWithoutOfferCount(BusinessUser user);

    /** Retrieves the count of supplier's conversations where an offer has not
     * been made
     *
     * @param user the supplier whose conversations to get
     * @return number of supplier conversations where an offer has not been made
     */
    long getSupplierConversationsWithOfferCount(BusinessUser user);

    /**
     * Retrieves a map of the latest <code>UserMessage</code> id's in each of the given
     * supplier's conversations along with the counts of messages in each conversation
     * that are accessible to the supplier where an offer is ACCEPTED of COMPLETED
     *
     * @param user the supplier whose conversations to get
     * @param queryName
     * @return map of the latest <code>UserMessage</code> ids and number of
     * messages in each conversation
     */
    Map<UserMessage, Integer> getSupplierConversationsWithAcceptedOffer(BusinessUser user);

    /**
     * Retrieves a map of the latest <code>UserMessage</code> id's in each of the given
     * supplier's conversations along with the counts of messages in each conversation
     * that are accessible to the supplier where <code>DemandStatus</code> is CLOSED.
     *
     * @param user the supplier whose conversations to get
     * @return map of the latest <code>UserMessage</code> ids and number of
     * messages in each conversation
     */
    Map<UserMessage, Integer> getSupplierConversationsWithClosedDemands(BusinessUser user);

   /**
    * Gets a list of all client's conversations related to the given demand where
    * no offer has been made
    *
    * @param user The client whose demands to get
    * @param demand the demand for which to get the conversations
    * @return A map keyed by the latest <code>UserMessage</code> and mapping
    * to the <code>ClientConversation</code> object containing the number of
    * messages in the conversation and the <code>BusinessUser</code> representing
    * the supplier with whom the conversation is being made
    */
    Map<UserMessage, ClientConversation> getClientConversationsWithoutOffer(
            BusinessUser user, Demand demand);

    /** Retrieves the count of client's conversations where an offer has not
     * been made
     *
     * @param user the client whose conversations to get
     * @param demand the demand for which to get the conversations
     * @return number of client's conversations where an offer has not been made
     */
    long getClientConversationsWithoutOfferCount(BusinessUser user, Demand demand);

   /**
    * Gets a list of all client's conversations related to the given demand where
    * an offer has been made
    *
    * @param user The client whose demands to get
    * @param demand the demand for which to get the conversations
    * @return A map keyed by the latest <code>UserMessage</code> and mapping
    * to the <code>ClientConversation</code> object containing the number of
    * messages in the conversation and the <code>BusinessUser</code> representing
    * the supplier with whom the conversation is being made
    */
    Map<UserMessage, ClientConversation> getClientConversationsWithOffer(
            BusinessUser user, Demand demand);

   /**
    * Gets a list of all client's conversations related to the given demand where
    * an offer has been made
    *
    * @param user The client whose demands to get
    * @param demand the demand for which to get the conversations
    * @param offerState the state of the offer linked to the latest conversation
    * messages
    * @return A map keyed by the latest <code>UserMessage</code> and mapping
    * to the <code>ClientConversation</code> object containing the number of
    * messages in the conversation and the <code>BusinessUser</code> representing
    * the supplier with whom the conversation is being made
    */
    Map<UserMessage, ClientConversation> getClientConversationsWithOffer(
            BusinessUser user, Demand demand, OfferState offerState);

     /** Retrieves the count of client's conversations where an offer has
     * been made for given demand.
     *
     * @param user the client whose conversations to get
     * @param demand the demand for which to get the conversations
     * @return number of client's conversations where an offer has been made
     */
    long getClientConversationsWithOfferCount(BusinessUser user, Demand demand);

    /**
     * Gets client's conversations where an offer has been ACCEPTED or COMPLETED
     * @param user the client
     * @return <code>Map</code> of the latest <code>UserMessage</code> in each conversation
     * to <code>ClientConversation</code>
     */
    Map<UserMessage, ClientConversation> getClientConversationsWithAcceptedOffer(BusinessUser user);

    /**
     * Gets client's conversations where an offer has been CLOSED
     * @param user the client
     * @return <code>Map</code> of the latest <code>UserMessage</code> in each conversation
     * to <code>ClientConversation</code>
     */
    Map<UserMessage, ClientConversation> getClientConversationsWithClosedOffer(BusinessUser user);

    /**
     * Gets conversation of the <code>user</code> with the <code>counetrparty</<code>
     * that originated in the <code>threadRoot</code>
     * @param user the user for whom to get the UserMessages
     * @param counterparty the user with whom we are communicating
     * @param rootMessage the first (topmost) message in the conversation
     * @return  list of UserMessages belonging to <code>user</code> in the order from
     * the newest to the oldest
     */
    List<UserMessage> getConversation(User user, User counterparty, Message rootMessage);
}
