from PIL import Image
import os

DST = r"D:/learning/java-learn/Assignment2/Game-test/assets/sprites"
FRAME = 150
PIX = 3          # chunky pixels matching mushroom visual weight
BOTTOM = 48      # align feet near mushroom baseline (~y=101)


def canvas():
    return Image.new("RGBA", (FRAME, FRAME), (0, 0, 0, 0))


def put(img, ox, oy, pixels, scale=PIX):
    px = img.load()
    for dx, dy, color in pixels:
        for sy in range(scale):
            for sx in range(scale):
                x, y = ox + dx * scale + sx, oy + dy * scale + sy
                if 0 <= x < FRAME and 0 <= y < FRAME:
                    px[x, y] = color


def anchor_bottom(img, bottom=BOTTOM):
    px = img.load()
    minx, miny, maxx, maxy = FRAME, FRAME, 0, 0
    for y in range(FRAME):
        for x in range(FRAME):
            if px[x, y][3] > 0:
                minx, miny = min(minx, x), min(miny, y)
                maxx, maxy = max(maxx, x), max(maxy, y)
    if maxx <= minx:
        return img
    cropped = img.crop((minx, miny, maxx + 1, maxy + 1))
    out = canvas()
    ox = (FRAME - cropped.width) // 2
    oy = FRAME - cropped.height - bottom
    out.paste(cropped, (ox, max(0, oy)), cropped)
    return out


def make_sheet(frames):
    out = Image.new("RGBA", (FRAME * len(frames), FRAME), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        out.paste(f, (i * FRAME, 0), f)
    return out


# ── Bat (mushroom-like muted purple) ──
P_BAT = (97, 58, 118, 255)
D_BAT = (58, 34, 82, 255)
W_BAT = (74, 46, 108, 255)
E_BAT = (168, 48, 58, 255)
O_BAT = (38, 22, 48, 255)


def bat_frame(wing):
    img = canvas()
    cx, cy, s = FRAME // 2 - 12, FRAME - BOTTOM - 12 * PIX, PIX
    put(img, cx, cy, [(0, 2, P_BAT), (1, 2, P_BAT), (0, 3, D_BAT), (1, 3, D_BAT)], s)
    put(img, cx, cy, [(-1, 1, D_BAT), (2, 1, D_BAT)], s)
    put(img, cx, cy, [(0, 2, E_BAT), (1, 2, E_BAT)], s)
    wing_map = {
        0: [(-3, 0, W_BAT), (-2, 1, W_BAT), (3, 0, W_BAT), (4, 1, W_BAT)],
        1: [(-3, 1, W_BAT), (-2, 2, W_BAT), (3, 1, W_BAT), (4, 2, W_BAT)],
        2: [(-4, 1, W_BAT), (-3, 2, W_BAT), (4, 1, W_BAT), (5, 2, W_BAT)],
        3: [(-4, 2, W_BAT), (-3, 3, W_BAT), (4, 2, W_BAT), (5, 3, W_BAT)],
        4: [(-3, 3, W_BAT), (-2, 4, W_BAT), (3, 3, W_BAT), (4, 4, W_BAT)],
    }
    put(img, cx, cy, wing_map[wing], s)
    return anchor_bottom(img)


def gen_bat():
    seq = [0, 1, 2, 3, 4, 3, 2, 1]
    return make_sheet([bat_frame(i) for i in seq])


# ── Ghost ──
G1 = (189, 171, 148, 255)
G2 = (142, 123, 99, 255)
G3 = (58, 68, 92, 255)


def ghost_frame(t):
    img = canvas()
    cx, cy, s = FRAME // 2 - 15, FRAME - BOTTOM - 14 * PIX, PIX
    body = [
        (2, 0, G1), (3, 0, G1), (4, 0, G1), (5, 0, G1), (6, 0, G1),
        (1, 1, G1), (2, 1, G1), (3, 1, G1), (4, 1, G1), (5, 1, G1), (6, 1, G1), (7, 1, G1),
        (1, 2, G2), (2, 2, G2), (3, 2, G2), (4, 2, G2), (5, 2, G2), (6, 2, G2), (7, 2, G2),
        (1, 3, G2), (2, 3, G2), (3, 3, G2), (4, 3, G2), (5, 3, G2), (6, 3, G2), (7, 3, G2),
        (2, 4, G2), (3, 4, G2), (4, 4, G2), (5, 4, G2), (6, 4, G2),
    ]
    tails = {
        0: [(1, 5, G2), (2, 5, G2), (3, 5, G2), (4, 5, G2), (5, 5, G2), (6, 5, G2), (7, 5, G2), (2, 6, G2), (6, 6, G2)],
        1: [(1, 5, G2), (2, 5, G2), (3, 5, G2), (4, 5, G2), (5, 5, G2), (6, 5, G2), (7, 5, G2), (1, 6, G2), (4, 6, G2), (7, 6, G2)],
        2: [(1, 5, G2), (2, 5, G2), (3, 5, G2), (4, 5, G2), (5, 5, G2), (6, 5, G2), (7, 5, G2), (2, 6, G2), (3, 6, G2), (5, 6, G2), (6, 6, G2)],
        3: [(1, 5, G2), (2, 5, G2), (3, 5, G2), (4, 5, G2), (5, 5, G2), (6, 5, G2), (7, 5, G2), (0, 6, G2), (8, 6, G2)],
    }
    put(img, cx, cy, body + tails[t % 4], s)
    put(img, cx, cy, [(2, 1, G3), (3, 1, G3), (5, 1, G3), (6, 1, G3)], PIX)
    return anchor_bottom(img)


def gen_ghost():
    return make_sheet([ghost_frame(i) for i in range(8)])


# ── Slime ──
S1 = (58, 118, 68, 255)
S2 = (42, 92, 52, 255)
S3 = (88, 148, 82, 255)
K = (28, 38, 32, 255)
W = (210, 220, 205, 255)


def slime_frame(t):
    img = canvas()
    cx, cy, s = FRAME // 2 - 15, FRAME - BOTTOM - 10 * PIX, PIX
    shapes = {
        0: [
            (2, 1, S1), (3, 1, S1), (4, 1, S1), (5, 1, S1), (6, 1, S1),
            (1, 2, S1), (2, 2, S3), (3, 2, S3), (4, 2, S3), (5, 2, S3), (6, 2, S3), (7, 2, S1),
            (1, 3, S2), (2, 3, S2), (3, 3, S2), (4, 3, S2), (5, 3, S2), (6, 3, S2), (7, 3, S2),
        ],
        1: [
            (1, 2, S1), (2, 2, S1), (3, 2, S3), (4, 2, S3), (5, 2, S3), (6, 2, S1), (7, 2, S1),
            (0, 3, S2), (1, 3, S2), (2, 3, S2), (3, 3, S2), (4, 3, S2), (5, 3, S2), (6, 3, S2), (7, 3, S2), (8, 3, S2),
        ],
        2: [(0, 3, S1), (1, 3, S1), (2, 3, S1), (3, 3, S1), (4, 3, S1), (5, 3, S1), (6, 3, S1), (7, 3, S1), (8, 3, S1)],
        3: [
            (3, 0, S1), (4, 0, S1), (2, 1, S1), (3, 1, S3), (4, 1, S3), (5, 1, S1),
            (2, 2, S2), (3, 2, S2), (4, 2, S2), (5, 2, S2),
            (3, 3, S2), (4, 3, S2),
        ],
        4: [(3, 0, S1), (4, 0, S1), (3, 1, S3), (4, 1, S3), (3, 2, S2), (4, 2, S2), (3, 3, S2), (4, 3, S2)],
        5: [
            (3, 0, S1), (4, 0, S1), (2, 1, S1), (3, 1, S3), (4, 1, S3), (5, 1, S1),
            (2, 2, S2), (3, 2, S2), (4, 2, S2), (5, 2, S2),
            (3, 3, S2), (4, 3, S2),
        ],
        6: [
            (1, 2, S1), (2, 2, S1), (3, 2, S3), (4, 2, S3), (5, 2, S3), (6, 2, S1), (7, 2, S1),
            (0, 3, S2), (1, 3, S2), (2, 3, S2), (3, 3, S2), (4, 3, S2), (5, 3, S2), (6, 3, S2), (7, 3, S2), (8, 3, S2),
        ],
        7: [
            (2, 1, S1), (3, 1, S1), (4, 1, S1), (5, 1, S1), (6, 1, S1),
            (1, 2, S1), (2, 2, S3), (3, 2, S3), (4, 2, S3), (5, 2, S3), (6, 2, S3), (7, 2, S1),
            (1, 3, S2), (2, 3, S2), (3, 3, S2), (4, 3, S2), (5, 3, S2), (6, 3, S2), (7, 3, S2),
        ],
    }
    put(img, cx, cy, shapes[t], s)
    put(img, cx, cy, [(2, 1, W), (3, 1, W), (5, 1, W), (6, 1, W), (2, 2, K), (3, 2, K), (5, 2, K), (6, 2, K)], PIX)
    return anchor_bottom(img)


def gen_slime():
    return make_sheet([slime_frame(i) for i in range(8)])


# ── Giant ──
R1 = (142, 123, 99, 255)
R2 = (97, 78, 62, 255)
R3 = (189, 171, 148, 255)
B1 = (97, 33, 40, 255)
B2 = (52, 16, 20, 255)
Y = (168, 138, 58, 255)


def giant_frame(step):
    img = canvas()
    cx, cy, s = FRAME // 2 - 15, FRAME - BOTTOM - 18 * PIX, PIX
    core = [
        (3, 0, R3), (4, 0, R3), (5, 0, R3),
        (2, 1, R1), (3, 1, R1), (4, 1, R1), (5, 1, R1), (6, 1, R1),
        (2, 2, R2), (3, 2, R2), (4, 2, R2), (5, 2, R2), (6, 2, R2),
        (1, 3, R1), (2, 3, R1), (3, 3, R3), (4, 3, R3), (5, 3, R1), (6, 3, R1), (7, 3, R1),
        (2, 4, R2), (3, 4, R2), (4, 4, R2), (5, 4, R2), (6, 4, R2),
        (2, 5, B1), (3, 5, Y), (4, 5, B1), (5, 5, Y), (6, 5, B1),
        (3, 1, Y), (5, 1, Y),
    ]
    legs_a = [(2, 6, R2), (3, 6, R2), (5, 6, R2), (6, 6, R2), (2, 7, B2), (3, 7, B2), (5, 7, B2), (6, 7, B2)]
    legs_b = [(1, 6, R2), (2, 6, R2), (4, 6, R2), (5, 6, R2), (1, 7, B2), (2, 7, B2), (4, 7, B2), (5, 7, B2)]
    arms_a = [(0, 3, R1), (1, 3, R1), (0, 4, R2), (1, 4, R2), (8, 3, R1), (9, 3, R1), (8, 4, R2), (9, 4, R2)]
    arms_b = [(0, 4, R1), (1, 4, R1), (0, 5, R2), (1, 5, R2), (8, 4, R1), (9, 4, R1), (8, 5, R2), (9, 5, R2)]
    put(img, cx, cy, core)
    put(img, cx, cy, legs_a if step % 2 == 0 else legs_b, s)
    put(img, cx, cy, arms_a if step % 2 == 0 else arms_b, s)
    return anchor_bottom(img)


def gen_giant():
    return make_sheet([giant_frame(i) for i in range(8)])


# ── Boss Vampire ──
V1 = (251, 233, 209, 255)
V2 = (189, 171, 148, 255)
C1 = (43, 13, 16, 255)
C2 = (97, 33, 40, 255)
C3 = (142, 38, 48, 255)
G = (168, 138, 58, 255)


def boss_frame(t):
    img = canvas()
    cx, cy, s = FRAME // 2 - 15, FRAME - BOTTOM - 20 * PIX, PIX
    core = [
        (2, 0, V1), (3, 0, V1), (4, 0, V1), (5, 0, V1), (6, 0, V1),
        (1, 1, V1), (2, 1, V2), (3, 1, V2), (4, 1, V2), (5, 1, V2), (6, 1, V1), (7, 1, V1),
        (2, 2, V2), (3, 2, V2), (4, 2, V2), (5, 2, V2),
        (2, 3, V1), (5, 3, V1),
        (2, 4, C1), (3, 4, C1), (4, 4, C3), (5, 4, C1), (6, 4, C1),
        (2, 5, C1), (3, 5, G), (4, 5, G), (5, 5, G), (6, 5, C1),
        (2, 6, C1), (3, 6, C1), (4, 6, C1), (5, 6, C1), (6, 6, C1),
        (2, 7, C1), (3, 7, C1), (5, 7, C1), (6, 7, C1),
        (0, 2, C2), (0, 3, C3), (0, 4, C2), (0, 5, C2), (0, 6, C2),
        (8, 2, C2), (8, 3, C3), (8, 4, C2), (8, 5, C2), (8, 6, C2),
        (1, 2, C3), (7, 2, C3),
    ]
    arm = [(0, 4, V2), (-1, 4, V2), (-1, 5, V2)] if t in (3, 7) else []
    put(img, cx, cy, core + arm, s)
    return anchor_bottom(img)


def gen_boss():
    return make_sheet([boss_frame(i) for i in range(10)])


def main():
    os.makedirs(DST, exist_ok=True)
    files = {
        "enemy_bat.png": gen_bat(),
        "enemy_ghost.png": gen_ghost(),
        "enemy_slime.png": gen_slime(),
        "enemy_giant.png": gen_giant(),
        "enemy_boss.png": gen_boss(),
    }
    for name, img in files.items():
        path = os.path.join(DST, name)
        img.save(path, "PNG")
        print(f"saved {name}")


if __name__ == "__main__":
    main()
