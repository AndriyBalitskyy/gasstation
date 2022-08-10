package com.andriybalitskyy;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class MyGasStation implements GasStation {
	private CopyOnWriteArrayList<GasPump> gasPumps = new CopyOnWriteArrayList();
	private ConcurrentMap<GasType, Double> prices = new ConcurrentHashMap<>();
	private AtomicLong revenue = new AtomicLong(0);
	private AtomicInteger NotEnoughGasCount = new AtomicInteger(0);
	private AtomicInteger GasTooExpensiveCount = new AtomicInteger(0);
	private AtomicInteger saleCount = new AtomicInteger(0);

	public void addGasPump(GasPump pump) {
		gasPumps.add(pump);
	}

	public Collection<GasPump> getGasPumps() {
		return gasPumps;
	}

	public synchronized double buyGas(GasType type, double amountInLiters, double maxPricePerLiter)
			throws NotEnoughGasException, GasTooExpensiveException {

		if(getPrice(type) > maxPricePerLiter) {
			GasTooExpensiveCount.incrementAndGet();
			throw new GasTooExpensiveException();
		}

		Boolean isEnoughGas = false;

		for(GasPump gasPump : gasPumps) {
			if(gasPump.getGasType().equals(type)) {
				if(gasPump.getRemainingAmount() >= amountInLiters) {
					gasPump.pumpGas(amountInLiters);
					revenue.getAndAdd((long) (amountInLiters * (maxPricePerLiter - getPrice(type))));
					saleCount.incrementAndGet();
					isEnoughGas = true;
				}
			}
		}
		
		if(!isEnoughGas) {
			NotEnoughGasCount.incrementAndGet();
			throw new NotEnoughGasException();
		}

		return maxPricePerLiter * amountInLiters;
	}

	public double getRevenue() {
		return revenue.doubleValue();
	}

	public int getNumberOfSales() {
		return saleCount.get();
	}

	public int getNumberOfCancellationsNoGas() {
		return NotEnoughGasCount.get();
	}

	public int getNumberOfCancellationsTooExpensive() {
		return GasTooExpensiveCount.get();
	}

	public double getPrice(GasType type) {
		return prices.get(type);
	}

	public void setPrice(GasType type, double price) {
		prices.put(type, price);
	}

}
