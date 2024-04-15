package multiverse.cr_downloader.exceptions;

public class CRDownloaderException extends Exception {
    public CRDownloaderException(String message) {
        super(message);
    }

    public CRDownloaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
