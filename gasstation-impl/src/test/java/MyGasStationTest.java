import com.andriybalitskyy.MyGasStation;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.*;

@ExtendWith(MockitoExtension.class)
public class MyGasStationTest {

	@Mock
	private MyGasStation theStation;
	@Mock
	private List<ClientBuyGas> clients = new ArrayList<>();

	@BeforeEach
	public void setUp() {
		theStation = new MyGasStation();

		GasPump regularGas = new GasPump(GasType.REGULAR, 900);
		GasPump dieselGas = new GasPump(GasType.DIESEL, 900);
		GasPump superGas = new GasPump(GasType.SUPER, 900);

		theStation.addGasPump(regularGas);
		theStation.addGasPump(dieselGas);
		theStation.addGasPump(superGas);

		theStation.setPrice(GasType.REGULAR, 40);
		theStation.setPrice(GasType.DIESEL, 55);
		theStation.setPrice(GasType.SUPER, 50);

		clients = new ArrayList<>();
		clients.add(new ClientBuyGas(GasType.DIESEL, 17, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 5, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 5, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 5, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 2, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 3, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 19, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 7, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 7, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 8, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 1, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 1, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 6, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 5, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 8, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 3, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 5, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 2, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 4, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 8, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 10, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 14, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 1, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 55, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 2, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 17, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 5, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 5, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 5, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 2, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 3, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 19, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 7, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 7, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 8, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 1, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 1, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 6, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 5, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 8, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 3, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 5, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 2, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 4, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 8, 70));
		clients.add(new ClientBuyGas(GasType.SUPER, 10, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 14, 70));
		clients.add(new ClientBuyGas(GasType.DIESEL, 1, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 55, 70));
		clients.add(new ClientBuyGas(GasType.REGULAR, 2, 70));
	}

	@Test
	public void checkCorrectGasPumpAmountLeft() throws InterruptedException {

		ExecutorService service = Executors.newFixedThreadPool(50);
		Map<GasType, Double> gasAmountLeft = new HashMap<>();

		CountDownLatch latch = new CountDownLatch(50);
		for(GasPump gasPump : theStation.getGasPumps()) {
			gasAmountLeft.put(gasPump.getGasType(), gasPump.getRemainingAmount());
		}
		for (ClientBuyGas client : clients) {
			gasAmountLeft.put(client.getGasType(), gasAmountLeft.get(client.getGasType()) - client.getAmountInLiters());
		}


		for (ClientBuyGas client : clients) {
			service.submit(() -> {
				try {
					theStation.buyGas(client.getGasType(), client.getAmountInLiters(), client.getMaxPricePerLiter());
				} catch (NotEnoughGasException | GasTooExpensiveException e) {
					e.printStackTrace();
				}

			});
			latch.countDown();
		}

		latch.await();

		service.shutdown();

		service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		for(GasPump gasPump : theStation.getGasPumps()) {
			Assertions.assertEquals(gasPump.getRemainingAmount(), gasAmountLeft.get(gasPump.getGasType()));
		}
	}

	@Test
	public void catchNotEnoughGasException() {
		Assertions.assertThrows(
				NotEnoughGasException.class,
				() -> theStation.buyGas(GasType.DIESEL, 1000, 60));
		Assertions.assertEquals(theStation.getNumberOfCancellationsNoGas(), 1);
		Assertions.assertEquals(theStation.getNumberOfCancellationsTooExpensive(), 0);
	}

	@Test
	public void catchGasTooExpensiveException() {
		Assertions.assertThrows(
				GasTooExpensiveException.class,
				() -> theStation.buyGas(GasType.REGULAR, 100, 30));
		Assertions.assertEquals(theStation.getNumberOfCancellationsTooExpensive(), 1);
		Assertions.assertEquals(theStation.getNumberOfCancellationsNoGas(), 0);
	}


}

class ClientBuyGas {
	private GasType gasType;
	private int amountInLiters;
	private int maxPricePerLiter;

	public ClientBuyGas(GasType gasType, int amountInLiters, int maxPricePerLiter) {
		this.gasType = gasType;
		this.amountInLiters = amountInLiters;
		this.maxPricePerLiter = maxPricePerLiter;
	}

	public GasType getGasType() {
		return gasType;
	}

	public int getAmountInLiters() {
		return amountInLiters;
	}

	public int getMaxPricePerLiter() {
		return maxPricePerLiter;
	}
}