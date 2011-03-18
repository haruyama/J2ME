
import java.io.IOException;
import java.io.InputStream;


import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class FileUtil {

    public static final Object FILE_LOCK = new Object();
    

    public static byte[] getFileContent(String filePath, long offset) throws IOException{
        FileConnection fc = null;
        InputStream is = null;

        
        synchronized (FILE_LOCK) {
            try {
                System.err.println(filePath + " " + offset);
                fc = (FileConnection) Connector.open(filePath);
                if(fc.exists())  {
                    if(!fc.isDirectory()) {
                        long length = fc.fileSize();
                        byte[] buffer;
                        buffer = new byte[(int)(length-offset)];                            

                        is = fc.openInputStream();

                        long skip = 0;
                        while(offset >  skip) {
                            skip += is.skip(offset-skip);
                        }
                        
                        
                                                
                        is.read(buffer);


                        
                        return buffer;
                            
                    }
                    throw new IOException(filePath + "is  directory.");
                    
                }
                throw new IOException("File not Found");

            } finally {
//                if (isr != null) {
//                    isr.close();
//                }
                
//                if(dis != null) {
//                    dis.close();
//                }

                
                if (is != null) {
                    is.close();
                }
                
                
                if (fc != null) {
                    fc.close();
                }
            }
        }

    }
}
