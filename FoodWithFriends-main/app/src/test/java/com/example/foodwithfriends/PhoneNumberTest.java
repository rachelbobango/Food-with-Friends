package com.example.foodwithfriends;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.security.auth.login.LoginException;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(JUnit4.class)
public class PhoneNumberTest extends MainActivity{
    final String correctNum = "+13333333333";
    String baseNumber = "3333333333";
    LoginFragment loginFragment;
    final String TAG = "phoneTest";
    @Test
    public void phoneNumber_isCorrect() {
        //FragmentScenario<LoginFragment> act = FragmentScenario.launch(LoginFragment.class);
        loginFragment = new LoginFragment();

        assertEquals(correctNum, loginFragment.getPhoneNumber(baseNumber));
    }
}