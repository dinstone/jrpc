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
package com.dinstone.jrpc.transport.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.dinstone.jrpc.protocol.Message;
import com.dinstone.jrpc.protocol.MessageCodec;

/**
 * Transport Protocol Encoder.
 *
 * @author guojinfei
 * @version 1.0.0.2014-6-20
 */
public class TransportProtocolEncoder extends ProtocolEncoderAdapter {

    private int maxObjectSize = Integer.MAX_VALUE;

    public TransportProtocolEncoder() {
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
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (message instanceof Message<?>) {
            byte[] rpcBytes = MessageCodec.encodeMessage((Message<?>) message);
            writeFrame(out, rpcBytes);
        }
    }

    private void writeFrame(ProtocolEncoderOutput out, byte[] rpcBytes) {
        int objectSize = rpcBytes.length;
        if (objectSize > maxObjectSize) {
            throw new IllegalArgumentException(
                "The encoded object is too big: " + objectSize + " (> " + maxObjectSize + ')');
        }

        // FrameLen = PrefixLen + RpcObjectSize
        IoBuffer frame = IoBuffer.allocate(4 + objectSize);
        frame.putInt(objectSize);
        frame.put(rpcBytes);
        frame.flip();

        out.write(frame);
    }
}
