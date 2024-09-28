package com.yogpc.qp.machine.advquarry;

enum AdvQuarryState {
    FINISHED,
    WAITING,
    MAKE_FRAME,
    BREAK_BLOCK,
    CLEAN_UP,
    ;

    static boolean isWorking(AdvQuarryState state) {
        return switch (state) {
            case FINISHED, WAITING -> false;
            default -> true;
        };
    }
}
