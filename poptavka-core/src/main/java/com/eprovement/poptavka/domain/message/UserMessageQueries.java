/*
 * Copyright (C) 2014, eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.domain.message;

/**
 *
 * @author Martin Slavkovsky
 * @since 19.4.2014
 */
public interface UserMessageQueries {

    /**
     * Gets demands count in given <b>status</b> that has no conversation bind to given <b>user</b>.
     */
    String ADMIN_NEW_DEMANDS_COUNT = "Admin.getNewDemandsCount";
    String ADMIN_NEW_DEMANDS_COUNT_QUERY
        = "SELECT COUNT(m.demand.id) FROM Message as m \n"
        + " WHERE m.demand.status = 'NEW' "
        + " AND m.id NOT IN (" + UserMessageQueries.OPERATORS_USER_MESSAGES + ")";

    /**
     * Gets all demands in given <b>status</b> that has no conversation bind to given <b>user</b>.
     */
    String ADMIN_NEW_DEMANDS = "Admin.getNewDemands";
    String ADMIN_NEW_DEMANDS_QUERY
        = "SELECT m.demand FROM Message as m \n"
        + " WHERE m.demand.status = 'NEW' "
        + " AND m.id NOT IN (" + UserMessageQueries.OPERATORS_USER_MESSAGES + ")"
        + " ORDER BY m.demand.createdDate DESC";

    /**
     * Gets user messages count for given user and demand status.
     */
    String ADMIN_ASSIGNED_DEMANDS_COUNT = "Admin.getAssignedDemandsCount";
    String ADMIN_ASSIGNED_DEMANDS_COUNT_QUERY
        = "select count(userMessage.id) \n"
        + "from UserMessage as userMessage \n"
        + "where userMessage.user.id = :userId"
        + " and userMessage.message.demand.status = :demandStatus"
        //nemusim hladat max-send, staci root message
        + " and userMessage.message.parent is null";

    /**
     * Gets all user messages and their counts for given user and demand status.
     */
    String ADMIN_ASSIGNED_DEMANDS = "Admin.getAssignedDemands";
    String ADMIN_ASSIGNED_DEMANDS_QUERY
        = "select userMessage, count(userMessage.message.id) \n"
        + "from UserMessage as userMessage \n"
        + "where userMessage.user.id = :userId"
        + " and userMessage.message.demand.status = :demandStatus "
        + " and userMessage.message.sent = " + UserMessageQueries.MAX_SENT + "\n"
        + "group by userMessage.message.threadRoot.id";

    /**
     * Gets all <b>max</b> value of <b>sent</b> date from userMessage's message's threadRoot message.
     */
    String MAX_SENT
        = "(select max(um.sent) "
        + "from Message as um "
        + "where um.threadRoot.id = userMessage.message.threadRoot.id)";
    String OPERATORS_USER_MESSAGES
        = "SELECT um.message.id FROM UserMessage as um WHERE um.user.id IN (" + UserMessageQueries.OPERATORS + ")";
    String OPERATORS
        = "SELECT u.id FROM User as u JOIN u.accessRoles as r WHERE r.code IN ('ROLE_ADMIN','ROLE_OPERATOR')";

    String MAX_USER_MESSAGE
        = " select MAX(userMessage.id)"
        + " from UserMessage as userMessage"
        + " where userMessage.message.demand is not null"
        + "   and userMessage.message.demand.status IN (:demandStatuses)"
        + "   and userMessage.message.demand.client.businessUser != :user"
        + "   and userMessage.user = :user"
        + " group by userMessage.message.threadRoot";
}
