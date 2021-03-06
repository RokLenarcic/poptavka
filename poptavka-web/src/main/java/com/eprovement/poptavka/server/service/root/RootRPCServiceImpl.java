/*
 * Copyright (C) 2014, eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.server.service.root;

import com.eprovement.poptavka.client.service.root.RootRPCService;
import com.eprovement.poptavka.domain.settings.NotificationItem;
import com.eprovement.poptavka.domain.user.BusinessUser;
import com.eprovement.poptavka.domain.user.User;
import com.eprovement.poptavka.server.service.AutoinjectingRemoteService;
import com.eprovement.poptavka.service.GeneralService;
import com.eprovement.poptavka.shared.exceptions.RPCException;
import com.googlecode.genericdao.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author Martin Slavkovsky
 * @since 9.4.2014
 */
@Configurable
public class RootRPCServiceImpl extends AutoinjectingRemoteService implements RootRPCService {

    @Autowired
    private GeneralService generalService;

    @Override
    public Boolean unsubscribe(String password) throws RPCException {
        Search search = new Search(User.class);
        search.addFilterEqual("password", password);
        User user = (User) generalService.searchUnique(search);
        for (NotificationItem notificationItem : user.getSettings().getNotificationItems()) {
            notificationItem.setEnabled(Boolean.FALSE);
        }
        generalService.save(user);
        return true;
    }

    @Override
    public Integer getCreditCount(long userId) throws RPCException {
        BusinessUser user = generalService.find(BusinessUser.class, userId);
        return user.getBusinessUserData().getCurrentCredits();
    }
}
