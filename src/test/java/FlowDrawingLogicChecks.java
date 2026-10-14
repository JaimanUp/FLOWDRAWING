import brush.Brush;
import brush.BrushEngine;
import bot.BotEngine;
import bot.Bot;
import field.Vector2D;
import field.VectorField;
import export.DrawingExporter;
import optimization.PathOptimizer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowDrawingLogicChecks {
  public static void main(String[] args) throws Exception {
    checkCanvasDefaults();
    checkCameraCoordinateMapping();
    checkCameraViewportFit();
    checkVectorToolActions();
    checkBilinearSampling();
    checkFieldMagnitudeScale();
    checkFieldRandomizationAndReset();
    checkFieldNormalization();
    checkCleanNormalization();
    checkAdaptiveBotSmoothing();
    checkBotStabilityModulation();
    checkBotRepulsionUsesOldTraces();
    checkDeadBotTraceRepulsionRetention();
    checkKillBotsKeepsTraces();
      checkBotDriftModes();
    checkLiveBotParameterUpdates();
    checkRadarSampleBounds();
    checkLifeExpiryRespawnsWithoutAutoSpawn();
    checkRateLimitedAutoSpawn();
    checkEraserStrength();
    checkFalloffAndHardness();
    checkBrushSpacing();
    checkLuminanceLifeDecay();
    checkSimulationBackgroundSources();
    checkDrawingExport();
    checkPathSimplification();
    checkNearestNeighborOrdering();
    System.out.println("All FlowDrawing logic checks passed.");
  }

  private static void checkCanvasDefaults() {
    if (config.Config.CANVAS_WIDTH != 2000 || config.Config.CANVAS_HEIGHT != 2000) {
      throw new AssertionError("Canvas defaults must remain 2000 x 2000 in Config");
    }

    java.awt.Dimension landscape = config.Config.canvasSizeForViewport(1600, 900);
    checkCondition(landscape.width > 2000 && landscape.height == 2000,
      "Landscape canvas should keep 2000 px height and expand its width");
    checkClose(landscape.width / (float) landscape.height, 1600.0f / 900.0f,
      "landscape canvas aspect ratio");

    java.awt.Dimension portrait = config.Config.canvasSizeForViewport(900, 1600);
    checkCondition(portrait.width == 2000 && portrait.height > 2000,
      "Portrait canvas should keep 2000 px width and expand its height");
    checkClose(portrait.width / (float) portrait.height, 900.0f / 1600.0f,
      "portrait canvas aspect ratio");

    java.awt.Dimension invalidViewport = config.Config.canvasSizeForViewport(0, 0);
    checkCondition(invalidViewport.width == 2000 && invalidViewport.height == 2000,
      "Invalid viewport should fall back to default canvas dimensions");
  }

  private static void checkCameraCoordinateMapping() {
    render.CameraController camera = new render.CameraController(1000, 800);
    camera.pan(-30.0f, 20.0f);
    camera.setZoom(2.0f);
    java.awt.geom.Point2D.Float worldPoint = camera.screenToWorld(30.0f, -70.0f, 1000, 800);
    checkClose(worldPoint.x, 250.0f, "pan/zoom world x mapping");
    checkClose(worldPoint.y, 175.0f, "pan/zoom world y mapping");

    java.awt.geom.Point2D.Float cursorWorldBefore = camera.screenToWorld(350.0f, 200.0f, 1000, 800);
    camera.zoomAtMouse(1.5f, 350, 200, 1000, 800);
    java.awt.geom.Point2D.Float cursorWorldAfter = camera.screenToWorld(350.0f, 200.0f, 1000, 800);
    checkClose(cursorWorldAfter.x, cursorWorldBefore.x, "cursor-anchored zoom x");
    checkClose(cursorWorldAfter.y, cursorWorldBefore.y, "cursor-anchored zoom y");
  }

  private static void checkCameraViewportFit() {
    render.CameraController camera = new render.CameraController(2000, 1000);
    checkCondition(camera.fitToViewport(1200, 800, 0.95f),
      "Camera should fit a valid landscape viewport");
    checkClose(camera.getZoom(), 0.57f, "landscape viewport fit zoom");
    checkClose(camera.getPanX(), -228.0f, "landscape viewport horizontal centering");
    checkClose(camera.getPanY(), -57.0f, "landscape viewport vertical centering");

    checkCondition(camera.fitToViewport(800, 1200, 0.95f),
      "Camera should fit a valid portrait viewport");
    checkClose(camera.getZoom(), 0.38f, "portrait viewport fit zoom");
    checkClose(camera.getPanX(), -228.0f, "portrait viewport horizontal centering");
    checkClose(camera.getPanY(), 38.0f, "portrait viewport vertical centering");

    float previousZoom = camera.getZoom();
    checkCondition(!camera.fitToViewport(0, 0, 0.95f),
      "Camera fit must wait for non-zero viewport dimensions");
    checkClose(camera.getZoom(), previousZoom, "zero-size viewport preserves camera state");
  }

  private static void checkBotRepulsionUsesOldTraces() {
    Bot follower = new Bot(100.0f, 100.0f, 100.0f, 20.0f, 2.0f);
    follower.setFieldInfluence(0.0f);
    follower.setDriftInfluence(0.0f);
    follower.setRepulsionInfluence(1.0f);
    follower.setRepulsionRadius(25.0f);
    follower.getVelocity().vx = -1.0f;
    follower.getVelocity().vy = 0.0f;

    Bot trailOwner = new Bot(500.0f, 500.0f, 100.0f, 20.0f, 2.0f);
    trailOwner.getPath().add(new Bot.TracePoint(110.0f, 100.0f, 1.0f, Color.BLACK));
    trailOwner.getPosition().vx = 900.0f;
    trailOwner.getPosition().vy = 900.0f;

    ArrayList<Bot> bots = new ArrayList<>(Arrays.asList(follower, trailOwner));
    follower.update(new Vector2D(0.0f, 0.0f), bots, 0.0f);
    checkCondition(follower.getPosition().vx < 100.0f,
      "Bot should repel from an old nearby trace even when its owner has moved away");
  }

  private static void checkKillBotsKeepsTraces() {
    VectorField field = new VectorField(1000, 1000, 10.0f);
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) vector.vx = 1.0f;
    }

    BotEngine engine = new BotEngine(field);
    engine.spawnBotAt(500.0f, 500.0f);
    for (int frame = 0; frame < 20; frame++) engine.update();
    Bot bot = engine.getBots().get(0);
    int traceLength = bot.getPath().size();

    engine.killBots();
    checkCondition(engine.getBots().isEmpty(), "Killing bots should remove all live bots");
    checkCondition(engine.getDeadBots().contains(bot) && !bot.isAlive()
      && bot.getPath().size() == traceLength,
      "Killed bots should keep their trace as dead bots");
  }

  private static void checkDeadBotTraceRepulsionRetention() {
    VectorField field = new VectorField(1000, 1000, 10.0f);
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) vector.vx = 1.0f;
    }

    BotEngine engine = new BotEngine(field);
    engine.setBotMaxLife(1.0f);
    engine.spawnBotAt(500.0f, 500.0f);
    engine.update();
    engine.update();
    checkCondition(engine.getDeadBots().size() == 1,
      "Expired bot should remain stored with its trace");

    Bot trailOwner = engine.getDeadBots().get(0);
    trailOwner.getPath().add(new Bot.TracePoint(110.0f, 100.0f, 1.0f, Color.BLACK));
    trailOwner.getPosition().vx = 900.0f;
    trailOwner.getPosition().vy = 900.0f;

    engine.getBots().clear();
    engine.setBotMaxLife(100.0f);
    engine.setFieldInfluence(0.0f);
    engine.setDriftInfluence(0.0f);
    engine.setRepulsionInfluence(1.0f);
    engine.setRepulsionRadius(25.0f);
    engine.spawnBotAt(100.0f, 100.0f);
    Bot follower = engine.getBots().get(0);
    follower.getVelocity().vx = -1.0f;
    follower.getVelocity().vy = 0.0f;

    engine.update();
    checkCondition(follower.getPosition().vx < 100.0f,
      "Living bot should repel from a nearby trace owned by a dead bot");

    engine.clearTraces();
    checkCondition(engine.getDeadBots().isEmpty(),
      "Explicit trace clearing should release dead bot trace memory");
  }

  private static void checkVectorToolActions() throws Exception {
    final boolean[] drawingEnabled = {false};
    final boolean[] brushMode = {false};
    final String[] selectedDriftMode = {null};
    final int[] colorPickerCalls = {0};
    final int[] actionCounts = new int[3];
    final int[] layerClearCounts = new int[3];
    final boolean[] botLayerVisible = {true};
    final float[] layerTransparency = {-1.0f, -1.0f, -1.0f};
    javax.swing.SwingUtilities.invokeAndWait(() -> {
      ui.ToolPanel panel = new ui.ToolPanel(new ui.ToolPanel.ToolListener() {
        @Override public void onFieldDrawingEnabled(boolean enabled) {
          drawingEnabled[0] = enabled;
        }
        @Override public void onBrushModeChanged(boolean isBrush) { brushMode[0] = isBrush; }
        @Override public void onRandomVectorField() { actionCounts[0]++; }
        @Override public void onCleanVectorField() { actionCounts[1]++; }
        @Override public void onResetVectorField() { actionCounts[2]++; }
        @Override public void onBotDriftModeChanged(String mode) { selectedDriftMode[0] = mode; }
        @Override public void onBotColorPicker() { colorPickerCalls[0]++; }
        @Override public void onBotfieldToggle(boolean visible) { botLayerVisible[0] = visible; }
        @Override public void onClearBackgroundImage() { layerClearCounts[0]++; }
        @Override public void onClearBots() { layerClearCounts[1]++; }
        @Override public void onClearTraces() { layerClearCounts[2]++; }
        @Override public void onBackgroundTransparencyChanged(float alpha) {
          layerTransparency[0] = alpha;
        }
        @Override public void onVectorFieldTransparencyChanged(float alpha) {
          layerTransparency[1] = alpha;
        }
        @Override public void onBotfieldTransparencyChanged(float alpha) {
          layerTransparency[2] = alpha;
        }
      });
      checkCondition(panel.getPreferredSize().width == 230, "ToolPanel preferred width must be 230 px");
      checkCondition(panel.getMinimumSize().width == 200, "ToolPanel minimum width must be 200 px");
      javax.swing.JPanel windowSurface = new javax.swing.JPanel(new java.awt.BorderLayout());
      windowSurface.add(panel, java.awt.BorderLayout.WEST);
      windowSurface.setSize(1280, 800);
      windowSurface.doLayout();
      checkCondition(panel.getHeight() == 800,
        "ToolPanel must stretch to the full available window height");
      javax.swing.AbstractButton backgroundLayer = findButton(panel, "Background layer");
      checkCondition(backgroundLayer instanceof javax.swing.JToggleButton
          && backgroundLayer.getModel().isSelected(), "Background layer must expose selected state");
      checkCondition(((javax.swing.JPanel) backgroundLayer.getParent()).getPreferredSize().height >= 52,
        "Layer tabs must provide enough height for their controls and insets");
      java.awt.GridLayout layerLayout = (java.awt.GridLayout)
        ((javax.swing.JPanel) backgroundLayer.getParent()).getLayout();
      checkCondition(layerLayout.getRows() == 1 && layerLayout.getColumns() == 3
          && layerLayout.getHgap() == 8,
        "Primary layer buttons must share one row with consistent spacing");
      int toolbarHeight = findToolbar(panel).getPreferredSize().height;
      checkCondition(toolbarHeight == 112, "Tool ribbon must use a fixed two-row height");
      checkCondition(((java.awt.GridLayout) findToolbar(panel).getLayout()).getHgap() == 8
          && ((java.awt.GridLayout) findToolbar(panel).getLayout()).getVgap() == 8,
        "Tool icons must use a consistent 8px gap");
      checkToolbarIcons(panel, new String[] {"Color", "Image"});
      javax.swing.AbstractButton backgroundVisibility = findButton(panel,
        "Background layer visibility");
      javax.swing.JSlider backgroundTransparency = findSlider(panel,
        "Background layer transparency");
      checkCondition(backgroundVisibility != null && backgroundTransparency != null
          && backgroundVisibility.getParent() == backgroundTransparency.getParent()
          && backgroundVisibility.getText().isEmpty() && backgroundVisibility.getIcon() != null,
        "Icon-only visibility and unlabeled transparency controls must share one general row");
      clickToolButton(panel, "Clear background or image", true);
      checkCondition(layerClearCounts[0] == 1, "Background clear action must be in the general row");
      backgroundTransparency.setValue(75);
      checkClose(layerTransparency[0], 0.75f, "background transparency callback");

      clickToolButton(panel, "VECFD", false);
      javax.swing.JSlider fieldTransparency = findSlider(panel, "Vector field layer transparency");
      checkCondition(fieldTransparency != null && fieldTransparency.getValue() == 100,
        "Vector-field transparency must keep an independent initial value");
      fieldTransparency.setValue(64);
      checkClose(layerTransparency[1], 0.64f, "vector-field transparency callback");
      checkCondition(findToolbar(panel).getPreferredSize().height == toolbarHeight,
        "VECFD ribbon height must match BKGD");
      checkToolbarIcons(panel, new String[] {"Paint", "Erase", "Field Actions"});
      javax.swing.AbstractButton fieldReset = findButton(panel, "Reset vector field to zero");
      checkCondition(fieldReset != null && fieldReset.getIcon() != null && fieldReset.getText().isEmpty(),
        "Vector-field reset must be an icon-only general action");
      panel.setSize(200, 320);
      layoutTree(panel);
      javax.swing.JScrollPane settingsScroll = findScrollPane(panel);
      checkCondition(settingsScroll != null, "ToolPanel must contain its settings scroll pane");
      checkCondition(settingsScroll.getVerticalScrollBarPolicy()
          == javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER,
        "Settings vertical scrollbar must be hidden");
      checkCondition(settingsScroll.isWheelScrollingEnabled(),
        "Settings must retain mouse-wheel scrolling");
      java.awt.Container generalControlsRow = fieldTransparency.getParent();
      for (java.awt.Component control : generalControlsRow.getComponents()) {
        java.awt.Rectangle bounds = control.getBounds();
        checkCondition(bounds.x >= 0 && bounds.x + bounds.width <= generalControlsRow.getWidth(),
          "General layer controls must fit in one row at the minimum panel width");
      }
      checkCondition(fieldTransparency.getWidth() >= 30,
        "Layer transparency slider must remain usable at the minimum panel width");
      javax.swing.JViewport settingsViewport = settingsScroll.getViewport();
      java.awt.Component settingsView = settingsViewport.getView();
      int viewportWidth = settingsViewport.getExtentSize().width;
      checkCondition(settingsView.getWidth() == viewportWidth,
        "VECFD settings must track the available viewport width");
      checkInteractiveControlsFit((java.awt.Container) settingsView, settingsView, viewportWidth);

      javax.swing.JSlider paintSize = findSlider(panel, "size");
      javax.swing.JSlider paintHardness = findSlider(panel, "hardness");
      javax.swing.JSlider paintStrength = findSlider(panel, "strength");
      checkCondition(paintSize != null && paintHardness != null && paintStrength != null,
        "Paint must expose keyed size, hardness, and strength controls");
      paintSize.setValue(88);
      paintHardness.setValue(73);
      paintStrength.setValue(17);
      javax.swing.AbstractButton linearFalloff = findButton(panel, "Brush falloff");
      checkCondition(linearFalloff instanceof javax.swing.JToggleButton,
        "Vector falloff choices must expose selected state");
      linearFalloff.doClick();
      clickToolButton(panel, "Erase", true);
      checkCondition(findSlider(panel, "size").getValue() == 88
          && findSlider(panel, "hardness").getValue() == 73
          && findSlider(panel, "strength").getValue() == 17
          && findButton(panel, "Brush falloff").isSelected(),
        "Vector brush values and falloff must persist when switching tools");

      clickToolButton(panel, "Field Actions", true);
      checkCondition(!drawingEnabled[0], "Field Actions must disable brush input");
      clickToolButton(panel, "Randomize Whole Field", false);
      clickToolButton(panel, "Normalize Whole Field", false);
      clickToolButton(panel, "Reset vector field to zero", true);

      clickToolButton(panel, "BOTFD", false);
      checkCondition(findToolbar(panel).getPreferredSize().height == toolbarHeight,
        "BOTFD ribbon height must match BKGD and VECFD");
      checkToolbarIcons(panel, new String[] {"Place", "Random", "Auto", "Export"});
      layoutTree(panel);
      javax.swing.AbstractButton botLayer = findButton(panel, "Bot field layer");
      checkCondition(botLayer instanceof javax.swing.JToggleButton
          && botLayer.getModel().isSelected(), "Bot field layer must expose selected state");
      javax.swing.AbstractButton visibility = findButton(panel, "Bot field layer visibility");
      javax.swing.JSlider botTransparency = findSlider(panel, "Bot field layer transparency");
      checkCondition(visibility instanceof javax.swing.JToggleButton
          && visibility.getModel().isSelected(), "Visible BOTFD state must be announced as selected");
      checkCondition(botTransparency != null && visibility.getParent() == botTransparency.getParent()
          && ((javax.swing.JPanel) visibility.getParent()).getPreferredSize().height >= 40,
        "BOTFD visibility and transparency must share the general-controls row");
        checkCondition(findButton(panel, "Kill all bots (keep traces)") != null
          && findButton(panel, "Clear all bot traces") != null,
        "BOTFD general row must expose separate bot and trace clearing actions");
        javax.swing.Icon visibleEyeIcon = visibility.getIcon();
        java.awt.Color visibleButtonColor = visibility.getBackground();
      visibility.doClick();
      checkCondition(!botLayerVisible[0] && !visibility.getModel().isSelected(),
        "BOTFD visibility toggle must retain its component and expose the hidden state");
        checkCondition(visibility.getIcon() != visibleEyeIcon
            && !visibleButtonColor.equals(visibility.getBackground()),
          "Eye icon and active color must change when layer visibility changes");
      checkCondition("Layer is hidden".equals(
          visibility.getAccessibleContext().getAccessibleDescription()),
        "BOTFD visibility description must reflect its state");
      clickToolButton(panel, "Kill all bots (keep traces)", true);
      clickToolButton(panel, "Clear all bot traces", true);
      botTransparency.setValue(42);
      checkClose(layerTransparency[2], 0.42f, "bot-field transparency callback");

      javax.swing.AbstractButton randomTool = findButton(panel, "Random");
      randomTool.doClick();
      checkCondition(findButton(panel, "Random") == randomTool
          && randomTool.getModel().isSelected(),
        "Selecting a tool must retain the focused ribbon control and expose selected state");
      clickToolButton(panel, "Place", true);
      javax.swing.JComboBox<?> driftMode = findComboBox(panel, "Drift mode");
      checkCondition(driftMode != null, "Place must expose the drift mode selector");
      checkCondition(driftMode.isEnabled() && !driftMode.isEditable()
          && driftMode.getItemCount() > 1,
        "Drift selector must remain enabled and allow changing among its options");
      javax.swing.JSlider lifeSlider = findSlider(panel, "Life");
      checkCondition(lifeSlider != null && findLabelFor(panel, lifeSlider) != null,
        "Place sliders must have accessible names and associated labels");
      checkCondition("FlowSliderUI".equals(lifeSlider.getUI().getClass().getSimpleName()),
        "Place sliders must use the FlowDrawing slider style");
      checkDropdownsHaveVisibleDefaults((java.awt.Container) settingsView);
      javax.swing.AbstractButton traceColorButton = findButton(panel, "Choose trace base color");
      checkCondition(driftMode.getX() == lifeSlider.getX()
          && driftMode.getWidth() == lifeSlider.getWidth()
          && traceColorButton.getX() == lifeSlider.getX()
          && traceColorButton.getWidth() == lifeSlider.getWidth(),
        "Place dropdowns, sliders, and action buttons must share consistent margins");
      driftMode.setSelectedItem("Canvas");
      checkCondition("Canvas".equals(driftMode.getSelectedItem()),
        "Drift dropdown must update its visible selected option");
      lifeSlider.setValue(640);
      clickToolButton(panel, "Trace base color...", false);

      clickToolButton(panel, "Random", true);
      layoutTree(panel);
      javax.swing.JSlider randomCountSlider = findSlider(panel, "Random bot count");
      javax.swing.JComboBox<?> randomDriftMode = findComboBox(panel, "Drift mode");
      javax.swing.JLabel driftModeLabel = findLabelText(panel, "Drift mode");
      javax.swing.AbstractButton spawnRandomButton = findButtonByText(panel, "Spawn Random");
      checkCondition(randomCountSlider != null && randomDriftMode != null
          && driftModeLabel != null && spawnRandomButton != null,
        "Random BOTFD controls must include count, drift mode, label, and spawn action");
      checkCondition("Canvas".equals(randomDriftMode.getSelectedItem()),
        "Drift mode must persist from Place to Random tools");
      checkAlignedBounds(settingsView, randomCountSlider, randomDriftMode,
        driftModeLabel, spawnRandomButton);
      java.awt.Rectangle spawnBounds = javax.swing.SwingUtilities.convertRectangle(
        spawnRandomButton.getParent(), spawnRandomButton.getBounds(), settingsView);
      checkCondition(spawnBounds.x == 8
          && spawnBounds.x + spawnBounds.width == settingsView.getWidth() - 8,
        "Spawn Random must use the same 8px side inset as the toolbar controls");
      randomCountSlider.setValue(77);

      clickToolButton(panel, "Auto", true);
      checkCondition(findSlider(panel, "Life").getValue() == 640
          && findSlider(panel, "Target bots").getValue() == 77
          && findSlider(panel, "Radar radius").getValue() == 50,
        "Bot Field parameters must stay synchronized between Place, Random, and Auto tools");
      javax.swing.JComboBox<?> radarFalloff = findComboBox(panel, "Radar falloff");
      checkCondition(radarFalloff != null && findLabelFor(panel, radarFalloff) != null,
        "Auto radar falloff must have an associated visible label");
      checkCondition(findButton(panel, "Life decay from background luminance") != null,
        "Auto luminance checkbox must expose a plain accessible name");

      clickToolButton(panel, "Export", true);
      layoutTree(panel);
      javax.swing.JComboBox<?> exportFormat = findComboBox(panel, "Export format");
      checkCondition(exportFormat != null && "SVG".equals(exportFormat.getSelectedItem()),
        "Export format dropdown must show its default option");
      clickToolButton(panel, "VECFD", false);
      checkCondition(findSlider(panel, "Vector field layer transparency").getValue() == 64,
        "Vector-field transparency must persist when switching groups");
      clickToolButton(panel, "BOTFD", false);
      checkCondition(findSlider(panel, "Bot field layer transparency").getValue() == 42,
        "Bot-field transparency must persist when switching groups");
    });

    if (actionCounts[0] != 1 || actionCounts[1] != 1 || actionCounts[2] != 1) {
      throw new AssertionError("Random, Clean, and Reset actions must each fire exactly once");
    }
    if (layerClearCounts[0] != 1 || layerClearCounts[1] != 1 || layerClearCounts[2] != 1) {
      throw new AssertionError("Background, bot, and trace clear actions must each fire once");
    }
    checkCondition("canvas".equals(selectedDriftMode[0]), "Drift mode selection must reach its callback");
    checkCondition(colorPickerCalls[0] == 1, "Trace base color action must reach its callback");
  }

  private static javax.swing.JComboBox<?> findComboBox(java.awt.Container container, String accessibleName) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JComboBox
          && accessibleName.equals(component.getAccessibleContext().getAccessibleName())) {
        return (javax.swing.JComboBox<?>) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.JComboBox<?> found = findComboBox((java.awt.Container) component, accessibleName);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static javax.swing.AbstractButton findButton(java.awt.Container container,
                                                        String accessibleName) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.AbstractButton
          && accessibleName.equals(component.getAccessibleContext().getAccessibleName())) {
        return (javax.swing.AbstractButton) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.AbstractButton found = findButton((java.awt.Container) component, accessibleName);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static javax.swing.JSlider findSlider(java.awt.Container container, String accessibleName) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JSlider
          && accessibleName.equals(component.getAccessibleContext().getAccessibleName())) {
        return (javax.swing.JSlider) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.JSlider found = findSlider((java.awt.Container) component, accessibleName);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static javax.swing.JLabel findLabelFor(java.awt.Container container,
                                                  java.awt.Component target) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JLabel
          && ((javax.swing.JLabel) component).getLabelFor() == target) {
        return (javax.swing.JLabel) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.JLabel found = findLabelFor((java.awt.Container) component, target);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static javax.swing.JLabel findLabelText(java.awt.Container container, String text) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JLabel
          && text.equals(((javax.swing.JLabel) component).getText())) {
        return (javax.swing.JLabel) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.JLabel found = findLabelText((java.awt.Container) component, text);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static javax.swing.AbstractButton findButtonByText(java.awt.Container container,
                                                               String text) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.AbstractButton
          && text.equals(((javax.swing.AbstractButton) component).getText())) {
        return (javax.swing.AbstractButton) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.AbstractButton found = findButtonByText((java.awt.Container) component, text);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static void checkAlignedBounds(java.awt.Component coordinateSpace,
      java.awt.Component... components) {
    java.awt.Rectangle firstBounds = javax.swing.SwingUtilities.convertRectangle(
      components[0].getParent(), components[0].getBounds(), coordinateSpace);
    for (int index = 1; index < components.length; index++) {
      java.awt.Rectangle bounds = javax.swing.SwingUtilities.convertRectangle(
        components[index].getParent(), components[index].getBounds(), coordinateSpace);
      checkCondition(bounds.x == firstBounds.x && bounds.width == firstBounds.width,
        components[index].getClass().getSimpleName()
          + " must share the same left and right margins as neighboring controls");
    }
  }

  private static javax.swing.JScrollPane findScrollPane(java.awt.Container container) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JScrollPane) {
        return (javax.swing.JScrollPane) component;
      }
      if (component instanceof java.awt.Container) {
        javax.swing.JScrollPane found = findScrollPane((java.awt.Container) component);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static javax.swing.JPanel findToolbar(java.awt.Container container) {
    javax.swing.JPanel found = findToolbarOrNull(container);
    if (found == null) throw new AssertionError("Could not find fixed-height tool ribbon");
    return found;
  }

  private static javax.swing.JPanel findToolbarOrNull(java.awt.Container container) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JPanel) {
        java.awt.LayoutManager layout = ((javax.swing.JPanel) component).getLayout();
        if (layout instanceof java.awt.GridLayout
            && ((java.awt.GridLayout) layout).getRows() == 2
            && ((java.awt.GridLayout) layout).getColumns() == 4) {
          return (javax.swing.JPanel) component;
        }
      }
      if (component instanceof java.awt.Container) {
        javax.swing.JPanel found = findToolbarOrNull((java.awt.Container) component);
        if (found != null) return found;
      }
    }
    return null;
  }

  private static void checkToolbarIcons(java.awt.Container panel, String[] toolNames) {
    for (String toolName : toolNames) {
      javax.swing.AbstractButton button = findButton(panel, toolName);
      checkCondition(button != null && button.getIcon() != null,
        toolName + " toolbar button must show an icon");
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void checkDropdownsHaveVisibleDefaults(java.awt.Container container) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JComboBox) {
        javax.swing.JComboBox<?> comboBox = (javax.swing.JComboBox<?>) component;
        Object selected = comboBox.getSelectedItem();
        checkCondition(comboBox.getItemCount() > 0 && comboBox.getSelectedIndex() >= 0
            && selected != null,
          "Every non-empty dropdown must have a selected default option");
        javax.swing.ListCellRenderer renderer = comboBox.getRenderer();
        java.awt.Component rendered = renderer.getListCellRendererComponent(
          new javax.swing.JList(), selected, comboBox.getSelectedIndex(), false, false);
        checkCondition(rendered instanceof javax.swing.JLabel
            && String.valueOf(selected).equals(((javax.swing.JLabel) rendered).getText()),
          "Dropdown renderer must visibly display the selected option");
        java.awt.Dimension originalSize = comboBox.getSize();
        java.awt.Dimension preferredSize = comboBox.getPreferredSize();
        comboBox.setSize(Math.max(180, preferredSize.width), Math.max(36, preferredSize.height));
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
          comboBox.getWidth(), comboBox.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        comboBox.paint(graphics);
        graphics.dispose();
        int visibleTextPixels = 0;
        int valueTextRight = Math.min(image.getWidth() - 24, 112);
        for (int y = 6; y < image.getHeight() - 6; y++) {
          for (int x = 12; x < valueTextRight; x++) {
            java.awt.Color pixel = new java.awt.Color(image.getRGB(x, y), true);
            if (pixel.getAlpha() > 0 && pixel.getRed() > 175
                && pixel.getGreen() > 175 && pixel.getBlue() > 175) {
              visibleTextPixels++;
            }
          }
        }
        checkCondition(visibleTextPixels >= 6,
          "Closed dropdown must visibly paint its selected option text");
        comboBox.setSize(originalSize);
      }
      if (component instanceof java.awt.Container) {
        checkDropdownsHaveVisibleDefaults((java.awt.Container) component);
      }
    }
  }

  private static void layoutTree(java.awt.Container container) {
    container.doLayout();
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof java.awt.Container) {
        layoutTree((java.awt.Container) component);
      }
    }
  }

  private static void checkInteractiveControlsFit(java.awt.Container container,
                                                   java.awt.Component viewportView,
                                                   int viewportWidth) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.JSlider
          || component instanceof javax.swing.AbstractButton
          || component instanceof javax.swing.JComboBox) {
        java.awt.Rectangle bounds = javax.swing.SwingUtilities.convertRectangle(
          component.getParent(), component.getBounds(), viewportView);
        checkCondition(bounds.x >= 0 && bounds.x + bounds.width <= viewportWidth,
          component.getClass().getSimpleName() + " must fit inside the settings viewport");
      }
      if (component instanceof java.awt.Container) {
        checkInteractiveControlsFit((java.awt.Container) component, viewportView, viewportWidth);
      }
    }
  }

  private static void clickToolButton(java.awt.Container container, String name,
                                      boolean useAccessibleName) {
    for (java.awt.Component component : container.getComponents()) {
      if (component instanceof javax.swing.AbstractButton) {
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) component;
        String candidate = useAccessibleName
          ? button.getAccessibleContext().getAccessibleName() : button.getText();
        if (name.equals(candidate)) {
          button.doClick();
          return;
        }
      }
      if (component instanceof java.awt.Container) {
        try {
          clickToolButton((java.awt.Container) component, name, useAccessibleName);
          return;
        } catch (AssertionError ignored) {
        }
      }
    }
    throw new AssertionError("Could not find tool control: " + name);
  }

  private static void checkCondition(boolean condition, String description) {
    if (!condition) {
      throw new AssertionError(description);
    }
  }

  private static void checkBilinearSampling() {
    VectorField field = new VectorField(20, 20, 10.0f);
    Vector2D[][] vectors = field.getVectors();
    vectors[0][0].vx = 0.0f;
    vectors[0][0].vy = 0.0f;
    vectors[1][0].vx = 1.0f;
    vectors[1][0].vy = 0.0f;
    vectors[0][1].vx = 0.0f;
    vectors[0][1].vy = 1.0f;
    vectors[1][1].vx = 1.0f;
    vectors[1][1].vy = 1.0f;

    Vector2D center = field.sample(5.0f, 5.0f);
    checkClose(center.vx, 0.5f, "bilinear x");
    checkClose(center.vy, 0.5f, "bilinear y");
    checkClose(field.sample(-5.0f, 5.0f).vy, 0.5f, "clamped edge");
    center.vx = 99.0f;
    checkClose(field.sample(5.0f, 5.0f).vx, 0.5f, "defensive sample copy");
  }

  private static void checkFieldMagnitudeScale() {
    VectorField field = new VectorField(20, 20, 10.0f);
    field.getVector(0, 0).vx = 2.0f;
    checkClose(field.sample(0.0f, 0.0f).vx, 2.0f, "default field magnitude scale");

    field.setFieldMagnitudeScale(1.5f);
    checkClose(field.sample(0.0f, 0.0f).vx, 3.0f, "scaled field sample");
    checkClose(field.getVector(0, 0).vx, 2.0f, "magnitude scale preserves stored data");

    field.setFieldMagnitudeScale(3.0f);
    checkClose(field.getFieldMagnitudeScale(), 2.0f, "maximum magnitude scale");
    field.setFieldMagnitudeScale(-1.0f);
    checkClose(field.getFieldMagnitudeScale(), 0.0f, "minimum magnitude scale");
  }

  private static void checkFieldRandomizationAndReset() {
    VectorField field = new VectorField(100, 100, 10.0f);
    field.randomize();
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) {
        if (vector.magnitude() < 0.5f || vector.magnitude() > 2.5f) {
          throw new AssertionError("Randomized vector magnitude is outside the expected range");
        }
      }
    }

    field.clear();
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) {
        checkClose(vector.magnitude(), 0.0f, "reset clears randomized field");
      }
    }
  }

  private static void checkEraserStrength() {
    VectorField field = new VectorField(100, 100, 10.0f);
    field.addForce(5, 5, 1.0f, 0.0f);
    field.subtractForceWithFalloff(
      50.0f, 50.0f, 20.0f, 0.5f, 0.5f, VectorField.FalloffType.LINEAR, 0.0f);
    checkClose(field.getVector(5, 5).vx, 0.5f, "half-strength erase");
  }

  private static void checkCleanNormalization() {
    VectorField field = new VectorField(20, 20, 10.0f);
    field.getVector(0, 0).vx = 3.0f;
    field.getVector(0, 0).vy = 4.0f;

    field.normalizeVectors();
    checkClose(field.getVector(0, 0).vx, 0.6f, "clean normalized x");
    checkClose(field.getVector(0, 0).vy, 0.8f, "clean normalized y");
    checkClose(field.getVector(1, 1).magnitude(), 0.0f, "clean preserves zero vectors");
  }

  private static void checkAdaptiveBotSmoothing() {
    Bot bot = new Bot(50.0f, 50.0f, 100.0f, 20.0f, 1.0f);
    bot.getVelocity().vx = 1.0f;
    bot.getVelocity().vy = 0.0f;
    bot.setDriftInfluence(0.0f);
    bot.setFieldInfluence(1.0f);
    bot.setRepulsionInfluence(0.0f);
    bot.update(new Vector2D(0.0f, 1.0f), new java.util.ArrayList<Bot>(), 0.0f);
    checkClose(bot.getVelocity().vx, 0.5f, "adaptive turn smoothing x");
    checkClose(bot.getVelocity().vy, 0.5f, "adaptive turn smoothing y");
  }

  private static void checkBotStabilityModulation() {
    Bot stableBot = new Bot(50.0f, 50.0f, 100.0f, 20.0f, 1.0f);
    stableBot.getVelocity().zero();
    stableBot.setDriftInfluence(1.0f);
    stableBot.setFieldInfluence(0.0f);
    stableBot.setRepulsionInfluence(0.0f);
    stableBot.update(new Vector2D(), new ArrayList<Bot>(), 0.0f, 0.0f);

    Bot unstableBot = new Bot(50.0f, 50.0f, 100.0f, 20.0f, 1.0f);
    unstableBot.getVelocity().zero();
    unstableBot.setDriftInfluence(1.0f);
    unstableBot.setFieldInfluence(0.0f);
    unstableBot.setRepulsionInfluence(0.0f);
    unstableBot.update(new Vector2D(), new ArrayList<Bot>(), 0.0f, 1.0f);

    if (unstableBot.getVelocity().magnitude() >= stableBot.getVelocity().magnitude()) {
      throw new AssertionError("Unstable conditions did not reduce bot movement");
    }
    if (stableBot.getCurvatureVariance() < 0.0f || unstableBot.getCurvatureVariance() < 0.0f) {
      throw new AssertionError("Curvature variance must not be negative");
    }
  }

  private static void checkBotDriftModes() {
    BotEngine collective = createDriftEngine("collective");
    Vector2D firstCollective = collective.getBots().get(0).getVelocity();
    Vector2D secondCollective = collective.getBots().get(1).getVelocity();
    checkClose(firstCollective.vx, secondCollective.vx, "collective drift shared x");
    checkClose(firstCollective.vy, secondCollective.vy, "collective drift shared y");

    BotEngine canvas = createDriftEngine("canvas");
    Vector2D firstCanvas = canvas.getBots().get(0).getVelocity();
    Vector2D secondCanvas = canvas.getBots().get(1).getVelocity();
    if (firstCanvas.distance(secondCanvas) < 0.1f) {
      throw new AssertionError("Canvas drift should vary with bot position");
    }

    BotEngine unique = new BotEngine(new VectorField(100, 100, 10.0f));
    checkCondition("unique".equals(unique.getDriftMode()), "Unique drift must be the default");
    unique.setDriftMode("invalid");
    checkCondition("unique".equals(unique.getDriftMode()), "Unknown drift modes must be ignored");
  }

  private static BotEngine createDriftEngine(String mode) {
    VectorField field = new VectorField(200, 200, 10.0f);
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) {
        vector.vx = 1.0f;
      }
    }
    BotEngine engine = new BotEngine(field);
    engine.setDriftMode(mode);
    engine.setDriftInfluence(1.0f);
    engine.setFieldInfluence(0.0f);
    engine.setRepulsionInfluence(0.0f);
    engine.setRepulsionRadius(0.0f);
    engine.spawnBotAt(20.0f, 20.0f);
    engine.spawnBotAt(180.0f, 180.0f);
    engine.getBots().get(0).getVelocity().zero();
    engine.getBots().get(1).getVelocity().zero();
    engine.update();
    return engine;
  }

  private static void checkLiveBotParameterUpdates() {
    BotEngine engine = new BotEngine(new VectorField(100, 100, 10.0f));
    engine.spawnBotAt(50.0f, 50.0f);
    Bot bot = engine.getBots().get(0);
    engine.setBotRadar(35.0f);
    engine.setBotSpeed(3.0f);
    engine.setBotMaxLife(500.0f);
    checkClose(bot.getRadar(), 35.0f, "live bot radar update");
    checkClose(bot.getSpeed(), 3.0f, "live bot speed update");
    checkClose(bot.getLifeNormalized(), 1.0f, "live bot life update");
  }

  private static void checkRadarSampleBounds() {
    BotEngine engine = new BotEngine(new VectorField(100, 100, 10.0f));
    engine.setRadarSamples(1);
    if (engine.getRadarSamples() != 4) {
      throw new AssertionError("Radar samples must clamp to the minimum of 4");
    }
    engine.setRadarSamples(99);
    if (engine.getRadarSamples() != 32) {
      throw new AssertionError("Radar samples must clamp to the maximum of 32");
    }
  }

  private static void checkLifeExpiryRespawnsWithoutAutoSpawn() {
    VectorField field = new VectorField(100, 100, 10.0f);
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) {
        vector.vx = 1.0f;
      }
    }
    BotEngine engine = new BotEngine(field);
    engine.setBotMaxLife(1.0f);
    engine.spawnBotAt(50.0f, 50.0f);
    engine.update();
    engine.update();
    if (engine.getBots().size() != 1 || engine.getDeadBots().size() != 1) {
      throw new AssertionError("An expired bot must respawn while retaining its trace");
    }
  }

  private static void checkRateLimitedAutoSpawn() {
    VectorField field = new VectorField(100, 100, 10.0f);
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) {
        vector.vx = 1.0f;
      }
    }
    BotEngine engine = new BotEngine(field);
    engine.setBotNumber(3);
    engine.setBotSpawnRate(60);
    engine.setAutoSpawnEnabled(true);
    engine.update();
    if (engine.getBots().size() != 1) {
      throw new AssertionError("Auto-spawn should add no more than one bot per interval");
    }
    engine.update();
    engine.update();
    if (engine.getBots().size() != 3) {
      throw new AssertionError("Rate-limited auto-spawn did not reach its target");
    }
  }

  private static void checkFieldNormalization() {
    VectorField field = new VectorField(20, 20, 10.0f);
    Vector2D[][] vectors = field.getVectors();
    vectors[0][0].vx = 5.0f;
    vectors[0][1].vx = 5.0f;
    vectors[1][0].vx = 2.0f;

    field.normalizeFieldIfNeeded();
    checkClose(vectors[0][0].vx, 3.5f, "normalization at threshold");
    checkClose(vectors[1][0].vx, 1.4f, "proportional field normalization");

    field.normalizeFieldIfNeeded();
    checkClose(vectors[0][0].vx, 3.5f, "normalization remains below threshold");
  }

  private static void checkFalloffAndHardness() {
    Brush brush = new Brush();
    brush.setFalloffType(VectorField.FalloffType.HARD_EDGE);
    if (brush.getEffectiveFalloffType() != VectorField.FalloffType.HARD_EDGE) {
      throw new AssertionError("Explicit falloff selection was ignored");
    }

    VectorField softField = new VectorField(100, 100, 10.0f);
    softField.addForceWithFalloff(
      50.0f, 50.0f, 40.0f, 0.0f, 1.0f, VectorField.FalloffType.GAUSSIAN, 0.0f);
    VectorField hardField = new VectorField(100, 100, 10.0f);
    hardField.addForceWithFalloff(
      50.0f, 50.0f, 40.0f, 0.0f, 1.0f, VectorField.FalloffType.GAUSSIAN, 0.5f);
    if (hardField.getVector(6, 5).vy <= softField.getVector(6, 5).vy) {
      throw new AssertionError("Brush hardness did not change the Gaussian core");
    }
  }

  private static void checkBrushSpacing() {
    Brush brush = new Brush(5.0f, 0.0f, 0.5f, 5.0f);
    VectorField field = new VectorField(100, 100, 10.0f);
    BrushEngine engine = new BrushEngine(brush, field);
    engine.startStroke(20.0f, 20.0f);
    engine.paintAt(35.0f, 20.0f);
    if (field.getVector(2, 2).vx < 1.9f || field.getVector(3, 2).vx < 1.9f) {
      throw new AssertionError("Long segment omitted intermediate brush dabs");
    }
    engine.endStroke();

    Brush secondBrush = new Brush(5.0f, 0.0f, 0.5f, 5.0f);
    VectorField secondField = new VectorField(100, 100, 10.0f);
    BrushEngine secondEngine = new BrushEngine(secondBrush, secondField);
    secondEngine.startStroke(20.0f, 20.0f);
    if (secondEngine.paintAt(23.0f, 20.0f)) {
      throw new AssertionError("Brush applied before reaching spacing");
    }
    if (!secondEngine.paintAt(26.0f, 20.0f)) {
      throw new AssertionError("Accumulated movement did not trigger a brush dab");
    }
    if (secondField.getVector(2, 2).vx < 1.9f) {
      throw new AssertionError("Brush dab was not placed at the interpolated point");
    }
  }

  private static void checkLuminanceLifeDecay() {
    float darkBackgroundLife = remainingLifeOnBackground(0x000000);
    float lightBackgroundLife = remainingLifeOnBackground(0xFFFFFF);
    if (lightBackgroundLife >= darkBackgroundLife) {
      throw new AssertionError("Bright backgrounds must drain more bot life than dark backgrounds");
    }
  }

  private static void checkSimulationBackgroundSources() {
    render.LayerRenderer renderer = new render.LayerRenderer(4, 4);
    Color backgroundColor = new Color(24, 48, 72);
    renderer.setBackgroundColor(backgroundColor);
    if (renderer.getBackgroundForSimulation().getRGB(0, 0) != backgroundColor.getRGB()) {
      throw new AssertionError("Background color was not exposed to bot simulation");
    }

    BufferedImage reference = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    reference.setRGB(0, 0, Color.WHITE.getRGB());
    renderer.setBackgroundImage(reference);
    if (renderer.getBackgroundForSimulation() != renderer.getBackgroundImage()
        || renderer.getBackgroundForSimulation().getRGB(0, 0) != Color.WHITE.getRGB()) {
      throw new AssertionError("Loaded background image was not exposed to bot simulation");
    }

    renderer.clearBackgroundImage();
    if (renderer.getBackgroundForSimulation().getRGB(0, 0) != backgroundColor.getRGB()) {
      throw new AssertionError("Clearing the image did not restore the selected background color");
    }
    renderer.setBackgroundImage(reference);
    int simulationPixel = renderer.getBackgroundForSimulation().getRGB(0, 0);
    renderer.setBackgroundTransparency(0.0f);
    if (renderer.getBackgroundForSimulation().getRGB(0, 0) != simulationPixel) {
      throw new AssertionError("Display transparency must not alter bot luminance input");
    }
  }

  private static void checkDrawingExport() throws Exception {
    Path directory = Files.createTempDirectory("flowdrawing-export-check");
    try {
      List<List<Vector2D>> paths = new ArrayList<>();
      paths.add(Arrays.asList(new Vector2D(1.0f, 2.0f), new Vector2D(3.0f, 4.0f)));
      Path svg = directory.resolve("drawing.svg");
      Path dxf = directory.resolve("drawing.dxf");
      Path png = directory.resolve("drawing.png");
      Path jpg = directory.resolve("drawing.jpg");

      DrawingExporter.writeSvg(svg, paths, 20, 30);
      DrawingExporter.writeDxf(dxf, paths);
      BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
      image.setRGB(0, 0, Color.RED.getRGB());
      DrawingExporter.writeRaster(png, image, "png");
      DrawingExporter.writeRaster(jpg, image, "jpg");

      String svgText = new String(Files.readAllBytes(svg), StandardCharsets.UTF_8);
      String dxfText = new String(Files.readAllBytes(dxf), StandardCharsets.US_ASCII);
      if (!svgText.contains("<polyline points=\"1.000,2.000 3.000,4.000\"/>")) {
        throw new AssertionError("SVG export omitted the bot path");
      }
      if (!dxfText.contains("0\nLINE\n8\n0\n10\n1.000\n20\n2.000")) {
        throw new AssertionError("DXF export omitted the bot segment");
      }
      BufferedImage exportedImage = javax.imageio.ImageIO.read(png.toFile());
      if (exportedImage == null || exportedImage.getRGB(0, 0) != Color.RED.getRGB()) {
        throw new AssertionError("PNG export did not preserve the raster image");
      }
      BufferedImage exportedJpeg = javax.imageio.ImageIO.read(jpg.toFile());
      if (exportedJpeg == null || exportedJpeg.getWidth() != 2 || exportedJpeg.getHeight() != 2) {
        throw new AssertionError("JPG export did not produce a readable raster image");
      }
    } finally {
      try (java.util.stream.Stream<Path> files = Files.list(directory)) {
        files.forEach(file -> {
          try {
            Files.deleteIfExists(file);
          } catch (java.io.IOException exception) {
            throw new RuntimeException(exception);
          }
        });
      }
      Files.deleteIfExists(directory);
    }
  }

  private static void checkPathSimplification() {
    List<Vector2D> path = Arrays.asList(
      new Vector2D(0.0f, 0.0f),
      new Vector2D(1.0f, 0.1f),
      new Vector2D(2.0f, 0.0f),
      new Vector2D(3.0f, 3.0f));
    List<Vector2D> simplified = PathOptimizer.simplify(path, 0.5f);
    if (simplified.size() != 3) {
      throw new AssertionError("Douglas-Peucker did not remove a point within epsilon");
    }
    checkClose(simplified.get(0).vx, 0.0f, "simplification start endpoint");
    checkClose(simplified.get(2).vy, 3.0f, "simplification end endpoint");

    List<Vector2D> loop = Arrays.asList(
      new Vector2D(0.0f, 0.0f), new Vector2D(2.0f, 0.0f),
      new Vector2D(2.0f, 2.0f), new Vector2D(0.0f, 0.0f));
    List<Vector2D> simplifiedLoop = PathOptimizer.simplify(loop, 10.0f);
    if (simplifiedLoop.size() < 3
        || simplifiedLoop.get(0).distance(simplifiedLoop.get(simplifiedLoop.size() - 1)) > 0.001f) {
      throw new AssertionError("Path simplification did not preserve the closed loop");
    }

    List<Vector2D> corners = Arrays.asList(
      new Vector2D(0.0f, 0.0f), new Vector2D(10.0f, 0.0f),
      new Vector2D(10.0f, 10.0f), new Vector2D(20.0f, 10.0f));
    List<Vector2D> simplifiedCorners = PathOptimizer.simplify(corners, 100.0f);
    if (simplifiedCorners.size() != 4) {
      throw new AssertionError("Simplification removed a sharp corner");
    }

    List<Vector2D> longLine = Arrays.asList(
      new Vector2D(0.0f, 0.0f), new Vector2D(20.0f, 0.0f),
      new Vector2D(40.0f, 0.0f), new Vector2D(60.0f, 0.0f),
      new Vector2D(80.0f, 0.0f));
    List<Vector2D> segmentedLine = PathOptimizer.simplify(longLine, 5.0f);
    if (segmentedLine.size() < 3) {
      throw new AssertionError("Simplification created excessively long segments");
    }
  }

  private static void checkNearestNeighborOrdering() {
    List<List<Vector2D>> paths = new ArrayList<>();
    paths.add(Arrays.asList(new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f)));
    paths.add(Arrays.asList(new Vector2D(20.0f, 0.0f), new Vector2D(2.0f, 0.0f)));
    paths.add(Arrays.asList(new Vector2D(4.0f, 0.0f), new Vector2D(5.0f, 0.0f)));

    List<List<Vector2D>> ordered = PathOptimizer.orderNearestNeighbor(paths);
    checkClose(ordered.get(1).get(0).vx, 2.0f, "nearest path reversal start");
    checkClose(ordered.get(1).get(1).vx, 20.0f, "nearest path reversal end");
    checkClose(ordered.get(2).get(0).vx, 5.0f, "nearest path reversal");
    checkClose(ordered.get(2).get(1).vx, 4.0f, "nearest path preserves geometry");
  }

  private static float remainingLifeOnBackground(int color) {
    VectorField field = new VectorField(100, 100, 10.0f);
    for (Vector2D[] column : field.getVectors()) {
      for (Vector2D vector : column) {
        vector.vx = 1.0f;
      }
    }

    BotEngine engine = new BotEngine(field);
    engine.setBotMaxLife(100.0f);
    engine.spawnBotAt(50.0f, 50.0f);
    BufferedImage background = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < background.getHeight(); y++) {
      for (int x = 0; x < background.getWidth(); x++) {
        background.setRGB(x, y, color);
      }
    }
    engine.setBackgroundImage(background);
    engine.setLuminanceDecayEnabled(true);
    engine.update();
    return engine.getBots().get(0).getLifeNormalized();
  }

  private static void checkClose(float actual, float expected, String label) {
    if (Math.abs(actual - expected) > 0.001f) {
      throw new AssertionError(label + ": expected " + expected + ", got " + actual);
    }
  }
}