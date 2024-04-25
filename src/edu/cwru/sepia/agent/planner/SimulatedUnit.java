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
		public SimulatedUnit(List<SimulatedUnit> unitList, List<Position> occupiedPositionList, int xExtent, int yExtent) {
			id = unitList.size()+1;
			
			// starts at top left of map as possible new position
			Position newPosition = new Position(1,1);
			
			// loop through to find an open position to place the new simulated unit
			while (this.position==null) {
				for (Position occupiedPosition:occupiedPositionList) {
					// check if new position is occupied
					if (!newPosition.equals(occupiedPosition)) {
						this.position = newPosition;
						break;
					}
					else {
						// moves new position one unit to the east
						if (newPosition.y< yExtent) {
							newPosition.move(Direction.EAST);
						}
						// moves new position back to left most side and down one row
						else if (newPosition.x+1 < xExtent) {
							newPosition = new Position(newPosition.x+1,1);
						}
						// goes out of while loop if all places on map is occupied
						else {
							System.out.println("All map location is occupied");
							break;
						}
					}
				}
			}
			
			cargoAmount = 0;
			cargoType = null;
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