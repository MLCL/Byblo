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

import uk.ac.susx.mlcl.byblo.commands.ExternalCountCommand;
import uk.ac.susx.mlcl.byblo.commands.ExternalSortCommand;
import uk.ac.susx.mlcl.byblo.commands.AbstractMergeCommand;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import uk.ac.susx.mlcl.byblo.commands.MergeTokenPairCommand;
import uk.ac.susx.mlcl.byblo.commands.MergeWeightedTokenCommand;
import uk.ac.susx.mlcl.byblo.commands.MergeWeightedTokenPairCommand;
import uk.ac.susx.mlcl.lib.command.AbstractCommand;
import uk.ac.susx.mlcl.lib.command.Command;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Main extends AbstractCommand {

    private static final Map<String, Class<? extends Command>> SUBCOMMANDS;

    static {
        final Map<String, Class<? extends Command>> tmp =
            new HashMap<String, Class<? extends Command>>();
        tmp.put("sort-sims", ExternalSortCommand.WeightedTokenPiarExternalSortCommand.class);
        tmp.put("sort-ents", ExternalSortCommand.WeightedTokenExternalSortTask.class);
        tmp.put("sort-feats", ExternalSortCommand.WeightedTokenExternalSortTask.class);
        tmp.put("sort-events", ExternalSortCommand.WeightedTokenPiarExternalSortCommand.class);
        tmp.put("merge-sims", MergeTokenPairCommand.class);
        tmp.put("merge-ents", MergeWeightedTokenPairCommand.class);
        tmp.put("merge-feats", MergeWeightedTokenCommand.class);
        tmp.put("merge-events", MergeWeightedTokenCommand.class);
        tmp.put("knn-sims", SimsKnnCommand.class);
        tmp.put("allpairs", AllPairsCommand.class);
        tmp.put("count", ExternalCountCommand.class);
        tmp.put("filter", FilterTask.class);
        SUBCOMMANDS = Collections.unmodifiableMap(tmp);
    }

    public Main() {
        super(SUBCOMMANDS);
    }

    @Override
    public void runCommand() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main(final String[] args) throws Exception {
        new Main().runCommand(args);
    }
}
