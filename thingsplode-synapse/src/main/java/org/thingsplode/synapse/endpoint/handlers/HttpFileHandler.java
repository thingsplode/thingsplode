/*
 * Copyright 2016 tamas.csaba@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsplode.synapse.endpoint.handlers;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.activation.MimetypesFileTypeMap;
import org.thingsplode.synapse.core.domain.FileRequest;
import org.thingsplode.synapse.core.domain.Request.RequestHeader;
import org.thingsplode.synapse.util.Util;

/**
 *
 * @author tamas.csaba@gmail.com
 */
@Sharable
public class HttpFileHandler extends SimpleChannelInboundHandler<FileRequest> {

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    private static final int HTTP_CACHE_SECONDS = 60;
    private static final Pattern INSECURE_URI_PATTERN = Pattern.compile(".*[<>&\"].*");
    private static final String MIME_TYPES_FILE = "/META-INF/server.mime.types";
    private static MimetypesFileTypeMap MIME_TYPES_MAP;
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HttpFileHandler.class);
    private final File webroot;

    public HttpFileHandler(String webroot) throws FileNotFoundException {
        File f = new File(webroot);
        if (f.exists() && f.isDirectory() && f.canRead()) {
            this.webroot = f;
            logger.info("Webroot initialized at ["+f.getAbsolutePath()+"]");
        } else {
            throw new FileNotFoundException("The folder " + webroot + " cannot be found, not a directory or cannot be read;");
        }
        synchronized (this) {
            if (MIME_TYPES_MAP == null) {
                InputStream is = this.getClass().getResourceAsStream(MIME_TYPES_FILE);
                if (is != null) {
                    MIME_TYPES_MAP = new MimetypesFileTypeMap(is);
                } else {
                    throw new FileNotFoundException(MIME_TYPES_FILE + " couldn't be found.");
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileRequest req) throws Exception {
        Optional<File> pathOpt = getSanitizedPath(req.getHeader().getUri().getPath());
        if (!pathOpt.isPresent()) {
            HttpResponseHandler.sendError(ctx, HttpResponseStatus.FORBIDDEN, "Path is not available.");
            return;
        }

        File file = pathOpt.get();
        //if ((!file.exists()) && "/index.html".equals(msg.getHeader().getUri().getPath())) {
        //    file = new File(sanitizeUri("/index.html"));
        //}

        if (!file.exists() || file.isHidden() || !file.exists() || file.isDirectory()) {
            HttpResponseHandler.sendError(ctx, HttpResponseStatus.NOT_FOUND, "File not found.");
            return;
        }

        if (!file.isFile()) {
            HttpResponseHandler.sendError(ctx, HttpResponseStatus.FORBIDDEN, "Is not a file.");
            return;
        }
        
        String contentType = MIME_TYPES_MAP.getContentType(file.getPath());
       
        //don't send apps
        //if ("application/octet-stream".equals(contentType)) {
        //    file = new File(sanitizeUri("/index.html"));
        //}

      // Cache Validation
      Optional<String> ifModifiedSinceOpt = req.getHeader().getRequestProperty(HttpHeaderNames.IF_MODIFIED_SINCE.toString());
      if (ifModifiedSinceOpt.isPresent() && !Util.isEmpty(ifModifiedSinceOpt.get())) {
         SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
         Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSinceOpt.get());

         // Only compare up to the second because the datetime format we send to the client
         // does not have milliseconds
         long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
         long fileLastModifiedSeconds = file.lastModified() / 1000;
         if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
            sendNotModified(ctx);
            return;
         }
      }
      
            RandomAccessFile raf;
      try {
         raf = new RandomAccessFile(file, "r");
      } catch (FileNotFoundException ex) {
        HttpResponseHandler.sendError(ctx, HttpResponseStatus.NOT_FOUND, ex.getMessage());
         return;
      }
      
      long fileLength = raf.length();

      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      HttpHeaders.setContentLength(response, fileLength);
      setContentTypeHeader(response, file);
      setDateAndCacheHeaders(response, file);
      if (isKeepAlive(req.getHeader())) {
         response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
      }
      // Write the initial line and the header.
      ctx.write(response);
      // Write the content.
      ChannelFuture sendFileFuture;
      ChannelFuture lastContentFuture;
      if (ctx.pipeline().get(SslHandler.class) == null) {
         sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
         // Write the end marker.
         lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
      } else {
         sendFileFuture = ctx.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                         ctx.newProgressivePromise());
         // HttpChunkedInput will write the end marker (LastHttpContent) for us.
         lastContentFuture = sendFileFuture;
    }
    }

    private boolean isKeepAlive(RequestHeader header) {
        Optional<String> connOpt = header.getRequestProperty(HttpHeaderNames.CONNECTION.toString());
        if (connOpt.isPresent() && HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connOpt.get())) {
            return false;
        }

//        if (header.protocolVersion().isKeepAliveDefault()) {
//            return !HttpHeaderValues.CLOSE.contentEqualsIgnoreCase(connection);
//        } else {
//            return HttpHeaderValues.KEEP_ALIVE.contentEqualsIgnoreCase(connection);
//        }
        return true;
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a
     * "304 Not Modified"
     *
     * @param ctx Context
     */
    private void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response HTTP response
     */
    public void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file file to extract content type
     */
    public void setContentTypeHeader(HttpResponse response, File file) {
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, MIME_TYPES_MAP.getContentType(file.getPath()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response HTTP response
     * @param fileToCache file to extract content type
     */
    public void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private Optional<File> getSanitizedPath(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (Util.isEmpty(uri)) {
            return Optional.empty();
        }

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        uri = uri.replace('/', File.separatorChar);

        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator)
                || uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.'
                || INSECURE_URI_PATTERN.matcher(uri).matches()) {
            return Optional.empty();
        }
        return Optional.of(new File(webroot, uri));
    }

}
