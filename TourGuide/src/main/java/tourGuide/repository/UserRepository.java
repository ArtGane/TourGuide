package tourGuide.repository;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Repository;
import tourGuide.conf.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

@Repository
@Slf4j
public class UserRepository {

    private final Map<String, User> internalUserMap = new HashMap<>();
    private static final List<String> CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "JPY", "AUD");

    public Map<String, User> getMapOfUsers() {
        if (internalUserMap.isEmpty()) {
            initializeInternalUsers();
        }
        return internalUserMap;
    }

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "TestUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email,
                    new UserPreferences());
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i-> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    private int generateRandomAttractionProximity() {
        Random random = new Random();
        return random.nextInt(10);
    }


    private String generateRandomCurrency() {
        Random random = new Random();
        int index = random.nextInt(CURRENCIES.size());
        return CURRENCIES.get(index);
    }

    private BigDecimal generateRandomAmount() {
        Random random = new Random();
        int amount = random.nextInt(990) + 10;
        return BigDecimal.valueOf(amount);
    }

    private CurrencyUnit generateCurrency(String currencyCode) {
        return Monetary.getCurrency(currencyCode);
    }

    private Money generateRandomMoney() {
        String currencyCode = generateRandomCurrency();
        BigDecimal amount = generateRandomAmount();
        CurrencyUnit currency = generateCurrency(currencyCode);
        return (Money) Monetary.getDefaultAmountFactory()
                .setCurrency(currency)
                .setNumber(amount)
                .create();
    }

    private int generateRandomTripDuration() {
        Random random = new Random();
        return random.nextInt(30) + 1;    }

    private int generateRandomTicketQuantity() {
        Random random = new Random();
        return random.nextInt(10) + 1;    }

    private int generateRandomNumberOfAdults() {
        Random random = new Random();
        return random.nextInt(4) + 1;    }

    private int generateRandomNumberOfChildren() {
        Random random = new Random();
        return random.nextInt(5);    }
}
