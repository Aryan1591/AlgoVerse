package com.algoverse.platform.entity;

public enum Category {
    EASY(3),
    MEDIUM(5),
    HARD(8);

    private final int score;

    Category(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}

