package floor.file.storage;

import java.nio.file.Path;

public interface FileStorage extends AutoCloseable {

    String upload(Path path, String suffix) throws UploadFileException;

    String upload(byte[] bytes, String suffix) throws UploadFileException;

    String download(String link);

    void delete(String link);
}
