# Java FX Task Manager

## 📌 Description
Simple Task manager with a graphical interface written in Java using JavaFX. This project was created to study bindings.

## 🚀 Features
- Add task to general task list with form
- Sort task list
- Filter task list by categories and completion status
- Remove task or change completion status by clicking RMB

## ⚙️ How It Works

The application uses JavaFX bindings and observable collections:

- `ObservableList` stores tasks
- `FilteredList` applies filtering
- `SortedList` applies sorting
- UI updates automatically via bindings

## 📸 Screenshots

| Main UI                                       | Filtering                                       | Sorting                                       |
|-----------------------------------------------|-------------------------------------------------|-----------------------------------------------|
| <img src="screenshots/main.png" width="250"/> | <img src="screenshots/filter.png" width="250"/> | <img src="screenshots/sort.png" width="250"/> |

## 🛠 Tech Stack
- Java
- JavaFX

## ⚙️ Installation & Run
1. Clone repository:
```bash
git clone https://github.com/lyney19/TaskManager.git
```
2. Open in IDE (IntelliJ IDEA, Eclipse)
3. Run `Main.java`

## 📁 Project Structure
```
TaskManager/
├─ Main.java            # UI & logic
└─ Operation.java       # ViewModel & Model
```

## 🧠 What I Learned
- JavaFX bindings
- JavaFX collections
- Basics of the declarative approach to programming

## 🔮 Future Improvements
### Core
- [ ] Save/load tasks (JSON or database)
- [ ] Search bar

### UI/UX
- [ ] Better application design

### Architecture
- [ ] Refactor to MVVM
- [ ] Introduce FXML