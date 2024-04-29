package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.Deposit;
import edu.cwru.sepia.agent.planner.actions.Harvest;
import edu.cwru.sepia.agent.planner.actions.Move;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.util.ArrayList;
import java.util.List;

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

    public final State.StateView state;
    // current resources
    public int gold;
    public int wood;
    public int food;
    // required gold & wood
    public int requiredGold;
    public int requiredWood;

    public boolean buildPeasants;

    private final int xExtent;
    private final int yExtent;

    private final List<UnitView> allUnits;
    private List<UnitView> playerUnits = new ArrayList<>();
    private UnitView townHall;
    private List<Peasant> peasantUnits = new ArrayList<>();

    //resources maps
    private final List<ResourceView> resourceNodes;
    private final boolean[][] map;
    private final int[][] woodMap;
    private final int[][] goldMap;

    private final int playernum; // The player number of agent

    private GameState parent = null; // The parent of the game state

    private double cost;
    private double heuristic;
    private List<StripsAction> plan;

    //getter
    public State.StateView getState() {
        return state;
    }

    public int getPlayerNum() {
        return playernum;
    }

    public GameState getParent() {
        return parent;
    }

    public int[][] getWoodMap(){
        return this.woodMap;
    }

    public boolean[][] getMap() {
        return map;
    }

    public int[][] getGoldMap(){
        return this.goldMap;
    }

    public int getXExtent() {
        return xExtent;
    }

    public int getYExtent() {
        return yExtent;
    }

    public int getGold() {
        return gold;
    }

    public int getWood() {
        return wood;
    }

    public int getRequiredGold() {
        return requiredGold;
    }

    public int getRequiredWood() {
        return requiredWood;
    }

    public List<UnitView> getPlayerUnits() {
        return playerUnits;
    }

    public List<UnitView> getAllUnits() {
        return allUnits;
    }

    public List<ResourceView> getResourceNodes() {
        return resourceNodes;
    }

    public boolean isBuildPeasants() {
        return buildPeasants;
    }

    public List<Peasant> getPeasantUnits() {
        return peasantUnits;
    }

    public StripsAction getPreviousAction() {
        return plan.get(0);
    }

    public List<StripsAction> getPlan(){
        return plan;
    }

    public double getCost() {
        return cost;
    }

    public double getHeuristic() {
        return heuristic;
    }

    // setter
    public void addCost(double cost) {
        this.cost += cost;
    }

    public void addGold(int gold) {
        this.gold += gold;
    }

    public void addWood(int wood) {
        this.wood += wood;
    }

    public void addFood(int food) {
        this.food += food;
    }

    public void addPlan(StripsAction action) {
        plan.add(action);
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

        // misc
        this.state = state;
        this.playernum = playernum;
        this.buildPeasants = buildPeasants;

        // resources
        this.requiredGold = requiredGold;
        this.gold = state.getResourceAmount(playernum, ResourceType.GOLD);
        this.requiredWood = requiredWood;
        this.wood = state.getResourceAmount(playernum, ResourceType.WOOD);
        this.food = state.getSupplyAmount(playernum);

        // map
        this.xExtent = state.getXExtent();
        this.yExtent = state.getYExtent();
        this.allUnits = state.getAllUnits();

        for (UnitView u : allUnits) {
            if (u.getTemplateView().getPlayer() == playernum) {
                if (u.getTemplateView().getName().equalsIgnoreCase("peasant")) {
                    this.playerUnits.add(u);

                    Position spawn = new Position(townHall.getXPosition(), townHall.getYPosition());
                    Peasant p = new Peasant(u.getID(), false, false, u.getCargoAmount(), u.getXPosition(), u.getYPosition(), spawn);

                    if (u.getCargoType() == ResourceType.GOLD) {
                        p.hasGold = true;
                        p.hasWood = false;
                    } else if (u.getCargoType() == ResourceType.WOOD) {
                        p.hasWood = true;
                        p.hasGold = false;
                    }
                    this.peasantUnits.add(p);
                } else if (u.getTemplateView().getName().equalsIgnoreCase("townhall")) {
                    this.townHall = u;
                }
            }
        }

        this.map = new boolean[xExtent][yExtent];
        this.goldMap = new int[xExtent][yExtent];
        this.woodMap = new int[xExtent][yExtent];
        for (int x = 0; x < xExtent; x++) { //Initialize
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
            else if (r.getType() == ResourceNode.Type.TREE) {
                woodMap[r.getXPosition()][r.getYPosition()] = r.getAmountRemaining();
            }
        }

        this.plan = new ArrayList<>();
        this.cost = getCost();
        this.parent = null;
        this.heuristic = heuristic();
    }

    // constructor for children
    public GameState(GameState copiedState) {
        // misc
        this.state = copiedState.state;
        this.parent = copiedState;
        this.playernum = copiedState.playernum;
        this.buildPeasants = copiedState.buildPeasants;
        this.townHall = copiedState.townHall;

        // resources
        this.requiredGold = copiedState.requiredGold;
        this.requiredWood = copiedState.requiredWood;
        this.wood = copiedState.wood;
        this.gold = copiedState.gold;
        this.food = copiedState.food;

        // map

        this.xExtent = copiedState.xExtent;
        this.yExtent = copiedState.yExtent;

        List<ResourceView> copiedResources = new ArrayList<>();
        for (ResourceView r : copiedState.getResourceNodes()) {
            ResourceView toCopy = new ResourceView(new ResourceNode(r.getType(), r.getXPosition(), r.getYPosition(), r.getAmountRemaining(), r.getID()));
            copiedResources.add(toCopy);
        }
        this.resourceNodes = copiedResources;

        boolean[][] originalMap = copiedState.map;
        boolean[][] copiedMap = new boolean[originalMap.length][originalMap[0].length];
        for (int x = 0; x < originalMap.length; x++) {
            System.arraycopy(originalMap[x], 0, copiedMap[x], 0, originalMap[0].length);
        }
        this.map = copiedMap;

        int[][] originalGoldMap = copiedState.goldMap;
        int[][] copiedGoldMap = new int[originalGoldMap.length][originalGoldMap[0].length];
        for (int x = 0; x < originalGoldMap.length; x++) {
            System.arraycopy(originalGoldMap[x], 0, copiedGoldMap[x], 0, originalGoldMap[0].length);
        }
        this.goldMap = copiedGoldMap;

        int[][] originalWoodMap = copiedState.goldMap;
        int[][] copiedWoodMap = new int[originalGoldMap.length][originalGoldMap[0].length];
        for (int x = 0; x < originalWoodMap.length; x++) {
            System.arraycopy(originalGoldMap[x], 0, copiedWoodMap[x], 0, originalWoodMap[0].length);
        }
        this.woodMap = copiedWoodMap;

        List<UnitView> copiedUnits = new ArrayList<>();
        for (UnitView uv : copiedState.allUnits) {
            Unit unit = new Unit(new UnitTemplate(uv.getID()), uv.getID());
            unit.setxPosition(uv.getXPosition());
            unit.setyPosition(uv.getYPosition());
            unit.setCargo(uv.getCargoType(), uv.getCargoAmount());
            copiedUnits.add(new UnitView(unit));
        }
        this.allUnits = copiedUnits;

        List<UnitView> copiedPlayerUnits = new ArrayList<>();
        for (UnitView uv : copiedState.playerUnits) {
            Unit unit = new Unit(new UnitTemplate(uv.getID()), uv.getID());
            unit.setxPosition(uv.getXPosition());
            unit.setyPosition(uv.getYPosition());
            unit.setCargo(uv.getCargoType(), uv.getCargoAmount());
            copiedPlayerUnits.add(new UnitView(unit));
        }
        this.playerUnits = copiedPlayerUnits;

        List<Peasant> copiedPeasants = new ArrayList<>();
        for (Peasant p : copiedState.peasantUnits) {
            Peasant unit = new Peasant(p.id, p.hasGold, p.hasWood, p.cargoAmount, p.x, p.y, p.neighbor);
            copiedPeasants.add(unit);
        }
        this.peasantUnits = copiedPeasants;

        this.cost = getCost();
        this.plan = new ArrayList<>(copiedState.getPlan());

        this.heuristic = heuristic();
    }

    public GameState getChild(StripsAction action) {
        GameState result = parentAction.apply(this);
        this.childAction = action;
        result.cost = getCost();
        result.parent = this;
        return result;
    }

    public UnitView findUnit(int unitId, List<UnitView> units) {
        for (UnitView u : units) {
            if (u.getID() == unitId) {
                return u;
            }
        }
        return null;
    }

    public ResourceView findResource(int x, int y, List<ResourceView> resources) {
        for (ResourceView r : resources) {
            if (r.getXPosition() == x && r.getYPosition() == y) {
                return r;
            }
        }
        return null;
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        return this.getGold() >= this.requiredGold && this.getWood() >= this.requiredWood;
    }

    private List<Peasant> copyPeasant (List<Peasant> peasants) {
        List<Peasant> copiedPeasants = new ArrayList<>();
        for (Peasant p : peasants) {
            Peasant unit = new Peasant(p.id, p.hasGold, p.hasWood, p.cargoAmount, p.x, p.y, p.neighbor);
            copiedPeasants.add(unit);
        }
        return copiedPeasants;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        List<GameState> result = new ArrayList<>();
        List<Peasant> copiedPeasant = copyPeasant(this.peasantUnits);
        int PeasantSize = copiedPeasant.size();
        List<List<Peasant>> allCombinations = new ArrayList<>();

        for (int i = 0; i < (1 << PeasantSize); i++) {
            List<Peasant> currentSet = new ArrayList<>();
            for (int j = 0; j < PeasantSize; j++) {
                if ((i & (1 << j)) > 0) {
                    currentSet.add(copiedPeasant.get(i));
                }
            }
            allCombinations.add(currentSet);
        }
        allCombinations.remove(0);

        for (List<Peasant> combination : allCombinations) {
            Position bestGoldMine = findMostGold(new Position(combination.get(0).x, combination.get(0).y));
            if (bestGoldMine != null) {
                Move moveActionGold = new Move(combination, bestGoldMine, this);
            }
        }

        for (UnitView uv : playerUnits) {
            for (Direction dir : Direction.values()) {
                int x = uv.getXPosition() + dir.xComponent();
                int y = uv.getYPosition() + dir.yComponent();
                Position newPosition = new Position(x, y);
                UnitView th = townHall;

                Deposit depositGold = new Deposit(newPosition, th, uv, this, true);
                if (depositGold.preconditionsMet(this)) {
                    System.out.println("dgold");
                    result.add(depositGold.apply(this));
                }

                Deposit depositWood = new Deposit(newPosition, th, uv, this, false);
                if (depositWood.preconditionsMet(this)) {
                    System.out.println("dwood");
                    result.add(depositWood.apply(this));
                }

                Harvest harvestGold = new Harvest(newPosition, uv, this, true);
                if (harvestGold.preconditionsMet(this)) {
                    System.out.println("hwood");
                    result.add(harvestGold.apply(this));
                }

                Harvest harvestWood = new Harvest(newPosition, uv, this, true);
                if (harvestWood.preconditionsMet(this)) {
                    System.out.println("hwood");
                    result.add(harvestWood.apply(this));
                }
            }
            Position bestGold = findMostGold(new Position(uv.getXPosition(), uv.getYPosition()));
            System.out.println("gold");
            bestMove(result, uv, bestGold);

            Position bestWood = findMostWood(new Position(uv.getXPosition(), uv.getXPosition()));
            System.out.println("wood");
            bestMove(result, uv, bestWood);

            Position thPosition = new Position(townHall.getXPosition(), townHall.getYPosition());
            System.out.println("move");
            bestMove(result, uv, thPosition);
        }
        return result;
    }

    private void bestMove(List<GameState> possibles, UnitView unit, Position position) {
        for (Direction dir : Direction.values()) {
            int x = position.x + dir.xComponent();
            int y = position.y + dir.yComponent();

            Move move = new Move(new Position(x, y), unit, this);
            if (move.preconditionsMet(this)) {
                System.out.println("move" + " " + x + " " + y);
                possibles.add(move.apply(this));
            }
        }
    }

    public Position findMostGold(Position position) {
        return getPosition(position, requiredGold, gold, goldMap);
    }

    public Position findMostWood(Position position) {
        return getPosition(position, requiredWood, wood, woodMap);
    }

    private Position getPosition(Position position, int requiredWood, int currentRes, int[][] map) {
        Position result = null;
        int resource = 0;
        int currentDistance = 0;
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                int currentBest = map[x][y];
                if (currentBest > 0) {
                    int distance = position.chebyshevDistance(new Position(x, y));
                    System.out.println(resource);
                    if (result == null) {
                        result = new Position(x, y);
                        currentDistance = distance;
                        resource = currentBest;
                    } else {
                        if (currentDistance <= distance) {
                            if (currentBest >= resource || currentBest >= requiredWood - currentRes) {
                                result = new Position(x, y);
                                currentDistance = distance;
                                resource = currentBest;
                            }
                        }
                    }
                }
            }
        }
        return result;
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
        int goldDiff = requiredGold - gold;
        int woodDiff = requiredWood - wood;
        double result = goldDiff + woodDiff;


        UnitView peasant = playerUnits.get(0);
        Position unitPosition = new Position(peasant.getXPosition(), peasant.getYPosition());


        if (peasant.getCargoAmount() > 0) {
            Position th = new Position(townHall.getXPosition(), townHall.getYPosition());
            result -= peasant.getCargoAmount() * 0.5;
            double disFromTh =th.chebyshevDistance(unitPosition);
            result += disFromTh;
        }
        else {
            Position bestResource = findBestResource(unitPosition);
            result += bestResource.chebyshevDistance(unitPosition) * 0.5;
            double harvestAmount = 0;
            for(Direction d : Direction.values()) {
                int x = peasant.getXPosition() + d.xComponent();
                int y = peasant.getYPosition() + d.yComponent();
                int goldAmount = goldMap[x][y];
                int woodAmount = woodMap[x][y];
                if (goldAmount > 0) {
                    if (requiredGold - gold > 0) {
                        if (goldAmount > 100) {
                            harvestAmount = 100;
                        }
                        else {
                            harvestAmount = goldMap[x][y];
                        }
                    }
                }
                if (woodAmount > 0) {
                    if (requiredWood - wood > 0) {
                        if (woodAmount > 100) {
                            harvestAmount = 100;
                        }
                        else {
                            harvestAmount = woodMap[x][y];}
                    }
                }
            }

            result -= harvestAmount * 0.3;
        }


        return result;
    }

    private Position findBestResource(Position currentPosition) {
        // determine which resource gap is larger
        int goldNeeded = requiredGold - gold;
        int woodNeeded = requiredWood - wood;
        boolean prioritizeGold = goldNeeded > woodNeeded;

        // switch to the correct map and other params
        int[][] resourceMap = prioritizeGold ? goldMap : woodMap;
        int requiredResource = prioritizeGold ? requiredGold : requiredWood;
        int currentResource = prioritizeGold ? gold : wood;

        // initialize params to tell if best position
        Position bestPosition = null;
        int minDistance = Integer.MAX_VALUE;
        int maxResourceAtBest = 0;

        for (int i = 0; i < resourceMap.length; i++) {
            for (int j = 0; j < resourceMap[i].length; j++) {
                int resourceAtPosition = resourceMap[i][j];
                if (resourceAtPosition > 0) {
                    int distance = currentPosition.chebyshevDistance(new Position(i, j));
                    boolean isCloserOrEqual = distance <= minDistance;
                    boolean isBetterResource = resourceAtPosition > maxResourceAtBest;
                    boolean meetsResourceNeed = resourceAtPosition >= requiredResource - currentResource;

                    if ((bestPosition == null) || (isCloserOrEqual && (isBetterResource || meetsResourceNeed))) {
                        bestPosition = new Position(i, j);
                        minDistance = distance;
                        maxResourceAtBest = resourceAtPosition;
                    }
                }
            }
        }

        return bestPosition;
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
        return Double.compare(this.getCost(), o.getCost());
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
            return this.gold == ((GameState) o).getGold()
                    && this.wood == ((GameState) o).getWood()
                    && this.allUnits.equals(((GameState) o).getAllUnits())
                    && this.heuristic() == ((GameState) o).heuristic();
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
        return ((this.gold + this.wood)% 37);
    }
}