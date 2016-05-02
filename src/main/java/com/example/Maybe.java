package com.example;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Maybe&lt;A&gt; wraps a value of type A</p>
 *
 * @author Bob Carberry
 */
public abstract class Maybe<A> {

  /**
   * <p>Returns a Maybe&lt;A&gt; that is an instance
   * of Nothing</p>
   * @param <T> the type of the Maybe expression
   * @return an instance of Nothing as a Maybe&lt;A&gt;
   */
  public static <T> Maybe<T> nothing() { return (Maybe<T>) NOTHING; }

  /**
   * <p>Wraps any non null argument as an instance of Just&lt;A&gt;
   * and any null argument as an instance of Nothing.</p>
   * @param value  the value to wrap as a Maybe&lt;A&gt;
   * @param <T>    the type of the underlying value
   * @return       an instance of Maybe&lt;A&gt;
   */
  public static <T> Maybe<T> apply(T value) {
    if (value == null)
      return nothing();
    else
      return just(value);
  }

  /*
   * A private method for getting the underlying value
   * of a Just. Should only be used when this is guaranteed
   * to be a Just instance
   */
  private A val() { return ((Just<A>)this).value; }

  /**
   * <p>Gets the underlying value of this Maybe&lt;A&gt;
   * or throws a NoSuchElement exception if this is a Nothing.</p>
   * @return  the underlying value of this Maybe&lt;A&gt;
   */
  public A get() {
    if (this instanceof Just)
      return val();
    else
      throw new NoSuchElementException("Nothing.get()");
  }

  /**
   * <p>Returns the underlying value of this Maybe&lt;A&gt;
   * or evaluates the argument function to return a default value.</p>
   * @param defaultValue  a function that returns the default value if this was Nothing
   * @return              a value of type A. Either the value wrapped by this Maybe or
   *                      the result of evaluating the default value.
   */
  public A getOrElse(Supplier<? extends A> defaultValue) {
    if (this instanceof Just)
      return val();
    else
      return defaultValue.get();
  }

  /**
   * <p>Applies a function that maps a value of type A to type B to
   * the value contained within this Maybe&lt;A&gt; tranforming it
   * into a Maybe&lt;B&gt;</p>
   * @param f    the function that will be applied to the value of this Maybe
   * @param <B>  the target type or CoDomain of the function f
   * @return     an instance of Maybe&lt;B&gt;
   */
  public <B> Maybe<B> map(Function<? super A, ? extends B> f) {
    if (this instanceof Just)
      return just(f.apply(val()));
    else
      return nothing();
  }

  /**
   * <p>Applies a function that maps a value of type A to type Maybe&lt;B&gt;
   * to the value contained within this Maybe&lt;A&gt; transforming it
   * into a Maybe&lt;B&gt;</p>
   * @param f    the function that will be applied to the value of this Maybe
   * @param <B>  the target type or CoDomain of the value returned as a Maybe by function f
   * @return     an instance of Maybe&lt;B&gt;
   */
  public <B> Maybe<? extends B> flatMap(Function<? super A, ? extends Maybe<? extends B>> f) {
    if (this instanceof Just)
      return f.apply(val());
    else
      return nothing();
  }

  /**
   * <p>Transforms the contents of this Maybe&lt;A&gt; to an Either&lt;A,B&gt;
   * such that if this instance was an instance of Just&lt;A&gt; containing a
   * value of type a the instance returned will be Left&lt;A&gt;. Otherwise,
   * the function parameter will be evaluated to return a default value of type
   * B that will be returned as an instance of Right&lt;B&gt;.</p>
   * @param rightValue  the function that will provide a default value of type B
   * @param <B>         the type of the default value
   * @return            an instance of Either&lt;A,B&gt; that will be either
   *                    Left(a) or Right(b) where a is the content of Maybe(a)
   *                    and b is the result of evaluating rightValue.apply()
   */
  public <B> Either<A,B> toLeft(Supplier<B> rightValue) {
    if (this instanceof Just)
      return Either.left(val()).value();
    else
      return Either.right(rightValue.get()).value();
  }

  /**
   * <p>Transforms the contents of this Maybe&lt;A&gt; to an Either&lt;A,B&gt;
   * such that if this instance was an instance of Just&lt;A&gt; containing a
   * value of type a the instance returned will be Right&lt;A&gt;. Otherwise,
   * the function parameter will be evaluated to return a default value of type
   * B that will be returned as an instance of Left&lt;B&gt;.</p>
   * @param leftValue  the function that will provide a default value of type B
   * @param <B>         the type of the default value
   * @return            an instance of Either&lt;A,B&gt; that will be either
   *                    Left(b) or Right(a) where a is the content of Maybe(a)
   *                    and b is the result of evaluating leftValue.apply()
   */
  public <B> Either<B,A> toRight(Supplier<B> leftValue) {
    if (this instanceof Just)
      return Either.right(val()).value();
    else
      return Either.left(leftValue.get()).value();
  }

  /**
   * <p>A concrete implementation of Maybe for values that exist and
   * contain a value of type A</p>
   * @param <A>  the type of the value contained by this Just instance
   */
  static final class Just<A> extends Maybe<A> {
    final A value;
    Just(A value) { this.value = value; }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Just<?> just = (Just<?>) o;
      return value.equals(just.value);
    }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return "Just("+value+')'; }
  }
  private static <T> Maybe<T> just(T value) { return new Just<T>(value); }

  /**
   * <p>A concrete implementation of Maybe for values that don't exist
   * and as such don't contain anything</p>
   */
  static final class Nothing extends Maybe<Object> {
    private Nothing() {}
    @Override public String toString() { return "Nothing"; }
  }

  /*
   * A shared instance of Nothing that can be obtained with better
   * type inference using the static Maybe.nothing() method
   */
  private static final Nothing NOTHING = new Nothing();

}
