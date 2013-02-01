/*
 * Copyright (c) 2010-2013, University of Sussex
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
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import static com.google.common.base.Preconditions.*;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * EnumConverter is a JCommander class for parsing command line argument strings into an EnumSet of
 * some element type.
 * <p/>
 * Sadly JCommander is only parameterizable with a class that implements IStringConverter, rather
 * than an instance object, which means you have to subclass this convert for every single possible
 * element type (i.e every enum you want to use.)
 * <p/>
 * Matching is case-insensitive.
 *
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <E> element type
 */
@Nonnull
@Immutable
public abstract class EnumSetConverter<E extends Enum<E>>
        extends BaseConverter<EnumSet<E>> {

    private static final String DEFAULT_OPTION_NAME = "";
    private static final Pattern DELIMITER = Pattern.compile(",");
    private final Class<E> elementType;

    protected EnumSetConverter(final Class<E> elementType, final String optionName) {
        super(optionName);
        this.elementType = checkNotNull(elementType);
    }

    protected EnumSetConverter(final Class<E> elementType) {
        super(DEFAULT_OPTION_NAME);
        this.elementType = checkNotNull(elementType);
    }

    public final Class<E> getElementType() {
        return elementType;
    }

    @Override
    public EnumSet<E> convert(final String stringValue) {
        checkNotNull(stringValue);
        final EnumSet<E> resultSet = EnumSet.noneOf(elementType);
        for (String name : DELIMITER.split(stringValue)) {
            try {
                name = name.trim().toLowerCase();
                final E value = Enum.valueOf(elementType, name);
                resultSet.add(value);
            } catch (IllegalArgumentException ex) {
                throw new ParameterException(MessageFormat.format(
                        "Failed to convert option {0} from \"{1}\" to EnumSet<{2}>: "
                        + "Found unknown name \"{3}\", but expecting one of {4}",
                        new Object[]{getOptionName(), stringValue, elementType.getSimpleName(),
                            name, EnumSet.allOf(elementType)}));
            }
        }
        return resultSet;
    }

    @Override
    public String toString() {
        return "EnumSetConverter{elementType=" + elementType + '}';
    }
}
