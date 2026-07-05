extends CharacterBody3D

signal rescued
signal started_following

enum State { FEAR, FOLLOW, RESCUED }

const ModelVisuals := preload("res://scripts/world/ModelVisuals.gd")
const CharacterVisuals := preload("res://scripts/world/CharacterVisuals.gd")
const FOLLOW_SPEED := 2.35
const FOLLOW_STOP_DISTANCE := 0.55
const SAFE_ZONE_GUIDE_DISTANCE := 2.8

var state := State.FEAR
var player: Node3D
var safe_zone_position := Vector3.ZERO
var collision_shape: CollisionShape3D
var visual_root: Node3D
var visual_animation_player: AnimationPlayer
var visual_current_animation := ""
var visual_base_y := 0.0
var visual_anim_time := 0.0

func _ready() -> void:
	name = "CivilianNPC"
	add_to_group("civilian")
	add_to_group("interactable")
	_create_visual()

func setup(target_player: Node3D, safe_pos: Vector3) -> void:
	player = target_player
	safe_zone_position = safe_pos

func interact(_player: Node) -> void:
	if state == State.FEAR:
		state = State.FOLLOW
		_set_companion_collision(false)
		started_following.emit()

func interaction_focus_point(_player: Node) -> Vector3:
	return global_position + Vector3(0, 0.82, 0)

func _physics_process(delta: float) -> void:
	if state == State.FOLLOW and player:
		var target := player.global_position + player.global_transform.basis.z * 1.4
		if player.global_position.distance_to(safe_zone_position) < SAFE_ZONE_GUIDE_DISTANCE:
			target = safe_zone_position
		var direction := target - global_position
		direction.y = 0
		if direction.length() > FOLLOW_STOP_DISTANCE:
			var n := direction.normalized()
			velocity.x = n.x * FOLLOW_SPEED
			velocity.z = n.z * FOLLOW_SPEED
			look_at(Vector3(target.x, global_position.y, target.z), Vector3.UP)
		else:
			velocity.x = 0
			velocity.z = 0
		if global_position.distance_to(safe_zone_position) < 2.0:
			state = State.RESCUED
			rescued.emit()
	else:
		velocity = Vector3.ZERO
	move_and_slide()
	_animate_visual(delta)

func is_rescued() -> bool:
	return state == State.RESCUED

func _create_visual() -> void:
	var collision := CollisionShape3D.new()
	var capsule := CapsuleShape3D.new()
	capsule.radius = 0.28
	capsule.height = 1.52
	collision.shape = capsule
	collision_shape = collision
	add_child(collision)
	var model_path := "res://assets/curated/models/quaternius/animated_woman_smooth.glb"
	var packed := load(model_path) if FileAccess.file_exists(model_path + ".import") else null
	if packed:
		var model: Node3D = packed.instantiate()
		model.name = "CivilianVisual"
		add_child(model)
		CharacterVisuals.fit_model_height(model, 1.48, -0.35)
		visual_root = model
		visual_base_y = model.position.y
		visual_animation_player = CharacterVisuals.find_animation_player(model)
	else:
		var model := CharacterVisuals.make_smooth_humanoid(
			"CivilianSmoothVisual",
			Color(0.18, 0.36, 0.31),
			Color(0.55, 0.72, 0.58),
			Color(0.66, 0.50, 0.40)
		)
		add_child(model)
		CharacterVisuals.fit_model_height(model, 1.48, -0.35)
		visual_root = model
		visual_base_y = model.position.y

func _set_companion_collision(enabled: bool) -> void:
	if collision_shape:
		collision_shape.set_deferred("disabled", not enabled)

func _animate_visual(delta: float) -> void:
	if not visual_root:
		return
	var moving := Vector2(velocity.x, velocity.z).length() > 0.08
	visual_current_animation = CharacterVisuals.play_best_animation(visual_animation_player, moving, visual_current_animation)
	if moving:
		visual_anim_time += delta * 6.5
		visual_root.position.y = visual_base_y + sin(visual_anim_time * 2.0) * 0.018
		visual_root.rotation.z = sin(visual_anim_time) * 0.024
	else:
		visual_anim_time += delta * 1.1
		visual_root.position.y = lerpf(visual_root.position.y, visual_base_y + sin(visual_anim_time) * 0.006, delta * 4.0)
		visual_root.rotation.z = lerpf(visual_root.rotation.z, 0.0, delta * 5.0)
