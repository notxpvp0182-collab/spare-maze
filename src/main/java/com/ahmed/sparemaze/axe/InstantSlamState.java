package com.ahmed.sparemaze.axe;

public enum InstantSlamState {
    IDLE,
    VALIDATING_TARGET,
    SWITCHING_TO_AXE,
    BREAKING_SHIELD,
    WAITING_SHIELD_BREAK,
    SWITCHING_TO_MACE,
    MACE_HIT,
    RESTORING,
    COMPLETED,
    FAILED
}
