package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;
import edu.cwru.sepia.util.*;

import java.util.*;

public class Deposit_k implements StripsAction {

    int k;
    Position tHPos; 
    List<Peasant> peasants = null;
	List<Position> newPosition = null;
	List<Action> sepiaAction = new ArrayList<Action>();
	GameState parent;

    /**
     * Constructor that sets up the deposit
     * @param peasants list of all available peasants
     * @param tHPos the location of the townhall
     * @param parent the parent/current state
     */
    public Deposit_k (List<Peasant> peasants, Position tHPos, GameState parent) {
        
        // retrieve the current peasant population
        if (peasants != null) this.k = peasants.size();

        // set up the params for each peasant
        this.peasants = peasants;
        this.tHPos = tHPos;
        this.parent = parent;
        this.newPosition = new ArrayList<Position>();
        for (Peasant peasant : peasants) {
            newPosition.add(new Position(peasant.x, peasant.y));
        }
    }

    /**
     * Find if the current state meets the condition
     * @param state the current state
     * @return the flag which indicates whether peasant can deposit
     */
	@Override
	public boolean preconditionsMet (GameState state) {
        // traverse all peasant and if their don't have anything or they are not by the townhall, return false
		for (int i = 0; i < k; i++) {
			if (peasants.get(i).cargoAmount <= 0) return false;
            else if (!legalPosition(newPosition.get(i))) return false;
            }
		return true;
	}

    /**
	 * Util method that tells if a certain position is legal for a peasant to deposit their cargo
	 * @param pos the position of the current peasant
	 * @return whether it is leagal
	 */
	private boolean legalPosition(Position pos) {
		
        // if out of map or overlapping with other game objects, return false
		if (pos.x < 0 || pos.x >= parent.getxExtent() || pos.y < 0 ||pos.y >= parent.getyExtent() 
        || parent.getMap()[pos.x][pos.y]
        || (Math.abs(pos.x - tHPos.x) > 1 || Math.abs(pos.y - tHPos.y) > 1)) return false;
        // if peasant is around the townhall
        else return true;
	}
	

    /**
	 * Execute the action to the given state
	 * @param state the given current state to deposit
	 * @return the new gameState 
	 */
	@Override
	public GameState apply(GameState state) {

		GameState newGameState = new GameState(state);

        // traverse all peasents to add their cargo to total inventory
        for (int i = 0; i < k; i++) {
            Peasant peasant = newGameState.findPeasant(peasants.get(i).id, newGameState.getPeasantUnits());
            if (peasant.hasGold) newGameState.addGold(peasant.cargoAmount);
            else if (peasant.hasWood) newGameState.addWood(peasant.cargoAmount);    
            peasant.clearInventory();
            sepiaAction.add(Action.createPrimitiveDeposit(peasant.id, getDirection(peasant.x, peasant.y, tHPos.x, tHPos.y)));
}
        //add plan and update heuristics
        newGameState.addCost(1);
        newGameState.heuristic();
        newGameState.addPlan(this);

		return newGameState;
	}

    private Direction getDirection(int originalX, int originalY, int currentX, int currentY) {
        for (Direction direction : Direction.values()) {
            if((currentX - originalX) == direction.xComponent() && (currentY - originalY) == direction.yComponent()) {
                return direction;
            }
        }
        return null;
    }

    @Override
    public GameState getParent() {
        return this.parent;
    }

	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
	}


}