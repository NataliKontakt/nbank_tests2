package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {
    private RandomData(){};

    public static String getUserName(){
        return RandomStringUtils.randomAlphabetic(10);
    }
    public static String getUserPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase()+
                RandomStringUtils.randomAlphabetic(3).toLowerCase()+
                RandomStringUtils.randomNumeric(2)+ "#^&";
    }
    public static String getName(){
        return RandomStringUtils.randomAlphabetic(10)+" "+RandomStringUtils.randomAlphabetic(10);
    }
}
