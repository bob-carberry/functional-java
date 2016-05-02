package com.example;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Class for testing Maybe functionality</p>
 */
@RunWith(JUnit4.class)
public class MaybeSpec {

  private static List<Integer> ints = Arrays.asList(0,1,2,3,4,5,6,7,8,9);

  private static final Supplier<Integer> two = () -> 2;
  private static final Function<Integer, Integer> inc = i -> i + 1;
  private static final Function<Object, String> toString = any -> any.toString();
  private static final Function<Integer,Maybe<Integer>> index = i -> {
    if (0 <= i && i < ints.size()) {
      return Maybe.apply(ints.get(i));
    } else {
      return Maybe.nothing();
    }
  };

  static class Person {}
  static class Parent extends Person {}
  static class Child extends Person {}

  private static final Supplier<Child> aChild = Child::new;

  @Test
  public void maybeApplyIntIsJustInt() {
    Maybe<Integer> maybeInt = Maybe.apply(1);
    Assert.assertTrue("Maybe.apply(1) is Just(1)", maybeInt instanceof Maybe.Just);
    int expected = 1;
    int actual = maybeInt.get();
    Assert.assertEquals("Maybe.apply(1).value is 1", expected, actual);
  }

  @Test
  public void maybeApplyNullIsNothing() {
    Maybe<Integer> maybeInt = Maybe.nothing();
    Assert.assertTrue("Maybe.nothing is Nothing", (Maybe)maybeInt instanceof Maybe.Nothing);
  }

  @Test
  public void justIntGetReturnsInt() {
    int expected = 1;
    int actual = Maybe.apply(1).get();
    Assert.assertEquals("Maybe.apply(1).get() is 1", expected, actual);
  }

  @Test(expected = NoSuchElementException.class)
  public void nothingIntGetThrowsNoSuchElementException() {
    Maybe<Integer> maybeInt = Maybe.nothing();
    maybeInt.get();
  }

  @Test
  public void justIntOneGetOrElseTwoIsOne() {
    int expected = 1;
    int actual = Maybe.apply(1).getOrElse(two);
    Assert.assertEquals("Maybe(1).getOrElse(2) is 1", expected, actual);
  }

  @Test
  public void justParentIsAParent() {
    Person expected = new Parent();
    Person actual = Maybe.apply(expected).getOrElse(aChild);
    Assert.assertEquals("Maybe(1).getOrElse(2) is 1", expected, actual);
  }

  @Test
  public void nothingGetOrElseTwoIsTwo() {
    int expected = 2;
    int actual = Maybe.<Integer>nothing().getOrElse(two);
    Assert.assertEquals("Nothing.getOrElse(2) is 2", expected, actual);
  }

  @Test
  public void just1MapIncToStringIsTwo() {
    String expected = "2";
    String actual = Maybe.apply(1).map(inc.andThen(toString)).get();
    Assert.assertEquals("Maybe.apply(1).map(inc.andThen(toString)) is Just(\"2\")", expected, actual);
  }

  @Test
  public void nothingMapIncToStringIsNothing() {
    Object expected = Maybe.nothing();
    Object actual = Maybe.<Integer>nothing().map(inc.andThen(toString));
    Assert.assertEquals("Nothing.map(f) is Nothing", expected, actual);
  }

  @Test
  public void justIntFlatMapIndexInListRangeIsInt() {
    int expected = 1;
    int actual = Maybe.apply(1).flatMap(index).get();
    Assert.assertEquals("Maybe.apply(1).flatMap(index) is 1", expected, actual);
  }

  @Test
  public void justIntFlatMapIndexOutOfListRangeIsNothing() {
    Object expected = Maybe.nothing();
    Object actual = Maybe.apply(20).flatMap(index);
    Assert.assertEquals("Maybe.apply(20).flatMap(index) is Nothing", expected, actual);
  }

  @Test
  public void nothingFlatMapIndexIsNothing() {
    Object expected = Maybe.nothing();
    Object actual = Maybe.<Integer>nothing().flatMap(index);
    Assert.assertEquals("Nothing.flatMap(index) is Nothing", expected, actual);
  }

  @Test
  public void justIntToLeftIsLeftInt() {
    Object expected = Either.left(1).value();
    Object actual = Maybe.apply(1).toLeft(two);
    Assert.assertEquals("Left(1) is Maybe(1).toLeft(two)", expected, actual);
  }

  @Test
  public void nothingToLeftisRight() {
    Object expected = Either.right(2).value();
    Object actual = Maybe.<Integer>nothing().toLeft(two);
    Assert.assertEquals("Right(2) is Nothing.toLeft(two)", expected, actual);
  }

  @Test
  public void justIntToRightIsRightInt() {
    Object expected = Either.right(1).value();
    Object actual = Maybe.apply(1).toRight(two);
    Assert.assertEquals("Right(1) is Maybe(1).toRight(two)", expected, actual);
  }

  @Test
  public void nothingToRightIsLeft() {
    Object expected = Either.left(2).value();
    Object actual = Maybe.<Integer>nothing().toRight(two);
    Assert.assertEquals("Left(2) is Nothing.toLeft(two)", expected, actual);
  }

  @Test
  public void maybeEqualBehaviour() {
    Assert.assertEquals("Nothing == Nothing", Maybe.nothing(), Maybe.nothing());
    Assert.assertEquals("Maybe(1) == Maybe(1)", Maybe.apply(1), Maybe.apply(1));
    Assert.assertEquals("Maybe(\"test string\") == Maybe(\"test string\")",
        Maybe.apply("test string"), Maybe.apply("test string"));
    Assert.assertNotEquals("Maybe(1) != Maybe(2)", Maybe.apply(1), Maybe.apply(2));
    Assert.assertNotEquals("Maybe(\"UPPERCASE\") != Maybe(\"lowercase\")",
        Maybe.apply("UPPERCASE"), Maybe.apply("lowercase"));
  }

  @Test
  public void maybeMapBehaviour() {
    Map<Maybe<String>, Integer> map = new HashMap<Maybe<String>, Integer>();
    map.put(Maybe.apply("string1"), 1);
    map.put(Maybe.apply("string2"), 2);
    Assert.assertEquals("map.get(Maybe(string1)) == 1", 1, map.get(Maybe.apply("string1")).intValue());
    Assert.assertEquals("map.get(Maybe(string2)) == 2", 2, map.get(Maybe.apply("string2")).intValue());
    Assert.assertNull("map.get(Maybe(string3)) == null", map.get(Maybe.apply("string3")));
    map.put(Maybe.apply("string1"), 3);
    Assert.assertEquals("map.get(Maybe(string1)) == 3", 3, map.get(Maybe.apply("string1")).intValue());
    Assert.assertNull("map.get(Nothing) == null", map.get(Maybe.<String>nothing()));
    map.put(Maybe.<String>nothing(), 10);
    Assert.assertEquals("map.get(Nothing) == 10", 10, map.get(Maybe.<String>nothing()).intValue());
  }

  @Test
  public void maybeToStringBehaviour() {
    Object expected = "Just(1)";
    Object actual = Maybe.apply(1).toString();
    Assert.assertEquals("Just(1).toString == \"Just(1)\"", expected, actual);
    expected = "Just(some string)";
    actual = Maybe.apply("some string").toString();
    Assert.assertEquals("Just(\"some string\").toString == \"Just(some string)\"", expected, actual);
    expected = "Nothing";
    actual = Maybe.nothing().toString();
    Assert.assertEquals("Nothing.toString == \"Nothing\"", expected, actual);
  }

}
