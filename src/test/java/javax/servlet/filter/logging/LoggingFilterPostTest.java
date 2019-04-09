package javax.servlet.filter.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import javax.servlet.*;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.MarkerFactory.getMarker;

@SuppressWarnings({"squid:S00100", "squid:S00112", "squid:S3457"})
@ExtendWith({MockitoExtension.class})
public class LoggingFilterPostTest {

	@InjectMocks
	private LoggingFilter loggingFilter = new LoggingFilter();

	@Mock
	private Logger logger;

	private MockHttpServletRequest httpServletRequest;

	private MockHttpServletResponse httpServletResponse;

	private MockFilterChain filterChain;

	@BeforeEach
	public void setUp() {

		httpServletRequest = new MockHttpServletRequest("POST", "http://localhost:8080/test");
		httpServletRequest.addHeader("Accept", "application/json");
		httpServletRequest.addParameter("field1", "1000");
		httpServletRequest.addParameter("field2", "2000");
		httpServletRequest.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);

		httpServletResponse = new MockHttpServletResponse();
		httpServletResponse.setContentType(MediaType.TEXT_PLAIN_VALUE);

		filterChain = new MockFilterChain(new HttpRequestHandlerServlet(), new TestFilter());
	}

	@Test
	public void testDoFilter_Full() throws Exception {

		when(logger.isDebugEnabled()).thenReturn(true);
		when(logger.isTraceEnabled()).thenReturn(true);

		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

		verify(logger).isDebugEnabled();
		verify(logger).debug(getMarker("REQUEST"), "REQUEST: {\"sender\":\"127.0.0.1\",\"method\":\"POST\",\"path\":\"http://localhost:8080/test\",\"headers\":{\"Accept\":\"application/json\",\"Content-Type\":\"application/x-www-form-urlencoded\"},\"body\":\"field1=1000&field2=2000\"}");

		// not able to capture mocked response status and headers in servlet 2.5
		verify(logger).debug(getMarker("RESPONSE"), "RESPONSE: {\"status\":0,\"body\":\"Test response body\"}");
	}

	@Test
	public void testDoFilter_MarkeredOnly() throws Exception {

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.addInitParameter("disablePrefix", "true");
		loggingFilter.init(filterConfig);

		when(logger.isDebugEnabled()).thenReturn(true);
		when(logger.isTraceEnabled()).thenReturn(true);

		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

		verify(logger).isDebugEnabled();
		verify(logger).debug(getMarker("REQUEST"), "{\"sender\":\"127.0.0.1\",\"method\":\"POST\",\"path\":\"http://localhost:8080/test\",\"headers\":{\"Accept\":\"application/json\",\"Content-Type\":\"application/x-www-form-urlencoded\"},\"body\":\"field1=1000&field2=2000\"}");

		// not able to capture mocked response status and headers in servlet 2.5
		verify(logger).debug(getMarker("RESPONSE"), "{\"status\":0,\"body\":\"Test response body\"}");
	}

	@Test
	public void testDoFilter_JsonOnly() throws Exception {

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.addInitParameter("disablePrefix", "true");
		filterConfig.addInitParameter("disableMarker", "true");
		loggingFilter.init(filterConfig);

		when(logger.isDebugEnabled()).thenReturn(true);
		when(logger.isTraceEnabled()).thenReturn(true);

		loggingFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

		verify(logger).isDebugEnabled();
		verify(logger).debug("{\"sender\":\"127.0.0.1\",\"method\":\"POST\",\"path\":\"http://localhost:8080/test\",\"headers\":{\"Accept\":\"application/json\",\"Content-Type\":\"application/x-www-form-urlencoded\"},\"body\":\"field1=1000&field2=2000\"}");

		// not able to capture mocked response status and headers in servlet 2.5
		verify(logger).debug("{\"status\":0,\"body\":\"Test response body\"}");
	}

	private class TestFilter implements Filter {

		@Override
		public void init(FilterConfig filterConfig) {
			// not used
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			response.getOutputStream().write("Test response body".getBytes());
		}

		@Override
		public void destroy() {
			// not used
		}
	}
}