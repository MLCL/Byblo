/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.config.beans;

import com.google.common.base.Preconditions;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import uk.ac.susx.mlcl.byblo.measures.Measure;

/**
 *
 * @author hamish
 */
public class BybloConfig {

    private Charset charset = Charset.defaultCharset();

    private Locale locale = Locale.getDefault();

    private Measure measure;

    private ArrayList<MeasureInfo> measures;

    public BybloConfig() {
    }

     
    public ArrayList<MeasureInfo> getMeasures() {
        return measures;
    }

    public void setMeasures(ArrayList<MeasureInfo> measures) {
        this.measures = measures;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        Preconditions.checkNotNull(charset, "charset");
        this.charset = charset;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        Preconditions.checkNotNull(locale, "locale");
        this.locale = locale;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "charset=" + charset
                + ", locale=" + locale
                + "measure=" + measure
                + ", measures=" + measures
                + '}';
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
    }
}
