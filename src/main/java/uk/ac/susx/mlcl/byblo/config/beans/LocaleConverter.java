/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.config.beans;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.configuration.ConversionException;

/**
 * <code>LocaleConverter</code> converts from property files value strings to
 * Locale instances.
 * <p/>
 * A locale string is expected to in the format
 * <code>LL[_CC[_VV]]</code>, where
 * <code>LL</code> is a two-letter ISO 639 language code,
 * <code>CC</code> is a two-letter ISO 3166 country code, and
 * <code>VV</code> is an arbitrary variant code. Note the brackets here denote
 * option components. The "
 * <code>_<code>" (underscore) delimiter can be replaced with a "
 * <code>-</code>" (dash) character. Letters are case-insensitive though it is
 * normal for the language code to be given in lower-case, while the country and
 * variant are given in upper-case.
 * <p/>
 * @author hamish
 */
@ThreadSafe
@CheckReturnValue
class LocaleConverter implements Converter {

    private static final boolean DEFAULT_VALIDATION_ENABLED = true;

    private boolean validationEnabled;

    private Set<String> languages = null;

    private Set<String> countries = null;

    private Set<Locale> availableLocales = null;

    private LocaleConverter(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    public LocaleConverter() {
        this(DEFAULT_VALIDATION_ENABLED);
    }

    @Override
    public Object convert(Class type, Object value) {
        try {
            final String localeString = (String) value;
            final Locale locale = parseLocaleString(localeString);
            if (validationEnabled)
                validateLocale(locale);
            return locale;
        } catch (Throwable ex) {
            if (ex instanceof ConversionException)
                throw (ConversionException) ex;
            throw new ConversionException(MessageFormat.format(
                    "Unable to convert object {1} of type {2} to type {0}",
                    type, value, value.getClass()), ex);
        }
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    @Override
    public String toString() {
        return "LocaleConverter{"
                + "validationEnabled=" + validationEnabled
                + '}';
    }

    private static Locale parseLocaleString(String localeString) {
        final int i = indexOfDelim(localeString, 0);
        if (i < 0)
            return new Locale(localeString.toLowerCase(Locale.getDefault()));
        else {
            final String language = localeString.substring(0, i)
                    .toLowerCase(Locale.getDefault());
            final int j = indexOfDelim(localeString, i + 1);
            if (j < 0) {
                String country = localeString.substring(i + 1)
                        .toUpperCase(Locale.getDefault());
                return new Locale(language, country);
            } else {
                final String country = localeString.substring(i + 1, j)
                        .toUpperCase(Locale.getDefault());
                final String variant = localeString.substring(j + 1)
                        .toUpperCase(Locale.getDefault());
                return new Locale(language, country, variant);
            }
        }
    }

    private static int indexOfDelim(String localString, int startIndex) {
        int j = localString.indexOf('-', startIndex);
        if (j < 0)
            j = localString.indexOf('_', startIndex);
        return j;
    }

    private synchronized void initValidationSets() {
        if (languages == null) {
            languages = new HashSet<String>(Locale.getISOLanguages().length);
            for (String language : Locale.getISOLanguages())
                languages.add(language.toLowerCase(Locale.getDefault()));

            countries = new HashSet<String>(Locale.getISOCountries().length);
            for (String country : Locale.getISOCountries())
                countries.add(country.toLowerCase(Locale.getDefault()));

            availableLocales = new HashSet<Locale>(
                    Locale.getAvailableLocales().length);
            availableLocales.addAll(Arrays.asList(Locale.getAvailableLocales()));
        }
    }

    private void validateLocale(Locale loc) throws ConversionException {
        initValidationSets();
        if (!languages.contains(loc.getLanguage().toLowerCase(Locale
                .getDefault())))
            throw new ConversionException(MessageFormat.format(
                    "Unknown language code \"{0}\" in locale \"{1}\"",
                    loc.getLanguage(), loc));
        if (!countries
                .contains(loc.getCountry().toLowerCase(Locale.getDefault())))
            throw new ConversionException(MessageFormat.format(
                    "Unknown country code \"{0}\" in locale \"{1}\"",
                    loc, loc.getCountry()));
        if (!availableLocales.contains(loc))
            throw new ConversionException(MessageFormat.format(
                    "Locale \"{0}\" is not available on this platform", loc));
    }
}
