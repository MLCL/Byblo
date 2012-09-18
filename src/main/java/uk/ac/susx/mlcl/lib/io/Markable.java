package uk.ac.susx.mlcl.lib.io;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import java.io.IOException;

/**
 * The <code>Markable</code> interface is implemented by resources that <em>may</em> have some (limited) support for
 * random access. Such objects allow for the current position to be stored internally (by calling the {@link #mark(int)}
 * method, and then return later to that position using the {@link #reset()} method. <p/>
 *
 * Note that just because a class implements the <code>Markable</code> interface, it doesn't guarantee the
 * <code>mark</code> will work as intended. For example an implementation might delegate to an underlying resource
 * that may or may not support marking. When handling a <code>Markable</code> resource the functionality should always
 * be checked by calling {@link #markSupported()}. <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Markable {

    /**
     * Marks the current position in this resource. A subsequent call to the <code>reset</code> method repositions this
     * resource at the last marked position so that subsequent reads re-read the same data. <p/> The
     * <code>readLimit</code> argument tells this resource to allow that many elements to be read before the mark
     * position gets invalidated. <p/> The element type (and consequently size) is implementer specific, but it should
     * be the smallest possible atomic unit supported by the resource. For example: In a binary data based resource the
     * <code>readLimit</code> will be measured in bytes.
     *
     * @param readLimit the maximum limit of bytes that can be read before the mark position becomes invalid.
     * @see #reset()
     * @see #markSupported()
     */
    void mark(@Nonnegative int readLimit);

    /**
     * Repositions this resource to the position at the time the <code>mark</code> method was last called. <p/>
     *
     * @throws IOException if the stream has not been marked or if the mark has been invalidated.
     * @see #mark(int)
     * @see #markSupported()
     */
    void reset() throws IOException;

    /**
     * Tests if this resource supports the <code>mark</code> and <code>reset</code> methods. <p/>
     *
     * @return true if this stream type supports the mark and reset method; false otherwise.
     * @see #mark(int)
     * @see #reset()
     */
    @CheckReturnValue
    boolean markSupported();
}
