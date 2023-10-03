/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.andavin.images.util;

import com.andavin.images.Images;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A class to make using the {@link BukkitScheduler} less
 * cumbersome and easier to use. Also, allows for easy
 * {@code do-while} loops and similar condition based looping
 * in timed loop tasks.
 *
 * @author Andavin
 * @since February 9, 2018
 */
public final class Scheduler {

    private static final Plugin INSTANCE = JavaPlugin.getPlugin(Images.class);


    /**
     * Run a task asynchronously on a separate thread using
     * the {@link BukkitScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     */
    public static void async(Runnable run) {
        Bukkit.getAsyncScheduler().runNow(INSTANCE, $ -> run.run());
    }

    /**
     * Run a task asynchronously after a specified amount of
     * ticks using the {@link BukkitScheduler}.
     *
     * @param run   The {@link Runnable} task to execute.
     * @param delay The ticks (1 tick = 50 milliseconds, 20 ticks = 1 second) after which to run the task.
     */
    public static void laterAsync(Runnable run, long delay) {
        Bukkit.getAsyncScheduler().runDelayed(INSTANCE, $ -> run.run(), delay * 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a task asynchronously repeatedly after a specified amount
     * of ticks and repeated every period ticks until canceled.
     *
     * @param run    The {@link Runnable} task to execute every period.
     * @param delay  The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     */
    public static void repeatAsync(Runnable run, long delay, long period) {
        Bukkit.getAsyncScheduler().runAtFixedRate(INSTANCE, $ -> run.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a task asynchronously repeatedly (every period of ticks) as long
     * as the condition met. Once the condition is no longer met (returns
     * {@code false}) the loop will end.
     * <p>
     * In behavior towards the condition, this is identical to a {@code while}
     * loop. If the condition is {@code false} to begin with or by the time
     * that {@code delay} has elapsed, then the task will never be executed and
     * will immediately be cancelled on the first run.
     *
     * @param run       The {@link Runnable} task to execute every period.
     * @param delay     The delay in ticks before the first run of the task.
     * @param period    The period in ticks to wait until running again after each run.
     * @param condition The {@link Supplier condition} that must be {@code true}
     *                  in order for the task to continue to run.
     */
    public static void repeatAsyncWhile(Runnable run, long delay, long period, BooleanSupplier condition) {
        Task task = new Task(run, condition);
        task.setTask(Bukkit.getAsyncScheduler().runAtFixedRate(INSTANCE, $ -> task.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS)::cancel);
    }

    private static class Task implements Runnable {

        CancellableTask task;
        boolean cancelled;
        private final Runnable runnable;
        private final BooleanSupplier condition;

        Task(Runnable runnable, BooleanSupplier condition) {
            this.runnable = runnable;
            this.condition = condition;
        }

        /**
         * The {@link BukkitTask} that represents the
         * task that is being run.
         *
         * @param task The task.
         */
        final void setTask(CancellableTask task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (this.cancelled) {
                if (this.task != null) {
                    this.task.cancel();
                }

                return;
            }

            if (this.condition.getAsBoolean()) {
                this.runnable.run();
            } else {
                if (this.task != null) {
                    this.task.cancel();
                } else {
                    this.cancelled = true;
                }
            }
        }
    }

    private interface CancellableTask {
        void cancel();
    }
}
