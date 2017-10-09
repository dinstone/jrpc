/*
 * Copyright (C) 2014~2017 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.jrpc.protocol;

import java.io.Serializable;

import com.dinstone.jrpc.serializer.SerializeType;

/**
 * transport message that includes headers and a content.
 *
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public abstract class Message<C extends Serializable> implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private int messageId;

    private MessageType messageType;

    private SerializeType serializeType;

    protected C content;

    public Message(int messageId, SerializeType serializeType, MessageType messageType, C content) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.serializeType = serializeType;
        this.content = content;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getMessageId() {
        return messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public SerializeType getSerializeType() {
        return serializeType;
    }

    public C getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Message [messageId=" + messageId + ", messageType=" + messageType + ", serializeType=" + serializeType
                + ", content=" + content + "]";
    }

}