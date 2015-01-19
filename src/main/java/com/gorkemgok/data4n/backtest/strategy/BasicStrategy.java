package com.gorkemgok.data4n.backtest.strategy;

import com.gorkemgok.data4n.backtest.Positions;
import com.gorkemgok.data4n.backtest.action.IAction;
import com.gorkemgok.data4n.backtest.action.result.ActionResult;
import com.gorkemgok.data4n.core.set.TickDataSet;

import java.util.ArrayList;

/**
 * Created by gorkemgok on 19/01/15.
 */
public class BasicStrategy implements IStrategy{
    private ArrayList<BasicStrategy> strategies = new ArrayList<BasicStrategy>();
    private ArrayList<IAction> actions = new ArrayList<IAction>();

    protected BasicStrategy(ArrayList<BasicStrategy> strategies, ArrayList<IAction> actions) {
        this.strategies = strategies;
        this.actions = actions;
    }

    public void apply(TickDataSet set,Positions positions) {
        for (IAction action : actions){
            ActionResult actionResult = action.run(set);
            if (actionResult.hasAction()) positions.addPosition(actionResult.getPosition());
        }
        for (IStrategy strategy : strategies){
            strategy.apply(set,positions);
        }
    }

}
