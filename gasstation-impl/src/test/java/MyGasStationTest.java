import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.andriybalitskyy.MyGasStation;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class MyGasStationTest {
	
	private MyGasStation theStation;
	
	@BeforeTest
	public void setUp() {
		theStation = new MyGasStation();
		
		GasPump regularGas = new GasPump(GasType.REGULAR, 260);
		GasPump dieselGas = new GasPump(GasType.DIESEL, 160);
		GasPump superGas = new GasPump(GasType.SUPER, 460);
		
		theStation.addGasPump(regularGas);
		theStation.addGasPump(dieselGas);
		theStation.addGasPump(superGas);

		theStation.setPrice(GasType.REGULAR, 40);
		theStation.setPrice(GasType.DIESEL, 55);
		theStation.setPrice(GasType.SUPER, 50);
	}

	@Test
	public void catchNotEnoughGasException() {
		Boolean thrownNotEnoughGasException = false;
		Boolean thrownGasTooExpensiveException = false;
		try {
			theStation.buyGas(GasType.DIESEL, 1000, 60);
		} catch (GasTooExpensiveException e) {
			thrownGasTooExpensiveException = true;
		} catch (NotEnoughGasException e) {
			thrownNotEnoughGasException = true;
		}
		
		Assert.assertEquals(thrownNotEnoughGasException, true);
		Assert.assertEquals(thrownGasTooExpensiveException, false);
	}

	@Test
	public void catchGasTooExpensiveException() {
		Boolean thrownNotEnoughGasException = false;
		Boolean thrownGasTooExpensiveException = false;
		try {
			theStation.buyGas(GasType.DIESEL, 100, 30);
		} catch (GasTooExpensiveException e) {
			thrownGasTooExpensiveException = true;
		} catch (NotEnoughGasException e) {
			thrownNotEnoughGasException = true;
		}

		Assert.assertEquals(thrownGasTooExpensiveException, true);
		Assert.assertEquals(thrownNotEnoughGasException, false);
	}
	
}
