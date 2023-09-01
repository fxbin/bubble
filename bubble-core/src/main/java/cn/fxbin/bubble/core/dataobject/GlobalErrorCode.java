package cn.fxbin.bubble.core.dataobject;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * GlobalErrorCode
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/8/29 00:12
 */
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    // 1xx Informational

    /**
     * {@code 100 Continue}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.1">HTTP/1.1: Semantics and Content, section 6.2.1</a>
     */
    CONTINUE(100, HttpStatusSeries.INFORMATIONAL, "Continue"),
    /**
     * {@code 101 Switching Protocols}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.2.2">HTTP/1.1: Semantics and Content, section 6.2.2</a>
     */
    SWITCHING_PROTOCOLS(101, HttpStatusSeries.INFORMATIONAL, "Switching Protocols"),
    /**
     * {@code 102 Processing}.
     * @see <a href="https://tools.ietf.org/html/rfc2518#section-10.1">WebDAV</a>
     */
    PROCESSING(102, HttpStatusSeries.INFORMATIONAL, "Processing"),
    /**
     * {@code 103 Early Hints}.
     * @see <a href="https://tools.ietf.org/html/rfc8297">An HTTP Status Code for Indicating Hints</a>
     * @since 6.0.5
     */
    EARLY_HINTS(103, HttpStatusSeries.INFORMATIONAL, "Early Hints"),
    /**
     * {@code 103 Checkpoint}.
     * @see <a href="https://code.google.com/p/gears/wiki/ResumableHttpRequestsProposal">A proposal for supporting
     * resumable POST/PUT HTTP requests in HTTP/1.0</a>
     * @deprecated in favor of {@link #EARLY_HINTS} which will be returned from {@code HttpStatus.valueOf(103)}
     */
    @Deprecated(since = "6.0.5")
    CHECKPOINT(103, HttpStatusSeries.INFORMATIONAL, "Checkpoint"),

    // 2xx Success

    /**
     * {@code 200 OK}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1: Semantics and Content, section 6.3.1</a>
     */
    OK(200, HttpStatusSeries.SUCCESSFUL, "OK"),
    /**
     * {@code 201 Created}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.2">HTTP/1.1: Semantics and Content, section 6.3.2</a>
     */
    CREATED(201, HttpStatusSeries.SUCCESSFUL, "Created"),
    /**
     * {@code 202 Accepted}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.3">HTTP/1.1: Semantics and Content, section 6.3.3</a>
     */
    ACCEPTED(202, HttpStatusSeries.SUCCESSFUL, "Accepted"),
    /**
     * {@code 203 Non-Authoritative Information}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.4">HTTP/1.1: Semantics and Content, section 6.3.4</a>
     */
    NON_AUTHORITATIVE_INFORMATION(203, HttpStatusSeries.SUCCESSFUL, "Non-Authoritative Information"),
    /**
     * {@code 204 No Content}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.5">HTTP/1.1: Semantics and Content, section 6.3.5</a>
     */
    NO_CONTENT(204, HttpStatusSeries.SUCCESSFUL, "No Content"),
    /**
     * {@code 205 Reset Content}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.6">HTTP/1.1: Semantics and Content, section 6.3.6</a>
     */
    RESET_CONTENT(205, HttpStatusSeries.SUCCESSFUL, "Reset Content"),
    /**
     * {@code 206 Partial Content}.
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.1">HTTP/1.1: Range Requests, section 4.1</a>
     */
    PARTIAL_CONTENT(206, HttpStatusSeries.SUCCESSFUL, "Partial Content"),
    /**
     * {@code 207 Multi-Status}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-13">WebDAV</a>
     */
    MULTI_STATUS(207, HttpStatusSeries.SUCCESSFUL, "Multi-Status"),
    /**
     * {@code 208 Already Reported}.
     * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.1">WebDAV Binding Extensions</a>
     */
    ALREADY_REPORTED(208, HttpStatusSeries.SUCCESSFUL, "Already Reported"),
    /**
     * {@code 226 IM Used}.
     * @see <a href="https://tools.ietf.org/html/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
     */
    IM_USED(226, HttpStatusSeries.SUCCESSFUL, "IM Used"),

    // 3xx Redirection

    /**
     * {@code 300 Multiple Choices}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.1">HTTP/1.1: Semantics and Content, section 6.4.1</a>
     */
    MULTIPLE_CHOICES(300, HttpStatusSeries.REDIRECTION, "Multiple Choices"),
    /**
     * {@code 301 Moved Permanently}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.2">HTTP/1.1: Semantics and Content, section 6.4.2</a>
     */
    MOVED_PERMANENTLY(301, HttpStatusSeries.REDIRECTION, "Moved Permanently"),
    /**
     * {@code 302 Found}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.3">HTTP/1.1: Semantics and Content, section 6.4.3</a>
     */
    FOUND(302, HttpStatusSeries.REDIRECTION, "Found"),
    /**
     * {@code 302 Moved Temporarily}.
     * @see <a href="https://tools.ietf.org/html/rfc1945#section-9.3">HTTP/1.0, section 9.3</a>
     * @deprecated in favor of {@link #FOUND} which will be returned from {@code HttpStatus.valueOf(302)}
     */
    @Deprecated
    MOVED_TEMPORARILY(302, HttpStatusSeries.REDIRECTION, "Moved Temporarily"),
    /**
     * {@code 303 See Other}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.4">HTTP/1.1: Semantics and Content, section 6.4.4</a>
     */
    SEE_OTHER(303, HttpStatusSeries.REDIRECTION, "See Other"),
    /**
     * {@code 304 Not Modified}.
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.1">HTTP/1.1: Conditional Requests, section 4.1</a>
     */
    NOT_MODIFIED(304, HttpStatusSeries.REDIRECTION, "Not Modified"),
    /**
     * {@code 305 Use Proxy}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.5">HTTP/1.1: Semantics and Content, section 6.4.5</a>
     * @deprecated due to security concerns regarding in-band configuration of a proxy
     */
    @Deprecated
    USE_PROXY(305, HttpStatusSeries.REDIRECTION, "Use Proxy"),
    /**
     * {@code 307 Temporary Redirect}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.4.7">HTTP/1.1: Semantics and Content, section 6.4.7</a>
     */
    TEMPORARY_REDIRECT(307, HttpStatusSeries.REDIRECTION, "Temporary Redirect"),
    /**
     * {@code 308 Permanent Redirect}.
     * @see <a href="https://tools.ietf.org/html/rfc7238">RFC 7238</a>
     */
    PERMANENT_REDIRECT(308, HttpStatusSeries.REDIRECTION, "Permanent Redirect"),

    // --- 4xx Client Error ---

    /**
     * {@code 400 Bad Request}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1: Semantics and Content, section 6.5.1</a>
     */
    BAD_REQUEST(400, HttpStatusSeries.CLIENT_ERROR, "Bad Request"),
    /**
     * {@code 401 Unauthorized}.
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1: Authentication, section 3.1</a>
     */
    UNAUTHORIZED(401, HttpStatusSeries.CLIENT_ERROR, "Unauthorized"),
    /**
     * {@code 402 Payment Required}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.2">HTTP/1.1: Semantics and Content, section 6.5.2</a>
     */
    PAYMENT_REQUIRED(402, HttpStatusSeries.CLIENT_ERROR, "Payment Required"),
    /**
     * {@code 403 Forbidden}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1: Semantics and Content, section 6.5.3</a>
     */
    FORBIDDEN(403, HttpStatusSeries.CLIENT_ERROR, "Forbidden"),
    /**
     * {@code 404 Not Found}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1: Semantics and Content, section 6.5.4</a>
     */
    NOT_FOUND(404, HttpStatusSeries.CLIENT_ERROR, "Not Found"),
    /**
     * {@code 405 Method Not Allowed}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.5">HTTP/1.1: Semantics and Content, section 6.5.5</a>
     */
    METHOD_NOT_ALLOWED(405, HttpStatusSeries.CLIENT_ERROR, "Method Not Allowed"),
    /**
     * {@code 406 Not Acceptable}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.6">HTTP/1.1: Semantics and Content, section 6.5.6</a>
     */
    NOT_ACCEPTABLE(406, HttpStatusSeries.CLIENT_ERROR, "Not Acceptable"),
    /**
     * {@code 407 Proxy Authentication Required}.
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.2">HTTP/1.1: Authentication, section 3.2</a>
     */
    PROXY_AUTHENTICATION_REQUIRED(407, HttpStatusSeries.CLIENT_ERROR, "Proxy Authentication Required"),
    /**
     * {@code 408 Request Timeout}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.7">HTTP/1.1: Semantics and Content, section 6.5.7</a>
     */
    REQUEST_TIMEOUT(408, HttpStatusSeries.CLIENT_ERROR, "Request Timeout"),
    /**
     * {@code 409 Conflict}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.8">HTTP/1.1: Semantics and Content, section 6.5.8</a>
     */
    CONFLICT(409, HttpStatusSeries.CLIENT_ERROR, "Conflict"),
    /**
     * {@code 410 Gone}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.9">
     *     HTTP/1.1: Semantics and Content, section 6.5.9</a>
     */
    GONE(410, HttpStatusSeries.CLIENT_ERROR, "Gone"),
    /**
     * {@code 411 Length Required}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.10">
     *     HTTP/1.1: Semantics and Content, section 6.5.10</a>
     */
    LENGTH_REQUIRED(411, HttpStatusSeries.CLIENT_ERROR, "Length Required"),
    /**
     * {@code 412 Precondition failed}.
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-4.2">
     *     HTTP/1.1: Conditional Requests, section 4.2</a>
     */
    PRECONDITION_FAILED(412, HttpStatusSeries.CLIENT_ERROR, "Precondition Failed"),
    /**
     * {@code 413 Payload Too Large}.
     * @since 4.1
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.11">
     *     HTTP/1.1: Semantics and Content, section 6.5.11</a>
     */
    PAYLOAD_TOO_LARGE(413, HttpStatusSeries.CLIENT_ERROR, "Payload Too Large"),
    /**
     * {@code 413 Request Entity Too Large}.
     * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.14">HTTP/1.1, section 10.4.14</a>
     * @deprecated in favor of {@link #PAYLOAD_TOO_LARGE} which will be
     * returned from {@code HttpStatus.valueOf(413)}
     */
    @Deprecated
    REQUEST_ENTITY_TOO_LARGE(413, HttpStatusSeries.CLIENT_ERROR, "Request Entity Too Large"),
    /**
     * {@code 414 URI Too Long}.
     * @since 4.1
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.12">
     *     HTTP/1.1: Semantics and Content, section 6.5.12</a>
     */
    URI_TOO_LONG(414, HttpStatusSeries.CLIENT_ERROR, "URI Too Long"),
    /**
     * {@code 414 Request-URI Too Long}.
     * @see <a href="https://tools.ietf.org/html/rfc2616#section-10.4.15">HTTP/1.1, section 10.4.15</a>
     * @deprecated in favor of {@link #URI_TOO_LONG} which will be returned from {@code HttpStatus.valueOf(414)}
     */
    @Deprecated
    REQUEST_URI_TOO_LONG(414, HttpStatusSeries.CLIENT_ERROR, "Request-URI Too Long"),
    /**
     * {@code 415 Unsupported Media Type}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.13">
     *     HTTP/1.1: Semantics and Content, section 6.5.13</a>
     */
    UNSUPPORTED_MEDIA_TYPE(415, HttpStatusSeries.CLIENT_ERROR, "Unsupported Media Type"),
    /**
     * {@code 416 Requested Range Not Satisfiable}.
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.4">HTTP/1.1: Range Requests, section 4.4</a>
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, HttpStatusSeries.CLIENT_ERROR, "Requested range not satisfiable"),
    /**
     * {@code 417 Expectation Failed}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.14">
     *     HTTP/1.1: Semantics and Content, section 6.5.14</a>
     */
    EXPECTATION_FAILED(417, HttpStatusSeries.CLIENT_ERROR, "Expectation Failed"),
    /**
     * {@code 418 I'm a teapot}.
     * @see <a href="https://tools.ietf.org/html/rfc2324#section-2.3.2">HTCPCP/1.0</a>
     */
    I_AM_A_TEAPOT(418, HttpStatusSeries.CLIENT_ERROR, "I'm a teapot"),
    /**
     * @deprecated See
     * <a href="https://tools.ietf.org/rfcdiff?difftype=--hwdiff&amp;url2=draft-ietf-webdav-protocol-06.txt">
     *     WebDAV Draft Changes</a>
     */
    @Deprecated
    INSUFFICIENT_SPACE_ON_RESOURCE(419, HttpStatusSeries.CLIENT_ERROR, "Insufficient Space On Resource"),
    /**
     * @deprecated See
     * <a href="https://tools.ietf.org/rfcdiff?difftype=--hwdiff&amp;url2=draft-ietf-webdav-protocol-06.txt">
     *     WebDAV Draft Changes</a>
     */
    @Deprecated
    METHOD_FAILURE(420, HttpStatusSeries.CLIENT_ERROR, "Method Failure"),
    /**
     * @deprecated
     * See <a href="https://tools.ietf.org/rfcdiff?difftype=--hwdiff&amp;url2=draft-ietf-webdav-protocol-06.txt">
     *     WebDAV Draft Changes</a>
     */
    @Deprecated
    DESTINATION_LOCKED(421, HttpStatusSeries.CLIENT_ERROR, "Destination Locked"),
    /**
     * {@code 422 Unprocessable Entity}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.2">WebDAV</a>
     */
    UNPROCESSABLE_ENTITY(422, HttpStatusSeries.CLIENT_ERROR, "Unprocessable Entity"),
    /**
     * {@code 423 Locked}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.3">WebDAV</a>
     */
    LOCKED(423, HttpStatusSeries.CLIENT_ERROR, "Locked"),
    /**
     * {@code 424 Failed Dependency}.
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.4">WebDAV</a>
     */
    FAILED_DEPENDENCY(424, HttpStatusSeries.CLIENT_ERROR, "Failed Dependency"),
    /**
     * {@code 425 Too Early}.
     * @since 5.2
     * @see <a href="https://tools.ietf.org/html/rfc8470">RFC 8470</a>
     */
    TOO_EARLY(425, HttpStatusSeries.CLIENT_ERROR, "Too Early"),
    /**
     * {@code 426 Upgrade Required}.
     * @see <a href="https://tools.ietf.org/html/rfc2817#section-6">Upgrading to TLS Within HTTP/1.1</a>
     */
    UPGRADE_REQUIRED(426, HttpStatusSeries.CLIENT_ERROR, "Upgrade Required"),
    /**
     * {@code 428 Precondition Required}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-3">Additional HTTP Status Codes</a>
     */
    PRECONDITION_REQUIRED(428, HttpStatusSeries.CLIENT_ERROR, "Precondition Required"),
    /**
     * {@code 429 Too Many Requests}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-4">Additional HTTP Status Codes</a>
     */
    TOO_MANY_REQUESTS(429, HttpStatusSeries.CLIENT_ERROR, "Too Many Requests"),
    /**
     * {@code 431 Request Header Fields Too Large}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-5">Additional HTTP Status Codes</a>
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, HttpStatusSeries.CLIENT_ERROR, "Request Header Fields Too Large"),
    /**
     * {@code 451 Unavailable For Legal Reasons}.
     * @see <a href="https://tools.ietf.org/html/draft-ietf-httpbis-legally-restricted-status-04">
     * An HTTP Status Code to Report Legal Obstacles</a>
     * @since 4.3
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, HttpStatusSeries.CLIENT_ERROR, "Unavailable For Legal Reasons"),

    // --- 5xx Server Error ---

    /**
     * {@code 500 Internal Server Error}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1: Semantics and Content, section 6.6.1</a>
     */
    INTERNAL_SERVER_ERROR(500, HttpStatusSeries.SERVER_ERROR, "Internal Server Error"),
    /**
     * {@code 501 Not Implemented}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.2">HTTP/1.1: Semantics and Content, section 6.6.2</a>
     */
    NOT_IMPLEMENTED(501, HttpStatusSeries.SERVER_ERROR, "Not Implemented"),
    /**
     * {@code 502 Bad Gateway}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.3">HTTP/1.1: Semantics and Content, section 6.6.3</a>
     */
    BAD_GATEWAY(502, HttpStatusSeries.SERVER_ERROR, "Bad Gateway"),
    /**
     * {@code 503 Service Unavailable}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.4">HTTP/1.1: Semantics and Content, section 6.6.4</a>
     */
    SERVICE_UNAVAILABLE(503, HttpStatusSeries.SERVER_ERROR, "Service Unavailable"),
    /**
     * {@code 504 Gateway Timeout}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.5">HTTP/1.1: Semantics and Content, section 6.6.5</a>
     */
    GATEWAY_TIMEOUT(504, HttpStatusSeries.SERVER_ERROR, "Gateway Timeout"),
    /**
     * {@code 505 HTTP Version Not Supported}.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.6">HTTP/1.1: Semantics and Content, section 6.6.6</a>
     */
    HTTP_VERSION_NOT_SUPPORTED(505, HttpStatusSeries.SERVER_ERROR, "HTTP Version not supported"),
    /**
     * {@code 506 Variant Also Negotiates}
     * @see <a href="https://tools.ietf.org/html/rfc2295#section-8.1">Transparent Content Negotiation</a>
     */
    VARIANT_ALSO_NEGOTIATES(506, HttpStatusSeries.SERVER_ERROR, "Variant Also Negotiates"),
    /**
     * {@code 507 Insufficient Storage}
     * @see <a href="https://tools.ietf.org/html/rfc4918#section-11.5">WebDAV</a>
     */
    INSUFFICIENT_STORAGE(507, HttpStatusSeries.SERVER_ERROR, "Insufficient Storage"),
    /**
     * {@code 508 Loop Detected}
     * @see <a href="https://tools.ietf.org/html/rfc5842#section-7.2">WebDAV Binding Extensions</a>
     */
    LOOP_DETECTED(508, HttpStatusSeries.SERVER_ERROR, "Loop Detected"),
    /**
     * {@code 509 Bandwidth Limit Exceeded}
     */
    BANDWIDTH_LIMIT_EXCEEDED(509, HttpStatusSeries.SERVER_ERROR, "Bandwidth Limit Exceeded"),
    /**
     * {@code 510 Not Extended}
     * @see <a href="https://tools.ietf.org/html/rfc2774#section-7">HTTP Extension Framework</a>
     */
    NOT_EXTENDED(510, HttpStatusSeries.SERVER_ERROR, "Not Extended"),
    /**
     * {@code 511 Network Authentication Required}.
     * @see <a href="https://tools.ietf.org/html/rfc6585#section-6">Additional HTTP Status Codes</a>
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, HttpStatusSeries.SERVER_ERROR, "Network Authentication Required");


    private static final GlobalErrorCode[] VALUES;

    static {
        VALUES = values();
    }


    private final int value;

    private final HttpStatusSeries series;

    private final String reasonPhrase;

    @Override
    public int value() {
        return this.value;
    }

    @Override
    public String reasonPhrase() {
        return this.reasonPhrase;
    }

    /**
     * Return the HTTP status series of this status code.
     * @see HttpStatusSeries
     */
    public HttpStatusSeries series() {
        return this.series;
    }

    @Override
    public boolean is1xxInformational() {
        return (series() == HttpStatusSeries.INFORMATIONAL);
    }

    @Override
    public boolean is2xxSuccessful() {
        return (series() == HttpStatusSeries.SUCCESSFUL);
    }

    @Override
    public boolean is3xxRedirection() {
        return (series() == HttpStatusSeries.REDIRECTION);
    }

    @Override
    public boolean is4xxClientError() {
        return (series() == HttpStatusSeries.CLIENT_ERROR);
    }

    @Override
    public boolean is5xxServerError() {
        return (series() == HttpStatusSeries.SERVER_ERROR);
    }

    @Override
    public boolean isError() {
        return (is4xxClientError() || is5xxServerError());
    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString() {
        return this.value + " " + name();
    }


    /**
     * Return the {@code GlobalErrorCode} enum constant with the specified numeric value.
     * @param errorCode the numeric value of the enum to be returned
     * @return the enum constant with the specified numeric value
     * @throws IllegalArgumentException if this enum has no constant for the specified numeric value
     */
    public static GlobalErrorCode valueOf(int errorCode) {
        GlobalErrorCode code = resolve(errorCode);
        if (code == null) {
            throw new IllegalArgumentException("No matching constant for [" + errorCode + "]");
        }
        return code;
    }

    /**
     * Resolve the given status code to an {@code GlobalErrorCode}, if possible.
     * @param errorCode the HTTP status code (potentially non-standard)
     * @return the corresponding {@code GlobalErrorCode}, or {@code null} if not found
     */
    @Nullable
    public static GlobalErrorCode resolve(int errorCode) {
        // Use cached VALUES instead of values() to prevent array allocation.
        for (GlobalErrorCode code : VALUES) {
            if (code.value == errorCode) {
                return code;
            }
        }
        return null;
    }

}
