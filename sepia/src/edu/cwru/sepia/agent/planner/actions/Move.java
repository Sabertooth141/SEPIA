package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;

public class Move implements StripsAction {

    Position currentPosition;

    UnitView unit;
    Action action;
    GameState parent;

    public Move(Position currentPosition, UnitView unit, GameState parent) {
        this.currentPosition = currentPosition;
        this.unit = unit;
        this.parent = parent;
    }

    @Override
    public GameState getParent() {
        return this.parent;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        int x = currentPosition.x;
        int y = currentPosition.y;
        // need to implement getMap() in GameState which is a map the logs the positions of objects other than the peasant i.e. gold, town hall, etc.
        if (x < state.getXExtent() && x > 0 && y < state.getYExtent() && y > 0 && !state.getMap()[x][y]) {
            // need to implement getPlayerUnits in GameState which gets the current player controlled unit
            for (UnitView unit : state.getPlayerUnits()) {
                if (x == unit.getXPosition() && y == unit.getYPosition()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public GameState apply(GameState state) {
        GameState result = new GameState(state);        // need a constructor
        int x = currentPosition.x;
        int y = currentPosition.y;

        UnitView originalUnit = result.findUnit(unit.getID(), result.getPlayerUnits()); // need to implement findUnit() and getPlayerUnits in GameState
        UnitTemplate currentTemplate = new UnitTemplate(originalUnit.getID());
        currentTemplate.setCanGather(true);

        Unit currentUnit = new Unit(currentTemplate, originalUnit.getID());
        currentUnit.setxPosition(unit.getXPosition());
        currentUnit.setyPosition(unit.getYPosition());

        if (unit.getCargoAmount() > 0) {
            currentUnit.setCargo(unit.getCargoType(), unit.getCargoAmount());
        }

        result.getPlayerUnits().remove(originalUnit);
        result.getPlayerUnits().add(new UnitView(currentUnit));

        Position originalPosition = new Position(unit.getXPosition(), unit.getYPosition());
        double distance = originalPosition.chebyshevDistance(currentPosition);

        // need to implement addCost and addPlan in GameState
        result.addCost(distance);
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
