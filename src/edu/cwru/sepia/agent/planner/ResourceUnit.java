package edu.cwru.sepia.agent.planner;

import java.util.List;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

public class ResourceUnit {
		private int id;
		private ResourceType resourceType;
		private int amountRemaining;
		private Position position;
		
		// default constructor
		public ResourceUnit() {
			;
		}
		
		// constructor to create resource unit based on the resources on the map
		public ResourceUnit(int id, ResourceType resourceType, int amountRemaining, Position position) {
			this.id = id;
			this.resourceType = resourceType;
			this.amountRemaining = amountRemaining;
			this.position = position;
		}
		
		// get id
		public int getID() {
			return this.id;
		}
		
		// get resource type
		public ResourceType getResourceType() {
			return this.resourceType;
		}
		
		// get amount remaining
		public int getAmountRemaining() {
			return this.amountRemaining;
		}
		
		// set amount remaining
		public void setAmountRemaining(int amountRemaining) {
			this.amountRemaining = amountRemaining;
		}
		
		// get position
		public Position getPosition() {
			return this.position;
		}
		
		// set position
		public void setPosition(Position position) {
			this.position = position;
		}
}
