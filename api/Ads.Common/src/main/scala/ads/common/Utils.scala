package ads.common

import org.apache.commons.net.ftp.{FTPHTTPClient, FTP, FTPClient}
import java.io.{FileInputStream, File}



object FtpUtils {

    def upload(ftpServer: String, port: Int, username: String, password: String, file: File, location: String): Boolean = {
        val ftpClient = new FTPClient()

        try {
            ftpClient.connect(ftpServer, port)
            ftpClient.login(username, password)
            ftpClient.enterLocalPassiveMode()


            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            var dirExist = ftpClient.changeWorkingDirectory(location)
            if (!dirExist){
                val b: Boolean = makeDirectories(ftpClient, location)
                if (!b) return false
                dirExist = ftpClient.changeWorkingDirectory(location)
            }
            if (!dirExist)
                return false

            val firstRemoteFile = file.getName
            val inputStream = new FileInputStream(file)

            val  done = ftpClient.storeFile(firstRemoteFile, inputStream)
            inputStream.close
            if (!done)
                return false
            return true

        }
        catch {
            case ex: Exception => {
                ex.printStackTrace
                //println(ex.getStackTrace)
                return false
            }
        }
        finally {
            try {
                if (ftpClient.isConnected) {
                    ftpClient.logout
                    ftpClient.disconnect
                }
            } catch {
                case ex: Exception => ex.printStackTrace
            }
        }
    }

    def uploadByProxy(proxyHost: String, proxyPort: Int, ftpServer: String, port: Int, username: String, password: String, file: File, location: String): Boolean = {
        println("UPLOAD BY PROXY")
        val ftpClient = new FTPHTTPClient(proxyHost, proxyPort)

        try {
            ftpClient.connect(ftpServer, port)
            ftpClient.login(username, password)
            ftpClient.enterLocalPassiveMode()


            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            var dirExist = ftpClient.changeWorkingDirectory(location)
            if (!dirExist){
                val b: Boolean = makeDirectories(ftpClient, location)
                if (!b) return false
                dirExist = ftpClient.changeWorkingDirectory(location)
            }
            if (!dirExist)
                return false

            val firstRemoteFile = file.getName
            val inputStream = new FileInputStream(file)

            val  done = ftpClient.storeFile(firstRemoteFile, inputStream)
            inputStream.close
            if (!done)
                return false
            return true

        }
        catch {
            case ex: Exception => {
                ex.printStackTrace
                //println(ex.getStackTrace)
                return false
            }
        }
        finally {
            try {
                if (ftpClient.isConnected) {
                    ftpClient.logout
                    ftpClient.disconnect
                }
            } catch {
                case ex: Exception => ex.printStackTrace
            }
        }
    }


    def makeDirectories(ftpClient: FTPClient, dirPath: String): Boolean = {
        val  pathElements = dirPath.split("/")
        val currentPath = ftpClient.printWorkingDirectory()
        if (pathElements != null && pathElements.length > 0) {
            for (singleDir <- pathElements) {
                val existed = ftpClient.changeWorkingDirectory(singleDir)
                if (!existed) {
                    val created = ftpClient.makeDirectory(singleDir)
                    if (created) {
                        ftpClient.changeWorkingDirectory(singleDir)
                    } else {
                        ftpClient.changeWorkingDirectory(currentPath)
                        return false;
                    }
                }
            }
        }
        ftpClient.changeWorkingDirectory(currentPath)
        return true
    }
}