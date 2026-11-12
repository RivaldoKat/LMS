package Learning.Management.System.service;

import Learning.Management.System.exceptions.StorageException;
import Learning.Management.System.exceptions.StorageFileNotFoundException;
import Learning.Management.System.repository.StorageRepo;
import Learning.Management.System.storage.StorageProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService implements StorageRepo {

    private final Path rootLocation;

    @Autowired
    public FileStorageService(StorageProperties properties) {
        String location = properties.getLocation();
        if (location == null || location.isBlank()) {
            throw new StorageException("File upload location can not be empty");
        }

        this.rootLocation = Paths.get(location.trim()).toAbsolutePath().normalize();
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Failed to store empty file");
        }

        String extension = getExtension(file);

        String storedFilename = UUID.randomUUID() + extension;
        Path destinationFile = rootLocation.resolve(storedFilename);
        try {
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
        return storedFilename;
    }

    private static @NonNull String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new StorageException("Uploaded file must have a filename");
        }

        String extension = "";
        int extensionSeparator = originalFilename.lastIndexOf('.');
        if (extensionSeparator >= 0) {
            String candidateExtension = originalFilename.substring(extensionSeparator + 1);
            if (candidateExtension.matches("[A-Za-z0-9]{1,10}")) {
                extension = "." + candidateExtension;
            }
        }
        return extension;
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.list(this.rootLocation)
                    .filter(Files::isRegularFile)
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files.", e);
        }
    }

    @Override
    public Path load(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new StorageFileNotFoundException("File not found");
        }
        Path file = rootLocation.resolve(filename).normalize();
        if (!file.startsWith(rootLocation) || file.equals(rootLocation)) {
            throw new StorageFileNotFoundException("Invalid file path");
        }
        return file;
    }

    @Override
    public Resource loadAsResource(String filename) {
        try{
            Path realRoot = rootLocation.toRealPath();
            Path realFile = load(filename).toRealPath();
            if (!realFile.startsWith(realRoot)
                    || !Files.isRegularFile(realFile)
                    || !Files.isReadable(realFile)) {
                throw new StorageFileNotFoundException("Could not read file:" + filename);
            }
            return new UrlResource(realFile.toUri());
        }
        catch (IOException e) {
            throw new StorageFileNotFoundException("Could not read file:" + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

}
