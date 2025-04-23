// src/io/PartitionIO.java
package io;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class PartitionIO {

    // Writes string data to a file using memory-mapped I/O
    public static void writePartition(String filePath, String data) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel fc = raf.getChannel()) {

            byte[] bytes = data.getBytes(StandardCharsets.UTF_8); // Convert string to bytes
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length); // Map to memory
            mbb.put(bytes); // Write to buffer
        }
    }

    // Reads and returns the contents of a file using memory-mapped I/O
    public static String readPartition(String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             FileChannel fc = raf.getChannel()) {

            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()); // Map file to buffer
            byte[] buffer = new byte[(int) fc.size()];
            mbb.get(buffer); // Read from buffer
            return new String(buffer, StandardCharsets.UTF_8); // Convert bytes to string
        }
    }
}
