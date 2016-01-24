/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

public class HessianSerializer implements Serializer {

    public <T> byte[] serialize(T data) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(bos);
        // out.startMessage();
        out.writeObject(data);
        // out.completeMessage();
        out.flush();
        return bos.toByteArray();
    }

    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        Hessian2Input in = new Hessian2Input(bin);
        // in.startMessage();
        Object obj = in.readObject(clazz);
        // in.completeMessage();
        return clazz.cast(obj);
    }

}
