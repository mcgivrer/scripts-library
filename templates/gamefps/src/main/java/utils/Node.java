package{{BASE_PACKAGE}}.utils;

import{{BASE_PACKAGE}}.{{MAIN_CLASS_NAME}};

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Node<T> {
    private static long index = 0;
    protected long id = index++;
    protected String name = "node_%d".formatted(id);

    private static Node<?> root;
    protected Node<?> parent;
    protected List<Node<?>> children = new CopyOnWriteArrayList<>();

    public Node(String name) {
        this.name = name;
        if (root == null) {
            root = this;
            parent = null;
        }
    }

    public void add(Node<?> child) {
        child.parent = this;
        this.children.add(child);
    }

    public static Node<?> find(String name) {
        if (root == null)
            return null;
        if (root.name.equals(name))
            return root;

        // Recherche parallèle dans tous les enfants de la racine et leurs descendants
        return root.children.parallelStream()
                .map(child -> child.findFromHere(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Recherche récursive à partir de ce nœud (utilisée par parallelStream)
     */
    public Node findFromHere(String name) {
        if (this.name.equals(name)) {
            return this;
        }

        // Si ce nœud a des enfants, recherche parallèle dans les enfants
        if (!children.isEmpty()) {
            return children.parallelStream()
                    .map(child -> child.findFromHere(name))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    /**
     * Log l'intégralité de l'arbre à partir de ce nœud.
     */
    public void logTree() {
        logTree(0);
    }

    /**
     * Log l'arbre avec indentation.
     *
     * @param indent niveau d'indentation
     */
    private void logTree(int indent) {
        String indentation = "   ".repeat(indent);
        {{MAIN_CLASS_NAME}}.debug(Node.class, 0, indentation + "|_ %s (id:%d)[%s]", name, id, getClass().getCanonicalName());

        for (Node<?> child : children) {
            child.logTree(indent + 1);
        }
    }

}
