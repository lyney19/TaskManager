import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class TaskProperty {
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty category = new SimpleStringProperty("");
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    public TaskProperty() {}

    public TaskProperty(String title, String category, boolean completed) {
        this.title.set(title);
        this.category.set(category);
        this.completed.set(completed);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public String getTitle() {
        return title.get();
    }

    public String getCategory() {
        return category.get();
    }

    public boolean completed() {
        return completed.get();
    }

    public Task toTask() {
        return new Task(title.get(), category.get(), completed.get());
    }

    public void changeCompletionStatus() {
        completed.set(!completed.get());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof TaskProperty tp) {
            return Objects.equals(getTitle(), tp.getTitle())
                    && Objects.equals(getCategory(), tp.getCategory())
                    && completed() == tp.completed();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getCategory(), completed());
    }

    @Override
    public String toString() {
        return String.format(
                "Task: %s, category: %s, status: %s",
                title.get(),
                category.get(),
                completed.get() ? "completed" : "in process"
        );
    }
}
