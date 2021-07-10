/*
 * Copyright (C) 2012, eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.server.service.clientdemands;

import com.eprovement.poptavka.client.service.demand.ClientDemandsModuleRPCService;
import com.eprovement.poptavka.domain.address.Locality;
import com.eprovement.poptavka.domain.demand.Category;
import com.eprovement.poptavka.domain.demand.Demand;
import com.eprovement.poptavka.domain.demand.Rating;
import com.eprovement.poptavka.domain.enums.CommonAccessRoles;
import com.eprovement.poptavka.domain.enums.DemandStatus;
import com.eprovement.poptavka.domain.enums.OfferStateType;
import com.eprovement.poptavka.domain.message.ClientConversation;
import com.eprovement.poptavka.domain.message.UserMessage;
import com.eprovement.poptavka.domain.offer.Offer;
import com.eprovement.poptavka.domain.offer.OfferState;
import com.eprovement.poptavka.domain.user.BusinessUser;
import com.eprovement.poptavka.domain.user.Client;
import com.eprovement.poptavka.domain.user.Supplier;
import com.eprovement.poptavka.domain.user.User;
import com.eprovement.poptavka.server.converter.Converter;
import com.eprovement.poptavka.server.converter.SearchConverter;
import com.eprovement.poptavka.server.security.PoptavkaUserAuthentication;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.service.demand.DemandService;
import com.eprovement.poptavka.service.offer.OfferService;
import com.eprovement.poptavka.service.user.ClientService;
import com.eprovement.poptavka.service.user.SupplierService;
import com.eprovement.poptavka.service.user.UserSearchCriteria;
import com.eprovement.poptavka.service.usermessage.UserMessageService;
import com.eprovement.poptavka.shared.domain.RatingDetail;
import com.eprovement.poptavka.shared.selectors.catLocSelector.ICatLocDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientDashboardDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientDemandConversationDetail;
import com.eprovement.poptavka.shared.domain.clientdemands.ClientDemandDetail;
import com.eprovement.poptavka.shared.domain.demand.FullDemandDetail;
import com.eprovement.poptavka.shared.domain.message.UnreadMessagesDetail;
import com.eprovement.poptavka.shared.domain.offer.ClientOfferedDemandOffersDetail;
import com.eprovement.poptavka.shared.exceptions.ApplicationSecurityException;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.googlecode.genericdao.search.Field;
import com.googlecode.genericdao.search.Search;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This RPC handles all requests for ClientDemands module.
 * @author Martin Slavkovsky
 */
@Configurable
public class ClientDemandsModuleRPCServiceImpl extends AutoinjectingRemoteService
        implements ClientDemandsModuleRPCService {

    /**************************************************************************/
    /* Attributes                                                             */
    /**************************************************************************/
    public static final String QUERY_TO_POTENTIAL_DEMAND_SUBJECT = "Dotaz na Vasu zadanu poptavku";
    //Services
    private ClientService clientService;
    private SupplierService supplierService;
    private GeneralService generalService;
    private OfferService offerService;
    private UserMessageService userMessageService;
    private DemandService demandService;
    //Converters
    private Converter<Category, ICatLocDetail> categoryConverter;
    private Converter<Locality, ICatLocDetail> localityConverter;
    private Converter<Demand, FullDemandDetail> demandConverter;
    private SearchConverter searchConverter;
    private Converter<Demand, ClientDemandDetail> clientDemandConverter;

    /**************************************************************************/
    /* Autowire services and converters                                       */
    /**************************************************************************/
    //Services
    @Autowired
    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }

    @Autowired
    public void setSupplierService(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Autowired
    public void setGeneralService(GeneralService generalService) {
        this.generalService = generalService;
    }

    @Autowired
    public void setUserMessageService(UserMessageService userMessageService) {
        this.userMessageService = userMessageService;
    }

    @Autowired
    public void setOfferService(OfferService offerService) {
        this.offerService = offerService;
    }

    @Autowired
    public void setDemandService(DemandService demandService) {
        this.demandService = demandService;
    }

    //Converters
    @Autowired
    public void setCategoryConverter(
            @Qualifier("categoryConverter") Converter<Category, ICatLocDetail> categoryConverter) {
        this.categoryConverter = categoryConverter;
    }

    @Autowired
    public void setLocalityConverter(
            @Qualifier("localityConverter") Converter<Locality, ICatLocDetail> localityConverter) {
        this.localityConverter = localityConverter;
    }

    @Autowired
    public void setDemandConverter(
            @Qualifier("fullDemandConverter") Converter<Demand, FullDemandDetail> demandConverter) {
        this.demandConverter = demandConverter;
    }

    @Autowired
    public void setClientDemandConverter(
            @Qualifier("clientDemandConverter") Converter<Demand, ClientDemandDetail> clientDemandConverter) {
        this.clientDemandConverter = clientDemandConverter;
    }

    @Autowired
    public void setSearchConverter(@Qualifier("searchConverter") SearchConverter searchConverter) {
        this.searchConverter = searchConverter;
    }

    /**************************************************************************/
    /* My Demands                                                             */
    /**************************************************************************/
    /**
     * Get all demand's count that has been created by client. When new demand is created by client, will be involved
     * here. As Client: "All demands created by me."
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @param searchDefinition - define searching criteria if any
     * @return count
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final Client client = findClient(userId);
        final Search search = searchConverter.convertToSourceForCount(Demand.class, searchDefinition);
        ArrayList<DemandStatus> demandStatuses = new ArrayList<DemandStatus>();
        demandStatuses.add(DemandStatus.ACTIVE);
        demandStatuses.add(DemandStatus.NEW);
        demandStatuses.add(DemandStatus.INVALID);
        demandStatuses.add(DemandStatus.INACTIVE);
        demandStatuses.add(DemandStatus.OFFERED);
        search.addFilterIn("status", demandStatuses);
        search.addFilterEqual("client", client);
        return generalService.count(search);
    }

    /**
     * {@inheritDoc}
     *
     * <p> PERFORMANCE: This implementation can be slow if client has large amount of demands because it loads all his
     * demands from DB into the memory and perform filtering afterwards. It this is the issue then consider
     * reimplementation of this method. </p>
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @return list of demand's detail objects
     * @throws ApplicationSecurityException if user is not authorized to call this method
     * @throws IllegalArgumentException if given user does not exist or it represents user other than client
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<ClientDemandDetail> getClientDemands(long userId, SearchDefinition searchDefinition) throws
            RPCException, ApplicationSecurityException, IllegalArgumentException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Search search = searchConverter.convertToSource(Demand.class, searchDefinition);
        final Map<Demand, Integer> clientDemandsWithUnreadSubMsgs = demandService
                .getClientDemandsWithUnreadSubMsgs(businessUser, search);

        ArrayList<ClientDemandDetail> clientDemands = clientDemandConverter
                .convertToTargetList(clientDemandsWithUnreadSubMsgs.keySet());

        // load a map of unread messages for all client demands with offer, then iterate this map and
        // match with clientDemands so that unread submessages count will be set
        for (Map.Entry<Demand, Integer> userMessageEntry : clientDemandsWithUnreadSubMsgs.entrySet()) {
            for (ClientDemandDetail cdd : clientDemands) {
                if (cdd.getDemandId() == userMessageEntry.getKey().getId()) {
                    cdd.setUnreadMessagesCount(((Integer) userMessageEntry.getValue()));
                    break;
                }
            }
        }
        return clientDemands;
    }

    /**
     * When supplier asks something about a demand of some client. The conversation has more messages of course but I
     * want count of threads. As Client: "Questions made by suppliers to demands made by me." "How many suppliers are
     * asing something about a certain demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @param demandID - demand's ID
     * @param filter - define searching criteria if any
     * @return count
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientDemandConversationsCount(final long userId, final long demandID,
            final SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Demand demand = generalService.find(Demand.class, demandID);
        return userMessageService.getClientConversationsWithoutOfferCount(businessUser, demand);
    }

    /**
     * Gets a list of all client's conversation related to their demands where no offer has been made
     *
     * @param userId logged client user Id
     * @param demandID demand with question responses from suppliers
     * @param searchDefinition defines a search criteria for pager etc.
     * @return list of latest user messages from suppliers with total count of submessages
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<ClientDemandConversationDetail> getClientDemandConversations(final long userId, final long demandID,
            final SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Demand demand = generalService.find(Demand.class, demandID);
        final Search search = searchConverter.convertToSource(UserMessage.class, searchDefinition);
        final Map<UserMessage, ClientConversation> latestSupplierUserMessagesWithUnreadSub =
                userMessageService.getClientConversationsWithoutOffer(businessUser, demand, search);

        final List<ClientDemandConversationDetail> list = new ArrayList<ClientDemandConversationDetail>();

        for (UserMessage userMessage : latestSupplierUserMessagesWithUnreadSub.keySet()) {
            // TODO LATER ivlcek - make detail object converter
            // TODO RELEASE vojto - implement search definition
            final ClientDemandConversationDetail cdcd = new ClientDemandConversationDetail();
            // set UserMessage attributes
            cdcd.setUserMessageId(userMessage.getId());
            cdcd.setStarred(userMessage.isStarred());
            cdcd.setRead(userMessage.isRead());
            cdcd.setMessagesCount(latestSupplierUserMessagesWithUnreadSub.get(userMessage).getMessageCount());
            // set Message attributes
            cdcd.setThreadRootId(userMessage.getMessage().getThreadRoot().getId());
            cdcd.setMessageBody(userMessage.getMessage().getBody());
            cdcd.setMessageSent(userMessage.getMessage().getSent());
            // set Demand attributes
            cdcd.setDemandId(demandID);
            // set Supplier attributes
            final Supplier supplier = findSupplier(
                    latestSupplierUserMessagesWithUnreadSub.get(userMessage).getSupplier().getId());
            cdcd.setSupplierId(supplier.getId());
            cdcd.setSenderId(supplier.getBusinessUser().getId());
            cdcd.setDisplayName(supplier.getBusinessUser().getBusinessUserData().getDisplayName());
            cdcd.setRating(supplier.getOveralRating().intValue());

            list.add(cdcd);
        }
        return list;
    }

    /**************************************************************************/
    /* Offers                                                                 */
    /**************************************************************************/
    /**
     * Get all demands where have been placed an offer by some supplier. When supplier place an offer to client's
     * demand, the demand will be involved here. As Client: "Demands that have already an offer."
     * As soon as first offer has been submed by some supplier we change the state of this demand to OFFERED.
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @param filter
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientOfferedDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final Client client = findClient(userId);
        final Search search = searchConverter.convertToSourceForCount(Demand.class, searchDefinition);
        search.addFilterIn("status", DemandStatus.OFFERED);
        search.addFilterEqual("client", client);
        return generalService.count(search);
    }

    /**
     * Get all demands where have been placed an offer by some supplier. When supplier place an offer to client's
     * demand, the demand will be involved here. As Client: "Demands that have already an offer."
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @param demandID - demand's ID
     * @param start
     * @param maxResult
     * @param filter
     * @param orderColumns
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<ClientDemandDetail> getClientOfferedDemands(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Search search = searchConverter.convertToSource(Demand.class, searchDefinition);
        final Map<Demand, Integer> clientDemandsWithUnreadSubMsgs = demandService
                .getClientOfferedDemandsWithUnreadOfferSubMsgs(businessUser, search);

        ArrayList<ClientDemandDetail> clientDemands = clientDemandConverter
                .convertToTargetList(clientDemandsWithUnreadSubMsgs.keySet());

        // load a map of unread messages for all client demands with offer, then iterate this map and
        // match with clientDemands so that unread submessages count will be set
        for (Map.Entry<Demand, Integer> userMessageEntry : clientDemandsWithUnreadSubMsgs.entrySet()) {
            for (ClientDemandDetail cdd : clientDemands) {
                if (cdd.getDemandId() == userMessageEntry.getKey().getId()) {
                    cdd.setUnreadMessagesCount(((Integer) userMessageEntry.getValue()));
                    break;
                }
            }
        }
        return clientDemands;
    }

    /**
     * Get all offers of given demand. When supplier place an offer to client's demand, the offer will be involved here.
     * As Client: "How many suppliers placed an offers to a certain demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @return offers count of given demand
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientOfferedDemandOffersCount(final long userId, final long demandID,
            final SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        // TODO RELEASE vojto - implement searchDefinition
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Demand demand = generalService.find(Demand.class, demandID);
        return userMessageService.getClientConversationsWithOfferCount(businessUser, demand);
    }

    /**
     * Get a list of latest user messages from suppliers who sent a offer response to given demand.
     *
     * @param userId logged client user Id
     * @param demandID demand with offer responses from suppliers
     * @param threadRootId thread root message that represents demand message made by client
     * @param searchDefinition defines a search criteria for pager etc.
     * @return list of latest user messages from suppliers with total count of submessages
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<ClientOfferedDemandOffersDetail> getClientOfferedDemandOffers(long userId, long demandID,
            long threadRootId, SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Demand demand = generalService.find(Demand.class, demandID);
        final OfferState offerPending = offerService.getOfferState(OfferStateType.PENDING.getValue());
        final Search search = searchConverter.convertToSource(UserMessage.class, searchDefinition);

        final Map<UserMessage, ClientConversation> latestClientUserMessagesWithUnreadSub =
                userMessageService.getClientConversationsWithOffer(businessUser, demand, offerPending, search);

        List<ClientOfferedDemandOffersDetail> listCodod = new ArrayList<ClientOfferedDemandOffersDetail>();

        for (UserMessage userMessage : latestClientUserMessagesWithUnreadSub.keySet()) {
            Offer offer = userMessage.getMessage().getOffer();
            // TODO LATER ivlcek - refactor and create converter
            ClientOfferedDemandOffersDetail codod = new ClientOfferedDemandOffersDetail();
            // set UserMessage attributes
            codod.setRead(userMessage.isRead());
            codod.setStarred(userMessage.isStarred());
            codod.setMessagesCount(latestClientUserMessagesWithUnreadSub.get(userMessage).getMessageCount());
            codod.setThreadRootId(userMessage.getMessage().getThreadRoot().getId());
            codod.setUserMessageId(userMessage.getId());
            // set Supplier attributes
            codod.setSupplierId(offer.getSupplier().getId());
            codod.setDisplayName(offer.getSupplier().getBusinessUser().getBusinessUserData().getDisplayName());
            codod.setRating(offer.getSupplier().getOveralRating());
            codod.setSenderId(offer.getSupplier().getBusinessUser().getId());
            // set Offer attributes
            codod.setOfferId(offer.getId());
            codod.setDemandId(offer.getDemand().getId());
            codod.setPrice(offer.getPrice());
            codod.setDeliveryDate(offer.getFinishDate());
            codod.setReceivedDate(offer.getCreated());
            // set demand attributes
            codod.setDemandTitle(demand.getTitle());

            listCodod.add(codod);
        }
        return listCodod;
    }

    /**************************************************************************/
    /*  Assigned Demands                                                      */
    /**************************************************************************/
    /**
     * Get all offers that were accepted by client to solve a demand. When client accept an offer, will be involved
     * here. As Client: "All offers that were accepted by me to solve my demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @param filter
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientAssignedDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final Client client = findClient(userId);
        final Search search = searchConverter.convertToSourceForCount(Demand.class, searchDefinition);
        search.addFilterIn("status", DemandStatus.ASSIGNED, DemandStatus.PENDINGCOMPLETION);
        search.addFilterEqual("client", client);
        return generalService.count(search);
    }

    /**
     * Get all offers that were accepted by client to solve a demand. When client accept an offer, will be involved
     * here. As Client: "All offers that were accepted by me to solve my demand."
     *
     * @param userId id of user represented by client. Note that userId and userId are different If userId represents
     * some different user than client, exception will be thrown
     * @param searchDefinition search filters, ordering
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<ClientOfferedDemandOffersDetail> getClientAssignedDemands(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Search search = searchConverter.convertToSource(UserMessage.class, searchDefinition);
        final Map<UserMessage, ClientConversation> latestClientUserMessagesWithUnreadSub =
                userMessageService.getClientConversationsWithAcceptedOffer(businessUser, search);

        List<ClientOfferedDemandOffersDetail> listCodod = new ArrayList<ClientOfferedDemandOffersDetail>();

        for (UserMessage userMessage : latestClientUserMessagesWithUnreadSub.keySet()) {
            final Offer offer = userMessage.getMessage().getOffer();
            // TODO LATER ivlcek - create converter
            ClientOfferedDemandOffersDetail codod = new ClientOfferedDemandOffersDetail();
            // set UserMessage attributes
            codod.setRead(userMessage.isRead());
            codod.setStarred(userMessage.isStarred());
            codod.setUserMessageId(userMessage.getId());
            codod.setMessagesCount(latestClientUserMessagesWithUnreadSub.get(userMessage).getMessageCount());
            codod.setThreadRootId(userMessage.getMessage().getThreadRoot().getId());
            // set Supplier attributes
            final Supplier supplier = findSupplier(
                    latestClientUserMessagesWithUnreadSub.get(userMessage).getSupplier().getId());
            codod.setSupplierId(supplier.getId());
            codod.setSenderId(supplier.getBusinessUser().getId());
            codod.setDisplayName(supplier.getBusinessUser().getBusinessUserData().getDisplayName());
            codod.setRating(supplier.getOveralRating().intValue());
            // set Offer attributes
            codod.setOfferId(offer.getId());
            codod.setPrice(offer.getPrice());
            codod.setDeliveryDate(offer.getFinishDate());
            codod.setReceivedDate(offer.getCreated());
            // set Demand attributes
            codod.setDemandId(offer.getDemand().getId());
            codod.setDemandTitle(offer.getDemand().getTitle());

            listCodod.add(codod);
        }
        return listCodod;
    }

    /**************************************************************************/
    /*  Closed Demands                                                        */
    /**************************************************************************/
    /**
     * Get all closed demands.
     * When client accept supplier's work, demand is closed and stored to demand's history.
     *
     * @param userId user's id
     * @param filter
     * @return count of closed demands
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientClosedDemandsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final Client client = findClient(userId);
        final Search search = searchConverter.convertToSourceForCount(Demand.class, searchDefinition);
        search.addFilterEqual("status", DemandStatus.CLOSED);
        search.addFilterEqual("client", client);
        return generalService.count(search);
    }

    /**
     * Get all closed demands.
     * When client accept supplier's work, demand is closed and stored to demand's history.
     *
     * @param userId user's id
     * @param searchDefinition
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<ClientOfferedDemandOffersDetail> getClientClosedDemands(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final BusinessUser businessUser = generalService.find(BusinessUser.class, userId);
        final Search search = searchConverter.convertToSource(UserMessage.class, searchDefinition);
        final Map<UserMessage, ClientConversation> latestClientUserMessagesWithUnreadSub =
                userMessageService.getClientConversationsWithClosedOffer(businessUser, search);

        List<ClientOfferedDemandOffersDetail> listCodod = new ArrayList<ClientOfferedDemandOffersDetail>();

        for (UserMessage userMessage : latestClientUserMessagesWithUnreadSub.keySet()) {
            Offer offer = userMessage.getMessage().getOffer();
            // TODO LATER ivlcek - create converter
            ClientOfferedDemandOffersDetail codod = new ClientOfferedDemandOffersDetail();
            // set UserMessage attributes
            codod.setRead(userMessage.isRead());
            codod.setStarred(userMessage.isStarred());
            codod.setUserMessageId(userMessage.getId());
            codod.setMessagesCount(latestClientUserMessagesWithUnreadSub.get(userMessage).getMessageCount());
            codod.setThreadRootId(userMessage.getMessage().getThreadRoot().getId());
            // set Supplier attributes
            codod.setSupplierId(offer.getSupplier().getId());
            codod.setDisplayName(offer.getSupplier().getBusinessUser().getBusinessUserData().getDisplayName());
            codod.setRating(offer.getSupplier().getOveralRating());
            codod.setSenderId(offer.getSupplier().getBusinessUser().getId());
            // set Offer attributes
            codod.setOfferId(offer.getId());
            codod.setPrice(offer.getPrice());
            codod.setDeliveryDate(offer.getFinishDate());
            codod.setReceivedDate(offer.getCreated());
            // set Demand attributes
            codod.setDemandId(offer.getDemand().getId());
            codod.setDemandTitle(offer.getDemand().getTitle());

            listCodod.add(codod);
        }
        return listCodod;
    }

    //******************** CLIENT - My Ratings ********************************/
    /**
     * Get ratings of my closed demands.
     *
     * @param userId user's id
     * @param filter
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public int getClientRatingsCount(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {
        final Client client = findClient(userId);
        final Search clientDemandsSearch = searchConverter.convertToSourceForCount(Demand.class, searchDefinition);
        clientDemandsSearch.addFilterEqual("status", DemandStatus.CLOSED);
        clientDemandsSearch.addFilterEqual("client", client);
        clientDemandsSearch.addFilterNotNull("rating");
        return generalService.count(clientDemandsSearch);
    }

    /**
     * Get ratings of my all closed demands.
     *
     * @param userId user's id
     * @param searchDefinition
     * @return
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public List<RatingDetail> getClientRatings(long userId,
            SearchDefinition searchDefinition) throws RPCException, ApplicationSecurityException {

        final Client client = findClient(userId);
        final OfferState offerClosed = offerService.getOfferState(OfferStateType.CLOSED.getValue());
        final OfferState offerCompleted = offerService.getOfferState(OfferStateType.COMPLETED.getValue());
        final Search supplierClosedDemandsSearch = searchConverter.convertToSource(Offer.class, searchDefinition);
        supplierClosedDemandsSearch.addFilterEqual("demand.client", client);
        supplierClosedDemandsSearch.addFilterIn("state", offerClosed, offerCompleted);
        supplierClosedDemandsSearch.addFilterNotNull("demand.rating");
        final List<Offer> offersWithRating = generalService.search(supplierClosedDemandsSearch);

        ArrayList<RatingDetail> ratings = new ArrayList<RatingDetail>();

        for (Offer offer : offersWithRating) {
            RatingDetail drd = new RatingDetail();
            Demand demand = offer.getDemand();
            drd.setDemandId(demand.getId());
            drd.setDemandTitle(demand.getTitle());
            drd.setPrice(demand.getPrice());
            ratings.add(drd);
        }
        return ratings;
    }

    /**************************************************************************/
    /* Setter methods                                                         */
    /**************************************************************************/
    /**
     * Accept selected offer and decline other offers and change demand state to ASSIGNED.
     *
     * @param offerId to be accepted
     * @param latestUserMessageId for which a new automatic reply message will be sent
     * @return void
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public void acceptOffer(final long offerId) throws RPCException, ApplicationSecurityException {
        final Offer offer = (Offer) this.generalService.find(Offer.class, offerId);

        // set offer as accepted
        offer.setState((OfferState) offerService.getOfferState(
                OfferStateType.ACCEPTED.getValue()));
        generalService.save(offer);

        // load other offers and set them as declined
        Demand demand = offer.getDemand();
        for (Offer declinedOffer : demand.getOffers()) {
            if (!declinedOffer.equals(offer)) {
                declinedOffer.setState(offerService.getOfferState(OfferStateType.DECLINED.getValue()));
                generalService.save(declinedOffer);
            }
        }
        demand.setStatus(DemandStatus.ASSIGNED);
        generalService.save(demand);
        demandService.decrementDemandCount(demand);
    }

    /**************************************************************************/
    /* HELPER METHODS                                                         */
    /**************************************************************************/
    private Client findClient(long userId) {
        final User user = generalService.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException("User with id=" + userId + " does not exist!");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            throw new IllegalStateException("User has no email. Invalid state of explication! Each user MUST HAVE"
                    + " a valid email address!");
        }
        final List<Client> clients = this.clientService.searchByCriteria(
                UserSearchCriteria.Builder.userSearchCriteria().withEmail(user.getEmail()).build());
        if (CollectionUtils.isEmpty(clients)) {
            throw new IllegalArgumentException("No client with email=" + user.getEmail() + " has been found!");
        }

        if (clients.size() > 1) {
            throw new IllegalArgumentException("One client  with email=" + user.getEmail() + " expected, but found"
                    + clients.size());
        }

        return clients.get(0);
    }

    private Supplier findSupplier(long userId) {
        final User user = generalService.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException("User with id=" + userId + " does not exist!");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            throw new IllegalStateException("User has no email. Invalid state of explication! Each user MUST HAVE"
                    + " a valid email address!");
        }
        final List<Supplier> suppliers = this.supplierService.searchByCriteria(
                UserSearchCriteria.Builder.userSearchCriteria().withEmail(user.getEmail()).build());
        if (CollectionUtils.isEmpty(suppliers)) {
            throw new IllegalArgumentException("No supplier with email=" + user.getEmail() + " has been found!");
        }

        if (suppliers.size() > 1) {
            throw new IllegalArgumentException("One supplier with email=" + user.getEmail() + " expected, but found"
                    + suppliers.size());
        }

        return suppliers.get(0);
    }

    /**
     * This method will update number of unread messages of logged user. Since this RPC class requires access of
     * authenticated user (see security-web.xml) this method will be called only when PoptavkaUserAuthentication object
     * exist in SecurityContextHolder and we can retrieve userId.
     *
     * @return UnreadMessagesDetail with number of unread messages and other info to be displayed after users logs in
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public UnreadMessagesDetail updateUnreadMessagesCount() throws RPCException, ApplicationSecurityException {
        Long userId = ((PoptavkaUserAuthentication) SecurityContextHolder.getContext().getAuthentication()).getUserId();
        Search unreadMessagesSearch = new Search(UserMessage.class);
        unreadMessagesSearch.addFilterNotNull("message.demand");
        unreadMessagesSearch.addFilterEqual("isRead", false);
        unreadMessagesSearch.addFilterEqual("user.id", userId.longValue());
        unreadMessagesSearch.addField("id", Field.OP_COUNT);
        unreadMessagesSearch.setResultMode(Search.RESULT_SINGLE);
        UnreadMessagesDetail unreadMessagesDetail = new UnreadMessagesDetail();
        unreadMessagesDetail.setUnreadMessagesCount((
                (Long) generalService.searchUnique(unreadMessagesSearch)).intValue());
        Search unreadSystemMessagesSearch = new Search(UserMessage.class);
        unreadSystemMessagesSearch.addFilterNull("message.demand");
        unreadSystemMessagesSearch.addFilterEqual("isRead", false);
        unreadSystemMessagesSearch.addFilterEqual("user.id", userId.longValue());
        unreadSystemMessagesSearch.addField("id", Field.OP_COUNT);
        unreadSystemMessagesSearch.setResultMode(Search.RESULT_SINGLE);
        unreadMessagesDetail.setUnreadSystemMessageCount((
                (Long) generalService.searchUnique(unreadSystemMessagesSearch)).intValue());
        return unreadMessagesDetail;
    }

    /**************************************************************************/
    /* CRUD operation of demand                                               */
    /**************************************************************************/
    /**
     * Upadtes demand.
     * @param demandId to be updated
     * @param updatedDemand carrying new data
     * @return updated demand detail
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public FullDemandDetail updateDemand(long demandId, FullDemandDetail updatedDemand) throws
            RPCException, ApplicationSecurityException {
        Demand demand = generalService.find(Demand.class, demandId);
        updateDemandFields(demand, updatedDemand);
        return demandConverter.convertToTarget(generalService.save(demand));
    }

    /**
     * Delete demand in a way that CANCELED <code>DemandStatus</code> is assigned to this demand. Canceled demands
     * are not dipslayed anywhere.
     *
     * @param demandId
     * @return
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public FullDemandDetail requestDeleteDemand(long demandId) throws RPCException, ApplicationSecurityException {
        Demand demand = demandService.getById(demandId);
        demand.setStatus(DemandStatus.CANCELED);
        return demandConverter.convertToTarget(demandService.update(demand));
    }

    /**
     * Updates domain demand fields.
     * @param demand to be updated
     * @param updatedDemand demand detail carrying new data
     * @return updated domain demand object
     */
    private Demand updateDemandFields(Demand demand, FullDemandDetail updatedDemand) {
        demand.setTitle(updatedDemand.getDemandTitle());
        demand.setDescription(updatedDemand.getDescription());
        demand.setPrice(updatedDemand.getPrice());
        demand.setEndDate(updatedDemand.getEndDate());
        demand.setValidTo(updatedDemand.getValidTo());
        demand.setMaxSuppliers(updatedDemand.getMaxSuppliers());
        demand.setMinRating(updatedDemand.getMinRating());
        demand.setCategories(categoryConverter.convertToSourceList(updatedDemand.getCategories()));
        demand.setLocalities(localityConverter.convertToSourceList(updatedDemand.getLocalities()));
        return demand;
    }

    /**
     * Client enters a new feedback for Supplier with respect to given demand.
     *
     * @param demandID of Demand to which this feedback is connected
     * @param supplierRating integer number that will be assigned to supplier
     * @param supplierMessage comment that will be assigned to supplier
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public void closeDemandAndEnterFeedbackForSupplier(final long demandID, final long offerID,
        final Integer supplierRating, final String supplierMessage)
        throws RPCException, ApplicationSecurityException {
        final Demand demand = generalService.find(Demand.class, demandID);
        //close demand by updating its status to closed
        demand.setStatus(DemandStatus.CLOSED);
        //set demand's rating for supplier
        Rating rating;
        if (demand.getRating() == null) {
            rating = new Rating();
        } else {
            rating = demand.getRating();
        }
        rating.setSupplierRating(supplierRating);
        rating.setSupplierMessage(supplierMessage);
        generalService.save(rating);
        demand.setRating(rating);
        generalService.save(demand);
        Offer offer = offerService.getById(offerID);
        offer.setState(offerService.getOfferState(OfferStateType.CLOSED.getValue()));
        offerService.update(offer);
        recalculateSupplierOveralRating(offer.getSupplier());
    }

    /**
     * Recalculate overall <code>Supplier</code> rating from all ratings posted by <code>Client</code>-s.
     *
     * @param supplier whose overalRating will be recalculated.
     */
    private void recalculateSupplierOveralRating(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Passed Supplier object is null");
        }
        final OfferState offerClosed = offerService.getOfferState(OfferStateType.CLOSED.getValue());
        final OfferState offerCompleted = offerService.getOfferState(OfferStateType.COMPLETED.getValue());
        final Search offersWithRatingSearch = new Search(Offer.class);
        offersWithRatingSearch.addFilterIn("state", offerClosed, offerCompleted);
        offersWithRatingSearch.addFilterEqual("supplier", supplier);
        offersWithRatingSearch.addFilterNotNull("demand.rating");
        List<Offer> offersWithRating = generalService.search(offersWithRatingSearch);

        int numberOfRatings = 0;
        int ratingSum = 0;
        for (Offer offer : offersWithRating) {
            if (offer.getDemand().getRating().getSupplierRating() != null) {
                ratingSum = ratingSum + offer.getDemand().getRating().getSupplierRating().intValue();
                numberOfRatings++;
            }
        }
        Double ratingScore = (double) ratingSum / numberOfRatings;
        supplier.setOveralRating(Integer.valueOf(ratingScore.intValue()));
        generalService.merge(supplier);
    }

    /**
     * Load all data to construct ClientDashboardDetail. Data such as number of unread messages for particular sections
     * will be retrieved.
     *
     * @param userId of Client for which dashboard object will be created
     * @return clientDashboardDetail
     * @throws RPCException
     * @throws ApplicationSecurityException
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public ClientDashboardDetail getClientDashboardDetail(long userId) throws RPCException,
    ApplicationSecurityException {
        User user = generalService.find(User.class, userId);
        ClientDashboardDetail cdd = new ClientDashboardDetail();
        cdd.setUserId(userId);
        // my Demands unread messages
        final Search search1 = new Search(UserMessage.class);
        search1.addFilterEqual("user", user);
        search1.addFilterEqual("isRead", false);
        search1.addFilterNotNull("message.demand");
        search1.addFilterEqual("message.threadRoot.sender", user);
        search1.addFilterNull("message.offer");
        search1.addField("id", Field.OP_COUNT);
        search1.setResultMode(Search.RESULT_SINGLE);
        cdd.setUnreadMessagesMyDemandsCount(((Long) generalService.searchUnique(search1)).intValue());
        // my Offered Demands unread messages
        OfferState offerPending = offerService.getOfferState(OfferStateType.PENDING.getValue());
        final Search search2 = new Search(UserMessage.class);
        search2.addFilterEqual("user", user);
        search2.addFilterEqual("isRead", false);
        search2.addFilterNotNull("message.demand");
        search2.addFilterEqual("message.threadRoot.sender", user);
        search2.addFilterNotNull("message.offer");
        search2.addFilterEqual("message.offer.state", offerPending);
        search2.addField("id", Field.OP_COUNT);
        search2.setResultMode(Search.RESULT_SINGLE);
        cdd.setUnreadMessagesOfferedDemandsCount(((Long) generalService.searchUnique(search2)).intValue());
        // my Assigned Demands unread messages
        OfferState offerAccepted = offerService.getOfferState(OfferStateType.ACCEPTED.getValue());
        OfferState offerCompleted = offerService.getOfferState(OfferStateType.COMPLETED.getValue());
        final Search search3 = new Search(UserMessage.class);
        search3.addFilterEqual("user", user);
        search3.addFilterEqual("isRead", false);
        search3.addFilterNotNull("message.demand");
        search3.addFilterEqual("message.threadRoot.sender", user);
        search3.addFilterNotNull("message.offer");
        search3.addFilterIn("message.offer.state", offerAccepted, offerCompleted);
        search3.addField("id", Field.OP_COUNT);
        search3.setResultMode(Search.RESULT_SINGLE);
        cdd.setUnreadMessagesAssignedDemandsCount(((Long) generalService.searchUnique(search3)).intValue());
        // my Closed Demands unread messages
        OfferState offerClosed = offerService.getOfferState(OfferStateType.CLOSED.getValue());
        final Search search4 = new Search(UserMessage.class);
        search4.addFilterEqual("user", user);
        search4.addFilterEqual("isRead", false);
        search4.addFilterNotNull("message.demand");
        search4.addFilterEqual("message.threadRoot.sender", user);
        search4.addFilterNotNull("message.offer");
        search4.addFilterEqual("message.offer.state", offerClosed);
        search4.addField("id", Field.OP_COUNT);
        search4.setResultMode(Search.RESULT_SINGLE);
        cdd.setUnreadMessagesClosedDemandsCount(((Long) generalService.searchUnique(search4)).intValue());

        return cdd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Secured(CommonAccessRoles.CLIENT_ACCESS_ROLE_CODE)
    public Boolean substractCredit(long offerId, int credits) throws RPCException, ApplicationSecurityException {
        Offer offer = generalService.find(Offer.class, offerId);
        BusinessUser businessUser = offer.getSupplier().getBusinessUser();
        businessUser.getBusinessUserData().substractCredits(credits);
        generalService.save(businessUser);
        return true;
    }

void nastyIfs() {
      if (true) {
        if (false) {
          if (true) {
            if (false) {
              if (true) {
                if (false) {
                }
              }
            }
          }
        }
      }
    }
}
