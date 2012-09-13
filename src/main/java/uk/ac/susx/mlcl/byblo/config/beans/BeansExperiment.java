/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.config.beans;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.BeanHelper;
import org.apache.commons.configuration.beanutils.DefaultBeanFactory;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author hamish
 */
public class BeansExperiment {

    public static void main(String[] args) {

        try {
            run();
        } catch (Throwable t) {
            printFullStackTrace(t);
        }
    }

    private static void run() throws ConfigurationException, ParserConfigurationException, SAXException {
        ConvertUtils.register(new CharsetConverter(), Charset.class);
        ConvertUtils.register(new LocaleConverter(), Locale.class);


        BeanHelper.setDefaultBeanFactory(new DefaultBeanFactory());

        URL configUrl = ClassLoader.getSystemResource(
                "uk/ac/susx/mlcl/byblo/measures/beans/bybloconfig.xml");
//       
        Preconditions.checkNotNull(configUrl, "configUrl");

        XMLConfiguration config = new XMLConfiguration(configUrl);
//        
//        DocumentBuilderFactory.newInstance();
//        SchemaFactory.newInstance(null).
        config.setDocumentBuilder(createDocumentBuilder());


//        config.registerEntityId("byblo", schemaUrl);
//        config.setValidating(true);
//        config.setSchemaValidation(true);
//        config.setEntityResolver(new EntityResolver() {
//
//            @Override
//            public InputSource resolveEntity(String string, String string1)
//                    throws SAXException, IOException {
//                System.out.println(string + ", " + string1);
//                return null;
//            }
//        });
//        
//      
//        config.load();

        config.validate();

        BeanDeclaration declaration = new PrettyXMLBeanDeclaration(config, "config");
        BybloConfig bybloConfig = (BybloConfig) BeanHelper.createBean(declaration);

        System.out.println(bybloConfig);


    }
//
//    /**
//     * Schema Language key for the parser
//     */
//    private static final String JAXP_SCHEMA_LANGUAGE =
//            "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
//
//    /**
//     * Schema Language for the parser
//     */
//    private static final String W3C_XML_SCHEMA =
//            "http://www.w3.org/2001/XMLSchema";

    private static DocumentBuilder createDocumentBuilder()
            throws ParserConfigurationException, SAXException {

        SchemaFactory schemaFactory =
                SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        File schemaFile = new File("bybloconfig.xsd");
        Schema schema = schemaFactory.newSchema(schemaFile);


        DocumentBuilderFactory documentFactory =
                DocumentBuilderFactory.newInstance();

        documentFactory.setNamespaceAware(true);

        documentFactory.setSchema(schema);

        DocumentBuilder result = documentFactory.newDocumentBuilder();

//        SAXParserFactory.newInstance()

        result.setErrorHandler(new DefaultHandler() {
            @Override
            public void error(SAXParseException ex) throws SAXException {
                throw ex;
            }
        });

        
        return result;

    }

    public static class PrettyXMLBeanDeclaration implements BeanDeclaration {

        /**
         * Constant for the prefix of reserved attributes.
         */
        public static final String RESERVED_PREFIX = "c-";

        /**
         * Constant for the prefix for reserved attributes.
         */
        public static final String ATTR_PREFIX = "[@" + RESERVED_PREFIX;

        /**
         * Constant for the bean class attribute.
         */
        public static final String ATTR_BEAN_CLASS = ATTR_PREFIX + "class]";

        /**
         * Constant for the bean factory attribute.
         */
        public static final String ATTR_BEAN_FACTORY = ATTR_PREFIX + "factory]";

        /**
         * Constant for the bean factory parameter attribute.
         */
        public static final String ATTR_FACTORY_PARAM = ATTR_PREFIX
                + "factoryParam]";

        /**
         * Stores the associated configuration.
         */
        private SubnodeConfiguration configuration;

        /**
         * Stores the configuration node that contains the bean declaration.
         */
        private ConfigurationNode node;

        public PrettyXMLBeanDeclaration(HierarchicalConfiguration config,
                                        @Nullable String key) {
            Preconditions.checkNotNull(config, "config");

            try {
                configuration = config.configurationAt(key);
                node = configuration.getRootNode();
            } catch (IllegalArgumentException iex) {
                // If we reach this block, the key does not have exactly one value
                if (config.getMaxIndex(key) > 0) {
                    throw iex;
                }
                configuration = config.configurationAt(null);
                node = new DefaultConfigurationNode();
            }
            initSubnodeConfiguration(getConfiguration());
        }

        /**
         * Creates a new instance of {@code PrettyXMLBeanDeclaration} and initializes
         * it with the configuration node that contains the bean declaration.
         * <p/>
         * @param config the configuration
         * @param node   the node with the bean declaration.
         */
        public PrettyXMLBeanDeclaration(SubnodeConfiguration config,
                                        ConfigurationNode node) {
            Preconditions.checkNotNull(config, "config");
            Preconditions.checkNotNull(node, "node");

            this.node = node;
            configuration = config;
            initSubnodeConfiguration(config);
        }

        /**
         * Returns the configuration object this bean declaration is based on.
         * <p/>
         * @return the associated configuration
         */
        public SubnodeConfiguration getConfiguration() {
            return configuration;
        }

        /**
         * Returns the node that contains the bean declaration.
         * <p/>
         * @return the configuration node this bean declaration is based on
         */
        public ConfigurationNode getNode() {
            return node;
        }

        /**
         * Returns the name of the bean factory. This information is fetched
         * from the {@code c:factory} attribute.
         * <p/>
         * @return the name of the bean factory
         */
        @Override
        public String getBeanFactoryName() {
            return getConfiguration().getString(ATTR_BEAN_FACTORY);
        }

        /**
         * Returns a parameter for the bean factory. This information is fetched
         * from the {@code c:factoryParam} attribute.
         * <p/>
         * @return the parameter for the bean factory
         */
        @Override
        public Object getBeanFactoryParameter() {
            return getConfiguration().getProperty(ATTR_FACTORY_PARAM);
        }

        /**
         * Returns the name of the class of the bean to be created. This
         * information is obtained from the {@code config-class} attribute.
         * <p/>
         * @return the name of the bean's class
         */
        @Override
        public String getBeanClassName() {
            return getConfiguration().getString(ATTR_BEAN_CLASS);
        }

        /**
         * Returns a map with the bean's (simple) properties. The properties are
         * collected from all attribute nodes, which are not reserved.
         * <p/>
         * @return a map with the bean's properties
         */
        @Override
        public Map<String, Object> getBeanProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            for (ConfigurationNode attr : getNode().getAttributes()) {
                if (!isReservedNode(attr)) {
                    props.put(attr.getName(), interpolate(attr.getValue()));
                }
            }
            for (ConfigurationNode child : getNode().getChildren()) {
                if (!isReservedNode(child) && isSimpleNode(child)) {
                    props.put(child.getName(), interpolate(child.getValue()));
                }
            }

            return props;
        }

        @Override
        public String toString() {
            return "XMLBeanDeclaration{"
                    + "configuration=" + configuration.getSubnodeKey()
                    + ", nodeName=" + node.getName()
                    + ", nodeValue=" + node.getValue()
                    + '}';
        }

        boolean isSimpleNode(ConfigurationNode node) {
            return node.getChildrenCount() == 0 && node.getAttributeCount() == 0;
        }

        /**
         * Returns a map with bean declarations for the complex properties of
         * the bean to be created. These declarations are obtained from the
         * child nodes of this declaration's root node.
         * <p/>
         * @return a map with bean declarations for complex properties
         */
        @Override
        public Map<String, Object> getNestedBeanDeclarations() {
            Map<String, Object> nested = new HashMap<String, Object>();
            for (ConfigurationNode child : getNode().getChildren()) {

                if (isReservedNode(child) || isSimpleNode(child))
                    continue;

                if (nested.containsKey(child.getName())) {

                    Object obj = nested.get(child.getName());
                    List<BeanDeclaration> list;

                    if (obj instanceof List) {

                        // Safe because we created the lists ourselves.
                        list = (List<BeanDeclaration>) obj;

                    } else {

                        list = new ArrayList<BeanDeclaration>();
                        list.add((BeanDeclaration) obj);
                        nested.put(child.getName(), list);

                    }

                    list.add(createBeanDeclaration(child));

                } else {
                    nested.put(
                            child.getName(),
                            createBeanDeclaration(child));
                }

            }

            return nested;
        }

        /**
         * Performs interpolation for the specified value. This implementation
         * will interpolate against the current sub-node configuration's parent.
         * If sub classes need a different interpolation mechanism, they should
         * override this method.
         * <p/>
         * @param value the value that is to be interpolated
         * @return the interpolated value
         */
        Object interpolate(Object value) {

            return PropertyConverter.interpolate(
                    value, getConfiguration().getParent());
        }

        boolean isReservedNode(ConfigurationNode nd) {
            return nd.getName().startsWith(RESERVED_PREFIX);
        }

        /**
         * Creates a new {@code BeanDeclaration} for a child node of the current
         * configuration node. This method is called by
         * {@code getNestedBeanDeclarations()} for all complex sub properties
         * detected by this method. Derived classes can hook in if they need a
         * specific initialization. This base implementation creates a
         * {@code PrettyXMLBeanDeclaration} that is properly initialized from the
         * passed in node.
         * <p/>
         * @param node the child node, for which a {@code BeanDeclaration} is to
         *             be created
         * @return the {@code BeanDeclaration} for this child node
         * @since 1.6
         */
        BeanDeclaration createBeanDeclaration(ConfigurationNode node) {
            List<? extends Configuration> list =
                    getConfiguration().configurationsAt(node.getName());

            if (list.size() == 1) {

                return new PrettyXMLBeanDeclaration(
                        (SubnodeConfiguration) list.get(0), node);

            } else {

                for (Configuration aList : list) {
                    SubnodeConfiguration config = (SubnodeConfiguration) aList;
                    if (config.getRootNode().equals(node)) {
                        return new PrettyXMLBeanDeclaration(config, node);
                    }
                }

                throw new ConfigurationRuntimeException(
                        "Unable to match node for " + node.getName());
            }
        }

        private void initSubnodeConfiguration(SubnodeConfiguration config) {
            config.setThrowExceptionOnMissing(false);
            config.setExpressionEngine(null);
        }
    }

    private static void printFullStackTrace(Throwable t) {
        try {
            StringBuilder sb = new StringBuilder();
            fullStackTrace(t, sb);
            System.out.println(sb);
        } catch (IOException ex) {
            // Not possible because we are using a StringBuilder Appendable
            throw new AssertionError(ex);
        }
    }

    private static void fullStackTrace(Throwable t, Appendable sb) throws IOException {
        sb.append(t.toString());
        sb.append('\n');
        for (StackTraceElement e : t.getStackTrace()) {
            sb.append("\tat ");
            sb.append(e.toString());
            sb.append('\n');
        }
        if (t.getCause() != null) {
            sb.append("Caused by: ");
            fullStackTrace(t.getCause(), sb);
        }

    }
}
