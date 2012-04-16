/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Files;

/**
 *
 * @author hiam20
 */
public class FileDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-c", "--charset"},
               description = "The character set encoding to use for both reading input and writing output files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @Parameter(names = {"--disable-compact-format"}, hidden = true)
    private boolean compactFormatDisabled = false;

    public FileDeligate(Charset charset) {
        setCharset(charset);
    }

    public FileDeligate() {
    }

    /**
     * @return 
     * @deprecated TODO: Replace with isCompactFormatEnabled()
     */
    @Deprecated
    public boolean isCompactFormatDisabled() {
        return compactFormatDisabled;
    }

    /**
     * @param compactFormatDisabled 
     * @deprecated TODO: Replace with setCompactFormatEnabled(boolean)
     */
    @Deprecated
    public void setCompactFormatDisabled(boolean compactFormatDisabled) {
        this.compactFormatDisabled = compactFormatDisabled;
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("charset", getCharset()).
                add("compact", !isCompactFormatDisabled());
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }
}
