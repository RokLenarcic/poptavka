package com.eprovement.poptavka.domain.message;

import com.eprovement.poptavka.domain.common.DomainObject;
import com.eprovement.poptavka.domain.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Stores per-user message attributes
 *
  * @author Vojtech Hubr
 *         Date 12.4.11
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "getUserMessageThreads",
                query = "select userMessage.message "
                        + " from UserMessage userMessage"
                        + " where userMessage.user = :user"),

        @NamedQuery(name = "getPotentialDemandConversation",
                query = " select userMessage.message"
                        + " from UserMessage userMessage"
                        + " where "
                        // either message itself is thread root or it has given thread root
                        + " (userMessage.message = :threadRoot OR userMessage.message.threadRoot = :threadRoot)"
                        + "   and userMessage.user = :supplier and userMessage.message.offer is null"),

        @NamedQuery(name = "getConversationUserMessages",
                query = " select userMessage"
                        + " from UserMessage userMessage"
                        + " where "
                        // either message itself is thread root or it has given thread root
                        + " (userMessage.message = :threadRoot OR userMessage.message.threadRoot = :threadRoot)"
                        + "   and userMessage.user = :user"),
        @NamedQuery(name = "getConversationWithCounterparty",
                query = "select userMessage"
                        + " from UserMessage userMessage"
                        + " left join userMessage.message.roles toRole\n"
                        + "where userMessage.message.threadRoot = :rootMessage"
                        + " and ((userMessage.message.sender = :user"
                        + " and toRole.user = :counterparty)"
                        + " or (userMessage.message.sender = :counterparty"
                        + " and toRole.user = :user))"
                        + " and userMessage.user = :user\n"
                        + "order by userMessage.message.id desc"),
        @NamedQuery(name = "getPotentialOfferConversation",
                query = " select userMessage.message"
                        + " from UserMessage userMessage"
                        + " where "
                        // either message itself is thread root or it has given thread root
                        + " (userMessage.message = :threadRoot OR userMessage.message.threadRoot = :threadRoot)"
                        + "   and userMessage.user = :supplier and userMessage.message.offer is not null"),
        @NamedQuery(name = "getUserMesage",
                query = " select userMessage"
                        + " from UserMessage userMessage\n"
                        + "where userMessage.user = :user"
                        + " and userMessage.message = :message"),
        @NamedQuery(name = "getInbox",
                query = "select userMessage"
                        + " from UserMessage userMessage"
                        + " inner join userMessage.message.roles role\n"
                        + "where userMessage.user = :user"
                        + " and role.user = :user"),
        @NamedQuery(name = "getSentItems",
                query = "select userMessage"
                        + " from UserMessage userMessage\n"
                        + "where userMessage.user = :user"
                        + " and userMessage.message.sender = :user"),
        //TODO Martin 15.10.2014 - same named queries - duplicit
        //getSupplierConversationsWithoutOffer == getPotentialDemands
        //getSupplierConversationsWithoutOfferCount == getPotentialDemandsCount
        @NamedQuery(name = "getPotentialDemands",
                query = "select latestUserMessage\n"
                        + "from UserMessage as subUserMessage right join\n"
                        + " subUserMessage.message.threadRoot as rootMessage,"
                        + "UserMessage as latestUserMessage\n"
                        + "where latestUserMessage.id in (" + UserMessageQueries.MAX_USER_MESSAGE + ")"
                        + " and latestUserMessage.message.threadRoot = rootMessage"
                        + " and subUserMessage.user = :user"
                        + " and latestUserMessage.message.offer is null\n"
                        + "group by latestUserMessage.id"),
        @NamedQuery(name = "getPotentialDemandsCount",
                query = "select count(latestUserMessage.id)\n"
                        + "from UserMessage as latestUserMessage\n"
                        + "where latestUserMessage.id in (" + UserMessageQueries.MAX_USER_MESSAGE + ")"
                        + " and latestUserMessage.message.offer is null\n"),
        @NamedQuery(name = "getSupplierConversationsWithoutOffer",
                query = "select latestUserMessage, count(subUserMessage.id)\n"
                        + "from UserMessage as subUserMessage right join\n"
                        + " subUserMessage.message.threadRoot as rootMessage,"
                        + "UserMessage as latestUserMessage\n"
                        + "where latestUserMessage.id in (" + UserMessageQueries.MAX_USER_MESSAGE + ")"
                        + " and latestUserMessage.message.threadRoot = rootMessage"
                        + " and subUserMessage.user = :user"
                        + " and latestUserMessage.message.offer is null\n"
                        + "group by latestUserMessage.id"),
        @NamedQuery(name = "getSupplierConversationsWithOfferCount",
                query = "select count(latestUserMessage.id)\n"
                        + "from UserMessage as latestUserMessage\n"
                        + "where latestUserMessage.user = :user"
                        + " and latestUserMessage.message.demand is not null"
                        + " and latestUserMessage.message.demand.client.businessUser != :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and latestUserMessage.message.offer is not null\n"),
        @NamedQuery(name = "getSupplierConversationsWithoutOfferCount",
                query = "select count(latestUserMessage.id)\n"
                        + "from UserMessage as latestUserMessage\n"
                        + "where latestUserMessage.id in (" + UserMessageQueries.MAX_USER_MESSAGE + ")"
                        + " and latestUserMessage.message.offer is null\n"),
        @NamedQuery(name = "getSupplierConversationsWithOfferState",
                query = "select latestUserMessage, ("
                        + "select count(subUserMessage.id) from UserMessage as subUserMessage\n"
                        + "where subUserMessage.message.threadRoot = latestUserMessage.message.threadRoot"
                        + " and subUserMessage.user = :user"
                        + ")\n"
                        + "from UserMessage latestUserMessage\n"
                        + "where latestUserMessage.user = :user"
                        + " and latestUserMessage.message.threadRoot.demand is not null"
                        + " and latestUserMessage.message.threadRoot.demand.client.businessUser != :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and latestUserMessage.message.offer is not null\n"
                        + " and latestUserMessage.message.offer.state.code IN (:offerStates)"),
            @NamedQuery(name = "getClientConversationsForDemandWithoutOfferCount",
                query = "select count(latestUserMessage.id)\n"
                        + "from UserMessage as latestUserMessage\n"
                        + "where latestUserMessage.user = :user"
                        + " and latestUserMessage.message.threadRoot != latestUserMessage.message"
                        + " and latestUserMessage.message.demand = :demand"
                        + " and latestUserMessage.message.demand.client.businessUser = :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and latestUserMessage.message.offer is null"),
            @NamedQuery(name = "getClientConversationsForDemandWithoutOffer",
                query = "select latestUserMessage, supplier,"
                        + " count(subUserMessage.id)\n"
                        + "from UserMessage as subUserMessage right join\n"
                        + " subUserMessage.message.threadRoot as rootMessage,"
                        + " UserMessage as latestUserMessage"
                        + " left join latestUserMessage.message.roles toRole"
                        + " left join latestUserMessage.message.offer as offer,"
                        + " User as supplier\n"
                        + "where latestUserMessage.message.threadRoot = rootMessage"
                        + " and rootMessage.demand = :demand"
                        + " and rootMessage.demand.client.businessUser = :user"
                        + " and latestUserMessage.user = :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and offer is null"
                        + " and ((latestUserMessage.message.sender = :user"
                        + " and toRole.user = supplier)"
                        + " or (latestUserMessage.message.sender = supplier"
                        + " and toRole.user = :user))"
                        + " and subUserMessage.user = supplier"
                        + " and subUserMessage.message.threadRoot != subUserMessage.message"
                        + "\n"
                        + "group by latestUserMessage.id, supplier.id"),
            @NamedQuery(name = "getClientConversationsForDemandWithOffer",
                query = "select latestUserMessage, supplier,"
                        + " count(subUserMessage.id)\n"
                        + "from UserMessage as subUserMessage right join\n"
                        + " subUserMessage.message.threadRoot as rootMessage,"
                        + " UserMessage as latestUserMessage"
                        + " left join latestUserMessage.message.roles toRole"
                        + " inner join latestUserMessage.message.offer as offer,"
                        + " User as supplier\n"
                        + "where latestUserMessage.message.threadRoot = rootMessage"
                        + " and rootMessage.demand = :demand"
                        + " and rootMessage.demand.client.businessUser = :user"
                        + " and latestUserMessage.user = :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and ((latestUserMessage.message.sender = :user"
                        + " and toRole.user = supplier)"
                        + " or (latestUserMessage.message.sender = supplier"
                        + " and toRole.user = :user))"
                        + " and subUserMessage.user = supplier"
                        + "\n"
                        + "group by latestUserMessage.id, supplier.id"),
            @NamedQuery(name = "getClientConversationsForDemandWithOfferState",
                query = "select latestUserMessage, supplier,"
                        + " count(subUserMessage.id)\n"
                        + "from UserMessage as subUserMessage right join\n"
                        + " subUserMessage.message.threadRoot as rootMessage,"
                        + " UserMessage as latestUserMessage"
                        + " left join latestUserMessage.message.roles toRole"
                        + " inner join latestUserMessage.message.offer as offer,"
                        + " User as supplier\n"
                        + "where latestUserMessage.message.threadRoot = rootMessage"
                        + " and rootMessage.demand = :demand"
                        + " and rootMessage.demand.client.businessUser = :user"
                        + " and latestUserMessage.user = :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and offer.state = :offerState"
                        + " and ((latestUserMessage.message.sender = :user"
                        + " and toRole.user = supplier)"
                        + " or (latestUserMessage.message.sender = supplier"
                        + " and toRole.user = :user))"
                        + " and subUserMessage.user = supplier"
                        + "\n"
                        + "group by latestUserMessage.id, supplier.id"),
            @NamedQuery(name = "getClientConversationsForDemandWithOfferCount",
                query = "select count(latestUserMessage.id)\n"
                        + "from UserMessage latestUserMessage"
                        + " inner join latestUserMessage.message.offer\n"
                        + "where latestUserMessage.user = :user"
                        + " and latestUserMessage.message.demand = :demand"
                        + " and latestUserMessage.message.demand.client.businessUser = :user"
                        + " and latestUserMessage.message.firstBorn is null"),
            @NamedQuery(name = "getClientConversationsForOfferState",
                query = "select latestUserMessage, supplier,"
                        + " count(subUserMessage.id)\n"
                        + "from UserMessage as subUserMessage right join\n"
                        + " subUserMessage.message.threadRoot as rootMessage,"
                        + " UserMessage as latestUserMessage"
                        + " left join latestUserMessage.message.roles toRole"
                        + " inner join latestUserMessage.message.offer as offer,"
                        + " User as supplier\n"
                        + "where latestUserMessage.message.threadRoot = rootMessage"
                        + " and rootMessage.demand.client.businessUser = :user"
                        + " and latestUserMessage.user = :user"
                        + " and latestUserMessage.message.firstBorn is null"
                        + " and offer.state.code IN (:offerStates)"
                        + " and ((latestUserMessage.message.sender = :user"
                        + " and toRole.user = supplier)"
                        + " or (latestUserMessage.message.sender = supplier"
                        + " and toRole.user = :user))"
                        + " and subUserMessage.user = supplier"
                        + " and subUserMessage.message.offer is not null"
                        + "\n"
                        + "group by latestUserMessage.id, supplier.id"),
            @NamedQuery(name = UserMessageQueries.ADMIN_NEW_DEMANDS_COUNT,
                query = UserMessageQueries.ADMIN_NEW_DEMANDS_COUNT_QUERY),
            @NamedQuery(name = UserMessageQueries.ADMIN_NEW_DEMANDS,
                query = UserMessageQueries.ADMIN_NEW_DEMANDS_QUERY),
            @NamedQuery(name = UserMessageQueries.ADMIN_ASSIGNED_DEMANDS_COUNT,
                query = UserMessageQueries.ADMIN_ASSIGNED_DEMANDS_COUNT_QUERY),
            @NamedQuery(name = UserMessageQueries.ADMIN_ASSIGNED_DEMANDS,
                query = UserMessageQueries.ADMIN_ASSIGNED_DEMANDS_QUERY) }
)
public class UserMessage extends DomainObject {

    @ManyToOne
    private Message message;
    @ManyToOne
    private User user;


    /** Column cannot be named "read" because that is MySQL reserved key word */
    // workaround - see http://stackoverflow.com/questions/8667965/found-bit-expected-boolean-after-hibernate-4-upgrade
    @Column(columnDefinition = "BIT")
    private boolean isRead;
    // workaround - see http://stackoverflow.com/questions/8667965/found-bit-expected-boolean-after-hibernate-4-upgrade
    @Column(columnDefinition = "BIT")
    private boolean starred;


    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ContactMessage");
        sb.append("{user.email='").append(user.getEmail()).append('\'');
        sb.append("{message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
