package com.yogpc.qp.machines;

import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;

import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.valuesInt;

@SuppressWarnings("SpellCheckingInspection")
public class TranslationKeys {
    public static final String ADD = "tof.addnewore";
    public static final String ADD_FLUID_SURE = "tof.addfluidsure";
    public static final String ALREADY_REGISTERED_ERROR = "tof.alreadyerror";
    public static final String CANCEL = "gui.cancel";
    public static final String CHANGEMODE = "chat.changemode";
    public static final String CHANGE_DIRECTION = "pp.change";
    public static final String CONTAINER_INVENTORY = "container.inventory";
    public static final String COPY_FROM_OTHER_DIRECTION = "pp.copy";
    public static final String COPY_SELECT = "pp.copy.select";
    public static final String CURRENT_MODE = "chat.currentmode";
    public static final String DELETE = "selectServer.delete";
    public static final String DELETE_BLOCK_SURE = "tof.deleteblocksure";
    public static final String DELETE_FLUID_SURE = "tof.deletefluidsure";
    public static final String DONE = "gui.done";
    public static final String DOWN = "FD.down";
    public static final String EAST = "FD.east";
    public static final String FILLER_MODE = "chat.fillermode";
    public static final String FLUID_ID = "tof.fluidid";
    public static final String FROM_LIST = "tof.fromlist";
    public static final String GO_BOTTOM = "tof.bottom";
    public static final String GO_DOWN = "tof.down";
    public static final String GO_TOP = "tof.top";
    public static final String GO_UP = "tof.up";
    public static final String INDENT = "chat.indent";
    public static final String LIQUID_FORMAT = "yog.pump.liquid";
    public static final String LIST_SETTING = "pp.list.setting";
    public static final String MACHINE_BUFFER = "container.yog.basic";
    public static final String MANUAL_INPUT = "tof.manualinput";
    public static final String MARKER_AREA = "chat.markerarea";
    public static final String NORTH = "FD.north";
    public static final String PLUSENCHANT = "chat.plusenchant";
    public static final String PLUSENCHANTNO = "chat.plusenchantno";
    public static final String PUMP_CONTAIN = "chat.pumpcontain";
    public static final String PUMP_CONTAIN_NO = "chat.pumpcontainno";
    public static final String PUMP_RTOGGLE_NUM = "chat.pump_rtoggle.num";
    public static final String PUMP_RTOGGLE_QUARRY = "chat.pump_rtoggle.quarry";
    public static final String QP_ENABLE_LIST = "qp.list.setting";
    public static final String QUARRY_MODE = "chat.quarrymode";
    public static final String SELECT_FLUID = "tof.selectfluid";
    public static final String SET_SELECT = "pp.set.select";
    public static final String SOUTH = "FD.south";
    public static final String TOF_ADDED = "tof.success";
    public static final String TOF_EXCLUDE = "tof.exclude";
    public static final String TOF_INCLUDE = "tof.include";
    public static final String UP = "FD.up";
    public static final String WEST = "FD.west";
    public static final String Y_LEVEL = "chat.ylevel";
    public static final String YOG_SPAWNER_SETTING = "yog.spanwer.setting";

    public static final String TOOLTIP_PUMP = "quarryplus.tooltip.blockpump";
    public static final String TOOLTIP_EXPPUMP = "quarryplus.tooltip.exppump";
    public static final String TOOLTIP_REPLACER = "quarryplus.tooltip.replacer";
    public static final String TOOLTIP_ADVPUMP = "quarryplus.tooltip.advpump";

    public static final String advpump = "block.quarryplus." + QuarryPlus.Names.advpump;
    public static final String advquarry = "block.quarryplus." + QuarryPlus.Names.advquarry;
    public static final String breaker = "block.quarryplus." + QuarryPlus.Names.breaker;
    //    public static final String controller = "block.quarryplus." + QuarryPlus.Names.controller ;
    public static final String exppump = "block.quarryplus." + QuarryPlus.Names.exppump;
    public static final String frame = "block.quarryplus." + QuarryPlus.Names.frame;
    public static final String laser = "block.quarryplus." + QuarryPlus.Names.laser;
    public static final String marker = "block.quarryplus." + QuarryPlus.Names.marker;
    public static final String miningwell = "block.quarryplus." + QuarryPlus.Names.miningwell;
    public static final String mover = "block.quarryplus." + QuarryPlus.Names.mover;
    public static final String moverfrombook = "block.quarryplus." + QuarryPlus.Names.moverfrombook;
    public static final String placer = "block.quarryplus." + QuarryPlus.Names.placer;
    //    public static final String plainpipe = "block.quarryplus." + QuarryPlus.Names.plainpipe ;
    public static final String pump = "block.quarryplus." + QuarryPlus.Names.pump;
    public static final String quarry = "block.quarryplus." + QuarryPlus.Names.quarry;
    public static final String solidquarry = "block.quarryplus." + QuarryPlus.Names.solidquarry;
    //    public static final String refinery = "block.quarryplus." + QuarryPlus.Names.refinery ;
    public static final String replacer = "block.quarryplus." + QuarryPlus.Names.replacer;
    public static final String workbench = "block.quarryplus." + QuarryPlus.Names.workbench;

    public static final Map<Integer, String> ENCHANT_LEVELS;

    static {
        final String s = "enchantment.level.";
        Map<Integer, String> map = IntStream.rangeClosed(1, 10).mapToObj(valuesInt(i -> s + i)).collect(entryToMap());
        ENCHANT_LEVELS = Collections.unmodifiableMap(map);
    }
}
