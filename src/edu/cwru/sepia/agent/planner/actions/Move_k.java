package edu.cwru.sepia.agent.planner.actions;
import java.util.*;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceUnit;
import edu.cwru.sepia.agent.planner.SimulatedUnit;
import edu.cwru.sepia.agent.planner.actions.StripsAction.StripsActionType;
import edu.cwru.sepia.environment.model.state.ResourceType;

/**
 * Move_k applies the specified strips action Move
 */
public class Move_k implements StripsAction {

	private StripsActionType actionType = null;
    private Integer actorID = null;
    private Position actorPos = null;
    private Integer targetID = null;
    private Position targetPos = null;
    private ArrayList<SimulatedUnit> peasantList;
    private ArrayList<SimulatedUnit> moveList;
    private ArrayList<Position> occupiedPositionList;
    private ArrayList<ResourceUnit> resourceList;
    private ResourceType type;
	
	public Move_k(StripsActionType actionType) {
		this.actionType = actionType;
	}
	
	// constructor for MOVE for Townhall
	public Move_k(ArrayList<SimulatedUnit> peasantList, ArrayList<Position> occupiedPositionList, Position townhallPosition) {
		this.peasantList = peasantList;
		this.occupiedPositionList = occupiedPositionList;
		this.targetPos = townhallPosition;
		this.actionType = StripsActionType.MOVE;
	}
	
	// constructor for MOVE towards gold or wood
	public Move_k(ArrayList<SimulatedUnit> peasantList, ArrayList<Position> occupiedPositionList, ArrayList<ResourceUnit> resourceList, ResourceType type) {
		this.peasantList = peasantList;
		this.occupiedPositionList = occupiedPositionList;
		this.resourceList = resourceList;
		this.type = type;
		this.actionType = StripsActionType.MOVE;
	}
	
	// override the preconditionsMet method from abstract class StripsAction
	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
				
		moveList = new ArrayList<SimulatedUnit>();
		
		// adds idle peasants to move towards townhall
		if (!peasantList.isEmpty() && targetPos != null) {
			for (SimulatedUnit peasant: peasantList) {
				
			// checks if peasant's current location and new location are both within map bound
				if (peasant.getPosition().inBounds(state.getXExtent(), state.getYExtent()) && targetPos.inBounds(state.getXExtent(), state.getYExtent())) {
					moveList.add(peasant);
				}
			}
		}
		
		// adds idle peasants to move towards gold
		else if (type != null && type.equals(ResourceType.GOLD)) {
			for (SimulatedUnit peasant: peasantList) {
				this.targetID = peasant.getNearestResourceID(resourceList, peasant.getPosition(), ResourceType.GOLD);
				this.targetPos = peasant.getNearestResourcePosition(resourceList, peasant.getPosition(), ResourceType.GOLD);
				
				// checks if peasant's current location and new location are both within map bound
					if (peasant.getPosition().inBounds(state.getXExtent(), state.getYExtent()) && targetPos.inBounds(state.getXExtent(), state.getYExtent())) {
						moveList.add(peasant);
					}
				}
		}
		
		// adds idle peasants to move towards wood
		else if (type != null && type.equals(ResourceType.WOOD)) {
			for (SimulatedUnit peasant: peasantList) {
				this.targetID = peasant.getNearestResourceID(resourceList, peasant.getPosition(), ResourceType.WOOD);
				this.targetPos = peasant.getNearestResourcePosition(resourceList, peasant.getPosition(), ResourceType.WOOD);
				
				if (targetPos != null) {
				// checks if peasant's current location and new location are both within map bound
					if (peasant.getPosition().inBounds(state.getXExtent(), state.getYExtent()) && targetPos.inBounds(state.getXExtent(), state.getYExtent())) {
						moveList.add(peasant);
					}
				}
			}
		}
		
		
		
		// check if the list of peasants to move is empty or not
		if (moveList.size() > 0) {
			return true;
		}
		
		return false;
			
	}
	
	// override the apply method from abstract class StripsAction
	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		
		GameState newState = new GameState(state, this);
		
		// moves k peasants to the new position
		for (SimulatedUnit move: moveList) {
			this.actorID = move.getID();
			this.actorPos = move.getPosition();
			this.targetID = move.getID();
			newState.movePeasant();
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

		return "MOVE: Unit " + this.actorID + " to " + this.targetPos;
	}
}
