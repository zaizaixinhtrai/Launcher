package ngoc.neamar.kiss.test;

import ngoc.neamar.kiss.BuildConfig;
import ngoc.neamar.kiss.MainActivity;
import ngoc.neamar.kiss.KissApplication;
import ngoc.neamar.kiss.DataHandler;

import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {
    DataHandler mockDataHandler;

    @Before
    public void setUp() {
        mockDataHandler = mock(DataHandler.class);
        KissApplication.setDataHandler(mockDataHandler);
    }

    @Test
    public void testSomething() throws Exception {
        // TODO-add more tests...
        assertTrue(Robolectric.setupActivity(MainActivity.class) != null);
    }
}
