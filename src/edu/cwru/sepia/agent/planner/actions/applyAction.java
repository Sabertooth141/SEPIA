package edu.cwru.sepia.agent.planner.actions;
import java.util.*;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceUnit;
import edu.cwru.sepia.agent.planner.SimulatedUnit;

/**
 * applyAction applies the specified strips action to form a new game state
 */
public class ApplyAction implements StripsAction {

	private StripsActionType actionType = null;
    private Integer actorID = null;
    private Position actorPos = null;
    private Integer targetID = null;
    private Position targetPos = null;
    private ArrayList<SimulatedUnit> peasantList;
    private ArrayList<Position> occupiedPositionList;
    private ArrayList<ResourceUnit> resourceList;
	
	public ApplyAction(StripsActionType actionType) {
		this.actionType = actionType;
	}
	
	// constructor for MOVE
	public ApplyAction(ArrayList<SimulatedUnit> peasantList, ArrayList<Position> occupiedPositionList, int peasantID, Position peasantCurrentPosition, Position newPeasantPosition) {
		this.peasantList = peasantList;
		this.occupiedPositionList = occupiedPositionList;
		this.actorID = peasantID;
		this.actorPos = peasantCurrentPosition;
		this.targetID = peasantID;
		this.targetPos = newPeasantPosition;
		this.actionType = StripsActionType.MOVE;
	}
	
	// constructor for DEPOSIT
	public ApplyAction(ArrayList<SimulatedUnit> peasantList, int peasantID, Position peasantCurrentPosition, int townhallID, Position townhallPosition) {
		this.peasantList = peasantList;
		this.occupiedPositionList = null;
		this.actorID = peasantID;
		this.actorPos = peasantCurrentPosition;
		this.targetID = townhallID;
		this.targetPos = townhallPosition;
		this.actionType = StripsActionType.DEPOSIT;
	}
	
	// constructor for BUILD
	public ApplyAction(ArrayList<SimulatedUnit> peasantList, ArrayList<Position> occupiedPositionList,int townhallID, Position townhallPosition) {
		this.peasantList = peasantList;
		this.occupiedPositionList = occupiedPositionList;
		this.actorID = townhallID;
		this.actorPos = townhallPosition;
		this.targetID = peasantList.size() + 1;
		this.targetPos = null;
		this.actionType = StripsActionType.BUILD;
	}
	
	// constructor for COLLECT
	public ApplyAction(ArrayList<SimulatedUnit> peasantList, ArrayList<ResourceUnit> resourceList, int peasantID, Position peasantPosition, int resourceID, Position resourcePosition) {
		this.peasantList = peasantList;
		this.occupiedPositionList = null;
		this.resourceList = resourceList;
		this.actorID = peasantID;
		this.actorPos = peasantPosition;
		this.targetID = resourceID;
		this.targetPos = resourcePosition;
		this.actionType = StripsActionType.COLLECT;
	}
	
	// override the preconditionsMet method from abstract class StripsAction
	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
		
		switch (actionType) {
		case BUILD: 
			// return if enough gold and wood is present to build peasant
			return state.isGoal();
			
		case COLLECT: 
			// loop through peasant list to find the peasant and the resource currently being worked on
			if (peasantList != null && resourceList != null) {
				for (SimulatedUnit unit: peasantList) {
					for (ResourceUnit resource: resourceList) {
						if ((unit.getID() == actorID) && (resource.getPosition().equals(targetPos))) {
							// return true only when peasant is adjacent to resources and when peasant is not carrying any cargo
							if ((unit.getPosition().isAdjacent(targetPos)) && (unit.getCargoAmount()==0)) {
								return true;
							}
						}
					}
				}
			}
			return false;
		
		case DEPOSIT: 
			// loop through peasant list to find the peasant currently being worked on
			if (peasantList != null) {
				for (SimulatedUnit unit: peasantList) {
					if (unit.getID() == actorID) {
						// return true only when peasant is adjacent to townhall and peasant carries cargo
						if ((unit.getPosition().isAdjacent(targetPos)) && (unit.getCargoAmount()>0)) {
							return true;
						}
					}
				}
			}
			return false;
			
		case MOVE:
			// check if position are null
			if ((actorPos == null)|| targetPos == null) {
				return false;
			}
			
			// checks if peasant's current location and new location are both within map bound
			if (actorPos.inBounds(state.getXExtent(), state.getYExtent()) && targetPos.inBounds(state.getXExtent(), state.getYExtent())) {
				return true;
			}
			return false;
			
		}
		return false;
	}
	
	// override the apply method from abstract class StripsAction
	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		
		GameState newState = new GameState(state, this);
		
		switch (actionType) {
		case BUILD: 
			newState.buildPeasant();
			return newState;
			
		case COLLECT: 
			newState.collectResource();
			return newState;
			
		case DEPOSIT: 
			newState.depositCargo();
			return newState;
			
		case MOVE:
			newState.movePeasant();
			return newState;
		}
		
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
		switch (this.actionType) {
			case MOVE:
				return "MOVE: Unit " + this.actorID + " to " + this.targetPos;
			case COLLECT:
				return "COLLECT: Unit " + this.actorID + " at " + this.targetPos;
			case BUILD:
				return "BUILD: Townhall " + this.actorID;
			case DEPOSIT:
				return "DEPOSIT: Unit " + this.actorID + " into " + this.targetPos;
			default:
				return "UNKNOWN ACTION";
		}
	}
}
