import com.andriybalitskyy.MyGasStation;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MyGasStationTest {

	private MyGasStation theStation;
	private List<ClientBuyGas> clients = new ArrayList<>();

	@BeforeEach
	public void setUp() {
		theStation = new MyGasStation();

		clients = new ArrayList<>();

		List<ClientBuyGas> clientsBuyRegular = generateClients(100, GasType.REGULAR, 10, 70);
		List<ClientBuyGas> clientsBuyDiesel = generateClients(100, GasType.DIESEL, 10, 70);
		List<ClientBuyGas> clientsBuySuper = generateClients(100, GasType.SUPER, 10, 70);

		GasPump regularGas = new GasPump(GasType.REGULAR, clientsBuyRegular.stream().mapToInt(ClientBuyGas::getAmountInLiters).sum());
		GasPump dieselGas = new GasPump(GasType.DIESEL, clientsBuyDiesel.stream().mapToInt(ClientBuyGas::getAmountInLiters).sum());
		GasPump superGas = new GasPump(GasType.SUPER, clientsBuySuper.stream().mapToInt(ClientBuyGas::getAmountInLiters).sum());

		theStation.addGasPump(regularGas);
		theStation.addGasPump(dieselGas);
		theStation.addGasPump(superGas);

		theStation.setPrice(GasType.REGULAR, 40);
		theStation.setPrice(GasType.DIESEL, 55);
		theStation.setPrice(GasType.SUPER, 50);

		clients.addAll(clientsBuyRegular);
		clients.addAll(clientsBuyDiesel);
		clients.addAll(clientsBuySuper);
	}

	@Test
	public void checkCorrectGasPumpAmountLeft() throws InterruptedException {
		ExecutorService service = Executors.newCachedThreadPool();
		Map<GasType, Double> gasAmountLeft = new HashMap<>();

		CountDownLatch latch = new CountDownLatch(clients.size()/2);
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
				() -> theStation.buyGas(GasType.DIESEL, 1000000, 60));
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

	private List<ClientBuyGas> generateClients(int countOfClients, GasType type, int numbRange, int maxPricePerLiter) {
		return IntStream.range(1, countOfClients).mapToObj(i -> generateClient(type, numbRange, maxPricePerLiter)).collect(Collectors.toList());
	}

	private ClientBuyGas generateClient(GasType type, int numbRange, int maxPricePerLiter) {
		return new ClientBuyGas(type, new Random().nextInt(numbRange) + 1, 70);
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