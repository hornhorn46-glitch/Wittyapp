extends SceneTree

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path("res://artifacts/screenshots"))
	_force_russian()
	var packed := load("res://scenes/Main.tscn")
	var main: Node = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	_force_russian()
	await _save_viewport("menu.png")
	main._start_game()
	await process_frame
	await process_frame
	await _save_viewport("intro_legend.png")
	main.current_scene._start_tutorial_from_intro()
	await process_frame
	await _save_viewport("gameplay_start.png")
	var player: Node3D = get_nodes_in_group("player")[0]
	var enemy: Node = get_nodes_in_group("hostile")[0]
	enemy.player = null
	player.global_position = Vector3(-1.5, 0.35, 1.4)
	player.set_physics_process(false)
	enemy.global_position = Vector3(13.0, 0.35, -0.7)
	enemy.set_physics_process(false)
	var capture_camera := _set_capture_camera(main.current_scene, Vector3(15.45, 1.38, -0.15), Vector3(12.95, 0.78, -0.75), 70.0)
	await process_frame
	await _save_viewport("patrol_room.png")
	capture_camera.queue_free()
	player.global_position = Vector3(11.7, 0.35, -6.0)
	var civilian: Node3D = get_nodes_in_group("civilian")[0]
	civilian.global_position = Vector3(13.4, 0.35, -7.45)
	civilian.set_physics_process(false)
	capture_camera = _set_capture_camera(main.current_scene, Vector3(11.2, 1.55, -10.0), Vector3(13.45, 0.82, -7.55), 74.0)
	await process_frame
	await _save_viewport("rescue_room.png")
	capture_camera.queue_free()
	main.current_scene._set_paused(true)
	await process_frame
	await _save_viewport("pause.png")
	print("Visual captures saved.")
	quit(0)

func _save_viewport(file_name: String) -> void:
	await process_frame
	var image := root.get_texture().get_image()
	image.save_png("res://artifacts/screenshots/" + file_name)

func _set_capture_camera(parent: Node, position: Vector3, target: Vector3, fov: float) -> Camera3D:
	var camera := Camera3D.new()
	camera.name = "CaptureCamera"
	camera.fov = fov
	camera.near = 0.03
	parent.add_child(camera)
	camera.global_position = position
	camera.look_at(target, Vector3.UP)
	camera.current = true
	return camera

func _force_russian() -> void:
	var settings := root.get_node_or_null("/root/SettingsManager")
	if settings:
		settings.language = "ru"
	var localization := root.get_node_or_null("/root/LocalizationManager")
	if localization and localization.has_method("set_language"):
		localization.set_language("ru")
