package com.lblw.vphx.phms.common.databind;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/** Implementation for an xPath to object binder. */
@Slf4j
public class XPathBinder implements IXPathBinder {
  /**
   * A memoization map. It is used to cache the list of {@link XPathTarget} annotated methods for a
   * given class.
   */
  private static final Map<Class<?>, List<Method>> classAnnotatedMethodsMemo =
      new ConcurrentHashMap<>();

  /**
   * A memoization map. It is used to cache the list of {@link XPathTarget.Binding} annotated
   * Parameters of a {@link XPathTarget} annotated method for a given class.
   */
  private static final Map<Method, List<Pair<XPathTarget.Binding, Parameter>>> methodBindingsMemo =
      new ConcurrentHashMap<>();

  /**
   * Scheduler to be used by all {@link XPathTarget} annotated async methods with Mono return type.
   */
  private static final Scheduler ioBoundScheduler = Schedulers.boundedElastic();

  /** List of all {@link XPathTarget} annotated async methods Mono returns to be parallelized. */
  private final List<Mono<Void>> asyncSubTasks = new LinkedList<>();

  /** The root XML context to read and bind from. */
  private final Element xml;

  /**
   * Constructs a new XPathBinder instance using the provided XML element.
   *
   * @param xml the root XML context to read and bound from
   */
  public XPathBinder(Element xml) {
    this.xml = xml;
  }

  private static String getPrintableName(Class<?> clazz) {
    return clazz.getSimpleName();
  }

  private static String getPrintableName(Method method) {
    return method.getName();
  }

  private static String getPrintableName(Object object) {
    return getPrintableName(object.getClass());
  }

  /** {@inheritDoc} */
  @Override
  public void bind(Object instance) {
    try {
      new BindInstanceTask(instance, new XPathEvaluator(xml)).run();
      Mono.when(asyncSubTasks).subscribeOn(ioBoundScheduler).toFuture().join();
    } catch (XPathBinderException exception) {
      log.error(exception.getMessage(), exception);
      throw exception;
    } catch (Exception exception) {
      var wrappedException =
          new XPathBinderException(
              String.format(
                  XPathBinderConstants.MESSAGE_EXCEPTION_BINDING_INSTANCE,
                  getPrintableName(instance)),
              exception);
      log.error(wrappedException.getMessage(), wrappedException);
      throw wrappedException;
    } finally {
      asyncSubTasks.clear();
    }
  }

  /**
   * XPathBinder internal Task to process target instance. Uses {@link BindMethodSubTask} for
   * individual methods
   */
  private class BindInstanceTask implements Runnable {
    private final Object instance;
    private final XPathEvaluator xPathEvaluator;
    private final List<Method> annotatedMethods;

    private BindInstanceTask(Object instance, XPathEvaluator xPathEvaluator) {
      this.xPathEvaluator = xPathEvaluator;
      this.instance = instance;
      this.annotatedMethods =
          classAnnotatedMethodsMemo.computeIfAbsent(
              instance.getClass(),
              cls ->
                  Arrays.stream(cls.getDeclaredMethods())
                      .filter(
                          method ->
                              Modifier.isPublic(method.getModifiers())
                                  && method.isAnnotationPresent(XPathTarget.class))
                      .collect(Collectors.toList()));
    }

    @Override
    public void run() {
      annotatedMethods.forEach(
          method -> {
            /* if method return type is Mono, list and run BindMethodSubTask in parallel IO Scheduler */
            if (Mono.class.isAssignableFrom(method.getReturnType())) {
              var asyncSubTask = new BindMethodSubTask<Mono<?>>(instance, method, xPathEvaluator);
              asyncSubTasks.add(asyncSubTask.get().subscribeOn(Schedulers.boundedElastic()).then());
              return;
            }

            /* else invoke BindMethodSubTask immediately */
            new BindMethodSubTask<Object>(instance, method, xPathEvaluator).get();
          });
    }
  }

  /**
   * {@link XPathBinder} internal SubTask to process methods of a target instance. Used by {@link
   * BindInstanceTask} for processing the instance itself
   */
  private class BindMethodSubTask<T> implements Supplier<T> {
    private final Object instance;
    private final Method method;
    private final XPathEvaluator xPathEvaluator;

    public BindMethodSubTask(Object instance, Method method, XPathEvaluator xPathEvaluator) {
      this.instance = instance;
      this.method = method;
      this.xPathEvaluator = xPathEvaluator;
    }

    @Override
    public T get() {
      return executeAnnotatedMethod(method);
    }

    private T executeAnnotatedMethod(Method method) {
      var parameterBindings =
          methodBindingsMemo.computeIfAbsent(
              method,
              targetMethod ->
                  Arrays.stream(targetMethod.getParameters())
                      .map(
                          parameter ->
                              Pair.of(
                                  parameter.getAnnotation(XPathTarget.Binding.class), parameter))
                      .collect(Collectors.toList()));

      Object[] arguments =
          parameterBindings.stream()
              .map(
                  parameterBinding -> {
                    var binding = parameterBinding.getLeft();
                    var parameter = parameterBinding.getRight();

                    if (binding == null) {
                      return null;
                    }

                    var xPath = binding.xPath();

                    if (List.class.isAssignableFrom(parameter.getType())) {
                      return withExceptionMessagingBuildArgument(
                          parameter, xPath, this::buildListArgument);
                    }

                    if (BindMany.class.isAssignableFrom(parameter.getType())) {
                      return withExceptionMessagingBuildArgument(
                          parameter, xPath, this::buildBindManyArgument);
                    }

                    if (String.class.isAssignableFrom(parameter.getType())) {
                      return withExceptionMessagingBuildArgument(
                          parameter, xPath, this::buildStringArgument);
                    }

                    if (BindOne.class.isAssignableFrom(parameter.getType())) {
                      return withExceptionMessagingBuildArgument(
                          parameter, xPath, this::buildBindOneArgument);
                    }

                    throw new XPathBinderException(
                        new IllegalArgumentException(
                            String.format(
                                XPathBinderConstants
                                    .MESSAGE_UNEXPECTED_PARAMETER_TYPE_FOR_METHOD_OF_INSTANCE,
                                getPrintableName(parameter.getType()),
                                getPrintableName(method),
                                getPrintableName(instance))));
                  })
              .toArray();

      try {
        return (T) method.invoke(instance, arguments);
      } catch (XPathBinderException exception) {
        throw exception;
      } catch (Exception e) {
        throw new XPathBinderException(
            String.format(
                XPathBinderConstants.MESSAGE_EXCEPTION_BINDING_METHOD_OF_INSTANCE_WITH_ARGUMENTS,
                getPrintableName(method),
                getPrintableName(instance),
                Arrays.stream(Objects.requireNonNullElse(arguments, new Object[0]))
                    .map(XPathBinder::getPrintableName)
                    .collect(Collectors.toList())),
            e);
      }
    }

    /**
     * Builds argument for parameters annotated with {@code XPathTarget.Binding(...) String}
     *
     * @param xPath The xPath expression to evaluate.
     * @return built argument.
     */
    private String buildStringArgument(String xPath) {
      return xPathEvaluator.evaluate(xPath, String.class);
    }

    /**
     * Builds argument for parameters annotated with {@code XPathTarget.Binding(...) List<String>}
     *
     * @param xPath The xPath expression to evaluate.
     * @return built argument.
     */
    private List<?> buildListArgument(String xPath) {
      return xPathEvaluator.evaluate(xPath, List.class);
    }

    /**
     * Builds argument for parameters annotated with {@code XPathTarget.Binding(...)
     * BindOne<InstanceType>}
     *
     * @param xPath The xPath expression to evaluate.
     * @return built argument.
     */
    private BindOne<?> buildBindOneArgument(String xPath) {
      var evaluation = xPathEvaluator.evaluate(xPath, Element.class);

      if (evaluation == null) {
        return null;
      }

      return instanceSupplier -> {
        var suppliedInstance = instanceSupplier.get();
        new BindInstanceTask(suppliedInstance, new XPathEvaluator(evaluation)).run();
        return suppliedInstance;
      };
    }

    /**
     * Builds argument for parameters annotated with {@code XPathTarget.Binding(...)
     * BindMany<InstanceType>}
     *
     * @param xPath The xPath expression to evaluate.
     * @return built argument.
     */
    private BindMany<?> buildBindManyArgument(String xPath) {
      var evaluation = xPathEvaluator.evaluate(xPath, List.class);

      if (evaluation == null) {
        return null;
      }

      return instanceSupplier ->
          evaluation.stream()
              .map(
                  arg -> {
                    var suppliedInstance = instanceSupplier.get();
                    new BindInstanceTask(suppliedInstance, new XPathEvaluator((Element) arg)).run();
                    return suppliedInstance;
                  });
    }

    private <R> R withExceptionMessagingBuildArgument(
        Parameter parameter, String xPath, Function<String, R> operation) {
      try {
        return operation.apply(xPath);
      } catch (ClassCastException exception) {
        throw new XPathBinderException(
            new IllegalArgumentException(
                String.format(
                    XPathBinderConstants
                        .MESSAGE_UNEXPECTED_PARAMETER_TYPE_FOR_XPATH_ON_METHOD_OF_INSTANCE,
                    getPrintableName(parameter.getType()),
                    xPath,
                    getPrintableName(method),
                    getPrintableName(instance))));
      }
    }
  }
}
