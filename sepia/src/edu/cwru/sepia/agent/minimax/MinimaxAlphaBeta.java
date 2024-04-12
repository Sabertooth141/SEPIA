package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;

    private boolean maximizingPlayer = true;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
}

@Override
public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
        //base situation
        if(depth <= 0) {
            node.state.setHeuristic(node.state.getUtility());
            return node;
        }
        //other situations
        List<GameStateChild> children = orderChildrenWithHeuristics(node.state.getChildren());

        double bestVal;
        GameStateChild bestChild = null;
        if (maximizingPlayer) {
            bestVal = Double.NEGATIVE_INFINITY;
            maximizingPlayer = false;
            for(GameStateChild child : children){
                GameStateChild desc = alphaBetaSearch(child, depth - 1, alpha, beta);
                double childValue = desc.state.getHeuristic();
                if (childValue > bestVal) {
                    bestVal = childValue;
                    bestChild = child;
                }
                alpha = Math.max(alpha, bestVal);
                if (alpha >= beta) {
                    break; //cut off
                }
            }
        } else {
            bestVal = Double.POSITIVE_INFINITY;
            maximizingPlayer = true;
            for(GameStateChild child : children){
                GameStateChild desc = alphaBetaSearch(child, depth - 1, alpha, beta);
                double childValue = desc.state.getHeuristic();
                if (childValue < bestVal) {
                    bestVal = childValue;
                    bestChild = child;
                }
                beta = Math.min(beta, bestVal);
                if (bestVal <= alpha) {
                    break; //cut off
                }
            }
        }
        if (bestChild != null) {
            bestChild.state.setHeuristic(bestVal);
        }
        return bestChild;
    }

/**
 * You will implement this.
 *
 * Given a list of children you will order them according to heuristics you make up.
 * See the assignment description for suggestions on heuristics to use when sorting.
 *
 * Use this function inside of your alphaBetaSearch method.
 *
 * Include a good comment about what your heuristics are and why you chose them.
 *
 * @param children
 * @return The list of children sorted by your heuristic.
 */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
        //the heuristic is keep attacking whenever possible
        children.sort((child1, child2) -> {
            Integer a = 0;
            if (ActionType.PRIMITIVEATTACK.equals(child1.action.get(a).getType()) && ActionType.PRIMITIVEMOVE.equals(child2.action.get(a).getType())) {
                return -1;
            } else if (ActionType.PRIMITIVEMOVE.equals(child1.action.get(a).getType()) && ActionType.PRIMITIVEATTACK.equals(child2.action.get(a).getType())) {
                return 1;
            }
            else {
                return 0;
            }
        });
        return children;
    }
}
