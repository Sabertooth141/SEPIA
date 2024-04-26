package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.*;
import edu.cwru.sepia.environment.model.state.*;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.*;

import java.util.*;

public class HarvestK implements StripsAction {

    int k;
    Position resourcePosition;

    List<Peasant> peasants = null;
    List<Position> newPosition = null;

    List<Action> sepiaAction = new ArrayList<Action>();
    GameState parent;




    public HarvestK (List<Peasant> peasants, Position resourcePosition, GameState parent) {
        if (peasants != null) {
            this.k = peasants.size();
        }
        this.peasants = peasants;
        this.resourcePosition = resourcePosition;
        this.parent = parent;

        this.newPosition = new ArrayList<Position>();
        for (Peasant p : peasants) {
            newPosition.add(new Position(p.x_pos, p.y_pos));
        }
    }

    private boolean legal_pos(Position pos) {
        if (pos.x < 0 || pos.x >= parent.getxExtent() || pos.y < 0 || pos.y >= parent.getyExtent()) {
            return false;
        }
        else if (parent.getMap()[pos.x][pos.y]) { //resource
            return false;
        }
        else if (Math.abs(pos.x - resourcePosition.x) > 1 || Math.abs(pos.y - resourcePosition.y) > 1) {
            return false;
        }
        else {
            return true;
        }
    }


    private Direction getDirection(int originalX, int originalY, int currentX, int currentY) {
        for (Direction d : Direction.values()) {
            if((currentX - originalX) == d.xComponent() && (currentY - originalY) == d.yComponent()) {
                return d;
            }
        }
        return null;
    }


    @Override
    public GameState getParent() {
        return this.parent;
    }


    @Override
	public boolean preconditionsMet(GameState state) {
        boolean flag = true;
        for (int i = 0; i < k; i++) {
            try {
                if (peasants.get(i).cargo_amount != 0) {
                    flag = false;
                }
                else if (!legal_pos(newPosition.get(i))) {
                    flag = false;
                }
            } catch (Exception e) { // possible null pointer
                flag = false;
                return flag;
            }
    	}
        return flag;
	}



	// @Override
	// public GameState apply(GameState state) {
    //     GameState result = new GameState(state);
    //
    //
	// 	for (int i = 0; i < k; i++) {
    //         Peasant p = result.findPeasant(peasants.get(i).id, result.getPeasantUnits());
    //
    //         // int x = peasants.get(i).x_pos;
    //         // int y = peasants.get(i).y_pos;
    //
    //         int x = resourcePosition.x;
	// 		int y = resourcePosition.y;
    //
    //         int amount = 0;
    //         int gold_amount = result.getGoldMap()[x][y];
    //         int wood_amount = result.getWoodMap()[x][y];
    //         boolean is_gold = false;
    //
    //         ResourceView originalResource = state.findResource(x, y, state.getResourceNodes());
    // 		ResourceNode currentResource = null;
    //
    //         if (gold_amount >= wood_amount) {
    //             amount = gold_amount;
    //             is_gold = true;
    //         }
    //         else if (wood_amount > gold_amount) {
    //             amount = wood_amount;
    //             is_gold = false;
    //         }
    //         else {
    //             continue;
    //         }
    //
    //         if (is_gold == true) {
    //     		if (amount < 100) {
    //     			result.getGoldMap()[x][y] = 0;
    //                 p.have_gold = true;
    //                 p.have_wood = false;
    //     			p.cargo_amount = amount;
    //     			amount = 0;
    //     		}
    //             else {
    //     			result.getGoldMap()[x][y] -= 100;
    //                 p.have_gold = true;
    //                 p.have_wood = false;
    //     			p.cargo_amount = amount;
    //     			amount -= 100;
    //     		}
    //             currentResource = new ResourceNode(ResourceNode.Type.GOLD_MINE, x, y, amount, originalResource.getID());
    //         }
    //
    //         if (is_gold == false) {
    //             if (amount < 100) {
    //     			result.getWoodMap()[x][y] = 0;
    //                 p.have_wood = true;
    //                 p.have_gold = false;
    //     			p.cargo_amount = amount;
    //                 amount = 0;
    //     		}
    //     		else {
    //     			result.getWoodMap()[x][y] -= 100;
    //                 p.have_wood = true;
    //                 p.have_gold = false;
    //     			p.cargo_amount = 100;
    //     			amount -= 100;
    //     		}
    //             currentResource = new ResourceNode(ResourceNode.Type.TREE, x, y, amount, originalResource.getID());
    //         }
    //
    //         result.getResourceNodes().remove(originalResource);
    //         result.getResourceNodes().add(new ResourceView(currentResource));
    //
    //         sepiaAction.add(Action.createPrimitiveGather(p.id,
	// 				getDirection(p.x_pos, p.y_pos, x, y)));
    //
    // 	}
    //
    //     result.addCost(1);
    //     result.heuristic();
    //     result.addPlan(this);
    //     return result;
    // }



    @Override
	public GameState apply(GameState state) {

        GameState result = new GameState(state);


		for (int i = 0; i < k; i++) {
			Peasant p = result.findPeasant(peasants.get(i).id, result.getPeasantUnits());


            int x = resourcePosition.x;
            int y = resourcePosition.y;

			int resource_amount = Math.max(result.getGoldMap()[x][y], result.getWoodMap()[x][y]);

			ResourceView originalResource = state.findResource(x, y, state.getResourceNodes());
			ResourceNode currentResource = null;



				if (originalResource.getType() == ResourceNode.Type.GOLD_MINE) {
                    if (resource_amount <= 100) {
    					result.getMap()[x][y] = false;
    					result.getGoldMap()[x][y] = -1;

    					p.cargo_amount = resource_amount;
    					p.have_gold = true;
    					p.have_wood = false;

    					resource_amount = 0;
                    }
                    else {
                        result.getGoldMap()[x][y] -= 100;
                        resource_amount -= 100;

                        p.cargo_amount = 100;
                        p.have_gold = true;
                        p.have_wood = false;
                    }
                    currentResource = new ResourceNode(ResourceNode.Type.GOLD_MINE, x, y, resource_amount, originalResource.getID());
				}
                else if (originalResource.getType() == ResourceNode.Type.TREE) {
                    if (resource_amount <= 100) {
                        result.getMap()[x][y] = false;
    					result.getWoodMap()[x][y] = -1;

    					p.cargo_amount = resource_amount;
    					p.have_gold = false;
                        p.have_wood = true;

    					resource_amount = 0;
                    }
                    else {
    					result.getWoodMap()[x][y] -= 100;
                        resource_amount -= 100;

                        p.cargo_amount = 100;
                        p.have_gold = false;
                        p.have_wood = true;
                    }
                    currentResource = new ResourceNode(ResourceNode.Type.TREE, x, y, resource_amount, originalResource.getID());
				}


			result.getResourceNodes().remove(originalResource);
			result.getResourceNodes().add(new ResourceView(currentResource));

			sepiaAction.add(Action.createPrimitiveGather(p.id, getDirection(p.x_pos, p.y_pos, x, y)));
		}

		result.addCost(1);
		result.heuristic();
		result.addPlan(this);

		return result;
	}



	@Override
	public List<Action> createSEPIAaction() {
		return sepiaAction;
    }

}
