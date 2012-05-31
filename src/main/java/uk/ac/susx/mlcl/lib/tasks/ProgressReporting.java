/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

/**
 * Defines a common interface for tasks which can report their progress as a
 * percentage completed.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface ProgressReporting {

    enum State {

        PENDING,
        RUNNING,
        COMPLETED,
        ERROR

    }

    String getName();

    /**
     * Accessor to the operations progress towards completion as a percentage.
     *
     * @return percentage completed as an integer in the interval [0,100]
     * @throws UnsupportedOperationException when unsupported
     */
    int getProgressPercent();

    /**
     * Whether or not the ProgressReporting implementation supports progress
     * reporting as a percentage towards completion.
     *
     * Not all ProgressReporting tasks can estimate the total progress towards
     * completion, for example because the task operates on a data stream of
     * unknown length. In this case the implementation should return false.
     *
     * @return true if progress percentage, false otherwise.
     */
    boolean isProgressPercentageSupported();

    /**
     * Accessor to whether or not operation has started. By definition when this {@link #isStarted()
     * } = <tt>true</tt>, {@link #getProgressPercent()
     * } > 0.
     *
     * @return whether the operation has started
     */
    boolean isStarted();

    /**
     * Accessor to whether or not operation has completed. By definition when
     * this {@link #isCompleted() } = <tt>true</tt>, {@link #getProgressPercent()
     * } = 100.
     *
     * @return whether the operation has completed
     */
    boolean isCompleted();

    /**
     *
     * @return a human-readable string representing the current progress
     */
    String getProgressReport();

    /**
     * Attach a ProgressListener that will be notified when the progress changes
     * (presumably when it increases).
     *
     * If the given progress listener is already attached then nothing will
     * change.
     *
     * @param progressListener listener to be notified of progress changes
     */
    void addProgressListener(ProgressListener progressListener);

    /**
     * Detach a ProgressListener from receiving progress change events.
     *
     * If the listener is not already attached then nothing happens.
     *
     * @param progressListener listener to be removed
     */
    void removeProgressListener(ProgressListener progressListener);

    /**
     * @return array of currently attached listeners.
     */
    ProgressListener[] getProgressListeners();

}
