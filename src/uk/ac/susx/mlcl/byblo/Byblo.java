/*
 * Copyright (c) 2010-2011, University of Sussex
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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Byblo {

    private static final Logger LOG = Logger.getLogger(CopyTask.class.getName());

    @Parameter(names = {"-h", "--help"},
               description = "Display this message")
    private boolean usageRequested = false;

    public boolean isUsageRequested() {
        return usageRequested;
    }

    enum Command {

        sort(ExternalSortTask.class),
        merge(MergeTask.class),
        knn(ExternalKnnTask.class),
        allpairs(AllPairsTask.class),
        count(ExternalCountTask.class),
        filter(FilterTask.class);

        private Class<? extends Task> taskClass;

        private Command(Class<? extends Task> taskClass) {
            this.taskClass = taskClass;
        }

        public Class<? extends Task> getTaskClass() {
            return taskClass;
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, Exception {

        if (args == null)
            throw new NullPointerException();

        Byblo byblo = new Byblo();

        JCommander jc = new JCommander();
        jc.setProgramName("byblo");
        jc.addConverterFactory(new ConverterFactory());

        jc.addObject(byblo);
        jc.setDescriptionsBundle(
                ResourceBundle.getBundle("uk.ac.susx.mlcl.byblo.strings"));

        EnumMap<Command, Task> tasks =
                new EnumMap<Command, Task>(Command.class);

        for (Command c : Command.values()) {
            Task t = c.getTaskClass().newInstance();
            jc.addCommand(c.name(), t);
            tasks.put(c, t);
        }

        try {

            jc.parse(args);
            if (byblo.isUsageRequested() || jc.getParsedCommand() == null) {

                jc.usage();

                if (jc.getParsedCommand() != null) {
                    jc.usage(jc.getParsedCommand());
                }

            } else {

                Task t = tasks.get(Command.valueOf(jc.getParsedCommand()));
                t.run();

                while (t.isExceptionThrown()) {
                    t.throwException();
                }
            }

        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            if (jc.getParsedCommand() != null) {
                jc.usage(jc.getParsedCommand(), sb);
            }
            System.err.println(sb);
            System.exit(-1);
        }
    }

    public static class ConverterFactory implements IStringConverterFactory {

        private final Map<Class<?>, Class<? extends IStringConverter<?>>> conv;

        public ConverterFactory() {
            conv = new HashMap<Class<?>, Class<? extends IStringConverter<?>>>();
            conv.put(Charset.class, CharsetConverter.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Class<? extends IStringConverter<T>> getConverter(
                Class<T> forType) {
            return (Class<? extends IStringConverter<T>>) conv.get(forType);
        }
    }

    public static class CharsetConverter implements IStringConverter<Charset> {

        @Override
        public Charset convert(String string) {
            return Charset.forName(string);
        }
    }
}
