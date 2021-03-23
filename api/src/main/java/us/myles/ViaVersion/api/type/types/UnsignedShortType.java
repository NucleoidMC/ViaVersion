/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class UnsignedShortType extends Type<Integer> implements TypeConverter<Integer> {
    public UnsignedShortType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) {
        return buffer.readUnsignedShort();
    }

    @Override
    public void write(ByteBuf buffer, Integer object) {
        buffer.writeShort(object);
    }

    @Override
    public Integer from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return (Integer) o;
    }
}
