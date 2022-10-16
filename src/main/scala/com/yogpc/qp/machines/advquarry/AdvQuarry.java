package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.QuarryPlus;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

final class AdvQuarry {
    static final Logger LOGGER = QuarryPlus.getLogger(AdvQuarry.class);
    static final Marker ACTION = MarkerManager.getMarker("AdvQuarryAction");
    static final Marker MESSAGE = MarkerManager.getMarker("AdvActionMessage");
    static final Marker BLOCK = MarkerManager.getMarker("BlockAdvQuarry");
}
