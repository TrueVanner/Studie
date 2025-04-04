An Android application developed in a team of 6 as part of App Development course at TU/e. 
### List of 3rd party Java sources:
- android-pdf-viewer (com.github.barteksc:android-pdf-viewer)
- commons-net ("commons-net:commons-net")
- firebase-auth ("com.google.firebase:firebase-auth")
- firebase-bom ("com.google.firebase:firebase-auth")
- firebase-database ("com.google.firebase")
- firebase-storage ("com.google.firebase")
- mockito-core ("org.mockito:mockito-core")

For the project, an FTP server was used to store large files. The setup instructions are as follows:
- Install Docker
- Run ```docker pull bogem/ftp```
- Run ```docker run -d -v <host folder>:/home/vsftpd \
				-p 20:20 -p 21:21 -p 47400-47470:47400-47470 \
				-e FTP_USER=<username> \
				-e FTP_PASS=<password> \
				-e PASV_ADDRESS=<ip address of your server> \
				--name ftp \
				--restart=always bogem/ftp```
replacing the relevant fields with local information
- Update the relevant values in local credentials file.
