package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.util.*;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.*;

public class DepositK implements StripsAction {

    int k;
    Position townHallPosition;
	// Position currentPosition;
    List<Peasant> peasants = null;
	List<Position> newPosition = null;

    // UnitView unit;
	List<Action> sepiaAction = new ArrayList<Action>();
	GameState parent;
    // Boolean forGold;



    public DepositK (List<Peasant> peasants, Position townHallPosition, GameState parent) {
        if (peasants != null) {
            this.k = peasants.size();
        }
        this.peasants = peasants;
        this.townHallPosition = townHallPosition;
        this.parent = parent;

        this.newPosition = new ArrayList<Position>();
        for (Peasant p : peasants) {
            newPosition.add(new Position(p.x_pos, p.y_pos));
        }
    }


    private boolean legal_pos(Position pos) {
        if (pos.x < 0 || pos.x >= parent.getxExtent() || pos.y < 0 || pos.y >= parent.getyExtent()) {
            return false;
        }
        else if (parent.getMap()[pos.x][pos.y]) { //resource
            return false;
        }
        else if (Math.abs(pos.x - townHallPosition.x) > 1 || Math.abs(pos.y - townHallPosition.y) > 1) {
            return false;
        }
        else {
            return true;
        }
    }


    private Direction getDirection(int originalX, int originalY, int currentX, int currentY) {
        for (Direction d : Direction.values()) {
            if((currentX - originalX) == d.xComponent() && (currentY - originalY) == d.yComponent()) {
                return d;
            }
        }
        return null;
    }


    @Override
    public GameState getParent() {
        return this.parent;
    }

	@Override
	public boolean preconditionsMet (GameState state) {
		boolean flag = true;
		for (int i = 0; i < k; i++) {
			if (peasants.get(i).cargo_amount <= 0) {
                flag = false;
            }
            else if (!legal_pos(newPosition.get(i))) {
                flag = false;
            }
		}
		return flag;
	}


	@Override
	public GameState apply(GameState state) {

		GameState result = new GameState(state);

		for (int i = 0; i < k; i++) {
			Peasant p = result.findPeasant(peasants.get(i).id, result.getPeasantUnits());

            if (p.have_gold) {
                result.addGold(p.cargo_amount);
            }
            else if (p.have_wood) {
                result.addWood(p.cargo_amount);
            }
            p.clear_cargo();

            sepiaAction.add(Action.createPrimitiveDeposit(p.id, getDirection(p.x_pos, p.y_pos, townHallPosition.x, townHallPosition.y)));
            // return result;
		}
        result.addCost(1);
        result.heuristic();
        result.addPlan(this);

		return result;
	}


	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
	}


}