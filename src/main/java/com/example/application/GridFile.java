package com.example.application;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GridFile extends Div {
  public record GridItemHolder(File file) {
    private String getFilename() {
      return file.getPath();
    }
  }

  private GridItemHolder draggedItem;
  private final GridListDataView<GridItemHolder> dataView;

  public GridListDataView<GridItemHolder> getDataView() {
    return dataView;
  }

  private final Grid<GridItemHolder> grid;

  public Grid<GridItemHolder> getGrid() {
    return grid;
  }

  public GridFile() {
    grid = setupGrid();

    // Modifying the data view requires a mutable collection
    List<GridItemHolder> people = new ArrayList<>();
    dataView = grid.setItems(people);
    grid.setDropMode(GridDropMode.BETWEEN);
    grid.setRowsDraggable(true);

    grid.addDragStartListener(
        e -> draggedItem = e.getDraggedItems().get(0));

    grid.addDropListener(e -> {
      GridItemHolder targetPerson = e.getDropTargetItem().orElse(null);
      GridDropLocation dropLocation = e.getDropLocation();

      boolean personWasDroppedOntoItself = draggedItem.equals(targetPerson);

      if (targetPerson == null || personWasDroppedOntoItself)
        return;

      dataView.removeItem(draggedItem);

      if (dropLocation == GridDropLocation.BELOW) {
        dataView.addItemAfter(draggedItem, targetPerson);
      } else {
        dataView.addItemBefore(draggedItem, targetPerson);
      }
    });

    grid.addDragEndListener(e -> draggedItem = null);

    add(grid);
  }

  private static Grid<GridItemHolder> setupGrid() {
    Grid<GridItemHolder> grid = new Grid<>(GridItemHolder.class, false);
    grid.setWidth("400px");
    grid.setHeight("400px");
    grid.addColumn(GridItemHolder::getFilename)
        .setHeader("Имя файла")
        .setAutoWidth(true)
        .setFlexGrow(0);

    return grid;
  }
}
