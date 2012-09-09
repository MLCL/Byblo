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
package uk.ac.susx.mlcl.byblo;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author hiam20
 */
public class BybloSettings {

    private final ResourceBundle props;

    private static final Locale locale = Locale.getDefault();

    private BybloSettings(ResourceBundle props) {
        this.props = props;
    }

    private BybloSettings() {
        this(ResourceBundle.getBundle(
                BybloSettings.class.getPackage().getName() + ".settings"));
    }

    public static Locale getLocale() {
        return locale;
    }

    public static BybloSettings getInstance() {
        return InstanceHolder.instance;
    }

    private static final class InstanceHolder {

        private InstanceHolder() {
        }

        private static final BybloSettings instance = new BybloSettings();

    }

    public boolean isInstancesSkipIndexColumn1Enabled() {
        return getBoolean("io.instances.skipIndexColumn1");
    }

    public boolean isInstancesSkipIndexColumn2Enabled() {
        return getBoolean("io.instances.skipIndexColumn2");
    }

    public boolean isInstancesCompactEnabled() {
        return getBoolean("io.instances.compact");
    }

    public boolean isEntriesSkipIndexColumn1Enabled() {
        return getBoolean("io.entries.skipIndexColumn1");
    }

    public boolean isFeaturesSkipIndexColumn1Enabled() {
        return getBoolean("io.features.skipIndexColumn1");
    }

    public boolean isEventsSkipIndexColumn1Enabled() {
        return getBoolean("io.events.skipIndexColumn1");
    }

    public boolean isEventsSkipIndexColumn2Enabled() {
        return getBoolean("io.events.skipIndexColumn2");
    }

    public boolean isEventsCompactEnabled() {
        return getBoolean("io.events.compact");
    }

    public boolean isSimsSkipIndexColumn1Enabled() {
        return getBoolean("io.sims.skipIndexColumn1");
    }

    public boolean isSimsSkipIndexColumn2Enabled() {
        return getBoolean("io.sims.skipIndexColumn2");
    }

    public boolean isSimsCompactEnabled() {
        return getBoolean("io.sims.compact");
    }

    public boolean isNeighboursSkipIndexColumn1Enabled() {
        return getBoolean("io.neighbours.skipIndexColumn1");
    }

    public boolean isNeighboursSkipIndexColumn2Enabled() {
        return getBoolean("io.neighbours.skipIndexColumn2");
    }

    public boolean isNeighboursCompactEnabled() {
        return getBoolean("io.neighbours.compact");
    }

    private boolean getBoolean(String key) {
        return Boolean.valueOf(props.getString(key));
    }
}
