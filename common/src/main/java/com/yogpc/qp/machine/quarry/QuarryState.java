package com.yogpc.qp.machine.quarry;

enum QuarryState {
    FINISHED,
    WAITING,
    BREAK_INSIDE_FRAME,
    MAKE_FRAME,
    MOVE_HEAD,
    BREAK_BLOCK,
    REMOVE_FLUID,
    FILLER,
    ;

    static boolean isWorking(QuarryState state) {
        return switch (state) {
            case FINISHED, WAITING -> false;
            default -> true;
        };
    }
}
