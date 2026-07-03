extends SceneTree

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	var packed := load("res://scenes/Main.tscn")
	_assert(packed != null, "Main scene loads")
	var main: Node = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	_assert(main.current_scene != null, "Main menu scene created")
	_assert(main.current_scene.has_signal("new_game_requested"), "Main menu exposes new game flow")
	main._start_game()
	await process_frame
	await process_frame
	var world: Node = main.current_scene
	_assert(world.name == "TutorialLevel", "Tutorial level starts")
	_assert(get_nodes_in_group("player").size() == 1, "Player exists")
	_assert(get_nodes_in_group("hostile").size() == 1, "Hostile NPC exists")
	_assert(get_nodes_in_group("civilian").size() == 1, "Civilian NPC exists")
	_assert(get_nodes_in_group("throwable").size() >= 3, "Throwable items exist")
	_assert(get_nodes_in_group("room_zone").size() >= 4, "Multiple level zones exist")
	var enemy = get_nodes_in_group("hostile")[0]
	var before_state = enemy.state
	root.get_node("/root/SoundEventSystem").emit_sound(enemy.global_position, 20.0, world)
	await process_frame
	_assert(enemy.state != before_state or enemy.state == 2, "Enemy reacts to sound event")
	print("Godot smoke test passed.")
	quit(0)

func _assert(condition: bool, message: String) -> void:
	if not condition:
		push_error("Smoke assertion failed: " + message)
		quit(1)
