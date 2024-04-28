package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;

public class Harvest implements StripsAction {

    Position currentPosition;

    UnitView unit;
    Action action;
    GameState parent;
    Boolean forGold;

    public Harvest(Position currentPosition, UnitView unit, GameState parent, Boolean forGold) {
        this.currentPosition = currentPosition;
        this.unit = unit;
        this.parent = parent;
        this.forGold = forGold;
    }

    @Override
    public GameState getParent() {
        return this.parent;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        int x = currentPosition.x;
        int y = currentPosition.y;

        if (x < 0 || x >= state.getXExtent() || y < 0 || y >= state.getYExtent()) {
            return false;  //check if out of the bound of the map
        } else {
            if (this.forGold) {
                return state.getGoldMap()[x][y] > 0 && unit.getCargoAmount() == 0;
            } else {
                return state.getWoodMap()[x][y] > 0 && unit.getCargoAmount() == 0;
            }
        }
    }

    @Override
    public GameState apply(GameState state) {
        GameState result = new GameState(state);        // need a constructor
        int x = currentPosition.x;
        int y = currentPosition.y;

        int amount = 0;
        if (this.forGold) {
            amount = result.getGoldMap()[x][y];
        } else {
            amount = result.getWoodMap()[x][y];
        }

        UnitView originalUnit = result.findUnit(unit.getID(), result.getPlayerUnits()); // need to implement findUnit() and getPlayerUnits in GameState
        UnitTemplate currentTemplate = new UnitTemplate(originalUnit.getID());
        currentTemplate.setCanGather(true);

        Unit currentUnit = new Unit(currentTemplate, originalUnit.getID());
        currentUnit.setxPosition(unit.getXPosition());
        currentUnit.setyPosition(unit.getYPosition());

        ResourceNode.ResourceView originalResource = result.findResource(x, y, result.getResourceNodes());
        ResourceNode currentResource;

        // If resource location has less than 100, take all into cargo, or take 100
        if (this.forGold) {
            if (amount < 100) {
                result.getGoldMap()[x][y] = 0;
                currentUnit.setCargo(ResourceType.GOLD, amount);
                amount = 0;
            } else {
                result.getGoldMap()[x][y] -= 100;
                currentUnit.setCargo(ResourceType.GOLD, 100);
                amount -= 100;
            }
            // set the resource point to the correct amount, similar steps repeated below for woods
            currentResource = new ResourceNode(ResourceNode.Type.GOLD_MINE, x, y, amount, originalResource.getID());
        } else {
            if (amount < 100) {
                result.getWoodMap()[x][y] = 0;
                currentUnit.setCargo(ResourceType.WOOD, amount);
                amount = 0;
            } else {
                result.getWoodMap()[x][y] -= 100;
                currentUnit.setCargo(ResourceType.WOOD, 100);
                amount -= 100;
    	    }
            currentResource = new ResourceNode(ResourceNode.Type.TREE, x, y, amount, originalResource.getID());
        }

        result.getPlayerUnits().remove(originalUnit);
        result.getPlayerUnits().add(new UnitView(currentUnit));
        result.getResourceNodes().remove(originalResource);
        result.getResourceNodes().add(new ResourceNode.ResourceView(currentResource));

        // need to implement addCost and addPlan in GameState
        result.addCost(1);
        result.heuristic();
        result.addPlan(this);

        action = Action.createCompoundMove(originalUnit.getID(), x, y);
        return result;
    }

    @Override
    public Action convertAction() {
        return action;
    }
}