package com.example.application;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.MultiFileReceiver;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class UploadArea extends VerticalLayout {

    private final Upload uploadField;
    private final Span errorField;

    public UploadArea(File uploadFolder) {
        uploadField = new Upload(createFileReceiver(uploadFolder));
        uploadField.setI18n(new UploadI18N());
        uploadField.getI18n().setDropFiles(new UploadI18N.DropFiles().setOne("Перетащить сюда"));
        uploadField.getI18n().setAddFiles(new UploadI18N.AddFiles().setOne("Загрузить файлы"));
        uploadField.setMaxFiles(100);
        // set max file size to 1 MB
        uploadField.setMaxFileSize(15 * 1024 * 1024);
        uploadField.setUploadButton(new Button("Загрузить файлы"));
        uploadField.setDropLabel(new Label("Или перетащить сюда"));

        errorField = new Span();
        errorField.setVisible(false);
        errorField.getStyle().set("color", "red");

        uploadField.addFailedListener(e -> showErrorMessage(e.getReason().getMessage()));
        uploadField.addFileRejectedListener(e -> showErrorMessage(e.getErrorMessage()));

        add(uploadField, errorField);
    }

    public Upload getUploadField() {
        return uploadField;
    }

    public void hideErrorField() {
        errorField.setVisible(false);
    }

    private Receiver createFileReceiver(File uploadFolder) {
        return (MultiFileReceiver) (filename, mimeType) -> {
            File file = new File(uploadFolder, filename);
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                return null;
            }
        };
    }

    private void showErrorMessage(String message) {
        errorField.setVisible(true);
        errorField.setText(message);
    }
}
