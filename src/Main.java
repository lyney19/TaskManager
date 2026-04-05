import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main extends Application {

    private final BorderPane mainPane = new BorderPane();
    private final VBox form = new VBox();
    private final ListView<TaskProperty> taskView = new ListView<>();

    private final TaskProperty task = new TaskProperty();

    private final VBox leftPane = new VBox();
    private final HBox searchingTools = new HBox();
    private final VBox searchingCheckboxes = new VBox();

    private final ComboBox<String> sortedComboBox = new ComboBox<>();
    private final StringProperty sortedMode = new SimpleStringProperty();

    private final ComboBox<String> filterComboBox = new ComboBox<>();
    private final StringProperty filterMode = new SimpleStringProperty();

    private final ObservableList<String> sortedPropertyList = FXCollections.observableArrayList(
        "Sort by name (A-Z)",
            "Sort by name (Z-A)",
            "Sort by status (uncompleted first)",
            "Sort by status (completed first)"
    );

    private final ObservableList<String> filterPropertyList = FXCollections.observableArrayList();
    private final CheckBox useFilterCheckbox = new CheckBox();
    private final BooleanProperty useFilter = new SimpleBooleanProperty();

    private final CheckBox showCompletedCheckbox = new CheckBox();
    private final BooleanProperty showCompleted = new SimpleBooleanProperty();

    private final CheckBox showUncompletedCheckbox = new CheckBox();
    private final BooleanProperty showUncompleted = new SimpleBooleanProperty();


    private final TextField titleField = new TextField();
    private final TextField categoryField = new TextField();
    private final CheckBox completedCheckbox = new CheckBox();
    private final Button addTaskButton = new Button("Add new task");

    private final ObservableList<TaskProperty> tasks = FXCollections.observableArrayList(
            task -> new Observable[] { task.titleProperty(), task.categoryProperty(), task.completedProperty() }
    );
    private final FilteredList<TaskProperty> filteredTasks = new FilteredList<>(tasks);
    private final SortedList<TaskProperty> sortedFilteredTasks = new SortedList<>(filteredTasks);

    private Comparator<TaskProperty> createComparator(String mode) {
        return switch (mode) {
            case "Sort by name (Z-A)" -> Comparator.comparing(TaskProperty::getTitle).reversed()
                    .thenComparing(TaskProperty::getCategory)
                    .thenComparing(TaskProperty::completed);

            case "Sort by status (uncompleted first)" -> Comparator.comparing(TaskProperty::completed)
                    .thenComparing(TaskProperty::getTitle)
                    .thenComparing(TaskProperty::getCategory);

            case "Sort by status (completed first)" -> Comparator.comparing(TaskProperty::completed).reversed()
                    .thenComparing(TaskProperty::getTitle)
                    .thenComparing(TaskProperty::getCategory);

            default -> Comparator.comparing(TaskProperty::getTitle)
                    .thenComparing(TaskProperty::getCategory)
                    .thenComparing(TaskProperty::completed);
        };
    }

    private Predicate<TaskProperty> createPredicate(
            boolean useFilter,
            boolean showCompleted,
            boolean showUncompleted,
            String category
    ) {
        return task -> {
            boolean matchesCompletion = task.completed() ? showCompleted : showUncompleted;
            boolean matchesCategory = category == null || category.isBlank() || task.getCategory().equals(category);
            boolean filterDisabled = !useFilter;

            return filterDisabled || (matchesCompletion && matchesCategory);
        };
    }
    
    private void notifyUser(String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operation failed...");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) {

        sortedComboBox.setValue(sortedPropertyList.getFirst());
        sortedComboBox.setItems(sortedPropertyList);
        sortedMode.bind(sortedComboBox.valueProperty());

        filterComboBox.setPromptText("Filter by");
        filterComboBox.setItems(filterPropertyList);
        filterComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Filter by category (only " + item + ")");
                }
            }
        });
        filterComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Filter by category (only " + item + ")");
                }
            }
        });
        filterMode.bind(filterComboBox.valueProperty());

        filteredTasks.predicateProperty().bind(Bindings.createObjectBinding(
                () -> createPredicate(
                        useFilter.get(),
                        showCompleted.get(),
                        showUncompleted.get(),
                        filterMode.get()
                ), useFilter, showCompleted, showUncompleted, filterMode

        ));

        sortedFilteredTasks.comparatorProperty().bind(Bindings.createObjectBinding(
                        () -> createComparator(sortedMode.get()),
                   sortedMode
        ));

        tasks.addListener((ListChangeListener<TaskProperty>) change -> {
            Set<String> categories = tasks.stream()
                    .map(TaskProperty::getCategory)
                    .collect(Collectors.toCollection(TreeSet::new));

            filterPropertyList.setAll(categories);
        });

        useFilterCheckbox.setText("Use filter?");
        useFilter.bind(useFilterCheckbox.selectedProperty());

        showCompletedCheckbox.setText("Show completed?");
        showCompletedCheckbox.setSelected(true);
        showCompleted.bind(showCompletedCheckbox.selectedProperty());

        showUncompletedCheckbox.setText("Show uncompleted?");
        showUncompletedCheckbox.setSelected(true);
        showUncompleted.bind(showUncompletedCheckbox.selectedProperty());

        taskView.setItems(sortedFilteredTasks);
        taskView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TaskProperty taskProperty, boolean empty) {
                super.updateItem(taskProperty, empty);

                if (empty || taskProperty == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText(taskProperty.toString());

                    MenuItem removeTask = new MenuItem("Remove task");
                    removeTask.setOnAction(_ -> tasks.remove(taskProperty));

                    MenuItem setCompleted = new MenuItem(String.format("Set task %scompleted", taskProperty.completed() ? "un" : ""));
                    setCompleted.setOnAction(_ -> taskProperty.completedProperty().set(!taskProperty.completed()));

                    ContextMenu contextMenu = new ContextMenu(removeTask, setCompleted);
                    setContextMenu(contextMenu);
                }
            }
        });

        titleField.setPromptText("Input task title");
        titleField.textProperty().bindBidirectional(task.titleProperty());

        categoryField.setPromptText("Input task category");
        categoryField.textProperty().bindBidirectional(task.categoryProperty());

        completedCheckbox.setText("add task as completed?");
        completedCheckbox.selectedProperty().bindBidirectional(task.completedProperty());

        addTaskButton.setOnAction(_ -> {
            var title = task.getTitle();
            var category = task.getCategory();

            if (
                    title == null || category == null
                    || title.isBlank() || category.isBlank()
            ) {
                notifyUser("Fill all fields before adding new task");
                return;
            }

            final var newTask = new TaskProperty(task.getTitle(), task.getCategory(), task.completed());

            if (!tasks.contains(newTask)) {
                tasks.add(newTask);
            } else {
                notifyUser("This task is already exist");
            }

            task.titleProperty().set("");
            task.categoryProperty().set("");
            task.completedProperty().set(false);
        });

        searchingCheckboxes.getChildren().addAll(useFilterCheckbox, showCompletedCheckbox, showUncompletedCheckbox);
        searchingTools.getChildren().addAll(filterComboBox, sortedComboBox, searchingCheckboxes);

        leftPane.getChildren().addAll(searchingTools, taskView);

        form.getChildren().addAll(titleField, categoryField, completedCheckbox, addTaskButton);

        mainPane.setLeft(leftPane);
        mainPane.setCenter(form);

        final var scene = new Scene(mainPane, 800, 400);

        stage.setScene(scene);
        stage.setTitle("Task Manager");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}