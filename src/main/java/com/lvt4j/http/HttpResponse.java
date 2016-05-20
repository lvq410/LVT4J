package com.lvt4j.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author LV
 */
public class HttpResponse implements HttpServletResponse {

    // the default buffer size
    private static final int BUFFER_SIZE = 1024;

    private Socket socket;
    private HttpRequest request;
    private OutputStream output;
    private PrintWriter writer;

    protected byte[] buffer = new byte[BUFFER_SIZE];

    protected int bufferCount = 0;

    /**
     * Has this response been committed yet?
     */
    protected boolean committed = false;

    /**
     * The actual number of bytes written to this Response.
     */
    protected int contentCount = 0;

    /**
     * The content length associated with this Response.
     */
    protected int contentLength = -1;
    
    /**
     * The content type associated with this Response.
     */
    protected String contentType = null;

    /**
     * The character encoding associated with this Response.
     */
    protected String encoding = null;

    /**
     * The set of Cookies associated with this Response.
     */
    protected ArrayList<Cookie> cookies = new ArrayList<Cookie>();

    /**
     * The HTTP headers explicitly added via addHeader(), but not including
     * those to be added with setContentLength(), setContentType(), and so on.
     * This collection is keyed by the header name, and the elements are
     * ArrayLists containing the associated values that have been set.
     */
    protected HashMap<String,List<String>> headers = new HashMap<String,List<String>>();

    /**
     * The date format we will use for creating date headers.
     */
    protected final SimpleDateFormat format = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    /**
     * The error message set by <code>sendError()</code>.
     */
    protected String message = getStatusMessage(HttpServletResponse.SC_OK);

    /**
     * The HTTP status code associated with this Response.
     */
    protected int status = HttpServletResponse.SC_OK;

    public HttpResponse(Socket socket, HttpRequest request) throws IOException {
        this.request = request;
        this.socket = socket;
        this.output = socket.getOutputStream();
    }

    /**
     * call this method to send headers and response to the output
     */
    public void finishResponse() {
        // sendHeaders();
        // Flush and close the appropriate output mechanism
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    protected String getProtocol() {
        return request.getProtocol();
    }

    /**
     * Returns a default status message for the specified HTTP status code.
     *
     * @param status
     *            The status code for which a message is desired
     */
    protected String getStatusMessage(int status) {
        switch (status) {
            case SC_OK:
                return ("OK");
            case SC_ACCEPTED:
                return ("Accepted");
            case SC_BAD_GATEWAY:
                return ("Bad Gateway");
            case SC_BAD_REQUEST:
                return ("Bad Request");
            case SC_CONFLICT:
                return ("Conflict");
            case SC_CONTINUE:
                return ("Continue");
            case SC_CREATED:
                return ("Created");
            case SC_EXPECTATION_FAILED:
                return ("Expectation Failed");
            case SC_FORBIDDEN:
                return ("Forbidden");
            case SC_GATEWAY_TIMEOUT:
                return ("Gateway Timeout");
            case SC_GONE:
                return ("Gone");
            case SC_HTTP_VERSION_NOT_SUPPORTED:
                return ("HTTP Version Not Supported");
            case SC_INTERNAL_SERVER_ERROR:
                return ("Internal Server Error");
            case SC_LENGTH_REQUIRED:
                return ("Length Required");
            case SC_METHOD_NOT_ALLOWED:
                return ("Method Not Allowed");
            case SC_MOVED_PERMANENTLY:
                return ("Moved Permanently");
            case SC_MOVED_TEMPORARILY:
                return ("Moved Temporarily");
            case SC_MULTIPLE_CHOICES:
                return ("Multiple Choices");
            case SC_NO_CONTENT:
                return ("No Content");
            case SC_NON_AUTHORITATIVE_INFORMATION:
                return ("Non-Authoritative Information");
            case SC_NOT_ACCEPTABLE:
                return ("Not Acceptable");
            case SC_NOT_FOUND:
                return ("Not Found");
            case SC_NOT_IMPLEMENTED:
                return ("Not Implemented");
            case SC_NOT_MODIFIED:
                return ("Not Modified");
            case SC_PARTIAL_CONTENT:
                return ("Partial Content");
            case SC_PAYMENT_REQUIRED:
                return ("Payment Required");
            case SC_PRECONDITION_FAILED:
                return ("Precondition Failed");
            case SC_PROXY_AUTHENTICATION_REQUIRED:
                return ("Proxy Authentication Required");
            case SC_REQUEST_ENTITY_TOO_LARGE:
                return ("Request Entity Too Large");
            case SC_REQUEST_TIMEOUT:
                return ("Request Timeout");
            case SC_REQUEST_URI_TOO_LONG:
                return ("Request URI Too Long");
            case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
                return ("Requested Range Not Satisfiable");
            case SC_RESET_CONTENT:
                return ("Reset Content");
            case SC_SEE_OTHER:
                return ("See Other");
            case SC_SERVICE_UNAVAILABLE:
                return ("Service Unavailable");
            case SC_SWITCHING_PROTOCOLS:
                return ("Switching Protocols");
            case SC_UNAUTHORIZED:
                return ("Unauthorized");
            case SC_UNSUPPORTED_MEDIA_TYPE:
                return ("Unsupported Media Type");
            case SC_USE_PROXY:
                return ("Use Proxy");
            case 207: // WebDAV
                return ("Multi-Status");
            case 422: // WebDAV
                return ("Unprocessable Entity");
            case 423: // WebDAV
                return ("Locked");
            case 507: // WebDAV
                return ("Insufficient Storage");
            default:
                return ("HTTP Response Status " + status);
        }
    }

    public OutputStream getStream() {
        return this.output;
    }

    /**
     * Send the HTTP response headers, if this has not already occurred.
     */
    protected void sendHeaders() throws IOException {
        if (isCommitted())
            return;
        // Prepare a suitable output writer
        OutputStreamWriter osr = null;
        try {
            osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            osr = new OutputStreamWriter(getStream());
        }
        final PrintWriter outputWriter = new PrintWriter(osr);
        // Send the "Status:" header
        outputWriter.print(this.getProtocol());
        outputWriter.print(" ");
        outputWriter.print(status);
        if (message != null) {
            outputWriter.print(" ");
            outputWriter.print(message);
        }
        outputWriter.print("\r\n");
        // Send the content-length and content-type headers (if any)
        if (getContentType() != null) {
            outputWriter.print("Content-Type: " + getContentType() + "\r\n");
        }
        if (contentLength>=0) {
            outputWriter.print("Content-Length: " + contentLength + "\r\n");
        }
        // Send all specified headers (if any)
        synchronized (headers) {
            Iterator<String> names = headers.keySet().iterator();
            while (names.hasNext()) {
                String name = names.next();
                List<String> values = headers.get(name);
                Iterator<String> items = values.iterator();
                while (items.hasNext()) {
                    String value = (String) items.next();
                    outputWriter.print(name);
                    outputWriter.print(": ");
                    outputWriter.print(value);
                    outputWriter.print("\r\n");
                }
            }
        }
        // Add the session ID cookie if necessary
        /*
         * HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
         * HttpSession session = hreq.getSession(false); if ((session != null)
         * && session.isNew() && (getContext() != null) &&
         * getContext().getCookies()) { Cookie cookie = new Cookie("JSESSIONID",
         * session.getId()); cookie.setMaxAge(-1); String contextPath = null; if
         * (context != null) contextPath = context.getPath(); if ((contextPath
         * != null) && (contextPath.length() > 0)) cookie.setPath(contextPath);
         * else cookie.setPath("/"); if (hreq.isSecure())
         * cookie.setSecure(true); addCookie(cookie); }
         */
        // Send all specified cookies (if any)
        synchronized (cookies) {
            Iterator<Cookie> items = cookies.iterator();
            while (items.hasNext()) {
                Cookie cookie = (Cookie) items.next();
                outputWriter.print(CookieTools.getCookieHeaderName(cookie));
                outputWriter.print(": ");
                outputWriter.print(CookieTools.getCookieHeaderValue(cookie));
                outputWriter.print("\r\n");
            }
        }

        // Send a terminating blank line to mark the end of the headers
        outputWriter.print("\r\n");
        outputWriter.flush();

        committed = true;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void write(int b) throws IOException {
        sendHeaders();
        output.write(b);
    }

    public void write(byte b[]) throws IOException {
        sendHeaders();
        output.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        sendHeaders();
        output.write(b, off, len);
    }

    /** implementation of HttpServletResponse */

    public void addCookie(Cookie cookie) {
        if (isCommitted())
            return;
        // if (included)
        // return; // Ignore any call from an included servlet
        synchronized (cookies) {
            cookies.add(cookie);
        }
    }

    public void addDateHeader(String name, long value) {
        if (isCommitted())
            return;
        addHeader(name, format.format(new Date(value)));
    }

    public void addHeader(String name, String value) {
        if (isCommitted())
            return;
        synchronized (headers) {
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<String>();
                headers.put(name, values);
            }
            values.add(value);
        }
    }

    public void addIntHeader(String name, int value) {
        if (isCommitted())
            return;
        addHeader(name, "" + value);
    }

    public boolean containsHeader(String name) {
        synchronized (headers) {
            return (headers.get(name) != null);
        }
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    public String encodeURL(String url) {
        return null;
    }

    public void flushBuffer() throws IOException {
        // committed = true;
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            } finally {
                bufferCount = 0;
            }
        }
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        if (encoding == null)
            return ("ISO-8859-1");
        else
            return (encoding);
    }

    public Locale getLocale() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        sendHeaders();
        ResponseStream newStream = new ResponseStream(this);
        newStream.setCommit(false);
        OutputStreamWriter osr = new OutputStreamWriter(newStream,
                getCharacterEncoding());
        writer = new ResponseWriter(osr);
        return writer;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * Has the output of this response already been committed?
     */
    public boolean isCommitted() {
        return (committed);
    }

    public void reset() {}

    public void resetBuffer() {}

    public void sendError(int sc) throws IOException {}

    public void sendError(int sc, String message) throws IOException {}

    public void sendRedirect(String location) throws IOException {}

    public void setBufferSize(int size) {}

    public void setContentLength(int length) {
        if (isCommitted())
            return;
        this.contentLength = length;
    }

    public void setContentType(String type) {
        this.contentType = type;
    }

    public void setDateHeader(String name, long value) {
        if (isCommitted())
            return;
        setHeader(name, format.format(new Date(value)));
    }

    public void setHeader(String name, String value) {
        if (isCommitted())
            return;
        List<String> values = new ArrayList<String>();
        values.add(value);
        synchronized (headers) {
            headers.put(name, values);
        }
        String match = name.toLowerCase();
        if (match.equals("content-length")) {
            int contentLength = -1;
            try {
                contentLength = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                ;
            }
            if (contentLength >= 0)
                setContentLength(contentLength);
        } else if (match.equals("content-type")) {
            setContentType(value);
        }
    }

    public void setIntHeader(String name, int value) {
        if (isCommitted())
            return;
        setHeader(name, "" + value);
    }

    public void setLocale(Locale locale) {
        if (isCommitted())
            return;
        String language = locale.getLanguage();
        if ((language != null) && (language.length() > 0)) {
            String country = locale.getCountry();
            StringBuffer value = new StringBuffer(language);
            if ((country != null) && (country.length() > 0)) {
                value.append('-');
                value.append(country);
            }
            setHeader("Content-Language", value.toString());
        }
    }

    public void setStatus(int sc) {}

    public void setStatus(int sc, String message) {}

    @Override
    public void setCharacterEncoding(String arg0) {
    }

	@Override
	public void setContentLengthLong(long arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

}

class CookieTools {

    /**
     * Return the header name to set the cookie, based on cookie version
     */
    public static String getCookieHeaderName(Cookie cookie) {
        int version = cookie.getVersion();

        if (version == 1) {
            return "Set-Cookie2";
        } else {
            return "Set-Cookie";
        }
    }

    /**
     * Return the header value used to set this cookie
     * 
     * @deprecated Use StringBuffer version
     */
    public static String getCookieHeaderValue(Cookie cookie) {
        StringBuffer buf = new StringBuffer();
        getCookieHeaderValue(cookie, buf);
        return buf.toString();
    }

    /**
     * Return the header value used to set this cookie
     */
    public static void getCookieHeaderValue(Cookie cookie, StringBuffer buf) {
        int version = cookie.getVersion();

        // this part is the same for all cookies

        String name = cookie.getName(); // Avoid NPE on malformed cookies
        if (name == null)
            name = "";
        String value = cookie.getValue();
        if (value == null)
            value = "";

        buf.append(name);
        buf.append("=");
        maybeQuote(version, buf, value);

        // add version 1 specific information
        if (version == 1) {
            // Version=1 ... required
            buf.append(";Version=1");

            // Comment=comment
            if (cookie.getComment() != null) {
                buf.append(";Comment=");
                maybeQuote(version, buf, cookie.getComment());
            }
        }

        // add domain information, if present

        if (cookie.getDomain() != null) {
            buf.append(";Domain=");
            maybeQuote(version, buf, cookie.getDomain());
        }

        // Max-Age=secs/Discard ... or use old "Expires" format
        if (cookie.getMaxAge() >= 0) {
            if (version == 0) {
                buf.append(";Expires=");
                if (cookie.getMaxAge() == 0)
                    DateTool.oldCookieFormat.format(new Date(10000), buf,
                            new FieldPosition(0));
                else
                    DateTool.oldCookieFormat.format(
                            new Date(System.currentTimeMillis()
                                    + cookie.getMaxAge() * 1000L), buf,
                            new FieldPosition(0));
            } else {
                buf.append(";Max-Age=");
                buf.append(cookie.getMaxAge());
            }
        } else if (version == 1)
            buf.append(";Discard");

        // Path=path
        if (cookie.getPath() != null) {
            buf.append(";Path=");
            maybeQuote(version, buf, cookie.getPath());
        }

        // Secure
        if (cookie.getSecure()) {
            buf.append(";Secure");
        }
    }

    static void maybeQuote(int version, StringBuffer buf, String value) {
        if (version == 0 || isToken(value))
            buf.append(value);
        else {
            buf.append('"');
            buf.append(value);
            buf.append('"');
        }
    }

    //
    // from RFC 2068, token special case characters
    //
    private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";

    /*
     * Return true iff the string counts as an HTTP/1.1 "token".
     */
    private static boolean isToken(String value) {
        int len = value.length();

        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
                return false;
        }
        return true;
    }

}

class DateTool {

    /**
     * US locale - all HTTP dates are in english
     */
    public final static Locale LOCALE_US = Locale.US;

    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    public final static TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

    /**
     * format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT"
     */
    public final static String RFC1123_PATTERN = "EEE, dd MMM yyyyy HH:mm:ss z";

    // format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT"
    private final static String rfc1036Pattern = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    // format for C asctime() date string -- "Sun Nov  6 08:49:37 1994"
    private final static String asctimePattern = "EEE MMM d HH:mm:ss yyyyy";

    /**
     * Pattern used for old cookies
     */
    public final static String OLD_COOKIE_PATTERN = "EEE, dd-MMM-yyyy HH:mm:ss z";

    /**
     * DateFormat to be used to format dates
     */
    public final static DateFormat rfc1123Format = new SimpleDateFormat(
            RFC1123_PATTERN, LOCALE_US);

    /**
     * DateFormat to be used to format old netscape cookies
     */
    public final static DateFormat oldCookieFormat = new SimpleDateFormat(
            OLD_COOKIE_PATTERN, LOCALE_US);

    public final static DateFormat rfc1036Format = new SimpleDateFormat(
            rfc1036Pattern, LOCALE_US);

    public final static DateFormat asctimeFormat = new SimpleDateFormat(
            asctimePattern, LOCALE_US);

    static {
        rfc1123Format.setTimeZone(GMT_ZONE);
        oldCookieFormat.setTimeZone(GMT_ZONE);
        rfc1036Format.setTimeZone(GMT_ZONE);
        asctimeFormat.setTimeZone(GMT_ZONE);
    }

}

class ResponseStream extends ServletOutputStream {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a servlet output stream associated with the specified Request.
     *
     * @param response
     *            The associated response
     */
    public ResponseStream(HttpResponse response) {

        super();
        closed = false;
        commit = false;
        count = 0;
        this.response = response;
        // this.stream = response.getStream();

    }

    // ----------------------------------------------------- Instance Variables

    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;

    /**
     * Should we commit the response when we are flushed?
     */
    protected boolean commit = false;

    /**
     * The number of bytes which have already been written to this stream.
     */
    protected int count = 0;

    /**
     * The content length past which we will not write, or -1 if there is no
     * defined content length.
     */
    protected int length = -1;

    /**
     * The Response with which this input stream is associated.
     */
    protected HttpResponse response = null;

    /**
     * The underlying output stream to which we should write data.
     */
    protected OutputStream stream = null;

    // ------------------------------------------------------------- Properties

    /**
     * [Package Private] Return the "commit response on flush" flag.
     */
    public boolean getCommit() {

        return (this.commit);

    }

    /**
     * [Package Private] Set the "commit response on flush" flag.
     *
     * @param commit
     *            The new commit flag
     */
    public void setCommit(boolean commit) {

        this.commit = commit;

    }

    // --------------------------------------------------------- Public Methods

    /**
     * Close this output stream, causing any buffered data to be flushed and any
     * further output data to throw an IOException.
     */
    public void close() throws IOException {
        if (closed)
            throw new IOException("responseStream.close.closed");
        response.flushBuffer();
        closed = true;
    }

    /**
     * Flush any buffered data for this output stream, which also causes the
     * response to be committed.
     */
    public void flush() throws IOException {
        if (closed)
            throw new IOException("responseStream.flush.closed");
        if (commit)
            response.flushBuffer();

    }

    /**
     * Write the specified byte to our output stream.
     *
     * @param b
     *            The byte to be written
     * @exception IOException
     *                if an input/output error occurs
     */
    public void write(int b) throws IOException {

        if (closed)
            throw new IOException("responseStream.write.closed");

        if ((length > 0) && (count >= length))
            throw new IOException("responseStream.write.count");

        response.write(b);
        count++;

    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);

    }

    public void write(byte b[], int off, int len) throws IOException {
        if (closed)
            throw new IOException("responseStream.write.closed");

        int actual = len;
        if ((length > 0) && ((count + len) >= length))
            actual = length - count;
        response.write(b, off, actual);
        count += actual;
        if (actual < len)
            throw new IOException("responseStream.write.count");

    }

    // -------------------------------------------------------- Package Methods

    /**
     * Has this response stream been closed?
     */
    boolean closed() {
        return (this.closed);

    }

    /**
     * Reset the count of bytes written to this stream to zero.
     */
    void reset() {

        count = 0;

    }

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
		// TODO Auto-generated method stub
		
	}

}

class ResponseWriter extends PrintWriter {

    public ResponseWriter(OutputStreamWriter writer) {
        super(writer);
    }

    public void print(boolean b) {
        super.print(b);
        super.flush();
    }

    public void print(char c) {
        super.print(c);
        super.flush();
    }

    public void print(char ca[]) {
        super.print(ca);
        super.flush();
    }

    public void print(double d) {
        super.print(d);
        super.flush();
    }

    public void print(float f) {
        super.print(f);
        super.flush();
    }

    public void print(int i) {
        super.print(i);
        super.flush();
    }

    public void print(long l) {
        super.print(l);
        super.flush();
    }

    public void print(Object o) {
        super.print(o);
        super.flush();
    }

    public void print(String s) {
        super.print(s);
        super.flush();
    }

    public void println() {
        super.println();
        super.flush();
    }

    public void println(boolean b) {
        super.println(b);
        super.flush();
    }

    public void println(char c) {
        super.println(c);
        super.flush();
    }

    public void println(char ca[]) {
        super.println(ca);
        super.flush();
    }

    public void println(double d) {
        super.println(d);
        super.flush();
    }

    public void println(float f) {
        super.println(f);
        super.flush();
    }

    public void println(int i) {
        super.println(i);
        super.flush();
    }

    public void println(long l) {
        super.println(l);
        super.flush();
    }

    public void println(Object o) {
        super.println(o);
        super.flush();
    }

    public void println(String s) {
        super.println(s);
        super.flush();
    }

    public void write(char c) {
        super.write(c);
        super.flush();
    }

    public void write(char ca[]) {
        super.write(ca);
        super.flush();
    }

    public void write(char ca[], int off, int len) {
        super.write(ca, off, len);
        super.flush();
    }

    public void write(String s) {
        super.write(s);
        super.flush();
    }

    public void write(String s, int off, int len) {
        super.write(s, off, len);
        super.flush();
    }
}
