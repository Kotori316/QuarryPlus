package com.yogpc.qp.machines;

import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;

import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.valuesInt;

@SuppressWarnings("SpellCheckingInspection")
public class TranslationKeys {
    public static final String ADD = "tof.add_new_ore";
    public static final String ADD_FLUID_SURE = "tof.add_fluid_sure";
    public static final String ALREADY_REGISTERED_ERROR = "tof.already_error";
    public static final String BLACKLIST = "quarryplus.gui.blacklist";
    public static final String CANCEL = "gui.cancel";
    public static final String CHANGEMODE = "quarryplus.chat.change_mode";
    public static final String CHANGE_DIRECTION = "pp.change";
    public static final String CONTAIN_ITEM = "quarryplus.status.item";
    public static final String CONTAIN_FLUID = "quarryplus.status.fluid";
    public static final String CONTAINER_INVENTORY = "container.inventory";
    public static final String COPY_FROM_OTHER_DIRECTION = "pp.copy";
    public static final String COPY_SELECT = "pp.copy.select";
    public static final String CURRENT_MODE = "quarryplus.chat.current_mode";
    public static final String DELETE = "selectServer.delete";
    public static final String DELETE_BLOCK_SURE = "tof.delete_block_sure";
    public static final String DELETE_FLUID_SURE = "tof.delete_fluid_sure";
    public static final String DISABLE_MESSAGE = "quarryplus.chat.disable_message";
    public static final String DONE = "gui.done";
    public static final String DOWN = "FD.down";
    public static final String EAST = "FD.east";
    public static final String EMPTY_ITEM = "quarryplus.status.item_empty";
    public static final String EMPTY_FLUID = "quarryplus.status.fluid_empty";
    public static final String ENCHANTMENT = "quarryplus.status.enchantment";
    public static final String FILLER_MODE = "quarryplus.chat.filler_mode";
    public static final String FLUID_ID = "tof.fluid_id";
    public static final String FROM_LIST = "tof.from_list";
    public static final String GO_BOTTOM = "tof.bottom";
    public static final String GO_DOWN = "tof.down";
    public static final String GO_TOP = "tof.top";
    public static final String GO_UP = "tof.up";
    public static final String INDENT = "quarryplus.chat.indent";
    public static final String LIQUID_FORMAT = "yog.pump.liquid";
    public static final String LIST_SETTING = "pp.list.setting";
    public static final String MACHINE_BUFFER = "container.yog.basic";
    public static final String MANUAL_INPUT = "tof.manual_input";
    public static final String MARKER_AREA = "quarryplus.chat.marker_area";
    public static final String NORTH = "FD.north";
    public static final String NEW_ENTRY = "quarryplus.gui.new_entry";
    public static final String OF = "quarryplus.gui.of";
    public static final String PLACER_RS = "quarryplus.chat.placer_rs";
    public static final String PLUS_ENCHANTMENT = "quarryplus.chat.plus_enchantments";
    public static final String PLUS_NO_ENCHANTMENTS = "quarryplus.chat.no_enchantments";
    public static final String PUMP_CONTAIN = "quarryplus.chat.pump_contain";
    public static final String PUMP_CONTAIN_NO = "quarryplus.chat.chat.pump_not_contain";
    public static final String PUMP_RANGE_NUM = "quarryplus.chat.pump_range.num";
    public static final String PUMP_RANGE_QUARRY = "quarryplus.chat.pump_range.quarry";
    public static final String QP_ENABLE_LIST = "qp.list.setting";
    public static final String QUARRY_MODE = "quarryplus.chat.quarry_mode";
    public static final String QUARRY_RESTART = "quarryplus.chat.quarry.restart";
    public static final String RECEIVES = "quarryplus.status.receive";
    public static final String REQUIRES = "quarryplus.status.require";
    public static final String SELECT_FLUID = "tof.select_fluid";
    public static final String SET_SELECT = "pp.set.select";
    public static final String SOUTH = "FD.south";
    public static final String TOF_ADDED = "tof.success";
    public static final String TOF_EXCLUDE = "tof.exclude";
    public static final String TOF_INCLUDE = "tof.include";
    public static final String UP = "FD.up";
    public static final String WEST = "FD.west";
    public static final String WHITELIST = "quarryplus.gui.whitelist";
    public static final String Y_LEVEL = "quarryplus.chat.y_level";
    public static final String YOG_SPAWNER_SETTING = "yog.spawner.setting";

    public static final String TOOLTIP_PUMP = "quarryplus.tooltip.blockpump";
    public static final String TOOLTIP_EXPPUMP = "quarryplus.tooltip.exppump";
    public static final String TOOLTIP_REPLACER = "quarryplus.tooltip.replacer";
    public static final String TOOLTIP_ADVPUMP = "quarryplus.tooltip.advpump";
    public static final String TOOLTIP_PLACER_PLUS = "quarryplus.tooltip.placer_plus";

    public static final String advpump = "block.quarryplus." + QuarryPlus.Names.advpump;
    public static final String advquarry = "block.quarryplus." + QuarryPlus.Names.advquarry;
    public static final String breaker = "block.quarryplus." + QuarryPlus.Names.breaker;
    //    public static final String controller = "block.quarryplus." + QuarryPlus.Names.controller ;
    public static final String exppump = "block.quarryplus." + QuarryPlus.Names.exppump;
    public static final String frame = "block.quarryplus." + QuarryPlus.Names.frame;
    public static final String laser = "block.quarryplus." + QuarryPlus.Names.laser;
    public static final String marker = "block.quarryplus." + QuarryPlus.Names.marker;
    public static final String mini_quarry = "block.quarryplus." + QuarryPlus.Names.mini_quarry;
    public static final String miningwell = "block.quarryplus." + QuarryPlus.Names.miningwell;
    public static final String mover = "block.quarryplus." + QuarryPlus.Names.mover;
    public static final String moverfrombook = "block.quarryplus." + QuarryPlus.Names.moverfrombook;
    public static final String placer = "block.quarryplus." + QuarryPlus.Names.placer;
    //    public static final String plainpipe = "block.quarryplus." + QuarryPlus.Names.plainpipe ;
    public static final String pump = "block.quarryplus." + QuarryPlus.Names.pump;
    public static final String quarry = "block.quarryplus." + QuarryPlus.Names.quarry;
    public static final String quarry2 = "block.quarryplus." + QuarryPlus.Names.quarry2;
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
