package edu.cwru.sepia.agent.planner;

import java.util.List;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

public class SimulatedUnit {
		private int id;
		private Position position;
		private int cargoAmount = 0;
		private ResourceType cargoType = null;
		
		// default constructor
		public SimulatedUnit() {
			;
		}
		
		// constructor to create Peasant from existing Unit on map
		public SimulatedUnit(Unit.UnitView unit) {
			id = unit.getID();
			position = new Position(unit.getXPosition(),unit.getYPosition());
			
			// only peasant can carry cargo so check if unit is peasant
			if (unit.getTemplateView().getName().toLowerCase().equals("peasant")) {
				cargoAmount = unit.getCargoAmount();
				cargoType = unit.getCargoType();
			}
		}
		
		// constructor to create a new simulated unit
		public SimulatedUnit(int id, Position position, int cargoAmount, ResourceType cargoType) {
			this.id = id;
			this.position = position;
			this.cargoAmount = cargoAmount;
			this.cargoType = cargoType;
		}
		
		// constructor to create a new simulated unit given a list of existing simulated units on the map
		public SimulatedUnit(List<SimulatedUnit> unitList, List<Position> occupiedPositionList, Position townhallPosition, int xExtent, int yExtent) {
			id = unitList.size()+1;
			
			for (Position adjacentPosition: townhallPosition.getAdjacentPositions()) {
				for (SimulatedUnit unit:unitList) {
					if (!adjacentPosition.equals(unit.getPosition())) {
						this.position = adjacentPosition;
					}
				}
			}
			
			cargoAmount = 0;
			cargoType = null;
		}
		
		// get nearest resource position
		public Position getNearestResourcePosition(List<ResourceUnit> resourceList, Position p, ResourceType resourceType) {
	    	Position nearestPosition = null;
	    	if(resourceType.equals(resourceType.GOLD)){
	    		for (ResourceUnit list: resourceList) {
	    			if (list.getResourceType().equals(ResourceType.GOLD)) {
	    				Position goldMine = list.getPosition();
			    		// initially store one of the resource locations as nearestPosition
		    			if (nearestPosition == null) {
		    				nearestPosition = goldMine;
		    			}
			    		// for all other resources, check if they are closer to the position p
		    			else {
		    				if (goldMine.chebyshevDistance(p) < nearestPosition.chebyshevDistance(p)) {
		    					nearestPosition = goldMine;
		    				}
		    			}
	    			}
	    		}
			}
	    	else if(resourceType.equals(resourceType.WOOD)){
	    		for (ResourceUnit list: resourceList) {
	    			if (list.getResourceType().equals(ResourceType.WOOD)) {
	    				Position tree = list.getPosition();
	    			// initially store one of the resource locations as nearestPosition
	    			if (nearestPosition == null) {
	    				nearestPosition = tree;
	    			}
	    			// for all other resources, check if they are closer to the position p
	    			else {
	    				if (tree.chebyshevDistance(p) < nearestPosition.chebyshevDistance(p)) {
	    					nearestPosition = tree;
	    				}
	    			}
	    			}
	    		}
			}
			else{
				System.out.println(new IllegalArgumentException());
			}
	    	
	    	return nearestPosition;
	    }
		
		// gets nearest resource ID
	    public int getNearestResourceID(List<ResourceUnit> resourceList, Position p, ResourceType resourceType) {
	    	Position nearestPosition = this.getNearestResourcePosition(resourceList, p, resourceType);
	    	int nearestResourceID = - 1;
	    	
	    	for (ResourceUnit list: resourceList) {
				if (list.getPosition().equals(nearestPosition)) {
					nearestResourceID = list.getID();
				}
			}
	    	
	    	return nearestResourceID;
	    }
		
		
		// get ID
		public int getID() {
			return this.id;
		}
		
		// get position
		public Position getPosition() {
			return this.position;
		}
		
		// set position
		public void setPosition(Position position) {
			this.position = position;
		}
		
		// get cargo amount
		public int getCargoAmount() {
			return this.cargoAmount;
		}
		
		// set cargo amount
		public void setCargoAmount(int cargoAmount) {
			this.cargoAmount = cargoAmount;
		}
		
		// get cargo type
		public ResourceType getCargoType() {
			return this.cargoType;
		}
		
		// set cargo type
		public void setCargoType(ResourceType cargoType) {
			this.cargoType = cargoType;
		}
}