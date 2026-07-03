extends Node

signal settings_changed

const CONFIG_PATH := "user://settings.cfg"

var master_volume := 0.85
var music_volume := 0.65
var sfx_volume := 0.85
var fullscreen := false
var resolution := Vector2i(1280, 720)
var language := "en"

func load_settings() -> void:
	var config := ConfigFile.new()
	var err := config.load(CONFIG_PATH)
	if err != OK:
		return
	master_volume = float(config.get_value("audio", "master_volume", master_volume))
	music_volume = float(config.get_value("audio", "music_volume", music_volume))
	sfx_volume = float(config.get_value("audio", "sfx_volume", sfx_volume))
	fullscreen = bool(config.get_value("display", "fullscreen", fullscreen))
	var width := int(config.get_value("display", "width", resolution.x))
	var height := int(config.get_value("display", "height", resolution.y))
	resolution = Vector2i(width, height)
	language = str(config.get_value("game", "language", language))

func save_settings() -> void:
	var config := ConfigFile.new()
	config.set_value("audio", "master_volume", master_volume)
	config.set_value("audio", "music_volume", music_volume)
	config.set_value("audio", "sfx_volume", sfx_volume)
	config.set_value("display", "fullscreen", fullscreen)
	config.set_value("display", "width", resolution.x)
	config.set_value("display", "height", resolution.y)
	config.set_value("game", "language", language)
	config.save(CONFIG_PATH)

func apply_settings() -> void:
	_set_bus_volume("Master", master_volume)
	_set_bus_volume("Music", music_volume)
	_set_bus_volume("SFX", sfx_volume)
	DisplayServer.window_set_size(resolution)
	DisplayServer.window_set_mode(DisplayServer.WINDOW_MODE_FULLSCREEN if fullscreen else DisplayServer.WINDOW_MODE_WINDOWED)
	save_settings()
	settings_changed.emit()

func set_language(value: String) -> void:
	language = value
	LocalizationManager.set_language(language)
	apply_settings()

func _set_bus_volume(bus_name: String, linear: float) -> void:
	var idx := AudioServer.get_bus_index(bus_name)
	if idx == -1:
		return
	AudioServer.set_bus_mute(idx, linear <= 0.001)
	AudioServer.set_bus_volume_db(idx, linear_to_db(clamp(linear, 0.001, 1.0)))

