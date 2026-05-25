from PIL import Image
import os

DST = r"D:/Assignment 2/Game-test/assets/sprites"
FRAME = 64
PIX = 3  # chunky pixel size, each logical px = 3x3 actual px

# ── Color palette ──
# Tier 1 colors
HAT1 = (128, 64, 192, 255)
HAT_D1 = (72, 32, 120, 255)
HAT_L1 = (168, 104, 216, 255)
ROBE1 = (120, 56, 184, 255)
ROBE_D1 = (64, 24, 112, 255)
ROBE_L1 = (176, 104, 220, 255)
SKIN = (255, 215, 175, 255)
SKIN_D = (200, 160, 120, 255)
STAFF = (130, 90, 50, 255)
STAFF_D = (90, 60, 30, 255)
ORB1 = (120, 200, 255, 255)
ORB_L1 = (180, 230, 255, 255)
EYE = (255, 255, 255, 255)
PUPIL = (40, 20, 60, 255)
OUTLINE = (30, 12, 60, 255)

# Tier 2: brighter robe, staff orb glows
HAT2 = (152, 88, 208, 255)
HAT_D2 = (96, 48, 144, 255)
HAT_L2 = (192, 128, 232, 255)
ROBE2 = (144, 80, 200, 255)
ROBE_D2 = (88, 40, 136, 255)
ROBE_L2 = (200, 128, 236, 255)
ORB2 = (80, 220, 255, 255)
ORB_L2 = (160, 240, 255, 255)
TRIM2 = (200, 180, 100, 255)  # gold trim

# Tier 3: golden decorations, bright glow
HAT3 = (168, 104, 220, 255)
HAT_D3 = (112, 56, 160, 255)
HAT_L3 = (208, 144, 240, 255)
ROBE3 = (160, 96, 216, 255)
ROBE_D3 = (104, 48, 152, 255)
ROBE_L3 = (216, 144, 244, 255)
ORB3 = (60, 240, 255, 255)
ORB_L3 = (140, 255, 255, 255)
TRIM3 = (255, 210, 60, 255)  # brighter gold
AURA3 = (255, 220, 80, 40)   # subtle golden aura


def canvas():
    return Image.new("RGBA", (FRAME, FRAME), (0, 0, 0, 0))


def put(img, ox, oy, pixels, scale=PIX):
    """Place pixel blocks at logical coordinates offset by (ox, oy)."""
    px = img.load()
    for dx, dy, color in pixels:
        for sy in range(scale):
            for sx in range(scale):
                x, y = ox + dx * scale + sx, oy + dy * scale + sy
                if 0 <= x < FRAME and 0 <= y < FRAME:
                    px[x, y] = color


def make_sheet(frames):
    """Horizontally concatenate frames into a sprite strip."""
    out = Image.new("RGBA", (FRAME * len(frames), FRAME), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        out.paste(f, (i * FRAME, 0), f)
    return out


# ═══════════════════════════════════════════════════════════════
#  Mage body part builders
#  Each returns list of (lx, ly, color) tuples in logical coords
#  (lx, ly) range roughly 0..14 wide, 0..19 tall, centered later
# ═══════════════════════════════════════════════════════════════

def hat_pixels(tier, bob=0):
    """Pointed wizard hat. bob = vertical offset for idle breathing."""
    if tier == 1: H, HD, HL = HAT1, HAT_D1, HAT_L1
    elif tier == 2: H, HD, HL = HAT2, HAT_D2, HAT_L2
    else: H, HD, HL = HAT3, HAT_D3, HAT_L3
    by = bob
    return [
        # tip
        (5, by+0, HD), (6, by+0, HD),
        (4, by+1, HD), (5, by+1, HL), (6, by+1, HL), (7, by+1, HD),
        # upper hat
        (3, by+2, HD), (4, by+2, H), (5, by+2, H), (6, by+2, H), (7, by+2, H), (8, by+2, HD),
        (2, by+3, HD), (3, by+3, H), (4, by+3, H), (5, by+3, HL), (6, by+3, H), (7, by+3, H), (8, by+3, H), (9, by+3, HD),
        (2, by+4, HD), (3, by+4, H), (4, by+4, H), (5, by+4, H), (6, by+4, H), (7, by+4, H), (8, by+4, H), (9, by+4, HD),
        # brim
        (1, by+5, HD), (2, by+5, HL), (3, by+5, HL), (4, by+5, H), (5, by+5, H), (6, by+5, H), (7, by+5, HL), (8, by+5, HL), (9, by+5, HL), (10, by+5, HD),
        (1, by+6, HD), (2, by+6, HD), (3, by+6, HD), (4, by+6, H), (5, by+6, H), (6, by+6, H), (7, by+6, HD), (8, by+6, HD), (9, by+6, HD), (10, by+6, HD),
    ]


def face_pixels(bob=0):
    """Simple face with eyes. bob = vertical offset."""
    by = bob
    return [
        # face background (skin)
        (4, by+0, SKIN), (5, by+0, SKIN), (6, by+0, SKIN), (7, by+0, SKIN),
        (3, by+1, SKIN_D), (4, by+1, SKIN), (5, by+1, SKIN), (6, by+1, SKIN), (7, by+1, SKIN), (8, by+1, SKIN_D),
        # eyes
        (4, by+1, EYE), (6, by+1, EYE),
        (4, by+1, PUPIL), (6, by+1, PUPIL),  # small pupils
    ]


def robe_body_pixels(tier, bob=0):
    """Robe/torso. bob = vertical offset."""
    if tier == 1: R, RD, RL = ROBE1, ROBE_D1, ROBE_L1
    elif tier == 2: R, RD, RL = ROBE2, ROBE_D2, ROBE_L2
    else: R, RD, RL = ROBE3, ROBE_D3, ROBE_L3
    by = bob
    return [
        # shoulders
        (2, by+0, RD), (3, by+0, R), (4, by+0, R), (5, by+0, R), (6, by+0, R), (7, by+0, R), (8, by+0, R), (9, by+0, RD),
        (1, by+1, RD), (2, by+1, R), (3, by+1, RL), (4, by+1, R), (5, by+1, R), (6, by+1, R), (7, by+1, RL), (8, by+1, R), (9, by+1, R), (10, by+1, RD),
        # upper body
        (1, by+2, RD), (2, by+2, R), (3, by+2, R), (4, by+2, R), (5, by+2, R), (6, by+2, R), (7, by+2, R), (8, by+2, R), (9, by+2, R), (10, by+2, RD),
        (2, by+3, RD), (3, by+3, R), (4, by+3, R), (5, by+3, R), (6, by+3, R), (7, by+3, R), (8, by+3, R), (9, by+3, RD),
        # mid body - gap for staff on right side
        (2, by+4, RD), (3, by+4, R), (4, by+4, R), (5, by+4, R), (6, by+4, R), (7, by+4, R), (8, by+4, R), (9, by+4, RD),
        (2, by+5, RD), (3, by+5, R), (4, by+5, R), (5, by+5, R), (6, by+5, R), (7, by+5, R), (8, by+5, RD),
        (2, by+6, RD), (3, by+6, R), (4, by+6, R), (5, by+6, R), (6, by+6, R), (7, by+6, R), (8, by+6, RD),
        # lower robe (wider)
        (1, by+7, RD), (2, by+7, R), (3, by+7, R), (4, by+7, R), (5, by+7, R), (6, by+7, R), (7, by+7, R), (8, by+7, R), (9, by+7, R), (10, by+7, RD),
        (1, by+8, RD), (2, by+8, R), (3, by+8, R), (4, by+8, R), (5, by+8, R), (6, by+8, R), (7, by+8, R), (8, by+8, R), (9, by+8, R), (10, by+8, RD),
        # robe bottom
        (2, by+9, RD), (3, by+9, R), (4, by+9, R), (5, by+9, R), (6, by+9, R), (7, by+9, R), (8, by+9, R), (9, by+9, RD),
    ]


def staff_pixels(tier, bob=0):
    """Staff held on right side of body."""
    if tier == 1: O, OL = ORB1, ORB_L1
    elif tier == 2: O, OL = ORB2, ORB_L2
    else: O, OL = ORB3, ORB_L3
    by = bob
    return [
        # orb at top
        (9, by+0, OL), (10, by+0, OL), (9, by+1, O), (10, by+1, O),
        # staff shaft (between orb and ground)
        (9, by+2, STAFF_D), (10, by+2, STAFF),
        (9, by+3, STAFF_D), (10, by+3, STAFF),
        (9, by+4, STAFF_D), (10, by+4, STAFF),
        (9, by+5, STAFF_D), (10, by+5, STAFF),
        (9, by+6, STAFF_D), (10, by+6, STAFF),
        (9, by+7, STAFF_D), (10, by+7, STAFF),
        (9, by+8, STAFF_D), (10, by+8, STAFF),
        (9, by+9, STAFF_D), (10, by+9, STAFF),
        # staff base
        (8, by+10, STAFF_D), (9, by+10, STAFF), (10, by+10, STAFF), (11, by+10, STAFF_D),
    ]


def feet_pixels(bob=0):
    """Small feet/robe bottom."""
    by = bob
    return [
        (3, by+0, ROBE_D1), (4, by+0, ROBE1), (5, by+0, ROBE1), (6, by+0, ROBE1), (7, by+0, ROBE1), (8, by+0, ROBE_D1),
        (3, by+1, ROBE_D1), (5, by+1, SKIN_D), (6, by+1, SKIN_D), (8, by+1, ROBE_D1),  # feet
    ]


def tier_trim_pixels(tier):
    """Gold trim for tier 2 and 3."""
    if tier == 2:
        return [
            (1, 10, TRIM2), (10, 10, TRIM2),
            (2, 15, TRIM2), (9, 15, TRIM2),
        ]
    elif tier == 3:
        return [
            (1, 10, TRIM3), (10, 10, TRIM3),
            (2, 15, TRIM3), (9, 15, TRIM3),
            (5, 1, TRIM3), (6, 1, TRIM3),  # hat trim
        ]
    return []


# ═══════════════════════════════════════════════════════════════
#  Full character assembler
#  Assembles all body parts with proper offsets for a frame
# ═══════════════════════════════════════════════════════════════

def build_character(tier, bob=0, sway=0, squash=0, alpha_pixels=None):
    """Build full character as list of (lx, ly, color) logical pixels.

    The character occupies roughly 12w x 20h logical pixels.
    Logical origin (0,0) is top-left of the character bounding box.
    Will be centered in the 64x64 frame by the caller.

    bob: vertical bounce (idle breathing, walk bounce)
    sway: horizontal sway (walk, attack)
    squash: vertical compression for squash-and-stretch
    alpha_pixels: set of (lx, ly) positions to make semi-transparent (death fade)
    """
    pixels = []

    # Hat region (rows 0-6)
    pixels.extend(hat_pixels(tier, bob))

    # Face region (rows 7-8)
    pixels.extend(face_pixels(7 + bob))

    # Robe body (rows 9-18)
    pixels.extend(robe_body_pixels(tier, 9 + bob))

    # Staff (rows 7-19, positioned relative to body)
    pixels.extend(staff_pixels(tier, 7 + bob))

    # Feet (row 19-20)
    pixels.extend(feet_pixels(19 + bob))

    # Tier trim
    pixels.extend(tier_trim_pixels(tier))

    # Apply horizontal sway
    if sway != 0:
        pixels = [(lx + sway, ly, c) for lx, ly, c in pixels]

    # Apply squash
    if squash != 0:
        mid_y = 10
        pixels = [(lx, mid_y + int((ly - mid_y) * (1.0 + squash)), c) for lx, ly, c in pixels]

    # Apply alpha to dying pixels
    if alpha_pixels:
        result = []
        alpha_set = set(alpha_pixels)
        for lx, ly, c in pixels:
            if (lx, ly) in alpha_set:
                r, g, b, a = c
                result.append((lx, ly, (r, g, b, max(0, a // 3))))
            else:
                result.append((lx, ly, c))
        return result

    return pixels


def frame_to_img(pixels):
    """Convert logical pixels to a 64x64 PIL Image, centered."""
    img = canvas()
    px = img.load()
    for lx, ly, color in pixels:
        # Center in 64x64 frame
        # Character is ~12 wide, center at lx=5
        # Character is ~21 tall, center offset
        ox = 10 + lx  # logical origin offset
        oy = 2 + ly
        for sy in range(PIX):
            for sx in range(PIX):
                x, y = ox * PIX + sx, oy * PIX + sy
                if 0 <= x < FRAME and 0 <= y < FRAME:
                    r, g, b = color[0], color[1], color[2]
                    a = color[3] if len(color) > 3 else 255
                    if a > 0:
                        px[x, y] = (r, g, b, a)
    return img


# ═══════════════════════════════════════════════════════════════
#  Multi-direction frame builders
#  DOWN=0, LEFT=1, RIGHT=2, UP=3
# ═══════════════════════════════════════════════════════════════

def mirror_pixels(pixels):
    """Flip pixels horizontally for facing LEFT vs RIGHT."""
    # Find center of character (~lx=5..6)
    center_x = 5.5
    return [((int(2 * center_x - lx)), ly, c) for lx, ly, c in pixels]


def rear_pixels(tier, bob=0):
    """Character facing UP (back view) - no face, back of robe and hat."""
    if tier == 1: H, HD, HL = HAT1, HAT_D1, HAT_L1; R, RD, RL = ROBE1, ROBE_D1, ROBE_L1
    elif tier == 2: H, HD, HL = HAT2, HAT_D2, HAT_L2; R, RD, RL = ROBE2, ROBE_D2, ROBE_L2
    else: H, HD, HL = HAT3, HAT_D3, HAT_L3; R, RD, RL = ROBE3, ROBE_D3, ROBE_L3
    by = bob
    pixels = [
        # Hat (same as front)
        (5, by+0, HD), (6, by+0, HD),
        (4, by+1, HD), (5, by+1, HL), (6, by+1, HL), (7, by+1, HD),
        (3, by+2, HD), (4, by+2, H), (5, by+2, H), (6, by+2, H), (7, by+2, H), (8, by+2, HD),
        (2, by+3, HD), (3, by+3, H), (4, by+3, H), (5, by+3, HL), (6, by+3, H), (7, by+3, H), (8, by+3, H), (9, by+3, HD),
        (2, by+4, HD), (3, by+4, H), (4, by+4, H), (5, by+4, H), (6, by+4, H), (7, by+4, H), (8, by+4, H), (9, by+4, HD),
        (1, by+5, HD), (2, by+5, HL), (3, by+5, HL), (4, by+5, H), (5, by+5, H), (6, by+5, H), (7, by+5, HL), (8, by+5, HL), (9, by+5, HL), (10, by+5, HD),
        (1, by+6, HD), (2, by+6, HD), (3, by+6, HD), (4, by+6, H), (5, by+6, H), (6, by+6, H), (7, by+6, HD), (8, by+6, HD), (9, by+6, HD), (10, by+6, HD),
        # Back of robe (no face)
        (3, by+7, RD), (4, by+7, R), (5, by+7, R), (6, by+7, R), (7, by+7, R), (8, by+7, RD),
        (2, by+8, RD), (3, by+8, R), (4, by+8, RL), (5, by+8, R), (6, by+8, R), (7, by+8, RL), (8, by+8, R), (9, by+8, RD),
        (2, by+9, RD), (3, by+9, R), (4, by+9, R), (5, by+9, R), (6, by+9, R), (7, by+9, R), (8, by+9, R), (9, by+9, RD),
        (1, by+10, RD), (2, by+10, R), (3, by+10, RL), (4, by+10, R), (5, by+10, R), (6, by+10, R), (7, by+10, RL), (8, by+10, R), (9, by+10, R), (10, by+10, RD),
        (1, by+11, RD), (2, by+11, R), (3, by+11, R), (4, by+11, R), (5, by+11, R), (6, by+11, R), (7, by+11, R), (8, by+11, R), (9, by+11, R), (10, by+11, RD),
        (2, by+12, RD), (3, by+12, R), (4, by+12, R), (5, by+12, R), (6, by+12, R), (7, by+12, R), (8, by+12, R), (9, by+12, RD),
        (2, by+13, RD), (3, by+13, R), (4, by+13, R), (5, by+13, R), (6, by+13, R), (7, by+13, R), (8, by+13, RD),
        (2, by+14, RD), (3, by+14, R), (4, by+14, R), (5, by+14, R), (6, by+14, R), (7, by+14, R), (8, by+14, RD),
        (2, by+15, RD), (3, by+15, R), (4, by+15, R), (5, by+15, R), (6, by+15, R), (7, by+15, R), (8, by+15, R), (9, by+15, RD),
        (1, by+16, RD), (2, by+16, R), (3, by+16, R), (4, by+16, R), (5, by+16, R), (6, by+16, R), (7, by+16, R), (8, by+16, R), (9, by+16, R), (10, by+16, RD),
        # Staff (back view, staff behind robe on one side)
        (9, by+0, STAFF_D), (10, by+0, STAFF),
        (9, by+1, STAFF_D), (10, by+1, STAFF),
        (9, by+2, STAFF_D), (10, by+2, STAFF),
        (9, by+3, STAFF_D), (10, by+3, STAFF),
        (9, by+4, STAFF_D), (10, by+4, STAFF),
        (9, by+5, STAFF_D), (10, by+5, STAFF),
        (9, by+6, STAFF_D), (10, by+6, STAFF),
        (9, by+7, STAFF_D), (10, by+7, STAFF),
        (9, by+8, STAFF_D), (10, by+8, STAFF),
        (9, by+9, STAFF_D), (10, by+9, STAFF),
        (9, by+10, STAFF_D), (10, by+10, STAFF),
        (9, by+11, STAFF_D), (10, by+11, STAFF),
        (9, by+12, STAFF_D), (10, by+12, STAFF),
        (9, by+13, STAFF_D), (10, by+13, STAFF),
        (9, by+14, STAFF_D), (10, by+14, STAFF),
        (9, by+15, STAFF_D), (10, by+15, STAFF),
        (8, by+16, STAFF_D), (9, by+16, STAFF), (10, by+16, STAFF), (11, by+16, STAFF_D),
        # Feet
        (3, by+17, ROBE_D1), (4, by+17, ROBE1), (5, by+17, ROBE1), (6, by+17, ROBE1), (7, by+17, ROBE1), (8, by+17, ROBE_D1),
    ]
    return pixels


def side_pixels(tier, bob=0, facing_right=True):
    """Character facing LEFT or RIGHT (side profile)."""
    if tier == 1: H, HD, HL = HAT1, HAT_D1, HAT_L1; R, RD, RL = ROBE1, ROBE_D1, ROBE_L1
    elif tier == 2: H, HD, HL = HAT2, HAT_D2, HAT_L2; R, RD, RL = ROBE2, ROBE_D2, ROBE_L2
    else: H, HD, HL = HAT3, HAT_D3, HAT_L3; R, RD, RL = ROBE3, ROBE_D3, ROBE_L3
    if tier == 1: O, OL = ORB1, ORB_L1
    elif tier == 2: O, OL = ORB2, ORB_L2
    else: O, OL = ORB3, ORB_L3
    by = bob
    # Side view: narrower body, hat tilted, staff leading
    pixels = [
        # Hat (side profile - sloping forward if facing right)
        (4, by+0, HD), (5, by+0, HD),
        (4, by+1, HD), (5, by+1, H), (6, by+1, HD),
        (3, by+2, HD), (4, by+2, H), (5, by+2, H), (6, by+2, H), (7, by+2, HD),
        (3, by+3, HD), (4, by+3, H), (5, by+3, HL), (6, by+3, H), (7, by+3, H), (8, by+3, HD),
        (2, by+4, HD), (3, by+4, H), (4, by+4, H), (5, by+4, H), (6, by+4, H), (7, by+4, H), (8, by+4, HD),
        (2, by+5, HD), (3, by+5, HL), (4, by+5, H), (5, by+5, H), (6, by+5, H), (7, by+5, HL), (8, by+5, HL), (9, by+5, HD),
        (1, by+6, HD), (2, by+6, HD), (3, by+6, HD), (4, by+6, H), (5, by+6, H), (6, by+6, H), (7, by+6, HD), (8, by+6, HD), (9, by+6, HD),
        # Face (side)
        (3, by+7, SKIN_D), (4, by+7, SKIN), (5, by+7, SKIN), (6, by+7, SKIN_D),
        (3, by+8, SKIN_D), (4, by+8, SKIN), (5, by+8, EYE), (6, by+8, PUPIL), (7, by+8, SKIN_D),
        # Robe (side profile - narrower)
        (2, by+9, RD), (3, by+9, R), (4, by+9, R), (5, by+9, R), (6, by+9, R), (7, by+9, RD),
        (2, by+10, RD), (3, by+10, R), (4, by+10, RL), (5, by+10, R), (6, by+10, R), (7, by+10, RL), (8, by+10, RD),
        (1, by+11, RD), (2, by+11, R), (3, by+11, R), (4, by+11, R), (5, by+11, R), (6, by+11, R), (7, by+11, R), (8, by+11, RD),
        (1, by+12, RD), (2, by+12, R), (3, by+12, R), (4, by+12, R), (5, by+12, R), (6, by+12, R), (7, by+12, R), (8, by+12, RD),
        (2, by+13, RD), (3, by+13, R), (4, by+13, R), (5, by+13, R), (6, by+13, R), (7, by+13, R), (8, by+13, RD),
        (2, by+14, RD), (3, by+14, R), (4, by+14, R), (5, by+14, R), (6, by+14, R), (7, by+14, R), (8, by+14, RD),
        (2, by+15, RD), (3, by+15, R), (4, by+15, R), (5, by+15, R), (6, by+15, R), (7, by+15, R), (8, by+15, RD),
        (1, by+16, RD), (2, by+16, R), (3, by+16, R), (4, by+16, R), (5, by+16, R), (6, by+16, R), (7, by+16, R), (8, by+16, R), (9, by+16, RD),
        (2, by+17, RD), (3, by+17, R), (4, by+17, R), (5, by+17, R), (6, by+17, R), (7, by+17, R), (8, by+17, RD),
        # Staff (held forward)
        (7, by+3, STAFF_D), (8, by+3, STAFF),
        (7, by+4, STAFF_D), (8, by+4, STAFF),
        (7, by+5, O), (8, by+5, OL),  # orb
        (7, by+6, STAFF_D), (8, by+6, STAFF),
        (7, by+7, STAFF_D), (8, by+7, STAFF),
        (7, by+8, STAFF_D), (8, by+8, STAFF),
        (7, by+9, STAFF_D), (8, by+9, STAFF),
        (7, by+10, STAFF_D), (8, by+10, STAFF),
        (7, by+11, STAFF_D), (8, by+11, STAFF),
        (7, by+12, STAFF_D), (8, by+12, STAFF),
        (7, by+13, STAFF_D), (8, by+13, STAFF),
        (7, by+14, STAFF_D), (8, by+14, STAFF),
        (7, by+15, STAFF_D), (8, by+15, STAFF),
        (7, by+16, STAFF_D), (8, by+16, STAFF),
        (6, by+17, STAFF_D), (7, by+17, STAFF), (8, by+17, STAFF), (9, by+17, STAFF_D),
        # Feet
        (3, by+18, ROBE_D1), (4, by+18, ROBE1), (5, by+18, SKIN_D), (6, by+18, ROBE1), (7, by+18, ROBE_D1),
    ]
    if not facing_right:
        pixels = mirror_pixels(pixels)
    return pixels


# ═══════════════════════════════════════════════════════════════
#  Animation frame generators
#  Each returns a list of 64x64 PIL Images (one per frame)
# ═══════════════════════════════════════════════════════════════

def gen_idle_frames(tier):
    """4 frames: subtle breathing bob."""
    frames = []
    for i in range(4):
        bob = (i % 2) * 1  # 0, 1, 0, 1 breathing
        frames.append(frame_to_img(build_character(tier, bob=bob)))
    return frames


def gen_run_frames(tier):
    """6 frames: walk cycle with bounce and sway."""
    frames = []
    bounce = [0, 2, 0, -1, 0, 2]
    sway_vals = [0, 1, 0, -1, -1, 0]
    for i in range(6):
        frames.append(frame_to_img(build_character(tier, bob=bounce[i], sway=sway_vals[i])))
    return frames


def gen_attack_frames(tier):
    """6 frames: staff swing attack."""
    frames = []
    # Attack: raise staff, swing forward, return
    staff_sway = [0, 1, 2, 1, 0, -1]
    body_sway = [0, 0, 1, 1, 0, 0]
    for i in range(6):
        pixels = build_character(tier, bob=(0 if i < 3 else 0), sway=body_sway[i])
        # Add extra "swing" effect by shifting staff pixels further
        if staff_sway[i] != 0:
            pixels = [(lx + (staff_sway[i] if lx >= 8 else 0), ly, c) for lx, ly, c in pixels]
        frames.append(frame_to_img(pixels))
    return frames


def gen_hurt_frames(tier):
    """2 frames: flinch backward."""
    frames = []
    frames.append(frame_to_img(build_character(tier, bob=0, sway=-1)))
    frames.append(frame_to_img(build_character(tier, bob=0, sway=-2)))
    return frames


def gen_death_frames(tier):
    """4 frames: collapse and fade."""
    frames = []
    for i in range(4):
        bob = i * 3  # sink down
        squash = -0.05 * i  # slight vertical compression
        # Fade out progressively
        alpha_set = set()
        if i >= 2:
            # Top half fades first
            for ly in range(0, 8 if i == 2 else 14):
                for lx in range(0, 14):
                    alpha_set.add((lx, ly))
        if i == 3:
            for ly in range(0, 22):
                for lx in range(0, 14):
                    alpha_set.add((lx, ly))
        frames.append(frame_to_img(build_character(tier, bob=bob, squash=squash, alpha_pixels=alpha_set if alpha_set else None)))
    return frames


# ═══════════════════════════════════════════════════════════════
#  Direction-specific sprite sheet generators
#  Each generates frames for a specific direction
# ═══════════════════════════════════════════════════════════════

def gen_dir_frames(tier, anim):
    """Generate frames for all 4 directions of an animation.
    Returns list of (dir_name, frames_list).
    Each direction has its own set of frames for variety.
    """
    if anim == "idle":
        fn = gen_idle_frames
    elif anim == "run":
        fn = gen_run_frames
    elif anim == "attack":
        fn = gen_attack_frames
    elif anim == "hurt":
        fn = gen_hurt_frames
    elif anim == "death":
        fn = gen_death_frames
    else:
        return []

    # DOWN: default
    down = fn(tier)
    # LEFT: side view facing left
    left = []
    for i in range(len(down)):
        left.append(frame_to_img(side_pixels(tier, bob=(i % 2) if anim == "idle" else 0, facing_right=False)))
    # RIGHT: side view facing right
    right = []
    for i in range(len(down)):
        right.append(frame_to_img(side_pixels(tier, bob=(i % 2) if anim == "idle" else 0, facing_right=True)))
    # UP: rear view
    up = []
    for i in range(len(down)):
        up.append(frame_to_img(rear_pixels(tier, bob=(i % 2) if anim == "idle" else 0)))

    return [("DOWN", down), ("LEFT", left), ("RIGHT", right), ("UP", up)]


def gen_sprite_strip(tier, anim):
    """Generate a horizontal sprite strip with 4 rows (directions).
    Layout: Row 0=DOWN, Row 1=LEFT, Row 2=RIGHT, Row 3=UP
    Columns = animation frames
    """
    dirs = gen_dir_frames(tier, anim)  # [(name, [frames]), ...]
    num_cols = len(dirs[0][1])
    total_w = FRAME * num_cols
    total_h = FRAME * 4
    sheet = Image.new("RGBA", (total_w, total_h), (0, 0, 0, 0))
    for row_idx, (dname, frames) in enumerate(dirs):
        for col_idx, frm in enumerate(frames):
            sheet.paste(frm, (col_idx * FRAME, row_idx * FRAME), frm)
    return sheet


# ═══════════════════════════════════════════════════════════════
#  Main
# ═══════════════════════════════════════════════════════════════

def main():
    os.makedirs(DST, exist_ok=True)

    animations = {
        "idle": "idle",
        "run": "run",
        "attack": "attack",
        "hurt": "hurt",
        "death": "death",
    }

    for tier in [1, 2, 3]:
        prefix = f"char_mage_lvl{tier}"
        for anim_key, anim_name in animations.items():
            filename = f"{prefix}_{anim_name}.png"
            filepath = os.path.join(DST, filename)
            sheet = gen_sprite_strip(tier, anim_key)
            sheet.save(filepath, "PNG")
            print(f"Saved {filename} ({sheet.width}x{sheet.height})")


if __name__ == "__main__":
    main()
