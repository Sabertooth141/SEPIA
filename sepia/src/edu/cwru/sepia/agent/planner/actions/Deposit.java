package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;

public class Deposit implements StripsAction {

    Position currentPosition;
    Position townHallPosition;
    UnitView unit;
    UnitView townHall;
    Action action;
    GameState parent;
    Boolean forGold;

    public Deposit(Position currentPosition, UnitView townHall, UnitView unit, GameState parent, Boolean forGold) {
        this.currentPosition = currentPosition;
        this.townHallPosition = new Position(townHall.getXPosition(), townHall.getYPosition());
        this.townHall = townHall;
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
        if (this.forGold) {
            return unit.getCargoAmount() > 0 && unit.getCargoType() == ResourceType.GOLD && currentPosition.equals(townHallPosition);
        } else {
            return unit.getCargoAmount() > 0 && unit.getCargoType() == ResourceType.WOOD && currentPosition.equals(townHallPosition);
        }
    }

    @Override
    public GameState apply(GameState state) {

        GameState result = new GameState(state);        // need a constructor
        UnitView originalUnit = result.findUnit(unit.getID(), result.getPlayerUnits()); // need to implement findUnit() and getPlayerUnits in GameState
        UnitTemplate currentTemplate = new UnitTemplate(originalUnit.getID());

        Unit currentUnit = new Unit(currentTemplate, originalUnit.getID());
        currentUnit.setxPosition(unit.getXPosition());
        currentUnit.setyPosition(unit.getYPosition());
        currentUnit.clearCargo();
        result.getPlayerUnits().remove(originalUnit);
        result.getPlayerUnits().add(new UnitView(currentUnit));

        if (this.forGold) {
            result.addGold(unit.getCargoAmount());
        }
        else {
            result.addWood(unit.getCargoAmount());
        }

        // need to implement addCost and addPlan in GameState
        result.addCost(1);
        result.heuristic();
        result.addPlan(this);

        action = Action.createCompoundDeposit(originalUnit.getID(), townHall.getID());
        return result;
    }

    @Override
    public Action convertAction() {
        return action;
    }
}