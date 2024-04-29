package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoveK implements StripsAction {
    int k;
    List<Peasant> peasants = null;
    GameState parent;

    List<Action> sepiaAction  = new ArrayList<>();

    Position start;
    Position end;
    List<Position> availablePositions = new ArrayList<>();

    public MoveK(List<Peasant> peasants, Position end, GameState parent) {
        this.end = end;
        this.peasants = peasants;
        this.parent = parent;

        this.start = peasants.get(0).neighbor;
    }

    private boolean legalPosition(Position pos) {
        if (pos.x <= 0 || pos.x >= parent.getXExtent() || pos.y <= 0 || pos.y >= parent.getYExtent()) {
            return false;
        } else {
            return !parent.getMap()[pos.x][pos.y];
        }
    }

    @Override
    public GameState getParent() {
        return parent;
    }

    @Override
    public boolean preconditionsMet(GameState state) {
        int x = end.x;
        int y = end.y;

        int gold = parent.getGoldMap()[x][y];
        int wood = parent.getWoodMap()[x][y];
        if (gold != 0 || wood != 0) {
            if (gold != 0) {
                if (((gold - 1) / 100) + 1 < peasants.size()) {
                    return false;
                }
                if (((parent.getRequiredGold() - parent.getGold() - 1) / 100) + 1 < peasants.size()) {
                    return false;
                }
            }

            if (wood != 0) {
                if (((wood - 1) / 100) + 1 < peasants.size()) {
                    return false;
                }
                if (((parent.getRequiredWood() - parent.getWood() - 1) / 100) + 1 < peasants.size()) {
                    return false;
                }
            }
        }

        List<Position> potentialPos = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                potentialPos.add(new Position(i, j));
            }
        }

        for (Position pos : potentialPos) {
            if (legalPosition(pos)){
                boolean isCollide = false;
                for (Peasant p : state.getPeasantUnits()) {
                    if (p.x == pos.x && p.y == pos.y) {
                        isCollide = true;
                    }
                }

                if (!isCollide) {
                    availablePositions.add(pos);
                }

            }
        }

        return availablePositions.size() > peasants.size();

    }

    @Override
    public GameState apply(GameState state) {
        GameState result = new GameState(state);

        int x = end.x;
        int y = end.y;

        int minChebyshev = 0;

        for (Peasant p : peasants) {
            Position peasantPos = new Position(p.x, p.y);
            Position opimalPos = null;

            for (Position potentialPos : availablePositions) {
                if (opimalPos != null) {
                    int potentialCheby = peasantPos.chebyshevDistance(potentialPos);
                    if (potentialCheby < minChebyshev) {
                        minChebyshev = potentialCheby;
                        opimalPos = potentialPos;
                    }
                } else {
                    opimalPos = potentialPos;
                    minChebyshev = peasantPos.chebyshevDistance(potentialPos);
                }
            }

            int bestCheby = peasantPos.chebyshevDistance(opimalPos);
            if (bestCheby > minChebyshev) {
                minChebyshev = bestCheby;
            }

            Peasant updatePeasant = result.findPeasant(p.id, result.getPeasantUnits());
            updatePeasant.x = opimalPos.x;
            updatePeasant.y = opimalPos.y;
            updatePeasant.neighbor = end;
            availablePositions.remove(opimalPos);

            sepiaAction.add(Action.createCompoundMove(p.id, opimalPos.x, opimalPos.y));
        }
        result.addCost(minChebyshev);
        result.heuristic();
        result.addPlan(this);

        return result;
    }

    @Override
    public List<Action> createSEPIAAction() {
        return Collections.emptyList();
    }

}
