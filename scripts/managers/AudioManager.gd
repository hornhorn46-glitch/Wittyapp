extends Node

var music_player: AudioStreamPlayer
var ambient_player: AudioStreamPlayer
var ui_player: AudioStreamPlayer
var sfx_player: AudioStreamPlayer

var streams := {}
var rng := RandomNumberGenerator.new()

func _ready() -> void:
	rng.randomize()
	_ensure_bus("Music", "Master")
	_ensure_bus("SFX", "Master")
	music_player = _make_player("Music")
	ambient_player = _make_player("Music")
	ui_player = _make_player("SFX")
	sfx_player = _make_player("SFX")
	streams = {
		"ui_hover": _load_stream("res://assets/audio/ui_hover.wav"),
		"ui_click": _load_stream("res://assets/audio/ui_click.wav"),
		"footsteps": [
			_load_stream("res://assets/audio/kenney_footstep_00.ogg"),
			_load_stream("res://assets/audio/kenney_footstep_01.ogg"),
			_load_stream("res://assets/audio/kenney_footstep_02.ogg"),
			_load_stream("res://assets/audio/kenney_footstep_03.ogg"),
			_load_stream("res://assets/audio/kenney_footstep_04.ogg"),
		],
		"door_open": _load_stream("res://assets/audio/kenney_door_open.ogg"),
		"door_close": _load_stream("res://assets/audio/kenney_door_close.ogg"),
		"throw": _load_stream("res://assets/audio/object_throw_drop.wav"),
		"voice": _load_stream("res://assets/audio/distant_muffled_voice.wav"),
		"ambient": _load_stream("res://assets/audio/tense_ambient_loop.wav"),
		"music": _load_stream("res://assets/audio/low_background_music_loop.wav"),
		"level_music": _load_stream("res://assets/audio/kenney_level_music.ogg")
	}

func _load_stream(path: String) -> AudioStream:
	if path.get_extension().to_lower() == "ogg":
		return AudioStreamOggVorbis.load_from_file(path)
	var stream := load(path) as AudioStream
	if stream:
		return stream
	return null

func _ensure_bus(name: String, send: String) -> void:
	if AudioServer.get_bus_index(name) != -1:
		return
	AudioServer.add_bus()
	var idx := AudioServer.bus_count - 1
	AudioServer.set_bus_name(idx, name)
	AudioServer.set_bus_send(idx, send)

func _make_player(bus_name: String) -> AudioStreamPlayer:
	var player := AudioStreamPlayer.new()
	player.bus = bus_name
	add_child(player)
	return player

func start_menu_music() -> void:
	_play_loop(music_player, streams.get("music"), -14.0)

func start_level_ambient() -> void:
	_play_loop(ambient_player, streams.get("ambient"), -19.0)
	_play_loop(music_player, streams.get("level_music"), -21.0)

func stop_level_ambient() -> void:
	ambient_player.stop()
	music_player.stop()

func play_ui_hover() -> void:
	_play_one_shot(ui_player, streams.get("ui_hover"))

func play_ui_click() -> void:
	_play_one_shot(ui_player, streams.get("ui_click"))

func play_sfx(name: String) -> void:
	_play_one_shot(sfx_player, streams.get(name), -7.0, 1.0)

func play_footstep(crouched: bool = false) -> void:
	var variants: Array = streams.get("footsteps", [])
	if variants.is_empty():
		return
	var stream := variants[rng.randi_range(0, variants.size() - 1)] as AudioStream
	var volume := -19.0 if crouched else -13.5
	var pitch := rng.randf_range(0.92, 1.06)
	_play_one_shot(sfx_player, stream, volume, pitch)

func _play_loop(player: AudioStreamPlayer, stream: AudioStream, volume_db: float = 0.0) -> void:
	if not stream:
		return
	player.stream = stream
	player.volume_db = volume_db
	player.pitch_scale = 1.0
	player.play()

func _play_one_shot(player: AudioStreamPlayer, stream: AudioStream, volume_db: float = 0.0, pitch_scale: float = 1.0) -> void:
	if not stream:
		return
	player.stream = stream
	player.volume_db = volume_db
	player.pitch_scale = pitch_scale
	player.play()
