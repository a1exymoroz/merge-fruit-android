from PIL import Image, ImageDraw

SS = 8          # supersample
V = 108         # source viewport
OUT = 512
S = OUT * SS
sc = S / V

def px(v): return v * sc

img = Image.new("RGB", (S, S), "#667EEA")
d = ImageDraw.Draw(img)

def circle(cx, cy, r, fill):
    d.ellipse([px(cx - r), px(cy - r), px(cx + r), px(cy + r)], fill=fill)

def rect(x, y, w, h, fill):
    d.rectangle([px(x), px(y), px(x + w), px(y + h)], fill=fill)

circle(54, 54, 34, "#27AE60")   # rind
circle(54, 54, 30, "#ECF0F1")   # white pith
circle(54, 54, 26, "#FF4757")   # red flesh
rect(52.5, 47, 3, 10, "#2F3542")  # seeds
rect(35, 52.5, 10, 3, "#2F3542")
rect(63, 52.5, 10, 3, "#2F3542")
rect(52.5, 63, 3, 10, "#2F3542")

img = img.resize((OUT, OUT), Image.LANCZOS)
img.save("icon-512.png")
print("wrote icon-512.png", img.size, img.mode)
