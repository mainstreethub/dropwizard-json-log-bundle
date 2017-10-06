package io.dropwizard.bundles.jsonlog.test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import org.junit.rules.ExternalResource;

/**
 * Interceptor which will capture any messages written to the console to test assertions against.
 */
public class ConsoleInterceptor extends ExternalResource {
  private final PrintStream current;

  private List<String> logs = new CopyOnWriteArrayList<>();

  public ConsoleInterceptor() {
    current = System.out;
    assert current != null;
  }

  @Override
  protected void before() throws Throwable {
    final PrintStream out = new PrintStream(current) {
      @Override
      public void write(byte[] b) throws IOException {
        logs.add(new String(b));
        super.write(b);
      }

      @Override
      public void write(byte[] buf, int off, int len) {
        logs.add(new String(buf, off, len));

        super.write(buf, off, len);
      }
    };

    System.setOut(out);
  }

  public Callable<Boolean> contains(Predicate<String> test) {
    return () -> logs.stream().anyMatch(test);
  }

  public List<String> getLogs() {
    return Collections.unmodifiableList(logs);
  }

  @Override
  protected void after() {
    System.setOut(current);
  }
}
