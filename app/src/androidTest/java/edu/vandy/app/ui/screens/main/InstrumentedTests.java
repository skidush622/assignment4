package edu.vandy.app.ui.screens.main;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.core.internal.deps.guava.collect.Iterables;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import edu.vanderbilt.grader.rubric.Rubric;
import edu.vandy.R;
import edu.vandy.app.ui.screens.settings.Settings;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;
import edu.vandy.simulator.model.implementation.components.SimulatorComponent;
import edu.vandy.simulator.model.interfaces.ModelObserver;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;

@RunWith(AndroidJUnit4.class)
@Rubric(threshold = 0.7, precision = 1)
public class InstrumentedTests {
    /**
     * Logging tag.
     */
    private static final String TAG = "PalantiriActivityTest";

    /**
     * Wait time constants.
     */
    private final int CONFIG_TIMEOUT = 4000;
    private final int PALANTIRI = 6;
    private final int BEINGS = 10;
    private final int ITERATIONS = 10;
    private final int SPEED = 100;
    private final Range<Integer> GAZING = new Range<>(0, 1);
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);
    /**
     * Model parameters.
     */
    private BeingManager.Factory.Type beingManager =
            BeingManager.Factory.Type.ASYNC_TASK;
    private PalantiriManager.Factory.Type palantirManager =
        PalantiriManager.Factory.Type.CONCURRENT_MAP_FAIR_SEMAPHORE;
    private SimulatorComponent.State finalState =
            SimulatorComponent.State.UNDEFINED;

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Rubric(
            value = "normalTest",
            goal = "The goal of this evaluation is to ensure that your implementation " +
                    "runs correctly on an Android emulator using with 10 beings, " +
                    "6 palantiri, 10 iterations and a gazing delay of 0 to 50 milliseconds.",
            reference = {
                    "https://www.youtube.com/watch?v=WxpjEXt7J0g&index=6&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&t=15s",
                    "https://www.youtube.com/watch?v=8Ij9Q4AGfgc&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&index=7",
                    "https://www.youtube.com/watch?v=GdrXGs2Ipp4&index=8&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3"
            }
    )
    @Test(timeout = 50000)
    public void normalTest() throws Throwable {
        // Sleep to wait for app to start.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction actionButton = onView(
                allOf(withId(R.id.progressFab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.drawerLayout),
                                        0),
                                2),
                        isDisplayed()));

        // Force config change.
        setOrientationPortrait(CONFIG_TIMEOUT);

        Settings.setBeingManagerType(beingManager);
        Settings.setPalantirManagerType(palantirManager);
        Settings.setBeingCount(BEINGS);
        Settings.setPalantirCount(PALANTIRI);
        Settings.setGazingIterations(ITERATIONS);
        Settings.setAnimationSpeed(SPEED);
        Settings.setGazingDuration(GAZING);

        assertThat(Settings.getBeingManagerType(), equalTo(beingManager));
        assertThat(Settings.getPalantirManagerType(), equalTo(palantirManager));
        assertThat(Settings.getBeingCount(), equalTo(BEINGS));
        assertThat(Settings.getPalantirCount(), equalTo(PALANTIRI));
        assertThat(Settings.getGazingIterations(), equalTo(ITERATIONS));
        assertThat(Settings.getAnimationSpeed(), equalTo(SPEED));

        MainActivity activity = (MainActivity) getCurrentActivity();

        finalState = SimulatorComponent.State.UNDEFINED;

        final SimulatorComponent.State desiredState =
                SimulatorComponent.State.COMPLETED;

        // Has to be declared as a variable or else it
        // will be garbage collected after the addObserver()
        // call.
        ModelObserver observer = snapshot -> {
            SimulatorComponent.State state =
                    snapshot.getSimulator().getState();
            switch (state) {
                case IDLE:
                case RUNNING:
                case CANCELLING:
                    break;
                case CANCELLED:
                case ERROR:
                    assertThat(state, equalTo(desiredState));
                case COMPLETED:
                    if (finalState ==
                            SimulatorComponent.State.UNDEFINED) {
                        finalState =
                                snapshot.getSimulator().getState();
                    }
                    break;
            }
        };

        // Add this test as a snapshot observer.
        Objects.requireNonNull(activity.viewModel
                .getSimulator())
                .addObserver(observer, false);

        // Force config change.
        actionButton.perform(click());

        int maxSleeps = 30; // 40 seconds max
        long sleepTime = 1000;

        for (int i = 1; i <= maxSleeps; i++) {
            Thread.sleep(sleepTime);

            if (i % 4 == 0) {
                toggleOrientation(CONFIG_TIMEOUT);
            }

            if (finalState != SimulatorComponent.State.UNDEFINED) {
                break;
            }
        }

        // Force a config change.
        setOrientationPortrait(CONFIG_TIMEOUT);

        assertThat(finalState, equalTo(SimulatorComponent.State.COMPLETED));

        // Success!
        Log.d(TAG, "The test was successful!");
    }

    @Rubric(
            value = "startStopStressTest",
            goal = "The goal of this evaluation is to ensure that your implementation " +
                    "handles a series of 20 cancel and restart operations using 10 beings, " +
                    "6 palantiri, 100 iterations, and a gazing delay of 0 milliseconds.",
            reference = {
                    "https://www.youtube.com/watch?v=WxpjEXt7J0g&index=6&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&t=15s",
                    "https://www.youtube.com/watch?v=8Ij9Q4AGfgc&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&index=7",
                    "https://www.youtube.com/watch?v=GdrXGs2Ipp4&index=8&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3"
            }
    )
    @Test(timeout = 50000)
    public void startStopStressTest() throws Throwable {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction actionButton = onView(
                allOf(withId(R.id.progressFab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.drawerLayout),
                                        0),
                                2),
                        isDisplayed()));

        // Force config change.
        setOrientationPortrait(CONFIG_TIMEOUT);

        Settings.setBeingManagerType(beingManager);
        Settings.setPalantirManagerType(palantirManager);
        Settings.setBeingCount(BEINGS);
        Settings.setPalantirCount(PALANTIRI);
        Settings.setGazingIterations(ITERATIONS);
        Settings.setAnimationSpeed(0);
        Settings.setGazingDuration(new Range<>(0, 0));

        assertThat(Settings.getBeingManagerType(), equalTo(beingManager));
        assertThat(Settings.getPalantirManagerType(), equalTo(palantirManager));
        assertThat(Settings.getBeingCount(), equalTo(BEINGS));
        assertThat(Settings.getPalantirCount(), equalTo(PALANTIRI));
        assertThat(Settings.getGazingIterations(), equalTo(ITERATIONS));
        assertThat(Settings.getAnimationSpeed(), equalTo(0));

        MainActivity activity = (MainActivity) getCurrentActivity();

        // Has to be declared as a variable or else it will be
        // garbage collected after the addObserver() call.
        finalState = SimulatorComponent.State.UNDEFINED;
        ModelObserver observer = snapshot -> {
            finalState = snapshot.getSimulator().getState();
            assertThat(finalState, not(equalTo(SimulatorComponent.State.ERROR)));
        };

        // Add this test as a snapshot observer.
        Objects.requireNonNull(activity.viewModel
                .getSimulator())
                .addObserver(observer, false);

        for (int i = 1; i < 20; i++) {
            actionButton.perform(click());
            Thread.sleep(1);
        }

        Thread.sleep(50);

        assertThat(
                finalState,
                isOneOf(SimulatorComponent.State.COMPLETED,
                        SimulatorComponent.State.CANCELLED));

        // Success!
        Log.d(TAG, "The test was successful!");
    }

    public void setOrientationLandscape(int wait) {
        Log.d(TAG, "palantiriActivityTest: setting orientation to LANDSCAPE");
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, wait);
    }

    public void setOrientationPortrait(int wait) {
        Log.d(TAG, "palantiriActivityTest: setting orientation to PORTRAIT");
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, wait);
    }

    public void setOrientation(int orientation, int wait) {
        try {
            getCurrentActivity().setRequestedOrientation(orientation);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // Give the system app to settle.
        SystemClock.sleep(wait);
    }

    public void toggleOrientation(int wait) {
        try {
            switch (getCurrentActivity().getRequestedOrientation()) {
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    setOrientationLandscape(wait);
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    setOrientationPortrait(wait);
                    break;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    private Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            java.util.Collection<Activity> activities =
                    ActivityLifecycleMonitorRegistry
                            .getInstance().getActivitiesInStage(Stage.RESUMED);
            activity[0] = Iterables.getOnlyElement(activities);
        });
        return activity[0];
    }
}
