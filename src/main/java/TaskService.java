import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class TaskService {
    private static final Path PATH = Paths.get("tasks/tasks.list");

    public void saveAllTasks(List<Task> tasks) throws IOException {
        List<String> lines = tasks.stream()
                .map(task -> String.format(
                        "%s;%s;%b",
                        task.getTitle(),
                        task.getCategory(),
                        task.isCompleted()
                ))
                .toList();

        Files.write(PATH, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public List<Task> loadAllTasks() throws IOException {
        if (Files.exists(PATH)) {
            return Files.readAllLines(PATH).stream()
                    .map(s -> s.split(";"))
                    .map(s -> new Task(s[0], s[1], Boolean.parseBoolean(s[2])))
                    .toList();
        }

        return List.of();
    }
}
