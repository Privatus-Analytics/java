package com.privatusanalytics.spring;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Holds back the body of uncompressed 200 {@code text/html} responses so the filter can add the
 * tag. The choice is made when the app first asks for the output stream or writer. Every other
 * response is passed straight through.
 */
final class HtmlBufferingResponse extends HttpServletResponseWrapper {
  private enum Mode {
    UNDECIDED,
    BUFFER,
    PASS
  }

  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(8192);
  private Mode mode = Mode.UNDECIDED;
  private BufferStream stream;
  private PrintWriter writer;
  private Long contentLength;

  HtmlBufferingResponse(HttpServletResponse response) {
    super(response);
  }

  private void decide() {
    if (mode != Mode.UNDECIDED) {
      return;
    }
    String type = getContentType();
    boolean html = type != null && type.toLowerCase(Locale.ROOT).startsWith("text/html");
    if (html && getStatus() == SC_OK && !isEncoded() && !isCommitted()) {
      mode = Mode.BUFFER;
    } else {
      mode = Mode.PASS;
      releaseContentLength();
    }
  }

  boolean isEncoded() {
    return getHeader("Content-Encoding") != null;
  }

  Charset charset() {
    String name = getCharacterEncoding();
    try {
      return name == null ? StandardCharsets.ISO_8859_1 : Charset.forName(name);
    } catch (IllegalArgumentException e) {
      return StandardCharsets.ISO_8859_1;
    }
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    decide();
    if (mode == Mode.PASS && stream == null) {
      return super.getOutputStream();
    }
    if (writer != null) {
      throw new IllegalStateException("getWriter() has already been called");
    }
    if (stream == null) {
      stream = new BufferStream();
    }
    return stream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    decide();
    if (mode == Mode.PASS && writer == null && stream == null) {
      return super.getWriter();
    }
    if (writer == null) {
      if (stream != null) {
        throw new IllegalStateException("getOutputStream() has already been called");
      }
      stream = new BufferStream();
      writer = new PrintWriter(new OutputStreamWriter(stream, charset()));
    }
    return writer;
  }

  @Override
  public void flushBuffer() throws IOException {
    if (mode == Mode.BUFFER) {
      if (writer != null) {
        writer.flush();
      }
      return;
    }
    super.flushBuffer();
  }

  @Override
  public void resetBuffer() {
    if (mode == Mode.BUFFER) {
      if (writer != null) {
        writer.flush();
      }
      buffer.reset();
    }
    super.resetBuffer();
  }

  @Override
  public void reset() {
    resetBuffer();
    contentLength = null;
    super.reset();
  }

  @Override
  public void sendError(int sc, String msg) throws IOException {
    dropBuffer();
    super.sendError(sc, msg);
  }

  @Override
  public void sendError(int sc) throws IOException {
    dropBuffer();
    super.sendError(sc);
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    dropBuffer();
    super.sendRedirect(location);
  }

  private void dropBuffer() {
    buffer.reset();
    contentLength = null;
    if (mode == Mode.UNDECIDED) {
      mode = Mode.PASS;
    }
  }

  @Override
  public void setContentLength(int len) {
    setContentLengthLong(len);
  }

  @Override
  public void setContentLengthLong(long len) {
    if (mode == Mode.PASS) {
      super.setContentLengthLong(len);
    } else {
      contentLength = len;
    }
  }

  @Override
  public void setHeader(String name, String value) {
    if (isContentLength(name) && mode != Mode.PASS) {
      contentLength = value == null ? null : Long.valueOf(value.trim());
    } else {
      super.setHeader(name, value);
    }
  }

  @Override
  public void addHeader(String name, String value) {
    if (isContentLength(name) && mode != Mode.PASS) {
      contentLength = value == null ? null : Long.valueOf(value.trim());
    } else {
      super.addHeader(name, value);
    }
  }

  @Override
  public void setIntHeader(String name, int value) {
    if (isContentLength(name) && mode != Mode.PASS) {
      contentLength = (long) value;
    } else {
      super.setIntHeader(name, value);
    }
  }

  @Override
  public void addIntHeader(String name, int value) {
    if (isContentLength(name) && mode != Mode.PASS) {
      contentLength = (long) value;
    } else {
      super.addIntHeader(name, value);
    }
  }

  private static boolean isContentLength(String name) {
    return "Content-Length".equalsIgnoreCase(name);
  }

  private void releaseContentLength() {
    if (contentLength != null) {
      super.setContentLengthLong(contentLength);
      contentLength = null;
    }
  }

  /**
   * Gives up on adding the tag: writes what is held back and sends the rest straight through.
   *
   * @throws IOException when writing fails
   */
  void stopBuffering() throws IOException {
    if (mode == Mode.UNDECIDED) {
      mode = Mode.PASS;
      releaseContentLength();
      return;
    }
    if (mode != Mode.BUFFER) {
      return;
    }
    if (writer != null) {
      writer.flush();
    }
    mode = Mode.PASS;
    releaseContentLength();
    ServletOutputStream out = super.getOutputStream();
    buffer.writeTo(out);
    buffer.reset();
    stream.target = out;
  }

  /** Throws away anything held back, for example after an exception. */
  void discard() {
    dropBuffer();
    mode = Mode.PASS;
  }

  /**
   * Ends buffering.
   *
   * @return the held back body, or null when the body was passed straight through
   */
  byte[] finish() {
    if (mode == Mode.UNDECIDED) {
      mode = Mode.PASS;
      releaseContentLength();
      return null;
    }
    if (mode != Mode.BUFFER) {
      return null;
    }
    if (writer != null) {
      writer.flush();
    }
    mode = Mode.PASS;
    contentLength = null;
    return buffer.toByteArray();
  }

  private final class BufferStream extends ServletOutputStream {
    private OutputStream target = buffer;

    @Override
    public void write(int b) throws IOException {
      target.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      target.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      if (target != buffer) {
        target.flush();
      }
    }

    @Override
    public boolean isReady() {
      return !(target instanceof ServletOutputStream out) || out.isReady();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
      try {
        stopBuffering();
        ((ServletOutputStream) target).setWriteListener(listener);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
