public enum SortedMode {
    NAME_ASC("Sort by name (A-Z)"),
    NAME_DEC("Sort by name (Z-A)"),
    COMPLETED_ASC("Sort by status (uncompleted first)"),
    COMPLETED_DEC("Sort by status (completed first)");

    private final String text;

    SortedMode(String text) {
        this.text = text;
    }

    public static SortedMode getDefault() {
        return NAME_ASC;
    }

    @Override
    public String toString() {
        return text;
    }
}
