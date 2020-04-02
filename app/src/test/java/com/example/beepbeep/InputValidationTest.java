package com.example.beepbeep;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class InputValidationTest {
    @Test
    public void testValidEmail(){
        assertTrue(InputValidation.validEmail("abc@gmail.com"));
        assertFalse(InputValidation.validEmail("abcasf@gmda12--il.com"));
    }

    @Test
    public void testValidPhone(){
        assertFalse(InputValidation.validPhone("111111111112"));
        assertFalse(InputValidation.validPhone("1"));
        assertFalse(InputValidation.validPhone("flkn2j3f"));
        assertTrue(InputValidation.validPhone("111-111-1111"));
    }

    @Test
    public void testValidUsername(){
        assertFalse(InputValidation.validUsername("asd"));
        assertTrue(InputValidation.validUsername("ewfqwfwqfwfwe"));
    }

}
