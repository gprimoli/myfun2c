package it.util.tree;

import hu.webarticum.treeprinter.TreeNode;
import hu.webarticum.treeprinter.printer.traditional.TraditionalTreePrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public abstract class Node<T> implements TreeNode {
    private Node<T> father = null;
    private final T info;
    private final List<Node<T>> child;


    //  Constructors
    private void createChilds(Object... nodes) {
        for (Object n : nodes)
            if (n != null)
                if (n.getClass().isArray())
                    for (Object o : (Object[]) n) createChilds(o);
                else {
                    Node<T> x = (Node<T>) n;
                    x.father = this;
                    this.child.add(x);
                }
    }

    public Node(T info, Object... nodes) {
        this.child = new LinkedList<>();
        this.info = info;
        createChilds(nodes);
    }


    //  Getter & Setter
    public T getInfo() {
        return info;
    }

    public Node<T> getFather() {
        return father;
    }

    public List<Node<T>> getChild() {
        return child;
    }

    public void addChild(Object... nodes) {
        createChilds(nodes);
    }

    public boolean isRoot() {
        return father == null;
    }


    //  TreeNode Method (Print)
    @Override
    public String content() {
        return info.toString();
    }

    @Override
    public List<TreeNode> children() {
        return new LinkedList<>(child);
    }

    public static void debug(Node root, String dir, String nameFile) {
        try {
            String res = new TraditionalTreePrinter().stringify(root);
            PrintWriter printWriter = new PrintWriter(new FileWriter(dir + File.separator + nameFile + ".txt"));
            printWriter.print(res);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
