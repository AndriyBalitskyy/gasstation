package com.andriybalitskyy;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class MyGasStationDemo {

	public static void main(String[] args) throws NotEnoughGasException, GasTooExpensiveException {
		MyGasStation myGasStation = new MyGasStation();
		myGasStation.addGasPump(new GasPump(GasType.DIESEL, 80));
		myGasStation.addGasPump(new GasPump(GasType.REGULAR, 40));
		myGasStation.addGasPump(new GasPump(GasType.SUPER, 40));
		
		myGasStation.setPrice(GasType.DIESEL, 55);
		myGasStation.setPrice(GasType.REGULAR, 42);
		myGasStation.setPrice(GasType.SUPER, 48);

		int countSalesBeforeOpening = myGasStation.getNumberOfSales();
		System.out.println("countSalesBeforeOpening : " + countSalesBeforeOpening);
		
		myGasStation.buyGas(GasType.REGULAR, 10, 45);
		myGasStation.buyGas(GasType.DIESEL, 10, 60);
		myGasStation.buyGas(GasType.DIESEL, 10, 62);
		myGasStation.buyGas(GasType.SUPER, 10, 48);
//		myGasStation.buyGas(GasType.REGULAR, 10, 40);
//		myGasStation.buyGas(GasType.REGULAR, 50, 45);
		myGasStation.buyGas(GasType.DIESEL, 10, 70);
		myGasStation.buyGas(GasType.REGULAR, 10, 45);
		myGasStation.buyGas(GasType.DIESEL, 10, 60);
		myGasStation.buyGas(GasType.DIESEL, 10, 62);
		myGasStation.buyGas(GasType.SUPER, 10, 48);
//		myGasStation.buyGas(GasType.REGULAR, 10, 40);
//		myGasStation.buyGas(GasType.REGULAR, 50, 45);
		myGasStation.buyGas(GasType.DIESEL, 10, 70);
		
		
		System.out.println("NotEnoughGasCount " + myGasStation.getNumberOfCancellationsNoGas());
		System.out.println("GasTooExpensiveCount " + myGasStation.getNumberOfCancellationsTooExpensive());
		System.out.println("revenue " + myGasStation.getRevenue());
	}

}
