package com.soen345.ticketreservation.activity.event;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketreservation.event.EventManager;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventManagerTest {

    @Test
    public void testSingletonIdentity() {
        EventManager instance1 = EventManager.getInstance();
        EventManager instance2 = EventManager.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }
}