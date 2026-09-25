package com.news.utils.common;

import com.news.model.wemedia.pojos.WmNews;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffUtil {

    /**
     * Serialize object to byte array using Protostuff
     *
     * @param t Object to serialize
     * @param <T> Object type
     * @return Serialized byte array
     */
    public static <T> byte[] serialize(T t){
        Schema schema = RuntimeSchema.getSchema(t.getClass());
        return ProtostuffIOUtil.toByteArray(t,schema,
                LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
 
    }

    /**
     * Deserialize byte array to object using Protostuff
     *
     * @param bytes Byte array to deserialize
     * @param c Target class
     * @param <T> Object type
     * @return Deserialized object
     */
    public static <T> T deserialize(byte []bytes,Class<T> c) {
        T t = null;
        try {
            t = c.newInstance();
            Schema schema = RuntimeSchema.getSchema(t.getClass());
             ProtostuffIOUtil.mergeFrom(bytes,t,schema);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * Performance comparison between JDK serialization and Protostuff serialization
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        long start =System.currentTimeMillis();
        for (int i = 0; i <1000000 ; i++) {
            WmNews wmNews =new WmNews();
            JdkSerializeUtil.serialize(wmNews);
        }
        System.out.println("JDK serialization time: "+(System.currentTimeMillis()-start)+"ms");

        start =System.currentTimeMillis();
        for (int i = 0; i <1000000 ; i++) {
            WmNews wmNews =new WmNews();
            ProtostuffUtil.serialize(wmNews);
        }
        System.out.println("Protostuff serialization time: "+(System.currentTimeMillis()-start)+"ms");
    }

 
 
}
