/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import java.util.regex.Pattern;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class DoubleConverter implements com.beust.jcommander.IStringConverter<Double> {

    private static final Pattern infinityRegex = Pattern.compile(
            "^[\\+\\-]?[Ii][Nn][Ff]([Ii]([Nn]([Ii]([Tt][Yy]?)?)?)?)?$");

    @Override
    public Double convert(String value) {
        if (infinityRegex.matcher(value).matches())
            return value.charAt(0) == '-'
                    ? Double.NEGATIVE_INFINITY
                    : Double.POSITIVE_INFINITY;
        return Double.valueOf(value);
    }
}
