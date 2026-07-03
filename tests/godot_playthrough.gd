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
	var player: Node = get_nodes_in_group("player")[0]
	var enemy: Node = get_nodes_in_group("hostile")[0]
	var civilian: Node = get_nodes_in_group("civilian")[0]
	var item: Node = get_nodes_in_group("throwable")[0]
	player.pick_up(item)
	_assert(player.held_item == item, "Player can pick up a throwable")
	player._throw_held_item()
	await process_frame
	_assert(enemy.state == 2, "Thrown item emits sound and enemy investigates")
	civilian.interact(player)
	await process_frame
	_assert(civilian.state == 1, "Civilian starts following after interaction")
	civilian.global_position = world.safe_zone_pos
	await process_frame
	civilian._physics_process(0.016)
	_assert(civilian.is_rescued(), "Civilian reaches rescued state in safe zone")
	world._on_exit_body_entered(player)
	await process_frame
	_assert(world.game_over, "Exit trigger completes the level after rescue")
	print("Godot playthrough simulation passed.")
	quit(0)

func _assert(condition: bool, message: String) -> void:
	if not condition:
		push_error("Playthrough assertion failed: " + message)
		quit(1)
