/*
 * Copyright (C) 2014~2016 dinstone<dinstone@163.com>
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
package com.dinstone.jrpc.serialize;

public enum SerializeType {
    JACKSON((byte) 1), HESSIAN((byte) 2);

    private byte value;

    private SerializeType(byte value) {
        this.value = value;
    }

    /**
     * the value to get
     * 
     * @return the value
     * @see SerializeType#value
     */
    public byte getValue() {
        return value;
    }

    public static SerializeType valueOf(int type) {
        switch (type) {
        case 1:

            return JACKSON;
        case 2:

            return HESSIAN;

        default:
            break;
        }

        throw new IllegalArgumentException("unsupported serialize type [" + type + "]");
    }

}
