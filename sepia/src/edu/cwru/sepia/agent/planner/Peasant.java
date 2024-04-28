package edu.cwru.sepia.agent.planner;

public class Peasant {
    public int id;
    public boolean has_gold;
    public boolean has_wood;
    public int cargo_amount = 0;

    public int x;
    public int y;

    public Position neighbor;

    public Peasant(int id, boolean has_gold, boolean has_wood, int cargo_amount, int x, int y, Position neighbor) {
        this.id = id;
        this.has_gold = has_gold;
        this.has_wood = has_wood;
        this.cargo_amount = cargo_amount;
        this.x = x;
        this.y = y;
        this.neighbor = neighbor;
    }

    public void clearInventory() {
        this.has_gold = false;
        this.has_wood = false;
        this.cargo_amount = 0;
    }
}
