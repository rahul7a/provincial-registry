package com.lblw.vphx.phms.common.databind;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An @XPathTarget.Binding target for binding a nested context
 *
 * <pre>{@code
 * @XPathTarget
 * void bindMedicine(
 *      @XPathTarget.Binding(xPath="medicine") BindOne<Medicine> binder
 * ) {
 *      ...
 * }
 * }</pre>
 *
 * Example binds XML 'medicine' to supplied Medicine instance factory
 */
public interface BindOne<T> extends Function<Supplier<T>, T> {}
