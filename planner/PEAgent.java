package edu.cwru.sepia.agent.planner;

import java.io.*;
import java.util.*;

import edu.cwru.sepia.action.*;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;


/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
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
	 * This is where you will read the provided plan and execute it. If your plan is
	 * correct then when the plan is empty the scenario should end with a victory.
	 * If the scenario keeps running after you run out of actions to execute then
	 * either your plan is incorrect or your execution of the plan has a bug.
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
	 * these actions are stored in a mapping between the peasant unit ID executing
	 * the action and the action you created.
	 *
	 * For the compound actions you will need to check their progress and wait until
	 * they are complete before issuing another action for that unit. If you issue
	 * an action before the compound action is complete then the peasant will stop
	 * what it was doing and begin executing the new action.
	 *
	 * To check an action's progress you can use the historyview object. Here is a
	 * short example. if (stateView.getTurnNumber() != 0) { Map<Integer,
	 * ActionResult> actionResults = historyView.getCommandFeedback(playernum,
	 * stateView.getTurnNumber() - 1); for (ActionResult result :
	 * actionResults.values()) { <stuff> } } Also remember to check your plan's
	 * preconditions before executing!
	 */
	@Override
	public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {

		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		boolean actionComplete = false;


        if (stateView.getTurnNumber() != 0) {
			Map<Integer, ActionResult> results = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
            for (ActionResult result : results.values()) {
                if (result.getFeedback().toString().equals("INCOMPLETE")) {
                    actionComplete = true;
                    return actions;
                } else if (result.getFeedback().toString().equals("FAILED")){
                    System.err.println("ACTION FAILED");
                    actions.put(result.getAction().getUnitId(), result.getAction());
                    // return actions;
                }
            }
		}


        if (actions.isEmpty()) {
			StripsAction stripsAction = plan.pop();

			if (stripsAction.preconditionsMet(stripsAction.getParent())) {
				Map<Integer, Integer> peasants_to_act = new HashMap<Integer, Integer>();
				for (Integer peasantId : peasants_to_act.keySet()) {
					peasants_to_act.put(peasantId, peasantId);
				}

				List<Action> sepiaActions = createSepiaAction(stripsAction);
				for (Action a : sepiaActions) {
					actions.put(a.getUnitId(), a);
					// peasants_to_act.add(a.getUnitId());
					peasants_to_act.remove(a.getUnitId());
				}
			}

		}

    	// if (actionComplete == false){
    	// 	List<Action> next = new ArrayList<Action>();
    	// 	Action action = (Action) plan.pop().createSEPIAaction();
    	// 	next.add(action);
        //
    	// 	for (int i = 0; i < next.size(); i++) {
    	// 		System.out.println(next.get(i).toString());
    	// 		actions.put(next.get(i).getType() == ActionType.PRIMITIVEPRODUCE ? i : peasantIdMap.get(i+1) , next.get(i));
    	// 	}
    	// }


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
     private List<Action> createSepiaAction(StripsAction action) {
 		return action.createSEPIAaction();
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

    public Stack<StripsAction> getPlan() {
		return this.plan;
	}

}