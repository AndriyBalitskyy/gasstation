package com.andriybalitskyy;

import java.util.Collection;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class MyGasStation implements GasStation {

	public void addGasPump(GasPump pump) {
		// TODO Auto-generated method stub

	}

	public Collection<GasPump> getGasPumps() {
		// TODO Auto-generated method stub
		return null;
	}

	public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter)
			throws NotEnoughGasException, GasTooExpensiveException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getRevenue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfSales() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfCancellationsNoGas() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfCancellationsTooExpensive() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getPrice(GasType type) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setPrice(GasType type, double price) {
		// TODO Auto-generated method stub

	}

}
