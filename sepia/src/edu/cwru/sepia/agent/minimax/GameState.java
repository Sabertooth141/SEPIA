package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.*;
/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

    static class MapLocation implements Comparable<MapLocation> {
        public int x, y;
        public MapLocation cameFrom;
        public float cost;

        public MapLocation(int x, int y, MapLocation cameFrom, float cost) {
            this.x = x;
            this.y = y;
            this.cameFrom = cameFrom;
            this.cost = cost;
        }

        public MapLocation(int x, int y) {
            this.x = x;
            this.y = y;
            this.cameFrom = null;
            this.cost = 0;
        }

        @Override
        public int compareTo(MapLocation other) {
            return Float.compare(this.cost, other.cost);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MapLocation that = (MapLocation) obj;
            return (x == that.x && y == that.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    // Map boundary
    private int xExtent;
    private int yExtent;

    // Set of resource locations in the map
    private List<Integer> resourceIds;
    private Set<MapLocation> resourceLocations;

    // List of player and enemy units
    private List<Integer> playerUnitIDs;
    private List<Integer> enemyUnitIDs;

    // Set of player and enemy unitview
    private Set<Unit.UnitView> playerUnitView;
    private Set<Unit.UnitView> enemyUnitView;

    // Hashmap of the resulting moves
    private HashMap<Integer, Action> hashMap;

    // Set of simulated child units after the action is taken
    private Set<AppliedUnit> appliedUnitList;


    // tell if it's player's move
    private boolean atMaxNode;

    // heuristic value
    private double heuristic;


    // Simulated unit constructor(similar to map location)
    static class AppliedUnit {
        private int x;
        private int y;
        private int hp;
        private final int unitID;
        private Direction direction;
        private final Unit.UnitView oldUnit;

        // Constructor taking Unit and its params
        public AppliedUnit(Unit.UnitView oldUnit) {
            this.oldUnit = oldUnit;
            x = oldUnit.getXPosition();
            y = oldUnit.getYPosition();
            hp = oldUnit.getHP();
            unitID = oldUnit.getID();
        }

        // Constructor taking another applied unit and its params
        public AppliedUnit(AppliedUnit oldUnit) {
            this.oldUnit = oldUnit.oldUnit;
            x = oldUnit.x;
            y = oldUnit.y;
            hp = oldUnit.hp;
            unitID = oldUnit.unitID;
        }

        private int getXPosition() {
            return x;
        }

        private int getYPosition() {
            return y;
        }

        private int getHP() {
            return hp;
        }

        // Refresh new direction
        private void move(Direction direction) {
            this.direction = direction;
            this.x += direction.xComponent();
            this.y += direction.yComponent();
        }

        // If taken damage, change the hp
        private void getAttacked() {
            hp -= oldUnit.getTemplateView().getBasicAttack();
        }

    }

    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     * <p>
     * You may find the following state methods useful:
     * <p>
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIds(): returns the IDs of all of the obstacles in the map
     * state.getResourceNode(int resourceID): Return a ResourceView for the given ID
     * <p>
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     * <p>
     * You can get a list of all the units belonging to a player with the following command:
     * state.getUnitIds(int playerNum): gives a list of all unit IDs beloning to the player.
     * You control player 0, the enemy controls player 1.
     * <p>
     * In order to see information about a specific unit, you must first get the UnitView
     * corresponding to that unit.
     * state.getUnit(int id): gives the UnitView for a specific unit
     * <p>
     * With a UnitView you can find information about a given unit
     * unitView.getXPosition() and unitView.getYPosition(): get the current location of this unit
     * unitView.getHP(): get the current health of this unit
     * <p>
     * SEPIA stores information about unit types inside TemplateView objects.
     * For a given unit type you will need to find statistics from its Template View.
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit type deals
     * unitView.getTemplateView().getBaseHealth(): The initial amount of health of this unit type
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
        this.xExtent = state.getXExtent();
        this.yExtent = state.getYExtent();

        this.resourceIds = state.getAllResourceIds();

        this.playerUnitIDs = state.getUnitIds(0);
        this.enemyUnitIDs = state.getUnitIds(1);
        if (!resourceIds.isEmpty()) {
            this.resourceLocations = new HashSet<>();

            for (Integer id : resourceIds) {
                resourceLocations.add(new MapLocation(state.getResourceNode(id).getXPosition(), state.getResourceNode(id).getYPosition()));
            }
        }

        if (!playerUnitIDs.isEmpty()) {
            this.playerUnitView = new HashSet<>();
            for (Integer id : playerUnitIDs) {
                playerUnitView.add(state.getUnit(id));
            }
        } else {
            System.err.println("No player unit found");
        }

        if (!enemyUnitIDs.isEmpty()) {
            this.enemyUnitView = new HashSet<>();
            for (Integer id : enemyUnitIDs) {
                enemyUnitView.add(state.getUnit(id));
            }
        } else {
            System.err.println("No enemy unit found");
        }
    }

    public GameState(GameState gameState, HashMap<Integer, Action> nextHashMap) {
        xExtent = gameState.xExtent;
        yExtent = gameState.yExtent;

        resourceLocations = gameState.resourceLocations;
        playerUnitIDs = gameState.playerUnitIDs;
        enemyUnitIDs = gameState.enemyUnitIDs;
        playerUnitView = gameState.playerUnitView;
        enemyUnitView = gameState.enemyUnitView;
        hashMap = nextHashMap;

        if (gameState.appliedUnitList != null) {
            appliedUnitList = gameState.appliedUnitList;
        } else {
            appliedUnitList = new HashSet<>();
        }
    }

    // getter & setter
    public int getxExtent() {
        return xExtent;
    }

    public void setxExtent(int xExtent) {
        this.xExtent = xExtent;
    }

    public int getyExtent() {
        return yExtent;
    }

    public void setyExtent(int yExtent) {
        this.yExtent = yExtent;
    }

    public List<Integer> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Integer> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public Set<MapLocation> getResourceLocations() {
        return resourceLocations;
    }

    public void setResourceLocations(Set<MapLocation> resourceLocations) {
        this.resourceLocations = resourceLocations;
    }

    public List<Integer> getPlayerUnitIDs() {
        return playerUnitIDs;
    }

    public void setPlayerUnitIDs(List<Integer> playerUnitIDs) {
        this.playerUnitIDs = playerUnitIDs;
    }

    public List<Integer> getEnemyUnitIDs() {
        return enemyUnitIDs;
    }

    public void setEnemyUnitIDs(List<Integer> enemyUnitIDs) {
        this.enemyUnitIDs = enemyUnitIDs;
    }

    public Set<Unit.UnitView> getPlayerUnitView() {
        return playerUnitView;
    }

    public void setPlayerUnitView(Set<Unit.UnitView> playerUnitView) {
        this.playerUnitView = playerUnitView;
    }

    public Set<Unit.UnitView> getEnemyUnitView() {
        return enemyUnitView;
    }

    public void setEnemyUnitView(Set<Unit.UnitView> enemyUnitView) {
        this.enemyUnitView = enemyUnitView;
    }

    public double getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(double heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * You will implement this function.
     * <p>
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     * <p>
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     * <p>
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
        double utilityValue = 0.0;
        // If the expected moves stored in AppliedUnitList is not null, utility will be calculated based of these units
        if (!appliedUnitList.isEmpty()) {
            // Traverse each possible moves
            double bestDistance = 1.0;
            for (AppliedUnit unit : appliedUnitList) {
                for (Unit.UnitView enemyUnit : enemyUnitView) {
                    if (unit.oldUnit == enemyUnit) {
                        // weight more on attacking and deal damage on the enemy
                        utilityValue += 3.0 * (enemyUnit.getHP() - unit.getHP());
                    } else {
                        double checkDistance = Math.abs(enemyUnit.getXPosition() - unit.getXPosition()) + Math.abs(enemyUnit.getYPosition() - unit.getYPosition());
                        if (bestDistance < checkDistance) {
                            bestDistance = checkDistance;
                        }
                    }
                }
                utilityValue -= 0.3 * bestDistance;
            }
        } else {
            for (Unit.UnitView unit : playerUnitView) {
                double bestDistance = 0;
                for (Unit.UnitView enemyUnit : enemyUnitView) {
                    double checkDistance = Math.abs(enemyUnit.getXPosition() - unit.getXPosition()) + Math.abs(enemyUnit.getYPosition() - unit.getYPosition());
                    if (bestDistance < checkDistance) {
                        bestDistance = checkDistance;
                    }
                }
                utilityValue -= 0.3 * bestDistance;
            }
        }
        return utilityValue;
    }

    /**
     * You will implement this function.
     * <p>
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     * <p>
     * It may be useful to be able to create a SEPIA Action. In this assignment you will
     * deal with movement and attacking actions. There are static methods inside the Action
     * class that allow you to create basic actions:
     * Action.createPrimitiveAttack(int attackerID, int targetID): returns an Action where
     * the attacker unit attacks the target unit.
     * Action.createPrimitiveMove(int unitID, Direction dir): returns an Action where the unit
     * moves one space in the specified direction.
     * <p>
     * You may find it useful to iterate over all the different directions in SEPIA. This can
     * be done with the following loop:
     * for(Direction direction : Directions.values())
     * <p>
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     * <p>
     * If you wish to explicitly use a Direction you can use the Direction enum, for example
     * Direction.NORTH or Direction.NORTHEAST.
     * <p>
     * You can check many of the properties of an Action directly:
     * action.getType(): returns the ActionType of the action
     * action.getUnitID(): returns the ID of the unit performing the Action
     * <p>
     * ActionType is an enum containing different types of actions. The methods given above
     * create actions of type ActionType.PRIMITIVEATTACK and ActionType.PRIMITIVEMOVE.
     * <p>
     * For attack actions, you can check the unit that is being attacked. To do this, you
     * must cast the Action as a TargetedAction:
     * ((TargetedAction)action).getTargetID(): returns the ID of the unit being attacked
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
        List<GameStateChild> finalList = new LinkedList<>();
        Map<Integer, Action> hashmap = new HashMap<Integer, Action>();
        List<Unit.UnitView> unitList;
        if (atMaxNode) {
            unitList = new ArrayList<>(playerUnitView);
        } else {
            unitList = new ArrayList<>(enemyUnitView);
        }
        genChild(unitList, 0, hashmap, finalList);

        return finalList;
    }

    // switches value when player and enemy take turns
    public List<GameStateChild> getChildren(boolean b) {
        atMaxNode = b;
        return getChildren();
    }

    private void genChild(List<Unit.UnitView> unitList, int i, Map<Integer, Action> actionMap, List<GameStateChild> finalList) {
        Unit.UnitView unit = unitList.get(i);

        // traverse every moves
        for (Direction direction : Direction.values()) {
            // adds play's best favorables
            if (atMaxNode) {
                if (enemyUnitView != null) {
                    for (Unit.UnitView enemyUnit : enemyUnitView) {
                        if (isWithinAttackRange(unit, enemyUnit)) {
                            actionMap.put(unit.getID(), Action.createPrimitiveAttack(unit.getID(), enemyUnit.getID()));
                        } else if (validMove(unit, direction)) {
                            actionMap.put(unit.getID(), Action.createPrimitiveMove(unit.getID(), direction));
                        }
                    }
                }
            } else { // enemy's nodes
                if (playerUnitView != null) {
                    for (Unit.UnitView playerUnit : playerUnitView) {
                        if (isWithinAttackRange(unit, playerUnit)) {
                            actionMap.put(unit.getID(), Action.createPrimitiveAttack(unit.getID(), playerUnit.getID()));
                        } else if (validMove(unit, direction)) {
                            actionMap.put(unit.getID(), Action.createPrimitiveMove(unit.getID(), direction));
                        }
                    }
                }
            }

            if (i + 1 < unitList.size()) {
                genChild(unitList, i + 1, actionMap, finalList);
            } else {
                HashMap<Integer, Action> nextHashMap = new HashMap<Integer, Action>(actionMap);
                GameState nextState = new GameState(this, nextHashMap);
                finalList.add(new GameStateChild(nextHashMap, nextState));
            }
        }
    }

    // Tells if the next move is valid
    private boolean validMove(Unit.UnitView unitView, Direction direction) {

        MapLocation newLocation = new MapLocation(unitView.getXPosition() + direction.xComponent(), unitView.getYPosition() + direction.yComponent());

        // return false if out of range or rams into an obstacle or there is another agent
        if (newLocation.x < 0 || newLocation.x >= xExtent || newLocation.y < 0 || newLocation.y >= yExtent) {
            return false;
        }
        for (MapLocation resourceLocation : resourceLocations) {
            if (newLocation.equals(resourceLocation)) {
                return false;
            }
        }
        if (playerUnitView != null) {
            for (Unit.UnitView unit : playerUnitView) {
                if ((unit.getXPosition() == newLocation.x) && (unit.getYPosition() == newLocation.y)) {
                    return false;
                }
            }
        }
        for (Unit.UnitView unit : enemyUnitView) {
            if ((unit.getXPosition() == newLocation.x) && (unit.getYPosition() == newLocation.y)) {
                return false;
            }
        }
        return true;

    }

    // Tells if enemy is within attack range
    private boolean isWithinAttackRange(Unit.UnitView footman, Unit.UnitView archer) {
        int dx = Math.abs(footman.getXPosition() - archer.getXPosition());
        int dy = Math.abs(footman.getYPosition() - archer.getYPosition());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
}
