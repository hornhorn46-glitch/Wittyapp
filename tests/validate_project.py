import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED_FILES = [
    "project.godot",
    "scenes/Main.tscn",
    "scripts/Main.gd",
    "scripts/managers/SettingsManager.gd",
    "scripts/managers/LocalizationManager.gd",
    "scripts/managers/AudioManager.gd",
    "scripts/player/PlayerController.gd",
    "scripts/world/GameWorld.gd",
    "scripts/world/EnemyAI.gd",
    "scripts/world/SecurityTerminal.gd",
    "scripts/world/ModelVisuals.gd",
    "scripts/world/RescueNPC.gd",
    "scripts/world/ThrowableItem.gd",
    "scripts/world/KeycardItem.gd",
    "scripts/world/TutorialSystem.gd",
    "scripts/ui/MainMenu.gd",
    "scripts/ui/SettingsMenu.gd",
    "scripts/ui/PauseMenu.gd",
    "tests/godot_audio_system.gd",
    "ASSET_CREDITS.md",
    "README.md",
]

REQUIRED_AUDIO = [
    "ui_hover.wav",
    "ui_click.wav",
    "footstep.wav",
    "door_open_close.wav",
    "object_throw_drop.wav",
    "distant_muffled_voice.wav",
    "tense_ambient_loop.wav",
    "low_background_music_loop.wav",
    "menu_unsettling_loop.wav",
    "game_unsettling_music_loop.wav",
    "communal_room_ambient_loop.wav",
    "radiator_knock.wav",
    "pipe_water_noise.wav",
    "phone_ring.wav",
    "hostile_phone_argument.wav",
    "soft_throw_drop.wav",
    "door_soft_open.wav",
    "door_soft_close.wav",
    "footstep_concrete_00.wav",
    "footstep_concrete_01.wav",
    "footstep_concrete_02.wav",
    "footstep_concrete_03.wav",
    "footstep_concrete_04.wav",
    "footstep_concrete_05.wav",
]

REQUIRED_EXTERNAL_ASSETS = [
    "assets/curated/textures/concrete_light.png",
    "assets/curated/textures/concrete_damaged.png",
    "assets/curated/textures/concrete_bunker.png",
    "assets/curated/textures/wood_subtle.png",
    "assets/curated/textures/floor_tiles_dirty.png",
    "assets/curated/models/characters/civilian.glb",
    "assets/curated/models/characters/hostile.glb",
    "assets/curated/models/factory/machine.glb",
    "assets/curated/models/factory/robot-arm-a.glb",
    "assets/curated/models/factory/conveyor-bars-stripe.glb",
    "assets/curated/models/factory/screen-panel-wide.glb",
    "assets/curated/models/factory/structure-window-wide.glb",
    "assets/curated/models/furniture/benchCushion.glb",
    "assets/curated/models/furniture/chairDesk.glb",
    "assets/curated/models/furniture/loungeSofaLong.glb",
    "assets/curated/models/furniture/radio.glb",
    "assets/curated/textures/pbr/Bricks097_color.jpg",
    "assets/curated/textures/pbr/Concrete048_color.jpg",
    "assets/curated/textures/pbr/Carpet016_color.jpg",
    "assets/curated/textures/pbr/PaintedPlaster017_color.jpg",
    "assets/curated/ui/button_normal.png",
]

REQUIRED_LOCALIZATION_KEYS = [
    "game.title",
    "menu.new_game",
    "menu.settings",
    "menu.credits",
    "menu.exit",
    "settings.master",
    "settings.music",
    "settings.sfx",
    "settings.fullscreen",
    "settings.resolution",
    "settings.language",
    "pause.resume",
    "pause.restart",
    "hint.move",
    "hint.crouch",
    "hint.pickup",
    "hint.security",
    "hint.security_disabled",
    "hint.throw",
    "hint.rescue",
    "hint.exit",
    "level.win",
    "level.fail",
    "intro.title",
    "intro.body",
    "intro.start",
    "minimap.title",
    "interact.open_door",
    "interact.locked_keycard",
    "interact.disable_security",
    "interact.security_disabled",
    "item.security_badge",
    "status.awareness",
    "status.searching",
]


def read(path):
    return (ROOT / path).read_text(encoding="utf-8")


def test_required_files_exist():
    missing = [path for path in REQUIRED_FILES if not (ROOT / path).exists()]
    assert not missing, f"Missing required files: {missing}"


def test_audio_assets_exist_and_are_nonempty():
    missing = []
    for name in REQUIRED_AUDIO:
        path = ROOT / "assets" / "audio" / name
        if not path.exists() or path.stat().st_size < 1000:
            missing.append(name)
    assert not missing, f"Missing or too-small audio assets: {missing}"


def test_external_visual_assets_exist():
    missing = [path for path in REQUIRED_EXTERNAL_ASSETS if not (ROOT / path).exists()]
    assert not missing, f"Missing required external visual assets: {missing}"


def test_localization_key_parity():
    en = json.loads(read("localization/en.json"))
    ru = json.loads(read("localization/ru.json"))
    assert set(en) == set(ru), "EN/RU localization keys differ"
    missing = [key for key in REQUIRED_LOCALIZATION_KEYS if key not in en]
    assert not missing, f"Missing required localization keys: {missing}"


def test_russian_localization_is_readable_utf8():
    ru_text = read("localization/ru.json")
    assert "Тихий выход" in ru_text
    assert any("А" <= char <= "я" or char == "ё" or char == "Ё" for char in ru_text)
    for marker in ["Рў", "СЃ", "вЂ", "Рџ"]:
        assert marker not in ru_text, f"Russian localization contains mojibake marker: {marker}"


def test_menu_buttons_and_settings_controls_are_declared():
    menu = read("scripts/ui/MainMenu.gd")
    settings = read("scripts/ui/SettingsMenu.gd")
    for key in ["menu.new_game", "menu.settings", "menu.credits", "menu.exit"]:
        assert key in menu
    for token in ["master_volume", "music_volume", "sfx_volume", "fullscreen", "resolution", "set_language"]:
        assert token in settings


def test_tutorial_level_contains_required_systems():
    world = read("scripts/world/GameWorld.gd")
    for token in ["StartRoom", "Corridor", "PatrolRoom", "RescueRoom", "SecurityOffice", "MaintenanceBay", "SafeZone", "ExitTrigger"]:
        assert token in world
    assert world.count("_create_room(") >= 5
    for token in [
        "PlayerController.gd",
        "EnemyAI.gd",
        "RescueNPC.gd",
        "ThrowableItem.gd",
        "KeycardItem.gd",
        "_create_window_lighting",
        "_create_reflection_probes",
        "_create_furniture_set",
        "_create_real_room_details",
        "_create_door_thresholds",
        "SecurityTerminal.gd",
        "Bricks097",
        "Carpet016",
        "security_badge",
    ]:
        assert token in world


def test_gameplay_scripts_cover_required_behaviors():
    player = read("scripts/player/PlayerController.gd")
    enemy = read("scripts/world/EnemyAI.gd")
    audio = read("scripts/managers/AudioManager.gd")
    item = read("scripts/world/ThrowableItem.gd")
    npc = read("scripts/world/RescueNPC.gd")
    door = read("scripts/world/Door.gd")
    for token in ["move_forward", "crouch", "interact", "throw_item", "drop_item", "give_item", "get_current_interaction_text"]:
        assert token in player
    for token in ["PATROL", "INVESTIGATE", "SEARCH", "ALERT", "DETECT_DISTANCE", "detection_multiplier", "phone_event_timer", "trigger_phone_call_for_test", "_on_sound_emitted", "_has_line_of_sight", "suspicion"]:
        assert token in enemy
    for token in ["menu_unsettling_loop", "communal_room_ambient_loop", "radiator_knock", "pipe_water", "play_spatial_sfx", "loop_players", "sfx_pool"]:
        assert token in audio
    terminal = read("scripts/world/SecurityTerminal.gd")
    for token in ["security_disabled", "interaction_text", "interact", "SecurityTerminalScreen"]:
        assert token in terminal
    assert "SoundEventSystem.emit_sound" in item
    assert "throw_loudness" in item
    for token in ["FEAR", "FOLLOW", "RESCUED", "is_rescued"]:
        assert token in npc
    for token in ["locked", "required_item", "interaction_text"]:
        assert token in door


if __name__ == "__main__":
    tests = [
        test_required_files_exist,
        test_audio_assets_exist_and_are_nonempty,
        test_external_visual_assets_exist,
        test_localization_key_parity,
        test_russian_localization_is_readable_utf8,
        test_menu_buttons_and_settings_controls_are_declared,
        test_tutorial_level_contains_required_systems,
        test_gameplay_scripts_cover_required_behaviors,
    ]
    for test in tests:
        test()
    print(f"{len(tests)} validation tests passed.")
