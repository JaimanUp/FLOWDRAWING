# Initial Tasks & Starting Point

Before writing any production code, the following steps should be completed to establish a clear foundation:

1. **Repository setup**
   - Create a simple `.gitignore` (Java/Processing output files, `.class`, `~` backups).
   - Add `README.md` (this file) and ensure both requirement docs are committed.

2. **Project scaffolding**
   - Create the directory hierarchy under `/src` as outlined in the roadmap.
   - Add `MainApp.pde` containing a minimal Processing sketch that:
     - Sets a P2D renderer
     - Opens a window at 800×600
     - Instantiates a `CanvasManager` placeholder
     - Responds to mouse drag for pan and mouse wheel for zoom
   - Add stub Java files (or PDE sketches) for `CanvasManager`, `CameraController`, `LayerRenderer` in appropriate packages/folders.

3. **Build configuration**
   - Decide whether to use the Processing IDE or a Gradle/Maven setup.
   - If using Gradle, create a `build.gradle` with `processing-core` dependency and a `run` task.

4. **Versioning and branches**
   - Use `master` for stable snapshots.
   - Create feature branches such as `phase-1-canvas` for work on the canvas system.

5. **Performance baseline**
   - After the initial canvas and camera are working, render a large `PGraphics` (e.g. 4000×4000) and measure frame rate.
   - Log results to guide later optimizations.

6. **Configuration placeholders**
   - Add a `config/Config.java` (or `Config.pde`) with constants for:
     - `NORMALIZATION_THRESHOLD = 0.3f` (percentage of vectors at max magnitude)
     - `RADAR_SAMPLES = 16`
     - `CELL_SIZE = 10` (default grid spacing)
   - These can be exposed via UI later but exist now for early testing.

7. **Documentation updates**
   - Keep `deveopment_roadmap.txt` and `Software requirements` in sync with decisions.
   - Add links to these docs in the README.

Working through this checklist ensures everyone begins with a consistent understanding and a runnable skeleton. Once these items are done, the path to Phase 1 (core canvas) is clear and development can proceed incrementally.