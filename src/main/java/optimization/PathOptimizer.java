package optimization;

import field.Vector2D;
import java.util.ArrayList;
import java.util.List;

public final class PathOptimizer {
  private static final float MIN_CORNER_ANGLE = (float) Math.toRadians(45.0);

  private PathOptimizer() {}

  public static List<Vector2D> simplify(List<Vector2D> points, float epsilon) {
    List<Vector2D> copy = copyPoints(points);
    if (copy.size() < 3 || epsilon <= 0.0f) {
      return copy;
    }

    if (copy.get(0).distance(copy.get(copy.size() - 1)) <= 0.001f) {
      return simplifyClosedPath(copy, epsilon);
    }

    boolean[] keep = new boolean[copy.size()];
    keep[0] = true;
    keep[keep.length - 1] = true;
    preserveCorners(copy, keep);
    simplifyAnchoredRanges(copy, epsilon, keep);
    preserveMaximumSegmentLength(copy, epsilon * 4.0f, keep);
    return selectPoints(copy, keep);
  }

  public static List<List<Vector2D>> simplifyPaths(List<List<Vector2D>> paths, float epsilon) {
    List<List<Vector2D>> simplified = new ArrayList<>();
    for (List<Vector2D> path : paths) {
      simplified.add(simplify(path, epsilon));
    }
    return simplified;
  }

  public static List<List<Vector2D>> orderNearestNeighbor(List<List<Vector2D>> paths) {
    List<List<Vector2D>> remaining = new ArrayList<>();
    for (List<Vector2D> path : paths) {
      if (!path.isEmpty()) {
        remaining.add(copyPoints(path));
      }
    }
    if (remaining.size() < 2) {
      return remaining;
    }

    List<List<Vector2D>> ordered = new ArrayList<>();
    List<Vector2D> currentPath = remaining.remove(0);
    ordered.add(currentPath);
    Vector2D currentPosition = currentPath.get(currentPath.size() - 1);

    while (!remaining.isEmpty()) {
      int nearestIndex = 0;
      boolean reverseNearest = false;
      float nearestDistance = Float.MAX_VALUE;
      for (int index = 0; index < remaining.size(); index++) {
        List<Vector2D> candidate = remaining.get(index);
        float startDistance = currentPosition.distance(candidate.get(0));
        float endDistance = currentPosition.distance(candidate.get(candidate.size() - 1));
        float candidateDistance = Math.min(startDistance, endDistance);
        if (candidateDistance < nearestDistance) {
          nearestDistance = candidateDistance;
          nearestIndex = index;
          reverseNearest = endDistance < startDistance;
        }
      }

      List<Vector2D> next = remaining.remove(nearestIndex);
      if (reverseNearest) {
        java.util.Collections.reverse(next);
      }
      ordered.add(next);
      currentPosition = next.get(next.size() - 1);
    }
    return ordered;
  }

  private static List<Vector2D> simplifyClosedPath(List<Vector2D> points, float epsilon) {
    int splitIndex = 1;
    float farthestDistance = 0.0f;
    Vector2D start = points.get(0);
    for (int index = 1; index < points.size() - 1; index++) {
      float distance = start.distance(points.get(index));
      if (distance > farthestDistance) {
        farthestDistance = distance;
        splitIndex = index;
      }
    }

    List<Vector2D> firstArc = new ArrayList<>(points.subList(0, splitIndex + 1));
    List<Vector2D> secondArc = new ArrayList<>(points.subList(splitIndex, points.size()));
    List<Vector2D> firstSimplified = simplifyOpenPath(firstArc, epsilon);
    List<Vector2D> secondSimplified = simplifyOpenPath(secondArc, epsilon);
    List<Vector2D> result = new ArrayList<>(firstSimplified);
    for (int index = 1; index < secondSimplified.size(); index++) {
      result.add(secondSimplified.get(index));
    }
    return result.size() >= 3 ? result : copyPoints(points);
  }

  private static List<Vector2D> simplifyOpenPath(List<Vector2D> points, float epsilon) {
    if (points.size() < 3) {
      return copyPoints(points);
    }
    boolean[] keep = new boolean[points.size()];
    keep[0] = true;
    keep[keep.length - 1] = true;
    preserveCorners(points, keep);
    simplifyAnchoredRanges(points, epsilon, keep);
    preserveMaximumSegmentLength(points, epsilon * 4.0f, keep);
    return selectPoints(points, keep);
  }

  private static void preserveCorners(List<Vector2D> points, boolean[] keep) {
    for (int index = 1; index < points.size() - 1; index++) {
      Vector2D previous = points.get(index - 1);
      Vector2D current = points.get(index);
      Vector2D next = points.get(index + 1);
      float firstX = previous.vx - current.vx;
      float firstY = previous.vy - current.vy;
      float secondX = next.vx - current.vx;
      float secondY = next.vy - current.vy;
      float firstLength = (float) Math.sqrt(firstX * firstX + firstY * firstY);
      float secondLength = (float) Math.sqrt(secondX * secondX + secondY * secondY);
      if (firstLength == 0.0f || secondLength == 0.0f) {
        continue;
      }
      float cosine = (firstX * secondX + firstY * secondY) / (firstLength * secondLength);
      float interiorAngle = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, cosine)));
      if ((float) Math.PI - interiorAngle >= MIN_CORNER_ANGLE) {
        keep[index] = true;
      }
    }
  }

  private static void simplifyAnchoredRanges(List<Vector2D> points, float epsilon, boolean[] keep) {
    int anchor = 0;
    for (int index = 1; index < keep.length; index++) {
      if (keep[index]) {
        simplifyRange(points, anchor, index, epsilon, keep);
        anchor = index;
      }
    }
  }

  private static void preserveMaximumSegmentLength(List<Vector2D> points,
                                                   float maximumLength, boolean[] keep) {
    int lastKept = 0;
    for (int index = 1; index < keep.length; index++) {
      if (keep[index] || points.get(lastKept).distance(points.get(index)) >= maximumLength) {
        keep[index] = true;
        lastKept = index;
      }
    }
  }

  private static void simplifyRange(List<Vector2D> points, int first, int last,
                                    float epsilon, boolean[] keep) {
    float maximumDistance = epsilon;
    int farthestIndex = -1;
    Vector2D start = points.get(first);
    Vector2D end = points.get(last);
    for (int index = first + 1; index < last; index++) {
      float distance = distanceToSegment(points.get(index), start, end);
      if (distance > maximumDistance) {
        maximumDistance = distance;
        farthestIndex = index;
      }
    }
    if (farthestIndex >= 0) {
      keep[farthestIndex] = true;
      simplifyRange(points, first, farthestIndex, epsilon, keep);
      simplifyRange(points, farthestIndex, last, epsilon, keep);
    }
  }

  private static float distanceToSegment(Vector2D point, Vector2D start, Vector2D end) {
    float segmentX = end.vx - start.vx;
    float segmentY = end.vy - start.vy;
    float segmentLengthSquared = segmentX * segmentX + segmentY * segmentY;
    if (segmentLengthSquared == 0.0f) {
      return point.distance(start);
    }
    float projection = ((point.vx - start.vx) * segmentX + (point.vy - start.vy) * segmentY)
      / segmentLengthSquared;
    projection = Math.max(0.0f, Math.min(1.0f, projection));
    float closestX = start.vx + projection * segmentX;
    float closestY = start.vy + projection * segmentY;
    float dx = point.vx - closestX;
    float dy = point.vy - closestY;
    return (float) Math.sqrt(dx * dx + dy * dy);
  }

  private static List<Vector2D> selectPoints(List<Vector2D> points, boolean[] keep) {
    List<Vector2D> result = new ArrayList<>();
    for (int index = 0; index < points.size(); index++) {
      if (keep[index]) {
        result.add(points.get(index).copy());
      }
    }
    return result;
  }

  private static List<Vector2D> copyPoints(List<Vector2D> points) {
    List<Vector2D> copy = new ArrayList<>(points.size());
    for (Vector2D point : points) {
      copy.add(point.copy());
    }
    return copy;
  }
}