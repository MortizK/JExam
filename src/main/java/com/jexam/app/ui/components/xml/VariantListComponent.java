package com.jexam.app.ui.components.xml;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Variant list with add/delete actions.
 *
 * @author Moritz
 */
public final class VariantListComponent extends VBox {
    private final ListView<String> listView = new ListView<>();
    private final Label titleLabel = new Label("Variants");
    private Consumer<Integer> selectHandler = index -> { };
    private Runnable createHandler = () -> { };
    private Consumer<Integer> deleteHandler = index -> { };
    private Runnable enterHandler = () -> { };
    private Runnable tabForwardHandler = () -> { };
    private Runnable tabBackwardHandler = () -> { };
    private boolean updating;

    /**
     * Creates the variant list with add/delete actions and keyboard navigation.
     */
    public VariantListComponent() {
        getStyleClass().add("variant-list");
        setSpacing(6);
        setPadding(new Insets(0, 0, 0, 0));

        Button addButton = new Button("Add Variant");
        HBox actions = new HBox(6, addButton);
        titleLabel.getStyleClass().add("section-label");
        addButton.getStyleClass().add("primary-action");
        listView.getStyleClass().add("variant-items");
        setFillWidth(true);
        setMinHeight(0);
        listView.setMinHeight(0);
        listView.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(listView, Priority.ALWAYS);
        addButton.setOnAction(event -> createHandler.run());
        listView.setCellFactory(list -> new VariantRowCell());
        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                int index = listView.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    deleteHandler.accept(index);
                }
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.ENTER) {
                enterHandler.run();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.TAB) {
                if (event.isShiftDown()) {
                    tabBackwardHandler.run();
                } else {
                    tabForwardHandler.run();
                }
                event.consume();
            }
        });
        listView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating && newValue != null) {
                selectHandler.accept(newValue.intValue());
            }
        });
        getChildren().addAll(titleLabel, listView, actions);
    }

    /**
     * Updates list view items without triggering selection callbacks.
     *
     * @param values variant question labels
     */
    public void setItems(final List<String> values) {
        updating = true;
        try {
            listView.setItems(FXCollections.observableArrayList(values));
        } finally {
            updating = false;
        }
    }

    /**
     * Sets the selected row index without triggering selection callbacks.
     *
     * @param index selected variant index
     */
    public void setSelectedIndex(final int index) {
        updating = true;
        try {
            if (index >= 0 && index < listView.getItems().size()) {
                listView.getSelectionModel().select(index);
            } else {
                listView.getSelectionModel().clearSelection();
            }
        } finally {
            updating = false;
        }
    }

    /**
     * Registers a callback for row selection changes.
     *
     * @param handler callback receiving selected index; {@code null} resets to no-op
     */
    public void setOnSelect(final Consumer<Integer> handler) {
        selectHandler = handler == null ? index -> { } : handler;
    }

    /**
     * Registers a callback for create action.
     *
     * @param handler callback for add action; {@code null} resets to no-op
     */
    public void setOnCreate(final Runnable handler) {
        createHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers a callback for delete action.
     *
     * @param handler callback receiving deleted index; {@code null} resets to no-op
     */
    public void setOnDelete(final Consumer<Integer> handler) {
        deleteHandler = handler == null ? index -> { } : handler;
    }

    /**
     * Registers a callback for Enter key action.
     *
     * @param handler callback for enter; {@code null} resets to no-op
     */
    public void setOnEnter(final Runnable handler) {
        enterHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers callbacks for Tab navigation (forward and backward).
     *
     * @param onForward callback for forward navigation; {@code null} resets to no-op
     * @param onBackward callback for backward navigation; {@code null} resets to no-op
     */
    public void setOnTabNavigation(final Runnable onForward, final Runnable onBackward) {
        tabForwardHandler = onForward == null ? () -> { } : onForward;
        tabBackwardHandler = onBackward == null ? () -> { } : onBackward;
    }

    /**
     * Requests keyboard focus for the list.
     */
    public void requestTableFocus() {
        listView.requestFocus();
    }

    private final class VariantRowCell extends ListCell<String> {
        private final Label textLabel = new Label();
        private final Button deleteButton = new Button("Delete");
        private final HBox content = new HBox(8, textLabel, deleteButton);

        private VariantRowCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textLabel.setWrapText(true);
            textLabel.getStyleClass().add("variant-item-text");
            deleteButton.getStyleClass().add("secondary-action");
            textLabel.maxWidthProperty().bind(listView.widthProperty().subtract(80));
            HBox.setHgrow(textLabel, Priority.ALWAYS);
            deleteButton.setFocusTraversable(false);
            deleteButton.setOnAction(event -> {
                if (!isEmpty() && getIndex() >= 0) {
                    deleteHandler.accept(getIndex());
                }
                event.consume();
            });
        }

        @Override
        protected void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            textLabel.setText(item);
            setGraphic(content);
        }
    }
}
