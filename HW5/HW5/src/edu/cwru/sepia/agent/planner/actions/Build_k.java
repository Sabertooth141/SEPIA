package edu.cwru.sepia.agent.planner.actions;
import java.util.*;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceUnit;
import edu.cwru.sepia.agent.planner.SimulatedUnit;

/**
 * Build_k involves the adding of new peasant when the peasant population is currently at k
 */
public class Build_k implements StripsAction {

	private StripsActionType actionType = null;
    private Integer THID = null;
    private Position THPos = null;
    private Integer targetID = null;
    private Position targetPos = null;
    private ArrayList<SimulatedUnit> peasants;
    private ArrayList<Position> occupiedPoses;
    private ArrayList<ResourceUnit> resourceList;
	
	public Build_k(StripsActionType actionType) {
		this.actionType = actionType;
	}
	
	// Constructor
	public Build_k(ArrayList<SimulatedUnit> peasants, ArrayList<Position> occupiedPoses, int townhallID, Position townhallPosition, int peasantTemplateID) {
		this.peasants = peasants;
		this.occupiedPoses = occupiedPoses;
		this.THID = townhallID;
		this.THPos = townhallPosition;
		this.targetID = peasantTemplateID;
		this.targetPos = null;
		this.actionType = StripsActionType.BUILD;
	}
	
	/**
	 * Check if the peasant if near the townhall in a state, and check for the avalaible resource if it can generate another peasant
	 * @param state the current GameState
	 * @return if we can build a peasant now 
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		// Traverse all peasant at the moment the function is called
		for (SimulatedUnit peasant: peasants) {
			// If the peasant finishes depositing or not carrying stuff 
			if (peasant.getCargoAmount()==0) {
				// If the peasant is at townhall
				if (peasant.getPosition().equals(THPos)) {
					// return ture if enough gold and available peasants slots left and game not finished 
					return (!(state.isGoal())&& (state.getCurrentGold() >= 400) && (state.getCurrentFood() >= 1));
				}
			}
		}
		return false;
	}
	
	/**
	 * Applying building peasant to the next state
	 * @param state the GameState we found that can build peasant
	 * @return the new GameState to execute 
	 */
	@Override
	public GameState apply(GameState state) {
		GameState newState = new GameState(state, this);
		newState.buildPeasant();
		return newState;
	}

	@Override
    public StripsActionType getActionType() {
		return actionType;
	}

	@Override
    public Integer getActorID() {
		return THID;
	}
	
	@Override
	public Position getActorPos() {
		return THPos;
	}
	
	@Override
    public void setActorPos(Position actorPos) {
		this.THPos = actorPos;
	}
	
	@Override
    public void setActorID(Integer actorID) {
		this.THID = actorID;
	}

	@Override
    public Integer getTargetID() {
		return targetID;
	}

	@Override
    public void setTargetID(Integer targetID) {
		this.targetID = targetID;
	}

	@Override
    public Position getTargetPos() {
		return targetPos;
	}

	@Override
    public void setTargetPos(Position targetPos) {
		this.targetPos = targetPos;
	}

	public String toString() {
		return "BUILD: Townhall " + this.THID;
	}
}
