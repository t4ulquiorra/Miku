# Miku — Adaptive Crate Spec

Reference prototype: `crate_adaptive.html` (open in a browser, use the size buttons and the W/H boxes, tick STATUS BAR).
**The prototype is the truth.** If this document and the prototype disagree, the prototype wins. Copy numbers from it; do not invent values.
Only the UI changes. Every NextPlayer feature stays (playback, folders, playlists, network, settings, search, select mode...).

## 0. Rules for the agent
- No hardcoded screen sizes or device names. Everything derives from the available width/height (`BoxWithConstraints` / window size).
- Real data only: real folders, real video counts/durations, real thumbnails, real last-played video. No placeholder strings in the app.
- Do not report "done" without the user's screenshot confirming. Do not claim things you did not verify.
- Work one phase at a time. Stop after each phase.
- Do not touch navigation behaviour, only its look (phase 6).

## 1. Design tokens
bg #0D0C0A · surface #17150F · key #1B1812 · borders #26221A / #2E2819 / #3A3220 · ink #ECE4D2 · ink2 #B9B09B · muted #8F8774 · dim #6F6858 · paper-ink #1A150C · stamp #C23A12 · accent #FF5A2E · accent shadow #7A2A12
Paper colours by folder index % 4: #D9CBA6, #CDBE96, #E2D6B8, #C4B48A. The folder at index 1 ("last played") has an accent-coloured tab.
Fonts: Bricolage Grotesque (titles/UI), JetBrains Mono (labels, numbers, paths). Both already in the app.
The "key" button: accent fill, ink #0D0C0A bold text, hard 5px bottom edge in #7A2A12 (no blur).

## 2. One folder card (the unit everything is built from)
Card = paper rectangle + one tab sticking out of the top edge.
- Wide card (width >= 640dp): reference 820x410. Radii: top-left 6, top-right 20, bottom 16/16. Tab 248x46 (max), tab radius 0.26*tabH on top corners.
- Narrow card (width < 640dp): reference 344x221 (shape 1.56). Radii 4/12/10/10. Tab 156x31 (max).
- Shape ratio: wide 2.0, narrow 1.56. Cards may shrink uniformly, never stretch.
- Card shadow: upward `0 -12 28 rgba(0,0,0,.4)` plus a 2dp inner white highlight on the top edge (the "seam"). The tab has its own shadow so it lifts off the card behind it, plus a subtle top-to-bottom darkening gradient.
- Tab text: index (mono, 78% size, 70% opacity), folder name (bold, ellipsis), video count (mono). Tab font = 14.5 wide / 12.5 narrow, scaled with tab height.
- Tab slots (left offset as fraction of card width), cycling by index: wide `[30,185,340,495]/820` (4 slots), narrow `[16,84,152]/344` (3 slots). Keep the tab inside the card: left <= W - tabW - 3.5% (wide) / 4% (narrow). This is why tab names never hide each other.
- Face content, top to bottom: label "FOLDER 01 / 18" (mono, 62% opacity), optional rotated "N NEW" stamp (2dp stamp border, -6deg), big title (bold, one line, ellipsis), optional video list, then a bottom block: "N VIDEOS · hh:mm:ss" + path (mono), waveform bars (only if folder has >= 3 videos; accent-coloured for new videos), up to 3 stacked thumbnails (tilted -5 / 0 / +4.5 deg, with a play dot and "videoN · mm:ss"), and the "Open folder →" key.
- Content adapts to card height class: roomy (>=440): also shows a 9-line video list; normal (>=200); short (>=150): no thumbnails, no bars, key on the same row as the meta; tiny (<150): no path, no stamp, no bars, no label shrink only. In narrow cards the bottom block is a 2-column grid (meta | bars / thumbs | key). In wide cards: meta | thumbs / bars | key.
- Sizes inside the card are proportional to the card (container units), not fixed dp.

## 3. The stack (crate)
Cards are stacked front (index = pos) to back. Front card sits at the bottom of the available area, others rise behind it.
For a card at distance d = index - pos (d >= 0, behind the front):
- translateY = -d * rise · translateZ = -d * Z · rotateX = 5deg · transform-origin top centre
- rise = max(cardH * lerp(.245, .117, t), 22dp), t = clamp((shape-1.56)/(2-1.56),0,1) (narrow cards rise more per step, wide less)
- Perspective P = 2 * cardW. Z = 0.1 * cardW. Perspective origin y = frontTop + oy, where oy = -lerp(.9,.26,t) * cardH
- Organic offsets (so the stack looks hand-placed): x offset ORG_X[i%18] * (cardW/344) * ease, z-rotation ORG_R[i%18] * ease, ease = min(1, d*1.1).
  ORG_X = [-18,17,-24,21,-11,24,-20,14,-22,19,-14,23,-17,12,-21,16,-10,18]; ORG_R(deg) = [-2.1,1.9,-2.7,2.4,-1.3,2.8,-2.3,1.6,-2.5,2.1,-1.7,2.6,-2,1.4,-2.4,1.8,-1.2,2]
- Depth darkening (NOT transparency — transparent cards let names of folders behind show through): brightness = max(.5, 1 - d*.08); beyond d > lim = max(2, n-2.5) multiply by max(.2, 1-(d-lim)*.32). Cards stay opaque. Only the card just past the last slot fades out in opacity: clamp((n+.5-d)/.5, 0, 1).
- Cards passed (d < 0, flipping away): translateY = -d * cardH*lerp(1.36,.78,t), translateZ = -d*Z*1.9, rotateX = d*30deg, opacity = max(0, 1 + d*1.05).
- z-order: behind cards 500 - round(d*10); passed cards on top.
- Projection: CSS perspective means scale = P / (P + d*Z) and positions converge towards the perspective origin. In Compose reproduce with graphicsLayer (rotationX, scaleX/scaleY = that scale, translationY computed from the same formula, cameraDistance ~ P * density). Verify against the prototype, not by eye.

## 4. How many folders show, and how big the card is (the solver)
Input: available width aw and height ah (screen minus rail/top bar/bottom bar/status inset/video pane). No fixed min or max folder count: as many as the height fits (all folders if there is room).
- wideScr = aw/ah >= 1.3 → shape A0 = 2.0, cap width 820; else A0 = 1.56, cap 560. margin m = clamp(aw*.03, 10, 18).
- base width = (A0 < 1.7) ? min(aw*.835, cap) : min(aw - 2m, cap). Card W = base * scale, H = W / shape.
- fit(shape, scale): n = largest k (<= folders-1) such that the perspective-squeezed extent from the farthest tab down to the front card bottom fits in ah - 2m.
- Strategy: (1) try scale 1 down to 0.75 requiring >= 4 behind; (2) else scale 1 requiring >= 2; (3) else try shapes [A0, A0-.2, A0-.4] and scales from 1 down to .62 requiring 5, 4, 3, 2 behind in that order (prefer more behind while the card stays >= 62% of approved size); (4) then the same down to scale .4; (5) fallback scale .45 with >= 2.
- The whole group (farthest tab ... front card bottom) is centred vertically in the available area; the card is centred horizontally in the available area (NOT the whole screen).
- Tab height wide = min(46, max(30, H*.112)); narrow = min(34, max(26, W*.09)).
- A thin "ruler" of ticks sits at the right edge of the crate area (one tick per folder, active one orange and longer). Dragging it jumps through folders. It is what tells the user there are many folders.

## 5. Interaction
- Vertical drag moves through folders: one folder per max(40, rise*2.8) px of drag. Release = fling (project velocity * 0.24) then snap to the nearest folder.
- Spring: vel = (vel + (target - pos) * .13) * .74 each frame. Redraw only while moving.
- Tap on a tab jumps to that folder. Tap on the front card's "Open folder" opens it (existing NextPlayer navigation).
- Tap-to-fan-out and select mode: keep the existing NextPlayer behaviour, restyled later.

## 6. Chrome around the crate (measured from the tablet; dp)
- Status bar: 26 (system inset, only when visible). Gesture inset: 17 (12 when height < 520).
- Top bar: 71 high (50 when screen height < 520). Left: "Miku" bold 24 (19 compact) with a 7dp accent dot, and under it a mono line "LIBRARY · N FOLDERS". Right: resume ticket, search key, crate/list switch.
- Resume ticket: paper colour, notched left/right edges, dashed tear line after the play disc, stamp-colour play disc (30dp), mono "RESUME · mm:ss", bold title one line, 2dp progress line. Rotated -1.2deg with a hard 3dp shadow. Collapses to disc + time when the top bar is narrower than 620dp, to disc only below 340. Tapping resumes the last played video (real data).
- Key buttons (search, settings): 42 square (34 compact), radius 13, fill #1B1812, border #2E2819, hard 3dp bottom edge #070605, line icons 21dp in #D9CBA6, stroke 1.8, round caps.
- Crate/list switch: two-button segment (crate icon | list icon), selected = paper colour with ink icon. Switches between crate view and the existing list view.
- Nav rail (the app's own rule: screenWidthDp >= 600 AND landscape): 106 wide (72 when height < 520; 58 when width < 720 and height < 520). "NP" mark at top, four tabs, settings key at the bottom. Tabs are vertical text with a small number above; the active tab is a paper-coloured index tab that protrudes 16dp (10 compact) over the content edge, radius 0 14 14 0, hard edge shadow. Inactive tabs dark with muted text. Tab height <= 112 and shrinks to fit.
- Bottom bar (everything else): 71 high, same four items. The active item is a raised paper tab (radius 12 12 0 0, taller than the others).
- Content panel: the area right of the rail and below the top bar, colour #0C0B09 with a soft radial lighten in the middle and very faint horizontal ruled lines (2% opacity, every 30dp), top-left radius 28.
- No floating play button anywhere.
- Switch rail ↔ bottom bar: keep the app's existing rule (width >= 600dp and landscape). The prototype now uses the same rule.

## 7. Video pane (short, wide screens only: phone landscape)
When the screen height < 520 and the width left after the rail is >= 560: split the content area. Crate on the left (the solver gets aw - paneW), video pane on the right.
- paneW = clamp(aw*.36, 236, 330); when aw < 640: clamp(aw*.30, 170, 200).
- Pane: dark sheet #100E0A, left border #26221A, top-left radius 18. Header: folder name (bold 15) and mono "N VIDEOS · duration · N NEW".
- Rows: 58x33 thumbnail (real thumbnail), title, mono "duration · resolution", orange "NEW" tag on new videos, dashed divider. Scrollable list of all videos in the front folder (lazy). Tapping a row plays that video directly.
- The list follows the front folder as the user flips the crate; scroll resets to top.
- Ruler sits at the right edge of the crate area, left of the pane.
- Tablets and portrait do not show the pane.

## 8. What stays vs what changes in the app
Stays: all NextPlayer features and data, navigation destinations and behaviour, theme colours/fonts already approved, player, folder screen, playlists, network, settings logic.
Changes (look only): top bar, nav rail / bottom bar look, home screen crate (this spec), resume entry point (ticket instead of FAB), view switch (crate/list), video pane on phone landscape.
Not yet designed (do not invent; ask): folder view screen, player UI, playlists/network/more screens, Quick Settings sheet, select mode visuals, empty states, loading states.

## 9. Test sizes (dp) the result must match the prototype at
360x640, 412x892, 892x412, 640x360, 884x740 (fold), 640x800 (split), 800x1280, 1280x800, 1024x768, 1600x1000 — with and without the status bar.
The info line above the prototype prints expected card size and folders-behind for each size; compare against those.
