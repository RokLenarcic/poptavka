/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eprovement.poptavka.server.service.root;

import com.eprovement.poptavka.client.service.demand.RootRPCService;
import com.eprovement.poptavka.domain.address.Locality;
import com.eprovement.poptavka.domain.demand.Category;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.enums.LocalityType;
import com.eprovement.poptavka.domain.message.Message;
import com.eprovement.poptavka.domain.message.UserMessage;
import com.eprovement.poptavka.domain.user.BusinessUser;
import com.eprovement.poptavka.domain.user.Supplier;
import com.eprovement.poptavka.domain.user.User;
import com.eprovement.poptavka.exception.MessageException;
import com.eprovement.poptavka.server.converter.Converter;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.service.address.LocalityService;
import com.eprovement.poptavka.service.demand.CategoryService;
import com.eprovement.poptavka.service.message.MessageService;
import com.eprovement.poptavka.service.usermessage.UserMessageService;
import com.eprovement.poptavka.shared.domain.BusinessUserDetail;
import com.eprovement.poptavka.shared.domain.CategoryDetail;
import com.eprovement.poptavka.shared.domain.LocalityDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.domain.message.MessageDetail;
import com.eprovement.poptavka.shared.domain.message.OfferMessageDetail;
import com.eprovement.poptavka.shared.domain.supplier.FullSupplierDetail;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Martin Slavkovsky
 */
@Configurable
public class RootRPCServiceImpl extends AutoinjectingRemoteService
        implements RootRPCService {

    //Services
    private LocalityService localityService;
    private CategoryService categoryService;
    private GeneralService generalService;
    private MessageService messageService;
    private UserMessageService userMessageService;
    //Converters
    private Converter<BusinessUser, BusinessUserDetail> businessUserConverter;
    private Converter<Demand, FullDemandDetail> demandConverter;
    private Converter<Supplier, FullSupplierDetail> supplierConverter;
    private Converter<Category, CategoryDetail> categoryConverter;
    private Converter<Locality, LocalityDetail> localityConverter;
    private Converter<Message, MessageDetail> messageConverter;

    /**************************************************************************/
    /* Autowired methods                                                      */
    /**************************************************************************/
    //Services
    @Autowired
    public void setLocalityService(LocalityService localityService) {
        this.localityService = localityService;
    }

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired
    public void setUserMessageService(UserMessageService userMessageService) {
        this.userMessageService = userMessageService;
    }

    //Converters
    @Autowired
    public void setBusinessUserConverter(
            @Qualifier("businessUserConverter") Converter<BusinessUser, BusinessUserDetail> businessUserConverter) {
        this.businessUserConverter = businessUserConverter;
    }

    @Autowired
    public void setDemandConverter(
            @Qualifier("fullDemandConverter") Converter<Demand, FullDemandDetail> demandConverter) {
        this.demandConverter = demandConverter;
    }

    @Autowired
    public void setSupplierConverter(
            @Qualifier("supplierConverter") Converter<Supplier, FullSupplierDetail> supplierConverter) {
        this.supplierConverter = supplierConverter;
    }

    @Autowired
    public void setCategoryConverter(
            @Qualifier("categoryConverter") Converter<Category, CategoryDetail> categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    @Autowired
    public void setLocalityConverter(
            @Qualifier("localityConverter") Converter<Locality, LocalityDetail> localityConverter) {
        this.localityConverter = localityConverter;
    }

    @Autowired
    public void setMessageConverter(
            @Qualifier("messageConverter") Converter<Message, MessageDetail> messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**************************************************************************/
    /* Localities methods                                                     */
    /**************************************************************************/
    /**
     * Returns locality list.
     *
     * @param type
     * @return list locality list according to type
     */
    @Override
    public List<LocalityDetail> getLocalities(LocalityType type) throws RPCException {
        List<Locality> localities = localityService.getLocalities(type);
        System.out.println(localities.size());
        return localityConverter.convertToTargetList(localities);
    }

    /**
     * Get children of locality specified by LOCALITY_CODE.
     *
     * @param locCode
     * @return list locality children list
     */
    @Override
    public List<LocalityDetail> getLocalities(String locCode) throws RPCException {
        final Locality locality = localityService.getLocality(locCode);
        if (locality != null) {
            return localityConverter.convertToTargetList(locality.getChildren());
        }
        return new ArrayList<LocalityDetail>();
    }

    /**************************************************************************/
    /* Categories methods                                                     */
    /**************************************************************************/
    @Override
    public List<CategoryDetail> getCategories() throws RPCException {
        final List<Category> categories = categoryService.getRootCategories();
        return categoryConverter.convertToTargetList(categories);
    }

    @Override
    public List<CategoryDetail> getCategoryChildren(Long category) throws RPCException {
        System.out.println("Getting children categories");
        if (category != null) {
            final Category cat = categoryService.getById(category);
            if (cat != null) {
                return categoryConverter.convertToTargetList(cat.getChildren());
            }
        }
        return new ArrayList<CategoryDetail>();
    }

    /**************************************************************************/
    /* User methods                                                           */
    /**************************************************************************/
    @Override
    public BusinessUserDetail getUserById(Long userId) throws RPCException {
        //Find vs. SearchUnique ?? - find - ak neviem ci existuje
        //                         - SearchUnique - ak urcite ze existuje
        return businessUserConverter.convertToTarget(generalService.find(BusinessUser.class, userId));
    }

    /**************************************************************************/
    /* DevelDetailWrapper widget methods                                      */
    /**************************************************************************/
    @Override
    public FullDemandDetail getFullDemandDetail(long demandId) throws RPCException {
        return demandConverter.convertToTarget(generalService.find(Demand.class, demandId));
    }

    @Override
    public FullSupplierDetail getFullSupplierDetail(long supplierId) throws RPCException {
        return supplierConverter.convertToTarget(generalService.find(Supplier.class, supplierId));
    }

    @Override
    // TODO call setMessageReadStatus in body
    public List<MessageDetail> getConversation(
            long threadId, long userId, long userMessageId) throws RPCException {
        Message threadRoot = messageService.getById(threadId);

        setMessageReadStatus(Arrays.asList(new Long[]{userMessageId}), true);

        User user = this.generalService.find(User.class, userId);
        ArrayList<Message> messages = (ArrayList<Message>) this.messageService.getPotentialDemandConversation(
                threadRoot, user);

        return messageConverter.convertToTargetList(messages);
    }

    /**************************************************************************/
    /* Messages methods                                                       */
    /**************************************************************************/
    /**
     * Change 'read' status of sent messages to chosen value.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setMessageReadStatus(List<Long> userMessageIds, boolean isRead) throws RPCException {
        for (Long userMessageId : userMessageIds) {
            UserMessage userMessage = this.generalService.find(UserMessage.class, userMessageId);
            userMessage.setRead(isRead);
            this.userMessageService.update(userMessage);
        }
    }

    /**
     * Message sent by supplier about a query to potential demand.
     * @param messageDetailImpl
     * @return message
     */
    @Override
    public MessageDetail sendQuestionMessage(MessageDetail messageDetailImpl) throws RPCException {
        try {
            Message m = messageService.newReply(this.messageService.getById(
                    messageDetailImpl.getThreadRootId()),
                    this.generalService.find(User.class, messageDetailImpl.getSenderId()));
            m.setBody(messageDetailImpl.getBody());
            m.setSubject(messageDetailImpl.getTitle());
            // TODO set the id correctly, check it
            MessageDetail messageDetailFromDB = messageConverter.convertToTarget(this.messageService.create(m));
            return messageDetailFromDB;
        } catch (MessageException ex) {
            Logger.getLogger(RootRPCServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public OfferMessageDetail sendOfferMessage(OfferMessageDetail offerMessageDetail) throws RPCException {
        //Implement sending offer
        return new OfferMessageDetail();
    }

    /**************************************************************************/
    /* Activation methods                                                     */
    /**************************************************************************/
    @Override
    public boolean activateClient(String activationCode) throws RPCException {
        //TODO backend
        return true;
    }

    @Override
    public boolean sentActivationCodeAgain(BusinessUserDetail client) throws RPCException {
        //TODO backend
        return true;
    }
}
