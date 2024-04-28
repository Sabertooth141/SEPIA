package edu.cwru.sepia.agent.planner.actions;
import java.util.*;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceUnit;
import edu.cwru.sepia.agent.planner.SimulatedUnit;
import edu.cwru.sepia.agent.planner.actions.StripsAction.StripsActionType;

/**
 * Build_k applies the specified strips action Build
 */
public class Build_k implements StripsAction {

	private StripsActionType actionType = null;
    private Integer actorID = null;
    private Position actorPos = null;
    private Integer targetID = null;
    private Position targetPos = null;
    private ArrayList<SimulatedUnit> peasantList;
    private ArrayList<Position> occupiedPositionList;
    private ArrayList<ResourceUnit> resourceList;
	
	public Build_k(StripsActionType actionType) {
		this.actionType = actionType;
	}
	
	// constructor for BUILD
	public Build_k(ArrayList<SimulatedUnit> peasantList, ArrayList<Position> occupiedPositionList, int townhallID, Position townhallPosition, int peasantTemplateID) {
		this.peasantList = peasantList;
		this.occupiedPositionList = occupiedPositionList;
		this.actorID = townhallID;
		this.actorPos = townhallPosition;
		this.targetID = peasantTemplateID;
		this.targetPos = null;
		this.actionType = StripsActionType.BUILD;
	}
	
	
	// override the preconditionsMet method from abstract class StripsAction
	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
		
		// check if there are any peasant that does not carry any cargo and are next to the townhall to create new peasant
		for (SimulatedUnit peasant: peasantList) {
			if (peasant.getCargoAmount()==0) {
				if (peasant.getPosition().equals(actorPos)) {
					
					// return if enough gold and food is present to build peasant
					return (!(state.isGoal())&& (state.getCurrentGold() >= 400) && (state.getCurrentFood() >= 1));
				}
			}
		}
		return false;
//		return state.isGoal();
	}
	
	// override the apply method from abstract class StripsAction
	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		
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
		return actorID;
	}
	
	@Override
	public Position getActorPos() {
		return actorPos;
	}
	
	@Override
    public void setActorPos(Position actorPos) {
		this.actorPos = actorPos;
	}
	
	@Override
    public void setActorID(Integer actorID) {
		this.actorID = actorID;
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

		return "BUILD: Townhall " + this.actorID;
	}
}
