package export;

import field.Vector2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

public final class DrawingExporter {
  private DrawingExporter() {}

  public static void writeSvg(Path target, List<List<Vector2D>> paths,
                              int width, int height) throws IOException {
    StringBuilder svg = new StringBuilder();
    svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
      .append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
      .append(width).append("\" height=\"").append(height)
      .append("\" viewBox=\"0 0 ").append(width).append(' ').append(height).append("\">\n")
      .append("<g fill=\"none\" stroke=\"#111111\" stroke-width=\"1\" stroke-linecap=\"round\" stroke-linejoin=\"round\">\n");
    for (List<Vector2D> path : paths) {
      if (path.size() < 2) {
        continue;
      }
      svg.append("<polyline points=\"");
      for (int index = 0; index < path.size(); index++) {
        if (index > 0) {
          svg.append(' ');
        }
        Vector2D point = path.get(index);
        svg.append(String.format(Locale.ROOT, "%.3f,%.3f", point.vx, point.vy));
      }
      svg.append("\"/>\n");
    }
    svg.append("</g>\n</svg>\n");
    Files.write(target, svg.toString().getBytes(StandardCharsets.UTF_8));
  }

  public static void writeDxf(Path target, List<List<Vector2D>> paths) throws IOException {
    StringBuilder dxf = new StringBuilder("0\nSECTION\n2\nENTITIES\n");
    for (List<Vector2D> path : paths) {
      for (int index = 0; index + 1 < path.size(); index++) {
        Vector2D start = path.get(index);
        Vector2D end = path.get(index + 1);
        dxf.append("0\nLINE\n8\n0\n10\n")
          .append(number(start.vx)).append("\n20\n").append(number(start.vy))
          .append("\n30\n0\n11\n").append(number(end.vx))
          .append("\n21\n").append(number(end.vy)).append("\n31\n0\n");
      }
    }
    dxf.append("0\nENDSEC\n0\nEOF\n");
    Files.write(target, dxf.toString().getBytes(StandardCharsets.US_ASCII));
  }

  public static void writeRaster(Path target, BufferedImage image, String format) throws IOException {
    String normalizedFormat = format.toLowerCase(Locale.ROOT);
    if (!"png".equals(normalizedFormat) && !"jpg".equals(normalizedFormat)
        && !"jpeg".equals(normalizedFormat)) {
      throw new IllegalArgumentException("Unsupported raster format: " + format);
    }
    if (!ImageIO.write(image, normalizedFormat, target.toFile())) {
      throw new IOException("No image writer available for " + normalizedFormat);
    }
  }

  private static String number(float value) {
    return String.format(Locale.ROOT, "%.3f", value);
  }
}