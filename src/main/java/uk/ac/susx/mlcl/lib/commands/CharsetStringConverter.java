/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.IStringConverter;
import java.nio.charset.Charset;

/**
 *
 * @author hamish
 */
public class CharsetStringConverter implements IStringConverter<Charset> {

    @Override
    public Charset convert(String string) {
        return Charset.forName(string);
    }
    
}
