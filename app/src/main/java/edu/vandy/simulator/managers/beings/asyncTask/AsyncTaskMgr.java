package edu.vandy.simulator.managers.beings.asyncTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.simulator.Controller;
import edu.vandy.simulator.managers.beings.BeingManager;

/**
 * This BeingManager implementation uses the Android AsyncTask
 * framework and the Java ExecutorService framework to create a pool
 * of Java threads that run the being simulations.
 */
public class AsyncTaskMgr
        extends BeingManager<AsyncBeing> {
    /**
     * Used for Android debugging.
     */
    private final static String TAG =
            AsyncTaskMgr.class.getName();

    /**
     * A custom ThreadPoolExecutor containing a pool of threads that
     * are allocated dynamically and cached.
     */
    // TODO -- you fill in here.
    private ThreadPoolExecutor mthreadPoolExecutor;
    /**
     * A CyclicBarrier entry barrier that ensures all background
     * threads start running at the same time.
     */
    // TODO -- you fill in here.
    private CyclicBarrier mEntryBarrier;
    /**
    /**
     * A CountDownLatch exit barrier that ensures the waiter thread
     * doesn't finish until all the async tasks finish.
     */
    // TODO -- you fill in here.
    private CountDownLatch mExitBarrier;
    /**
     * A ThreadFactory that spawns an appropriately named thread for
     * each being.
     */
    protected ThreadFactory mThreadFactory = new ThreadFactory() {
            /**
             * Used to allocate a unique id to each new thread.
             */
            private AtomicInteger mId = new AtomicInteger(0);

            /**
             * A factory method that constructs a new Thread with a
             * unique thread name.
             *
             * @param runnable a runnable to be executed by new thread instance.
             */
            @Override
            public Thread newThread(Runnable runnable) {
                // Use the mId field to ensure each new thread is
                // given a unique name.
                // TODO -- you fill in here by replacing "return null".
                return new Thread(runnable, ""+mId.incrementAndGet());
            }
    };

    /**
     * Default constructor.
     */
    public AsyncTaskMgr() {
    }

    /**
     * Resets the fields to their initial values
     * and tells all beings to reset themselves.
     */
    @Override
    public void reset() {
        super.reset();
    }

    /**
     * A factory method that BeingManagers implement to return a new
     * AsyncBeing instance.
     *
     * @return A new typed Being instance.
     */
    @Override
    public AsyncBeing newBeing() {
        // Return a new AsyncBeing instance.
        // TODO -- you fill in here, replacing null with the
        // appropriate code.
        return new AsyncBeing(this);
    }

    /**
     * This entry point method is called by the Simulator framework to
     * start the being gazing simulation.
     **/
    @Override
    public void runSimulation() {
        // Use the ThreadPoolExecutor to create and execute an async
        // task for each being.
        // TODO -- you fill in here.
        beginAsyncTasksGazing();
        // Wait for all the beings to finish gazing at the palantiri.
        // TODO -- you fill in here.
        waitForAsyncTasksToFinishGazing();
        // Call the shutdownNow() method to cleanly shutdown.
        // TODO -- you fill in here.
        shutdownNow();
    }

    /**
     * Create and execute the async tasks that represent the beings in
     * this simulation.
     */
    void beginAsyncTasksGazing() {
        // Store the number of beings to create.
        int beingCount = getBeings().size();

        // Initialize an entry barrier that ensures all async tasks
        // start running at the same time.
        // TODO -- you fill in here.
        mEntryBarrier = new CyclicBarrier(beingCount+1);
        // Initialize an exit barrier to ensure the waiter thread
        // doesn't finish until all the async tasks finish.
        // TODO -- you fill in here.
        mExitBarrier = new CountDownLatch(beingCount);
        // Create a ThreadPoolExecutor containing a pool of no more
        // than beingCount threads that are allocated dynamically and
        // cached for up to 60 seconds.  An instance of
        // SynchronousQueue should be used as the work queue and
        // mThreadFactory should be passed as the final parameter.
        // TODO -- you fill in here.
        mthreadPoolExecutor = new ThreadPoolExecutor(0,beingCount,60,TimeUnit.SECONDS,new SynchronousQueue<>(),mThreadFactory);
        // Execute all the async tasks on mThreadPoolExecutor,
        // passing in the entry and exit barriers.
        // TODO -- you fill in here.  Graduate students must use Java
        // 8 features, whereas undergraduate students can optionally
        // use Java 8 features.
        getBeings().stream().forEach(asyncBeing -> asyncBeing.executeOnExecutor(mEntryBarrier,mExitBarrier,mthreadPoolExecutor));
    }

    /**
     * Wait for all the beings to finish gazing at the palantiri.
     */
    void waitForAsyncTasksToFinishGazing() {
        // Wait for all the threads in the ThreadPoolExecutor to
        // terminate.
        try {
            // Allow all the async tasks to start gazing.
            // TODO -- you fill in here.
            mEntryBarrier.await();
            // Wait for all async tasks to stop gazing.
            // TODO -- you fill in here.
            mExitBarrier.await();
        } catch (Exception e) {
            Controller.log(TAG +
                           ": awaitTerminationOfThreadPoolExecutor() caught exception: "
                           + e);
            // Shutdown the simulation now.
            // TODO -- you fill in here.
            shutdownNow();
        }

        // Print the number of beings that were processed.
        Controller.log(TAG +
                ": awaitCompletionOfFutures: exiting with "
                + getRunningBeingCount()
                + "/"
                + getBeings().size()
                + " running beings.");
    }

    /**
     * Called to terminate the async tasks.  This method should only
     * return after all threads have been terminated and all resources
     * cleaned up.
     */
    @Override
    public void shutdownNow() {
        Controller.log(TAG + ": shutdownNow: entered");

        // Cancel all the outstanding async tasks immediately.
        // TODO -- you fill in here.  Graduate students must use Java 8
        // features, whereas undergraduate students can optionally use
        // Java 8 features.
        getBeings().stream().forEach(asyncBeing -> asyncBeing.cancel(true));
        // Shutdown the executor *now*.
        // TODO -- you fill in here.
        mthreadPoolExecutor.shutdownNow();
        Controller.log(TAG + ": shutdownNow: exited with "
                + getRunningBeingCount() + "/"
                + getBeingCount() + " running beings.");
    }
}
