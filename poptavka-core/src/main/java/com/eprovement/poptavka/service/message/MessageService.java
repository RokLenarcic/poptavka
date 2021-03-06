package com.eprovement.poptavka.service.message;

import com.eprovement.poptavka.dao.message.MessageDao;
import com.eprovement.poptavka.dao.message.MessageFilter;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.message.Message;
import com.eprovement.poptavka.domain.message.UserMessage;
import com.eprovement.poptavka.domain.user.User;
import com.eprovement.poptavka.exception.MessageException;
import com.eprovement.poptavka.service.GenericService;
import com.googlecode.genericdao.search.Search;

import java.util.List;

/**
 * @author Juraj Martinka
 *         Date: 3.5.11
 */
public interface MessageService extends GenericService<Message, MessageDao> {

    /**
     * Cretaes a new message which is a threadRoot of a new thread.
     * It creates a <code>UserMessage</code> for the sneder so that they can
     * see and work with the message.
     *
     * @param user The user who composes (and will send) the message
     * @return the userMessage, whose message's subject, body... should then be filled and
     * which should then be sent
     */
    UserMessage newThreadRoot(User user);
    /**
     * Creates a new message in a reply to a message provided. It constructs
     * the subject from the original message's sbject by prefixing "Re: " to it
     * (if it hasn't been done so before) It also sets the recipient to the
     * sender of the original message
     * It creates and returns a <code>UserMessage</code> for the sender so that they can
     * see and work with the message.
     *
     * @param inReplyTo the message to which the new reply should be created
     * @param user the sender of the reply
     * @return the reply userMessage to be completed and sent
     */
    UserMessage newReply(Message inReplyTo, User user);

    /**
     * For given message {@code inReplyTo} and {@code sender} which wants to send reply for {@code inReplyTo}
     * finds the other user to which the reply will be delivered.
     * <p>
     * This implementation finds user by calling {@link com.eprovement.poptavka.domain.message.Message#getParent()}
     * until the sender is different from {@code sender}.
     * </p>
     * @param inReplyTo message for which we want to send reply
     * @param sender sender who wants to send reply
     * @return user to whom the reply will be sent
     */
    User getUserForReply(Message inReplyTo, User sender);

    /**
     * Load all message threads' roots for specified <code>user</code>.
     * No default ordering is applied! (if <code>resultCriteria</code> is null or ordering is not specified by it).
     *
     * @param user
     * @param messageFilter additional filtering, e.g. only incoming messages
     * @return all threads of messages for given user represented by thread roots
     * @see Message
     */
    List<Message> getMessageThreads(User user, MessageFilter messageFilter);

    /**
     * Loads (really) all messages for given user without any special structuring of result.
     * <code>resultCriteria</code> parameter can be used for simple ordering or limiting size of results.
     * <p>
     * No default ordering is applied!
     *
     * @param user
     * @param messageFilter additional filter, e.g. only outgoing messages
     * @return
     */
    List<Message> getAllMessages(User user, MessageFilter messageFilter);

    /**
     * Returns a userMessage of given message. UserMessage stores attributes like
     * read, starred
     *
     * @param messages
     * @param messageFilter
     * @return
     */
    List<UserMessage> getUserMessages(List<Message> messages, MessageFilter messageFilter);

    /**
     * Loads conversation between supplier and  client related to potential demand supplier's queries.
     *
     * @param message
     * @param supplierUser
     * @return
     */
    List<Message> getPotentialDemandConversation(Message message, User supplierUser);

    /**
     * Loads conversation between supplier and  client related to potential demand supplier's queries.
     *
     * @param message
     * @param supplierUser
     * @param searchDefinition definition for applying additional filters to the list of conversion messages
     *                         such as sorting, paging, ...
     * @return
     */
    List<UserMessage> getConversationUserMessages(Message message, User supplierUser,
            Search searchDefinition);

    /**
     * Returns message thread root assigned to given demand.
     * @param demand
     * @return
     */
    Message getThreadRootMessage(Demand demand);

    /**
     * Loads conversation between supplier and client related to potential offer.
     *
     * @param message
     * @param supplierUser
     * @return
     */
    List<Message> getPotentialOfferConversation(Message threadRoot, User supplierUser);

    /**
     * Gets all the descendants (not just the children) of a given message
     * @param message
     * @return
     */
    List<Message> getAllDescendants(Message message);

    /**
     * Gets all the descendants (not just the children) of every item
     * in the given list of messages
     *
     * @param messages
     * @return
     */
    List<Message> getAllDescendants(List<Message> messages);

    /**
     * Gets count of all the descendants (not just the children) of every item
     * in the given list of messages
     *
     * @param messages
     * @return
     */
    int getAllDescendantsCount(List<Message> messages);

        /**
     * Gets count of all the descendants (not just the children) of the given
     * message
     *
     * @param message
     * @return
     */
    int getAllDescendantsCount(Message message);

    /**
     * Gets count of all the descendants (not just the children) of every item
     * in the given list of messages that the given user can see, i.e. there
     * is a UserMessage for the message
     *
     * @param messages
     * @param user the user, whose message count should be retrieved
     * @return
     */
    int getAllDescendantsCount(List<Message> messages, User user);

    /**
     * Gets count of all the descendants (not just the children) of the given
     * message that the given user can see, i.e. there
     * is a UserMessage for the message
     *
     * @param message
     * @param user the user, whose message count should be retrieved
     * @return
     */
    int getAllDescendantsCount(Message message, User user);

    /**
     * Gets count of all the descendants (not just the children) of every item
     * in the given list of messages that the given user can see and hasn't
     * read
     *
     * @param messages
     * @return
     */
    int getUnreadDescendantsCount(List<Message> messages, User user);

    /**
     * Gets count of all the descendants (not just the children) of the given
     * message that the given user can see and hasn't read
     *
     * @param messages
     * @return
     */
    int getUnreadDescendantsCount(Message message, User user);

    /**
     * Sends a message to the recipients stored in the message.
     * It creates <code>UserMessage</code>s for all the recipients enabling
     * them to see the message as well as send them appropriate notification via email (if they have suitable
     * notification settings).
     * It also places the message properly within the message tree structure.
     *
     * @param message
     * @param to direct recipients
     * @param cc carbon copy (to the attention of)
     * @param bcc blind carbon copy
     *
     * @throws MessageCannotBeSentException if Message is not in COMPOSED state or message.roles are empty
     */
    void send(Message message) throws MessageException;

    /**
     * Gets the child of the message that has been sent the last
     *
     * @param parent
     * @return
     */
    Message getLastChild(Message parent);
}
