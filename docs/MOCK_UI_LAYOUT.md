# Mock UI Layout - Tool-Centric Sidepanel

## Visual Overview

```
┌─────────────────────────────────────────────────┐
│ FlowDrawing                          _ ◻ ✕     │  ← Native title bar
├─────────────────────────────────────────────────┤
│ File  Edit  View  Help                          │  ← Menu bar
├──────────────────┬──────────────────────────────┤
│                  │                              │
│                  │        CANVAS AREA           │
│   SIDEPANEL      │         (Main render)        │
│   (230px)        │                              │
│                  │                              │
│  ┌──────────────────────────┐                   │
│  │ [●BKGD] [VECFD] [BOTFD] │  ← Layer tabs     │
│  └──────────────────────────┘                   │
│                  │                              │
│  ┌──────────────────────────┐                   │
│  │ [eye] [──────] [clear]    │ ← Layer controls│
│  └──────────────────────────┘                   │
│                  │                              │
│  ┌──────────────────────────┐                   │
│  │ SETTINGS: Color          │  ← Tool settings  │
│  │ [Show color picker...]   │                   │
│  │                          │                   │
│  └──────────────────────────┘                   │
│                  │                              │
└──────────────────┴──────────────────────────────┘
```

---

## Detailed Panel Layout (230px width)

### Section 1: Layer Selector Tabs (Top)
```
┌─────────────────────────────┐
│ [ ● BKGD ] [ VECFD ] [ BOTFD ]
└─────────────────────────────┘
```
**Contains**: 
- Horizontal layer selector (3 tabs)
- ● indicates selected layer (highlighted green)
- Clicking a layer switches tool ribbon and settings below

---

### Section 2: General Layer Controls
```
┌─────────────────────────────┐
│ [eye] [────────] [layer action] │
└─────────────────────────────┘
```
**Contains**:
- Open/closed eye icon; its color changes with visibility state
- Unlabeled layer-wide transparency slider
- Contextual icon actions: clear background, reset field, or clear bots/traces
- Values persist independently for BKGD, VECFD, and BOTFD

### Section 3: Tool Icon Ribbon
Contains only the active layer's tools. Selecting a tool updates the settings panel below.

#### BKGD Layer - Complete View
```
┌─────────────────────────────┐
│ Layer: BKGD                 │
├─────────────────────────────┤
│ [◉] [──────] [⟲]             │
├─────────────────────────────┤
│ Tools:                      │
│ [ ● Color ] [ • Image ]     │
├─────────────────────────────┤
│ SETTINGS: Color             │
├─────────────────────────────┤
│ Color Picker                │
│ [ Open Color Dialog ]       │
│ Currently: RGB(255,0,0)     │
│                             │
└─────────────────────────────┘
```

**User Action**: Toggle visibility in the general controls row
```
Layer Controls:
Eye open [──────] [clear]
↓ (click eye)
Eye closed [──────] [clear]

Layer hidden on canvas
Settings panel UNCHANGED: Still shows Color tool settings
```

#### BKGD Layer - Image Tool
```
┌─────────────────────────────┐
│ Layer: BKGD                 │
├─────────────────────────────┤
│ [eye] [───●───] [clear]     │
├─────────────────────────────┤
│ Tools:                      │
│ [ • Color ] [ ● Image ]     │
├─────────────────────────────┤
│ SETTINGS: Image             │
├─────────────────────────────┤
│ [ Load Image ] (button)     │
│ Loaded: background.jpg      │
│ Clear is in the layer row   │
└─────────────────────────────┘
```

#### VECFD Layer - Paint Tool Selected
```
┌─────────────────────────────┐
│ Layer: VECFD                │
├─────────────────────────────┤
│ [◉] [──────] [⟲]             │
├─────────────────────────────┤
│ Tools:                      │
│ [ ● Paint ] [ • Erase ]     │
│ [ • Field Actions ]          │
├─────────────────────────────┤
│ SETTINGS: Paint             │
├─────────────────────────────┤
│ Falloff Type:               │
│ (●) Linear  ○ Gaussian      │
│ ○ Hard Edge                 │
│                             │
│ ◀─ Size ──────────────────▶ │
│    Value: 45                │
│                             │
│ ◀─ Hardness ───────────────▶ │
│    Value: 0.75              │
│                             │
│ ◀─ Strength ───────────────▶ │
│    Value: 1.0               │
│                             │
└─────────────────────────────┘
```

**User Action**: Hide layer using the general controls row
```
Layer Controls:
Eye open [──────] [reset]
↓ (click eye)
Eye closed [──────] [reset]

Layer hidden on canvas
Tool selection UNCHANGED: Still Paint with settings shown
```

#### VECFD Layer - Erase Tool Selected
```
┌─────────────────────────────┐
│ Layer: VECFD                │
├─────────────────────────────┤
│ [eye] [──────] [reset]      │
├─────────────────────────────┤
│ Tools:                      │
│ [ • Paint ] [ ● Erase ]     │
│ [ • Field Actions ]          │
├─────────────────────────────┤
│ SETTINGS: Erase             │
├─────────────────────────────┤
│ Falloff Type:               │
│ (●) Linear  ○ Gaussian      │
│ ○ Hard Edge                 │
│                             │
│ ◀─ Size ──────────────────▶ │
│    Value: 60                │
│                             │
│ ◀─ Hardness ───────────────▶ │
│    Value: 0.50              │
│                             │
│ ◀─ Strength ───────────────▶ │
│    Value: 0.8               │
│                             │
└─────────────────────────────┘
```

#### BOTFD Layer - Place Tool Selected
```
┌──────────────────────────────────────┐
│ Layer: BOTFD                         │
├──────────────────────────────────────┤
│ [eye] [──────] [clear bots] [traces] │
├──────────────────────────────────────┤
│ [ ● Place ] [ • Random ] [ • Auto ]  │
│ [ • Export ]                         │
├─────────────────────────────┤
│ SETTINGS: Place             │
├─────────────────────────────┤
│ General                     │
│ ◀─ Amount ────────────────▶ │
│    Value: 5                 │
│                             │
│ [Color Picker] (popup)      │
│                             │
│ Force Balance               │
│ ◀─ Vector Field ──────────▶ │
│ ◀─ Repulsion ─────────────▶ │
│ ◀─ Drifting ──────────────▶ │
│                             │
│ [Show More ▼]               │
│                             │
└─────────────────────────────┘
```

#### BOTFD Layer - Auto Tool Selected
```
┌─────────────────────────────┐
│ Layer: BOTFD                │
├─────────────────────────────┤
│ [eye] [──────] [clear bots] [traces] │
├─────────────────────────────┤
│ Tools:                      │
│ [ • Place ] [ • Random ]    │
│ [ ● Auto ] [ • Export ]     │
├─────────────────────────────┤
│ SETTINGS: Auto Spawn        │
├─────────────────────────────┤
│ General                     │
│ ◀─ Amount ────────────────▶ │
│                             │
│ [Color Picker]              │
│                             │
│ Force Balance               │
│ ◀─ Vector Field ──────────▶ │
│ ◀─ Repulsion ─────────────▶ │
│ ◀─ Drifting ──────────────▶ │
│                             │
│ [Show More ▼]               │
│                             │
└─────────────────────────────┘
```
```

---

## Interaction Patterns

## Interaction Patterns

### General Layer Controls
```
Layer Controls:
Eye open [──────] [layer action]

User Action: Click open eye icon
  Open eye → closed eye  (Layer hidden)

RESULT:
  - Layer hides on canvas
  - Tool selection UNCHANGED (Paint still selected)
  - Settings panel UNCHANGED
  - Transparency slider remains beside the visibility toggle

NOTE: Both general controls apply to the entire active layer and are INDEPENDENT of:
  - Tool icon selection (clicking eye doesn't change tool)
  - Settings content (settings show tool parameters)
  - Other layers (each layer keeps its own values)
```

### Tool Icon Ribbon (Icon-Tab Style)
```
Multiple tools as selectable icons (like tabs):
┌─────────────────────────────┐
│ [ ● Paint ] [ • Erase ]     │  ← Small icon buttons
│ [ • Field Actions ]         │
└─────────────────────────────┘

● = Selected (highlighted in green)
• = Inactive (dimmed gray)

On click:
1. Icon highlighted in neon green
2. Settings panel below updates to show selected tool's settings
3. Position of settings panel stays same - only content changes
4. Smooth transition ~150ms
```

### Settings Panel Content Change
```
BEFORE: Paint selected
┌─────────────────────────────┐
│ [ ● Paint ] [ • Erase ]     │
├─────────────────────────────┤
│ SETTINGS: Paint             │
│ Falloff Type: (●) Linear    │
│ Size: 45                    │
│ Hardness: 0.75              │
│ Strength: 1.0               │
└─────────────────────────────┘

AFTER: Erase icon clicked
┌─────────────────────────────┐
│ [ • Paint ] [ ● Erase ]     │
├─────────────────────────────┤
│ SETTINGS: Erase             │  ← Content changes
│ Falloff Type: (●) Linear    │
│ Size: 60                    │
│ Hardness: 0.50              │
│ Strength: 0.8               │
└─────────────────────────────┘
```

### Icon States
```
Selected (Active tool):
┌───────────────────┐
│ ● Paint (green)   │  ← Green border/background, bright text
├───────────────────┤
│ Settings below... │
└───────────────────┘

Inactive (Available tool):
┌───────────────────┐
│ • Erase (gray)    │  ← Gray text, no highlight
└───────────────────┘

Disabled (Not available):
┌───────────────────┐
│ ◌ Future (dark)   │  ← Very dim, unclickable
└───────────────────┘
```

### Layer Tab Switch
```
Click on VECFD layer tab:
1. Layer tabs update (BKGD loses highlight, VECFD highlights green)
2. Tool ribbon changes to show VECFD tools
3. Settings panel updates to show VECFD's selected tool settings
4. Smooth transition ~150ms

BEFORE: BKGD selected
┌─────────────────────────────┐
│ [ ● BKGD ] [ VECFD ] [ BOTFD]
│ [eye] [──────] [clear]
│ [ ● Color ] [ • Image ] ...
│ Settings: Color...
└─────────────────────────────┘

AFTER: VECFD clicked
┌─────────────────────────────┐
│ [ BKGD ] [ ● VECFD ] [ BOTFD]
│ [eye] [──────] [reset]
│ [ ● Paint ] [ • Erase ] ...
│ Settings: Paint...
└─────────────────────────────┘
```
```
Tool-specific labeled value:
 Size: 45
 ◀─────●──────────────────────▶
 5                           100

Layer transparency has no label or visible percentage:
 ◀─────●──────────────────────▶
```

### Control Types Used in Settings

#### Layer Transparency Slider (General Controls Row)
```
Eye open/closed [────────] [contextual action]

Independent per layer:
- Each layer keeps its own transparency value
- Changing tools does not reset the layer value
- The visibility toggle hides/shows the layer
- Transparency adjusts the opacity of the visible layer
```

#### Tool Icon Buttons (Top of Settings Panel)
```
Compact tool ribbon, shown below the general layer controls:
[ ● Paint ] [ • Erase ] [ • Field Actions ]

Each icon is:
- 32-44px square (small for compact display)
- Green highlight when tool selected (● = selected)
- Gray when tool inactive (• = inactive)
- Click to swap settings above
```

#### Toggle/Radio Button Groups (Within Settings)
```
Falloff Type:
(●) Linear   ○ Gaussian

Alternative (button-style):
Falloff Type:
┌──────────┬──────────┬──────────┐
│ Linear↖  │ Gaussian │Hard Edge │
└──────────┴──────────┴──────────┘
(Selected button highlighted in green)
```

---

## Color Scheme

| Element | Color | RGB |
|---------|-------|-----|
| Background | Dark Gray | #2E2E2E |
| Panel Bg | Dark Gray | #2E2E2E |
| Text Primary | Off-white | #E8E8E8 |
| Text Secondary | Light Gray | #B0B0B0 |
| Accent (Active) | Neon Green | #00FF00 |
| Accent (Hover) | Cyan | #00FFD1 |
| Border (Subtle) | Medium Gray | #404040 |
| Divider | Medium Gray | #505050 |

---

## Responsive Behavior

### Wide Panel (230px - Default)
- Full tool names visible
- Icons + text on most buttons
- All sections visible in scroll area
- Comfortable spacing

### Narrow Panel (200px - Minimum)
- Truncate long tool names with "..."
- Icons only for less critical buttons
- Same layout, tighter spacing
- All functionality preserved

### Collapsed Section (Example: Paint tool)
- Shows only tool button (arrow + icon + name)
- Settings hidden to save space
- Smooth expand/collapse animation

---

## Animation & Behavior

### Visibility Toggle Click (General Layer Controls)
```
User clicks the eye icon in the layer controls row:
1. Icon changes: open eye → closed eye and its color changes (instant)
2. Layer visibility on canvas toggles (instant)
3. NO settings panel update
4. NO tool selection change
Total: Instant (no animation needed)

Example:
Eye open [──────] [contextual action]  ← Click eye
↓
Eye closed [──────] [contextual action]  ← Same tool selected, layer just hidden
```

### Transparency Slider (General Layer Controls)
```
User drags transparency slider beside the visibility toggle:
- Real-time opacity change on canvas
- Works independently of visibility toggle
- Each layer keeps its own transparency value
```

### Tool Icon Click
```
User clicks Paint icon:
1. Paint icon highlights: gray → green background (100ms)
2. Previous tool icon dehighlights: green → gray (100ms)
3. Settings header text fades: "Erase" → "Paint" (50ms)
4. Settings controls fade and swap content (100ms)
Total: ~250ms smooth transition
```

### Layer Tab Switch
```
User clicks VECFD layer tab:
1. Layer tab highlights: gray → green border/text (100ms)
2. Previous layer tab dehighlights (100ms)
3. Tool ribbon fades out (50ms)
4. New tool ribbon fades in (50ms)
5. Settings panel resets to first tool's settings (100ms)
Total: ~300ms smooth transition

NOTE: Scroll position within settings resets when switching layers
```

### Slider Feedback (Same as before - no change)
```
Dragging slider:
- Real-time value display in label
- No "Apply" button needed
- Immediate visual feedback on canvas
```

### Dynamic Settings Visibility

---

## Icons Used (Unicode/Font Awesome style)

| Tool | Icon | Alternatives |
|------|------|--------------|
| Visibility | Open/closed eye | 👁️ |
| Color Picker | ● | 🎨 ⬜ |
| Image/Background | ⬜ | 🖼️ 📷 |
| Paint | 🖌️ | ✏️ 🖍️ |
| Erase | 🗑️ | ✕ ⌫ |
| Field Actions | Whole-field operations | Randomize, Normalize |
| General layer actions | Context-specific clear/reset | Clear background, reset field, clear bots, clear traces |
| Place/Position | 📍 | 📌 push-pin |
| Auto | ▶ | ▶️ ⏵ ► |
| Clear/Delete | ✕ | ✖️ 🗑️ |
| Export | 📤 | 💾 ⬆️ |

---

## Layout Dimensions

### Panel Width
- Default: 230px
- Minimum: 200px
- Maximum: 300px (with nested scrollbars)

### Control Heights
- Layer header: 36px
- Tool button: 44px (WCAG touch target)
- Section header: 28px
- Slider with label: 28px
- Toggle group: 40px
- Nested settings container: auto (variable)

### Spacing
- Section padding: 12px
- Between controls: 8px
- Between tool groups: 16px
- Between sections: 12px

---

## Scrolling Behavior

```
Panel Structure (top to bottom):
┌─────────────────────────────┐
│ Layer Tabs [FIXED]          │  ← Non-scrolling (always visible)
├─────────────────────────────┤
│ Layer Controls [FIXED]      │  ← Visibility + transparency row
├─────────────────────────────┤
│ Tool Icon Ribbon [FIXED]    │  ← Tool selection only
├─────────────────────────────┤
│ Settings Panel [SCROLLABLE] │  ← Only this area scrolls
│ (Tool-specific controls)    │     when content > panel height
│                             │
│                             │
└─────────────────────────────┘

Scroll behavior:
- Vertical scroll only
- Horizontal scrolling disabled
- Smooth scrolling with mouse wheel
- Scroll position resets when switching tools
- Scroll position persists within same tool (nice UX)
```

---

## Accessibility Features

- **Tab Navigation**: Full keyboard navigation through all controls
- **Focus Indicators**: Green highlight (#00FF00) around focused element
- **Semantic Labels**: All controls have associated labels
- **Minimum Touch Target**: 44px height for all interactive elements
- **Color Contrast**: Text passes WCAG AA (4.5:1 ratio)
- **Keyboard Shortcuts**: Future - shortcuts for common tools

