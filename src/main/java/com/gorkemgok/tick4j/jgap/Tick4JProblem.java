package com.gorkemgok.tick4j.jgap;

/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
import java.util.*;

import com.gorkemgok.tick4j.backtest.action.SellExpAction;
import org.jgap.*;
import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.gp.terminal.Variable;

import com.gorkemgok.tick4j.backtest.PositionCalculator;
import com.gorkemgok.tick4j.backtest.Positions;
import com.gorkemgok.tick4j.backtest.action.BuyExpAction;
import com.gorkemgok.tick4j.backtest.action.ClosePositionExpAction;
import com.gorkemgok.tick4j.backtest.strategy.BasicStrategy;
import com.gorkemgok.tick4j.backtest.strategy.BasicStrategyBuilder;
import com.gorkemgok.tick4j.core.set.TickDataSet;
import com.gorkemgok.tick4j.jgap.dummy.IntegerIntervalRSIType;
import com.gorkemgok.tick4j.jgap.dummy.IntegerIntervalZeroBased;
import com.gorkemgok.tick4j.jgap.dummy.PricePos;
import com.gorkemgok.tick4j.jgap.dummy.TimePeriod;
import com.gorkemgok.tick4j.jgap.functions.CustomFunction;
import com.gorkemgok.tick4j.jgap.functions.Terminal;
import com.gorkemgok.tick4j.listener.CSVTickListener;
import com.gorkemgok.tick4j.util.TALibExpressionBuilder;
import com.gorkemgok.tick4j.util.csv.CSVLoader;

/**
 * Example demonstrating Genetic Programming (GP) capabilities of JGAP. Also
 * demonstrates usage of ADF's.<br>
 * The problem is to find a formula for a given truth table (X/Y-pairs).
 * <p>
 * <ul>
 * <li>The setup of the GP is done in method main and specifically in method
 * create()</li>
 * <li>The problem solving process is started via gp.evolve(800) in the main
 * method, with 800 the maximum number of evolutions to take place.
 * <li>The evaluation of the evolved formula is done in fitness function
 * FormulaFitnessFunction, which is implemented in this class, MathProblem
 * </ul>
 * <br>
 * For details, please see the mentioned methods and the fitness function.
 * <p>
 * 
 * @author Klaus Meffert
 * @since 3.0
 */
public class Tick4JProblem extends GPProblem {
	/** String containing the CVS revision. Read out via reflection! */
	@SuppressWarnings("unused")
	private final static String CVS_REVISION = "$Revision: 1.25 $";

	public static Variable vx;

	protected static Float[] x = new Float[20];

	protected static float[] y = new float[20];

	public Tick4JProblem(GPConfiguration a_conf, TickDataSet set) throws InvalidConfigurationException {
		super(a_conf);
	}

	/**
	 * This method is used for setting up the commands and terminals that can be
	 * used to solve the problem. In this example an ADF (an automatically
	 * defined function) is used for demonstration purpuses. Using an ADF is
	 * optional. If you want to use one, care about the places marked with
	 * "ADF-relevant:" below. If you do not want to use an ADF, please remove
	 * the below places (and reduce the outer size of the arrays "types",
	 * "argTypes" and "nodeSets" to one). Please notice, that the variables
	 * types, argTypes and nodeSets correspond to each other: they have the same
	 * number of elements and the element at the i'th index of each variable
	 * corresponds to the i'th index of the other variables!
	 *
	 * @return GPGenotype
	 * @throws InvalidConfigurationException
	 */
	@SuppressWarnings("rawtypes")
	public GPGenotype create() throws InvalidConfigurationException {
		GPConfiguration conf = getGPConfiguration();
		// At first, we define the return type of the GP program.
		// ------------------------------------------------------
		Class[] types = {
				// Return type of result-producing chromosome
				CommandGene.BooleanClass,
				// ADF-relevant:
				// Return type of ADF 1
				//CommandGene.FloatClass 
				};
		// Then, we define the arguments of the GP parts. Normally, only for
		// ADF's
		// there is a specification here, otherwise it is empty as in first
		// case.
		// -----------------------------------------------------------------------
		Class[][] argTypes = {
				// Arguments of result-producing chromosome: none
				{},
				// ADF-relevant:
				// Arguments of ADF1: all 3 are float
				//{ CommandGene.FloatClass, CommandGene.FloatClass,CommandGene.FloatClass }
				};
		// Next, we define the set of available GP commands and terminals to
		// use.
		// Please see package org.jgap.gp.function and org.jgap.gp.terminal
		// You can easily add commands and terminals of your own.
		// ----------------------------------------------------------------------
		CommandGene[][] nodeSets = {
				{
						// We use a variable that can be set in the fitness
						// function.
						// ----------------------------------------------------------
						Variable.create(conf, "O", CommandGene.FloatClass),
						Variable.create(conf, "H", CommandGene.FloatClass),
						Variable.create(conf, "L", CommandGene.FloatClass),
						Variable.create(conf, "C", CommandGene.FloatClass),
						Variable.create(conf, "o", PricePos.class),
						Variable.create(conf, "h", PricePos.class),
						Variable.create(conf, "l", PricePos.class),
						Variable.create(conf, "c", PricePos.class),
						//new Equals(conf, CommandGene.BooleanClass),
						//new LesserThan(conf, CommandGene.BooleanClass),
						//new And2(conf),
						//new END(conf, CommandGene.FloatClass),
						//new Or(conf),
						new CustomFunction(conf, "<",CommandGene.BooleanClass,CommandGene.FloatClass), 
						new CustomFunction(conf, ">",CommandGene.BooleanClass,CommandGene.FloatClass),
						
						new CustomFunction(conf, "<",CommandGene.BooleanClass,IntegerIntervalRSIType.class), 
						new CustomFunction(conf, ">",CommandGene.BooleanClass,IntegerIntervalRSIType.class),
						
						new CustomFunction(conf, "<",CommandGene.BooleanClass,IntegerIntervalZeroBased.class),
						new CustomFunction(conf, ">",CommandGene.BooleanClass,IntegerIntervalZeroBased.class),
						
						new CustomFunction(conf, "&",CommandGene.BooleanClass,CommandGene.BooleanClass),
						new CustomFunction(conf, "|",CommandGene.BooleanClass,CommandGene.BooleanClass),

						new CustomFunction(conf, "AVGPRICE",CommandGene.FloatClass,0),

						new CustomFunction(conf, "MIDPRICE",CommandGene.FloatClass,1,TimePeriod.class),

						new CustomFunction(conf, "SMA",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "TSF",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "WMA",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "TEMA",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "DEMA",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "MIN",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "MAX",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "KAMA",CommandGene.FloatClass,2,PricePos.class,TimePeriod.class),

						new CustomFunction(conf, "sma",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "tsf",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "wma",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "tema",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "dema",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "min",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "max",PricePos.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "kama",PricePos.class,2,PricePos.class,TimePeriod.class),

						new CustomFunction(conf, "TRIX",IntegerIntervalZeroBased.class,2,PricePos.class,TimePeriod.class),
						new CustomFunction(conf, "TRIMA",IntegerIntervalZeroBased.class,2,PricePos.class,TimePeriod.class),

						new CustomFunction(conf, "RSI",IntegerIntervalRSIType.class,2,PricePos.class,TimePeriod.class),

						new CustomFunction(conf, "MFI",IntegerIntervalRSIType.class,1,TimePeriod.class),

						new Terminal(conf, TimePeriod.class, 2, 30,true),
						new Terminal(conf, IntegerIntervalRSIType.class, 0, 100, true),
						new Terminal(conf, IntegerIntervalZeroBased.class, -2, 2, true)
				},
				// ADF-relevant:
				// and now the definition of ADF(1)
				//{ new Add3(conf, CommandGene.FloatClass), } 
				};
		// Here, we define the expected (optimal) output we want to achieve by
		// the
		// function/formula to evolve by the GP.
		// -----------------------------------------------------------------------
		Random random = new Random();
		// Randomly initialize function data (X-Y table) for x^4+x^3+x^2-x
		// ---------------------------------------------------------------
		for (int i = 0; i < 20; i++) {
			float f = 8.0f * (random.nextFloat() - 0.3f);
			x[i] = new Float(f);
			y[i] = f * f * f * f + f * f * f + f * f - f;
			System.out.println(i + ") " + x[i] + "   " + y[i]);
		}
		// Create genotype with initial population. Here, we use the
		// declarations
		// made above:
		// Use one result-producing chromosome (index 0) with return type float
		// (see types[0]), no argument (argTypes[0]) and several valid commands
		// and
		// terminals (nodeSets[0]). Contained in the node set is an ADF at index
		// 1
		// in the node set (as declared with the second parameter during
		// ADF-construction: new ADF(..,1,..)).
		// The ADF has return type float (types[1]), three input parameters of
		// type
		// float (argTypes[1]) and exactly one function: Add3 (nodeSets[1]).
		// ------------------------------------------------------------------------
		return GPGenotype.randomInitialGenotype(conf, types, argTypes,nodeSets, 20, true);
	}

	/**
	 * Starts the example.
	 *
	 * @param args
	 *            ignored
	 * @throws Exception
	 *
	 * @author Klaus Meffert
	 * @since 3.0
	 */
	public static void main(String[] args) throws Exception {
		TickDataSet set = new TickDataSet("VOBO30","5DK");
        CSVLoader loader;
			//loader = new CSVLoader("resources/vob30_5dk.csv","DATE>MM/dd/yy kk:mm:SSS,HOUR,OPEN,HIGH,LOW,CLOSE,VOLUME");
			loader = new CSVLoader("resources/vob30_5dk_15-19Agust.csv","DATE>MM/dd/yy kk:mm:SSS,HOUR,OPEN,HIGH,LOW,CLOSE,VOLUME");
			loader.addListener(new CSVTickListener(set));
	        loader.load();
	        
		System.out.println("Formula to discover: X^4 + X^3 + X^2 - X");
		// Setup the algorithm's parameters.
		// ---------------------------------
		GPConfiguration config = new GPConfiguration();
		// We use a delta fitness evaluator because we compute a defect rate,
		// not
		// a point score!
		// ----------------------------------------------------------------------
		config.setGPFitnessEvaluator(new DefaultGPFitnessEvaluator());
		config.setMaxInitDepth(10);
		config.setPopulationSize(1000);
		config.setMaxCrossoverDepth(20);
		config.setFitnessFunction(new Tick4JProblem.FormulaFitnessFunction(set));
		config.setStrictProgramCreation(false);
		GPProblem problem = new Tick4JProblem(config,set);
		// Create the genotype of the problem, i.e., define the GP commands and
		// terminals that can be used, and constrain the structure of the GP
		// program.
		// --------------------------------------------------------------------
		GPGenotype gp = problem.create();
		gp.setVerboseOutput(true);
		// Start the computation with maximum 800 evolutions.
		// if a satisfying result is found (fitness value almost 0), JGAP stops
		// earlier automatically.
		// --------------------------------------------------------------------
		gp.evolve(8000);
		// Print the best solution so far to the console.
		// ----------------------------------------------
		gp.outputSolution(gp.getAllTimeBest());
		// Create a graphical tree of the best solution's program and write it
		// to
		// a PNG file.
		// ----------------------------------------------------------------------
		problem.showTree(gp.getAllTimeBest(), "mathproblem_best.png");
	}

	/**
	 * Fitness function for evaluating the produced fomulas, represented as GP
	 * programs. The fitness is computed by calculating the result (Y) of the
	 * function/formula for integer inputs 0 to 20 (X). The sum of the
	 * differences between expected Y and actual Y is the fitness, the lower the
	 * better (as it is a defect rate here).
	 */
	public static class FormulaFitnessFunction extends GPFitnessFunction {

		private static final long serialVersionUID = 3049346574741996709L;
		private TickDataSet set;
		public FormulaFitnessFunction(TickDataSet set) {
			super();
			this.set = set;
		}

		protected double evaluate(final IGPProgram a_subject) {
			return computeRawFitness(a_subject);
		}

		public double computeRawFitness(final IGPProgram ind) {
			String function = ind.toStringNorm(0).split("==>")[0];
			try{
		        Positions positions = new Positions();
		        
		        //System.out.println(function);
		        BasicStrategy strategy = new BasicStrategyBuilder()
		                .addAction(new BuyExpAction(new TALibExpressionBuilder(set,"(TRIX((max((min((max((tema(l,29)),29)),29)),29)),24))<0").build()))
		                .addAction(new SellExpAction(new TALibExpressionBuilder(set,function).build()))
		                .addAction(new ClosePositionExpAction(new TALibExpressionBuilder(set, "(RSI((dema(o,27)),28))<(MFI(27))").build(), 0, positions))
		                .addAction(new ClosePositionExpAction(new TALibExpressionBuilder(set, "(RSI(h,8))>89").build(),0,positions))
						.build();
		        strategy.setMaxOpenPositionCount(1);
		        double lastClose = 0;
		        set.begin();
		        while (set.next()){
		            strategy.apply(set,positions);
		            lastClose = set.getRow().getClose();
		        }
		        set.reset();
				set.clearSubsets();
		        
		        PositionCalculator positionCalculator = new PositionCalculator(positions, lastClose);
		        positionCalculator.calculate();
		        double profit = positionCalculator.getProfit();
		        //System.out.println("PROFIT:"+profit);
		        //return fitness++;
		        return profit>0?profit:0;
			}catch(ArithmeticException e){
				//e.printStackTrace();
				//System.out.println(function);
			}
			return 0;
		}
	}
}
