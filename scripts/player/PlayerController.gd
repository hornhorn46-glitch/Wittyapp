extends CharacterBody3D

signal crouch_changed(is_crouched: bool)
signal item_picked
signal item_thrown
signal interacted
signal focus_changed(text: String)
signal inventory_changed(item_id: String)

const WALK_SPEED := 3.5
const CROUCH_SPEED := 1.55
const LOOK_SENSITIVITY := 0.0022
const THROW_FORCE := 8.0
const INTERACT_DISTANCE := 3.2
const INTERACT_FOCUS_DOT := 0.82
const MANUAL_MOUSE_DEADZONE_SQUARED := 0.25

var camera: Camera3D
var interact_ray: RayCast3D
var held_item: Node3D
var is_crouched := false
var look_pitch := 0.0
var footstep_timer := 0.0
var inventory := {}
var current_focus_text := ""
var gameplay_input_enabled := true
var manual_mouse_position := Vector2.ZERO
var manual_mouse_initialized := false
var mouse_event_cooldown_frames := 0

func _ready() -> void:
	name = "Player"
	add_to_group("player")
	_create_body()
	_capture_mouse_for_gameplay()
	call_deferred("_capture_mouse_for_gameplay")
	set_process(true)
	set_process_input(true)
	set_process_unhandled_input(true)

func _create_body() -> void:
	var collision := CollisionShape3D.new()
	var capsule := CapsuleShape3D.new()
	capsule.radius = 0.32
	capsule.height = 1.65
	collision.shape = capsule
	add_child(collision)
	camera = Camera3D.new()
	camera.current = true
	camera.position = Vector3(0, 0.68, 0)
	add_child(camera)
	interact_ray = RayCast3D.new()
	interact_ray.target_position = Vector3(0, 0, -INTERACT_DISTANCE)
	interact_ray.collide_with_areas = true
	camera.add_child(interact_ray)

func _input(event: InputEvent) -> void:
	if _handle_gameplay_input(event):
		get_viewport().set_input_as_handled()

func _unhandled_input(event: InputEvent) -> void:
	_handle_gameplay_input(event)

func _handle_gameplay_input(event: InputEvent) -> bool:
	if not gameplay_input_enabled or not is_inside_tree() or get_tree().paused:
		return false
	if event is InputEventMouseButton and event.pressed and not _is_gameplay_mouse_mode():
		_capture_mouse_for_gameplay()
		return true
	if event is InputEventMouseMotion and _can_look_with_mouse():
		handle_mouse_motion(event.relative)
		return true
	if event.is_action_pressed("interact"):
		_try_interact()
		return true
	if event.is_action_pressed("throw_item"):
		_throw_held_item()
		return true
	if event.is_action_pressed("drop_item"):
		_drop_held_item()
		return true
	return false

func _can_look_with_mouse() -> bool:
	return gameplay_input_enabled and is_inside_tree() and not get_tree().paused

func _capture_mouse_for_gameplay() -> void:
	if gameplay_input_enabled and is_inside_tree() and not get_tree().paused:
		Input.set_mouse_mode(Input.MOUSE_MODE_CAPTURED if _is_headless_display() else Input.MOUSE_MODE_CONFINED_HIDDEN)
		_reset_manual_mouse_tracking()

func force_capture_mouse() -> void:
	_capture_mouse_for_gameplay()

func set_gameplay_input_enabled(enabled: bool) -> void:
	gameplay_input_enabled = enabled
	if not enabled:
		current_focus_text = ""
		manual_mouse_initialized = false
		focus_changed.emit(current_focus_text)
	else:
		call_deferred("_capture_mouse_for_gameplay")

func handle_mouse_motion(relative: Vector2) -> bool:
	if not _can_look_with_mouse() or relative.length_squared() <= MANUAL_MOUSE_DEADZONE_SQUARED:
		return false
	_apply_look_delta(relative)
	mouse_event_cooldown_frames = 2
	_sync_manual_mouse_position()
	return true

func _apply_look_delta(relative: Vector2) -> void:
	rotate_y(-relative.x * LOOK_SENSITIVITY)
	look_pitch = clamp(look_pitch - relative.y * LOOK_SENSITIVITY, -1.25, 1.25)
	camera.rotation.x = look_pitch

func _process(_delta: float) -> void:
	_update_manual_mouse_look()

func _physics_process(delta: float) -> void:
	if gameplay_input_enabled and not _is_gameplay_mouse_mode():
		_capture_mouse_for_gameplay()
	var input_dir := Input.get_vector("move_left", "move_right", "move_forward", "move_back")
	var basis := global_transform.basis
	var direction := (basis * Vector3(input_dir.x, 0, input_dir.y)).normalized()
	is_crouched = Input.is_action_pressed("crouch")
	crouch_changed.emit(is_crouched)
	var speed := CROUCH_SPEED if is_crouched else WALK_SPEED
	velocity.x = direction.x * speed
	velocity.z = direction.z * speed
	if not is_on_floor():
		velocity.y -= 14.0 * delta
	else:
		velocity.y = -0.1
	move_and_slide()
	_update_held_item()
	_update_footsteps(delta, direction.length() > 0.1)
	_update_interaction_focus()

func _is_gameplay_mouse_mode() -> bool:
	return Input.get_mouse_mode() == Input.MOUSE_MODE_CAPTURED or Input.get_mouse_mode() == Input.MOUSE_MODE_CONFINED_HIDDEN

func _is_headless_display() -> bool:
	return DisplayServer.get_name() == "headless"

func _reset_manual_mouse_tracking() -> void:
	manual_mouse_initialized = false
	mouse_event_cooldown_frames = 2
	call_deferred("_center_manual_mouse")

func _center_manual_mouse() -> void:
	if not _can_look_with_mouse() or not is_inside_tree():
		return
	var viewport := get_viewport()
	if not viewport:
		return
	manual_mouse_position = viewport.get_visible_rect().size * 0.5
	if not _is_headless_display():
		viewport.warp_mouse(manual_mouse_position)
	manual_mouse_initialized = true

func _sync_manual_mouse_position() -> void:
	if not is_inside_tree():
		return
	var viewport := get_viewport()
	if viewport:
		manual_mouse_position = viewport.get_mouse_position()
		manual_mouse_initialized = true

func _update_manual_mouse_look() -> void:
	if not _can_look_with_mouse() or not _is_gameplay_mouse_mode():
		manual_mouse_initialized = false
		return
	var viewport := get_viewport()
	if not viewport:
		return
	var current := viewport.get_mouse_position()
	if not manual_mouse_initialized:
		manual_mouse_position = current
		manual_mouse_initialized = true
		return
	if mouse_event_cooldown_frames > 0:
		mouse_event_cooldown_frames -= 1
		manual_mouse_position = current
		return
	var relative := current - manual_mouse_position
	if relative.length_squared() > MANUAL_MOUSE_DEADZONE_SQUARED:
		_apply_look_delta(relative)
		_center_manual_mouse()
	else:
		manual_mouse_position = current

func _try_interact() -> void:
	var target := get_focused_interactable()
	if target and target.has_method("interact"):
		target.interact(self)
		interacted.emit()
		_update_interaction_focus()

func get_focused_interactable() -> Node:
	interact_ray.force_raycast_update()
	var target := interact_ray.get_collider()
	if target and target.has_method("interact"):
		return target
	return _nearest_center_screen_interactable()

func _nearest_center_screen_interactable() -> Node:
	if not camera:
		return null
	var best: Node = null
	var best_score := -1.0
	var forward := -camera.global_transform.basis.z.normalized()
	for candidate in get_tree().get_nodes_in_group("interactable"):
		if candidate == self or not (candidate is Node3D) or not candidate.has_method("interact"):
			continue
		var target_node := candidate as Node3D
		var focus_point := _interaction_focus_point(target_node)
		var to_candidate: Vector3 = focus_point - camera.global_position
		var distance := to_candidate.length()
		if distance > INTERACT_DISTANCE or distance <= 0.01:
			continue
		var direction := to_candidate / distance
		var focus := forward.dot(direction)
		if focus < INTERACT_FOCUS_DOT:
			continue
		if not _has_clear_interaction_line(target_node, focus_point):
			continue
		var score := focus - distance * 0.045
		if score > best_score:
			best_score = score
			best = candidate
	return best

func _interaction_focus_point(target_node: Node3D) -> Vector3:
	if target_node.has_method("interaction_focus_point"):
		return target_node.interaction_focus_point(self)
	if target_node.is_in_group("throwable") or target_node.is_in_group("key_item"):
		return target_node.global_position + Vector3(0, 0.08, 0)
	return target_node.global_position + Vector3(0, 0.85, 0)

func _has_clear_interaction_line(target_node: Node3D, focus_point: Vector3) -> bool:
	var query := PhysicsRayQueryParameters3D.create(camera.global_position, focus_point)
	query.exclude = [get_rid()]
	query.collide_with_areas = true
	query.collide_with_bodies = true
	var hit := get_world_3d().direct_space_state.intersect_ray(query)
	if hit.is_empty():
		return true
	var collider := hit.get("collider") as Node
	return collider == target_node or (collider != null and target_node.is_ancestor_of(collider))

func get_current_interaction_text() -> String:
	return current_focus_text

func _update_interaction_focus() -> void:
	var target := get_focused_interactable()
	var next_text := ""
	if target:
		if target.has_method("interaction_text"):
			next_text = str(target.interaction_text(self))
		else:
			next_text = LocalizationManager.text("interact.generic")
	if next_text != current_focus_text:
		current_focus_text = next_text
		focus_changed.emit(current_focus_text)

func pick_up(item: Node3D) -> void:
	if held_item:
		return
	held_item = item
	if held_item.has_method("pick_up"):
		held_item.pick_up(self)
	item_picked.emit()

func give_item(item_id: String) -> void:
	inventory[item_id] = true
	inventory_changed.emit(item_id)

func has_item(item_id: String) -> bool:
	return bool(inventory.get(item_id, false))

func _throw_held_item() -> void:
	if not held_item:
		return
	var item := held_item
	held_item = null
	var impulse := -camera.global_transform.basis.z * THROW_FORCE + Vector3.UP * 1.2
	if item.has_method("throw_from_player"):
		item.throw_from_player(camera.global_position + (-camera.global_transform.basis.z * 0.8), impulse)
	item_thrown.emit()

func _drop_held_item() -> void:
	if not held_item:
		return
	var item := held_item
	held_item = null
	if item.has_method("drop_from_player"):
		item.drop_from_player(camera.global_position + (-camera.global_transform.basis.z * 0.8))

func _update_held_item() -> void:
	if held_item:
		held_item.global_position = camera.global_position + (-camera.global_transform.basis.z * 0.8) + Vector3(0, -0.22, 0)

func _update_footsteps(delta: float, moving: bool) -> void:
	if not moving or not is_on_floor():
		footstep_timer = 0.0
		return
	footstep_timer -= delta
	if footstep_timer <= 0.0:
		AudioManager.play_footstep(is_crouched)
		if not is_crouched:
			SoundEventSystem.emit_sound(global_position, 4.2, self)
		footstep_timer = 0.62 if is_crouched else 0.43
