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

/**
 * message type.
 *
 * @author guojinfei
 * @version 1.0.0.2014-6-23
 */
public enum MessageType {
    REQUEST((byte) 1), RESPONSE((byte) 2), HEARTBEAT((byte) 3);

    private byte value;

    private MessageType(byte value) {
        this.value = value;
    }

    /**
     * the value to get
     *
     * @return the value
     * @see MessageType#value
     */
    public byte getValue() {
        return value;
    }

    public static MessageType valueOf(int value) {
        switch (value) {
            case 1:
                return REQUEST;
            case 2:
                return RESPONSE;
            case 3:
                return HEARTBEAT;

            default:
                break;
        }
        throw new IllegalArgumentException("unsupported message type [" + value + "]");
    }

}
