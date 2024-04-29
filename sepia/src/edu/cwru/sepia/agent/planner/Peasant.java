package edu.cwru.sepia.agent.planner;

public class Peasant {
    public int id;
    public boolean hasGold;
    public boolean hasWood;
    public int cargoAmount = 0;

    public int x;
    public int y;

    public Position neighbor;

    public Peasant(int id, int x, int y, boolean hasGold, boolean hasWood, int cargoAmount, Position neighbor) {
        this.id = id;
        this.hasGold = hasGold;
        this.hasWood = hasWood;
        this.cargoAmount = cargoAmount;
        this.x = x;
        this.y = y;
        this.neighbor = neighbor;
    }

    public void clearInventory() {
        this.hasGold = false;
        this.hasWood = false;
        this.cargoAmount = 0;
    }
}
