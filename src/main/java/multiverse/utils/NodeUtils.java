package multiverse.utils;

import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

public class NodeUtils {

    public static Node roundedNode(Node inputNode) {
        return roundedNode(inputNode, inputNode.getLayoutBounds().getWidth(), inputNode.getLayoutBounds().getHeight());
    }

    public static Node roundedNode(Node inputNode, double width, double height) {
        final Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.setWidth(width);
        clip.setHeight(height);
        inputNode.setClip(clip);

        return inputNode;
    }
}
