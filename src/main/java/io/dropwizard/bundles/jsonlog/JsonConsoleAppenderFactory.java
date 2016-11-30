package io.dropwizard.bundles.jsonlog;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.logstash.logback.encoder.LogstashEncoder;

/**
 * Appender which will write logging events as JSON out to the console.
 */
@JsonTypeName("json")
public class JsonConsoleAppenderFactory extends ConsoleAppenderFactory<ILoggingEvent> {
  @Override
  public Appender<ILoggingEvent> build(
      LoggerContext context, String applicationName, LayoutFactory<ILoggingEvent> layoutFactory,
      LevelFilterFactory<ILoggingEvent> levelFilterFactory,
      AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
    final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
    appender.setName("console-appender");
    appender.setContext(context);
    appender.setTarget(getTarget().get());

    final LogstashEncoder encoder = new LogstashEncoder();
    encoder.setTimeZone(getTimeZone().getID());
    encoder.start();

    appender.addFilter(levelFilterFactory.build(threshold));
    getFilterFactories().forEach(f -> appender.addFilter(f.build()));

    appender.setEncoder(encoder);
    appender.start();

    return wrapAsync(appender, asyncAppenderFactory);
  }
}
