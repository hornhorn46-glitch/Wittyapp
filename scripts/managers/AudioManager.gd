extends Node

var music_player: AudioStreamPlayer
var ambient_player: AudioStreamPlayer
var ui_player: AudioStreamPlayer
var sfx_player: AudioStreamPlayer

var streams := {}

func _ready() -> void:
	_ensure_bus("Music", "Master")
	_ensure_bus("SFX", "Master")
	music_player = _make_player("Music")
	ambient_player = _make_player("Music")
	ui_player = _make_player("SFX")
	sfx_player = _make_player("SFX")
	streams = {
		"ui_hover": load("res://assets/audio/ui_hover.wav"),
		"ui_click": load("res://assets/audio/ui_click.wav"),
		"footstep": load("res://assets/audio/footstep.wav"),
		"door": load("res://assets/audio/door_open_close.wav"),
		"throw": load("res://assets/audio/object_throw_drop.wav"),
		"voice": load("res://assets/audio/distant_muffled_voice.wav"),
		"ambient": load("res://assets/audio/tense_ambient_loop.wav"),
		"music": load("res://assets/audio/low_background_music_loop.wav")
	}

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
	_play_loop(music_player, streams.get("music"))

func start_level_ambient() -> void:
	_play_loop(ambient_player, streams.get("ambient"))

func stop_level_ambient() -> void:
	ambient_player.stop()

func play_ui_hover() -> void:
	_play_one_shot(ui_player, streams.get("ui_hover"))

func play_ui_click() -> void:
	_play_one_shot(ui_player, streams.get("ui_click"))

func play_sfx(name: String) -> void:
	_play_one_shot(sfx_player, streams.get(name))

func _play_loop(player: AudioStreamPlayer, stream: AudioStream) -> void:
	if not stream:
		return
	player.stream = stream
	player.play()

func _play_one_shot(player: AudioStreamPlayer, stream: AudioStream) -> void:
	if not stream:
		return
	player.stream = stream
	player.play()

