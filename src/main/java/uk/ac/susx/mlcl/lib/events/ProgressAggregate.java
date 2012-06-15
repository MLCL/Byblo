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
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.lib.events;

import java.util.concurrent.CopyOnWriteArrayList;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Implementation of ProgressReporting that reports progress based on the totals
 * of sum number of child ProgressReporting objects.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ProgressAggregate extends ProgressDeligate {

    private static final long serialVersionUID = 1L;

    /**
     * Thread safe list of child progress reporters.
     */
    private CopyOnWriteArrayList<ProgressReporting> children =
            new CopyOnWriteArrayList<ProgressReporting>();

    /**
     * Once a child has completed it will be removed from the listener list to
     * save memory, and this count of completed children is incremented.
     */
    private int completedChildren = 0;

    /**
     * Listener that will be attached to every child progress reporter.
     */
    private final ProgressListener childProgressListener =
            new ProgressListener() {

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    Checks.checkNotNull(progressEvent);
                    assert children.contains(progressEvent.getSource());
                    startAdjusting();
                    updateProgress();
                    setStateChangedSinceLastEvent();
                    endAdjusting();
                }

            };

    public ProgressAggregate(ProgressReporting outer) {
        super(outer, true);
    }

    @Override
    public String getName() {
        if (getOuter() == this || getOuter() == null)
            return "<unamed aggregate>";
        else
            return getOuter().getName();
    }

    /**
     * Calculate the average progress across all children. Uses integer
     * arithmetic to insure that 100% progress must be reached when (and only
     * when) all children are at 100%.
     *
     * If there are no children, then progress is always 0%.
     */
    protected void updateProgress() {
        startAdjusting();

        if (!hasChildProgressReporters())
            setProgressPercent(0);

        int childProgressSum = 0;
        boolean anyChildRunning = false;
        boolean allChildrenCompleted = true;
        for (ProgressReporting child : children) {
            // Add the progress percentage for the child, if it supported.
            // Otherwise just assume 0, 50, or 100 for pending, running, and
            // completed respectively.
            if (child.getState() == State.COMPLETED) {
                child.removeProgressListener(childProgressListener);
                children.remove(child);
                ++completedChildren;
            } else if (child.isProgressPercentageSupported()) {
                childProgressSum += child.getProgressPercent();
            }  // otherwise progress is 0

            anyChildRunning = anyChildRunning || child.getState() == State.RUNNING;
            allChildrenCompleted = allChildrenCompleted && child.getState() == State.COMPLETED;
        }

        // The aggregate is considered running if when any child is running
        if (anyChildRunning && super.getState() != State.RUNNING) {
            super.setState(State.RUNNING);
        } else if (allChildrenCompleted && super.getState() != State.COMPLETED) {
            super.setState(State.COMPLETED);
        }

        childProgressSum += 100 * completedChildren;
        int childrenCount = children.size() + completedChildren;
        super.setProgressPercent(childProgressSum / childrenCount);

        endAdjusting();
    }

    /**
     *
     * @return string representation of the progress
     */
    @Override
    public String getProgressReport() {
        return getDeepProgressReport(0);
    }

    public String getDeepProgressReport() {
        return getDeepProgressReport(0);
    }

    private String getDeepProgressReport(int depth) {
        assert depth >= 0;
        StringBuilder sb = new StringBuilder();
        sb.append(super.getProgressReport());
        for (ProgressReporting child : children) {
            if (child.getState() == State.RUNNING) {
                sb.append('\n');
                for (int i = 0; i < depth + 1; i++)
                    sb.append('\t');
                if (child instanceof ProgressAggregate)
                    sb.append(((ProgressAggregate) child).getDeepProgressReport(depth + 1));
                else {
                    sb.append(child.getProgressReport());
                }
            }

        }
        return sb.toString();
    }

    @Override
    protected void fireProgressChangedEvent() {
        int completed = 0;
        for (ProgressReporting child : children) {
            if (child.getState() == State.COMPLETED) {
                ++completed;
            }
        }
        super.fireProgressChangedEvent();
    }

    @Override
    public void setProgressPercent(int progressPercent) {
        throw new UnsupportedOperationException(
                "ProgressReporterAgregate inherits progress from it's children.");
    }

    public void addChildProgressReporter(ProgressReporting child) {
        Checks.checkNotNull(child);
        children.add(child);
        child.addProgressListener(childProgressListener);
    }

    public void removeChildProgressReporter(ProgressReporting child) {
        Checks.checkNotNull(child);
        children.remove(child);
        child.removeProgressListener(childProgressListener);
    }

    public ProgressReporting[] getChildProgressReporters() {
        return children.toArray(new ProgressReporting[0]);
    }

    public boolean hasChildProgressReporters() {
        return !children.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ProgressAggregate other = (ProgressAggregate) obj;
        if (this.children != other.children && (this.children == null || !this.children.equals(other.children)))
            return false;
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 29 * hash + (this.children != null ? this.children.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ProgressAggregate{" + "children=" + children + '}';
    }

}
