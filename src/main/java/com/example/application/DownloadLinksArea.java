package com.example.application;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadLinksArea extends VerticalLayout {

  private final File uploadFolder;

  public DownloadLinksArea(File uploadFolder) {
    this.uploadFolder = uploadFolder;
    refreshFileLinks();
    setMargin(true);
  }

  private GridFile grid;

  public void refreshFileLinks() {
    removeAll();
    add(new H4("Порядок файлов на сайте. ВАЖНО - первый файл будет на главной странице"));
    //grid.removeAll();
    grid = new GridFile();
    grid.getDataView().addItems(Arrays.stream(uploadFolder.listFiles()).map(GridFile.GridItemHolder::new).toList());
    grid.getDataView().refreshAll();
    add(grid);
  }

  public void convert(Path dir, String filename) {
    try {
      final File[] files = Arrays.stream(uploadFolder.listFiles())
          .filter(file -> !"result".equals(file.getName())).toArray(File[]::new);
      Thumbnails.of(files)
          .size(1588, 1800)
          .outputQuality(0.80)
          .toFiles(dir.toFile(), Rename.NO_CHANGE);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    try {
      addLinkToFile(Path.of(createThumbnailZip(dir, filename)).toFile());
      System.out.println("Zip file '" + dir + "' created successfully.");
    } catch (IOException e) {
      System.out.println("Error creating zip file: " + e.getMessage());
    }
  }

  public static String createThumbnailZip(Path path, String fileName) throws IOException {
    File[] files = path.toFile().listFiles();

    final String name = fileName + ".zip";
    try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(name))) {
      for (File file : files) {
        if (file.isFile()) {
          addToZip(file, zipOut);
        }
      }
    }
    return name;
  }

  private static void addToZip(File file, ZipOutputStream zipOut) throws IOException {
    byte[] buffer = new byte[1024];
    FileInputStream fis = new FileInputStream(file);
    zipOut.putNextEntry(new ZipEntry(file.getName()));

    int length;
    while ((length = fis.read(buffer)) > 0) {
      zipOut.write(buffer, 0, length);
    }

    zipOut.closeEntry();
    fis.close();
  }

  private void addLinkToFile(File file) {
    StreamResource streamResource = new StreamResource(file.getName(), () -> getStream(file));
    Anchor link = new Anchor(streamResource, String.format("%s (%d KB)", file.getName(),
        (int) file.length() / 1024));
    link.getElement().setAttribute("download", true);
    add(link);
  }

  private InputStream getStream(File file) {
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return stream;
  }

  public List<GridFile.GridItemHolder> getFiles() {
    return grid.getDataView().getItems().toList();
  }
}
