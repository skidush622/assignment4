package edu.vandy.simulator;

import org.junit.Ignore;
import org.junit.Test;

import edu.vanderbilt.grader.rubric.Rubric;
import edu.vandy.simulator.managers.beings.BeingManager;
import edu.vandy.simulator.managers.palantiri.PalantiriManager;

/**
 * Precision of 0 means round percents up to 0 decimal places.
 * No point values are used for each rubric since the default
 * point value of 1 is acceptable for this assignment. No weight
 * has been specified instrumented and unit tests are worth the
 * same value towards the final mark (50% for each class each).
 */
@Ignore
@Rubric
public class UnitTest1A {
    private BeingManager.Factory.Type beingManager =
        BeingManager.Factory.Type.ASYNC_TASK;
    private PalantiriManager.Factory.Type palantirManager =
            PalantiriManager.Factory.Type.CONCURRENT_MAP_FAIR_SEMAPHORE;
            //PalantiriManager.Factory.Type.SPIN_LOCK_SEMAPHORE;

    @Rubric(value = "normalTest",
            goal = "The goal of this evaluation is to ensure that your implementation " +
                    "runs correctly with 10 beings, 6 palantiri, 10 iterations and a " +
                    "gazing delay of 0 to 50 milliseconds.",
            reference = {
                    "https://www.youtube.com/watch?v=WxpjEXt7J0g&index=6&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&t=15s",
                    "https://www.youtube.com/watch?v=8Ij9Q4AGfgc&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&index=7",
                    "https://www.youtube.com/watch?v=GdrXGs2Ipp4&index=8&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3"
            }
    )
    @Test(timeout = 5000)
    public void normalTest() {
        Controller.setLogging(false);

        TestHelper.testStrategy(
                beingManager,
                palantirManager,
                /* beingCount */ 10,
                /* palantirCount */ 6,
                /* iterations */ 10,
                /* animationSpeed */ 0f,
                /* gazingRangeMin */ 0,
                /* gazingRangeMax */ 100);
    }

    @Rubric(value = "stressTest",
            goal = "The goal of this evaluation is to ensure that your implementation " +
                    "runs correctly with 50 beings, 10 palantiri, and 100 iterations " +
                    "with no gazing delay.",
            reference = {
                    "https://www.youtube.com/watch?v=WxpjEXt7J0g&index=6&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&t=15s",
                    "https://www.youtube.com/watch?v=8Ij9Q4AGfgc&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3&index=7",
                    "https://www.youtube.com/watch?v=GdrXGs2Ipp4&index=8&list=PLZ9NgFYEMxp4p5piHxajQXRRlsyDCLvI3"
            }
    )
    @Test(timeout = 5000)
    public void stressTest() {
        Controller.setLogging(false);

        TestHelper.testStrategy(
                beingManager,
                palantirManager,
                /* beingCount */ 50,
                /* palantirCount */ 10,
                /* iterations */ 100,
                /* animationSpeed */ 0f,
                /* gazingRangeMin */ 0,
                /* gazingRangeMax */ 0);
    }
}
