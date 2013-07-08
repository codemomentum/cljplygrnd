package org.codemomentum.cljplygrnd.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class CustomBusinessObjectSerializer extends Serializer {
    @Override
    public void write(Kryo kryo, Output output, Object o) {
        kryo.writeClassAndObject(output, o);
    }

    @Override
    public Object read(Kryo kryo, Input input, Class aClass) {
        return kryo.readClassAndObject(input);
    }

}
