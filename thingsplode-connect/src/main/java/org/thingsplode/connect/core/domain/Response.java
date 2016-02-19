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
package org.thingsplode.connect.core.domain;

import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.Serializable;

/**
 *
 * @author tamas.csaba@gmail.com
 * @param <T>
 */
public class Response<T extends Serializable> extends AbstractMessage<T> {

    private ResponseHeader header;

    public Response(ResponseHeader header) {
        super();
        this.header = header;
    }

    public Response(ResponseHeader header, T body) {
        super(body);
        this.header = header;
    }

    public ResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ResponseHeader header) {
        this.header = header;
    }

    public static class ResponseHeader extends AbstractMessage.MessageHeader {

        private HttpResponseStatus responseCode = HttpResponseStatus.INTERNAL_SERVER_ERROR;

        public ResponseHeader(HttpResponseStatus responseCode) {
            this.responseCode = responseCode;
        }

        public HttpResponseStatus getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(HttpResponseStatus responseCode) {
            this.responseCode = responseCode;
        }

    }

}
