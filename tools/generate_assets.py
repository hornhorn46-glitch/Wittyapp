import math
import os
import random
import struct
import wave
import zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUDIO_DIR = os.path.join(ROOT, "assets", "audio")
TEXTURE_DIR = os.path.join(ROOT, "assets", "textures")


def ensure_dirs():
    os.makedirs(AUDIO_DIR, exist_ok=True)
    os.makedirs(TEXTURE_DIR, exist_ok=True)


def write_wav(name, seconds, synth, sample_rate=22050):
    path = os.path.join(AUDIO_DIR, name)
    frames = []
    total = int(seconds * sample_rate)
    for i in range(total):
        t = i / sample_rate
        value = max(-1.0, min(1.0, synth(t, seconds)))
        frames.append(struct.pack("<h", int(value * 32767)))
    with wave.open(path, "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(sample_rate)
        wav.writeframes(b"".join(frames))


def tone(freq, decay=1.0, gain=0.4):
    return lambda t, s: math.sin(t * freq * math.tau) * gain * ((1 - t / s) ** decay)


def noise(seed, gain=0.2):
    rng = random.Random(seed)
    values = [rng.uniform(-1, 1) for _ in range(2048)]
    return lambda t, s: values[int(t * 22050) % len(values)] * gain


def write_png(name, width, height, pixel_func):
    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for x in range(width):
            r, g, b = pixel_func(x, y)
            raw.extend([r, g, b, 255])
    def chunk(kind, data):
        return (
            struct.pack(">I", len(data))
            + kind
            + data
            + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)
        )
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(os.path.join(TEXTURE_DIR, name), "wb") as f:
        f.write(png)


def generate_audio():
    write_wav("ui_hover.wav", 0.10, tone(880, 1.5, 0.16))
    write_wav("ui_click.wav", 0.12, lambda t, s: tone(520, 1.3, 0.20)(t, s) + tone(1040, 2.0, 0.08)(t, s))
    write_wav("footstep.wav", 0.18, lambda t, s: noise(7, 0.22)(t, s) * (1 - t / s))
    write_wav("door_open_close.wav", 0.45, lambda t, s: (noise(11, 0.12)(t, s) + math.sin(t * 80) * 0.10) * (1 - t / s))
    write_wav("object_throw_drop.wav", 0.32, lambda t, s: (noise(19, 0.18)(t, s) + math.sin(t * 180) * 0.06) * (1 - t / s))
    write_wav("distant_muffled_voice.wav", 1.35, lambda t, s: (math.sin(t * 120) + math.sin(t * 165)) * 0.08 * (0.6 + 0.4 * math.sin(t * 4)))
    write_wav("tense_ambient_loop.wav", 4.0, lambda t, s: (math.sin(t * 45) * 0.055 + math.sin(t * 72) * 0.035 + noise(23, 0.018)(t, s)))
    write_wav("low_background_music_loop.wav", 5.0, lambda t, s: (math.sin(t * 55) * 0.08 + math.sin(t * 82.5) * 0.045 + math.sin(t * 110) * 0.025) * (0.75 + 0.25 * math.sin(t * 1.2)))


def generate_textures():
    def concrete(x, y):
        rng = (x * 37 + y * 91 + (x ^ y) * 13) % 31
        base = 82 + rng
        crack = 34 if (x + y * 3) % 47 == 0 else 0
        return max(25, base - crack), max(27, base - crack + 4), max(28, base - crack + 5)

    def wood(x, y):
        band = int(30 * math.sin(x * 0.10 + math.sin(y * 0.06)))
        grain = (x * 17 + y * 5) % 19
        return 92 + band + grain, 58 + band // 2, 35

    def metal(x, y):
        scratch = 35 if (x * 5 + y * 11) % 73 == 0 else 0
        base = 70 + ((x * 3 + y * 7) % 20)
        return base + scratch, base + scratch + 4, base + scratch + 6

    write_png("concrete_wall.png", 128, 128, concrete)
    write_png("aged_wood.png", 128, 128, wood)
    write_png("dark_metal.png", 128, 128, metal)


if __name__ == "__main__":
    ensure_dirs()
    generate_audio()
    generate_textures()
    print("Generated audio and texture assets.")
