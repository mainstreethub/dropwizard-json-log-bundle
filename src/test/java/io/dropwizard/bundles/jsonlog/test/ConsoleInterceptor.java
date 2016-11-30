package io.dropwizard.bundles.jsonlog.test;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import org.junit.rules.ExternalResource;

public class ConsoleInterceptor extends ExternalResource {
  private List<String> logs = new CopyOnWriteArrayList<>();

  @Override
  protected void before() throws Throwable {
    System.setOut(new PrintStream(System.out) {
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
    });
  }

  public Callable<Boolean> contains(Predicate<String> test) {
    return () -> logs.stream().anyMatch(test);
  }

  public List<String> getLogs() {
    return Collections.unmodifiableList(logs);
  }

  @Override
  protected void after() {
    System.setOut(null);
  }
}
