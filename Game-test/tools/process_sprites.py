from PIL import Image
import os
from collections import deque
from colorsys import rgb_to_hsv, hsv_to_rgb

SRC = r"C:/Users/lenovo/.cursor/projects/d-learning-java-learn-Assignment2/assets"
DST = r"D:/learning/java-learn/Assignment2/Game-test/assets/sprites"
FRAME = 150

SPECS = {
    "enemy_bat.png": ("enemy_bat_pixel.png", 8, 26, 2, False),
    "enemy_boss.png": ("enemy_boss_pixel.png", 10, 36, 2, False),
    "enemy_ghost.png": ("enemy_ghost_pixel.png", 8, 28, 2, True),
    "enemy_giant.png": ("enemy_giant_pixel.png", 8, 34, 2, False),
    "enemy_slime.png": ("enemy_slime_pixel.png", 8, 24, 2, False),
}


def is_bg_seed(px, thresh=46):
    if len(px) == 4:
        r, g, b, a = px
        if a < 12:
            return True
    else:
        r, g, b = px[:3]
    if r < thresh and g < thresh and b < thresh:
        return True
    if r > 215 and g > 215 and b > 215:
        return True
    if r < 40 and g < 40 and 50 < b < 130:
        return True
    return False


def flood_strip_bg(img):
    img = img.convert("RGBA")
    w, h = img.size
    px = img.load()
    bg = [[False] * w for _ in range(h)]
    q = deque()

    for x in range(w):
        q.append((x, 0))
        q.append((x, h - 1))
    for y in range(h):
        q.append((0, y))
        q.append((w - 1, y))

    while q:
        x, y = q.popleft()
        if x < 0 or x >= w or y < 0 or y >= h or bg[y][x]:
            continue
        if not is_bg_seed(px[x, y]):
            continue
        bg[y][x] = True
        q.append((x + 1, y))
        q.append((x - 1, y))
        q.append((x, y + 1))
        q.append((x, y - 1))

    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    opx = out.load()
    for y in range(h):
        for x in range(w):
            if not bg[y][x]:
                opx[x, y] = px[x, y]
    return out


def content_bbox(img, alpha_min=10):
    w, h = img.size
    px = img.load()
    minx, miny, maxx, maxy = w, h, 0, 0
    found = False
    for y in range(h):
        for x in range(w):
            if px[x, y][3] > alpha_min:
                found = True
                minx = min(minx, x)
                miny = min(miny, y)
                maxx = max(maxx, x)
                maxy = max(maxy, y)
    if not found:
        return (0, 0, w, h)
    return (minx, miny, maxx + 1, maxy + 1)


def mute_colors(img, sat_factor=0.50, val_factor=0.76):
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a < 10:
                continue
            h, s, v = rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
            nr, ng, nb = hsv_to_rgb(h, s * sat_factor, v * val_factor)
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    return img


def harden_alpha(img, alpha_cut=130):
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a < alpha_cut:
                px[x, y] = (0, 0, 0, 0)
            else:
                px[x, y] = (r, g, b, 255)
    return img


def keep_largest_blob(img):
    w, h = img.size
    px = img.load()
    visited = [[False] * w for _ in range(h)]
    best = []

    for sy in range(h):
        for sx in range(w):
            if visited[sy][sx] or px[sx, sy][3] < 10:
                continue
            stack = [(sx, sy)]
            comp = []
            visited[sy][sx] = True
            while stack:
                x, y = stack.pop()
                comp.append((x, y))
                for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
                    if 0 <= nx < w and 0 <= ny < h and not visited[ny][nx] and px[nx, ny][3] >= 10:
                        visited[ny][nx] = True
                        stack.append((nx, ny))
            if len(comp) > len(best):
                best = comp

    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    opx = out.load()
    for x, y in best:
        opx[x, y] = px[x, y]
    return out


def remove_stray_pixels(img):
    w, h = img.size
    px = img.load()
    out = img.copy()
    opx = out.load()
    for y in range(h):
        for x in range(w):
            if px[x, y][3] < 10:
                continue
            neighbors = 0
            for nx in range(max(0, x - 1), min(w, x + 2)):
                for ny in range(max(0, y - 1), min(h, y + 2)):
                    if nx == x and ny == y:
                        continue
                    if px[nx, ny][3] >= 10:
                        neighbors += 1
            if neighbors <= 1:
                opx[x, y] = (0, 0, 0, 0)
    return out


def quantize_rgba(img, max_colors=14):
    alpha = img.split()[3]
    rgb = img.convert("RGB")
    q = rgb.quantize(colors=max_colors, method=Image.MEDIANCUT, dither=Image.Dither.NONE)
    out = q.convert("RGBA")
    out.putalpha(alpha)
    return out


def clean_sprite(img, keep_wispy=False):
    img = harden_alpha(img)
    if not keep_wispy:
        img = keep_largest_blob(img)
    img = remove_stray_pixels(img)
    return img


def pixelize_char(char, target_h, max_mult, keep_wispy=False):
    cw, ch = char.size
    small_h = target_h
    small_w = max(1, int(cw * target_h / ch))
    small = char.resize((small_w, small_h), Image.BOX)
    small = mute_colors(small)
    small = quantize_rgba(small, max_colors=14)
    small = clean_sprite(small, keep_wispy=keep_wispy)
    return small.resize((small_w * max_mult, small_h * max_mult), Image.NEAREST)


def extract_frames(src_path, n_frames, target_h, max_mult, keep_wispy=False):
    img = flood_strip_bg(Image.open(src_path).convert("RGBA"))
    row = img.crop(content_bbox(img))
    rw, rh = row.size
    slice_w = rw / n_frames
    frames = []
    for i in range(n_frames):
        x0 = int(round(i * slice_w))
        x1 = int(round((i + 1) * slice_w))
        if x1 <= x0:
            x1 = x0 + 1
        part = row.crop((x0, 0, x1, rh))
        bx = content_bbox(part)
        char = part.crop(bx)
        pixel = pixelize_char(char, target_h, max_mult, keep_wispy=keep_wispy)
        pw, ph = pixel.size
        canvas = Image.new("RGBA", (FRAME, FRAME), (0, 0, 0, 0))
        ox = (FRAME - pw) // 2
        oy = FRAME - ph - 10
        canvas.paste(pixel, (ox, oy), pixel)
        frames.append(clean_sprite(canvas, keep_wispy=keep_wispy))
    return frames


def main():
    for dst_name, (src_name, n, target_h, max_mult, keep_wispy) in SPECS.items():
        src = os.path.join(SRC, src_name)
        if not os.path.exists(src):
            print(f"missing {src_name}, skip")
            continue
        sheet = Image.new("RGBA", (FRAME * n, FRAME), (0, 0, 0, 0))
        frames = extract_frames(src, n, target_h, max_mult, keep_wispy=keep_wispy)
        for i, frame in enumerate(frames):
            sheet.paste(frame, (i * FRAME, 0), frame)
        out = os.path.join(DST, dst_name)
        sheet.save(out, "PNG")
        print(f"saved {dst_name}: pixel_h={target_h}")


if __name__ == "__main__":
    main()
