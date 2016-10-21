package utils;


import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Srinivas Kandibanda
 * @version 1.0, Sep 13, 2010
 */
public class GetBikeUtils {

    public static final String SHORT_DATE_FORMAT = "dd MMM, yy";
    public static final String SHORT_DAY_MONTH_FORMAT = "dd-MMM-yyyy";
    public static final String DATE_MONTH_FULL_YEAR_FORMAT_FOLLOWED_BY_SPACES = "dd MMM yyyy";
    public static final String YEAR_MONTH_DATE_WITHOUT_SPACE_FORMAT = "yyMMdd";
    public static final String DATE_MONTH_YEAR_FORMAT = "dd-MMM-yy";
    public static final String DATE_MONTH_YEAR_FORMAT_FOLLOWED_BY_SPACES = "dd MMM yy";
    public static final String[] VALID_DATE_FORMATS_ARRAY = {
            SHORT_DATE_FORMAT,
            SHORT_DAY_MONTH_FORMAT,
            DATE_MONTH_FULL_YEAR_FORMAT_FOLLOWED_BY_SPACES,
            YEAR_MONTH_DATE_WITHOUT_SPACE_FORMAT,
            DATE_MONTH_YEAR_FORMAT,
            DATE_MONTH_YEAR_FORMAT_FOLLOWED_BY_SPACES};
    public static final String VALID_DATE_FORMATS = "'" + SHORT_DATE_FORMAT + "', '" + SHORT_DAY_MONTH_FORMAT + "', '" + DATE_MONTH_FULL_YEAR_FORMAT_FOLLOWED_BY_SPACES + "', '" + YEAR_MONTH_DATE_WITHOUT_SPACE_FORMAT + "', '" + DATE_MONTH_YEAR_FORMAT + "', '" + DATE_MONTH_YEAR_FORMAT_FOLLOWED_BY_SPACES + "'";
    public static final String NEW_LINE = "\n";
    public static final String LINE_DELIMITERS = "\r\n";
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String HASH = "#";
    public static final double HUNDRED_PERCENT = 1.0;
    public static final String COMMA = ",";
    public static final String PERIOD = ".";
    public static final String COLON = ":";
    public static final String PIPE = "|";
    public static final String TILDA = "~";
    public static final char EQUAL_CH = '=';
    public static final String EQUAL = Character.toString(EQUAL_CH);

    public static Date parseShortDate(String dateString) {
        DatePointer result = new DatePointer();
        if (StringUtils.isNotNullAndEmpty(dateString)) {
            for (String dateFormat : VALID_DATE_FORMATS_ARRAY) {
                parseDate(result, dateString, dateFormat);
                if (result.getDate() != null) {
                    break;
                }
            }
            if (result.getDate() == null) {
                GetBikeLogger.fatal("Could not parse the date " + dateString);
            }
        }
        return result.getDate();
    }

    public static void parseDate(DatePointer datePointer, String dateString, String pattern) {
        if (datePointer.getDate() == null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                datePointer.setDate(sdf.parse(dateString));
            } catch (ParseException parseException) {
            }
        }
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getUniqueErrorId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(new Date());
    }

    public static String getUniqueId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

    public static String getFileName(String filePath) {
        File file = null;
        file = new File(filePath);
        return file.getName();
    }

    public static String getFileNameWithoutExtension(String filePath) {
        File file = null;
        file = new File(filePath);
        String result = file.getName();
        final int lastIndex = result.lastIndexOf(PERIOD);
        if (lastIndex >= 0) {
            result = result.substring(0, lastIndex);
        }
        return result;
    }

    public static Boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static File createFile(String fileName, InputStream inputStream) {
        File file = null;
        try {
            file = new File(fileName);
            file.createNewFile();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            FileWriter fw = new java.io.FileWriter(file);
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                fw.write(line + "\n");
            }
            inputStream.close();
            fw.close();
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex.getMessage());
            GetBikeLogger.fatal(ioex);
        }
        return file;
    }

    public static File createTempFile(String fileName, String extension, InputStream inputStream) {
        File file = null;
        try {
            file = File.createTempFile(fileName, extension);
            file.deleteOnExit();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            FileWriter fw = new java.io.FileWriter(file);
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                fw.write(line + "\n");
            }
            inputStream.close();
            fw.close();
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex.getMessage());
            GetBikeLogger.fatal(ioex);
        }
        return file;
    }

    public static File createBinaryTempFile(String fileName, String extension, InputStream inputStream) {
        File file = null;
        try {
            file = File.createTempFile(fileName, extension);
            file.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            outputStream.write(bytes);
            inputStream.close();
            outputStream.close();
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex.getMessage());
            GetBikeLogger.fatal(ioex);
        }
        return file;
    }

    public static void createTextFile(String filePath, StringBuilder fileContents) {
        if (filePath != null && fileContents != null) {
            try {
                OutputStream outputStream = new FileOutputStream(new File(filePath));
                outputStream.write(fileContents.toString().getBytes());
                outputStream.close();
            } catch (Exception ex) {
                GetBikeLogger.fatal(ex);
                throw new GetBikeRuntimeException("Could not export the file " + filePath);
            }
        }
    }

    public static List<String> getLines(InputStream inputDataStream) {
        List<String> result = new ArrayList<String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputDataStream));
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.startsWith("#")) {
                    continue;
                }
                result.add(line);
            }
        } catch (Exception e) {
            GetBikeLogger.fatal(e);
            throw new GetBikeRuntimeException();
        }
        return result;
    }

    public static BufferedReader createBufferedReader(String fileName) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex);
            throw new GetBikeRuntimeException("Failed to open the file : " + fileName);
        }
        return bufferedReader;
    }

    public static InputStream createInputStream(String fileName) {
        InputStream result;
        try {
            result = new FileInputStream(fileName);
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex);
            throw new GetBikeRuntimeException("Failed to open the file : " + fileName);
        }
        return result;
    }

    public static void closeBufferedReader(BufferedReader bufferedReader) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex);
            throw new GetBikeRuntimeException();
        }
    }

    public static void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ioex) {
            GetBikeLogger.fatal(ioex);
            throw new GetBikeRuntimeException();
        }
    }

    public static String getFileParent(String filePath) {
        String result = null;
        if (StringUtils.isNotNullAndEmpty(filePath)) {
            File file = new File(filePath);
            if (!StringUtils.isNullOrEmptyObject(file)) {
                result = file.getParent();
            }
        }
        return result;
    }

    public static void copyDirectory(File sourceFile, File destinationFile) {
        try {
            if (sourceFile.isDirectory()) {
                if (!destinationFile.exists()) {
                    destinationFile.mkdir();
                }
                String files[] = sourceFile.list();
                for (String file : files) {
                    File fileFromSource = new File(sourceFile, file);
                    File fileFromDestination = new File(destinationFile, file);
                    copyDirectory(fileFromSource, fileFromDestination);
                }
            } else {
                InputStream in = new FileInputStream(sourceFile);
                OutputStream out = new FileOutputStream(destinationFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            }
        } catch (IOException exception) {
            GetBikeLogger.fatal(exception);
        }
    }

    public static File getMatchedFileFromListOfFiles(File[] files, String fileName) {
        File result = null;
        for (File file : files) {
            if (fileName.equals(file.getName())) {
                result = file;
                break;
            }
        }
        return result;
    }

    public static String backupDirectory(File projectFolder) {
        String result = GetBikeUtils.getUniqueErrorId();
        File backupFolderForProject = new File(projectFolder.getPath() + StringUtils.UNDER_SCORE + "backup" + StringUtils.UNDER_SCORE + result);
        GetBikeUtils.copyDirectory(projectFolder, backupFolderForProject);
        return result;
    }

    public static String getTrimmedString(String mnemonic, int mnemonicWidthInPixels, int widthInPixels) {
        String result = "";
        int mnemonicLength = mnemonic.length();
        if (mnemonicWidthInPixels <= widthInPixels || mnemonicLength <= 3) {
            result = mnemonic;
        } else {
            int pixelsPerCharcter = mnemonicWidthInPixels / mnemonicLength;
            int requiredCharacters = widthInPixels / pixelsPerCharcter;
            if (requiredCharacters == mnemonicLength) {
                result = mnemonic;
            } else {
                int endIndex = requiredCharacters - 3;
                result = mnemonic.substring(0, endIndex > NumericUtils.INTEGER_ZERO ? endIndex : 1);
                result += "...";
            }
        }
        return result.trim();
    }
}

