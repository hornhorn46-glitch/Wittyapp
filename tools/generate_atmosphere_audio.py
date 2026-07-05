import math
import os
import random
import struct
import wave


ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUDIO_DIR = os.path.join(ROOT, "assets", "audio")
SAMPLE_RATE = 22050


def clamp(value):
    return max(-1.0, min(1.0, value))


def envelope(t, seconds, attack=0.08, release=0.18):
    fade_in = min(1.0, t / max(0.001, attack))
    fade_out = min(1.0, (seconds - t) / max(0.001, release))
    return max(0.0, min(fade_in, fade_out))


def write_wav(name, seconds, synth):
    os.makedirs(AUDIO_DIR, exist_ok=True)
    path = os.path.join(AUDIO_DIR, name)
    frames = []
    total = int(seconds * SAMPLE_RATE)
    for i in range(total):
        t = i / SAMPLE_RATE
        value = clamp(synth(t, seconds))
        frames.append(struct.pack("<h", int(value * 32767)))
    with wave.open(path, "wb") as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(SAMPLE_RATE)
        wav.writeframes(b"".join(frames))


def noise(seed, gain=1.0, size=8192):
    rng = random.Random(seed)
    values = [rng.uniform(-1.0, 1.0) for _ in range(size)]

    def sample(t):
        index = int(t * SAMPLE_RATE) % size
        prev = values[index - 1]
        cur = values[index]
        nxt = values[(index + 1) % size]
        return (prev + cur * 2.0 + nxt) * 0.25 * gain

    return sample


ROOM_NOISE = noise(301, 0.020)
KNOCK_NOISE = noise(421, 0.018)
WATER_DRIP_NOISE = noise(532, 0.055)
WATER_FLOW_NOISE = noise(533, 0.014)
SPEECH_NOISE = noise(777, 0.018)


def menu_music(t, seconds):
    pulse = 0.62 + 0.38 * math.sin(t * math.tau * 0.11)
    bass = math.sin(t * math.tau * 48.0) * 0.050
    fifth = math.sin(t * math.tau * 72.0) * 0.026
    soft_top = math.sin(t * math.tau * 144.0 + math.sin(t * 0.7) * 0.4) * 0.010
    return (bass + fifth + soft_top) * pulse * envelope(t, seconds, 0.35, 0.45)


def communal_ambient(t, seconds):
    room = ROOM_NOISE(t)
    hum = math.sin(t * math.tau * 42.0) * 0.030 + math.sin(t * math.tau * 59.5) * 0.012
    far_pipe = math.sin(t * math.tau * 7.0 + math.sin(t * 0.63) * 1.7) * 0.010
    slow_wobble = 0.75 + 0.25 * math.sin(t * math.tau * 0.07)
    return (room + hum + far_pipe) * slow_wobble * envelope(t, seconds, 0.45, 0.55)


def radiator_knock(t, seconds):
    value = KNOCK_NOISE(t)
    for hit in [0.18, 0.43, 0.78, 1.18, 1.62]:
        dt = max(0.0, t - hit)
        if 0.0 <= dt < 0.24:
            value += math.sin(dt * math.tau * 185.0) * math.exp(-dt * 18.0) * 0.32
            value += math.sin(dt * math.tau * 92.0) * math.exp(-dt * 10.0) * 0.12
    return value * envelope(t, seconds, 0.02, 0.22)


def pipe_water(t, seconds):
    drip = 0.0
    for hit in [0.22, 0.91, 1.46, 2.18, 2.76, 3.32]:
        dt = max(0.0, t - hit)
        if 0.0 <= dt < 0.20:
            drip += math.sin(dt * math.tau * 760.0) * math.exp(-dt * 32.0) * 0.12
            drip += WATER_DRIP_NOISE(dt) * math.exp(-dt * 25.0)
    flow = WATER_FLOW_NOISE(t) + math.sin(t * math.tau * 13.0) * 0.007
    return (flow + drip) * envelope(t, seconds, 0.05, 0.35)


def phone_ring(t, seconds):
    cycle = t % 1.25
    active = 1.0 if cycle < 0.62 else 0.0
    tone_a = math.sin(t * math.tau * 440.0)
    tone_b = math.sin(t * math.tau * 523.25)
    tremolo = 0.76 + 0.24 * math.sin(t * math.tau * 18.0)
    return (tone_a * 0.10 + tone_b * 0.075) * active * tremolo * envelope(t, seconds, 0.03, 0.18)


def hostile_argument(t, seconds):
    # Abstract muffled speech-like sound: rhythmic vowels, no actual words.
    rhythm = 0.18 + 0.82 * max(0.0, math.sin(t * math.tau * 2.15 + 0.6 * math.sin(t * 1.7)))
    vowel = (
        math.sin(t * math.tau * 135.0 + math.sin(t * 2.0) * 2.0) * 0.090
        + math.sin(t * math.tau * 182.0) * 0.050
        + math.sin(t * math.tau * 245.0 + math.sin(t * 6.0)) * 0.026
    )
    irritation = math.sin(t * math.tau * 4.7) * 0.020
    breath = SPEECH_NOISE(t)
    return (vowel * rhythm + irritation + breath) * envelope(t, seconds, 0.12, 0.70)


def make_footstep(seed, thump_pitch, grit_gain):
    grit = noise(seed, grit_gain, 4096)

    def footstep(t, seconds):
        thump = math.sin(t * math.tau * thump_pitch) * math.exp(-t * 18.0) * 0.22
        heel = math.sin(t * math.tau * (thump_pitch * 1.85)) * math.exp(-t * 25.0) * 0.08
        scrape = grit(t) * math.exp(-t * 8.0)
        cloth = math.sin(t * math.tau * 34.0) * math.exp(-t * 11.0) * 0.035
        return (thump + heel + scrape + cloth) * envelope(t, seconds, 0.004, 0.10)

    return footstep


def soft_throw_drop(t, seconds):
    body = math.sin(t * math.tau * 78.0) * math.exp(-t * 9.0) * 0.20
    slap = noise(901, 0.12, 4096)(t) * math.exp(-t * 12.0)
    tail = math.sin(t * math.tau * 32.0) * math.exp(-t * 5.0) * 0.07
    return (body + slap + tail) * envelope(t, seconds, 0.004, 0.18)


def door_soft_open(t, seconds):
    scrape = noise(911, 0.055, 8192)(t) * (0.4 + 0.6 * math.sin(t * math.pi / seconds))
    hinge = math.sin(t * math.tau * (68.0 + 9.0 * math.sin(t * 2.4))) * 0.070
    wood = math.sin(t * math.tau * 31.0) * 0.035
    return (scrape + hinge + wood) * envelope(t, seconds, 0.02, 0.20)


def door_soft_close(t, seconds):
    contact_time = 0.52
    scrape = noise(912, 0.045, 8192)(t) * (1.0 - min(1.0, t / seconds) * 0.45)
    value = scrape
    dt = max(0.0, t - contact_time)
    if 0.0 <= dt < 0.30:
        value += math.sin(dt * math.tau * 62.0) * math.exp(-dt * 11.0) * 0.20
        value += noise(913, 0.09, 4096)(dt) * math.exp(-dt * 16.0)
    return value * envelope(t, seconds, 0.02, 0.24)


def game_music(t, seconds):
    root = math.sin(t * math.tau * 41.2) * 0.035
    minor = math.sin(t * math.tau * 61.8 + math.sin(t * 0.18) * 0.7) * 0.024
    high = math.sin(t * math.tau * 123.6 + math.sin(t * 0.33)) * 0.010
    pulse = 0.66 + 0.34 * math.sin(t * math.tau * 0.052)
    return (root + minor + high) * pulse * envelope(t, seconds, 0.45, 0.65)


def generate():
    write_wav("menu_unsettling_loop.wav", 12.0, menu_music)
    write_wav("game_unsettling_music_loop.wav", 18.0, game_music)
    write_wav("communal_room_ambient_loop.wav", 16.0, communal_ambient)
    write_wav("radiator_knock.wav", 2.15, radiator_knock)
    write_wav("pipe_water_noise.wav", 3.80, pipe_water)
    write_wav("phone_ring.wav", 3.80, phone_ring)
    write_wav("hostile_phone_argument.wav", 14.0, hostile_argument)
    write_wav("soft_throw_drop.wav", 0.46, soft_throw_drop)
    write_wav("door_soft_open.wav", 0.78, door_soft_open)
    write_wav("door_soft_close.wav", 0.76, door_soft_close)
    for i, pitch in enumerate([70.0, 76.0, 68.0, 82.0, 73.0, 79.0]):
        write_wav(f"footstep_concrete_{i:02d}.wav", 0.34, make_footstep(800 + i, pitch, 0.055 + i * 0.004))


if __name__ == "__main__":
    generate()
    print("Generated atmosphere audio assets.")
