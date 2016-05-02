package com.example;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import java.util.NoSuchElementException;

/**
 * <p>Either&lt;A,B&gt; is a union of two types A and B.</p>
 * <p>Either has two concrete types Left and Right.
 * Left wraps a value of type A while Right wraps a value of type B.</p>
 *
 * @author Bob Carberry
 */
public abstract class Either<A, B> {


  // private helper method for creating Left values with inferred types
  private static <L, R> Either<L, R> lt(L value) {
    return (Either<L,R>)new Left<L>(value);
  }

  // private helper method for creating Right values with inferred types
  private static <L, R> Either<L, R> rt(R value) {
    return (Either<L,R>)new Right<R>(value);
  }

  /**
   * <p>Creates a LeftBuilder which can be used to wrap a value as a Left instance
   * of Either. E.G.</p>
   * <pre>
   *   <blockqoute>
   *     Either&lt;String,Integer&gt; leftText = Either.left("text").value();
   *     leftText.equals(Either.left("text").or(Integer.class));
   *   </blockqoute>
   * </pre>
   * @param value  the value wrapped as a Left
   * @param <L>    the type of the wrapped value
   * @return       a LeftBuilder instance for creating a value wrapped as a Left
   */
  public static <L> LeftBuilder<L> left(L value) {
    return new LeftBuilder<L>(value);
  }

  /**
   * <p>Creates a RightBuilder which can be used to wrap a value as a Right instance
   * of Either. E.G.</p>
   * <pre>
   *   <blockqoute>
   *     Either&lt;String,Integer&gt; leftText = Either.left("text").value();
   *     leftText.equals(Either.left("text").or(Integer.class));
   *   </blockqoute>
   * </pre>
   * @param value  the value wrapped as a Right
   * @param <R>    the type of the wrapped value
   * @return       a RightBuilder instance for creating a walue wrapped as a Right
   */
  public static <R> RightBuilder<R> right(R value) {
    return new RightBuilder<R>(value);
  }

  /**
   * <p>Gets the value wrapped by the Either if it is a Left otherwise
   * it evaluates the Supplier parameter</p>
   * @param expression  a function that returns a type A
   * @return            a value of type A. Either the value wrapped by a Left
   *                    or the result of evaluating the Supplier parameter
   */
  public final A getLeftOr(Supplier<A> expression) {
    if (this instanceof Left)
      return ((Left<A>)this).value;
    else
      return expression.get();
  }

  /**
   * <p>Gets the value wrapped by the Either if it is a Left otherwise
   * the RuntimeException parameter gets thrown.</p>
   * @param re  the RuntimeException to throw if the Either is a Right
   * @return    the value wrapped by this Either if it is a Left
   * @throws    RuntimeException if the Either is a Right
   */
  public final A getLeftOrThrow(RuntimeException re) throws RuntimeException {
    if (this instanceof Left)
      return ((Left<A>)this).value;
    else
      throw re;
  }

  /**
   * <p>Gets the value wrapped by the Either if it is a Right otherwise
   * it evaluates the Supplier parameter</p>
   * @param expression  a function that returns a type B
   * @return            a value of type B. Either the value wrapped by a Right
   *                    or the result of evaluating the Supplier parameter
   */
  public final B getRightOr(Supplier<B> expression) {
    if (this instanceof Right)
      return ((Right<B>)this).value;
    else
      return expression.get();
  }

  /**
   * <p>Gets the value wrapped by the Either if it is a Right, otherwise
   * the RuntimeException parameter gets thrown.</p>
   * @param re  the RuntimeException to throw if the Either is a Left
   * @return    the value wrapped by the Either if it is a Right
   * @throws    RuntimeException if the Either is a Left
   */
  public final B getRightOrThrow(RuntimeException re) throws RuntimeException {
    if (this instanceof Right)
      return ((Right<B>) this).value;
    else
      throw re;
  }

  /**
   * <p>If the Either is a Right this applies a function f that maps a type
   * B to a type C to the value of the Right, otherwise the Either is a Left
   * in which case the Left is returned unchanged.</p>
   * @param f    a function that maps a type B to a type C
   * @param <C>  the type that the Right will become
   * @return     a new Either. If the Either was a Left the new Either is
   *             unchanged. If the Either was a Right the new Either will
   *             be a Right with the value of the previous Right applied
   *             to the function f
   */
  public final <C> Either<A,C> map(Function<? super B, ? extends C> f) {
    if (this instanceof Left)
      return (Either<A,C>) this;
    else if (this instanceof Right)
      return rt(f.apply(((Right<B>)this).value));
    else
      throw new NoSuchElementException();
  }

  /**
   * <p>If the Either is a Right this applies a function f that maps a type
   * B to a type Either&lt;A,C&gt; to the value of the Right, returning the
   * result of the function. Otherwise the Either is a Left, in which case the
   * Left is returned unchanged.</p>
   * @param f    a function that maps a type B to a type Either&lt;A,C&gt;
   * @param <C>  the right type of the new Either
   * @return     a new Either. If the Either was a Left the new Either is
   *             unchanged. If the Either was a Right then the new Either
   *             will be the result of applying f to the value of Right.
   */
  public final <C> Either<? super A, ? extends C> flatMap(
      Function<? super B, ? extends Either<? super A, ? extends C>> f) {
    if (this instanceof Left)
      return (Either<A,C>) this;
    else if (this instanceof Right)
      return f.apply( ((Right<B>)this).value );
    else
      throw new NoSuchElementException();
  }

  /**
   * <p>Applies one of two functions to this either.</p>
   * <p>If this is a Left function f gets applied to the Left value.
   * If this is a Right function g gets applied to the Right value.
   * In either case the value is returned as a properly typed Either.</p>
   * @param f    a function that maps a type A to a type C
   * @param g    a function that maps a type B to a type D
   * @param <C>  the left type of the new Either
   * @param <D>  the right type fo the new Either
   * @return     a new Either with either f or g applied to the value
   *             that the Either wraps
   */
  public final <C,D> Either<C,D> fold(
      Function<? super A, ? extends C> f,
      Function<? super B, ? extends D> g) {
    if (this instanceof Left) {
      A a = ((Left<A>)this).value;
      return lt(f.apply(a));
    } else if (this instanceof Right) {
      B b = ((Right<B>)this).value;
      return rt(g.apply(b));
    } else
      throw new NoSuchElementException();
  }

  /**
   * <p>Left is a concrete implementation of Either that contains
   * a value of type A.</p>
   * @param <A>  the type of value the Left contains
   */
  static final class Left<A> extends Either<A, Object> {
    final A value;
    Left(A value) {
      this.value = value;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Left<?> left = (Left<?>) o;
      return value.equals(left.value);
    }
    @Override
    public int hashCode() {
      return value.hashCode() * 17;
    }
  }

  /**
   * <p>Right is a concrete implementation of Either that contains
   * a value of type B</p>
   * @param <B>  the type of value the Right contains
   */
  static final class Right<B> extends Either<Object, B> {
    final B value;
    public Right(B value) {
      this.value = value;
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Right<?> right = (Right<?>) o;
      return value.equals(right.value);
    }
    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }

  /**
   * <p>A builder class for creating properly typed instances of a Left Either.</p>
   * <p>Use the value() instance method where possible but there is also an or instance
   * method that allows the right type to be explicitly specified in cases where type
   * inference doesn't work. E.G.</p>
   * <pre>
   *   <blockqoute>
   *     Either&lt;String,Integer&gt; stringOrInteger = Either.left("string").value();
   *     Either.left("string")
   *           .or(Integer.class)
   *           .map(Function.&lt;Integer&gt;identity())
   *           .equals(stringOrInteger); // == true
   *   </blockqoute>
   * </pre>
   * @param <A>  the type of the value Left that will wrap
   */
  public static final class LeftBuilder<A> {
    private final A value;
    private LeftBuilder(A value) {
      this.value = value;
    }

    /**
     * <p>Returns a Left as a properly typed Either.</p>
     * @param <B>  the type of the right
     * @return     a Left as a properly typed Either
     */
    public <B> Either<A,B> value() {
      return lt(value);
    }

    /**
     * <p>Returns a Left as a properly typed Either.</p>
     * <p>A hint for the right type can be passed to this
     * method.</p>
     * @param hint  the class of desired right type
     * @param <B>   the right type
     * @return      a Left as a properly typed Either
     */
    public <B> Either<A,B> or(Class<? extends B> hint) {
      return lt(value);
    }
  }

  /**
   * <p>A builder class for creating properly typed instances of a Right Either.</p>
   * <p>Use the value() instance method where possible but there is also an or instance
   * method that allows the left type to be explicitly specified in cases where type
   * inference doesn't work. E.G.</p>
   * <pre>
   *   <blockqoute>
   *     Either&lt;String,Integer&gt; stringOrInteger = Either.right(1).value();
   *     Either.right(1)
   *           .or(String.class)
   *           .map(Function.&lt;Integer&gt;identity())
   *           .equals(stringOrInteger); // == true
   *   </blockqoute>
   * </pre>
   * @param <B>  the right type
   */
  public static final class RightBuilder<B> {
    private final B value;
    private RightBuilder(B value) {
      this.value = value;
    }

    /**
     * <p>Returns a Right as a properly typed Either.</p>
     * @param <A>  the left type
     * @return     a Right as a properly typed Either
     */
    public <A> Either<A,B> value() {
      return rt(value);
    }

    /**
     * <p>Returns a Right as a properly typed Either.</p>
     * @param hint  the class of the desired left type
     * @param <A>   the left type
     * @return      a Right as a properly typed Either
     */
    public <A> Either<A,B> or(Class<? extends A> hint) {
      return rt(value);
    }
  }

}
