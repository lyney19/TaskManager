import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    private ComboBox<String> filterComboBox;
    private final StringProperty filterMode = new SimpleStringProperty();
    private final ObservableList<String> filterPropertyList = FXCollections.observableArrayList();

    @FXML
    private CheckBox useFilterCheckBox;
    private final BooleanProperty useFilter = new SimpleBooleanProperty();

    @FXML
    private ComboBox<String> sortedComboBox;
    private final StringProperty sortedMode = new SimpleStringProperty();
    private final ObservableList<String> sortedPropertyList = FXCollections.observableArrayList(
            "Sort by name (A-Z)",
            "Sort by name (Z-A)",
            "Sort by status (uncompleted first)",
            "Sort by status (completed first)"
    );

    @FXML
    private CheckBox showCompletedCheckBox;
    private final BooleanProperty showCompleted = new SimpleBooleanProperty();

    @FXML
    private CheckBox showUncompletedCheckBox;
    private final BooleanProperty showUncompleted = new SimpleBooleanProperty();

    @FXML private ListView<TaskProperty> taskView;
    @FXML private TextField titleField;
    @FXML private TextField categoryField;
    @FXML private CheckBox completedCheckBox;

    private final TaskProperty task = new TaskProperty();

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

    @FXML
    private void addTask(ActionEvent actionEvent) {
        var title = task.getTitle();
        var category = task.getCategory();
        if (title == null || category == null || title.isBlank() || category.isBlank()) {
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
    }

    @FXML
    public void initialize() {

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

        useFilter.bind(useFilterCheckBox.selectedProperty());
        showCompleted.bind(showCompletedCheckBox.selectedProperty());
        showUncompleted.bind(showUncompletedCheckBox.selectedProperty());

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

        titleField.textProperty().bindBidirectional(task.titleProperty());
        categoryField.textProperty().bindBidirectional(task.categoryProperty());
        completedCheckBox.selectedProperty().bindBidirectional(task.completedProperty());
    }
}
