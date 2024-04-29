package edu.cwru.sepia.agent.planner.actions;
import java.util.*;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.agent.planner.ResourceUnit;
import edu.cwru.sepia.agent.planner.SimulatedUnit;

/**
 * Deposit_k applies the specified strips action DEPOSIT
 */
public class Deposit_k implements StripsAction {

	private StripsActionType actionType = null;
    private Integer actorID = null;
    private Position actorPos = null;
    private Integer targetID = null;
    private Position targetPos = null;
    private ArrayList<SimulatedUnit> peasantList;
    private ArrayList<SimulatedUnit> depositList;
    private ArrayList<Position> occupiedPositionList;
    private ArrayList<ResourceUnit> resourceList;
	
	public Deposit_k(StripsActionType actionType) {
		this.actionType = actionType;
	}
	
	// constructor for DEPOSIT
	public Deposit_k(ArrayList<SimulatedUnit> peasantList, int townhallID, Position townhallPosition) {
		this.peasantList = peasantList;
		this.occupiedPositionList = null;
		this.targetID = townhallID;
		this.targetPos = townhallPosition;
		this.actionType = StripsActionType.DEPOSIT;
	}
	
	
	// override the preconditionsMet method from abstract class StripsAction
	@Override
	public boolean preconditionsMet(GameState state) {
		// TODO Auto-generated method stub
		
		depositList = new ArrayList<SimulatedUnit>();
		
		// loop through peasant list to find the peasant currently being worked on
		if (peasantList != null) {
			for (SimulatedUnit unit: peasantList) {
					// return true only when peasant is adjacent to townhall and peasant carries cargo
					if (unit.getPosition().isAdjacent(targetPos) && (unit.getCargoAmount()>0)) {
						depositList.add(unit);
					}
			}
		}
				
		// check if there are k peasants to perform deposit
		if (depositList.size() > 0) {
			return true;
		}
		
		return false;
			
	}
	
	// override the apply method from abstract class StripsAction
	@Override
	public GameState apply(GameState state) {
		// TODO Auto-generated method stub
		
		GameState newState = new GameState(state, this);
		
//		newState.depositCargo();
		
		for (SimulatedUnit deposit: depositList) {
			this.actorID = deposit.getID();
			this.actorPos = deposit.getPosition();
			newState.depositCargo();
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

		return "DEPOSIT: Unit " + this.actorID + " into " + this.targetPos;
	}
}
