package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;

import java.util.*;

public class Move_k implements StripsAction {

    int k;
    List<Peasant> peasants = null;
    GameState parent;

	List<Action> sepiaAction = new ArrayList<Action>();

    Position startPos;
    Position endPos;
    List<Position> availablePositions = new ArrayList<Position>();

	/**
	 * constructor for move
	 * @param peasants list of available peasants
	 * @param endPos the destination of movement
	 * @param parent the parrent state
	 */
	public Move_k(List<Peasant> peasants, Position endPos, GameState parent) {
		this.peasants = peasants;
		this.endPos = endPos;
		this.parent = parent;
		this.startPos = peasants.get(0).neighbor;
	}

	/**
	 * Find if movement statisfies conditions
	 * @param state the current state
	 * @return if the movement can be constructed
	 */
    @Override
	public boolean preconditionsMet(GameState state) {

        int x = endPos.x;
		int y = endPos.y;

        int goldAmount = parent.getGoldMap()[x][y];
        int woodAmount = parent.getWoodMap()[x][y];

		if(goldAmount != 0 || woodAmount != 0) {
            if (goldAmount != 0 && notEnoughPeasant(goldAmount, parent.getRequiredGold() - parent.getCurrentGold(), peasants.size())) return false;
            if (woodAmount != 0 && notEnoughPeasant(woodAmount, parent.getRequiredWood() - parent.getCurrentWood(), peasants.size())) return false;
		}

        List<Position> candidate_poses = new ArrayList<>();
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				candidate_poses.add(new Position(i, j));
			}
		}

		for (Position pos : candidate_poses) {
			if (legalPosition(pos)) {
				boolean collision_flag = false;
				for(Peasant p : state.getPeasantUnits()) {
					if(p.x == pos.x && p.y == pos.y) {
						collision_flag = true;
					}
				}
				if (!collision_flag) {
					availablePositions.add(pos);
				}
    		}
        }

		return availablePositions.size() > peasants.size();
	}

	private boolean notEnoughPeasant(int resourceAmount, int requiredResource, int peasantCount) {
		int batches = resourceAmount / 100;
		int requiredBatches = requiredResource / 100 ;
		return batches < peasantCount || requiredBatches < peasantCount;
	}

	private boolean legalPosition(Position pos) {
		if (pos.x < 0 || pos.x >= parent.getxExtent() || pos.y < 0 || pos.y >= parent.getyExtent()) return false;
		else if (parent.getMap()[pos.x][pos.y]) return false;
		return true;
	}

    @Override
	public GameState apply(GameState state) {
        GameState result = new GameState(state);

		int min_Chebyshev = 0;

		for (Peasant p : peasants) {
            Position p_pos = new Position(p.x, p.y);
			Position optimal_pos = null;


            for (Position candidate_pos : availablePositions) {
                if (optimal_pos != null) {
                    int candidate_Chebyshev = p_pos.chebyshevDistance(candidate_pos);
    				if (candidate_Chebyshev < min_Chebyshev) {
    					min_Chebyshev = candidate_Chebyshev;
    					optimal_pos = candidate_pos;
    				}
                }
                else {
                    optimal_pos = candidate_pos;
    				min_Chebyshev = p_pos.chebyshevDistance(candidate_pos);
                }
            }

			int best_Chebyshev = p_pos.chebyshevDistance(optimal_pos);
			if (best_Chebyshev > min_Chebyshev) {
				min_Chebyshev = best_Chebyshev;
			}

			Peasant updated_p = result.findPeasant(p.id, result.getPeasantUnits());

            updated_p.x = optimal_pos.x;
            updated_p.y = optimal_pos.y;
			updated_p.neighbor = endPos;
			availablePositions.remove(optimal_pos);

			sepiaAction.add(Action.createCompoundMove(p.id, optimal_pos.x, optimal_pos.y));
		}


		result.addCost(min_Chebyshev);
        result.heuristic();
        result.addPlan(this);

		return result;
	}



	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
	}
	
    @Override
    public GameState getParent() {
        return this.parent;
    }


}