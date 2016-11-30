package io.dropwizard.bundles.jsonlog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.bundles.jsonlog.test.ConsoleInterceptor;
import io.dropwizard.jackson.Jackson;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.junit.Rule;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class JsonRequestLogFactoryTest {

  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();

  @Rule
  public ConsoleInterceptor interceptor = new ConsoleInterceptor();

  @Test
  public void build() throws Exception {
    final JsonRequestLogFactory logFactory = new JsonRequestLogFactory();

    final RequestLog requestLog = logFactory.build("test");
    assertNotNull(requestLog);

    final String expectedVerb = "GET";
    final String requestUri = "/test";
    final String expectedProtocol = "HTTP/1.1";
    final int expectedStatus = 419;

    final Request request = mock(Request.class);
    when(request.getRequestURI()).thenReturn(requestUri);
    when(request.getMethod()).thenReturn(expectedVerb);
    when(request.getProtocol()).thenReturn(expectedProtocol);
    when(request.getAttributeNames()).thenReturn(Collections.enumeration(ImmutableList.of()));
    final Response response = mock(Response.class);
    when(response.getHttpFields()).thenReturn(new HttpFields());
    when(response.getStatus()).thenReturn(expectedStatus);

    requestLog.log(request, response);

    await().atMost(5, TimeUnit.SECONDS).until(interceptor.contains(s -> s.contains(requestUri)));

    final Optional<String> opt = interceptor.getLogs().stream()
        .filter(s -> s.contains(requestUri)).findFirst();

    assertTrue(opt.isPresent());
    final Map<String, String> accessMap = OBJECT_MAPPER.readValue(opt.get(),
        new TypeReference<Map<String, String>>() {});

    assertEquals(expectedVerb, accessMap.get("verb"));
    assertEquals(requestUri, accessMap.get("request"));
    assertEquals(expectedProtocol, accessMap.get("protocol"));
    assertEquals(expectedStatus, Integer.parseInt(accessMap.get("response")));
  }
}