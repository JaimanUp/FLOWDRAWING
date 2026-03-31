# FLOWDRAWING UI Components Breakdown

## Overview
This document details all UI elements in the ToolPanel sidebar and their styling properties.

---

## Color Palette Reference

| Constant | RGB | Hex | Purpose |
|----------|-----|-----|---------|
| `COLOR_PANEL_BG` | 46, 46, 46 | #2E2E2E | Main background for panels and tabs |
| `COLOR_SECTION_BG` | 55, 55, 55 | #373737 | Background for section panels |
| `COLOR_SECTION_BORDER` | 90, 90, 90 | #5A5A5A | Border for section dividers |
| `COLOR_TEXT_PRIMARY` | 200, 200, 200 | #C8C8C8 | Primary text for labels and controls |
| `COLOR_TEXT_SECONDARY` | 180, 180, 180 | #B4B4B4 | Secondary text for titles |
| `COLOR_TEXT_TAB` | Light Gray | N/A | Tab label text |

---

## 1. JTabbedPane (Main Container)

**Purpose:** Main tabbed interface container for organizing tool panels

**Properties:**
- **Layout:** `JTabbedPane.TOP` - tabs positioned at top
- **Tab Policy:** `SCROLL_TAB_LAYOUT` - scrollable if overflow
- **Background:** `COLOR_PANEL_BG`
- **Foreground (Text):** `COLOR_TEXT_TAB`

**Tabs:**
1. **BG** - Background controls (Visibility, Appearance, Image)
2. **Field** - Vector field controls (Visibility, Appearance, Visualization mode, Actions)
3. **Sketch** - Sketch/brush controls (Visibility, Appearance, Brush Settings, Actions)
4. **Bots** - Bot simulation controls (Visibility, Appearance, Bot Settings, Spawn, Simulation)

**Code Location:** Lines 84-97

---

## 2. Section Panels

**Purpose:** Organize related controls into logical groups

**Styling Method:** `section(String title)` - Lines 114-127
- **Layout:** Vertical BoxLayout
- **Border:** `TitledBorder` with:
  - Line border in `COLOR_SECTION_BORDER`
  - Title text in `COLOR_TEXT_SECONDARY`
  - Font: Arial Bold 11pt
- **Background:** `COLOR_SECTION_BG`

**Section Types Used:**
- Visibility
- Appearance
- Image
- Brush Settings
- Bot Settings
- Simulation
- Actions

---

## 3. JLabel (Text Labels)

**Purpose:** Display static text and dynamic status values

**Styling Method:** `rowLabel(String text)` - Lines 129-135
- **Foreground:** `COLOR_TEXT_PRIMARY`
- **Font:** Arial 11pt (Plain)
- **Alignment:** Left-aligned

**Types:**
- **Static Labels:** "Visibility", "Appearance", "Brush Settings", etc.
- **Dynamic Status Labels:**
  - `brushSizeLabel` - "Size: 30"
  - `brushHardnessLabel` - "Hardness: 0.50"
  - `brushStrengthLabel` - "Strength: 2.0"
  - `botLifeLabel` - "Life: 500"
  - `botRadarLabel` - "Radar: 50"
  - `botDriftLabel` - "Drift: 0.30"
  - `botSpeedLabel` - "Speed: 2.0"
  - `botSpawnRateLabel` - "Spawn Rate: 5"

---

## 4. JSlider (Continuous Value Control)

**Purpose:** Allow users to adjust numeric values smoothly

**Styling Method:** `rowSlider(int min, int max, int value)` - Lines 137-145
- **Dimensions:** 180px width × 28px height
- **Background:** `COLOR_SECTION_BG`
- **Alignment:** Left-aligned

**Sliders Used:**
| Slider | Range | Default | Purpose |
|--------|-------|---------|---------|
| Alpha (Background) | 0-100 | 100 | Background transparency |
| Alpha (Vector Field) | 0-100 | 100 | Field transparency |
| Alpha (Sketch) | 0-100 | 100 | Strokes transparency |
| Size | 5-100 | 30 | Brush size |
| Hardness | 0-100 | 50 | Brush hardness |
| Strength | 1-50 | 20 | Brush strength (÷10 for display) |
| Scale | 1-500 | 100 | Vector field scale (÷100 for display) |
| Force | 0-200 | 100 | Vector field force (÷100 for display) |
| Life | 100-2000 | 500 | Bot lifespan |
| Radar | 10-200 | 50 | Bot detection radius |
| Drift | 0-100 | 30 | Bot drift (÷100 for display) |
| Speed | 1-50 | 20 | Bot speed (÷10 for display) |
| Spawn Rate | 1-20 | 5 | Bots spawned per frame |

---

## 5. JButton (Action Buttons)

**Purpose:** Trigger actions and open dialogs

**Styling Method:** `rowButton(String text)` - Lines 147-151
- **Dimensions:** 180px width (max) × 26px height (max)
- **Alignment:** Left-aligned

**Buttons:**
| Button | Action | Location |
|--------|--------|----------|
| Colour... | Open color picker | Background > Appearance |
| Load Image... | Load background image | Background > Image |
| Clear Image | Remove background image | Background > Image |
| Reset Field | Randomize vector field | Vector Field > Actions |
| Randomize Field | Reset vector field | Vector Field > Actions |
| Clear Strokes | Clear all drawn strokes | Sketch > Actions |
| Spawn Bot | Create bot at random position | Bots > Spawn |
| Clear Bots | Remove all bots | Bots > Spawn |

---

## 6. JCheckBox (Boolean Toggle)

**Purpose:** Toggle features on/off

**Styling Method:** `rowCheckBox(String text, boolean selected)` - Lines 153-160
- **Background:** `COLOR_SECTION_BG`
- **Foreground:** `COLOR_TEXT_PRIMARY`
- **Font:** Arial 11pt (Plain)
- **Alignment:** Left-aligned

**Checkboxes:**
| Checkbox | Default | Purpose |
|----------|---------|---------|
| Show Background | ✓ | Display/hide background layer |
| Show Vector Field | ✓ | Display/hide vector field |
| Show Strokes | ✓ | Display/hide drawn strokes |
| Show Botfield | ✓ | Display/hide bots |
| Auto Spawn | ✗ | Enable automatic bot spawning |

---

## 7. JRadioButton (Mutually Exclusive Selection)

**Purpose:** Choose between mutually exclusive options

**Styling Method:** `styleRadio(JRadioButton rb)` - Lines 489-495
- **Background:** `COLOR_SECTION_BG`
- **Foreground:** `COLOR_TEXT_PRIMARY`
- **Font:** Arial 11pt (Plain)
- **Grouped via:** `ButtonGroup`

**Radio Button Groups:**

### Visualization Mode (Vector Field Tab)
- **Arrow** - Display field as arrows (default)
- **Heatmap** - Display field as heat map

---

## 8. JScrollPane (Container)

**Purpose:** Enable vertical scrolling for tab content

**Styling Method:** `wrapScroll(JPanel content)` - Lines 103-110
- **Scroll Bars:** Vertical as-needed, Horizontal never
- **Border:** Empty (no border)
- **Unit Increment:** 12 pixels per scroll

**Applied To:** All four tab content panels

---

## 9. Helper Methods

### `pad(JPanel p)` - Line 162
Adds vertical spacing between controls
- **Size:** 4px rigid area

### `section(String title)` - Lines 114-127
Creates a titled section panel

### `rowLabel(String text)` - Lines 129-135
Creates a styled label

### `rowSlider(int min, int max, int value)` - Lines 137-145
Creates a styled slider

### `rowButton(String text)` - Lines 147-151
Creates a styled button

### `rowCheckBox(String text, boolean selected)` - Lines 153-160
Creates a styled checkbox

### `styleRadio(JRadioButton rb)` - Lines 489-495
Applies styling to radio button

---

## Layout Structure

```
ToolPanel (BorderLayout)
└── JTabbedPane
    ├── Tab 1: BG
    │   └── JScrollPane
    │       └── VBox Root Panel
    │           ├── Section: Visibility
    │           │   └── Checkbox: Show Background
    │           ├── Padding
    │           ├── Section: Appearance
    │           │   ├── Label: Transparency
    │           │   ├── Slider: Alpha
    │           │   └── Button: Colour...
    │           ├── Padding
    │           ├── Section: Image
    │           │   ├── Button: Load Image...
    │           │   ├── Padding
    │           │   └── Button: Clear Image
    │           └── Vertical Glue (fill space)
    │
    ├── Tab 2: Field
    │   └── [Similar structure with Field-specific controls]
    │
    ├── Tab 3: Sketch
    │   └── [Similar structure with Sketch-specific controls]
    │
    └── Tab 4: Bots
        └── [Similar structure with Bot-specific controls]
```

---

## Customization Points

To modify UI appearance, edit color constants at the top of ToolPanel.java:

```java
private static final Color COLOR_PANEL_BG = new Color(46, 46, 46);
private static final Color COLOR_SECTION_BG = new Color(55, 55, 55);
private static final Color COLOR_SECTION_BORDER = new Color(90, 90, 90);
private static final Color COLOR_TEXT_PRIMARY = new Color(200, 200, 200);
private static final Color COLOR_TEXT_SECONDARY = new Color(180, 180, 180);
private static final Color COLOR_TEXT_TAB = Color.LIGHT_GRAY;
```

All UI elements use these constants, so changing a color value updates the entire interface automatically.

---

## Summary Table

| Element | Count | Creation Method | Styling Method |
|---------|-------|-----------------|-----------------|
| JTabbedPane | 1 | Constructor | Constructor (Lines 84-87) |
| Section Panels | 13+ | `section()` | TitledBorder styling |
| JLabel | 20+ | `rowLabel()` | Arial 11pt, COLOR_TEXT_PRIMARY |
| JSlider | 13 | `rowSlider()` | Dimensions 180×28, COLOR_SECTION_BG |
| JButton | 8 | `rowButton()` | Max 180×26, basic styling |
| JCheckBox | 5 | `rowCheckBox()` | COLOR_SECTION_BG, COLOR_TEXT_PRIMARY |
| JRadioButton | 2 | `styleRadio()` | styled via method |
| JScrollPane | 4 | `wrapScroll()` | 12px increment, empty border |
