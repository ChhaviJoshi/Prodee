package com.chhavi.prodee.productivity.entity;

public enum TaskDifficulty {
    EASY(10, 5),
    MEDIUM(25, 15),
    HARD(50, 30),
    EPIC(100, 60);

    private final int xpReward;
    private final int coinReward;

    TaskDifficulty(int xpReward, int coinReward) {
        this.xpReward = xpReward;
        this.coinReward = coinReward;
    }

    public int getXpReward() { return xpReward; }
    public int getCoinReward() { return coinReward; }
}
