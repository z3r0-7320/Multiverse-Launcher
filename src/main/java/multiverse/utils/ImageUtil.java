package multiverse.utils;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.*;
import multiverse.MultiverseLauncher;

public class ImageUtil {
    public static Image loadImage(String... urls) {
        for (String url : urls) {
            if (url == null) continue;
            Image image = new Image(url, false);
            if (!image.isError()) {
                return image;
            }
        }
        return new Image(MultiverseLauncher.class.getResourceAsStream("img.png"));
    }

    public static void loadImage(ImageView imageView, int index, String... urls) {
        if (index < 0 || index >= urls.length)
            imageView.setImage(new Image(MultiverseLauncher.class.getResourceAsStream("img.png")));
        Image image = new Image(urls[index], true);
        image.errorProperty().addListener(change -> {
            if (image.isError()) {
                loadImage(imageView, index + 1, urls);
            }
        });
    }

    public static Image scale(Image source, int targetSize) {
        double scaleFactor = Math.max(targetSize / source.getWidth(), targetSize / source.getHeight());

        int targetWidth = (int) (source.getWidth() * scaleFactor);
        int targetHeight = (int) (source.getHeight() * scaleFactor);

        PixelReader reader = source.getPixelReader();
        if (reader == null) {
            return source;
        }
        WritableImage output = new WritableImage(targetWidth, targetHeight);
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int srcX = (int) (x / (double) targetWidth * source.getWidth());
                int srcY = (int) (y / (double) targetHeight * source.getHeight());
                writer.setColor(x, y, reader.getColor(srcX, srcY));
            }
        }

        return output;
    }

    public static void cropToAspectRatio(ImageView imageView, Image image, double aspectWidth, double aspectHeight) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        double targetWidth;
        double targetHeight;
        double targetX;
        double targetY;

        if (aspectWidth / aspectHeight * imageHeight <= imageWidth) {
            targetHeight = imageHeight;
            targetWidth = aspectWidth / aspectHeight * imageHeight;
            targetX = (imageWidth - targetWidth) / 2;
            targetY = 0;
        } else {
            targetWidth = imageWidth;
            targetHeight = aspectHeight / aspectWidth * imageWidth;
            targetX = 0;
            targetY = (imageHeight - targetHeight) / 2;
        }

        imageView.setImage(image);
        imageView.setViewport(new Rectangle2D(targetX, targetY, targetWidth, targetHeight));
    }

}
