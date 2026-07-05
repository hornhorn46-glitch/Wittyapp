extends SceneTree

const RESULT_PATH := "res://artifacts/audio_system_result.txt"

var failed := false

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	_write_result("started")
	var audio: Node = root.get_node("/root/AudioManager")
	for key in [
		"music",
		"level_music",
		"ambient",
		"radiator_knock",
		"pipe_water",
		"phone_ring",
		"hostile_phone_argument",
		"throw",
		"door_open",
		"door_close",
	]:
		_assert(audio.streams.has(key), "Audio stream key exists: %s" % key)
		_assert(audio.streams.get(key) != null, "Audio stream loads: %s" % key)
	_assert((audio.streams.get("footsteps") as Array).size() >= 6, "Concrete footstep variants load")

	audio.start_menu_music()
	await process_frame
	_assert(audio.music_player.playing, "Menu music player starts")

	var packed := load("res://scenes/Main.tscn")
	var main: Node = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	main._start_game()
	await process_frame
	await process_frame
	var world: Node = main.current_scene
	world._start_tutorial_from_intro()
	await process_frame
	_assert(audio.music_player.playing, "Level music loop starts")
	_assert(audio.ambient_player.playing, "Level ambient loop starts")
	_assert(audio.ambient_event_timer.time_left > 0.0, "Ambient event timer is scheduled")

	var enemy: Node = get_nodes_in_group("hostile")[0]
	enemy.trigger_phone_call_for_test()
	await process_frame
	_assert(enemy.phone_call_active, "Hostile phone event becomes active")
	_assert(enemy.phone_prop.visible, "Hostile phone prop becomes visible")
	_assert(_has_spatial_audio(enemy), "Hostile phone ring creates spatial audio")
	if not failed:
		_write_result("passed")
		print("Audio system test passed.")
		quit(0)

func _has_spatial_audio(node: Node) -> bool:
	for child in node.get_children():
		if child is AudioStreamPlayer3D and child.playing:
			return true
	return false

func _write_result(text: String) -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path("res://artifacts"))
	var file := FileAccess.open(ProjectSettings.globalize_path(RESULT_PATH), FileAccess.WRITE)
	if file:
		file.store_string(text)

func _assert(condition: bool, message: String) -> void:
	if not condition:
		if failed:
			return
		failed = true
		_write_result("failed: " + message)
		push_error("Audio system assertion failed: " + message)
		quit(1)
