package com.example.foodwithfriends;

import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UserGenerationTest extends User {
    @Test
    public void user_name_correct() {
        String name = "default user";
        User a = new User();
        a.setName(name);
        assertEquals(a.getName(), name);
    }

    @Test
    public void user_name_false() {
        String name = "default user";
        User a = new User();
        a.setName("wrong");
        assertNotEquals(a.getName(), name);
    }
}