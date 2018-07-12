package io.dropwizard.bundles.jsonlog;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLog;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.request.logging.async.AsyncAccessEventAppenderFactory;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import net.logstash.logback.encoder.LogstashAccessEncoder;
import net.logstash.logback.fieldnames.LogstashAccessFieldNames;
import org.eclipse.jetty.server.RequestLog;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.LoggerFactory;

/**
 * Creates a Logback access request logger which will log access requests as JSON messages.
 */
@SuppressWarnings("WeakerAccess")
@JsonTypeName("json-logback-access")
public class JsonRequestLogFactory
    extends AbstractAppenderFactory<IAccessEvent>
    implements RequestLogFactory<RequestLog> {

  @Valid
  @NotNull
  private Fields fields = new Fields();

  @JsonProperty
  public Fields getFields() {
    return fields;
  }

  @JsonProperty
  public void setFields(Fields fields) {
    this.fields = fields;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public RequestLog build(String name) {
    final LogbackAccessRequestLog requestLog = new LogbackAccessRequestLog();
    requestLog.addAppender(wrapAsync(createAppender(), new AsyncAccessEventAppenderFactory()));
    return requestLog;
  }

  @Override
  public Appender<IAccessEvent> build(
      LoggerContext context, String applicationName,
      LayoutFactory<IAccessEvent> layoutFactory,
      LevelFilterFactory<IAccessEvent> filterFactory,
      AsyncAppenderFactory<IAccessEvent> appenderFactory) {
    throw new UnsupportedOperationException();
  }

  protected LogstashAccessEncoder createEncoder() {
    final LogstashAccessEncoder encoder = new LogstashAccessEncoder();

    final LogstashAccessFieldNames fieldNames = new LogstashAccessFieldNames();
    fieldNames.setContentLength(fields.getContentLength().orElse(null));
    fieldNames.setElapsedTime(fields.getElapsedTime().orElse(null));
    fieldNames.setMethod(fields.getMethod().orElse(null));
    fieldNames.setProtocol(fields.getProtocol().orElse(null));
    fieldNames.setRemoteHost(fields.getRemoteHost().orElse(null));
    fieldNames.setRequestedUri(fields.getRequestedUri().orElse(null));
    fieldNames.setStatusCode(fields.getStatusCode().orElse(null));
    fieldNames.setRemoteHost(fields.getHostname().orElse(null));
    fieldNames.setRequestedUrl(fields.getRequestedUrl().orElse(null));
    fieldNames.setRemoteUser(fields.getRemoteUser().orElse(null));
    fieldNames.setMessage(fields.getMessage());
    encoder.setFieldNames(fieldNames);

    encoder.start();
    return encoder;
  }

  protected ConsoleAppender<IAccessEvent> createAppender() {
    final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
    logger.setAdditive(false);

    final ConsoleAppender<IAccessEvent> appender = new ConsoleAppender<>();
    appender.setName("console-appender");
    appender.setContext(logger.getLoggerContext());
    appender.setEncoder(createEncoder());
    appender.start();

    return appender;
  }

  @SuppressWarnings("unused")
  public static class Fields {
    private String contentLength = "bytes";
    private String elapsedTime = "duration";
    private String method = "verb";
    private String protocol = "protocol";
    private String remoteHost = "source_host";
    private String remoteUser = "remote_user";
    private String requestedUri = "request";
    private String statusCode = "response";
    private String hostname = null;
    private String requestedUrl = null;
    @NotBlank
    private String message = "message";

    @JsonProperty
    public void setContentLength(String contentLength) {
      this.contentLength = contentLength;
    }

    @JsonProperty
    public void setElapsedTime(String elapsedTime) {
      this.elapsedTime = elapsedTime;
    }

    @JsonProperty
    public void setMethod(String method) {
      this.method = method;
    }

    @JsonProperty
    public void setProtocol(String protocol) {
      this.protocol = protocol;
    }

    @JsonProperty
    public void setRemoteHost(String remoteHost) {
      this.remoteHost = remoteHost;
    }

    @JsonProperty
    public void setRemoteUser(String remoteUser) {
      this.remoteUser = remoteUser;
    }

    @JsonProperty
    public void setRequestedUri(String requestedUri) {
      this.requestedUri = requestedUri;
    }

    @JsonProperty
    public void setStatusCode(String statusCode) {
      this.statusCode = statusCode;
    }

    @JsonProperty
    public Optional<String> getContentLength() {
      return Optional.ofNullable(contentLength);
    }

    @JsonProperty
    public Optional<String> getElapsedTime() {
      return Optional.ofNullable(elapsedTime);
    }

    @JsonProperty
    public Optional<String> getMethod() {
      return Optional.ofNullable(method);
    }

    @JsonProperty
    public Optional<String> getProtocol() {
      return Optional.ofNullable(protocol);
    }

    @JsonProperty
    public Optional<String> getRemoteHost() {
      return Optional.ofNullable(remoteHost);
    }

    @JsonProperty
    public Optional<String> getRemoteUser() {
      return Optional.ofNullable(remoteUser);
    }

    @JsonProperty
    public Optional<String> getRequestedUri() {
      return Optional.ofNullable(requestedUri);
    }

    @JsonProperty
    public Optional<String> getStatusCode() {
      return Optional.ofNullable(statusCode);
    }

    @JsonProperty
    public Optional<String> getHostname() {
      return Optional.ofNullable(hostname);
    }

    @JsonProperty
    public Optional<String> getRequestedUrl() {
      return Optional.ofNullable(requestedUrl);
    }

    @JsonProperty
    public String getMessage() {
      return message;
    }
  }
}

