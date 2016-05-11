package com.example.chris.situaware;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.junit.Test;

import dalvik.annotation.TestTargetClass;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import android.content.SharedPreferences;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {

    String[] array = {"Incident 1", "Incident 2", "Incident 3", "Incident 4"};

    @Mock
    Context mMockContext;


    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getIncidentCode() throws Exception {
        Incident incident = new Incident(11, 2, "13 May 2016, 12:45", "51.01145", "14.12210", mMockContext);
        int result = incident.getIncidentType();
        assertThat(result, is(10));
    }


    @Test
    public void getIncidentDetail() throws Exception {
        when(mMockContext.getString(R.string.app_name))
                .thenReturn("SituAware");
        Incident incident = new Incident(11, 2, "13 May 2016, 12:45", "51.01145", "14.12210", mMockContext);
        String result = incident.getIncidentDetail();
        assertThat(result, is("SituAware"));
    }

    @Test
    public void getTime() throws Exception {
        Incident incident = new Incident(11, 2, "13 May 2016, 12:45", "51.01145", "14.12210", mMockContext);
        String result = incident.getIncidentTime();
        assertThat(result, is("13 May 2016, 12:45"));
    }

    @Test
    public void getLat() throws Exception {
        Incident incident = new Incident(11, 2, "13 May 2016, 12:45", "51.01145", "14.12210", mMockContext);
        String result = incident.getLatitude();
        assertThat(result, is("51.01145"));
    }

    @Test
    public void getLong() throws Exception {
        Incident incident = new Incident(11, 2, "13 May 2016, 12:45", "51.01145", "14.12210", mMockContext);
        String result = incident.getLongitude();
        assertThat(result, is("14.12210"));
    }
}