import com.andriybalitskyy.MyGasStation;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MyGasStationTest {

	private MyGasStation theStation;
	private List<ClientBuyGas> clients = new ArrayList<>();
	public static final int STANDARD_MAX_PRICE_PER_LITER = 70;
	public static final int COUNT_OF_CLIENTS = 50;
	public static final int NUMBER_RANGE = 10;

	@BeforeEach
	public void setUp() {
		theStation = new MyGasStation();

		clients = new ArrayList<>();

		List<ClientBuyGas> clientsBuyRegular = generateClients(COUNT_OF_CLIENTS, GasType.REGULAR, NUMBER_RANGE, STANDARD_MAX_PRICE_PER_LITER);
		List<ClientBuyGas> clientsBuyDiesel = generateClients(COUNT_OF_CLIENTS, GasType.DIESEL, NUMBER_RANGE, STANDARD_MAX_PRICE_PER_LITER);
		List<ClientBuyGas> clientsBuySuper = generateClients(COUNT_OF_CLIENTS, GasType.SUPER, NUMBER_RANGE, STANDARD_MAX_PRICE_PER_LITER);

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
	public void checkCorrectSales() throws InterruptedException {
		int salesCount = clients.size();
		ExecutorService service = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(clients.size()/2);

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

		Assertions.assertEquals(theStation.getNumberOfSales(), salesCount);
	}

	@Test
	public void checkCorrectRevenue() throws InterruptedException {
		double revenue = 0d;
		for (ClientBuyGas client : clients) {
			revenue += client.getAmountInLiters() * (STANDARD_MAX_PRICE_PER_LITER - theStation.getPrice(client.getGasType()));
		}

		ExecutorService service = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(clients.size()/2);

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

		Assertions.assertEquals(theStation.getRevenue(), revenue);
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
	public void catchNotEnoughGasException() throws InterruptedException {
		final int impossiblyLargeAmountOfGas = 50000;
		AtomicInteger countNotEnoughGasExceptions = new AtomicInteger();

		ExecutorService service = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(clients.size()/2);

		for (ClientBuyGas client : clients) {
			service.submit(() -> {

				try {
					theStation.buyGas(client.getGasType(), impossiblyLargeAmountOfGas, client.getMaxPricePerLiter());
				} catch (NotEnoughGasException e) {
					countNotEnoughGasExceptions.getAndIncrement();
				} catch (GasTooExpensiveException e) {
					e.printStackTrace();
				}

			});
			latch.countDown();
		}

		latch.await();
		service.shutdown();
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		Assertions.assertEquals(theStation.getNumberOfCancellationsNoGas(), countNotEnoughGasExceptions.get());
		Assertions.assertEquals(theStation.getNumberOfCancellationsTooExpensive(), 0);
	}

	@Test
	public void catchGasTooExpensiveException() throws InterruptedException {
		final int noRealCheapPricePerLiter = 30;
		AtomicInteger countGasTooExpensiveExceptions = new AtomicInteger();

		ExecutorService service = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(clients.size()/2);

		for (ClientBuyGas client : clients) {
			service.submit(() -> {

				try {
					theStation.buyGas(client.getGasType(), client.getAmountInLiters(), noRealCheapPricePerLiter);
				} catch (NotEnoughGasException e) {
					e.printStackTrace();
				} catch (GasTooExpensiveException e) {
					countGasTooExpensiveExceptions.getAndIncrement();
				}

			});
			latch.countDown();
		}

		latch.await();
		service.shutdown();
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		Assertions.assertEquals(theStation.getNumberOfCancellationsNoGas(), 0);
		Assertions.assertEquals(theStation.getNumberOfCancellationsTooExpensive(), countGasTooExpensiveExceptions.get());
	}

	@Test
	public void testCorrectRunningWithoutExceptions() throws InterruptedException {
		AtomicReference<Boolean> isNotEnoughGasExceptionOrGasTooExpensiveException = new AtomicReference<>(false);
		ExecutorService service = Executors.newCachedThreadPool();
		CountDownLatch latch = new CountDownLatch(clients.size()/2);

		for (ClientBuyGas client : clients) {
			service.submit(() -> {
				try {
					theStation.buyGas(client.getGasType(), client.getAmountInLiters(), client.getMaxPricePerLiter());
				} catch (NotEnoughGasException | GasTooExpensiveException e) {
					isNotEnoughGasExceptionOrGasTooExpensiveException.set(true);
				}

			});
			latch.countDown();
		}

		latch.await();
		service.shutdown();
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		Assertions.assertEquals(isNotEnoughGasExceptionOrGasTooExpensiveException.get(), Boolean.FALSE);
	}

	private List<ClientBuyGas> generateClients(int countOfClients, GasType type, int numbRange, int maxPricePerLiter) {
		return IntStream.range(1, countOfClients).mapToObj(i -> generateClient(type, numbRange, maxPricePerLiter)).collect(Collectors.toList());
	}

	private ClientBuyGas generateClient(GasType type, int numbRange, int maxPricePerLiter) {
		return new ClientBuyGas(type, new Random().nextInt(numbRange) + 1, maxPricePerLiter);
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