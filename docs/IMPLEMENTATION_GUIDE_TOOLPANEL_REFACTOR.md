# ToolPanel Refactoring: Implementation Guide

## Overview
The current ToolPanel uses a 4-tab structure (BG, Field, Sketch, Bots).  
The active structure uses four levels:
1. **Layer tabs** (BKGD, VECFD, BOTFD)
2. **Layer controls** (open/closed eye, unlabeled transparency slider, contextual clear/reset icons)
3. **Tool icon ribbon** (tools available for the active layer)
4. **Dynamic settings panel** (updates per tool selection)

## Architecture Changes

### Current Structure
```
JTabbedPane (4 tabs)
├─ BG tab
│  └─ Mixed controls (toggle, transparency, buttons...)
├─ Field tab
│  └─ Mixed controls
├─ Sketch tab
│  └─ Mixed controls
└─ Bots tab
   └─ Mixed controls
```

### New Structure
```
BorderLayout
├─ North: Layer Tabs Panel (fixed height)
│  └─ JPanel with 3 layer buttons (BKGD, VECFD, BOTFD)
│
├─ North: Layer Controls Row (fixed height)
│  └─ Open/closed eye + unlabeled layer transparency slider + contextual actions
│
├─ North: Tool Ribbon Panel (fixed height)
│  └─ JPanel with tool-selection icons only
│
└─ Center: Settings Scroll Pane (scrollable)
  └─ Dynamic settings panel (content updates per selected tool)
```

## Implementation Phases

### Phase 1: Core Architecture (Current)
- [ ] Create new BorderLayout hierarchy
- [ ] Create layer tab selector panel
- [ ] Create tool ribbon panel structure
- [ ] Create dynamic settings panel
- [ ] Add layer switching logic
- [ ] Add tool switching logic

**File**: `src/main/java/ui/ToolPanel.java`  
**Complexity**: HIGH (full rewrite vs. patching)

### Phase 2: Background Layer (BKGD)
- [ ] Implement Color tool
- [ ] Implement Image tool  
- [x] Add clear-background action to the general layer controls
- [ ] Wire to canvas rendering

### Phase 3: Vector Field Layer (VECFD)
- [ ] Implement Paint tool (falloff, size, hardness, strength)
- [ ] Implement Erase tool
- [x] Group Normalize and Randomize in the Field Actions tool
- [x] Add reset-vector-field action to the general layer controls

### Phase 4: Bot Field Layer (BOTFD)
- [ ] Implement Place tool (with nested settings)
- [ ] Implement Random tool
- [ ] Implement Auto tool
- [x] Add separate clear-bots and clear-traces actions to the general layer controls
- [ ] Implement Export tool

### Phase 5: Polish & Testing
- [ ] Test tool transitions
- [ ] Test visibility independence
- [ ] Test transparency per-tool
- [ ] Keyboard nav support
- [ ] Responsive sizing

## Key Design Patterns

### Tool Definition
Each tool should be a discrete class/structure:
```java
class ToolDefinition {
  String id;              // "paint", "erase", etc.
  Icon icon;              // Visual representation
  String name;            // Display name
  JPanel settingsPanel;   // Dynamic settings
  ToolListener callback;  // Listener for changes
}
```

### Layer Definition
Each layer should manage its tools:
```java
class LayerDefinition {
  String id;              // "bkgd", "vecfd", "botfd"
  String name;            // Display name
  boolean visible;        // Visibility state
  ToolDefinition[] tools; // Available tools
  int selectedToolIndex;  // Current tool
}
```

### Settings Panel Strategy
- Create settings lazily (only when tool is selected)
- Cache settings panels (don't recreate on every switch)
- Preserve slider values across tool switches

## Implementation Strategy

### Option A: Full Rewrite (Recommended for this branch)
- Pros: Clean architecture, easier to maintain
- Cons: Requires careful migration of all controls
- Time: 4-6 hours

### Option B: Gradual Migration
- Phase out old tabs one layer at a time
- Keep both systems temporarily
- Migrate listeners incrementally
- Time: 8-10 hours but less risky

### Option C: Adapter Pattern
- Keep old ToolPanel as-is
- Create new panel alongside
- Switch at render loop
- Time: 5-7 hours, but maintains compatibility

## Migration Checklist

### Listener Methods to Preserve
All methods in `ToolListener` interface must continue to work:
- Background: onBackgroundToggle, onBackgroundTransparencyChanged, etc.
- Vector Field: onVectorFieldToggle, onResetVectorField, etc.
- Botfield: onBotfieldToggle, onBotLifeChanged, etc.

### UI Components to Replace
- JTabbedPane → Layer selector buttons
- Individual control groups → Dynamic settings panels
- Static layout → Dynamic content swapper

### Test Points
1. Layer switching works
2. Tool switching works
3. Visibility toggle independent of tool
4. Transparency values persist per tool
5. All listener callbacks fire correctly
6. Panel resize handles narrow widths
7. Keyboard shortcuts still work

## Git Workflow
- Branch: `feature/sidepanel-ux-exploration`
- Commit after each phase
- Test after each phase
- Create PR when Phase 1 complete

## Resources
- MockUI design: `docs/MOCK_UI_LAYOUT.md`
- GUI spec: `gui_menu_design.txt`
- Current ToolPanel: `src/main/java/ui/ToolPanel.java` (~2500 lines)

## Risk Assessment

### High Risk
- Breaking existing listener contracts
- Losing slider value state
- Affecting render performance

### Medium Risk
- Keyboard navigation
- Responsive sizing
- Custom UI styling

### Low Risk
- Icon display
- Color scheme
- Basic button clicks

## Success Criteria

Phase 1 Complete:
- [ ] Layer tabs switch correctly
- [ ] Tool ribbon updates per layer
- [ ] Settings panel updates per tool
- [ ] Visibility toggle works
- [ ] All listeners still callable

Phase 2-4 Complete:
- [ ] All tools implemented and wired
- [ ] All settings properly connected
- [ ] Canvas responds to changes

Phase 5 Complete:
- [ ] Keyboard nav works
- [ ] Panel resizes gracefully
- [ ] No performance regression
- [ ] Ready for user testing
