package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;

import java.util.ArrayList;
import java.util.List;

public class Move implements StripsAction {

    Position currentPosition;

    List<Peasant> peasantList = new ArrayList<>();
    List<Action> actionList = new ArrayList<>();
    List<Position> availablePositions = new ArrayList<>();

    Position start;
    Position dest;

    GameState parent;

    public Move(List<Peasant> peasantList, Position dest, GameState parent) {
        this.peasantList = peasantList;
        this.dest = dest;
        this.parent = parent;

        this.start = peasantList.get(0).neighbor;
    }

    private Boolean isLegal(Position pos) {
        if (pos.x < 0 || pos.x >= parent.getXExtent() || pos.y < 0 || pos.y >= parent.getYExtent()) {
            return false;
        } else {
            return !parent.getMap()[pos.x][pos.y];
        }
    }

    @Override
    public GameState getParent() {
        return this.parent;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        int x = dest.x;
        int y = dest.y;

        int goldAmount = parent.getGoldMap()[x][y];
        int woodAmount = parent.getWoodMap()[x][y];

        if (goldAmount != 0 || woodAmount != 0) {
            if (goldAmount != 0) {
                if ((int) )
            }
        }


        Position currentPos = currentPosition;
        Position myPos = new Position(unit.getXPosition(), unit.getYPosition());
        // need to implement getMap() in GameState which is a map the logs the positions of objects other than the peasant i.e. gold, town hall, etc.
        return myPos.inBounds(state.getXExtent(), state.getYExtent()) && currentPos.inBounds(state.getXExtent(), state.getYExtent());
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
