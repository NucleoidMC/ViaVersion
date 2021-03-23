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
package us.myles.ViaVersion.api.minecraft.metadata.types;

import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.type.Type;

public enum MetaType1_12 implements MetaType {
    Byte(0, Type.BYTE),
    VarInt(1, Type.VAR_INT),
    Float(2, Type.FLOAT),
    String(3, Type.STRING),
    Chat(4, Type.COMPONENT),
    Slot(5, Type.ITEM),
    Boolean(6, Type.BOOLEAN),
    Vector3F(7, Type.ROTATION),
    Position(8, Type.POSITION),
    OptPosition(9, Type.OPTIONAL_POSITION),
    Direction(10, Type.VAR_INT),
    OptUUID(11, Type.OPTIONAL_UUID),
    BlockID(12, Type.VAR_INT),
    NBTTag(13, Type.NBT),
    Discontinued(99, null);

    private final int typeID;
    private final Type type;

    MetaType1_12(int typeID, Type type) {
        this.typeID = typeID;
        this.type = type;
    }

    public static MetaType1_12 byId(int id) {
        return values()[id];
    }

    @Override
    public int getTypeID() {
        return typeID;
    }

    @Override
    public Type getType() {
        return type;
    }
}
