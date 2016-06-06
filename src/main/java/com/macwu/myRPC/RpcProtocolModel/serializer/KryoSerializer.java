package com.macwu.myRPC.RpcProtocolModel.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;

/**
 * Created by macwu on 16-6-4.
 */

public class KryoSerializer
{
    private Kryo kryo;
    private Registration registration = null;
    private Class<?> t;
    public KryoSerializer()
    {
        // TODO Auto-generated constructor stub
        kryo = new Kryo();
        kryo.setReferences(true);
        // kryo.setRegistrationRequired(true);
        // kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public void register(Class<?> T)
    {
        //register the class
        t = T;
        registration = kryo.register(t);
    }

    public byte[] Serialize(Object object)
    {
        Output output = null;
        output = new Output(1, 4096);
        kryo.writeClassAndObject(output, object);
        byte[] bb = output.toBytes();
        output.flush();

        return bb;
    }

    public <t> t Deserialize(byte[] bb) {
        Input input = null;
        input = new Input(bb);
        @SuppressWarnings("unchecked")
        t res = (t) kryo.readClassAndObject(input);
        input.close();
        return res;
    }
}
