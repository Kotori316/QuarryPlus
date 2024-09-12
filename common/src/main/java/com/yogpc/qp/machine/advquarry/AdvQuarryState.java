package com.yogpc.qp.machine.advquarry;

enum AdvQuarryState {
    FINISHED,
    WAITING,
    ;

    static boolean isWorking(AdvQuarryState state) {
        return switch (state) {
            case FINISHED, WAITING -> false;
            default -> true;
        };
    }
}
