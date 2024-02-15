package com.lblw.vphx.phms.common.databind;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An @XPathTarget.Binding target for binding a nested context list
 *
 * <pre>{@code
 * @XPathTarget
 * void bindIngredients(
 *      @XPathTarget.Binding(xPath="medicine/ingredient[x]") BindMany<Ingredient> binder
 * ) {
 *      ...
 * }
 * }</pre>
 *
 * Example binds multiple XML medicine/ingredient(s) to supplied Ingredient instance factory
 */
public interface BindMany<T> extends Function<Supplier<T>, Stream<T>> {}
