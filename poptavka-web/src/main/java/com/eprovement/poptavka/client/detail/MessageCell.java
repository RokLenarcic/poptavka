/*
 * Copyright (C), eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.client.detail;

import com.eprovement.poptavka.client.common.session.Storage;
import com.eprovement.poptavka.shared.domain.message.MessageDetail;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiRenderer;
import java.util.Date;

/**
 * Displays messageDetail as message cell in conversation panel widget's list .
 *
 * @author Martin Slavkovsky
 */
public class MessageCell extends AbstractCell<MessageDetail> {

    /**************************************************************************/
    /* UiRenderer                                                             */
    /**************************************************************************/
    private static MyUiRenderer renderer = GWT.create(MyUiRenderer.class);

    interface MyUiRenderer extends UiRenderer {

        void render(SafeHtmlBuilder sb, String cssColor, String sender, String sent, String body);

        void onBrowserEvent(MessageCell o, NativeEvent e, Element p, MessageDetail n);

        PreElement getBodySpan(Element parent);
    }

    /**************************************************************************/
    /* Attributes                                                             */
    /**************************************************************************/
    private boolean open;

    /**************************************************************************/
    /* Initialization                                                         */
    /**************************************************************************/
    /**
     * Initializes message cell.
     */
    public MessageCell() {
        super(BrowserEvents.CLICK);
    }

    /**************************************************************************/
    /* Overriden methods                                                      */
    /**************************************************************************/
    /**
     * Renders message cell's view.
     * @param value - message detail
     */
    @Override
    public void render(Context context, MessageDetail value, SafeHtmlBuilder sb) {
        String cssColor;
        if (Storage.getUser().getUserId() == value.getSenderId()) {
            cssColor = Storage.RSCS.details().conversationDetailHeaderRed();
        } else {
            cssColor = Storage.RSCS.details().conversationDetailHeaderGreen();
        }
        renderer.render(sb, cssColor, value.getSender(),
                getSentText(value.getSent()), value.getBody());
    }

    /**
     * Handles browser events.
     * @param value - message detail
     */
    @Override
    public void onBrowserEvent(Context context, Element parent, MessageDetail value,
            NativeEvent event, ValueUpdater<MessageDetail> valueUpdater) {
        if (open) {
            renderer.getBodySpan(parent).addClassName(Storage.RSCS.details().conversationEllipsis());
        } else {
            renderer.getBodySpan(parent).removeClassName(Storage.RSCS.details().conversationEllipsis());
        }
        open = !open;
        renderer.onBrowserEvent(this, event, parent, value);
    }

    /**************************************************************************/
    /* Helper methods                                                         */
    /**************************************************************************/
    /**
     * Gets formated date.
     * @param sent date
     * @return string representation of given date
     */
    private String getSentText(Date sent) {
        if (sent == null) {
            return "";
        } else {
            return Storage.get().getDateTimeFormat().format(sent);
        }
    }

}
