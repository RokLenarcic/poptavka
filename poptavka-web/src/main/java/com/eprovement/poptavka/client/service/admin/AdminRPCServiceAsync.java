package com.eprovement.poptavka.client.service.admin;

import com.eprovement.poptavka.domain.enums.DemandStatus;
import com.eprovement.poptavka.shared.domain.adminModule.AdminDemandDetail;
import com.eprovement.poptavka.shared.domain.message.MessageDetail;
import com.eprovement.poptavka.shared.domain.message.UnreadMessagesDetail;
import com.eprovement.poptavka.shared.search.SearchDefinition;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import java.util.Set;

public interface AdminRPCServiceAsync {

    //---------------------- NEW DEMANDS --------------------------------------------
    void getAdminNewDemandsCount(AsyncCallback<Integer> callback);

    void getAdminNewDemands(SearchDefinition searchDefinition, AsyncCallback<List<AdminDemandDetail>> callback);

    //---------------------- ASSIGNED DEMANDS --------------------------------------------
    void getAdminAssignedDemandsCount(
        long userId, SearchDefinition searchDefinition, DemandStatus demandStatus, AsyncCallback<Integer> callback);

    void getAdminAssignedDemands(long userId, DemandStatus demandStatus, SearchDefinition searchDefinition,
        AsyncCallback<List<AdminDemandDetail>> callback);

    //---------------------- ACTIVE DEMANDS ----------------------------------------------
    void getAdminActiveDemandsCount(SearchDefinition searchDefinition, AsyncCallback<Integer> callback);

    void getAdminActiveDemands(SearchDefinition searchDefinition, AsyncCallback<List<AdminDemandDetail>> callback);

    void getConversation(long threadRootId, long loggedUserId, long counterPartyUserId,
        AsyncCallback<List<MessageDetail>> callback);

    void createConversation(long demandId, long userAdminId, AsyncCallback<Long> callback);

    void approveDemands(Set<AdminDemandDetail> demandsToApprove, AsyncCallback<Void> callback);

    //---------------------- OTHERS -------------------------------------------------------
    void updateUnreadMessagesCount(AsyncCallback<UnreadMessagesDetail> callback);
}
