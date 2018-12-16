package me.tassu.mill;

import lombok.val;
import org.junit.Test;

public class MillTest {

    @Test
    public void test() {
        val mill = Mill.create("example");
        for (Object registrable : mill.getRegistrables()) {
            System.out.println(registrable);
        }
    }

}
