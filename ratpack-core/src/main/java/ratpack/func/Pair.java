/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.func;

/**
 * A generic pair implementation that can be used to cumulatively aggregate a data structure during a promise pipeline.
 * <p>
 * This can sometimes be useful when collecting facts about something as part of a data stream without using mutable data structures.
 * <pre class="java">
 * import ratpack.exec.ExecControl;
 * import ratpack.exec.Promise;
 * import ratpack.func.Action;
 * import ratpack.func.Function;
 * import ratpack.func.Pair;
 * import ratpack.handling.Context;
 * import ratpack.handling.Handler;
 * import ratpack.launch.HandlerFactory;
 * import ratpack.launch.LaunchConfig;
 * import ratpack.launch.LaunchConfigBuilder;
 * import ratpack.test.embed.EmbeddedApplication;
 * import ratpack.test.embed.LaunchConfigEmbeddedApplication;
 * 
 * import java.util.concurrent.Callable;
 * 
 * public class Example {
 * 
 *   public static class PersonService {
 *     private final ExecControl exec;
 * 
 *     public PersonService(ExecControl exec) {
 *       this.exec = exec;
 *     }
 * 
 *     public Promise&lt;Integer&gt; getAge(int personId) {
 *       return exec.blocking(new Callable&lt;Integer&gt;() {
 *         public Integer call() throws Exception {
 *           return 21;
 *         }
 *       });
 *     }
 * 
 *     public Promise&lt;String&gt; getName(int personId) {
 *       return exec.blocking(new Callable&lt;String&gt;() {
 *         public String call() throws Exception {
 *           return "John Doe";
 *         }
 *       });
 *     }
 *   }
 * 
 *   private static class ExampleHandler implements Handler {
 *     private final PersonService personService;
 * 
 *     private ExampleHandler(PersonService personService) {
 *       this.personService = personService;
 *     }
 * 
 *     public void handle(final Context context) {
 *       final int personId = 1;
 *       personService
 *         .getAge(personId)
 *         .map(new Function&lt;Integer, Pair&lt;Integer, Integer&gt;&gt;() {
 *           public Pair&lt;Integer, Integer&gt; apply(Integer age) throws Exception {
 *             return Pair.of(personId, age);
 *           }
 *         })
 *         .flatMap(new Function&lt;Pair&lt;Integer, Integer&gt;, Promise&lt;Pair&lt;Integer, Pair&lt;String, Integer&gt;&gt;&gt;&gt;() {
 *           public Promise&lt;Pair&lt;Integer, Pair&lt;String, Integer&gt;&gt;&gt; apply(final Pair&lt;Integer, Integer&gt; pair) throws Exception {
 *             return personService.getName(personId).map(new Function&lt;String, Pair&lt;Integer, Pair&lt;String, Integer&gt;&gt;&gt;() {
 *               public Pair&lt;Integer, Pair&lt;String, Integer&gt;&gt; apply(String s) throws Exception {
 *                 return pair.nestRight(s);
 *               }
 *             });
 *           }
 *         }).then(new Action&lt;Pair&lt;Integer, Pair&lt;String, Integer&gt;&gt;&gt;() {
 *         public void execute(Pair&lt;Integer, Pair&lt;String, Integer&gt;&gt; pair) throws Exception {
 *           int id = pair.left;
 *           int age = pair.right.right;
 *           String name = pair.right.left;
 * 
 *           context.render(name + " [" + id + "] - age: " + age);
 *         }
 *       });
 *     }
 *   }
 * 
 *   private static EmbeddedApplication createApp() {
 *     return new LaunchConfigEmbeddedApplication() {
 *       protected LaunchConfig createLaunchConfig() {
 *         return LaunchConfigBuilder.noBaseDir().port(0).build(new HandlerFactory() {
 *           public Handler create(LaunchConfig launchConfig) {
 *             ExecControl execControl = launchConfig.getExecController().getControl();
 *             return new ExampleHandler(new PersonService(execControl));
 *           }
 *         });
 *       }
 *     };
 *   }
 * 
 *   public static void main(String[] args) {
 *     try (EmbeddedApplication app = createApp()) {
 *       assert app.getHttpClient().getText().equals("John Doe [1] - age: 21");
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * While the example above looks positively terrifying when using Java 7, it is much more reasonable when using Java 8 with lambda expressions and type inference.
 * It is also much more reasonable when written in idiomatic Groovy with its type inference.
 *
 * @param <L> the left data type
 * @param <R> the right data type
 */
public final class Pair<L, R> {

  /**
   * The left item of the pair.
   */
  public final L left;

  /**
   * The right item of the pair.
   */
  public final R right;

  private Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  /**
   * The left item of the pair.
   * 
   * @return the left item of the pair
   */
  public L getLeft() {
    return left;
  }

  /**
   * The right item of the pair.
   *
   * @return the right item of the pair
   */
  public R getRight() {
    return right;
  }

  /**
   * Creates a new pair.
   * 
   * @param left the left item
   * @param right the right item
   * @param <L> the type of the left item
   * @param <R> the type of the right item
   * @return a new pair
   */
  public static <L, R> Pair<L, R> of(L left, R right) {
    return new Pair<>(left, right);
  }

  /**
   * Creates a new pair.
   *
   * @param left the left item
   * @param right the right item
   * @param <L> the type of the left item
   * @param <R> the type of the right item
   * @return a new pair
   */
  public static <L, R> Pair<L, R> pair(L left, R right) {
    return of(left, right);
  }

  /**
   * Creates a new pair, with {@code this} as the right item and the given value as the left.
   *
   * @param t the left value for the returned pair
   * @param <T> the type of the left value for the returned pair
   * @return a new pair, with {@code this} as the right item and the given value as the left
   */
  public <T> Pair<T, Pair<L, R>> pushLeft(T t) {
    return of(t, this);
  }

  /**
   * Creates a new pair, with {@code this} as the left item and the given value as the right.
   *
   * @param t the right value for the returned pair
   * @param <T> the type of the right value for the returned pair
   * @return a new pair, with {@code this} as the left item and the given value as the right
   */
  public <T> Pair<Pair<L, R>, T> pushRight(T t) {
    return of(this, t);
  }

  /**
   * Creates a new pair, with {@code pair(t, this.left)} as the left item and the the right value of {@code this} as the right.
   *
   * @param t the item to nest with the left item of {@code this} pair
   * @param <T> the type of item to nest with the left item of {@code this} pair
   * @return a new pair, with {@code pair(t, this.left)} as the left item and the the right value of {@code this} as the right
   */
  public <T> Pair<Pair<T, L>, R> nestLeft(T t) {
    return of(of(t, left), right);
  }

  /**
   * Creates a new pair, with {@code pair(t, this.right)} as the right item and the the left value of {@code this} as the left.
   *
   * @param t the item to nest with the right item of {@code this} pair
   * @param <T> the type of item to nest with the right item of {@code this} pair
   * @return a new pair, with {@code pair(t, this.right)} as the right item and the the left value of {@code this} as the left
   */
  public <T> Pair<L, Pair<T, R>> nestRight(T t) {
    return of(left, of(t, right));
  }

  /**
   * Creates a new pair, with the left item being the result of applying the given function to the left item of {@code this}.
   * <p>
   * The right value is unchanged.
   *
   * @param function a transformer for the left value
   * @param <T> the type of the new left value
   * @return a new pair, with the left item being the result of applying the given function to the left item of {@code this}
   * @throws Exception any thrown by {@code function}
   */
  public <T> Pair<T, R> mapLeft(Function<? super L, ? extends T> function) throws Exception {
    T t = function.apply(left);
    return of(t, right);
  }

  /**
   * Creates a new pair, with the right item being the result of applying the given function to the right item of {@code this}.
   * <p>
   * The left value is unchanged.
   *
   * @param function a transformer for the right value
   * @param <T> the type of the new right value
   * @return a new pair, with the right item being the result of applying the given function to the right item of {@code this}
   * @throws Exception any thrown by {@code function}
   */
  public <T> Pair<L, T> mapRight(Function<? super R, ? extends T> function) throws Exception {
    T t = function.apply(right);
    return of(left, t);
  }

  /**
   * Applies the given function to {@code this}, returning the result.
   *
   * @param function a function to apply to {@code this}
   * @param <T> the function result type
   * @return the result of applying {@code function} to {@code this}
   * @throws Exception any thrown by {@code function}
   */
  public <T> T map(Function<? super Pair<L, R>, ? extends T> function) throws Exception {
    return function.apply(this);
  }

  /**
   * Convenience function for returning the left item of a pair.
   *
   * @param <L> the type of the left item
   * @param <P> the pair type
   * @return a function that when applied to a pair returns the left item
   */
  public static <L, P extends Pair<L, ?>> Function<P, L> unpackLeft() {
    return new Function<P, L>() {
      @Override
      public L apply(P pair) throws Exception {
        return pair.left;
      }
    };
  }

  /**
   * Convenience function for returning the right item of a pair.
   *
   * @param <R> the type of the right item
   * @param <P> the pair type
   * @return a function that when applied to a pair returns the right item
   */
  public static <R, P extends Pair<?, R>> Function<P, R> unpackRight() {
    return new Function<P, R>() {
      @Override
      public R apply(P pair) throws Exception {
        return pair.right;
      }
    };
  }

  /**
   * A pair is equal if its left and right items are equal to the left and right items of {@code this} respectively.
   *
   * @param o the object to compare to
   * @return the equality
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Pair<?, ?> pair = (Pair<?, ?>) o;

    return !(left != null ? !left.equals(pair.left) : pair.left != null) && !(right != null ? !right.equals(pair.right) : pair.right != null);
  }

  /**
   * Hash code.
   *
   * @return hash code.
   */
  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }

  /**
   * Returns "Pair[«left.toString()»,«right.toString()»]
   *
   * @return "Pair[«left.toString()»,«right.toString()»]
   */
  @Override
  public String toString() {
    return "Pair[" + left + "," + right + ']';
  }

}