package i2.act.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {

  public static final String changeFileExtension(final String originalFileName,
      final String newExtension) {
    final int positionOfLastSeparator = originalFileName.lastIndexOf(File.separator);
    final int positionOfLastDot = originalFileName.lastIndexOf('.');

    if (positionOfLastDot <= positionOfLastSeparator) {
      // last file name of path does not contain a file extension -> append new extension
      return originalFileName + "." + newExtension;
    } else {
      // last file name of path already contains an extension -> replace it
      return originalFileName.substring(0, positionOfLastDot + 1) + newExtension;
    }
  }

  public static final String getFileExtension(final String fileName) {
    final int positionOfLastSeparator = fileName.lastIndexOf(File.separator);
    final int positionOfLastDot = fileName.lastIndexOf('.');

    if (positionOfLastDot <= positionOfLastSeparator) {
      return "";
    } else {
      return fileName.substring(positionOfLastDot + 1);
    }
  }

  public static final String prependBeforeFileExtension(final String originalFileName,
      final String prefix) {
    return changeFileExtension(originalFileName, prefix + "." + getFileExtension(originalFileName));
  }

  public static final String getBaseName(final String fileName) {
    final File file = new File(fileName);
    return file.getName();
  }

  public static final String getStrippedBaseName(final String fileName) {
    final String baseName = getBaseName(fileName);
    final int positionOfLastDot = baseName.lastIndexOf('.');

    if (positionOfLastDot == -1) {
      // base name does not contain a file extension
      return baseName;
    } else {
      // base name contains an extension -> remove it
      return baseName.substring(0, positionOfLastDot);
    }
  }

  public static final boolean fileExists(final String fileName) {
    final File file = new File(fileName);
    return fileExists(file);
  }

  public static final boolean fileExists(final File file) {
    return file.exists() && file.isFile();
  }

  public static final boolean deleteFile(final String fileName) {
    final File file = new File(fileName);
    return deleteFile(file);
  }

  public static final boolean deleteFile(final File file) {
    return file.delete();
  }

  public static final void createPathIfNotExists(final String fileName) {
    final File file = new File(fileName);
    createPathIfNotExists(file);
  }

  public static final void createPathIfNotExists(final File file) {
    if (file.getParentFile() != null) {
      file.getParentFile().mkdirs();
    }
  }

  public static final String readFile(final File file) {
    try {
      byte[] bytes = Files.readAllBytes(file.toPath());
      return new String(bytes);
    } catch (final IOException exception) {
      throw new RuntimeException("unable to read file", exception);
    }
  }

  public static final String readFile(final String fileName) {
    final File file = new File(fileName);
    return readFile(file);
  }

  public static final void writeToFile(final String content, final String fileName) {
    final SafeWriter writer = SafeWriter.openFile(fileName);
    writer.write(content);
    writer.close();
  }

  public static final List<File> findFiles(final Path directory, final String pattern) {
    return findFiles(directory, pattern, true);
  }

  public static final List<File> findFiles(final Path directory, final String pattern,
      final boolean recursive) {
    final List<File> files = new ArrayList<>();

    try {
      Files.walkFileTree(directory, (new SimpleFileVisitor<Path>() {

        private final PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + pattern);

        @Override
        public final FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
          final Path fileName = file.getFileName();

          if (fileName != null && this.matcher.matches(fileName)) {
            files.add(file.toFile());
          }

          return FileVisitResult.CONTINUE;
        }

        @Override
        public final FileVisitResult preVisitDirectory(final Path currentDirectory,
            final BasicFileAttributes attrs) {
          if (currentDirectory.equals(directory) || recursive) {
            return FileVisitResult.CONTINUE;
          } else {
            return FileVisitResult.SKIP_SUBTREE;
          }
        }

      }));
    } catch (final IOException exception) {
      throw new RuntimeException("unable to find files", exception);
    }

    return files;
  }

}
