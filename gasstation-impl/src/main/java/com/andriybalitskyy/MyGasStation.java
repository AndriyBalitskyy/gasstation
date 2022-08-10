package com.andriybalitskyy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class MyGasStation implements GasStation {
	private List<GasPump> gasPumps = new ArrayList();
	private Map<GasType, Double> prices = new HashMap();
	private double revenue = 0;
	private int NotEnoughGasCount = 0;
	private int GasTooExpensiveCount = 0;
	private int saleCount = 0;

	public void addGasPump(GasPump pump) {
		gasPumps.add(pump);
	}

	public Collection<GasPump> getGasPumps() {
		return gasPumps;
	}

	public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter)
			throws NotEnoughGasException, GasTooExpensiveException {
		
		Boolean isNotEnoughGas = false;
		
		if(getPrice(type) > maxPricePerLiter) {
			GasTooExpensiveCount++;
			throw new GasTooExpensiveException();
		}
		
		for(GasPump gasPump : gasPumps) {
			if(gasPump.getGasType().equals(type)) {
				if(gasPump.getRemainingAmount() >= amountInLiters) {
					gasPump.pumpGas(amountInLiters);
					revenue += amountInLiters * (maxPricePerLiter - getPrice(type));
					saleCount++;
				} else {
					isNotEnoughGas = true;
				}
			}
		}
		
		if(isNotEnoughGas) {
			NotEnoughGasCount++;
			throw new NotEnoughGasException();
		}
		
		return maxPricePerLiter * amountInLiters;
	}

	public double getRevenue() {
		return revenue;
	}

	public int getNumberOfSales() {
		return saleCount;
	}

	public int getNumberOfCancellationsNoGas() {
		return NotEnoughGasCount;
	}

	public int getNumberOfCancellationsTooExpensive() {
		return GasTooExpensiveCount;
	}

	public double getPrice(GasType type) {
		return prices.get(type);
	}

	public void setPrice(GasType type, double price) {
		prices.put(type, price);
	}

}
