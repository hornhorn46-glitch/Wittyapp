extends SceneTree

const LOOK_SENSITIVITY := 0.0022
const WALK_EPSILON := 0.34
const MAX_WALK_FRAMES := 420
const RESULT_PATH := "res://artifacts/human_playtest_result.txt"
const SCREENSHOT_DIR := "res://artifacts/screenshots"

var main: Node
var world: Node
var player: Node
var enemy: Node
var civilian: Node
var failed := false

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path(SCREENSHOT_DIR))
	_write_result("started")
	var packed := load("res://scenes/Main.tscn")
	main = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	main._start_game()
	await process_frame
	await process_frame
	world = main.current_scene
	world._start_tutorial_from_intro()
	await process_frame
	player = get_nodes_in_group("player")[0]
	enemy = get_nodes_in_group("hostile")[0]
	civilian = get_nodes_in_group("civilian")[0]
	Input.set_mouse_mode(Input.MOUSE_MODE_CAPTURED)
	_release_all_actions()

	_frame_scene(Vector3(1.6, 1.4, 1.5))
	await _capture("human_01_start_chairs.png")

	var laptop := _nearest_group_node("throwable", Vector3(1.6, 0.45, 1.4))
	await _walk_to(Vector3(0.78, 0.35, 1.40), "walk to laptop")
	await _interact_with(laptop, "pick up laptop")
	_frame_scene(player.global_position + Vector3(-1.0, 0.0, 0.0), -0.04)
	_press_action("drop_item")

	var badge := get_nodes_in_group("key_item")[0]
	await _walk_to(Vector3(1.28, 0.35, -1.20), "walk to badge")
	_frame_scene(Vector3(1.25, 1.2, -1.95), -0.08)
	await _capture("human_02_badge_counter.png")
	await _interact_with(badge, "take badge")

	Input.action_press("crouch")
	var first_door := _nearest_interactable(Vector3(3.1, 0.0, -0.9), "first door")
	await _walk_to(Vector3(2.20, 0.35, -0.90), "walk to first door")
	_frame_scene(Vector3(3.10, 1.05, -0.90), -0.02)
	await _capture("human_03_first_door.png")
	await _interact_with(first_door, "open first door")
	await _walk_to(Vector3(4.10, 0.35, -0.92), "walk through first door")
	await _walk_to(Vector3(8.90, 0.35, -1.00), "walk corridor")
	_frame_scene(Vector3(9.80, 1.05, -1.00), -0.02)
	await _capture("human_04_corridor_locked_door.png")

	var locked_door := _nearest_interactable(Vector3(9.8, 0.0, -1.0), "locked door")
	await _interact_with(locked_door, "open locked door")
	await _walk_to(Vector3(10.55, 0.35, -1.05), "walk through locked door")
	await _walk_to(Vector3(10.85, 0.35, -1.45), "enter patrol room")
	_frame_scene(Vector3(13.20, 1.20, -2.80))
	await _capture("human_05_patrol_entry.png")
	await _walk_to(Vector3(10.85, 0.35, -3.35), "skirt cover")
	await _walk_to(Vector3(12.50, 0.35, -3.45), "cross patrol room")
	_frame_scene(Vector3(13.00, 1.15, -5.30), -0.02)
	await _capture("human_06_patrol_crossed.png")

	await _walk_to(Vector3(13.00, 0.35, -5.70), "enter rescue connector")
	await _walk_to(Vector3(13.00, 0.35, -6.55), "enter rescue room")
	_frame_scene(Vector3(14.35, 1.20, -7.75))
	await _capture("human_07_rescue_room_entry.png")
	await _interact_with(civilian, "guide civilian")
	await _walk_to(Vector3(13.30, 0.35, -7.35), "clear civilian corner")
	await _walk_to(Vector3(13.35, 0.35, -8.85), "through rescue furniture")
	_frame_scene(Vector3(13.00, 1.10, -10.25), -0.03)
	await _capture("human_08_rescue_furniture_path.png")
	await _walk_to(Vector3(13.10, 0.35, -9.85), "enter exit doorway")
	await _walk_to(Vector3(13.00, 0.35, -12.85), "safe zone", true)
	await _wait_for_rescue()
	_assert(civilian.is_rescued(), "civilian rescued")
	_frame_scene(Vector3(13.0, 1.20, -13.4))
	await _capture("human_09_exit_stairwell.png")
	world._on_exit_body_entered(player)
	await process_frame
	_frame_scene(Vector3(13.0, 1.20, -13.2))
	await _capture("human_10_result.png")
	_assert(world.game_over, "level ends")
	_release_all_actions()
	if not failed:
		_write_result("passed")
		print("Human playtest capture passed.")
		quit(0)

func _walk_to(target: Vector3, label: String, allow_game_over: bool = false) -> void:
	Input.action_press("move_forward")
	var frames := 0
	while _flat_distance(player.global_position, target) > WALK_EPSILON and frames < MAX_WALK_FRAMES:
		_face_point(target)
		await physics_frame
		frames += 1
	Input.action_release("move_forward")
	await physics_frame
	_assert(_flat_distance(player.global_position, target) <= WALK_EPSILON, "%s reached" % label)
	if not allow_game_over:
		_assert(not world.game_over, "%s did not fail" % label)

func _interact_with(target: Node, label: String) -> void:
	_assert(target != null, "%s target exists" % label)
	_face_point(_focus_point(target))
	for i in range(8):
		if player.has_method("_update_interaction_focus"):
			player._update_interaction_focus()
		await physics_frame
	_assert(player.get_focused_interactable() == target, "%s focused" % label)
	_press_action("interact")
	await physics_frame

func _press_action(action: String) -> void:
	Input.action_press(action)
	var event := InputEventAction.new()
	event.action = action
	event.pressed = true
	player._input(event)
	Input.action_release(action)

func _face_point(target: Vector3) -> void:
	var origin: Vector3 = player.camera.global_position
	var to_target := target - origin
	var flat := Vector2(to_target.x, to_target.z)
	if flat.length() > 0.001:
		var desired_yaw := atan2(-to_target.x, -to_target.z)
		var yaw_delta := wrapf(desired_yaw - player.rotation.y, -PI, PI)
		_send_mouse(Vector2(-yaw_delta / LOOK_SENSITIVITY, 0.0))
	var horizontal := Vector2(to_target.x, to_target.z).length()
	var desired_pitch := atan2(to_target.y, horizontal)
	var pitch_delta: float = desired_pitch - player.camera.rotation.x
	_send_mouse(Vector2(0.0, -pitch_delta / LOOK_SENSITIVITY))

func _frame_scene(target: Vector3, pitch: float = 0.02) -> void:
	var origin: Vector3 = player.camera.global_position
	_face_point(Vector3(target.x, origin.y, target.z))
	player.look_pitch = clamp(pitch, -1.25, 1.25)
	player.camera.rotation.x = player.look_pitch

func _send_mouse(relative: Vector2) -> void:
	var event := InputEventMouseMotion.new()
	event.relative = relative
	player._input(event)

func _focus_point(target: Node) -> Vector3:
	var node := target as Node3D
	if target.has_method("interaction_focus_point"):
		return target.interaction_focus_point(player)
	if target == civilian:
		return node.global_position + Vector3(0, 0.95, 0)
	if target.is_in_group("key_item"):
		return node.global_position + Vector3(0, 0.04, 0)
	if target.is_in_group("throwable"):
		return node.global_position + Vector3(0, 0.10, 0)
	return node.global_position + Vector3(0, 1.0, 0)

func _nearest_group_node(group_name: String, near_pos: Vector3) -> Node:
	var best: Node = null
	var best_distance := INF
	for node in get_nodes_in_group(group_name):
		if not (node is Node3D):
			continue
		var distance := (node as Node3D).global_position.distance_to(near_pos)
		if distance < best_distance:
			best_distance = distance
			best = node
	return best

func _nearest_interactable(near_pos: Vector3, label: String) -> Node:
	var best: Node = null
	var best_distance := INF
	for node in get_nodes_in_group("interactable"):
		if not (node is StaticBody3D):
			continue
		var distance := (node as Node3D).global_position.distance_to(near_pos)
		if distance < best_distance:
			best_distance = distance
			best = node
	_assert(best != null, "%s found" % label)
	return best

func _wait_for_rescue() -> void:
	for i in range(600):
		if civilian.is_rescued():
			return
		await physics_frame

func _capture(file_name: String) -> void:
	await process_frame
	var image := root.get_texture().get_image()
	image.save_png(SCREENSHOT_DIR + "/" + file_name)

func _flat_distance(a: Vector3, b: Vector3) -> float:
	return Vector2(a.x - b.x, a.z - b.z).length()

func _release_all_actions() -> void:
	for action in ["move_forward", "move_back", "move_left", "move_right", "crouch", "interact", "throw_item", "drop_item"]:
		Input.action_release(action)

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
		_release_all_actions()
		_write_result("failed: " + message)
		push_error("Human playtest assertion failed: " + message)
		quit(1)
