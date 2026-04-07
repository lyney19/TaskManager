import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskViewModel {

    private final TaskProperty taskProperty = new TaskProperty();
    private final TaskService taskService;
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final ObservableList<TaskProperty> tasks = FXCollections.observableArrayList(
            task -> new Observable[] { task.titleProperty(), task.categoryProperty(), task.completedProperty() }
    );
    private final FilteredList<TaskProperty> filteredTasks = new FilteredList<>(tasks);
    private final SortedList<TaskProperty> sortedFilteredTasks = new SortedList<>(filteredTasks);

    private final StringProperty filterMode = new SimpleStringProperty("");
    private final ObjectProperty<SortedMode> sortedMode = new SimpleObjectProperty<>(SortedMode.getDefault());
    private final BooleanProperty useFilter = new SimpleBooleanProperty(false);
    private final BooleanProperty showCompleted = new SimpleBooleanProperty(false);
    private final BooleanProperty showUncompleted = new SimpleBooleanProperty(false);
    private final ObservableList<String> filterPropertyList = FXCollections.observableArrayList();
    private final ObservableList<SortedMode> sortedPropertyList = FXCollections.observableArrayList(
            SortedMode.values()
    );

    public TaskViewModel(TaskService taskService) {
        this.taskService = taskService;

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

        onLoadAllTasks();
    }

    public TaskProperty getTaskProperty() {
        return taskProperty;
    }

    public ObservableList<TaskProperty> getTasks() {
        return sortedFilteredTasks;
    }

    public ObjectProperty<SortedMode> sortedProperty() {
        return sortedMode;
    }

    public StringProperty filterProperty() {
        return filterMode;
    }

    public StringProperty errorProperty() {
        return errorMessage;
    }

    public BooleanProperty useFilterProperty() {
        return useFilter;
    }

    public BooleanProperty showCompletedProperty() {
        return showCompleted;
    }

    public BooleanProperty showUncompletedProperty() {
        return showUncompleted;
    }

    public ObservableList<String> getFilterPropertyList() {
        return filterPropertyList;
    }

    public ObservableList<SortedMode> getSortedPropertyList() {
        return sortedPropertyList;
    }

    private Comparator<TaskProperty> createComparator(SortedMode mode) {
        return switch (mode) {
            case SortedMode.NAME_DEC -> Comparator.comparing(TaskProperty::getTitle).reversed()
                    .thenComparing(TaskProperty::getCategory)
                    .thenComparing(TaskProperty::completed);

            case SortedMode.COMPLETED_ASC -> Comparator.comparing(TaskProperty::completed)
                    .thenComparing(TaskProperty::getTitle)
                    .thenComparing(TaskProperty::getCategory);

            case SortedMode.COMPLETED_DEC -> Comparator.comparing(TaskProperty::completed).reversed()
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

    public void onSaveAllTasks() {
        try {
            taskService.saveAllTasks(
                    tasks.stream()
                    .map(TaskProperty::toTask)
                    .toList()
            );
        } catch (IOException e) {
            errorMessage.set("Failed to save tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onLoadAllTasks() {
        try {
            tasks.setAll(
                    taskService.loadAllTasks().stream()
                            .map(task -> new TaskProperty(task.title(), task.category(), task.completed()))
                            .toList()
            );
        } catch (IOException e) {
            errorMessage.set("Failed to load tasks: " + e.getMessage());
        }
    }

    public void addTask() {
        var title = taskProperty.getTitle();
        var category = taskProperty.getCategory();
        if (title == null || category == null || title.isBlank() || category.isBlank()) {
            errorMessage.set("Fill all fields before adding new task");
            return;
        }

        final var newTask = new TaskProperty(taskProperty.getTitle(), taskProperty.getCategory(), taskProperty.completed());

        if (!tasks.contains(newTask)) {
            tasks.add(newTask);
        } else {
            errorMessage.set("This task is already exist");
        }

        taskProperty.titleProperty().set("");
        taskProperty.categoryProperty().set("");
        taskProperty.completedProperty().set(false);
    }

    public void removeTask(TaskProperty task) {
        tasks.remove(task);
    }

    public void onChangeCompletionStatus(TaskProperty task) {
        task.changeCompletionStatus();
    }
}
