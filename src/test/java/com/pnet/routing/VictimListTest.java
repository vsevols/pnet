package com.pnet.routing;

import com.pnet.Victim;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VictimListTest {

    private VictimList victims;

    @BeforeEach
    void setUp() {
        victims = new VictimList();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getOrDefault() {
        victims.add(new Victim(123, "", ""));
        //Explicit boxing has meaning in this test
        Victim victim = victims.getOrDefault(new Integer(123), null);
        assertEquals(victim.id, 123);
    }
}