package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData(){};


    public static String getName(){
        return RandomStringUtils.randomAlphabetic(10)+" "+RandomStringUtils.randomAlphabetic(10);
    }

    public static float getDeposit() {
        int cents = ThreadLocalRandom.current().nextInt(1, 500001);
        return cents / 100.0f;  // Автоматически дает 2 знака после запятой
    }
}
