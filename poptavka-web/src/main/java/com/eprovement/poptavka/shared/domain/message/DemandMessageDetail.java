package com.eprovement.poptavka.shared.domain.message;

import com.eprovement.poptavka.shared.domain.demand.DemandTypeDetail;
import java.math.BigDecimal;
import java.util.Date;

import com.eprovement.poptavka.domain.enums.DemandStatus;
import com.google.gwt.user.client.rpc.IsSerializable;

public class DemandMessageDetail extends MessageDetail implements IsSerializable {

    private String demandTitle;
    //Asi bude robit problem, inde mi uz robilo - nechcelo predat na RPC servisu
    private BigDecimal price;
    private Date endDate;
    private Date validToDate;
    private long demandId;
    private int messageCount;
    private int unreadSubmessages;
    private DemandTypeDetail demandType;
    private DemandStatus demandStatus;

    public String getDemandTitle() {
        return demandTitle;
    }

    public void setDemandTitle(String demandTitle) {
        this.demandTitle = demandTitle;
    }

    public void setDemandStatus(DemandStatus status) {
        this.demandStatus = status;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setValidToDate(Date validTo) {
        this.validToDate = validTo;
    }

    public Date getValidTo() {
        return validToDate;
    }

    public void setPrice(BigDecimal price) {
        if (price == null) {
            this.price = BigDecimal.ZERO;
        } else {
            this.price = price;
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public long getDemandId() {
        return demandId;
    }

    public void setDemandId(long demandId) {
        this.demandId = demandId;
    }

    public DemandTypeDetail getDemandType() {
        return demandType;
    }

    public void setDemandType(DemandTypeDetail demandType) {
        this.demandType = demandType;
    }

    public String getDemandPrice() {
        return price.toString();
    }

    public DemandStatus getDemandStatus() {
        return demandStatus;
    }

    public Date getExpireDate() {
        return null;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public int getUnreadSubMessages() {
        return unreadSubmessages;
    }

    public void setUnreadSubmessages(int unreadSubmessages) {
        this.unreadSubmessages = unreadSubmessages;
    }
}
