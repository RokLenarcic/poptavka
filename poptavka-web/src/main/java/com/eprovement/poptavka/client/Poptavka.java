/*
 * Copyright (C), eProvement s.r.o. All rights reserved.
 */
package com.eprovement.poptavka.client;


import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.Mvp4gModule;

/**
 * Main entry point.
 *
 * @author Beho
 */
public class Poptavka implements EntryPoint {

    private static final Logger LOGGER = Logger.getLogger("poptavka");

    @Override
    public void onModuleLoad() {
        LOGGER.info("Loading ...");
        Mvp4gModule module =  GWT.create(Mvp4gModule.class);
        module.createAndStartModule();
        LOGGER.info("Loading done ... ");
        RootLayoutPanel.get().add((Widget) module.getStartView());
    }
}
