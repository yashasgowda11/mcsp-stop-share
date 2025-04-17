// src/io/PartitionIO.java
package io;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class PartitionIO {

    public static void writePartition(String filePath, String data) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel fc = raf.getChannel()) {

            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
            mbb.put(bytes);
        }
    }

    public static String readPartition(String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             FileChannel fc = raf.getChannel()) {

            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            byte[] buffer = new byte[(int) fc.size()];
            mbb.get(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        }
    }
}
