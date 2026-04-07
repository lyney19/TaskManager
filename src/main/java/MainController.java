import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainController {

    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private CheckBox useFilterCheckBox;
    @FXML
    private ComboBox<SortedMode> sortedComboBox;
    @FXML
    private CheckBox showCompletedCheckBox;
    @FXML
    private CheckBox showUncompletedCheckBox;
    @FXML
    private ListView<TaskProperty> taskView;
    @FXML
    private TextField titleField;
    @FXML
    private TextField categoryField;
    @FXML
    private CheckBox completedCheckBox;

    private final TaskViewModel taskViewModel;

    public MainController(TaskViewModel taskViewModel) {
        this.taskViewModel = taskViewModel;
    }

    @FXML
    public void initialize() {
        taskViewModel.errorProperty().addListener((_, _, message) -> {
            if (!message.isBlank()) {
                notifyUser(message);
            }
        });

        sortedComboBox.setValue(SortedMode.getDefault());
        sortedComboBox.setItems(taskViewModel.getSortedPropertyList());
        taskViewModel.sortedProperty().bindBidirectional(sortedComboBox.valueProperty());

        filterComboBox.setItems(taskViewModel.getFilterPropertyList());
        filterComboBox.setCellFactory(_ -> new ListCell<>() {
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
        taskViewModel.filterProperty().bindBidirectional(filterComboBox.valueProperty());

        taskViewModel.useFilterProperty().bindBidirectional(useFilterCheckBox.selectedProperty());
        taskViewModel.showCompletedProperty().bindBidirectional(showCompletedCheckBox.selectedProperty());
        taskViewModel.showUncompletedProperty().bindBidirectional(showUncompletedCheckBox.selectedProperty());

        taskView.setItems(taskViewModel.getTasks());
        taskView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(TaskProperty taskProperty, boolean empty) {
                super.updateItem(taskProperty, empty);

                if (empty || taskProperty == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText(taskProperty.toString());

                    MenuItem removeTask = new MenuItem("Remove task");
                    removeTask.setOnAction(_ -> taskViewModel.removeTask(taskProperty));

                    MenuItem setCompleted = new MenuItem(String.format("Set task %scompleted", taskProperty.completed() ? "un" : ""));
                    setCompleted.setOnAction(_ -> taskViewModel.onChangeCompletionStatus(taskProperty));

                    ContextMenu contextMenu = new ContextMenu(removeTask, setCompleted);
                    setContextMenu(contextMenu);
                }
            }
        });

        titleField.textProperty().bindBidirectional(taskViewModel.getTaskProperty().titleProperty());
        categoryField.textProperty().bindBidirectional(taskViewModel.getTaskProperty().categoryProperty());
        completedCheckBox.selectedProperty().bindBidirectional(taskViewModel.getTaskProperty().completedProperty());
    }

    private void notifyUser(String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operation failed...");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onAddTask(ActionEvent actionEvent) {
        taskViewModel.addTask();
    }

    @FXML
    private void onSaveAllTasks(ActionEvent actionEvent) {
        taskViewModel.onSaveAllTasks();
    }

    @FXML
    private void onLoadAllTasks(ActionEvent actionEvent) {
        taskViewModel.onLoadAllTasks();
    }
}
