/*
 * Copyright (C) 2012~2014 dinstone<dinstone@163.com>
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

package com.dinstone.jrpc.transport.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.Serializable;
import java.util.List;

import com.dinstone.jrpc.protocol.Message;
import com.dinstone.jrpc.protocol.MessageCodec;

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
        int remaining = in.readableBytes();
        if (remaining < 4) {
            return null;
        }

        int objectSize = in.getInt(0);
        if (objectSize > maxObjectSize) {
            throw new IllegalArgumentException("The encoded object is too big: " + objectSize + " (> " + maxObjectSize
                    + ')');
        }

        if (remaining - 4 >= objectSize) {
            objectSize = in.readInt();
            // RPC object size
            byte[] rpcBytes = new byte[objectSize];
            in.readBytes(rpcBytes);
            return rpcBytes;
        }
        return null;
    }
}
