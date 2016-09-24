package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

  private static final class Person {
    private final String firstName;
    private final Maybe<String> middleName;
    private final String lastName;
    private final int age;
    private final Gender gender;

    Person(String firstName, Maybe<String> middleName, String lastName,
           int age, Gender gender) {
      this.firstName = firstName;
      this.middleName = middleName;
      this.lastName = lastName;
      this.age = age;
      this.gender = gender;
    }

    public String getFirstName() {
      return firstName;
    }

    public Maybe<String> getMiddleName() {
      return middleName;
    }

    public String getLastName() {
      return lastName;
    }

    public int getAge() {
      return age;
    }

    public Gender getGender() {
      return gender;
    }
  }

  private static enum Gender {
    Male, Female;
  }

  private static final class RequirementNotMet {
    private final String field;
    private final String reason;

    RequirementNotMet(String field, String reason) {
      this.field = field;
      this.reason = reason;
    }

    public String getField() {
      return field;
    }

    public String getReason() {
      return reason;
    }

    public String toString() {
      return "RequirementNotMet(" + field + ", " + reason + ")";
    }

  }

  private static final Function<Person, Maybe<RequirementNotMet>> senior =
    (person) -> {
      if (person.getAge() < 65) {
        return Maybe.apply(new RequirementNotMet("age", "less than 65"));
      } else {
        return Maybe.nothing();
      }
    };

  private static final Function<Person, Maybe<RequirementNotMet>> female =
    (person) -> {
      if (person.getGender() == Gender.Male) {
        return Maybe.apply(new RequirementNotMet("gender", "is male"));
      } else {
        return Maybe.nothing();
      }
    };

  private static Either<List<RequirementNotMet>, Person> check(
		  List<Function<Person, Maybe<RequirementNotMet>>> requirements,
		  Person person) {

    List<RequirementNotMet> unmetRequirements = requirements.stream()
	    .map(requirement -> requirement.apply(person))
	    .filter(maybe -> Maybe.nothing().equals(maybe))
	    .map(maybe -> maybe.get())
	    .collect(Collectors.toList());


    if (unmetRequirements.isEmpty()) {
      return Either.right(person).value();
    } else {
      return Either.left(unmetRequirements).value();
    }
  }

  public static void main(String[] args) throws Exception {
    List<Function<Person, Maybe<RequirementNotMet>>> requirements =
      Arrays.asList(senior, female);

    Person oldLady = new Person("Mary", Maybe.nothing(), "Murphy",
		                66, Gender.Female);

    Person teenagerM = new Person("Tom", Maybe.apply("Dick"), "Harry",
		                  18, Gender.Male);

    System.out.println(check(requirements, oldLady));
  }
}
