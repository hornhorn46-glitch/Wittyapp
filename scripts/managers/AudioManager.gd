extends Node

var music_player: AudioStreamPlayer
var ambient_player: AudioStreamPlayer
var ui_player: AudioStreamPlayer
var sfx_player: AudioStreamPlayer
var sfx_pool: Array[AudioStreamPlayer] = []
var loop_players: Array[AudioStreamPlayer] = []
var ambient_event_timer: Timer

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
	for i in range(8):
		sfx_pool.append(_make_player("SFX"))
	ambient_event_timer = Timer.new()
	ambient_event_timer.one_shot = true
	ambient_event_timer.timeout.connect(_on_ambient_event_timeout)
	add_child(ambient_event_timer)
	streams = {
		"ui_hover": _load_stream("res://assets/audio/ui_hover.wav"),
		"ui_click": _load_stream("res://assets/audio/ui_click.wav"),
		"footsteps": _load_many([
			"res://assets/audio/footstep_concrete_00.wav",
			"res://assets/audio/footstep_concrete_01.wav",
			"res://assets/audio/footstep_concrete_02.wav",
			"res://assets/audio/footstep_concrete_03.wav",
			"res://assets/audio/footstep_concrete_04.wav",
			"res://assets/audio/footstep_concrete_05.wav",
			"res://assets/audio/kenney_footstep_00.ogg",
			"res://assets/audio/kenney_footstep_01.ogg",
			"res://assets/audio/kenney_footstep_02.ogg",
			"res://assets/audio/kenney_footstep_03.ogg",
			"res://assets/audio/kenney_footstep_04.ogg",
		]),
		"door_open": _load_stream("res://assets/audio/door_soft_open.wav"),
		"door_close": _load_stream("res://assets/audio/door_soft_close.wav"),
		"throw": _load_stream("res://assets/audio/soft_throw_drop.wav"),
		"voice": _load_stream("res://assets/audio/distant_muffled_voice.wav"),
		"ambient": _load_stream("res://assets/audio/communal_room_ambient_loop.wav"),
		"music": _load_stream("res://assets/audio/menu_unsettling_loop.wav"),
		"level_music": _load_stream("res://assets/audio/game_unsettling_music_loop.wav"),
		"radiator_knock": _load_stream("res://assets/audio/radiator_knock.wav"),
		"pipe_water": _load_stream("res://assets/audio/pipe_water_noise.wav"),
		"phone_ring": _load_stream("res://assets/audio/phone_ring.wav"),
		"hostile_phone_argument": _load_stream("res://assets/audio/hostile_phone_argument.wav"),
	}

func _load_stream(path: String) -> AudioStream:
	if path.get_extension().to_lower() == "ogg":
		return AudioStreamOggVorbis.load_from_file(path)
	if path.get_extension().to_lower() == "wav":
		var imported_stream := load(path) as AudioStream
		if imported_stream:
			return imported_stream
		return _load_wav_stream(path)
	var stream := load(path) as AudioStream
	if stream:
		return stream
	return null

func _load_wav_stream(path: String) -> AudioStreamWAV:
	var bytes := FileAccess.get_file_as_bytes(path)
	if bytes.size() < 44:
		return null
	if bytes.slice(0, 4).get_string_from_ascii() != "RIFF" or bytes.slice(8, 12).get_string_from_ascii() != "WAVE":
		return null
	var channels := 1
	var sample_rate := 22050
	var bits_per_sample := 16
	var data := PackedByteArray()
	var offset := 12
	while offset + 8 <= bytes.size():
		var chunk_id := bytes.slice(offset, offset + 4).get_string_from_ascii()
		var chunk_size := _read_u32(bytes, offset + 4)
		var chunk_data := offset + 8
		if chunk_id == "fmt " and chunk_data + 16 <= bytes.size():
			channels = _read_u16(bytes, chunk_data + 2)
			sample_rate = _read_u32(bytes, chunk_data + 4)
			bits_per_sample = _read_u16(bytes, chunk_data + 14)
		elif chunk_id == "data":
			data = bytes.slice(chunk_data, min(chunk_data + chunk_size, bytes.size()))
			break
		offset = chunk_data + chunk_size + (chunk_size % 2)
	if data.is_empty():
		return null
	var stream := AudioStreamWAV.new()
	stream.mix_rate = sample_rate
	stream.stereo = channels == 2
	stream.format = AudioStreamWAV.FORMAT_16_BITS if bits_per_sample == 16 else AudioStreamWAV.FORMAT_8_BITS
	stream.data = data
	return stream

func _read_u16(bytes: PackedByteArray, offset: int) -> int:
	return int(bytes[offset]) | (int(bytes[offset + 1]) << 8)

func _read_u32(bytes: PackedByteArray, offset: int) -> int:
	return int(bytes[offset]) | (int(bytes[offset + 1]) << 8) | (int(bytes[offset + 2]) << 16) | (int(bytes[offset + 3]) << 24)

func _load_many(paths: Array) -> Array[AudioStream]:
	var result: Array[AudioStream] = []
	for path in paths:
		var stream := _load_stream(str(path))
		if stream:
			result.append(stream)
	return result

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
	if ambient_event_timer:
		ambient_event_timer.stop()
	_stop_loop(ambient_player)
	_play_loop(music_player, streams.get("music"), -13.0)

func start_level_ambient() -> void:
	_play_loop(ambient_player, streams.get("ambient"), -17.0)
	_play_loop(music_player, streams.get("level_music"), -23.0)
	_schedule_ambient_event()

func stop_level_ambient() -> void:
	if ambient_event_timer:
		ambient_event_timer.stop()
	_stop_loop(ambient_player)
	_stop_loop(music_player)

func play_ui_hover() -> void:
	_play_one_shot(ui_player, streams.get("ui_hover"))

func play_ui_click() -> void:
	_play_one_shot(ui_player, streams.get("ui_click"))

func play_sfx(name: String, volume_db: float = 999.0, pitch_scale: float = 1.0) -> void:
	var volume := _default_sfx_volume(name) if volume_db > 900.0 else volume_db
	_play_one_shot(_next_sfx_player(), streams.get(name), volume, pitch_scale)

func play_footstep(crouched: bool = false) -> void:
	var variants: Array = streams.get("footsteps", [])
	if variants.is_empty():
		return
	var stream := variants[rng.randi_range(0, variants.size() - 1)] as AudioStream
	var volume := -23.0 if crouched else -16.0
	var pitch := rng.randf_range(0.94, 1.05)
	_play_one_shot(_next_sfx_player(), stream, volume, pitch)

func play_spatial_sfx(name: String, position: Vector3, parent: Node = null, volume_db: float = -8.0, pitch_scale: float = 1.0, max_distance: float = 18.0) -> void:
	var stream := streams.get(name) as AudioStream
	if not stream:
		return
	var player := AudioStreamPlayer3D.new()
	player.bus = "SFX"
	player.stream = stream
	player.volume_db = volume_db
	player.pitch_scale = pitch_scale
	player.max_distance = max_distance
	player.attenuation_model = AudioStreamPlayer3D.ATTENUATION_INVERSE_DISTANCE
	var parent_node := parent
	if not parent_node:
		parent_node = get_tree().current_scene
	if parent_node:
		parent_node.add_child(player)
	else:
		add_child(player)
	player.global_position = position
	player.finished.connect(player.queue_free)
	player.play()

func _process(_delta: float) -> void:
	for player in loop_players:
		if player and player.stream and not player.playing:
			player.play()

func _play_loop(player: AudioStreamPlayer, stream: AudioStream, volume_db: float = 0.0) -> void:
	if not stream:
		return
	if player.stream == stream and player.playing:
		return
	player.stream = stream
	player.volume_db = volume_db
	player.pitch_scale = 1.0
	if not loop_players.has(player):
		loop_players.append(player)
	player.play()

func _stop_loop(player: AudioStreamPlayer) -> void:
	if not player:
		return
	loop_players.erase(player)
	player.stop()

func _play_one_shot(player: AudioStreamPlayer, stream: AudioStream, volume_db: float = 0.0, pitch_scale: float = 1.0) -> void:
	if not stream:
		return
	player.stream = stream
	player.volume_db = volume_db
	player.pitch_scale = pitch_scale
	player.play()

func _next_sfx_player() -> AudioStreamPlayer:
	for player in sfx_pool:
		if not player.playing:
			return player
	return sfx_pool[rng.randi_range(0, sfx_pool.size() - 1)] if not sfx_pool.is_empty() else sfx_player

func _default_sfx_volume(name: String) -> float:
	match name:
		"door_open", "door_close":
			return -10.0
		"throw":
			return -9.0
		"radiator_knock":
			return -15.5
		"pipe_water":
			return -17.0
		"voice", "hostile_phone_argument":
			return -12.0
		"phone_ring":
			return -7.5
		_:
			return -8.0

func _schedule_ambient_event() -> void:
	if not ambient_event_timer:
		return
	ambient_event_timer.start(rng.randf_range(18.0, 46.0))

func _on_ambient_event_timeout() -> void:
	if not ambient_player or not ambient_player.playing:
		return
	var name := "radiator_knock" if rng.randf() < 0.56 else "pipe_water"
	play_sfx(name, _default_sfx_volume(name), rng.randf_range(0.94, 1.06))
	_schedule_ambient_event()
