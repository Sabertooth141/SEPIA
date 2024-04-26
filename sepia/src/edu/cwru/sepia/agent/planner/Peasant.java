package edu.cwru.sepia.agent.planner;

public class Peasant {
    public int id;
    public boolean have_gold;
    public boolean have_wood;
    public int cargo_amount = 0;

    public int x_pos;
    public int y_pos;
    public Position adjacent_pos;



	public void clearHoldingAmount() {
		this.have_gold = false;
		this.have_wood = false;
		this.cargo_amount = 0;
	}

    public void clear_cargo() {
		this.have_gold = false;
		this.have_wood = false;
		this.cargo_amount = 0;
	}

	public Peasant(int id, int x_pos, int y_pos, boolean have_gold, boolean have_wood, int cargo_amount, Position adjacent_pos) {
		this.id = id;
		this.x_pos = x_pos;
		this.y_pos = y_pos;
		this.have_gold = have_gold;
		this.have_wood = have_wood;
		this.cargo_amount = cargo_amount;
		this.adjacent_pos = adjacent_pos;
	}

}