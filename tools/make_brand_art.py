"""Generates the Copium Client brand images (logo, wordmarks, banners, title letters).

Run from the repository root:  python3 tools/make_brand_art.py
Every image is written at the same size as the SoberClient image it replaces,
so the client's layouts keep working.
"""
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageChops

A = "src/main/resources/assets/violet"
FONT = os.path.join(A, "font/blackopsone-regular.ttf")


def font(size):
    return ImageFont.truetype(FONT, size)


def fit_font(text, max_w, max_h, start=600):
    size = start
    while size > 8:
        f = font(size)
        l, t, r, b = f.getbbox(text)
        if r - l <= max_w and b - t <= max_h:
            return f
        size -= 4
    return font(8)


def chrome_fill(size, top=(255, 255, 255), mid=(150, 155, 170), bottom=(245, 245, 250)):
    w, h = size
    g = Image.new("RGBA", size)
    px = g.load()
    for y in range(h):
        t = y / max(1, h - 1)
        if t < 0.5:
            k = t / 0.5
            c = tuple(int(top[i] + (mid[i] - top[i]) * k) for i in range(3))
        else:
            k = (t - 0.5) / 0.5
            c = tuple(int(mid[i] + (bottom[i] - mid[i]) * k) for i in range(3))
        for x in range(w):
            px[x, y] = c + (255,)
    return g


def glow(mask, radius, color=(255, 255, 255), strength=1.0):
    g = mask.filter(ImageFilter.GaussianBlur(radius))
    if strength != 1.0:
        g = g.point(lambda v: min(255, int(v * strength)))
    layer = Image.new("RGBA", mask.size, color + (0,))
    layer.putalpha(g)
    return layer


def chrome_text(size, lines, pad=0.08, glow_color=(255, 255, 255), spacing=0.12):
    """Chrome text block centered in an image of `size`, transparent background."""
    w, h = size
    inner_w, inner_h = int(w * (1 - 2 * pad)), int(h * (1 - 2 * pad))
    per_line_h = inner_h / (len(lines) + spacing * (len(lines) - 1))
    fonts = [fit_font(t, inner_w, per_line_h) for t in lines]
    mask = Image.new("L", size, 0)
    d = ImageDraw.Draw(mask)
    heights = [f.getbbox(t)[3] - f.getbbox(t)[1] for f, t in zip(fonts, lines)]
    total = sum(heights) + spacing * per_line_h * (len(lines) - 1)
    y = (h - total) / 2
    for f, t, lh in zip(fonts, lines, heights):
        l, tp, r, b = f.getbbox(t)
        x = (w - (r - l)) / 2 - l
        d.text((x, y - tp), t, font=f, fill=255)
        y += lh + spacing * per_line_h
    out = Image.new("RGBA", size, (0, 0, 0, 0))
    out.alpha_composite(glow(mask, max(4, h // 40), glow_color, 0.9))
    edge = mask.filter(ImageFilter.MaxFilter(5))
    dark = Image.new("RGBA", size, (30, 32, 40, 0))
    dark.putalpha(edge)
    out.alpha_composite(dark)
    fill = chrome_fill(size)
    fill.putalpha(mask)
    out.alpha_composite(fill)
    # specular line across the upper third
    shine = Image.new("L", size, 0)
    ImageDraw.Draw(shine).rectangle([0, int(h * 0.30), w, int(h * 0.36)], fill=90)
    shine = ImageChops.multiply(shine, mask)
    s = Image.new("RGBA", size, (255, 255, 255, 0))
    s.putalpha(shine.filter(ImageFilter.GaussianBlur(2)))
    out.alpha_composite(s)
    return out


def tank_mask(size, box=None, stroke=None):
    """Outline mask of an oxygen ('copium') tank with a C on it."""
    w, h = size
    if box is None:
        box = (0, 0, w, h)
    bx, by, bw, bh = box[0], box[1], box[2] - box[0], box[3] - box[1]
    s = stroke or max(2, int(min(bw, bh) * 0.045))
    m = Image.new("L", size, 0)
    d = ImageDraw.Draw(m)
    cx = bx + bw / 2
    body_w = bw * 0.46
    body_top = by + bh * 0.26
    body_bot = by + bh * 0.96
    r = body_w / 2
    # body
    d.rounded_rectangle([cx - r, body_top, cx + r, body_bot], radius=r * 0.95, outline=255, width=s)
    # neck
    nw = body_w * 0.32
    d.rectangle([cx - nw / 2, by + bh * 0.15, cx + nw / 2, body_top + s], outline=255, width=s)
    # valve wheel + spout
    d.ellipse([cx - nw * 0.95, by + bh * 0.03, cx + nw * 0.95, by + bh * 0.15], outline=255, width=s)
    d.line([cx + nw / 2, by + bh * 0.20, cx + body_w * 0.62, by + bh * 0.20], fill=255, width=s)
    d.line([cx + body_w * 0.62, by + bh * 0.20, cx + body_w * 0.62, by + bh * 0.30], fill=255, width=s)
    # label band
    d.line([cx - r + s, by + bh * 0.47, cx + r - s, by + bh * 0.47], fill=255, width=max(1, s // 2))
    d.line([cx - r + s, by + bh * 0.80, cx + r - s, by + bh * 0.80], fill=255, width=max(1, s // 2))
    # "C" on the label
    f = fit_font("C", body_w * 0.62, bh * 0.28, start=400)
    l, t, rr, b = f.getbbox("C")
    d.text((cx - (rr - l) / 2 - l, by + bh * 0.635 - (b - t) / 2 - t), "C", font=f, fill=255)
    return m


def neon(mask, bg=(0, 0, 0, 255), size=None):
    size = size or mask.size
    out = Image.new("RGBA", size, bg)
    r = max(3, min(size) // 45)
    out.alpha_composite(glow(mask, r * 3, (200, 210, 255), 1.6))
    out.alpha_composite(glow(mask, r, (255, 255, 255), 1.4))
    core = Image.new("RGBA", size, (255, 255, 255, 0))
    core.putalpha(mask)
    out.alpha_composite(core)
    return out


def save(img, path, mode=None):
    full = os.path.join(A, path)
    old = Image.open(full)
    if img.size != old.size:
        img = img.resize(old.size, Image.LANCZOS)
    img.convert(mode or old.mode).save(full)
    print("wrote", path, old.size, mode or old.mode)


def icon(size):
    m = tank_mask((size, size), (size * 0.18, size * 0.08, size * 0.82, size * 0.92))
    return neon(m, (0, 0, 0, 255))


def dark_bg(size):
    w, h = size
    bg = Image.new("RGBA", size, (8, 8, 10, 255))
    vign = Image.new("L", size, 0)
    ImageDraw.Draw(vign).ellipse([-w * 0.1, -h * 0.4, w * 1.1, h * 1.4], fill=60)
    vign = vign.filter(ImageFilter.GaussianBlur(min(size) // 6))
    light = Image.new("RGBA", size, (40, 42, 52, 0))
    light.putalpha(vign)
    bg.alpha_composite(light)
    return bg


def main():
    # icons
    save(icon(512), "icon.png")
    for s in (16, 32, 48, 64, 128, 256):
        save(icon(max(s, 128)).resize((s, s), Image.LANCZOS), f"textures/brand/icon_{s}.png")

    # wordmarks
    for p in ("textures/brand/lockup.png", "textures/brand/wordmark.png"):
        save(chrome_text(Image.open(os.path.join(A, p)).size, ["Copium"]), p)
    save(chrome_text((1231, 858), ["Copium", "Client"], spacing=0.05), "textures/brand/sober_stacked.png")
    save(chrome_text((900, 726), ["COPIUM", "CLIENT"], spacing=0.08, glow_color=(170, 140, 255)), "textures/brand/stacked.png")
    save(chrome_text((2000, 466), ["COPIUM CLIENT"]), "textures/brand/sober_discord.png")
    save(chrome_text((768, 504), ["CC"], glow_color=(170, 140, 255)), "textures/brand/vc.png")

    # the bottle logo slots become the tank
    for p in ("textures/brand/orbit.png", "textures/brand/orbit_glow.png"):
        size = Image.open(os.path.join(A, p)).size
        m = tank_mask(size, (size[0] * 0.08, size[1] * 0.04, size[0] * 0.92, size[1] * 0.96))
        img = neon(m, (0, 0, 0, 0))
        save(img, p)

    # in-menu logo and the cape logo: white tank outline on transparent
    for p in ["textures/gui/brand.png"] + [f"textures/gui/logo_{s}.png" for s in (16, 32, 64, 128, 256, 512)]:
        size = Image.open(os.path.join(A, p)).size
        work = (max(size[0], 256), max(size[1], 256))
        m = tank_mask(work, (work[0] * 0.2, work[1] * 0.06, work[0] * 0.8, work[1] * 0.94))
        img = Image.new("RGBA", work, (0, 0, 0, 0))
        img.alpha_composite(glow(m, max(2, work[0] // 90), (255, 255, 255), 0.8))
        core = Image.new("RGBA", work, (255, 255, 255, 0))
        core.putalpha(m)
        img.alpha_composite(core)
        save(img, p)

    # media hider covers (opaque RGB)
    w, h = 1774, 887
    bg = dark_bg((w, h))
    side = tank_mask((w, h), (w * 0.03, h * 0.18, w * 0.20, h * 0.86))
    side2 = tank_mask((w, h), (w * 0.80, h * 0.18, w * 0.97, h * 0.86))
    faint = neon(ImageChops.lighter(side, side2), (0, 0, 0, 0))
    faint.putalpha(faint.getchannel("A").point(lambda v: v // 5))
    bg.alpha_composite(faint)
    bg.alpha_composite(chrome_text((w, h), ["Copium", "Client"], pad=0.22, spacing=0.0))
    save(bg, "textures/mediahider/sober_banner.png", "RGB")

    save(neon(tank_mask((512, 512), (90, 40, 422, 472)), (8, 8, 10, 255)), "textures/mediahider/sober_bottle.png", "RGB")

    bg = dark_bg((w, h))
    bg.alpha_composite(chrome_text((w, h), ["Copium", "NO CHEATS. JUST VIBES."], pad=0.16, spacing=0.15))
    save(bg, "textures/mediahider/sober_discord.png", "RGB")

    sw, sh = 1774, 95
    strip = Image.new("RGBA", (sw, sh), (8, 8, 10, 255))
    unit = chrome_text((sw // 4, sh), ["Copium"], pad=0.18)
    for i in range(4):
        strip.alpha_composite(unit, (i * sw // 4, 0))
    save(strip, "textures/mediahider/sober_strip.png", "RGB")

    # sky horizon text
    sky = Image.new("RGBA", (2000, 667), (0, 0, 0, 255))
    sky.alpha_composite(chrome_text((2000, 667), ["COPIUM CLIENT"], pad=0.2))
    save(sky, "textures/sky/sober_discord.png", "RGB")

    # island title: V I O L E T  ->  C O P I U M (puffy cloud letters)
    for i, ch in enumerate("COPIUM"):
        p = f"textures/menu/title_{i}.png"
        old = Image.open(os.path.join(A, p))
        h = old.size[1]
        f = font(int(h * 0.92))
        l, t, r, b = f.getbbox(ch)
        w = (r - l) + int(h * 0.22)
        m = Image.new("L", (w, h), 0)
        ImageDraw.Draw(m).text(((w - (r - l)) / 2 - l, (h - (b - t)) / 2 - t), ch, font=f, fill=255)
        puff = m.filter(ImageFilter.MaxFilter(15)).filter(ImageFilter.GaussianBlur(9))
        puff = puff.point(lambda v: 255 if v > 110 else int(v * 2.2))
        out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        out.alpha_composite(glow(puff, 14, (190, 170, 255), 1.0))
        body = Image.new("RGBA", (w, h), (255, 255, 255, 0))
        body.putalpha(puff)
        out.alpha_composite(body)
        out.save(os.path.join(A, p))
        print("wrote", p, (w, h))


if __name__ == "__main__":
    main()
