/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.poptavka.sample.client.service.demand;

import com.google.gwt.user.client.rpc.AsyncCallback;
import cz.poptavka.sample.shared.domain.UserDetail;
import cz.poptavka.sample.shared.domain.demand.FullDemandDetail;

/**
 *
 * @author praso
 */
public interface DemandCreationModuleRPCServiceAsync {

    void createNewDemand(FullDemandDetail newDemand, Long clientId,
            AsyncCallback<FullDemandDetail> callback);

    void createNewClient(UserDetail clientDetail, AsyncCallback<UserDetail> callback);

    void verifyClient(UserDetail client, AsyncCallback<UserDetail> callback);

    void checkFreeEmail(String email, AsyncCallback<Boolean> callback);

}
