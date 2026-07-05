extends SceneTree

const RESULT_PATH := "res://artifacts/security_terminal_result.txt"

var failed := false

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	_write_result("started")
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
	var terminal: Node = world.security_terminal
	_assert(terminal != null, "Security terminal exists")
	_assert(terminal.is_in_group("interactable"), "Security terminal is interactable")
	_assert(str(terminal.interaction_text(player)) != "", "Security terminal exposes prompt text")
	var before_multiplier: float = enemy.detection_multiplier
	terminal.interact(player)
	await process_frame
	_assert(world.security_system_disabled, "World records disabled security loop")
	_assert(bool(terminal.get("disabled_state")), "Terminal stores disabled state")
	_assert(enemy.detection_multiplier < before_multiplier, "Enemy detection multiplier is reduced")
	_assert(str(terminal.interaction_text(player)) == root.get_node("/root/LocalizationManager").text("interact.security_disabled"), "Terminal prompt changes after use")
	if not failed:
		_write_result("passed")
		print("Security terminal test passed.")
		quit(0)

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
		push_error("Security terminal assertion failed: " + message)
		quit(1)
