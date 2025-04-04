package nl.tue.appdev.studie;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class FileServer {
    private static final String TAG = "FileServer";
    private static FTPClient initFTP() {
        try {
            Log.d(TAG, "Starting to initialize FTP connection");
            FTPClient ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(1000);
            ftpClient.connect(BuildConfig.FTP_HOSTNAME, 21);
            ftpClient.login(BuildConfig.FTP_USERNAME, BuildConfig.FTP_PASSWORD);
//                Log.d(TAG, PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (SocketException e) {
            Log.e(TAG, "SocketException when establishing connection with FTP server");
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.e(TAG, "IOException when establishing connection with FTP server");
            throw new RuntimeException(e);
        }
    }
    private static void disconnectFTP(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
                Log.d(TAG, "Successfully disconnected from FTP server");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to close FTP connection; shouldn't be a big problem?...", e);
        }
    }
    public static boolean upload(byte[] file, String remoteFileName) {
        FTPClient ftpClient = initFTP();
        try (InputStream inputStream = new ByteArrayInputStream(file)) {
            Log.d(TAG, "Starting to upload");
            return ftpClient.storeFile(remoteFileName, inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Failed to upload file", e);
            throw new RuntimeException(e);
        } finally {
            disconnectFTP(ftpClient);
        }
    }
    public static void download(String remoteFilename, File localFile) {
        FTPClient ftpClient = initFTP();
        try (FileOutputStream outputStream = new FileOutputStream((localFile))) {
            Log.d(TAG, "Started downloading file...");
            boolean success = ftpClient.retrieveFile(remoteFilename, outputStream);
            outputStream.close();
            Log.d(TAG, String.valueOf(localFile.length()));
            if (success) {
                Log.d(TAG, "File retrieved successfully");
            } else {
                throw new RuntimeException("Downloading file failed, success=false");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            disconnectFTP(ftpClient);
        }
    }
    /*public static void downloadFile(String remoteFileName) {
        FTPClient ftpClient = new FTPClient();
        try (FileOutputStream outputStream = new FileOutputStream((localFilePath))) {
            ftpClient.connect(BuildConfig.FTP_HOSTNAME, 21);
            ftpClient.login(BuildConfig.FTP_USERNAME, BuildConfig.FTP_PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Change to the directory containing the file
            ftpClient.changeWorkingDirectory("/upload");

            boolean success = ftpClient.retrieveFile(remoteFileName, outputStream);

            if (success) {
                Log.d(TAG, "File downloaded successfully");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to download file", e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to close FTP connection, shouldn't be too bad?...", e);
            }
        }
    }*/
}
