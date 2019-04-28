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

    public static final String advpump = "tile." + QuarryPlus.Names.advpump + ".name";
    public static final String advquarry = "tile." + QuarryPlus.Names.advquarry + ".name";
    public static final String breaker = "tile." + QuarryPlus.Names.breaker + ".name";
    //    public static final String controller = "tile." + QuarryPlus.Names.controller + ".name";
    public static final String exppump = "tile." + QuarryPlus.Names.exppump + ".name";
    public static final String frame = "tile." + QuarryPlus.Names.frame + ".name";
    public static final String laser = "tile." + QuarryPlus.Names.laser + ".name";
    public static final String marker = "tile." + QuarryPlus.Names.marker + ".name";
    public static final String miningwell = "tile." + QuarryPlus.Names.miningwell + ".name";
    public static final String mover = "tile." + QuarryPlus.Names.mover + ".name";
    public static final String moverfrombook = "tile." + QuarryPlus.Names.moverfrombook + ".name";
    public static final String placer = "tile." + QuarryPlus.Names.placer + ".name";
    //    public static final String plainpipe = "tile." + QuarryPlus.Names.plainpipe + ".name";
    public static final String pump = "tile." + QuarryPlus.Names.pump + ".name";
    public static final String quarry = "tile." + QuarryPlus.Names.quarry + ".name";
    public static final String solidquarry = "tile." + QuarryPlus.Names.solidquarry + ".name";
    //    public static final String refinery = "tile." + QuarryPlus.Names.refinery + ".name";
    public static final String workbench = "tile." + QuarryPlus.Names.workbench + ".name";

    public static final Map<Integer, String> ENCHANT_LEVELS;

    static {
        final String s = "enchantment.level.";
        Map<Integer, String> map = IntStream.rangeClosed(1, 10).mapToObj(valuesInt(i -> s + i)).collect(entryToMap());
        ENCHANT_LEVELS = Collections.unmodifiableMap(map);
    }
}
