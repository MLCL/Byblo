/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.command;

import uk.ac.susx.mlcl.lib.command.CharsetStringConverter;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

/**
 *
 * @author hamish
 */
public class ConverterFactory implements IStringConverterFactory {

    private final Map<Class<?>, Class<? extends IStringConverter<?>>> conv;

    public ConverterFactory() {
        conv = new HashMap<Class<?>, Class<? extends IStringConverter<?>>>();
        conv.put(Charset.class, CharsetStringConverter.class);
        conv.put(TempFileFactory.class, TempFileFactoryConverter.class);
    }

    @SuppressWarnings(value = "unchecked")
    @Override
    public <T> Class<? extends IStringConverter<T>> getConverter(Class<T> forType) {
        return (Class<? extends IStringConverter<T>>) conv.get(forType);
    }
    
}
