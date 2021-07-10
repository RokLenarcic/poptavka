/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.dao.usermessage;

import com.eprovement.poptavka.dao.GenericHibernateDao;
import com.eprovement.poptavka.dao.message.MessageFilter;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.enums.DemandStatus;
import com.eprovement.poptavka.domain.enums.OfferStateType;
import com.eprovement.poptavka.domain.message.ClientConversation;
import com.eprovement.poptavka.domain.message.Message;
import com.eprovement.poptavka.domain.message.UserMessage;
import com.eprovement.poptavka.domain.message.UserMessageQueries;
import com.eprovement.poptavka.domain.offer.OfferState;
import com.eprovement.poptavka.domain.user.BusinessUser;
import com.eprovement.poptavka.domain.user.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Map;

/**
 *
 * @author ivan.vlcek
 */
public class UserMessageDaoImpl extends GenericHibernateDao<UserMessage> implements UserMessageDao {

    // TODO LATER ivlcek - consider creating UserMessageFilter instead of using MessageFilter
    @Override
    public List<UserMessage> getUserMessages(List<Message> messages, User user, MessageFilter messageFilter) {
        final Criteria userMessageCriteria = buildUserMessageCriteria(messages, user, messageFilter);
        return buildResultCriteria(userMessageCriteria, messageFilter.getResultCriteria()).list();
    }

    @Override
    public UserMessage getUserMessage(Message message, User user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("message", message);

        List<UserMessage> userMessages = runNamedQuery("getUserMesage", queryParams);
        if (userMessages.isEmpty()) {
            return null;
        } else {
            return userMessages.get(0);
        }
    }

    @Override
    public List<UserMessage> getInbox(User user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);

        return runNamedQuery("getInbox", queryParams);
    }

    @Override
    public List<UserMessage> getSentItems(User user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);

        return runNamedQuery("getSentItems", queryParams);
    }

    @Override
    public long getPotentialDemandsCount(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demandStatuses", Arrays.asList(DemandStatus.ACTIVE, DemandStatus.OFFERED));

        return (Long) runNamedQueryForSingleResult("getPotentialDemandsCount", queryParams);
    }

    @Override
    public List<UserMessage> getPotentialDemands(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demandStatuses", Arrays.asList(DemandStatus.ACTIVE, DemandStatus.OFFERED));

        return runNamedQuery("getPotentialDemands", queryParams);
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, Integer> getSupplierConversationsWithoutOffer(BusinessUser user) {
        return getSupplierConversationsHelper(user,
            "getSupplierConversationsWithoutOffer");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getAdminNewDemandsCount() {
        return (Long) em().createNamedQuery(UserMessageQueries.ADMIN_NEW_DEMANDS_COUNT).getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Demand> getAdminNewDemands(int start, int limit) {
        return (List<Demand>) em().createNamedQuery(UserMessageQueries.ADMIN_NEW_DEMANDS)
            .setFirstResult(start)
            .setMaxResults(limit)
            .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAdminConversationsWithDemandStatusCount(long adminId, DemandStatus status) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("userId", adminId);
        queryParams.put("demandStatus", status);
        return (Long) runNamedQueryForSingleResult(UserMessageQueries.ADMIN_ASSIGNED_DEMANDS_COUNT, queryParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UserMessage, Integer> getAdminConversationsWithDemandStatus(long adminId, DemandStatus status) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("userId", adminId);
        queryParams.put("demandStatus", status);
        List<Object[]> unread = runNamedQuery(
                UserMessageQueries.ADMIN_ASSIGNED_DEMANDS,
                queryParams);
        Map<UserMessage, Integer> unreadMap = new HashMap();
        for (Object[] entry : unread) {
            unreadMap.put((UserMessage) entry[0], ((Long) entry[1]).intValue());
        }
        return unreadMap;
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, Integer> getSupplierConversationsWithOffer(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        List<String> offerStates = new ArrayList();
        offerStates.add(OfferStateType.PENDING.getValue());
        queryParams.put("user", user);
        queryParams.put("offerStates", offerStates);
        List<Object[]> unread = runNamedQuery(
                "getSupplierConversationsWithOfferState",
                queryParams);
        Map<UserMessage, Integer> unreadMap = new HashMap();
        for (Object[] entry : unread) {
            unreadMap.put((UserMessage) entry[0], ((Long) entry[1]).intValue());
        }
        return unreadMap;
    }

    /** {@inheritDoc} */
    @Override
    public long getSupplierConversationsWithOfferCount(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);

        return ((Long) runNamedQueryForSingleResult(
            "getSupplierConversationsWithOfferCount",
            queryParams));
    }

    /** {@inheritDoc} */
    @Override
    public long getSupplierConversationsWithoutOfferCount(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demandStatuses", Arrays.asList(DemandStatus.ACTIVE, DemandStatus.OFFERED));

        return ((Long) runNamedQueryForSingleResult(
            "getSupplierConversationsWithoutOfferCount",
            queryParams));
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, Integer> getSupplierConversationsWithAcceptedOffer(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        List<String> offerStates = new ArrayList();
        offerStates.add(OfferStateType.ACCEPTED.getValue());
        offerStates.add(OfferStateType.COMPLETED.getValue());
        queryParams.put("user", user);
        queryParams.put("offerStates", offerStates);
        List<Object[]> unread = runNamedQuery(
                "getSupplierConversationsWithOfferState",
                queryParams);
        Map<UserMessage, Integer> unreadMap = new HashMap();
        for (Object[] entry : unread) {
            unreadMap.put((UserMessage) entry[0], ((Long) entry[1]).intValue());
        }
        return unreadMap;
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, Integer> getSupplierConversationsWithClosedDemands(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        List<String> offerStates = new ArrayList();
        offerStates.add(OfferStateType.CLOSED.getValue());
        queryParams.put("user", user);
        queryParams.put("offerStates", offerStates);

        List<Object[]> unread = runNamedQuery(
                "getSupplierConversationsWithOfferState",
                queryParams);
        Map<UserMessage, Integer> unreadMap = new HashMap();
        for (Object[] entry : unread) {
            unreadMap.put((UserMessage) entry[0], ((Long) entry[1]).intValue());
        }
        return unreadMap;
    }

    private Map<UserMessage, Integer> getSupplierConversationsHelper(BusinessUser user,
        String queryName) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demandStatuses", Arrays.asList(DemandStatus.ACTIVE, DemandStatus.OFFERED));

        List<Object[]> unread = runNamedQuery(
                queryName,
                queryParams);
        Map<UserMessage, Integer> conversationMap = new HashMap();
        for (Object[] entry : unread) {
            conversationMap.put((UserMessage) entry[0], ((Long) entry[1]).intValue());
        }
        return conversationMap;
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, ClientConversation> getClientConversationsWithoutOffer(
        BusinessUser user, Demand demand) {
        return getClientConversationsHelper(user, demand,
                "getClientConversationsForDemandWithoutOffer", null);
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, ClientConversation> getClientConversationsWithOffer(
        BusinessUser user, Demand demand) {
        return getClientConversationsHelper(user, demand,
                "getClientConversationsForDemandWithOffer", null);
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, ClientConversation> getClientConversationsWithOffer(
        BusinessUser user, Demand demand, OfferState offerState) {
        return getClientConversationsHelper(user, demand,
                "getClientConversationsForDemandWithOfferState", offerState);
    }

    private Map<UserMessage, ClientConversation> getClientConversationsHelper(BusinessUser user, Demand demand,
        String queryName, OfferState offerState) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demand", demand);
        if (offerState != null) {
            queryParams.put("offerState", offerState);
        }
        return getClientConversations(queryName, queryParams);
    }

    /** {@inheritDoc} */
    @Override
    public long getClientConversationsWithoutOfferCount(BusinessUser user, Demand demand) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demand", demand);

        return ((Long) runNamedQueryForSingleResult(
            "getClientConversationsForDemandWithoutOfferCount",
            queryParams));
    }

    /** {@inheritDoc} */
    @Override
    public long getClientConversationsWithOfferCount(BusinessUser user, Demand demand) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);
        queryParams.put("demand", demand);

        return ((Long) runNamedQueryForSingleResult(
            "getClientConversationsForDemandWithOfferCount",
            queryParams));
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, ClientConversation> getClientConversationsWithAcceptedOffer(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        List<String> offerStates = new ArrayList();
        offerStates.add(OfferStateType.ACCEPTED.getValue());
        offerStates.add(OfferStateType.COMPLETED.getValue());
        queryParams.put("user", user);
        queryParams.put("offerStates", offerStates);
        return getClientConversations("getClientConversationsForOfferState", queryParams);
    }

    /** {@inheritDoc} */
    @Override
    public Map<UserMessage, ClientConversation> getClientConversationsWithClosedOffer(BusinessUser user) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        List<String> offerStates = new ArrayList();
        offerStates.add(OfferStateType.CLOSED.getValue());
        queryParams.put("user", user);
        queryParams.put("offerStates", offerStates);
        return getClientConversations("getClientConversationsForOfferState", queryParams);
    }

    /** {@inheritDoc} */
    @Override
    public List<UserMessage> getConversation(User user, User counterparty, Message rootMessage) {
        final HashMap<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("user", user);

        queryParams.put("counterparty", counterparty);
        queryParams.put("rootMessage", rootMessage);

        List<Object[]> conversationRaw = runNamedQuery(
                "getConversationWithCounterparty",
                queryParams);
        List<UserMessage> usermessages = new ArrayList();
        for (Object entry : conversationRaw) {
            usermessages.add((UserMessage) entry);
        }
        return usermessages;
    }

    //---------------------------------------------- HELPER METHODS ---------------------------------------------------
    /**
     * Build criterion which can be used for getting a userMessage of given message.
     *
     * @param message
     * @param messageFilter
     * @return
     */
    private Criteria buildUserMessageCriteria(List<Message> messages, User user, MessageFilter messageFilter) {
        final Criteria userMessageCriteria = getHibernateSession().createCriteria(UserMessage.class);
//        userMessageCriteria.add(Restrictions.eq("message", messages));
        userMessageCriteria.add(Restrictions.eq("user", user));
        if (CollectionUtils.isNotEmpty(messages)) {
            userMessageCriteria.add(Restrictions.in("message", messages));
        }

//        userMessageCriteria.setProjection(Projections.property("message"));
        return userMessageCriteria;
    }

    private Map<UserMessage, ClientConversation> getClientConversations(String queryName,
        final HashMap<String, Object> queryParams) {
        List<Object[]> conversations = runNamedQuery(
                queryName,
                queryParams);
        Map<UserMessage, ClientConversation> userMessageMap = new HashMap();
        for (Object[] entry : conversations) {
            userMessageMap.put((UserMessage) entry[0], new ClientConversation(
                (UserMessage) entry[0], (BusinessUser) entry[1],
                ((Long) entry[2]).intValue()));
        }
        return userMessageMap;
    }
}
