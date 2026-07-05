extends SceneTree

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
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
	var player := get_nodes_in_group("player")[0]
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
	var click_event := InputEventMouseButton.new()
	click_event.button_index = MOUSE_BUTTON_LEFT
	click_event.pressed = true
	player._input(click_event)
	var yaw_before: float = player.rotation.y
	var pitch_before: float = player.camera.rotation.x
	var mouse_event := InputEventMouseMotion.new()
	mouse_event.relative = Vector2(120, -45)
	player._input(mouse_event)
	_assert(abs(player.rotation.y - yaw_before) > 0.01, "Mouse motion changes player yaw")
	_assert(abs(player.camera.rotation.x - pitch_before) > 0.01, "Mouse motion changes camera pitch")
	player.global_position = Vector3(1.2, 0.35, -0.9)
	player.rotation_degrees.y = -90.0
	player.camera.rotation.x = 0.0
	await process_frame
	player._update_interaction_focus()
	_assert(str(player.get_current_interaction_text()) != "", "Door interaction prompt appears near center focus")
	print("Controls and interaction test passed.")
	quit(0)

func _assert(condition: bool, message: String) -> void:
	if not condition:
		push_error("Controls assertion failed: " + message)
		quit(1)
