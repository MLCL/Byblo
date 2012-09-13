/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.config.beans;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.configuration.ConversionException;

/**
 *
 * @author hamish
 */
class CharsetConverter implements Converter {

    @Override
    public Object convert(Class type, Object value) {
        try {
            return Charset.forName((String) value);
        } catch (Throwable ex) {
            throw new ConversionException(MessageFormat
                    .format("Failed to convert object \"{1}\" of "
                    + "type \"{2}\" to type \"{0}\"",
                            type, value, value.getClass()), ex);
        }
    }
}
