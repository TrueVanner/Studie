package nl.tue.appdev.studie;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileServer {
    private static final String TAG = "FileServer";
    public static boolean upload(byte[] file, String remoteFileName) {
        FTPClient ftpClient = new FTPClient();
        try (InputStream inputStream = new ByteArrayInputStream(file)) {
            Log.d(TAG, "Starting to initialize FTP connection");
            ftpClient.setConnectTimeout(1000);
            ftpClient.connect(BuildConfig.FTP_HOSTNAME, 21);
            ftpClient.login(BuildConfig.FTP_USERNAME, BuildConfig.FTP_PASSWORD);
//                Log.d(TAG, PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Change to the upload directory
            if(!ftpClient.changeWorkingDirectory("/upload")) {
                Log.e(TAG, "Failed to change working directory");
            }

            Log.d(TAG, "Initialized FTP connection, starting to upload");
            return ftpClient.storeFile(remoteFileName, inputStream);

        } catch (IOException e) {
            Log.e(TAG, "Failed to upload file / establish connection with FTP server", e);
            throw new RuntimeException(e);
        } finally {
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
    }
    public static void downloadFile(String remoteFileName, String localFilePath) {
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
    }
}
