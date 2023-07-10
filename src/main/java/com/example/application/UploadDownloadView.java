package com.example.application;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.JsonObject;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route("upload-download")
@CssImport("./themes/myapp/styles.css")
@RouteAlias("")
public class UploadDownloadView extends VerticalLayout {
  //private Path uploadDirUrl = Path.of("c:", "rita", "public", "assets", "art_doll");

  private static final String text = """
      ---
      title: ':$title'
      excerpt: ':$excerpt'
      coverImage: '/assets/art_doll/:$coverImage'
      moreImages:
        folder: '/assets/art_doll/:$moreImagesFolder/'
        files: [:$moreImagesFiles]
      date: ':$dateCreate'
      categories: [:$categories]
      author:
        name: Rita Bogovick
        picture: '/assets/footer.jpg'
      ogImage:
        url: '/assets/art_doll/:$ogImage'
      status: :$status
      id: :$id
      ---
      :$text      
      """;


  private Button refreshPage() {
    Button refreshButton = new Button("Очистить");
    refreshButton.addClickListener(e -> getUI().get().getPage().reload());

    return refreshButton;
  }

  public UploadDownloadView() {
    VaadinSession.getCurrent().setErrorHandler(new GlobalErrorHandler());
    HorizontalLayout horizontalLayout = new HorizontalLayout();
    horizontalLayout.setClassName("cust-hor");
    Div layout = new Div();
    layout.setClassName("cust-padd");
    TextField folder = new TextField("Название папки", "Папка для файлов");
    TextField title = new TextField("Название листинга");
    TextField shortDescription = new TextField("Краткое описание");
    TextArea fullDescription = new TextArea("Полное описание");
    fullDescription.setLabel("Здесь нужно вставить описание....");
    fullDescription.setValueChangeMode(ValueChangeMode.EAGER);
    TextField categories = new TextField("Категории. Перечислять категории через ПРОБЕЛ", "Перечислять категории через ПРОБЕЛ");
    Checkbox status = new Checkbox("Поставь галочку если уже продан");
    shortDescription.setWidth("500px");
    folder.setWidth("300px");
    layout.setWidth("500px");
    status.setWidth("500px");
    categories.setWidth("500px");
    fullDescription.setWidth("500px");
    fullDescription.setHeight("200px");
    title.setWidth("500px");
    HorizontalLayout appl = new HorizontalLayout();
    appl.setClassName("cust-folder");
    Button applyFolder = new Button("Добавить фотки");
    applyFolder.setWidth("200px");
    appl.add(folder, applyFolder);

    HorizontalLayout hbox = new HorizontalLayout();
    Button save = new Button("Сгенерировать архив");
    save.setWidth("300px");
    hbox.add(save, refreshPage());

    layout.add(appl, title, shortDescription, fullDescription, categories, status, hbox);
    HorizontalLayout uploadDiv = new HorizontalLayout();
    applyFolder.addClickListener(ev -> {
      uploadDiv.removeAll();
      Assert.hasText(folder.getValue(), "Заполните поле - 'Название папки' ЭТО ВАЖНО");
      File uploadFolder = getUploadFolder(folder);
      UploadArea uploadArea = new UploadArea(uploadFolder);
      DownloadLinksArea linksArea = new DownloadLinksArea(uploadFolder);


      save.addClickListener(e ->
          createListing(
              folder,
              title,
              fullDescription,
              categories,
              status,
              linksArea
          ));

      uploadArea.getUploadField().getElement().addEventListener("file-remove", event -> {
        JsonObject eventData = event.getEventData();
        getUploadFolder(folder).toPath().resolve(eventData.getString("event.detail.file.name")).toFile().delete();
        linksArea.refreshFileLinks();
      }).addEventData("event.detail.file.name");

      uploadArea.getUploadField().addSucceededListener(e -> {

        //linksArea.convert();
      });
      uploadArea.getUploadField().addAllFinishedListener(e -> {
        //linksArea.convert();
        uploadArea.hideErrorField();
        linksArea.refreshFileLinks();
      });
      uploadDiv.add(uploadArea, linksArea);
    });


    horizontalLayout.add(layout, uploadDiv);

    add(horizontalLayout);
  }

  private static void createListing(TextField folder, TextField title, TextArea fullDescription,
                                    TextField categories, Checkbox status,
                                    DownloadLinksArea linksArea) {
    String result = text.replace(":$id", String.valueOf(System.currentTimeMillis()))
        .replace(":$dateCreate", LocalDateTime.now().toString())
        .replace(":$title", title.getValue())
        .replace(":$excerpt", title.getValue())
        .replace(":$moreImagesFolder", folder.getValue())
        .replace(":$text", fullDescription.getValue())
        .replace(":$categories", getCategoriesValue(categories))
        .replace(":$status", Boolean.TRUE.equals(status.getValue()) ? "1" : "0");
    final List<GridFile.GridItemHolder> files = linksArea.getFiles();
    if (!CollectionUtils.isEmpty(files)) {
      result = result.replace(":$coverImage", normalized(files.get(0).file()));
      result = result.replace(":$ogImage", normalized(files.get(0).file()));
      if (files.size() > 1) {
        result = result.replace(":$moreImagesFiles", files.subList(1, files.size()).stream().map(f ->
            "'" + f.file().getName() + "'"
        ).collect(Collectors.joining(",")));
      }
    }
    Assert.hasText(folder.getValue(), "Заполните поле - 'Название папки' ЭТО ВАЖНО");
    /*File resultFolder = uploadPath.resolve(folder.getValue()).normalize().toFile();
    if (resultFolder.exists()) {
      try {
        Files.deleteIfExists(resultFolder.toPath());
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    } else {
      resultFolder.mkdirs();
    }*/
    ///assets/art_doll C:\Users\Maxim\Documents\git_project\rita-blog\_posts\baba-yaga.md
    //linksArea.convert(resultFolder);
    try {
      Path path = Files.createTempDirectory(folder.getValue());
      try {
        //Files.writeString(Paths.get("C:\\Users\\Maxim\\Documents\\git_project\\rita-blog\\_posts")
        Files.writeString(path.resolve(folder.getValue().replaceAll("/", "_") + ".md"),
            result, StandardOpenOption.CREATE_NEW);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      linksArea.convert(path, folder.getValue());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private static String getCategoriesValue(TextField categories) {
    return StringUtils.hasText(categories.getValue()) ?
        Arrays.stream(categories.getValue().split(" ")).map(item -> "'" + item + "'").collect(Collectors.joining(","))
        : "";
  }

  private static String normalized(File file) {
    return normalized(file.getPath());
  }

  private static String normalized(String file) {
    return file
        .replace("uploaded-files\\", "")
        .replaceAll("\\\\", "/");
  }

  private static File getUploadFolder(TextField folderComponent) {
    File folder = Paths.get("uploaded-files", folderComponent.getValue()).normalize().toFile();
    if (!folder.exists()) {
      folder.mkdirs();
    }
    return folder;

  }
}
