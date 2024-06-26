package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.UnitTemplate;

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

    State.StateView state;
	private int playerNum;
	private int requiredGold, requiredWood, currentGold, currentWood, currentFood;
	private boolean buildPeasants;
	private int xExtent, yExtent;

	private List<ResourceView> resourceNodes;
	private boolean[][] map;
	private int[][] goldMap, woodMap;

	private List<UnitView> allUnits;
    private List<UnitView> playerUnits = new ArrayList<UnitView>();
    private UnitView townHall;
    private List<Peasant> peasantUnits = new ArrayList<>();

	private double cost;
	private double heuristic;
	private List<StripsAction> plan;
	private GameState parent;


	// Get & Set Methods 
	public State.StateView getState() {
		return state;
	}

    public int getPlayerNum() {
		return playerNum;
	}
	public int getxExtent() {
		return xExtent;
	}
	public int getyExtent() {
		return yExtent;
	}

    public int getRequiredGold() {
		return requiredGold;
	}
	public int getRequiredWood() {
		return requiredWood;
	}
    public int getCurrentGold() {
		return currentGold;
	}
	public int getCurrentWood() {
		return currentWood;
	}
    public int getCurrentFood() {
		return currentFood;
	}

	public List<ResourceView> getResourceNodes() {
		return resourceNodes;
	}
    public UnitView getTownHall() {
		return townHall;
	}
	public void setTownHall(UnitView townHall) {
		this.townHall = townHall;
	}
	public boolean[][] getMap() {
		return map;
	}
	public int[][] getGoldMap() {
		return this.goldMap;
	}
	public int[][] getWoodMap() {
		return this.woodMap;
	}

	public List<UnitView> getAllUnits() {
		return allUnits;
	}
	public List<UnitView> getPlayerUnits() {
		return playerUnits;
	}
    public boolean isBuildPeasants() {
        return buildPeasants;
    }
    public List<Peasant> getPeasantUnits() {
		return this.peasantUnits;
	}

	public GameState getParent() {
		return parent;
	}
    public StripsAction getPreviousAction() {
    	return plan.get(0);
    }
	public List<StripsAction> getPlan() {
		return this.plan;
	}



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
		// assign variables
        this.state = state;
		this.playerNum = playernum;
		this.buildPeasants = buildPeasants;
		this.xExtent = state.getXExtent();
		this.yExtent = state.getYExtent();
		this.allUnits = state.getAllUnits();
		// Assign position information & state information
        for (UnitView u : allUnits) {
			if (u.getTemplateView().getPlayer() == playerNum) {
				if (u.getTemplateView().getName().toLowerCase().equals("peasant")) {
					playerUnits.add(u);

                    Position born_place = new Position(townHall.getXPosition(), townHall.getYPosition());
                    Peasant p = new Peasant(u.getID(), u.getXPosition(), u.getYPosition(), false, false, u.getCargoAmount(), born_place);

					if (u.getCargoType() == ResourceType.GOLD) {
						p.hasGold = true;
                        p.hasWood = false;
					} else if (u.getCargoType() == ResourceType.WOOD) {
                        p.hasGold = false;
						p.hasWood = true;
					}
					this.peasantUnits.add(p);
				}
                else if (u.getTemplateView().getName().toLowerCase().equals("townhall")) {
					this.townHall = u;
				}
			}
		}
        // map & resources
		this.map = new boolean[xExtent][yExtent];
		this.goldMap = new int[xExtent][yExtent];
		this.woodMap = new int[xExtent][yExtent];
		for (int x = 0; x < xExtent; x++) {
			for (int y = 0; y < yExtent; y++) {
				map[x][y] = false;
				goldMap[x][y] = 0;
				woodMap[x][y] = 0;
			}
		}

        map[townHall.getXPosition()][townHall.getYPosition()] = true;

        this.resourceNodes = state.getAllResourceNodes();
		for (ResourceView r : resourceNodes) {
			map[r.getXPosition()][r.getYPosition()] = true;
			if (r.getType() == ResourceNode.Type.GOLD_MINE) {
				goldMap[r.getXPosition()][r.getYPosition()] = r.getAmountRemaining();
			}
			if (r.getType() == ResourceNode.Type.TREE) {
				woodMap[r.getXPosition()][r.getYPosition()] = r.getAmountRemaining();
			}
		}
		
		// Stuffs
        this.requiredGold = requiredGold;
		this.requiredWood = requiredWood;
		this.currentGold = state.getResourceAmount(playernum, ResourceType.GOLD);
		this.currentWood = state.getResourceAmount(playernum, ResourceType.WOOD);
        this.currentFood = state.getSupplyAmount(playernum);



		this.plan = new ArrayList<StripsAction>();
		this.cost = getCost();
        this.parent = null;
		this.heuristic = heuristic();
	}



    // Assign Parents
	public GameState(GameState copy) {
		// Set the parent state to the given state
		this.parent = copy;
		this.state = copy.state;
		this.playerNum = copy.playerNum;
        this.xExtent = copy.xExtent;
		this.yExtent = copy.yExtent;
        this.buildPeasants = copy.buildPeasants;

		this.requiredGold = copy.requiredGold;
		this.requiredWood = copy.requiredWood;

		this.currentGold = copy.currentGold;
		this.currentWood = copy.currentWood;
        this.currentFood = copy.currentFood;

        // Copy list of resource nodes
		List<ResourceView> cResourceNode = new ArrayList<ResourceView>();
		for (ResourceView r : copy.resourceNodes) {
			ResourceView rv = new ResourceView(new ResourceNode(r.getType(), r.getXPosition(),
					r.getYPosition(), r.getAmountRemaining(), r.getID()));
			cResourceNode.add(rv);
		}
		this.resourceNodes = cResourceNode;

		// Deep copy
		boolean[][] originalMap = copy.map;
		boolean[][] cMap = new boolean[originalMap.length][originalMap[0].length];
		for (int i = 0; i < originalMap.length; i++) {
			for (int j = 0; j < originalMap[0].length; j++) {
				cMap[i][j] = originalMap[i][j];
			}
		}
		this.map = cMap;
		
		// copy Gold map
		int[][] originalGmap = copy.goldMap;
		int[][] cGmap = new int[originalGmap.length][originalGmap[0].length];
		for (int i = 0; i < originalGmap.length; i++) {
			for (int j = 0; j < originalGmap[0].length; j++) {
				cGmap[i][j] = originalGmap[i][j];
			}
		}
		this.goldMap = cGmap;

		//copy wood map
		int[][] originalWmap = copy.woodMap;
		int[][] cWmap = new int[originalWmap.length][originalWmap[0].length];
		for (int i = 0; i < originalWmap.length; i++) {
			for (int j = 0; j < originalWmap[0].length; j++) {
				cWmap[i][j] = originalWmap[i][j];
			}
		}
		this.woodMap = cWmap;

		List<UnitView> cUnits = new ArrayList<UnitView>();
		for (UnitView uv : copy.allUnits) {
			Unit unit = new Unit(new UnitTemplate(uv.getID()), uv.getID());
			unit.setxPosition(uv.getXPosition());
			unit.setyPosition(uv.getYPosition());
            unit.setCargo(uv.getCargoType(), uv.getCargoAmount());
			cUnits.add(new UnitView(unit));
		}
		this.allUnits = cUnits;

		// Copy player-specific units.
		List<UnitView> cPlayerUnits = new ArrayList<UnitView>();
		for (UnitView uv : copy.playerUnits) {
			Unit unit = new Unit(new UnitTemplate(uv.getID()), uv.getID());
			unit.setxPosition(uv.getXPosition());
			unit.setyPosition(uv.getYPosition());
            unit.setCargo(uv.getCargoType(), uv.getCargoAmount());
			cPlayerUnits.add(new UnitView(unit));
		}
		this.playerUnits = cPlayerUnits;

		// Copy Peasants
        List<Peasant> cPeasantUnits = new ArrayList<>();
		for (Peasant p : copy.peasantUnits) {
			Peasant p_unit = new Peasant(p.id, p.x, p.y, p.hasGold, p.hasWood, p.cargoAmount, p.neighbor);
			cPeasantUnits.add(p_unit);
		}
		this.peasantUnits = cPeasantUnits;

		// Calculate and set the cost of this state
		this.cost = getCost();
		// Copy the action plan
		List<StripsAction> copiedList = new ArrayList<StripsAction>();
		for (StripsAction element : copy.getPlan()) {
			copiedList.add(element);
		}
		this.plan = copiedList;
		this.townHall = copy.townHall;
		// heuristic value
		this.heuristic = heuristic();
	}

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
	public boolean isGoal() {
		return (this.getCurrentGold() >= this.requiredGold && this.getCurrentWood() >= this.requiredWood);
	}

    /**
     * Write the function that computes the current cost to get to this node. This
     * is combined with your heuristic to determine which actions/states are better
     * to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
        return this.cost;
    }

    // Change resources amount
    public void addGold(int n) {
        currentGold += n;
    }
    public void addWood(int n) {
        currentWood += n;
    }
    public void addFood(int foodAdded) {
		currentFood += foodAdded;
	}
    public void addCost(double n) {
        this.cost += n;
    }
    
    // record plan
    public void addPlan(StripsAction action) {
        plan.add(action);
    }
    public double getHeuristic() {
        return this.heuristic;
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
		double thisTotalCost = this.getCost() + this.heuristic();
		double totalCost = o.getCost() + o.heuristic();

		return (int) ((int) thisTotalCost - totalCost);
	}


	// Copy peasants
    private List<Peasant> copyPeasants(List<Peasant> to_copy) {
        List<Peasant> cPeasantUnits = new ArrayList<>();
		for (Peasant p : to_copy) {
			Peasant p_unit = new Peasant(p.id, p.x, p.y, p.hasGold, p.hasWood, p.cargoAmount, p.neighbor);
			// add new peasant to list
			cPeasantUnits.add(p_unit);
		}
		return cPeasantUnits;
	}

	/**
	 * The branching factor of this search graph are much higher than the planning.
	 * Generate all of the possible successor states and their associated actions in
	 * this method.
	 *
	 * @return A list of the possible successor states and their associated actions
	 */
    public List<GameState> generateChildren() {
    	// result list 
 		List<GameState> children = new ArrayList<>();
 		// avoid modifying the original list
 		List<Peasant> peasantsCopy = copyPeasants(this.peasantUnits);
 		// number of peasants
 		int n = peasantsCopy.size();
 		List<List<Peasant>> allCombinations = new ArrayList<>();
 		
 		for (int i = 0; i < (1 << n); i++) { 
 			List<Peasant> currentSet = new ArrayList<>();
 			for (int j = 0; j < n; j++) {
 				if ((i & (1 << j)) > 0) {
 					currentSet.add(peasantsCopy.get(j));
 				}
 			}
 			allCombinations.add(currentSet);
 		}
 		List<List<Peasant>> powerSet = allCombinations;
 		powerSet.remove(0);
 		
 		// Iterate over each subset of peasants
 		for (List<Peasant> s : powerSet) {
 			// Find the position with the most gold closest
 			Position bestGoldMine = findMostGold(new Position(s.get(0).x, s.get(0).y));
 			// Generate a move action to the best gold mine
 			if (bestGoldMine != null) {
 				Move_k moveKActionGold = new Move_k(s, bestGoldMine, this);
 				if (moveKActionGold.preconditionsMet(this)) {
 					children.add(moveKActionGold.apply(this));
 				}
 			}
 			
 			// Find the position with the most wood closest to the first peasant's position.
 			Position bestWood = findMostWood(
 					new Position(peasantUnits.get(0).x, peasantUnits.get(0).y));
 			if (bestWood != null) {
 				Move_k moveKActionWood = new Move_k(s, bestWood, this);
 				if (moveKActionWood.preconditionsMet(this)) {
 					children.add(moveKActionWood.apply(this));
 				}
 			}

 			Position townhallPosition = new Position(this.townHall.getXPosition(), this.townHall.getYPosition());
 			Move_k moveKActionTownhall = new Move_k(s, townhallPosition, this);
 			if (moveKActionTownhall.preconditionsMet(this)) {
 				children.add(moveKActionTownhall.apply(this));
 			}



 		    // Generate a harvest action for gold
 			Harvest_k harvestKGold = new Harvest_k(s, bestGoldMine, this);
 			if (harvestKGold.preconditionsMet(this)) {
 				children.add(harvestKGold.apply(this));
 			}
 			// Generate a harvest action for gold
 			Harvest_k harvestKWood = new Harvest_k(s, bestWood, this);
 			if (harvestKWood.preconditionsMet(this)) {
 				children.add(harvestKWood.apply(this));
 			}
 			// Generate a deposit action at the town hall
 			Deposit_k depositKAction = new Deposit_k(s, townhallPosition, this);
 			if (depositKAction.preconditionsMet(this)) {
 				children.add((depositKAction.apply(this)));
 			}
 		}
 		// generate a new peasant if preconditions are met
 		BuildPeasant buildPeasant = new BuildPeasant(this);
 		if (buildPeasant.preconditionsMet(this)) {
 			children.add(buildPeasant.apply(this));
 		}

 		return children;
 	}
    
 	public Position findMostGold(Position currentPosition) {
 		Position result = null;  // Initialize the result as null to store the position of the best gold source.
 	    int resource = 0;  // To store the amount of gold at the best found position.
 	    int dist = 0;  // To store the distance to the best gold source found.
 	    int requiredAmount = requiredGold;  // The total amount of gold needed.
 	    int currentAmount = currentGold;  // The current amount of gold held.
 	    int [][] map = goldMap;  // Reference to the gold map showing gold distribution.
 	    // Loop through each cell in the gold map.
 	    for(int i = 0; i < map.length; i++) {
 	        for(int j = 0; j < map[i].length; j++) {
 	            int currentBest = map[i][j]; // Amount of gold at the current map cell.
 	            
 	            // Check if there is any gold at the current cell.
 	            if(currentBest > 0) {
 	                // Use the Chebyshev distance from the current position to the current cell.
 	                int distance = currentPosition.chebyshevDistance(new Position(i, j));
 	                
 	                // If no best position has been found yet
 	                //or the current one is being considered for the first time.
 	                if(result == null) {
 	                    // Set the current position as the best gold source.
 	                    result = new Position(i, j);
 	                    dist = distance;
 	                    resource = currentBest;
 	                } else {
 	                    // If a best position is already set, check if the current position is closer or equally close.
 	                    if(distance <= dist) {
 	                        // If the current position has more gold or enough gold to meet the requirement minus what's already collected.
 	                        if(currentBest >= resource || currentBest >= requiredAmount - currentAmount) {
 	                            result = new Position(i, j);
 	                            dist = distance;
 	                            resource = currentBest;
 							}
 						}
 					}
 				}
 			}
 		}

 		return result;
 	}
 	
 	// Use similar to Find Wood
 	public Position findMostWood(Position currentPosition) {
 		Position result = null;
 		int resource = 0;
 		int dist = 0;
 		int requiredAmount = requiredWood;
 		int currentAmount = currentWood;
 		int [][] map = woodMap;
 		for(int i = 0; i < map.length; i ++) {
 			for(int j = 0; j < map[i].length; j ++) {
 				int currentBest = map[i][j];
 				if(currentBest > 0) {
 					int distance = currentPosition.chebyshevDistance(new Position(i, j));
 					if(result == null) {
 						result = new Position(i, j);
 						dist = distance;
 						resource = currentBest;
 					}
 					else {
 						if(distance <= dist) {
 							if(currentBest >= resource || currentBest >= requiredAmount - currentAmount) {
 								result = new Position(i, j);
 								dist = distance;
 								resource = currentBest;
 							}
 						}
 					}
 				}
 			}
 		}

 		return result;
 	}

 	// return current Unit & Peasant & Resource
    public UnitView findUnit(int unitId, List<UnitView> units) {
    	// iterate units
        for (UnitView u : units) {
            if (u.getID() == unitId) {
                return u;
            }
        }
        return null;
    }
	public Peasant findPeasant(int peasantId, List<Peasant> peasants) {
		//iterate peasants
		for (Peasant p : peasants) {
			if (p.id == peasantId) {
				return p;
			}
		}
		return null;
	}
    public ResourceView findResource(int x, int y, List<ResourceView> resources) {
    	//iterate resources
        for (ResourceView r : resources) {
            if (r.getXPosition() == x && r.getYPosition() == y) {
                return r;
            }
        }
        return null;
    }

	/**
	 * Write your heuristic function here. Remember this must be admissible for the
	 * properties of A* to hold. If you can come up with an easy way of computing a
	 * consistent heuristic that is even better, but not strictly necessary.
	 *
	 * Add a description here in your submission explaining your heuristic.
	 *
	 * @return The value estimated remaining cost to reach a goal state from this
	 *         state.
	 */
     public double heuristic() {
    	 // To know resources we still need to collection
        int goldDiff = requiredGold - currentGold;
     	int woodDiff = requiredWood - currentWood;
        double result = goldDiff + woodDiff;

        // iterate over all peasants to see if they are available 
		for (Peasant p : peasantUnits) {
			double p_cargo_amount = (double) p.cargoAmount;
			// If taking cargo
			if (p_cargo_amount > 0) {
				// Available for deposit
				if (can_deposit(p)) {
					result -= p_cargo_amount * 0.75;
				}
				else {
					result -= p_cargo_amount * 0.5;
				}
			}

			else {
                int harvest_amount = try_harvest(p);
                if (harvest_amount > 0) {
                    result -= harvest_amount * 0.25;
                }
			}
		}

		// Heuristic
 		this.heuristic = result / peasantUnits.size();
 		return result;
 	}

    // Check if able to deposit
	public boolean can_deposit(Peasant p) {
		if (Math.abs(p.x - townHall.getXPosition()) <= 1 && Math.abs(p.y - townHall.getYPosition()) <= 1) {
			return true;
		}
		return false;
	}

	public int try_harvest(Peasant p) {
	    int x = p.x; // Store the peasant's position
	    int y = p.y;
	    int holder = 0; //maximum resources

	    // Loop over the area (3x3 grid centered on the peasant).
	    for (int i = x - 1; i <= x + 1; i++) {
	        for (int j = y - 1; j <= y + 1; j++) {
	            // either gold or wood exist.
	            if (getGoldMap()[i][j] != 0 || getWoodMap()[i][j] != 0) {
	                // Calculate the total resources at the current cell.
	                int temp = getGoldMap()[i][j] + getWoodMap()[i][j];
	                // Update holder if the current cell has more resources than previously recorded.
	                if (temp > holder) {
	                    holder = temp;
	                }
	            }
	        }
	    }

	    // Cap the harvest amount at 100 units.
	    if (holder > 100) {
	        holder = 100;
	    }
	    
	    return holder;
	}







    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
	public boolean equals(Object o) {
		if (o instanceof GameState) {
			// check if important variables are the same. If are the same, take as same state.
			if (this.currentGold == ((GameState) o).getCurrentGold()
				&& this.currentWood == ((GameState) o).getCurrentWood()
				&& this.allUnits.equals(((GameState) o).getAllUnits())
				&& this.heuristic() == ((GameState) o).heuristic()) {

                return true;
			}
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
		return 0; // since we did equals() this seems trivial?
	}



}