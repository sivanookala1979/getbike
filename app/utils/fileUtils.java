package utils;

import org.apache.commons.io.FileUtils;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Wahid on 15/3/17.
 */
public class fileUtils {

    public static String fileUpload(Http.MultipartFormData.FilePart<File> file, String url) {
        String fileName = "";
        if (file != null) {
            fileName = url + "-" + UUID.randomUUID() + file.getFilename();
            File destFile = (File) file.getFile();
            try {
                FileUtils.moveFile(destFile, new File("public/uploads/", fileName));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("File uploaded!");
        } else {
            System.out.println(" No images are uploaded !");
        }

        return fileName;

    }

    public static void delete(File file) {
        boolean success = false;
        if (file.isDirectory()) {
            for (File deleteMe : file.listFiles()) {
                // recursive delete
                delete(deleteMe);
            }
        }
        success = file.delete();
        if (success) {
            System.out.println(file.getAbsoluteFile() + " Deleted");
        } else {
            System.out.println(file.getAbsoluteFile() + " Deletion failed!!!");
        }
    }
}
