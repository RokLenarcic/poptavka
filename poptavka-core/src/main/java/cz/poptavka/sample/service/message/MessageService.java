package cz.poptavka.sample.service.message;

import cz.poptavka.sample.dao.message.MessageDao;
import cz.poptavka.sample.dao.message.MessageFilter;
import cz.poptavka.sample.domain.message.Message;
import cz.poptavka.sample.domain.user.User;
import cz.poptavka.sample.service.GenericService;

import java.util.List;

/**
 * @author Juraj Martinka
 *         Date: 3.5.11
 */
public interface MessageService extends GenericService<Message, MessageDao> {
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

}