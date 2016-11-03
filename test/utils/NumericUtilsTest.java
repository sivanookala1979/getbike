package utils;

import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by sivanookala on 03/11/16.
 */
public class NumericUtilsTest {

    @Test
    public void generateOtpTESTHappyFlow(){
        Set<String> otps = new HashSet<>();
        for(int i = 0; i < 200; i++) {
            String otp = NumericUtils.generateOtp();
            otps.add(otp);
            assertEquals(6, otp.length());
        }
        assertEquals(200, otps.size());
    }
}
