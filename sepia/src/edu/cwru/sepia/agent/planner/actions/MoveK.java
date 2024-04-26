package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.util.*;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.*;

public class MoveK implements StripsAction {

    int k;
    List<Peasant> peasants = null;
    GameState parent;

	List<Action> sepiaAction = new ArrayList<Action>();


    Position starPosition;
    Position destPosition;
    List<Position> availablePositions = new ArrayList<Position>();



	public MoveK(List<Peasant> peasants, Position destPosition, GameState parent) {
		this.peasants = peasants;
		this.destPosition = destPosition;
		this.parent = parent;

		this.starPosition = peasants.get(0).adjacent_pos;
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


    // private Direction getDirection(int originalX, int originalY, int currentX, int currentY) {
    //     for (Direction d : Direction.values()) {
    //         if((currentX - originalX) == d.xComponent() && (currentY - originalY) == d.yComponent()) {
    //             return d;
    //         }
    //     }
    //     return null;
    // }


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
					if(p.x_pos == pos.x && p.y_pos == pos.y) {
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
		int x = destPosition.x;
		int y = destPosition.y;

		int min_Chebyshev = 0;
        // Integer best_Chebyshev = null;

		for (Peasant p : peasants) {
            Position p_pos = new Position(p.x_pos, p.y_pos);
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
            // UnitView originalUnit = result.findUnit(unit.getID(), result.getPlayerUnits());

            updated_p.x_pos = optimal_pos.x;
            updated_p.y_pos = optimal_pos.y;
			updated_p.adjacent_pos = destPosition;
			availablePositions.remove(optimal_pos);

			sepiaAction.add(Action.createCompoundMove(p.id, optimal_pos.x, optimal_pos.y));
		}


		result.addCost(min_Chebyshev);
        result.heuristic();
        result.addPlan(this);

		// sepiaAction = Action.createCompoundMove(originalUnit.getID(), x, y);

		return result;
	}



	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
	}
}