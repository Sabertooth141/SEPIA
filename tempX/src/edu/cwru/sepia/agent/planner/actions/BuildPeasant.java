package edu.cwru.sepia.agent.planner.actions;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;
import edu.cwru.sepia.util.Direction;

public class BuildPeasant implements StripsAction {

	private GameState parent;

	private int currentGold;
	private int currentFood;

	// new peasant and its location
	private Peasant peasant;
	private int newX;
	private int newY;

	private String stripsAction;
	private List<Action> sepiaAction = new ArrayList<Action>();

	/**
	 * Constructer that points the designated state as parent
	 * @param parent the state to check for available build actions
	 */
	public BuildPeasant(GameState parent) {
		this.parent = parent;
	}

	/**
	 * Find if the conditions met to build a new peasant
	 * @param state the current state
	 * @return bool indicating if the peasant can be build
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		// get current reserve of both gold and food
		currentGold = state.getCurrentGold();
		currentFood = state.getCurrentFood();

		// If gold and food are sufficient
		if (state.isBuildPeasants() && currentGold >= 400 && currentFood < 3) {

			int tHX = state.getTownHall().getXPosition();
			int tHY = state.getTownHall().getYPosition();
			
			// find the first valid position to place the new peasant 
			for (Direction direction : Direction.values()) {
				newX = tHX + direction.xComponent();
				newY = tHY + direction.yComponent();
				// if everything passes, return true
				if (legalPosition(state, newX, newY)) return true;
			}
		}
		return false;
	}

	/**
	 * Util method that tells if a certain position is legal to place the new peasant
	 * @param state the current state
	 * @param x the supposed X coordinate
	 * @param y the supposed Y coodrdinate
	 * @return whether it is leagal
	 */
	private boolean legalPosition(GameState state, int x, int y) {
		// if out of map or overlapping with other game objects, return false
		if ((x < 0 || x >= state.getxExtent() || y < 0 || y >= state.getyExtent()) || state.getMap()[x][y]) return false;
		else {
			// or if overlapping with another peasant, return false
			for (UnitView currentPeasant : state.getPlayerUnits()) {
				if (x == currentPeasant.getXPosition() && y == currentPeasant.getYPosition()) return false;
			}
		}
		return true;
	}

	/**
	 * Execute the action to the given state
	 * @param state the given current state to build
	 * @return the new gameState 
	 */
	@Override
	public GameState apply(GameState state) {
		GameState newGameState = new GameState(state);

		// retrieve the position of the townHall
		Position tHPos = new Position(state.getTownHall().getXPosition(), state.getTownHall().getYPosition());

		// initiate the new peasant
		TemplateView peasantTemplate = newGameState.getState().getTemplate(newGameState.getPlayerNum(), "Peasant");
		int peasantTemplateID = peasantTemplate.getID();
		Unit peasantUnit = new Unit(new UnitTemplate(peasantTemplateID), peasantTemplateID);
		UnitView peasantUnitView = new UnitView(peasantUnit);
		// manually assign the new ids of the generated peasant on screen using unused ids
		if (newGameState.getPeasantUnits().size() == 1) {
			peasant = new Peasant(10, newX, newY, false, false, 0, tHPos);
		} else if (newGameState.getPeasantUnits().size() == 2) {
			peasant = new Peasant(11, newX, newY, false, false, 0, tHPos);
		}
		// add the generated peasant to the gamestate and view
		newGameState.getPeasantUnits().add(peasant);
		newGameState.getPlayerUnits().add(peasantUnitView);
		newGameState.getAllUnits().add(peasantUnitView);

		// deduct gold and increase food to finish the action
		newGameState.addGold(-400);
		newGameState.addFood(1);

		// set strips action, which is available to work with toString
		stripsAction = "BuildPeasant(" + state.getTownHall().getID() + "," + peasantTemplateID + ")";
		sepiaAction.add(Action.createPrimitiveProduction(state.getTownHall().getID(), peasantTemplateID));

		// update the cost, run A* search heuristics again, and add build to plan
		double cost = 1;
		newGameState.addCost(cost);
		newGameState.heuristic();
		newGameState.addPlan(this);

		return newGameState;
	}

	@Override
	public GameState getParent() {
		return this.parent;
	}

	@Override
	public String toString() {
		return stripsAction;
	}

	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
	}


}