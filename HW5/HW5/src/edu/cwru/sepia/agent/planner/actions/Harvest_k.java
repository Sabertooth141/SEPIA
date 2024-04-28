package edu.cwru.sepia.agent.planner.actions;
import java.util.*;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceUnit;
import edu.cwru.sepia.agent.planner.SimulatedUnit;
import edu.cwru.sepia.agent.planner.actions.StripsAction.StripsActionType;

/**
 * Harvest_k applies the specified strips action Harvest
 */
public class Harvest_k implements StripsAction {

	private StripsActionType actionType = null;
    private Integer actorID = null;
    private Position actorPos = null;
    private Integer targetID = null;
    private Position targetPos = null;
    private ArrayList<SimulatedUnit> peasantList;
    private ArrayList<SimulatedUnit> harvestList;
    private ArrayList<Position> occupiedPositionList;
    private ArrayList<ResourceUnit> resourceList;
	
	public Harvest_k(StripsActionType actionType) {
		this.actionType = actionType;
	}
	
	// constructor for Harvest
		public Harvest_k(ArrayList<SimulatedUnit> peasantList, ArrayList<ResourceUnit> resourceList, int resourceID, Position resourcePosition) {
			this.peasantList = peasantList;
			this.occupiedPositionList = null;
			this.resourceList = resourceList;
			this.targetID = resourceID;
			this.targetPos = resourcePosition;
			this.actionType = StripsActionType.HARVEST;
		}
		
		// constructor for Harvest GOLD or WOOD
		public Harvest_k(ArrayList<SimulatedUnit> peasantList, ArrayList<ResourceUnit> resourceList) {
			this.peasantList = peasantList;
			this.occupiedPositionList = null;
			this.resourceList = resourceList;
			this.actionType = StripsActionType.HARVEST;
		}
	
	
	// override the preconditionsMet method from abstract class StripsAction
	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
		
		harvestList = new ArrayList<SimulatedUnit>();
		
		if (targetPos == null) {
			for (SimulatedUnit unit:peasantList) {
				for (ResourceUnit resource: resourceList) {
					// return true only when peasant is adjacent to resources and when peasant is not carrying any cargo
					if ((unit.getPosition().isAdjacent(resource.getPosition())) && (unit.getCargoAmount()==0)) {
						// check if there are enough resources to harvest
						if (resource.getAmountRemaining() >= 100*(harvestList.size()+1)) {
							this.targetPos = resource.getPosition();
							this.targetID = resource.getID();
							harvestList.add(unit);
						}
					}
				}
			}
			
		}
		
		
		// return true only when there are k peasants that can perform harvest
		if (harvestList.size() > 0) {
			return true;
		}
		
		return false;
			
	}
	
	// override the apply method from abstract class StripsAction
	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		
		GameState newState = new GameState(state, this);
		
		for (SimulatedUnit harvest: harvestList) {
			this.actorID = harvest.getID();
			this.actorPos = harvest.getPosition();
			newState.collectResource();
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

		return "HARVEST: Unit " + this.actorID + " at " + this.targetPos;
	}
}
