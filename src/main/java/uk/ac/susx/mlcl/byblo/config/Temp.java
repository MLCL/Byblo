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
package uk.ac.susx.mlcl.byblo.config;

import java.net.URL;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ConfigurationNodeVisitorAdapter;
import uk.ac.susx.mlcl.byblo.FullBuild;
import uk.ac.susx.mlcl.byblo.config.impl.BybloConfigCommonsImpl;

/**
 *
 * @author hamish
 */
public class Temp {

    public static void main(String[] args) throws ConfigurationException {
//
//        Configuration configuration =
//                new PropertiesConfiguration("config.properties");
//        BybloConfig appConfig =
//                ConfigurationInterface.getInstance(configuration,
//                                                   BybloConfig.class);

        


        URL resUrl = FullBuild.class.getResource("defaults.xml");
        
        
        
        
//        URL resUrl = Temp.class.getResource("defaults.xml");

        XMLConfiguration config = new XMLConfiguration(resUrl);
        
        
//        config.setSchemaValidation(true);
//        config.setValidating(true);
//        config.validate();

        config.load();
        config.getRootNode().visit(new ConfigNodeDumper());
        
        
        BybloConfig bc = new BybloConfigCommonsImpl(config);
        
        System.out.println(bc.getCharset());
        System.out.println(bc.getLocale());
        
        
    }

    private static class ConfigNodeDumper
            extends ConfigurationNodeVisitorAdapter {

        private final StringBuilder sb = new StringBuilder();

        private int depth = 0;

        @Override
        public void visitBeforeChildren(ConfigurationNode node) {
            for (int i = 0; i < depth; i++)
                sb.append("   ");
            if (node.isAttribute())
                sb.append('@');
            sb.append(node.getName());
            if (node.isDefined() && node.getValue() != null) {
                sb.append(" = ");
                sb.append(node.getValue());
                sb.append(" (");
                sb.append(node.getValue().getClass());
                sb.append(")");
            }
            sb.append('\n');
            depth++;
        }

        @Override
        public void visitAfterChildren(ConfigurationNode node) {
            depth--;
            if (depth == 0)
                System.out.println(sb);
        }
    }
}
