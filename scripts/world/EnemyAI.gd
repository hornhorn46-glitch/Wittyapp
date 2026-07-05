extends CharacterBody3D

signal player_detected

enum State { IDLE, PATROL, INVESTIGATE, RETURN, SEARCH, ALERT }

const ModelVisuals := preload("res://scripts/world/ModelVisuals.gd")
const CharacterVisuals := preload("res://scripts/world/CharacterVisuals.gd")
const SPEED := 2.0
const DETECT_DISTANCE := 6.5
const DETECT_ANGLE := 0.58
const SUSPICION_BUILD_RATE := 0.85
const SUSPICION_DECAY_RATE := 0.34

var state := State.PATROL
var patrol_points: Array[Vector3] = []
var patrol_index := 0
var investigate_position := Vector3.ZERO
var start_position := Vector3.ZERO
var player: Node3D
var suspicion := 0.0
var search_timer := 0.0
var last_known_player_position := Vector3.ZERO
var visual_root: Node3D
var visual_animation_player: AnimationPlayer
var visual_current_animation := ""
var visual_base_y := 0.0
var visual_anim_time := 0.0

func _ready() -> void:
	name = "HostileNPC"
	add_to_group("hostile")
	start_position = global_position
	SoundEventSystem.sound_emitted.connect(_on_sound_emitted)
	_create_visual()

func setup(points: Array[Vector3], target_player: Node3D) -> void:
	patrol_points = points
	player = target_player

func _physics_process(delta: float) -> void:
	if player and _update_detection(delta):
		player_detected.emit()
		return
	match state:
		State.PATROL:
			_move_to(_current_patrol_point(), delta)
			if global_position.distance_to(_current_patrol_point()) < 0.35:
				patrol_index = (patrol_index + 1) % max(1, patrol_points.size())
		State.INVESTIGATE:
			_move_to(investigate_position, delta)
			if global_position.distance_to(investigate_position) < 0.45:
				state = State.SEARCH
				search_timer = 2.6
		State.RETURN:
			_move_to(_current_patrol_point(), delta)
			if global_position.distance_to(_current_patrol_point()) < 0.45:
				state = State.PATROL
		State.SEARCH:
			velocity = Vector3.ZERO
			rotation.y += delta * 1.2
			search_timer -= delta
			if search_timer <= 0.0:
				state = State.RETURN
		State.ALERT:
			_move_to(last_known_player_position, delta)
		State.IDLE:
			velocity = Vector3.ZERO
	move_and_slide()
	_animate_visual(delta)

func _current_patrol_point() -> Vector3:
	if patrol_points.is_empty():
		return start_position
	return patrol_points[patrol_index]

func _move_to(target: Vector3, _delta: float) -> void:
	var direction := target - global_position
	direction.y = 0
	if direction.length() < 0.05:
		velocity.x = 0
		velocity.z = 0
		return
	var normalized := direction.normalized()
	velocity.x = normalized.x * SPEED
	velocity.z = normalized.z * SPEED
	look_at(Vector3(target.x, global_position.y, target.z), Vector3.UP)

func _update_detection(delta: float) -> bool:
	if _can_see_player():
		last_known_player_position = player.global_position
		var distance := global_position.distance_to(player.global_position)
		var proximity := remap(clamp(distance, 1.0, DETECT_DISTANCE), DETECT_DISTANCE, 1.0, 0.65, 1.65)
		var crouch_modifier := 0.45 if player.get("is_crouched") else 1.0
		suspicion = clamp(suspicion + delta * SUSPICION_BUILD_RATE * proximity * crouch_modifier, 0.0, 1.0)
		if suspicion > 0.48 and state != State.ALERT:
			state = State.ALERT
		return suspicion >= 1.0
	suspicion = max(0.0, suspicion - delta * SUSPICION_DECAY_RATE)
	if state == State.ALERT and suspicion < 0.18:
		investigate_position = last_known_player_position
		state = State.INVESTIGATE
	return false

func _can_detect_player() -> bool:
	return _can_see_player() and suspicion >= 1.0

func _can_see_player() -> bool:
	var to_player := player.global_position - global_position
	to_player.y = 0
	var distance := to_player.length()
	if distance > DETECT_DISTANCE:
		return false
	var forward := -global_transform.basis.z
	var crouch_factor := 0.55 if player.get("is_crouched") else 1.0
	if distance < 1.45 * crouch_factor:
		return _has_line_of_sight()
	return forward.normalized().dot(to_player.normalized()) > DETECT_ANGLE and distance < DETECT_DISTANCE * crouch_factor and _has_line_of_sight()

func _has_line_of_sight() -> bool:
	if not player:
		return false
	var space := get_world_3d().direct_space_state
	var query := PhysicsRayQueryParameters3D.create(global_position + Vector3(0, 1.25, 0), player.global_position + Vector3(0, 0.85, 0))
	query.exclude = [get_rid()]
	var hit := space.intersect_ray(query)
	return hit.is_empty() or hit.get("collider") == player

func _on_sound_emitted(position: Vector3, loudness: float, source: Node) -> void:
	if source == self:
		return
	if global_position.distance_to(position) <= loudness:
		investigate_position = position
		state = State.INVESTIGATE
		if loudness >= 10.0:
			suspicion = max(suspicion, 0.32)

func get_awareness_ratio() -> float:
	return suspicion

func awareness_text_key() -> String:
	if suspicion >= 0.75:
		return "status.detecting"
	if state == State.INVESTIGATE or state == State.SEARCH:
		return "status.searching"
	if suspicion > 0.18 or state == State.ALERT:
		return "status.suspicious"
	return "status.hidden"

func _create_visual() -> void:
	var collision := CollisionShape3D.new()
	var capsule := CapsuleShape3D.new()
	capsule.radius = 0.30
	capsule.height = 1.62
	collision.shape = capsule
	add_child(collision)
	var model_path := "res://assets/curated/models/quaternius/animated_human.glb"
	var packed := load(model_path) if FileAccess.file_exists(model_path + ".import") else null
	if packed:
		var model: Node3D = packed.instantiate()
		model.name = "HostileVisual"
		add_child(model)
		CharacterVisuals.fit_model_height(model, 1.58, -0.35)
		visual_root = model
		visual_base_y = model.position.y
		visual_animation_player = CharacterVisuals.find_animation_player(model)
	else:
		var model := CharacterVisuals.make_smooth_humanoid(
			"HostileSmoothVisual",
			Color(0.18, 0.17, 0.15),
			Color(0.56, 0.10, 0.07),
			Color(0.64, 0.47, 0.36),
			true
		)
		add_child(model)
		CharacterVisuals.fit_model_height(model, 1.58, -0.35)
		visual_root = model
		visual_base_y = model.position.y
	var cone := SpotLight3D.new()
	cone.light_color = Color(1.0, 0.46, 0.32)
	cone.light_energy = 1.2
	cone.spot_range = 7.0
	cone.spot_angle = 34.0
	cone.rotation_degrees.x = -12
	add_child(cone)

func _animate_visual(delta: float) -> void:
	if not visual_root:
		return
	var moving := Vector2(velocity.x, velocity.z).length() > 0.08
	visual_current_animation = CharacterVisuals.play_best_animation(visual_animation_player, moving, visual_current_animation)
	if moving:
		visual_anim_time += delta * 8.0
		visual_root.position.y = visual_base_y + sin(visual_anim_time * 2.0) * 0.025
		visual_root.rotation.z = sin(visual_anim_time) * 0.035
	else:
		visual_anim_time += delta * 1.4
		visual_root.position.y = lerpf(visual_root.position.y, visual_base_y + sin(visual_anim_time) * 0.008, delta * 4.0)
		visual_root.rotation.z = lerpf(visual_root.rotation.z, 0.0, delta * 5.0)
