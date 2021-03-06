package com.gorkemgok.tick4j.model.function;

public class Multiply implements IFunction {

	public String getSymbol() {
		return "*";
	}

	public double calculate(double value,String param, double... dparams) {
		return value * Double.parseDouble(param);
	}

	public IFunction reverseFunction() {
		return new Divide();
	}

}
