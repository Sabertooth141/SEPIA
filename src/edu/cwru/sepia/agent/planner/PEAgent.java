package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;
    private int numActors = 0;
    private StripsAction currentAction = null;
    private Set<Integer> queuedActors = new HashSet<Integer>();

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private State.StateView stateView;
    private int townhallId;
    private int peasantTemplateId;

    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;

    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        // gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            if(unitType.equals("townhall")) {
                townhallId = unitId;
            } else if(unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            if(templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
	 * To check a unit's progress on the action they were executing last turn, you can use the following:
     * historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1).get(unitID).getFeedback()
     * This returns an enum ActionFeedback. When the action is done, it will return ActionFeedback.COMPLETED
     *
     * Alternatively, you can see the feedback for each action being executed during the last turn. Here is a short example.
     * if (stateView.getTurnNumber() != 0) {
     *   Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
     *   for (ActionResult result : actionResults.values()) {
     *     <stuff>
     *   }
     * }
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        // TODO: Implement me!
        this.stateView = stateView;
        Map<Integer,Action> actions = new HashMap<Integer,Action>();
        Map<Integer,ActionResult> actionFeedback = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
        
        // adds new peasant ID to peasantIdMap
        int count = 0;
        for (Unit.UnitView unit : stateView.getAllUnits()) {
        	// only adds id when peasant id is newly created (new peasant built on map)
    		peasantIdMap.putIfAbsent(count++, unit.getID());
        }
        
        // get next action in plan if previous action is queued
        while (numActors == 0) {
            currentAction = plan.pop();
            queuedActors.clear();
            for (Integer unitID : stateView.getUnitIds(playernum)) {
                if (currentAction.getActionType() == StripsAction.StripsActionType.BUILD && stateView.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("townhall")) {
                    numActors += 1;
                }
                else if (currentAction.getActionType() != StripsAction.StripsActionType.BUILD && stateView.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("peasant")) {
                    numActors = currentAction.getActorID();
                    break;
                }
            }
            System.out.println(currentAction.getActionType() + " " + numActors);
        }

        // queue up action for all needed idle peasants
        for (Integer unitID : stateView.getUnitIds(playernum)) {
            // skip incompatible unit types
            if (currentAction.getActionType() == StripsAction.StripsActionType.BUILD && !stateView.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("townhall")) {
                continue;
            }
            else if (currentAction.getActionType() != StripsAction.StripsActionType.BUILD && !stateView.getUnit(unitID).getTemplateView().getName().toLowerCase().equals("peasant")) {
                continue;
            }

            // skip already used actors
            if (queuedActors.contains(unitID)) {
                continue;
            }

            // check if actor is idle and queue up action if true
            if ((!actionFeedback.containsKey(unitID) || actionFeedback.get(unitID).getFeedback() == ActionFeedback.COMPLETED) && !actions.containsKey(unitID)) {
                currentAction.setActorID(unitID);
                actions.put(unitID, createSepiaAction(currentAction));
                numActors -= 1;
                queuedActors.add(unitID);
            }

            // end if we filled the quota
            if (numActors == 0) {
                break;
            }
        }
        
    	return actions;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     * 
     * Hint:
     * peasantId could be found in peasantIdMap
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(StripsAction action) {
        Action sepiaAction = null;
        Unit.UnitView unit = stateView.getUnit(action.getActorID());

        switch (action.getActionType()) {
            case MOVE:
                Position targetPos = action.getTargetPos();
                sepiaAction = Action.createCompoundMove(action.getActorID(), targetPos.x, targetPos.y);
                break;
            case HARVEST:
            	sepiaAction = Action.createPrimitiveGather(action.getActorID(), (new Position(unit.getXPosition(), unit.getYPosition())).getDirection(action.getTargetPos()));
                break;
            case BUILD:
            	sepiaAction = Action.createPrimitiveProduction(action.getActorID(), action.getTargetID());
                break;
            case DEPOSIT:
            	sepiaAction = Action.createPrimitiveDeposit(action.getActorID(), (new Position(unit.getXPosition(), unit.getYPosition())).getDirection(action.getTargetPos()));
                break;
            default:
                System.err.println("Unrecognized Strips Action " + action.getActionType());
        }

        return sepiaAction;
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {

    }

    @Override
    public void savePlayerData(OutputStream outputStream) {

    }

    @Override
    public void loadPlayerData(InputStream inputStream) {

    }
}