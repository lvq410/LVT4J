package com.lvt4j.http;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.lvt4j.basic.TLog;

/**
 * @author LV
 */
public class HttpRequest implements HttpServletRequest {

    private String contentType;

    private int contentLength;

    private InputStream input;

    private String method;

    private String protocol;

    private String queryString;

    private String requestURI;

    private String serverName;

    private int serverPort;

    private Socket socket;

    private boolean requestedSessionCookie;

    private String requestedSessionId;

    private boolean requestedSessionURL;

    /**
     * The request attributes for this request.
     */
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * The authorization credentials sent with this Request.
     */
    protected String authorization = null;

    /**
     * The context path for this request.
     */
    protected String contextPath = "";

    /**
     * The set of cookies associated with this Request.
     */
    protected ArrayList<Cookie> cookies = new ArrayList<Cookie>();

    /**
     * An empty collection to use for returning empty Enumerations. Do not add
     * any elements to this collection!
     */
    protected static ArrayList<String> empty = new ArrayList<String>();

    /**
     * The set of SimpleDateFormat formats to use in getDateHeader().
     */
    protected SimpleDateFormat formats[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    };

    /**
     * The HTTP headers associated with this Request, keyed by name. The values
     * are ArrayLists of the corresponding header values.
     */
    protected Map<String, List<String>> headers = new HashMap<String, List<String>>();

    /**
     * The parsed parameters for this request. This is populated only if
     * parameter information is requested via one of the
     * <code>getParameter()</code> family of method calls. The key is the
     * parameter name, while the value is a String array of values for this
     * parameter.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong> - Once the parameters for a
     * particular request are parsed and stored here, they are not modified.
     * Therefore, application level access to the parameters need not be
     * synchronized.
     */
    protected ParameterMap<String, String[]> parameters = null;

    /**
     * Have the parameters for this request been parsed yet?
     */
    protected boolean parsed = false;

    protected String pathInfo = null;

    /**
     * The reader that has been returned by <code>getReader</code>, if any.
     */
    protected BufferedReader reader = null;

    /**
     * The ServletInputStream that has been returned by
     * <code>getInputStream()</code>, if any.
     */
    protected ServletInputStream stream = null;

    public HttpRequest(Socket socket) throws IOException, ServletException {
        this.socket = socket;
        SocketInputStream in = new SocketInputStream(socket.getInputStream(),
                2048);
        input = in;
        parseRequest(in);
        parseHeaders(in);
    }

    public void addHeader(String name, String value) {
        name = name.toLowerCase();
        synchronized (headers) {
            List<String> values = headers.get(name);
            if (values == null) {
                values = new ArrayList<String>();
                headers.put(name, values);
            }
            values.add(value);
        }
    }

    /**
     * Parse the parameters of this request, if it has not already occurred. If
     * parameters are present in both the query string and the request content,
     * they are merged.
     */
    protected void parseParameters() {
        if (parsed)
            return;
        ParameterMap<String, String[]> results = parameters;
        if (results == null)
            results = new ParameterMap<String, String[]>();
        results.setLocked(false);
        String encoding = getCharacterEncoding();
        if (encoding == null)
            encoding = "ISO-8859-1";

        // Parse any parameters specified in the query string
        String queryString = getQueryString();
        try {
            RequestUtil.parseParameters(results, queryString, encoding);
        } catch (UnsupportedEncodingException e) {
            ;
        }

        // Parse any parameters specified in the input stream
        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        if ("POST".equals(getMethod()) && (getContentLength() > 0)
                && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength();
                int len = 0;
                byte buf[] = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0) {
                        break;
                    }
                    len += next;
                }
                is.close();
                if (len < max) {
                    throw new RuntimeException("Content length mismatch");
                }
                RequestUtil.parseParameters(results, buf, encoding);
            } catch (UnsupportedEncodingException ue) {
                ;
            } catch (IOException e) {
                throw new RuntimeException("Content read fail");
            }
        }

        // Store the final results
        results.setLocked(true);
        parsed = true;
        parameters = results;
    }

    public void addCookie(Cookie cookie) {
        synchronized (cookies) {
            cookies.add(cookie);
        }
    }

    /**
     * Create and return a ServletInputStream to read the content associated
     * with this Request. The default implementation creates an instance of
     * RequestStream associated with this request, but this can be overridden if
     * necessary.
     *
     * @exception IOException
     *                if an input/output error occurs
     */
    public ServletInputStream createInputStream() throws IOException {
        return (new RequestStream(this));
    }

    public InputStream getStream() {
        return input;
    }

    public void setContentLength(int length) {
        this.contentLength = length;
    }

    public void setContentType(String type) {
        this.contentType = type;
    }

    public void setContextPath(String path) {
        if (path == null)
            this.contextPath = "";
        else
            this.contextPath = path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPathInfo(String path) {
        this.pathInfo = path;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name
     *            The server name
     */
    public void setServerName(String name) {
        this.serverName = name;
    }

    /**
     * Set the port number of the server to process this request.
     *
     * @param port
     *            The server port
     */
    public void setServerPort(int port) {
        this.serverPort = port;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie. This is normally called by the HTTP
     * Connector, when it parses the request headers.
     *
     * @param flag
     *            The new flag
     */
    public void setRequestedSessionCookie(boolean flag) {
        this.requestedSessionCookie = flag;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    public void setRequestedSessionURL(boolean flag) {
        requestedSessionURL = flag;
    }

    /* implementation of the HttpServletRequest */
    public Object getAttribute(String name) {
        synchronized (attributes) {
            return (attributes.get(name));
        }
    }

    public Enumeration<String> getAttributeNames() {
        synchronized (attributes) {
            return (new Enumerator<String>(attributes.keySet()));
        }
    }

    public String getAuthType() {
        return null;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Cookie[] getCookies() {
        synchronized (cookies) {
            if (cookies.size() < 1)
                return (null);
            Cookie results[] = new Cookie[cookies.size()];
            return ((Cookie[]) cookies.toArray(results));
        }
    }

    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null)
            return (-1L);

        // Work around a bug in SimpleDateFormat in pre-JDK1.2b4
        // (Bug Parade bug #4106807)
        value += " ";

        // Attempt to convert the date header in a variety of formats
        for (int i = 0; i < formats.length; i++) {
            try {
                Date date = formats[i].parse(value);
                return (date.getTime());
            } catch (ParseException e) {
                ;
            }
        }
        throw new IllegalArgumentException(value);
    }

    public String getHeader(String name) {
        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList<String> values = (ArrayList<String>) headers.get(name);
            if (values != null)
                return ((String) values.get(0));
            else
                return null;
        }
    }

    public Enumeration<String> getHeaderNames() {
        synchronized (headers) {
            return (new Enumerator<String>(headers.keySet()));
        }
    }

    public Enumeration<String> getHeaders(String name) {
        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList<String> values = (ArrayList<String>) headers.get(name);
            if (values != null)
                return (new Enumerator<String>(values));
            else
                return (new Enumerator<String>(empty));
        }
    }

    public ServletInputStream getInputStream() throws IOException {
        if (reader != null)
            throw new IllegalStateException("getInputStream has been called");

        if (stream == null)
            stream = createInputStream();
        return (stream);
    }

    public int getIntHeader(String name) {
        String value = getHeader(name);
        if (value == null)
            return (-1);
        else
            return (Integer.parseInt(value));
    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration<Locale> getLocales() {
        return null;
    }

    public String getMethod() {
        return method;
    }

    public String getParameter(String name) {
        parseParameters();
        String values[] = (String[]) parameters.get(name);
        if (values != null)
            return (values[0]);
        else
            return (null);
    }

    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return (this.parameters);
    }

    public Enumeration<String> getParameterNames() {
        parseParameters();
        return (new Enumerator<String>(parameters.keySet()));
    }

    public String[] getParameterValues(String name) {
        parseParameters();
        String values[] = (String[]) parameters.get(name);
        if (values != null)
            return (values);
        else
            return null;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getQueryString() {
        return queryString;
    }

    public BufferedReader getReader() throws IOException {
        if (stream != null)
            throw new IllegalStateException("getInputStream has been called.");
        if (reader == null) {
            String encoding = getCharacterEncoding();
            if (encoding == null)
                encoding = "ISO-8859-1";
            InputStreamReader isr = new InputStreamReader(createInputStream(),
                    encoding);
            reader = new BufferedReader(isr);
        }
        return (reader);
    }

    public String getRealPath(String path) {
        return null;
    }

    public String getRemoteAddr() {
        return null;
    }

    public String getRemoteHost() {
        return null;
    }

    public String getRemoteUser() {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer(requestURI);
    }

    public HttpSession getSession() {
        return null;
    }

    public HttpSession getSession(boolean create) {
        return null;
    }

    public String getServletPath() {
        return null;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public boolean isSecure() {
        return false;
    }

    public boolean isUserInRole(String role) {
        return false;
    }

    public void removeAttribute(String attribute) {}

    public void setAttribute(String key, Object value) {}

    /**
     * Set the authorization credentials sent with this request.
     *
     * @param authorization
     *            The new authorization credentials
     */
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setCharacterEncoding(String encoding)
            throws UnsupportedEncodingException {}

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    private void parseHeaders(SocketInputStream input) throws IOException,
            ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();

            // Read the next header
            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("Invalid HTTP header format");
                }
            }

            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            addHeader(name, value);
            // do something for some headers, ignore others.
            if (name.equals("cookie")) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("jsessionid")) {
                        // Override anything requested in the URL
                        if (!isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            setRequestedSessionId(cookies[i].getValue());
                            setRequestedSessionCookie(true);
                            setRequestedSessionURL(false);
                        }
                    }
                    addCookie(cookies[i]);
                }
            } else if (name.equals("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException(
                            "Invalid 'Content-Length' header");
                }
                setContentLength(n);
            } else if (name.equals("content-type")) {
                setContentType(value);
            }
        } // end while
    }

    private void parseRequest(SocketInputStream input) throws IOException,
            ServletException {

        // Parse the incoming request line
        HttpRequestLine requestLine = new HttpRequestLine();
        input.readRequestLine(requestLine);
        String method = new String(requestLine.method, 0, requestLine.methodEnd);
        String uri = null;
        String protocol = new String(requestLine.protocol, 0,
                requestLine.protocolEnd);

        // Validate the incoming request line
        if (method.length() < 1) {
            throw new ServletException("Missing HTTP request method");
        } else if (requestLine.uriEnd < 1) {
            throw new ServletException("Missing HTTP request URI");
        }
        // Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            setQueryString(new String(requestLine.uri, question + 1,
                    requestLine.uriEnd - question - 1));
            uri = new String(requestLine.uri, 0, question);
        } else {
            setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }

        // Checking for an absolute URI (with the HTTP protocol)
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // Parsing out protocol and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }

        // Parse any requested session ID out of the request URI
        String match = ";jsessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            } else {
                setRequestedSessionId(rest);
                rest = "";
            }
            setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
        } else {
            setRequestedSessionId(null);
            setRequestedSessionURL(false);
        }

        // Normalize URI (using String operations at the moment)
        String normalizedUri = normalize(uri);

        // Set the corresponding request properties
        setMethod(method);
        setProtocol(protocol);
        if (normalizedUri != null) {
            setRequestURI(normalizedUri);
        } else {
            setRequestURI(uri);
        }

        if (normalizedUri == null) {
            TLog.w("Invalid URI: " + uri);
        }
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents the
     * canonical version of the specified path after ".." and "." elements are
     * resolved out. If the specified path attempts to go outside the boundaries
     * of the current context (i.e. too many ".." path elements are present),
     * return <code>null</code> instead.
     *
     * @param path
     *            Path to be normalized
     */
    protected String normalize(String path) {
        if (path == null)
            return null;
        // Create a place for the normalized path
        String normalized = path;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0)
                || (normalized.indexOf("%2F") >= 0)
                || (normalized.indexOf("%2E") >= 0)
                || (normalized.indexOf("%5C") >= 0)
                || (normalized.indexOf("%2f") >= 0)
                || (normalized.indexOf("%2e") >= 0)
                || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index)
                    + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index)
                    + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2)
                    + normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);

    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionURL;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return requestedSessionURL;
    }

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}

final class HttpRequestLine {

    // -------------------------------------------------------------- Constants
    public static final int INITIAL_METHOD_SIZE = 8;

    public static final int INITIAL_URI_SIZE = 64;

    public static final int INITIAL_PROTOCOL_SIZE = 8;

    public static final int MAX_METHOD_SIZE = 1024;

    public static final int MAX_URI_SIZE = 32768;

    public static final int MAX_PROTOCOL_SIZE = 1024;

    // ----------------------------------------------------------- Constructors
    public HttpRequestLine() {
        this(new char[INITIAL_METHOD_SIZE], 0, new char[INITIAL_URI_SIZE], 0,
                new char[INITIAL_PROTOCOL_SIZE], 0);
    }

    public HttpRequestLine(char[] method, int methodEnd, char[] uri,
            int uriEnd, char[] protocol, int protocolEnd) {
        this.method = method;
        this.methodEnd = methodEnd;
        this.uri = uri;
        this.uriEnd = uriEnd;
        this.protocol = protocol;
        this.protocolEnd = protocolEnd;
    }

    // ----------------------------------------------------- Instance Variables
    public char[] method;

    public int methodEnd;

    public char[] uri;

    public int uriEnd;

    public char[] protocol;

    public int protocolEnd;

    // ------------------------------------------------------------- Properties

    // --------------------------------------------------------- Public Methods

    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
        methodEnd = 0;
        uriEnd = 0;
        protocolEnd = 0;
    }

    /**
     * Test if the uri includes the given char array.
     */
    public int indexOf(char[] buf) {
        return indexOf(buf, buf.length);
    }

    /**
     * Test if the value of the header includes the given char array.
     */
    public int indexOf(char[] buf, int end) {
        char firstChar = buf[0];
        int pos = 0;
        while (pos < uriEnd) {
            pos = indexOf(firstChar, pos);
            if (pos == -1)
                return -1;
            if ((uriEnd - pos) < end)
                return -1;
            for (int i = 0; i < end; i++) {
                if (uri[i + pos] != buf[i])
                    break;
                if (i == (end - 1))
                    return pos;
            }
            pos++;
        }
        return -1;
    }

    /**
     * Test if the value of the header includes the given string.
     */
    public int indexOf(String str) {
        return indexOf(str.toCharArray(), str.length());
    }

    /**
     * Returns the index of a character in the value.
     */
    public int indexOf(char c, int start) {
        for (int i = start; i < uriEnd; i++) {
            if (uri[i] == c)
                return i;
        }
        return -1;
    }

    // --------------------------------------------------------- Object Methods
    public boolean equals(Object obj) {
        return false;
    }

}

class SocketInputStream extends InputStream {

    // -------------------------------------------------------------- Constants
    private static final byte CR = (byte) '\r';

    private static final byte LF = (byte) '\n';

    private static final byte SP = (byte) ' ';

    private static final byte HT = (byte) '\t';

    private static final byte COLON = (byte) ':';

    /** Lower case offset. */
    private static final int LC_OFFSET = 'A' - 'a';

    /** Internal buffer. */
    protected byte buf[];

    /** Last valid byte. */
    protected int count;

    /** Position in the buffer. */
    protected int pos;

    /** Underlying input stream. */
    protected InputStream is;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a servlet input stream associated with the specified socket
     * input.
     *
     * @param is
     *            socket input stream
     * @param bufferSize
     *            size of the internal buffer
     */
    public SocketInputStream(InputStream is, int bufferSize) {

        this.is = is;
        buf = new byte[bufferSize];

    }

    // -------------------------------------------------------------- Variables

    // ----------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Public Methods

    /**
     * Read the request line, and copies it to the given buffer. This function
     * is meant to be used during the HTTP request header parsing. Do NOT
     * attempt to read the request body using it.
     *
     * @param requestLine
     *            Request line object
     * @throws IOException
     *             If an exception occurs during the underlying socket read
     *             operations, or if the given buffer is not big enough to
     *             accomodate the whole line.
     */
    public void readRequestLine(HttpRequestLine requestLine) throws IOException {

        // Recycling check
        if (requestLine.methodEnd != 0)
            requestLine.recycle();

        // Checking for a blank line
        int chr = 0;
        do { // Skipping CR or LF
            try {
                chr = read();
            } catch (IOException e) {
                chr = -1;
            }
        } while ((chr == CR) || (chr == LF));
        if (chr == -1)
            throw new EOFException("Couldn't read line");
        pos--;

        // Reading the method name

        int maxRead = requestLine.method.length;
        @SuppressWarnings("unused")
        int readStart = pos;
        int readCount = 0;

        boolean space = false;

        while (!space) {
            // if the buffer is full, extend it
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpRequestLine.MAX_METHOD_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(requestLine.method, 0, newBuffer, 0,
                            maxRead);
                    requestLine.method = newBuffer;
                    maxRead = requestLine.method.length;
                } else {
                    throw new IOException("Line too long");
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                int val = read();
                if (val == -1) {
                    throw new IOException("Couldn't read line");
                }
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == SP) {
                space = true;
            }
            requestLine.method[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }

        requestLine.methodEnd = readCount - 1;

        // Reading URI

        maxRead = requestLine.uri.length;
        readStart = pos;
        readCount = 0;

        space = false;

        boolean eol = false;

        while (!space) {
            // if the buffer is full, extend it
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpRequestLine.MAX_URI_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(requestLine.uri, 0, newBuffer, 0, maxRead);
                    requestLine.uri = newBuffer;
                    maxRead = requestLine.uri.length;
                } else {
                    throw new IOException("Line too long");
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                int val = read();
                if (val == -1)
                    throw new IOException("Couldn't read line");
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == SP) {
                space = true;
            } else if ((buf[pos] == CR) || (buf[pos] == LF)) {
                // HTTP/0.9 style request
                eol = true;
                space = true;
            }
            requestLine.uri[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }

        requestLine.uriEnd = readCount - 1;

        // Reading protocol

        maxRead = requestLine.protocol.length;
        readStart = pos;
        readCount = 0;

        while (!eol) {
            // if the buffer is full, extend it
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpRequestLine.MAX_PROTOCOL_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(requestLine.protocol, 0, newBuffer, 0,
                            maxRead);
                    requestLine.protocol = newBuffer;
                    maxRead = requestLine.protocol.length;
                } else {
                    throw new IOException("Line too long");
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                // Copying part (or all) of the internal buffer to the line
                // buffer
                int val = read();
                if (val == -1)
                    throw new IOException("Couldn't read line");
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == CR) {
                // Skip CR.
            } else if (buf[pos] == LF) {
                eol = true;
            } else {
                requestLine.protocol[readCount] = (char) buf[pos];
                readCount++;
            }
            pos++;
        }

        requestLine.protocolEnd = readCount;

    }

    /**
     * Read a header, and copies it to the given buffer. This function is meant
     * to be used during the HTTP request header parsing. Do NOT attempt to read
     * the request body using it.
     *
     * @param requestLine
     *            Request line object
     * @throws IOException
     *             If an exception occurs during the underlying socket read
     *             operations, or if the given buffer is not big enough to
     *             accomodate the whole line.
     */
    public void readHeader(HttpHeader header) throws IOException {

        // Recycling check
        if (header.nameEnd != 0)
            header.recycle();

        // Checking for a blank line
        int chr = read();
        if ((chr == CR) || (chr == LF)) { // Skipping CR
            if (chr == CR)
                read(); // Skipping LF
            header.nameEnd = 0;
            header.valueEnd = 0;
            return;
        } else {
            pos--;
        }

        // Reading the header name

        int maxRead = header.name.length;
        @SuppressWarnings("unused")
        int readStart = pos;
        int readCount = 0;

        boolean colon = false;

        while (!colon) {
            // if the buffer is full, extend it
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpHeader.MAX_NAME_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(header.name, 0, newBuffer, 0, maxRead);
                    header.name = newBuffer;
                    maxRead = header.name.length;
                } else {
                    throw new IOException("Line too long");
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                int val = read();
                if (val == -1) {
                    throw new IOException("Couldn't read line");
                }
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == COLON) {
                colon = true;
            }
            char val = (char) buf[pos];
            if ((val >= 'A') && (val <= 'Z')) {
                val = (char) (val - LC_OFFSET);
            }
            header.name[readCount] = val;
            readCount++;
            pos++;
        }

        header.nameEnd = readCount - 1;

        // Reading the header value (which can be spanned over multiple lines)

        maxRead = header.value.length;
        readStart = pos;
        readCount = 0;

        @SuppressWarnings("unused")
        int crPos = -2;

        boolean eol = false;
        boolean validLine = true;

        while (validLine) {

            boolean space = true;

            // Skipping spaces
            // Note : Only leading white spaces are removed. Trailing white
            // spaces are not.
            while (space) {
                // We're at the end of the internal buffer
                if (pos >= count) {
                    // Copying part (or all) of the internal buffer to the line
                    // buffer
                    int val = read();
                    if (val == -1)
                        throw new IOException("Couldn't read line");
                    pos = 0;
                    readStart = 0;
                }
                if ((buf[pos] == SP) || (buf[pos] == HT)) {
                    pos++;
                } else {
                    space = false;
                }
            }

            while (!eol) {
                // if the buffer is full, extend it
                if (readCount >= maxRead) {
                    if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
                        char[] newBuffer = new char[2 * maxRead];
                        System.arraycopy(header.value, 0, newBuffer, 0, maxRead);
                        header.value = newBuffer;
                        maxRead = header.value.length;
                    } else {
                        throw new IOException("Line too long");
                    }
                }
                // We're at the end of the internal buffer
                if (pos >= count) {
                    // Copying part (or all) of the internal buffer to the line
                    // buffer
                    int val = read();
                    if (val == -1)
                        throw new IOException("Couldn't read line");
                    pos = 0;
                    readStart = 0;
                }
                if (buf[pos] == CR) {} else if (buf[pos] == LF) {
                    eol = true;
                } else {
                    // FIXME : Check if binary conversion is working fine
                    int ch = buf[pos] & 0xff;
                    header.value[readCount] = (char) ch;
                    readCount++;
                }
                pos++;
            }

            int nextChr = read();

            if ((nextChr != SP) && (nextChr != HT)) {
                pos--;
                validLine = false;
            } else {
                eol = false;
                // if the buffer is full, extend it
                if (readCount >= maxRead) {
                    if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
                        char[] newBuffer = new char[2 * maxRead];
                        System.arraycopy(header.value, 0, newBuffer, 0, maxRead);
                        header.value = newBuffer;
                        maxRead = header.value.length;
                    } else {
                        throw new IOException("Line too long");
                    }
                }
                header.value[readCount] = ' ';
                readCount++;
            }

        }

        header.valueEnd = readCount;

    }

    /**
     * Read byte.
     */
    public int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        return buf[pos++] & 0xff;
    }

    /**
     *
     */
    /*
     * public int read(byte b[], int off, int len) throws IOException { }
     */

    /**
     *
     */
    /*
     * public long skip(long n) throws IOException { }
     */

    /**
     * Returns the number of bytes that can be read from this input stream
     * without blocking.
     */
    public int available() throws IOException {
        return (count - pos) + is.available();
    }

    /**
     * Close the input stream.
     */
    public void close() throws IOException {
        if (is == null)
            return;
        is.close();
        is = null;
        buf = null;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Fill the internal buffer using data from the undelying input stream.
     */
    protected void fill() throws IOException {
        pos = 0;
        count = 0;
        int nRead = is.read(buf, 0, buf.length);
        if (nRead > 0) {
            count = nRead;
        }
    }

}

final class HttpHeader {

    // -------------------------------------------------------------- Constants

    public static final int INITIAL_NAME_SIZE = 32;

    public static final int INITIAL_VALUE_SIZE = 64;

    public static final int MAX_NAME_SIZE = 128;

    public static final int MAX_VALUE_SIZE = 4096;

    // ----------------------------------------------------------- Constructors

    public HttpHeader() {

        this(new char[INITIAL_NAME_SIZE], 0, new char[INITIAL_VALUE_SIZE], 0);

    }

    public HttpHeader(char[] name, int nameEnd, char[] value, int valueEnd) {

        this.name = name;
        this.nameEnd = nameEnd;
        this.value = value;
        this.valueEnd = valueEnd;

    }

    public HttpHeader(String name, String value) {

        this.name = name.toLowerCase().toCharArray();
        this.nameEnd = name.length();
        this.value = value.toCharArray();
        this.valueEnd = value.length();

    }

    // ----------------------------------------------------- Instance Variables

    public char[] name;

    public int nameEnd;

    public char[] value;

    public int valueEnd;

    protected int hashCode = 0;

    // ------------------------------------------------------------- Properties

    // --------------------------------------------------------- Public Methods

    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {

        nameEnd = 0;
        valueEnd = 0;
        hashCode = 0;

    }

    /**
     * Test if the name of the header is equal to the given char array. All the
     * characters must already be lower case.
     */
    public boolean equals(char[] buf) {
        return equals(buf, buf.length);
    }

    /**
     * Test if the name of the header is equal to the given char array. All the
     * characters must already be lower case.
     */
    public boolean equals(char[] buf, int end) {
        if (end != nameEnd)
            return false;
        for (int i = 0; i < end; i++) {
            if (buf[i] != name[i])
                return false;
        }
        return true;
    }

    /**
     * Test if the name of the header is equal to the given string. The String
     * given must be made of lower case characters.
     */
    public boolean equals(String str) {
        return equals(str.toCharArray(), str.length());
    }

    /**
     * Test if the value of the header is equal to the given char array.
     */
    public boolean valueEquals(char[] buf) {
        return valueEquals(buf, buf.length);
    }

    /**
     * Test if the value of the header is equal to the given char array.
     */
    public boolean valueEquals(char[] buf, int end) {
        if (end != valueEnd)
            return false;
        for (int i = 0; i < end; i++) {
            if (buf[i] != value[i])
                return false;
        }
        return true;
    }

    /**
     * Test if the value of the header is equal to the given string.
     */
    public boolean valueEquals(String str) {
        return valueEquals(str.toCharArray(), str.length());
    }

    /**
     * Test if the value of the header includes the given char array.
     */
    public boolean valueIncludes(char[] buf) {
        return valueIncludes(buf, buf.length);
    }

    /**
     * Test if the value of the header includes the given char array.
     */
    public boolean valueIncludes(char[] buf, int end) {
        char firstChar = buf[0];
        int pos = 0;
        while (pos < valueEnd) {
            pos = valueIndexOf(firstChar, pos);
            if (pos == -1)
                return false;
            if ((valueEnd - pos) < end)
                return false;
            for (int i = 0; i < end; i++) {
                if (value[i + pos] != buf[i])
                    break;
                if (i == (end - 1))
                    return true;
            }
            pos++;
        }
        return false;
    }

    /**
     * Test if the value of the header includes the given string.
     */
    public boolean valueIncludes(String str) {
        return valueIncludes(str.toCharArray(), str.length());
    }

    /**
     * Returns the index of a character in the value.
     */
    public int valueIndexOf(char c, int start) {
        for (int i = start; i < valueEnd; i++) {
            if (value[i] == c)
                return i;
        }
        return -1;
    }

    /**
     * Test if the name of the header is equal to the given header. All the
     * characters in the name must already be lower case.
     */
    public boolean equals(HttpHeader header) {
        return (equals(header.name, header.nameEnd));
    }

    /**
     * Test if the name and value of the header is equal to the given header.
     * All the characters in the name must already be lower case.
     */
    public boolean headerEquals(HttpHeader header) {
        return (equals(header.name, header.nameEnd))
                && (valueEquals(header.value, header.valueEnd));
    }

    // --------------------------------------------------------- Object Methods

    /**
     * Return hash code. The hash code of the HttpHeader object is the same as
     * returned by new String(name, 0, nameEnd).hashCode().
     */
    public int hashCode() {
        int h = hashCode;
        if (h == 0) {
            int off = 0;
            char val[] = name;
            int len = nameEnd;
            for (int i = 0; i < len; i++)
                h = 31 * h + val[off++];
            hashCode = h;
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return equals(((String) obj).toLowerCase());
        } else if (obj instanceof HttpHeader) {
            return equals((HttpHeader) obj);
        }
        return false;
    }

}

final class RequestUtil {

    /**
     * The DateFormat to use for generating readable dates in cookies.
     */
    private static SimpleDateFormat format = new SimpleDateFormat(
            " EEEE, dd-MMM-yy kk:mm:ss zz");

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Encode a cookie as per RFC 2109. The resulting string can be used as the
     * value for a <code>Set-Cookie</code> header.
     *
     * @param cookie
     *            The cookie to encode.
     * @return A string following RFC 2109.
     */
    public static String encodeCookie(Cookie cookie) {

        StringBuffer buf = new StringBuffer(cookie.getName());
        buf.append("=");
        buf.append(cookie.getValue());

        if (cookie.getComment() != null) {
            buf.append("; Comment=\"");
            buf.append(cookie.getComment());
            buf.append("\"");
        }

        if (cookie.getDomain() != null) {
            buf.append("; Domain=\"");
            buf.append(cookie.getDomain());
            buf.append("\"");
        }

        @SuppressWarnings("unused")
        long age = cookie.getMaxAge();
        if (cookie.getMaxAge() >= 0) {
            buf.append("; Max-Age=\"");
            buf.append(cookie.getMaxAge());
            buf.append("\"");
        }

        if (cookie.getPath() != null) {
            buf.append("; Path=\"");
            buf.append(cookie.getPath());
            buf.append("\"");
        }

        if (cookie.getSecure()) {
            buf.append("; Secure");
        }

        if (cookie.getVersion() > 0) {
            buf.append("; Version=\"");
            buf.append(cookie.getVersion());
            buf.append("\"");
        }

        return (buf.toString());
    }

    /**
     * Filter the specified message string for characters that are sensitive in
     * HTML. This avoids potential attacks caused by including JavaScript codes
     * in the request URL that is often reported in error messages.
     *
     * @param message
     *            The message string to be filtered
     */
    public static String filter(String message) {

        if (message == null)
            return (null);

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(content[i]);
            }
        }
        return (result.toString());

    }

    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it. <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths. It does not try
     * to perform security checks for malicious input.
     *
     * @param path
     *            Relative path to be normalized
     */
    public static String normalize(String path) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        if (normalized.equals("/."))
            return "/";

        // Add a leading "/" if necessary
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index)
                    + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index)
                    + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2)
                    + normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }

    /**
     * Parse the character encoding from the specified content type header. If
     * the content type is null, or there is no explicit character encoding,
     * <code>null</code> is returned.
     *
     * @param contentType
     *            a content type header
     */
    public static String parseCharacterEncoding(String contentType) {

        if (contentType == null)
            return (null);
        int start = contentType.indexOf("charset=");
        if (start < 0)
            return (null);
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0)
            encoding = encoding.substring(0, end);
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\""))
                && (encoding.endsWith("\"")))
            encoding = encoding.substring(1, encoding.length() - 1);
        return (encoding.trim());

    }

    /**
     * Parse a cookie header into an array of cookies according to RFC 2109.
     *
     * @param header
     *            Value of an HTTP "Cookie" header
     */
    public static Cookie[] parseCookieHeader(String header) {

        if ((header == null) || (header.length() < 1))
            return (new Cookie[0]);

        ArrayList<Cookie> cookies = new ArrayList<Cookie>();
        while (header.length() > 0) {
            int semicolon = header.indexOf(';');
            if (semicolon < 0)
                semicolon = header.length();
            if (semicolon == 0)
                break;
            String token = header.substring(0, semicolon);
            if (semicolon < header.length())
                header = header.substring(semicolon + 1);
            else
                header = "";
            try {
                int equals = token.indexOf('=');
                if (equals > 0) {
                    String name = token.substring(0, equals).trim();
                    String value = token.substring(equals + 1).trim();
                    cookies.add(new Cookie(name, value));
                }
            } catch (Throwable e) {
                ;
            }
        }

        return ((Cookie[]) cookies.toArray(new Cookie[cookies.size()]));

    }

    /**
     * Append request parameters from the specified String to the specified Map.
     * It is presumed that the specified Map is not accessed from any other
     * thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: URL decoding is performed
     * individually on the parsed name and value elements, rather than on the
     * entire query string ahead of time, to properly deal with the case where
     * the name or value includes an encoded "=" or "&" character that would
     * otherwise be interpreted as a delimiter.
     *
     * @param map
     *            Map that accumulates the resulting parameters
     * @param data
     *            Input string containing request parameters
     * @param urlParameters
     *            true if we're parsing parameters on the URL
     * @exception IllegalArgumentException
     *                if the data is malformed
     */
    @SuppressWarnings("deprecation")
    public static void parseParameters(Map<String, String[]> map, String data,
            String encoding) throws UnsupportedEncodingException {

        if ((data != null) && (data.length() > 0)) {
            int len = data.length();
            byte[] bytes = new byte[len];
            data.getBytes(0, len, bytes, 0);
            parseParameters(map, bytes, encoding);
        }

    }

    /**
     * Decode and return the specified URL-encoded String. When the byte array
     * is converted to a string, the system default character encoding is
     * used... This may be different than some other servers.
     *
     * @param str
     *            The url-encoded string
     * @exception IllegalArgumentException
     *                if a '%' character is not followed by a valid 2-digit
     *                hexadecimal number
     */
    public static String URLDecode(String str) {

        return URLDecode(str, null);

    }

    /**
     * Decode and return the specified URL-encoded String.
     *
     * @param str
     *            The url-encoded string
     * @param enc
     *            The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException
     *                if a '%' character is not followed by a valid 2-digit
     *                hexadecimal number
     */
    @SuppressWarnings("deprecation")
    public static String URLDecode(String str, String enc) {

        if (str == null)
            return (null);

        int len = str.length();
        byte[] bytes = new byte[len];
        str.getBytes(0, len, bytes, 0);

        return URLDecode(bytes, enc);

    }

    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes
     *            The url-encoded byte array
     * @exception IllegalArgumentException
     *                if a '%' character is not followed by a valid 2-digit
     *                hexadecimal number
     */
    public static String URLDecode(byte[] bytes) {
        return URLDecode(bytes, null);
    }

    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes
     *            The url-encoded byte array
     * @param enc
     *            The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException
     *                if a '%' character is not followed by a valid 2-digit
     *                hexadecimal number
     */
    public static String URLDecode(byte[] bytes, String enc) {

        if (bytes == null)
            return (null);

        int len = bytes.length;
        int ix = 0;
        int ox = 0;
        while (ix < len) {
            byte b = bytes[ix++]; // Get byte to test
            if (b == '+') {
                b = (byte) ' ';
            } else if (b == '%') {
                b = (byte) ((convertHexDigit(bytes[ix++]) << 4) + convertHexDigit(bytes[ix++]));
            }
            bytes[ox++] = b;
        }
        if (enc != null) {
            try {
                return new String(bytes, 0, ox, enc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new String(bytes, 0, ox);

    }

    /**
     * Convert a byte character value to hexidecimal digit value.
     *
     * @param b
     *            the character value byte
     */
    private static byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9'))
            return (byte) (b - '0');
        if ((b >= 'a') && (b <= 'f'))
            return (byte) (b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F'))
            return (byte) (b - 'A' + 10);
        return 0;
    }

    /**
     * Put name value pair in map.
     *
     * @param b
     *            the character value byte Put name and value pair in map. When
     *            name already exist, add value to array of values.
     */
    private static void putMapEntry(Map<String, String[]> map, String name,
            String value) {
        String[] newValues = null;
        String[] oldValues = map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

    /**
     * Append request parameters from the specified String to the specified Map.
     * It is presumed that the specified Map is not accessed from any other
     * thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: URL decoding is performed
     * individually on the parsed name and value elements, rather than on the
     * entire query string ahead of time, to properly deal with the case where
     * the name or value includes an encoded "=" or "&" character that would
     * otherwise be interpreted as a delimiter. NOTE: byte array data is
     * modified by this method. Caller beware.
     *
     * @param map
     *            Map that accumulates the resulting parameters
     * @param data
     *            Input string containing request parameters
     * @param encoding
     *            Encoding to use for converting hex
     * @exception UnsupportedEncodingException
     *                if the data is malformed
     */
    public static void parseParameters(Map<String, String[]> map, byte[] data,
            String encoding) throws UnsupportedEncodingException {

        if (data != null && data.length > 0) {
            @SuppressWarnings("unused")
            int pos = 0;
            int ix = 0;
            int ox = 0;
            String key = null;
            String value = null;
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                    case '&':
                        value = new String(data, 0, ox, encoding);
                        if (key != null) {
                            putMapEntry(map, key, value);
                            key = null;
                        }
                        ox = 0;
                        break;
                    case '=':
                        key = new String(data, 0, ox, encoding);
                        ox = 0;
                        break;
                    case '+':
                        data[ox++] = (byte) ' ';
                        break;
                    case '%':
                        data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4) + convertHexDigit(data[ix++]));
                        break;
                    default:
                        data[ox++] = c;
                }
            }
            // The last value does not end in '&'. So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }

    }

}

class RequestStream extends ServletInputStream {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a servlet input stream associated with the specified Request.
     *
     * @param request
     *            The associated request
     */
    public RequestStream(HttpRequest request) {

        super();
        closed = false;
        count = 0;
        length = request.getContentLength();
        stream = request.getStream();

    }

    // ----------------------------------------------------- Instance Variables

    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;

    /**
     * The number of bytes which have already been returned by this stream.
     */
    protected int count = 0;

    /**
     * The content length past which we will not read, or -1 if there is no
     * defined content length.
     */
    protected int length = -1;

    /**
     * The underlying input stream from which we should read data.
     */
    protected InputStream stream = null;

    // --------------------------------------------------------- Public Methods

    /**
     * Close this input stream. No physical level I-O is performed, but any
     * further attempt to read from this stream will throw an IOException. If a
     * content length has been set but not all of the bytes have yet been
     * consumed, the remaining bytes will be swallowed.
     */
    public void close() throws IOException {

        if (closed)
            throw new IOException("Request stream has already been closed");

        if (length > 0) {
            while (count < length) {
                int b = read();
                if (b < 0)
                    break;
            }
        }

        closed = true;

    }

    /**
     * Read and return a single byte from this input stream, or -1 if end of
     * file has been encountered.
     *
     * @exception IOException
     *                if an input/output error occurs
     */
    public int read() throws IOException {

        // Has this stream been closed?
        if (closed)
            throw new IOException("Unable to read from a closed stream");

        // Have we read the specified content length already?
        if ((length >= 0) && (count >= length))
            return (-1); // End of file indicator

        // Read and count the next byte, then return it
        int b = stream.read();
        if (b >= 0)
            count++;
        return (b);

    }

    /**
     * Read some number of bytes from the input stream, and store them into the
     * buffer array b. The number of bytes actually read is returned as an
     * integer. This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * @param b
     *            The buffer into which the data is read
     * @exception IOException
     *                if an input/output error occurs
     */
    public int read(byte b[]) throws IOException {

        return (read(b, 0, b.length));

    }

    /**
     * Read up to <code>len</code> bytes of data from the input stream into an
     * array of bytes. An attempt is made to read as many as <code>len</code>
     * bytes, but a smaller number may be read, possibly zero. The number of
     * bytes actually read is returned as an integer. This method blocks until
     * input data is available, end of file is detected, or an exception is
     * thrown.
     *
     * @param b
     *            The buffer into which the data is read
     * @param off
     *            The start offset into array <code>b</code> at which the data
     *            is written
     * @param len
     *            The maximum number of bytes to read
     * @exception IOException
     *                if an input/output error occurs
     */
    public int read(byte b[], int off, int len) throws IOException {

        int toRead = len;
        if (length > 0) {
            if (count >= length)
                return (-1);
            if ((count + len) > length)
                toRead = length - count;
        }
        int actuallyRead = super.read(b, off, toRead);
        return (actuallyRead);

    }

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener arg0) {
		// TODO Auto-generated method stub
		
	}

}

final class Enumerator<E> implements Enumeration<E> {

    // ----------------------------------------------------------- Constructors

    /**
     * Return an Enumeration over the values of the specified Collection.
     *
     * @param collection
     *            Collection whose values should be enumerated
     */
    public Enumerator(Collection<E> collection) {

        this(collection.iterator());

    }

    /**
     * Return an Enumeration over the values returned by the specified Iterator.
     *
     * @param iterator
     *            Iterator to be wrapped
     */
    public Enumerator(Iterator<E> iterator) {

        super();
        this.iterator = iterator;

    }

    /**
     * Return an Enumeration over the values of the specified Map.
     *
     * @param map
     *            Map whose values should be enumerated
     */
    public Enumerator(Map<?, E> map) {

        this(map.values().iterator());

    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The <code>Iterator</code> over which the <code>Enumeration</code>
     * represented by this class actually operates.
     */
    private Iterator<E> iterator = null;

    // --------------------------------------------------------- Public Methods

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return <code>true</code> if and only if this enumeration object contains
     *         at least one more element to provide, <code>false</code>
     *         otherwise
     */
    public boolean hasMoreElements() {

        return (iterator.hasNext());

    }

    /**
     * Returns the next element of this enumeration if this enumeration has at
     * least one more element to provide.
     *
     * @return the next element of this enumeration
     * @exception NoSuchElementException
     *                if no more elements exist
     */
    public E nextElement() throws NoSuchElementException {

        return iterator.next();

    }

}

final class ParameterMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new, empty map with the default initial capacity and load
     * factor.
     */
    public ParameterMap() {

        super();

    }

    /**
     * Construct a new, empty map with the specified initial capacity and
     * default load factor.
     *
     * @param initialCapacity
     *            The initial capacity of this map
     */
    public ParameterMap(int initialCapacity) {

        super(initialCapacity);

    }

    /**
     * Construct a new, empty map with the specified initial capacity and load
     * factor.
     *
     * @param initialCapacity
     *            The initial capacity of this map
     * @param loadFactor
     *            The load factor of this map
     */
    public ParameterMap(int initialCapacity, float loadFactor) {

        super(initialCapacity, loadFactor);

    }

    /**
     * Construct a new map with the same mappings as the given map.
     *
     * @param map
     *            Map whose contents are dupliated in the new map
     */
    public ParameterMap(Map<K, V> map) {

        super(map);

    }

    // ------------------------------------------------------------- Properties

    /**
     * The current lock state of this parameter map.
     */
    private boolean locked = false;

    /**
     * Return the locked state of this parameter map.
     */
    public boolean isLocked() {

        return (this.locked);

    }

    /**
     * Set the locked state of this parameter map.
     *
     * @param locked
     *            The new locked state
     */
    public void setLocked(boolean locked) {

        this.locked = locked;

    }

    // --------------------------------------------------------- Public Methods

    /**
     * Remove all mappings from this map.
     *
     * @exception IllegalStateException
     *                if this map is currently locked
     */
    public void clear() {

        if (locked)
            throw new IllegalStateException(
                    "No modifications are allowed to a locked ParameterMap");
        super.clear();

    }

    /**
     * Associate the specified value with the specified key in this map. If the
     * map previously contained a mapping for this key, the old value is
     * replaced.
     *
     * @param key
     *            Key with which the specified value is to be associated
     * @param value
     *            Value to be associated with the specified key
     * @return The previous value associated with the specified key, or
     *         <code>null</code> if there was no mapping for key
     * @exception IllegalStateException
     *                if this map is currently locked
     */
    public V put(K key, V value) {

        if (locked)
            throw new IllegalStateException(
                    "No modifications are allowed to a locked ParameterMap");
        return super.put(key, value);

    }

    /**
     * Copy all of the mappings from the specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys
     * currently in the specified Map.
     *
     * @param map
     *            Mappings to be stored into this map
     * @exception IllegalStateException
     *                if this map is currently locked
     */
    public void putAll(Map<? extends K, ? extends V> map) {

        if (locked)
            throw new IllegalStateException(
                    "No modifications are allowed to a locked ParameterMap");
        super.putAll(map);

    }

    /**
     * Remove the mapping for this key from the map if present.
     *
     * @param key
     *            Key whose mapping is to be removed from the map
     * @return The previous value associated with the specified key, or
     *         <code>null</code> if there was no mapping for that key
     * @exception IllegalStateException
     *                if this map is currently locked
     */
    public V remove(Object key) {

        if (locked)
            throw new IllegalStateException(
                    "No modifications are allowed to a locked ParameterMap");
        return (super.remove(key));

    }

}
