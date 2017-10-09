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
import java.nio.ByteBuffer;

import com.dinstone.jrpc.serializer.SerializeType;
import com.dinstone.jrpc.serializer.SerializerRegister;

/**
 * RPC message codec.
 *
 * @author guojinfei
 * @version 1.0.0.2014-6-23
 */
public class MessageCodec {

    private static final SerializerRegister REGISTER = SerializerRegister.getInstance();

    private MessageCodec() {
    }

    /**
     * encode RPC Message.
     *
     * @param message
     *        RpcMessage
     * @return
     * @throws Exception
     *         serializable exception
     */
    public static byte[] encodeMessage(Message<? extends Serializable> message) throws Exception {
        Serializable body = message.getContent();
        byte[] bodyBytes = REGISTER.find(message.getSerializeType()).serialize(body);

        ByteBuffer messageBuf = ByteBuffer.allocate(6 + bodyBytes.length);
        messageBuf.putInt(message.getMessageId());
        messageBuf.put(message.getMessageType().getValue());
        messageBuf.put(message.getSerializeType().getValue());
        messageBuf.put(bodyBytes);
        messageBuf.flip();

        return messageBuf.array();
    }

    /**
     * decode RPC Message.
     *
     * @param rpcBytes
     *        RpcMessage bytes
     * @return
     * @throws Exception
     *         deserialize exception
     */
    public static Message<? extends Serializable> decodeMessage(byte[] rpcBytes) throws Exception {
        ByteBuffer messageBuf = ByteBuffer.wrap(rpcBytes);
        // parse header
        int messageId = messageBuf.getInt();
        MessageType messageType = MessageType.valueOf(messageBuf.get());
        SerializeType serializeType = SerializeType.valueOf(messageBuf.get());

        // byte[] bodyBytes = Arrays.copyOfRange(rpcBytes, 6, rpcBytes.length);
        int bodyLength = rpcBytes.length - 6;
        if (messageType == MessageType.REQUEST) {
            Call call = REGISTER.find(serializeType).deserialize(rpcBytes, 6, bodyLength, Call.class);
            return new Request(messageId, serializeType, call);
        } else if (messageType == MessageType.RESPONSE) {
            Result result = REGISTER.find(serializeType).deserialize(rpcBytes, 6, bodyLength, Result.class);
            return new Response(messageId, serializeType, result);
        } else if (messageType == MessageType.HEARTBEAT) {
            Tick tick = REGISTER.find(serializeType).deserialize(rpcBytes, 6, bodyLength, Tick.class);
            return new Heartbeat(messageId, serializeType, tick);
        } else {
            throw new IllegalStateException("unsupported content type [" + messageType + "]");
        }
    }
}
