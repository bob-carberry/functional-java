  package com.example;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Class for testing Either functionality</p>
 */
@RunWith(JUnit4.class)
public class EitherSpec {

  private static final Supplier<String> defaultString = () -> "default";
  private static final Supplier<Integer> defaultInt = () -> 1;
  private static final Function<Integer, Integer> inc = i -> i + 1;
  private static final Function<String, Integer> hash = String::hashCode;
  private static final Function<Integer, Either<String, Integer>> hundredDivN =
      i -> {
        if (i == 0) {
          return Either.left("cannot div by zero").value();
        } else {
          return Either.right(100 / i).value();
        }
      };

  @Test
  public void leftStringValueIsLeftString() {
    Either<String,Integer> either = Either.left("test").value();
    Assert.assertTrue(either instanceof Either.Left);
    String expected = "test";
    String actual = either.getLeftOrThrow(new RuntimeException());
    Assert.assertEquals("Either.left(\"test\").value() value should be \"test\"", expected, actual);
  }

  @Test
  public void rightIntegerValueisRightInteger() {
    Either<String,Integer> either = Either.right(10).value();
    Assert.assertTrue(either instanceof Either.Right);
    int expected = 10;
    int actual = either.getRightOrThrow(new RuntimeException());
    Assert.assertEquals("Either.right(10).value() value should be 10", expected, actual);
  }

  @Test
  public void leftStringOrIntegerIsLeftString() {
    Either<String,Integer> either = Either.left("test").or(Integer.class);
    Assert.assertTrue(either instanceof Either.Left);
    String expected = "test";
    String actual = either.getLeftOrThrow(new RuntimeException());
    Assert.assertEquals("Either.left(\"test\").or(Integer.class) value should be \"test\"", expected, actual);
  }

  @Test
  public void rightIntegerOrStringIsRightInteger() {
    Either<String,Integer> either = Either.right(10).or(String.class);
    Assert.assertTrue(either instanceof Either.Right);
    int expected = 10;
    int actual = either.getRightOrThrow(new RuntimeException());
    Assert.assertEquals("Either.right(10).or(String.class) value should be 10", expected, actual);
  }

  @Test
  public void leftStringGetLeftOrDefaultStringReturnsString() {
    String expected = "test";
    String actual = Either.left("test").value().getLeftOr(defaultString);
    Assert.assertEquals("leftTest.getLeftOr(defaultString) should be \"test\"", expected, actual);
  }

  @Test
  public void rightIntGetLeftOrDefaultStringReturnsDefault() {
    String expected = "default";
    String actual = Either.right(1).or(String.class).getLeftOr(defaultString);
    Assert.assertEquals("right1.getLeftOr(defaultString) should be defaultString", expected, actual);
  }

  @Test
  public void leftStringGetRightOrDefaultIntReturnsDefault() {
    int expected = 1;
    int actual = Either.left("test").or(Integer.class).getRightOr(defaultInt);
    Assert.assertEquals("leftTest.getRightOr(defaultInt) should be defaultInt",expected,actual);
  }

  @Test
  public void rightIntGetRightOrDefaultIntReturnsInt() {
    int expected = 10;
    int actual = Either.right(10).or(String.class).getRightOr(defaultInt);
    Assert.assertEquals("right10.getRightOr(defaultInt) should be 10", expected, actual);
  }

  @Test
  public void leftStringGetLeftOrThrowEReturnsString() {
    String expected = "test";
    String actual = Either.left("test").value().getLeftOrThrow(new NoSuchElementException());
    Assert.assertEquals("leftTest.getLeftOrThrow(e) should return \"test\"", expected, actual);
  }

  @Test
  public void rightIntegerGetRightOrThrowEReturnsInteger() {
    int expected = 10;
    int actual = Either.right(10).value().getRightOrThrow(new NoSuchElementException());
    Assert.assertEquals("right10.getRightOrThrow(e) should return 10", expected, actual);
  }

  @Test(expected = NoSuchElementException.class)
  public void leftStringGetRightOrThrowEThrowsE() {
    Either.left("test").or(Integer.class).getRightOrThrow(new NoSuchElementException());
  }

  @Test(expected = NumberFormatException.class)
  public void rightIntegerGetLeftOrThrowEThrowsE() {
    Either.right(10).or(String.class).getLeftOrThrow(new NumberFormatException());
  }

  @Test
  public void leftMapFIsUnchanged() {
    Either<String, Integer> expected = Either.left("test").value();
    Either<String, Integer> actual = expected.map(new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer i) {
        return i + 1;
      }
    });
    Assert.assertEquals("leftTest.map(f) == leftTest", expected, actual);
  }

  @Test
  public void rightMapFMapsFOverRightValue() {
    Either<String, Double> expected = Either.right(Math.sqrt(2)).value();
    Either<String, Double> actual = Either.right(2).or(String.class).map(new Function<Integer, Double>() {
      @Override
      public Double apply(Integer i) {
        return Math.sqrt(i);
      }
    });
    Assert.assertEquals("rightSqrt2 == right2.map(sqrt)", expected, actual);
  }

  @Test
  public void leftFlatMapFIsUnchanged() {
    Either<String,Integer> expected = Either.left("test").value();
    Object actual = expected.flatMap(hundredDivN);
    Assert.assertEquals("leftTest.flatMap(hundredDivN) value should be \"test\"", expected, actual);
  }

  @Test
  public void right50FlatMapHundredDivNIsRight2() {
    Either<String,Integer> right50 = Either.right(50).value();
    Object expected = Either.right(2).value();
    Object actual = right50.flatMap(hundredDivN);
    Assert.assertEquals("right2 should equal right50.flatMap(hundredDivN)", expected, actual);
  }

  @Test
  public void right0FlatMapHundredDivNIsLeftErrString() {
    Either<String, Integer> right0 = Either.right(0).value();
    Object expected = Either.left("cannot div by zero").or(Integer.class);
    Object actual = right0.flatMap(hundredDivN);
    Assert.assertEquals("leftErrString should equals right0.flatMap(hundredDivN)", expected, actual);
  }

  @Test
  public void leftTestFoldHashInc() {
    Either<Integer,Integer> expected = Either.left("test".hashCode()).value();
    Either<String,Integer> either = Either.left("test").value();
    Either<Integer,Integer> actual = either.fold(hash, inc);
    Assert.assertEquals("leftTest.fold(hash,inc) should be leftTestHashCode", expected, actual);
  }

  @Test
  public void rightIntFoldHashInc() {
    Either<Integer,Integer> expected = Either.right(1).value();
    Either<String,Integer> either = Either.right(0).value();
    Either<Integer,Integer> actual = either.fold(hash,inc);
    Assert.assertEquals("right0.fold(hash,inc) should be right1", expected, actual);
  }

  @Test
  public void eitherEqualsOps() {
    // for 4 different values a,b,c,d => a != b != c != d
    Either<String, Integer> a = Either.left("test").value();
    Either<String, Integer> b = Either.left("different text").value();
    Either<String, Integer> c = Either.right(0).value();
    Either<String, Integer> d = Either.right(10).value();

    Assert.assertNotEquals(a,b);
    Assert.assertNotEquals(a,c);
    Assert.assertNotEquals(a,d);
    Assert.assertNotEquals(b,c);
    Assert.assertNotEquals(b,d);
    Assert.assertNotEquals(c,d);

    // for 4 values x,y,z,w => a == x && b == y && c == z && d == w
    Either<String, Integer> x = Either.left("test").value();
    Either<String, Integer> y = Either.left("different text").value();
    Either<String, Integer> z = Either.right(0).value();
    Either<String, Integer> w = Either.right(10).value();

    Assert.assertEquals(a,x);
    Assert.assertEquals(b,y);
    Assert.assertEquals(c,z);
    Assert.assertEquals(d,w);

  }

  @Test
  public void eitherMapOps() {
    Map<Either<String, Integer>, String> map =
        new HashMap<Either<String, Integer>, String>();

    String l1 = "test";
    String l2 = "alternative test string"; // l2.hashCode() == r2
    int r1 = l1.hashCode();
    int r2 = -1132440724;

    Either<String,Integer> a = Either.left(l1).value();
    Either<String,Integer> b = Either.left(l2).value();
    Either<String,Integer> c = Either.right(r1).value();
    Either<String,Integer> d = Either.right(r2).value();

    map.put(a, "left");
    map.put(b, "left");
    map.put(c, "right");
    map.put(d, "right");

    Assert.assertTrue(map.containsKey(a));
    Assert.assertTrue(map.containsKey(b));
    Assert.assertTrue(map.containsKey(c));
    Assert.assertTrue(map.containsKey(d));

    Assert.assertEquals("left", map.get(a));
    Assert.assertEquals("left", map.get(b));
    Assert.assertEquals("right", map.get(c));
    Assert.assertEquals("right", map.get(d));

  }

}
