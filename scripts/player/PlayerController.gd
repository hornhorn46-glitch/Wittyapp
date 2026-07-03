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

var camera: Camera3D
var interact_ray: RayCast3D
var held_item: Node3D
var is_crouched := false
var look_pitch := 0.0
var footstep_timer := 0.0
var inventory := {}
var current_focus_text := ""

func _ready() -> void:
	name = "Player"
	add_to_group("player")
	_create_body()
	Input.set_mouse_mode(Input.MOUSE_MODE_CAPTURED)

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
	interact_ray.target_position = Vector3(0, 0, -2.4)
	interact_ray.collide_with_areas = true
	camera.add_child(interact_ray)

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventMouseMotion and Input.get_mouse_mode() == Input.MOUSE_MODE_CAPTURED:
		rotate_y(-event.relative.x * LOOK_SENSITIVITY)
		look_pitch = clamp(look_pitch - event.relative.y * LOOK_SENSITIVITY, -1.25, 1.25)
		camera.rotation.x = look_pitch
	if event.is_action_pressed("interact"):
		_try_interact()
	if event.is_action_pressed("throw_item"):
		_throw_held_item()
	if event.is_action_pressed("drop_item"):
		_drop_held_item()

func _physics_process(delta: float) -> void:
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
	return null

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
		AudioManager.play_sfx("footstep")
		if not is_crouched:
			SoundEventSystem.emit_sound(global_position, 4.2, self)
		footstep_timer = 0.55 if is_crouched else 0.36
