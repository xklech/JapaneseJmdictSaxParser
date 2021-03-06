package cz.muni.fi.japanesejmdictsaxparser.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Jarek
 */
public class CompressFolder {
    
  public static void zip(File directory, File zipfile) throws IOException {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<>();
    queue.push(directory);
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zout = new ZipOutputStream(out);
      zout.setLevel(5);
      res = zout;
      while (!queue.isEmpty()) {
        directory = queue.pop();
        for (File kid : directory.listFiles()) {
          String name = base.relativize(kid.toURI()).getPath();
          if (kid.isDirectory()) {
            queue.push(kid);
            name = name.endsWith("/") ? name : name + "/";
            zout.putNextEntry(new ZipEntry(name));
          } else {
            zout.putNextEntry(new ZipEntry(name));
            copy(kid, zout);
            zout.closeEntry();
          }
        }
      }
    } finally {
      res.close();
    }
  }
  
  public static void unzip(File zipfile, File directory) throws IOException {
    ZipFile zfile = new ZipFile(zipfile);
    Enumeration<? extends ZipEntry> entries = zfile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      File file = new File(directory, entry.getName());
      if (entry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try (InputStream in = zfile.getInputStream(entry)) {
          copy(in, file);
        }
      }
    }
  }
  
  public static void deleteIfExist(File file){
      if(file.exists()){
          file.delete();
      }
  }

  
  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    while (true) {
      int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  private static void copy(File file, OutputStream out) throws IOException {
    try (InputStream in = new FileInputStream(file)) {
      copy(in, out);
    }
  }

  private static void copy(InputStream in, File file) throws IOException {
    try (OutputStream out = new FileOutputStream(file)) {
      copy(in, out);
    }
  }
  
    /**
     * Deletes given directory
     * 
     * @param directory directory to be deleted
     * @return true on succes
     */
    static public boolean deleteDirectory(File directory) {
            if (directory.exists()) {
                    File[] files = directory.listFiles();
        for(File file: files){
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
            }
            return (directory.delete());
    }
  
    public static void compressGzipFile(File file, File gzipFile) {
        try {
            FileOutputStream fos = new FileOutputStream(gzipFile);
            try (FileInputStream fis = new FileInputStream(file); GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {

                byte[] buffer = new byte[1024];
                int len;
                while((len=fis.read(buffer)) != -1){
                    gzipOS.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            
        }
        
    }
}
