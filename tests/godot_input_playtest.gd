extends SceneTree

const LOOK_SENSITIVITY := 0.0022
const WALK_EPSILON := 0.34
const MAX_WALK_FRAMES := 360
const RESULT_PATH := "res://artifacts/input_playtest_result.txt"

var main: Node
var world: Node
var player: Node
var enemy: Node
var civilian: Node
var failed := false

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
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

	var yaw_before: float = player.rotation.y
	_send_mouse(Vector2(80, -30))
	_assert(abs(player.rotation.y - yaw_before) > 0.01, "Mouse-look rotates the player")

	var laptop := _nearest_group_node("throwable", Vector3(1.6, 0.45, 1.4))
	await _walk_to(Vector3(0.78, 0.35, 1.40), "walk to laptop")
	await _interact_with(laptop, "pick up laptop with E")
	_assert(player.held_item == laptop, "Laptop is held after E interaction")
	_face_point(player.global_position + Vector3(-1.0, 0.5, 0.0))
	await process_frame
	_press_action("drop_item")
	await process_frame
	_assert(player.held_item == null, "Drop action releases held item")

	var badge := get_nodes_in_group("key_item")[0]
	await _walk_to(Vector3(1.28, 0.35, -1.20), "walk to badge counter")
	await _interact_with(badge, "take security badge with E")
	_assert(player.has_item("security_badge"), "Security badge added to inventory")

	var first_door := _nearest_interactable(Vector3(3.1, 0.0, -0.9), "first door")
	Input.action_press("crouch")
	await _walk_to(Vector3(2.20, 0.35, -0.90), "walk to first door")
	await _interact_with(first_door, "open first door with E")
	_assert(bool(first_door.get("opened")), "First door opens")
	await _walk_to(Vector3(4.10, 0.35, -0.92), "walk through first door")

	await _walk_to(Vector3(8.90, 0.35, -1.00), "crouch-walk to locked door")
	var locked_door := _nearest_interactable(Vector3(9.8, 0.0, -1.0), "locked security door")
	await _interact_with(locked_door, "open locked door with badge and E")
	_assert(bool(locked_door.get("opened")), "Locked door opens after badge")
	await _walk_to(Vector3(10.55, 0.35, -1.05), "walk through locked door")

	await _walk_to(Vector3(10.85, 0.35, -1.45), "enter patrol room")
	await _walk_to(Vector3(10.85, 0.35, -3.35), "skirt patrol cover")
	await _walk_to(Vector3(12.50, 0.35, -3.45), "cross patrol room")
	await _walk_to(Vector3(13.00, 0.35, -5.70), "enter rescue connector")
	await _walk_to(Vector3(13.00, 0.35, -6.55), "enter rescue room")
	await _interact_with(civilian, "guide civilian with E")
	_assert(civilian.state == 1, "Civilian starts following")

	await _walk_to(Vector3(13.30, 0.35, -7.35), "clear civilian corner")
	await _walk_to(Vector3(13.35, 0.35, -8.85), "lead civilian through rescue room")
	await _walk_to(Vector3(13.10, 0.35, -9.85), "reach exit doorway threshold")
	await _walk_to(Vector3(13.00, 0.35, -12.85), "lead civilian to stairwell safe zone", true)
	await _wait_for_rescue()
	_assert(civilian.is_rescued(), "Civilian reaches rescued state; %s" % _debug_state(world.safe_zone_pos))
	world._on_exit_body_entered(player)
	await process_frame
	_assert(world.game_over, "Exit trigger completes the level; %s" % _debug_state(world.safe_zone_pos))
	_release_all_actions()
	if not failed:
		_write_result("passed")
		print("Input-driven playtest passed.")
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
	_assert(_flat_distance(player.global_position, target) <= WALK_EPSILON, "%s reached; %s" % [label, _debug_state(target)])
	if not allow_game_over:
		_assert(not world.game_over, "%s did not fail the level; %s" % [label, _debug_state(target)])

func _interact_with(target: Node, label: String) -> void:
	_assert(target != null, "%s target exists" % label)
	var focus_point := _focus_point(target)
	_face_point(focus_point)
	for i in range(8):
		if player.has_method("_update_interaction_focus"):
			player._update_interaction_focus()
		await physics_frame
	var focused: Node = player.get_focused_interactable()
	_assert(focused == target, "%s is focused" % label)
	_assert(str(player.get_current_interaction_text()) != "", "%s prompt is visible" % label)
	_press_action("interact")
	await physics_frame

func _wait_for_rescue() -> void:
	for i in range(600):
		if civilian.is_rescued():
			return
		await physics_frame

func _press_action(action: String) -> void:
	Input.action_press(action)
	var event := InputEventAction.new()
	event.action = action
	event.pressed = true
	player._unhandled_input(event)
	Input.action_release(action)

func _send_mouse(relative: Vector2) -> void:
	var event := InputEventMouseMotion.new()
	event.relative = relative
	player._unhandled_input(event)

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
		if not (node is Node3D):
			continue
		if not node.has_method("interaction_text") or not node.has_method("interact"):
			continue
		if not (node is StaticBody3D):
			continue
		var distance := (node as Node3D).global_position.distance_to(near_pos)
		if distance < best_distance:
			best_distance = distance
			best = node
	_assert(best != null, "%s found" % label)
	return best

func _flat_distance(a: Vector3, b: Vector3) -> float:
	return Vector2(a.x - b.x, a.z - b.z).length()

func _release_all_actions() -> void:
	for action in ["move_forward", "move_back", "move_left", "move_right", "crouch", "interact", "throw_item", "drop_item"]:
		Input.action_release(action)

func _debug_state(target: Vector3) -> String:
	var civilian_collision_disabled := false
	if civilian and civilian.get("collision_shape"):
		civilian_collision_disabled = bool(civilian.collision_shape.disabled)
	return "player=%s target=%s enemy=%s enemy_state=%s suspicion=%.2f civilian=%s civilian_state=%s civilian_collision_disabled=%s" % [
		str(player.global_position),
		str(target),
		str(enemy.global_position if enemy else Vector3.ZERO),
		str(enemy.state if enemy else -1),
		float(enemy.suspicion if enemy else 0.0),
		str(civilian.global_position if civilian else Vector3.ZERO),
		str(civilian.state if civilian else -1),
		str(civilian_collision_disabled)
	]

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
		push_error("Input playtest assertion failed: " + message)
		quit(1)
