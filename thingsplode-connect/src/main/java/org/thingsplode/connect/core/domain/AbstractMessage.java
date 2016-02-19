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

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.thingsplode.connect.core.exceptions.ConnectMarshallerException;

/**
 *
 * @author tamas.csaba@gmail.com
 * @param <T>
 */
public abstract class AbstractMessage<T extends Serializable> implements Serializable {

    private T body;

    public class MessageHeader {

        private final AtomicLong id = new AtomicLong(0);
        private String protocolVersion;
        private InetSocketAddress callerAddress;
        private Set<Parameter> parameters;

        public MessageHeader() {
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public InetSocketAddress getCallerAddress() {
            return callerAddress;
        }

        public void setCallerAddress(InetSocketAddress callerAddress) {
            this.callerAddress = callerAddress;
        }

        public Set<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(Set<Parameter> parameters) {
            this.parameters = parameters;
        }
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public <T> T expectBody(Class<T> type) throws ConnectMarshallerException {
        try {
            if (body == null) {
                throw new ConnectMarshallerException("This message has not extension.");
            }
            return type.cast(body);
        } catch (ClassCastException ex) {
            throw new ConnectMarshallerException("The expected message is not type of " + type.getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

}