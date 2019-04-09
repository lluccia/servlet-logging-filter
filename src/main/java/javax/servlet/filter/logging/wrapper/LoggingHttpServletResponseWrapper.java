package javax.servlet.filter.logging.wrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class LoggingHttpServletResponseWrapper extends HttpServletResponseWrapper {

	private final LoggingServletOutpuStream loggingServletOutpuStream = new LoggingServletOutpuStream();

	private final HttpServletResponse delegate;

	Map<String, String> headers = new HashMap<>();

	int status;

	public LoggingHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		delegate = response;
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return loggingServletOutpuStream;
	}

	@Override
	public PrintWriter getWriter() {
		return new PrintWriter(loggingServletOutpuStream.baos);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getContent() {
		try {
			String responseEncoding = delegate.getCharacterEncoding();
			return loggingServletOutpuStream.baos.toString(responseEncoding != null ? responseEncoding : UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			return "[UNSUPPORTED ENCODING]";
		}
	}

	public byte[] getContentAsBytes() {
		return loggingServletOutpuStream.baos.toByteArray();
	}

	@Override
	public void addHeader(String name, String value) {
		headers.put(name, value);
		super.addHeader(name, value);
	}

	@Override
	public void setHeader(String name, String value) {
		headers.put(name, value);
		super.setHeader(name, value);
	}

	public void setDateHeader(String name, long date) {
		headers.put(name, Long.toString(date));
		super.setDateHeader(name, date);
	}

	public void addDateHeader(String name, long date) {
		headers.put(name, Long.toString(date));
		super.addDateHeader(name, date);
	}
	
	public void setIntHeader(String name, int value) {
		headers.put(name, Integer.toString(value));
		super.setIntHeader(name, value);
	}

	public void addIntHeader(String name, int value) {
		headers.put(name, Integer.toString(value));
		super.addIntHeader(name, value);
	}

	@Override
	public void setStatus(int sc) {
		this.status = sc;
		super.setStatus(sc);
	}

	@Override
	public void setStatus(int sc, String sm) {
		this.status = sc;
		super.setStatus(sc, sm);
	}

	public int getStatus() {
		return status;
	}

	private class LoggingServletOutpuStream extends ServletOutputStream {

		private ByteArrayOutputStream baos = new ByteArrayOutputStream();

		@Override
		public void write(int b) {
			baos.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			baos.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) {
			baos.write(b, off, len);
		}
	}
}
