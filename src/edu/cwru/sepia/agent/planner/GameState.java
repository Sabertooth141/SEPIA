package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction.StripsActionType;
import edu.cwru.sepia.agent.planner.actions.ApplyAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.*;

/**
 * This class is used to represent the state of the game after applying one of the avaiable actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
  * Note that SEPIA saves the townhall as a unit. Therefore when you create a GameState instance,
 * you must be able to distinguish the townhall from a peasant. This can be done by getting
 * the name of the unit type from that unit's TemplateView:
 * state.getUnit(id).getTemplateView().getName().toLowerCase(): returns "townhall" or "peasant"
 * 
 * You will also need to distinguish between gold mines and trees.
 * state.getResourceNode(id).getType(): returns the type of the given resource
 * 
 * You can compare these types to values in the ResourceNode.Type enum:
 * ResourceNode.Type.GOLD_MINE and ResourceNode.Type.TREE
 * 
 * You can check how much of a resource is remaining with the following:
 * state.getResourceNode(id).getAmountRemaining()
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
	// private fields for GameState
	
	private State.StateView state;
	private int playernum;
	private int requiredGold;
	private int requiredWood;
	private boolean buildPeasants;
	
	private int xExtent;
	private int yExtent;
	private int currentGold;
	private int currentWood;
	private ArrayList<Position> goldMineLocations;
	private ArrayList<Position> treeLocations;
	
	private ArrayList<ResourceUnit> resourceList;
	
	private ArrayList<Integer> peasantIDs;
	private ArrayList<SimulatedUnit> peasantList;
	private HashSet<Position> peasantPosition;
	private int townhallID = -1;
	private Position townhallPosition;
	
	private ArrayList<Position> occupiedPositionList;
	private ArrayList<Position> occupiedResourceList;
	
	private GameState parent;
	private StripsAction actionPerformed;

	public double cost = 0.;
		
    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        // TODO: Implement me!
    	// stores the inputs given for initializing GameState
    	this.state = state;
    	this.playernum = playernum;
    	this.requiredGold = requiredGold;
    	this.requiredWood = requiredWood;
    	this.buildPeasants = buildPeasants;
    	
    	// stores the x and y extent of the map
    	this.xExtent = state.getXExtent();
    	this.yExtent = state.getYExtent();
    	
    	occupiedPositionList = new ArrayList();
    	occupiedResourceList = new ArrayList();
    	List<Integer> resourceIDs = state.getAllResourceIds();
    	
    	// confirms that there are resources in the map
    	if (resourceIDs.size()!=0) {
	        List<ResourceNode.ResourceView> goldMineNodes = state.getResourceNodes(ResourceNode.Type.GOLD_MINE);
	    	List<ResourceNode.ResourceView> treeNodes = state.getResourceNodes(ResourceNode.Type.TREE);
	    	
	    	this.goldMineLocations = new ArrayList<Position>();
	        this.treeLocations = new ArrayList<Position>();
	        this.resourceList = new ArrayList<ResourceUnit>();
	        
	        // stores the location of gold mines if they are present on the map
	        if (goldMineNodes != null) {
		        for (ResourceNode.ResourceView goldMine: goldMineNodes) {
		        	goldMineLocations.add(new Position(goldMine.getXPosition(),goldMine.getYPosition()));
		        	occupiedPositionList.add(new Position(goldMine.getXPosition(),goldMine.getYPosition()));
		        	occupiedResourceList.add(new Position(goldMine.getXPosition(),goldMine.getYPosition()));
		        	
		        	resourceList.add(new ResourceUnit (goldMine.getID(), ResourceType.GOLD, goldMine.getAmountRemaining(),new Position(goldMine.getXPosition(),goldMine.getYPosition())));
		        }
	        }
	        
	        // stores the location of trees if they are present on the map
	        if (treeNodes != null) {
		        for (ResourceNode.ResourceView tree: treeNodes) {
		        	treeLocations.add(new Position(tree.getXPosition(),tree.getYPosition()));
		        	occupiedPositionList.add(new Position(tree.getXPosition(),tree.getYPosition()));
		        	occupiedResourceList.add(new Position(tree.getXPosition(),tree.getYPosition()));
		        	
		        	resourceList.add(new ResourceUnit (tree.getID(), ResourceType.WOOD, tree.getAmountRemaining(),new Position(tree.getXPosition(),tree.getYPosition())));
			        
		        }
	        }
	        
    	}
    	// returns message when no resources are found on map
    	else {
    		System.out.println("No resources found on map");
    	}
    	
    	List<Integer> unitIDs = state.getUnitIds(playernum);
    	
    	// stores the individual units in the game state
        if(unitIDs.size() != 0){
        	peasantIDs = new ArrayList<Integer>();
        	peasantList = new ArrayList<SimulatedUnit>();
        	
        	for (Integer id: unitIDs) {
        		Unit.UnitView temporaryUnit = state.getUnit(id);
                String unitType = temporaryUnit.getTemplateView().getName().toLowerCase();
                
                // stores the footman IDs
                if(unitType.equals("peasant"))
                {
                	peasantIDs.add(id);
                	peasantList.add(new SimulatedUnit(temporaryUnit));
                	occupiedPositionList.add(new Position(temporaryUnit.getXPosition(),temporaryUnit.getYPosition()));
                }
                // stores the townhall ID
                else if(unitType.equals("townhall"))
                {
                    townhallID =id;
                    townhallPosition = new Position(temporaryUnit.getXPosition(),temporaryUnit.getYPosition());
                    occupiedPositionList.add(townhallPosition);
                }
                // returns error if unknown unit found
                else
                {
                    System.err.println("Unknown unit type");
                }
                
        	}
        	
        	// checks if footman IDs are successfully stored
        	if (peasantIDs.size()==0) {
        		System.err.println("No peasant found");
        	}
        	// checks if townhall IDs are successfully stored
        	if (townhallID==-1) {
        		System.err.println("No townhall found");
        	}
        }
        // returns error message if no units found
        else {
        	System.err.println("No units found");
        }
        
        // stores the location of the peasants if there are peasants on the map
        if (peasantIDs.size()!=0) {
        	this.peasantPosition = this.getPeasantPosition();
    	}
          
    }
    
    // constructor for generating new GameState when an action is performed
    public GameState (GameState parent, StripsAction actionPerformed) {
    	// copy over the field values from parents to new game state
    	this.state = parent.state;
    	this.playernum = parent.playernum;
    	this.requiredGold = parent.requiredGold;
    	this.requiredWood = parent.requiredWood;
    	this.buildPeasants = parent.buildPeasants;
    	this.xExtent = parent.xExtent;
    	this.yExtent = parent.yExtent;
    	this.currentGold = parent.currentGold;
    	this.currentWood = parent.currentWood;
    	this.townhallID = parent.townhallID;
    	this.cost = parent.cost;
    	
    	this.goldMineLocations = parent.goldMineLocations;
    	this.treeLocations = parent.treeLocations;
    	this.peasantIDs = parent.peasantIDs;
    	this.peasantPosition = parent.peasantPosition;
    	this.townhallPosition = parent.townhallPosition;
    	this.occupiedPositionList = parent.occupiedPositionList;
    	this.occupiedResourceList = parent.occupiedResourceList;
    	
		// copy list of peasants from parents to new game state
		this.peasantList = this.copyPeasantList(parent.peasantList);
		
		// copy list of resources from parent to new game state
		this.resourceList = this.copyResourceList(parent.resourceList);
		
    	// store the parent node and action performed for the new game state
    	this.parent = parent;
    	this.actionPerformed = actionPerformed;

    }
    
    // helper method to copy the peasant list from parent
    private ArrayList<SimulatedUnit> copyPeasantList(ArrayList<SimulatedUnit> parentList) {
    	ArrayList<SimulatedUnit> copyList = new ArrayList<SimulatedUnit>();
    	
    	for (SimulatedUnit unit: parentList) {
    		copyList.add(new SimulatedUnit(unit.getID(), unit.getPosition(), unit.getCargoAmount(), unit.getCargoType()));
    	}
    	
    	return copyList;
    }
    
    // helper method to copy the resource list from parent
    private ArrayList<ResourceUnit> copyResourceList(ArrayList<ResourceUnit> parentList) {
    	ArrayList<ResourceUnit> copyList = new ArrayList<ResourceUnit>();
    	
    	for (ResourceUnit unit: parentList) {
    		copyList.add(new ResourceUnit(unit.getID(), unit.getResourceType(), unit.getAmountRemaining(), unit.getPosition()));
    	}
    	
    	return copyList;
    }
    
    // helper getter method to get xExtent
    public int getXExtent() {
    	return this.xExtent;
    }
    
    // helper getter method to get yExtent
    public int getYExtent() {
    	return this.yExtent;
    }
    
    // helper getter method to get parent of GameState
    public GameState getParent() {
    	return this.parent;
    }
    
    // helper getter method to get the action performed from parent to get to the game state
    public StripsAction getAction() {
    	return this.actionPerformed;
    }
    
    // helper getter method to get current gold
    public int getCurrentGold() {
    	return this.currentGold;
    }
    
    // helper getter method to get current gold
    public int getCurrentWood() {
    	return this.currentWood;
    }
    
    // helper getter method to get peasant list
    public ArrayList<SimulatedUnit> getPeasantList(){
    	return this.peasantList;
    }
    
    // helper setter method to set peasant list
    public void setPeasantList(ArrayList<SimulatedUnit> peasantList){
    	this.peasantList = peasantList;
    }
    
    // helper getter method to get peasant position 
    public HashSet<Position> getPeasantPosition() {
    	HashSet<Position> peasantPosition = new HashSet<Position>();
    	
    	if (peasantIDs.size()!= 0) {
    		for (int i=0; i < peasantIDs.size(); i++) {
    			peasantPosition.add(new Position(state.getUnit(i).getXPosition(),state.getUnit(i).getYPosition()));
    		}
    	}
    	
    	this.peasantPosition = peasantPosition;
    	
    	return peasantPosition;
    }
    
    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        // TODO: Implement me!
    	
    	// check if enough gold and wood is available
        if ((currentGold >= requiredGold) && (currentWood >= requiredWood)) {
        	return true;
        }
        
        // if supplies are not enough, returns false
        return false;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
    	
    	List<GameState> childList = new ArrayList<GameState>();
    	
    	// check if the game state allows peasants to be built
    	if (this.buildPeasants == true) {
    		ApplyAction build = new ApplyAction(peasantList, occupiedPositionList,townhallID, townhallPosition);
    		GameState newState = new GameState(this, build);
    		
    		// add children if precondition for BUILD is met
    		if (build.preconditionsMet(newState)) {
    			newState = build.apply(newState);
    			childList.add(newState);
    			
    			// focus on building peasants instead of other actions
    			return childList;
    		}
    		
    	}
    	// focus on performing other tasks if peasants cannot be built
    	else {
    		// loop through peasantList and move peasant to any adjacent position
    		if (peasantList != null) {
	    		for (SimulatedUnit peasant: peasantList) {
	    			int currentID = peasant.getID();
	    			Position currentPosition = peasant.getPosition();
					
					// checks if current position is a resource
					for (Position resourcePosition: this.occupiedResourceList) {
						if (currentPosition.equals(resourcePosition)) {
							int resourceID = -1;
							for (ResourceUnit resource:resourceList) {
								if (resource.getPosition().equals(resourcePosition)) {
									resourceID = resource.getID();
								}
							}
							
							ApplyAction collect = new ApplyAction(peasantList, resourceList, currentID, currentPosition, resourceID, resourcePosition);
							GameState newState = new GameState(this, collect);
							
							// add children if precondition for COLLECT is met
							if (collect.preconditionsMet(newState)) {
								newState = collect.apply(newState);
								childList.add(newState);
							}
						}
					}
					
					// checks if current position is a townhall
					if (currentPosition.equals(this.townhallPosition)) {
						
						ApplyAction deposit = new ApplyAction(peasantList, currentID, currentPosition, this.townhallID, this.townhallPosition);
						GameState newState = new GameState(this, deposit);
						
						// add children if precondition for COLLECT is met
						if (deposit.preconditionsMet(newState)) {
							newState = deposit.apply(newState);
							childList.add(newState);
						}
					}
					
					// alternatively, move peasant
					if ((currentGold/requiredGold)<= (currentWood/requiredWood)) {
						ApplyAction move = new ApplyAction(peasantList, occupiedPositionList, currentID, currentPosition, getNearestResourcePosition(currentPosition, ResourceType.GOLD));
						GameState newState = new GameState(this, move);
	
						if (move.preconditionsMet(newState)) {
							newState = move.apply(newState);
							childList.add(newState);
						}
					}
					
					else if (((currentGold/requiredGold) > (currentWood/requiredWood))) {
						ApplyAction move = new ApplyAction(peasantList, occupiedPositionList, currentID, currentPosition, getNearestResourcePosition(currentPosition, ResourceType.WOOD));
						GameState newState = new GameState(this, move);
	
						if (move.preconditionsMet(newState)) {
							newState = move.apply(newState);
							childList.add(newState);
						}
					}

					ApplyAction move = new ApplyAction(peasantList, occupiedPositionList, currentID, currentPosition, this.townhallPosition);
					GameState newState = new GameState(this, move);

					if (move.preconditionsMet(newState)) {
						newState = move.apply(newState);
						childList.add(newState);
					}
	    		}
    		}
    	}
    	
        return childList;
    }
    
    // helper method to build new peasants for BUILD applyAction
    public void buildPeasant() {
    	// use up resources to build the peasant
    	this.currentGold = currentGold - requiredGold;
    	this.currentWood = currentWood - requiredWood;
    	
    	// adds a new peasant to the list of peasants
    	peasantList.add(new SimulatedUnit(peasantList,occupiedPositionList,xExtent,yExtent));
    	
    	// set target position of the action to the final position 
    	this.actionPerformed.setTargetPos(this.peasantList.get(peasantList.size()).getPosition());
    }
    
    // helper method to move peasants for MOVE applyAction
    public void movePeasant() {
    	// loop through list to find the peasant that should be moved and change the peasant's position accordingly
		for (int i = 0; i < peasantList.size(); i++) {
			if (peasantList.get(i).getID()==this.actionPerformed.getActorID()) {
				peasantList.get(i).setPosition(this.actionPerformed.getTargetPos());
			}
		}
    }
    
    // helper method to deposit cargo to townhall for DEPOSIT applyAction
    public void depositCargo() {
    	// loop through list to find the peasant that deposits the cargo
		for (int i = 0; i < peasantList.size(); i++) {
			if (peasantList.get(i).getID()==this.actionPerformed.getActorID()) {
				// increase current gold cargo carried is gold
				if (peasantList.get(i).getCargoType() == ResourceType.GOLD) {
					this.currentGold += peasantList.get(i).getCargoAmount();
				}
				// increase current wood if cargo carried is wood
				else if (peasantList.get(i).getCargoType() == ResourceType.WOOD) {
					this.currentWood += peasantList.get(i).getCargoAmount();
				}
				
				// clears the cargo amount carried by the peasant
				peasantList.get(i).setCargoAmount(0);
			}
		}
    }
    
    // helper method to collect resource
    public void collectResource() {
    	for (int i = 0; i < peasantList.size(); i++) {
			if (peasantList.get(i).getID()==this.actionPerformed.getActorID()) {
				for (int j = 0; j < resourceList.size(); j++) {
					// finds the resource that is being collected
					if ((resourceList.get(j).getID() == this.actionPerformed.getTargetID()) && (resourceList.get(j).getPosition().equals(this.actionPerformed.getTargetPos()))) {
						if (resourceList.get(j).getAmountRemaining()!=0) {
							// check if gold is collected
							if (resourceList.get(j).getResourceType().equals(ResourceType.GOLD)) {
								// set cargo type to gold
								peasantList.get(i).setCargoType(ResourceType.GOLD);
								
								// check if 100 gold can be removed from the mine
								int goldRemainingAfterCollect = resourceList.get(j).getAmountRemaining() - 100;
								
								if (goldRemainingAfterCollect > 0) {
									// set amount of gold carried to be 100
									peasantList.get(i).setCargoAmount(100);
									resourceList.get(j).setAmountRemaining(goldRemainingAfterCollect);
								}
								else {
									// set the amount of cargo carried to what is remaining
									peasantList.get(i).setCargoAmount(resourceList.get(j).getAmountRemaining());
									
									// clears the resource from the resource list as all gold are taken by peasant
									resourceList.remove(j);
									
									
								}
							}
							else {
								// set cargo type to wood
								peasantList.get(i).setCargoType(ResourceType.WOOD);
								
								// check if 100 wood can be removed from the forest
								int woodRemainingAfterCollect = resourceList.get(j).getAmountRemaining() - 100;
								
								if (woodRemainingAfterCollect > 0) {
									// set amount of wood carried to be 100
									peasantList.get(i).setCargoAmount(100);
									resourceList.get(j).setAmountRemaining(woodRemainingAfterCollect);
								}
								else {
									// set the amount of cargo carried to what is remaining
									peasantList.get(i).setCargoAmount(resourceList.get(j).getAmountRemaining());
									
									// clears the resource from the resource list as all wood are taken by peasant
									resourceList.remove(j);
									
								}
								
							}
						}
					}
				}
			}
		}
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {
        // TODO: Implement me!

    	/**
		 * provide a weight calculation for both wood and gold collection, according to the weight calculated, the 
		 * agent will choose which one of the resources to collect while the distance is calculated by chebyshev 
		 * distance.
		*/
		// checks if the game is in need of more gold or more wood
    	if(currentGold/requiredGold <= currentWood/requiredWood){
			int shortestDistance = Integer.MAX_VALUE;
			for (SimulatedUnit peasant: peasantList) {
				Position peasantPosition = peasant.getPosition();
				// get shortest distance to gold mine
				shortestDistance = Math.min(peasantPosition.chebyshevDistance(this.getNearestResourcePosition(peasantPosition,ResourceType.GOLD)), shortestDistance);
			}
			
			// heuristic is 1 over the shortest distance. Shorter distance = higher heuristic
			return 1/shortestDistance; 
		}
		else{
			int shortestDistance = Integer.MAX_VALUE;
			for (SimulatedUnit peasant: peasantList) {
				Position peasantPosition = peasant.getPosition();
				// get shortest distance to wood
				shortestDistance = Math.min(peasantPosition.chebyshevDistance(this.getNearestResourcePosition(peasantPosition, ResourceType.WOOD)), shortestDistance);
			}
			
			// heuristic is 1 over the shortest distance. Shorter distance = higher heuristic
			return 1/shortestDistance;
		}

    }
    
    /**
	 * helper getter method to get nearest resource to collect
	 * @param p: Indicates the current location
	 * @param resourceType: Indicates the type of resource we are looking for, 0 stands for wood, 1 stands for gold.
	 * @return Return the nearest position of the designated resourceType.
	 */ 
    public Position getNearestResourcePosition(Position p, ResourceType resourceType) {
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
    
    // gets nearest position without specifying gold or wood
    private Position getNearestResourcePosition(Position p) {
    	Position nearestPosition = null;
    	for (ResourceUnit list: resourceList) {
			
				Position resource = list.getPosition();
	    		// initially store one of the resource locations as nearestPosition
    			if (nearestPosition == null) {
    				nearestPosition = resource;
    			}
	    		// for all other resources, check if they are closer to the position p
    			else {
    				if (resource.chebyshevDistance(p) < nearestPosition.chebyshevDistance(p)) {
    					nearestPosition = resource;
    				}
    			}
		}
    	
    	return nearestPosition;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
		double goldWorth = 0.;
		double woodWorth = 0.;
    	for (int i = 0; i < peasantList.size(); i++) {
			// calculate harvested gold's worth
			for (Position resourcePosition: this.goldMineLocations) {
				if (peasantList.get(i).getPosition().equals(resourcePosition)) {
					goldWorth += 25.;
					if (peasantList.get(i).getCargoAmount() > 0 && peasantList.get(i).getCargoType() == ResourceType.GOLD) {
						goldWorth += 25.;
					}
					break;
				}
			}
			// calculate harvested wood's worth
			for (Position resourcePosition: this.treeLocations) {
				if (peasantList.get(i).getPosition().equals(resourcePosition)) {
					woodWorth += 25.;
					if (peasantList.get(i).getCargoAmount() > 0 && peasantList.get(i).getCargoType() == ResourceType.WOOD) {
						woodWorth += 25.;
					}
					break;
				}
			}
			// calculate deposited resource's worth
			if (peasantList.get(i).getPosition().equals(this.townhallPosition)) {
				if (peasantList.get(i).getCargoAmount() > 0) {
					if (peasantList.get(i).getCargoType() == ResourceType.GOLD) {
						goldWorth += 75.;
					}
					else {
						woodWorth += 75.;
					}
				}
			}
		}
		return Math.max(0., this.requiredGold - this.currentGold - goldWorth) + Math.max(0., this.requiredWood - this.currentWood - woodWorth);
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        // TODO: Check me!
        double cost1 = getCost();
        double cost2 = o.getCost();

        if (cost1 > cost2) {
            return 1;
        }
        else if (cost1 < cost2) {
            return -1;
        }

        return 0;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        // TODO: Implement me!
    	
    	// check if object is GameState
    	if (o instanceof GameState) {
    		GameState compare = (GameState) o;
    		
    		return (
    				// check if map boundary are the same
    				(this.xExtent== compare.xExtent) && (this.yExtent== compare.yExtent) && 
    				// check if required gold and wood are the same
    				(this.requiredGold== compare.requiredGold) && (this.requiredWood== compare.requiredWood) && 
    				// check if current gold and wood are the same
    				(this.currentGold== compare.currentGold) && (this.currentWood== compare.currentWood) && 
    				// check if peasantList is the same
    				(this.peasantList.equals(compare.peasantList))
    				);
    	}
        return false;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        // TODO: Implement me!
    	int hash = 0;
    	
    	// compare two game state objects with the specified fields
    	hash += this.playernum + this.requiredGold + this.requiredWood + this.xExtent + this.yExtent + this.currentGold + this.currentWood + this.townhallID + (int)this.getCost();
    	
    	for (SimulatedUnit unit: peasantList) {
    		hash += Objects.hash(unit.getPosition(),unit.getCargoAmount());
    	}
    	
    	// Object.hash 
    	
        return hash;
    }
	
} 