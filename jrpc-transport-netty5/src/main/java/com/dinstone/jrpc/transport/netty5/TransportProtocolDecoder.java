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
package com.dinstone.jrpc.transport.netty5;

import java.io.Serializable;
import java.util.List;

import com.dinstone.jrpc.protocol.Message;
import com.dinstone.jrpc.protocol.MessageCodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class TransportProtocolDecoder extends ByteToMessageDecoder {

    /** 2GB */
    private int maxObjectSize = Integer.MAX_VALUE;

    public TransportProtocolDecoder() {
    }

    /**
     * the maxObjectSize to get
     *
     * @return the maxObjectSize
     * @see TransportProtocolEncoder#maxObjectSize
     */
    public int getMaxObjectSize() {
        return maxObjectSize;
    }

    /**
     * the maxObjectSize to set
     *
     * @param maxObjectSize
     * @see TransportProtocolEncoder#maxObjectSize
     */
    public void setMaxObjectSize(int maxObjectSize) {
        if (maxObjectSize <= 0) {
            throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
        }

        this.maxObjectSize = maxObjectSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] rpcBytes = readFrame(in);
        if (rpcBytes == null) {
            return;
        }

        Message<? extends Serializable> message = MessageCodec.decodeMessage(rpcBytes);
        out.add(message);
    }

    private byte[] readFrame(ByteBuf in) {
        if (in.readableBytes() > 4) {
            in.markReaderIndex();
            int len = in.readInt();
            if (len > maxObjectSize) {
                throw new IllegalStateException("The encoded object is too big: " + len + " (> " + maxObjectSize + ")");
            } else if (len < 1) {
                throw new IllegalStateException("The encoded object is too small: " + len + " (< 1)");
            }

            if (in.readableBytes() < len) {
                in.resetReaderIndex();
                return null;
            }

            byte[] rpcBytes = new byte[len];
            in.readBytes(rpcBytes);
            return rpcBytes;
        }

        return null;
    }
}
