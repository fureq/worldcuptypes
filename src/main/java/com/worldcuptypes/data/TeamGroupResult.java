package com.worldcuptypes.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public class TeamGroupResult implements Comparable<TeamGroupResult>{
    private Team team;
    private int points = 0;
    private int goalsScored = 0;
    private int goalsLoosed = 0;

    public TeamGroupResult(Team team) {
        this.team = team;
    }

    private int getGoalsDiff() {
        return goalsScored - goalsLoosed;
    }

    public void win() {
        this.points+=3;
    }

    public void draw() {
        this.points+=1;
    }

    public void updateGoals(int score, int lose) {
        this.goalsScored += score;
        this.goalsLoosed += lose;
    }

    public String print() {
        return team + "\t" + points + "\t" + getGoalsDiff() + "\t" + goalsScored + "\t" + goalsLoosed;
    }

    @Override
    public int compareTo(TeamGroupResult other) {
        if (other == null) {
            return 1;
        }
        if (points > other.points) {
            return 1;
        } else if (points < other.points) {
            return -1;
        } else if (getGoalsDiff() > other.getGoalsDiff()) {
            return 1;
        } else if (getGoalsDiff() < other.getGoalsDiff()) {
            return -1;
        } else return Integer.compare(goalsScored, other.goalsScored);
    }
}
