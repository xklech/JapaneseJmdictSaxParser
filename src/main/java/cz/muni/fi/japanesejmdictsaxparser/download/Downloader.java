package cz.muni.fi.japanesejmdictsaxparser.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 *
 * @author Jarek
 */
public class Downloader {
    
    public static File downloadFile(URL url, File file) throws IOException{
        (new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources")).mkdirs();
        if(file.exists()){
            file.delete();
        }

        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        return file;
    }

    
    public static File downloadTatoebaSentences(URL url) throws IOException{
        (new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources")).mkdirs();

        File file = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoeba_sentences.tar.bz2");
        if(file.exists()){
            file.delete();
        }
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
        return file;
    }
    
    
}
