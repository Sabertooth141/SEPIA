package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;

import java.util.*;

public class Move_k implements StripsAction {

    int k;
    List<Peasant> peasants = null;
    GameState parent;

	List<Action> sepiaAction = new ArrayList<Action>();


    Position starPosition;
    Position destPosition;
    List<Position> availablePositions = new ArrayList<Position>();



	public Move_k(List<Peasant> peasants, Position destPosition, GameState parent) {
		this.peasants = peasants;
		this.destPosition = destPosition;
		this.parent = parent;

		this.starPosition = peasants.get(0).neighbor;
	}


    private boolean legal_pos(Position pos) {
        if (pos.x < 0 || pos.x >= parent.getxExtent() || pos.y < 0 || pos.y >= parent.getyExtent()) {
            return false;
        }
        else if (parent.getMap()[pos.x][pos.y]) { //resource
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public GameState getParent() {
        return this.parent;
    }



    @Override
	public boolean preconditionsMet(GameState state) {

        int x = destPosition.x;
		int y = destPosition.y;

        int gold_amount = parent.getGoldMap()[x][y];
        int wood_amount = parent.getWoodMap()[x][y];
		if(gold_amount != 0 || wood_amount != 0) {
            if (gold_amount != 0) {
                if((int) ((gold_amount - 1) / 100) + 1 < peasants.size()) {
    				return false;
    			}
    			if((int) (((parent.getRequiredGold() - parent.getCurrentGold()) - 1) / 100) + 1 < peasants.size()) {
    				return false;
    			}
            }
            if (wood_amount != 0) {
                if((int) ((wood_amount - 1) / 100) + 1 < peasants.size()) {
    				return false;
    			}
    			if((int) (((parent.getRequiredWood() - parent.getCurrentWood()) - 1) / 100) + 1 < peasants.size()) {
    				return false;
    			}
            }

		}


        List<Position> candidate_poses = new ArrayList<>();
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				candidate_poses.add(new Position(i, j));
			}
		}

		for (Position pos : candidate_poses) {
			if (legal_pos(pos)) {
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
			updated_p.neighbor = destPosition;
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
}