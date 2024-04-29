package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.util.*;

import java.util.*;

public class Harvest_k implements StripsAction {

    int k;
    Position resourcePos;

    List<Peasant> peasants = null;
    List<Position> newPosition = null;

    List<Action> sepiaAction = new ArrayList<Action>();
    GameState parent;

    /**
     * Contructor to conduct a move
     * @param peasants list peasants available 
     * @param resourcePos position of resource to be harvested
     * @param parent
     */
    public Harvest_k (List<Peasant> peasants, Position resourcePos, GameState parent) {
        // retrieve the current population of peasant
        if (peasants != null) {
            this.k = peasants.size();
        }
        this.peasants = peasants;
        this.resourcePos = resourcePos;
        this.parent = parent;
        this.newPosition = new ArrayList<Position>();
        for (Peasant p : peasants) {
            newPosition.add(new Position(p.x, p.y));
        }
    }

    /**
     * Check if Peasant can harvest at given state
     * @param state the current GameState
     * @return if you can harvest
     */
    @Override
	public boolean preconditionsMet(GameState state) {
        for (int i = 0; i < k; i++) {
            try {    
                if (peasants.get(i).cargoAmount != 0) return false;
                else if (!legalPosition(newPosition.get(i))) return false;
                //possible null pointer exception
            } catch (Exception e){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Util Method to find if it's good position
     * @param pos the expected position to go
     * @return if it is legal
     */
    private boolean legalPosition(Position pos) {
        // false if out of the bound
        if (pos.x < 0 || pos.x >= parent.getxExtent() || pos.y < 0 || pos.y >= parent.getyExtent()) return false;
        // false if overlapping with other object
        else if (parent.getMap()[pos.x][pos.y]) return false;
        // false if too far from resource
        else if (Math.abs(pos.x - resourcePos.x) > 1 || Math.abs(pos.y - resourcePos.y) > 1)  return false;
        return true;
    }

    /** Execute the movemenet
     * @param state the current state
     * @return the new state after harvest
     */
    @Override
	public GameState apply(GameState state) {

        GameState newGameState = new GameState(state);
        
        // traverse all available peasants
		for (int i = 0; i < k; i++) {
			Peasant peasant = newGameState.findPeasant(peasants.get(i).id, newGameState.getPeasantUnits());
            int x = resourcePos.x;
            int y = resourcePos.y;
            // find the larger available resouce get given point, avoid extra clause to find the type
			int resourceAmount = Math.max(newGameState.getGoldMap()[x][y], newGameState.getWoodMap()[x][y]);
			ResourceView originalResource = state.findResource(x, y, state.getResourceNodes());
			ResourceNode currentResource = null;
			ResourceNode.Type type = originalResource.getType();
            boolean isGold = type == ResourceNode.Type.GOLD_MINE;
            boolean isWood = type == ResourceNode.Type.TREE;
            // from the type got from originalResource, designate the correct map
            int[][] map = isGold ? newGameState.getGoldMap() : newGameState.getWoodMap();
            boolean[][] gameStateMap = newGameState.getMap();
            // if the resource is <= 100, take and disable the resouce node
            if (resourceAmount <= 100) {
                gameStateMap[x][y] = false;
                map[x][y] = -1;
                peasant.cargoAmount = resourceAmount;
                resourceAmount = 0;
            // or take 100 and add to the cargo of the peasant
            } else {
                map[x][y] -= 100;
                resourceAmount -= 100;
                peasant.cargoAmount = 100;
            }

            // assign correct flag to the peasant
            peasant.hasGold = isGold;
            peasant.hasWood = isWood;

            currentResource = new ResourceNode(type, x, y, resourceAmount, originalResource.getID());

			newGameState.getResourceNodes().remove(originalResource);
			newGameState.getResourceNodes().add(new ResourceView(currentResource));

			sepiaAction.add(Action.createPrimitiveGather(peasant.id, getDirection(peasant.x, peasant.y, x, y)));
		}

		newGameState.addCost(1);
		newGameState.heuristic();
		newGameState.addPlan(this);

		return newGameState;
	}

	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
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
}