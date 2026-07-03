extends CharacterBody3D

signal rescued
signal started_following

enum State { FEAR, FOLLOW, RESCUED }

const ModelVisuals := preload("res://scripts/world/ModelVisuals.gd")
const FOLLOW_SPEED := 2.35

var state := State.FEAR
var player: Node3D
var safe_zone_position := Vector3.ZERO

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
		started_following.emit()

func _physics_process(_delta: float) -> void:
	if state == State.FOLLOW and player:
		var target := player.global_position + player.global_transform.basis.z * 1.4
		var direction := target - global_position
		direction.y = 0
		if direction.length() > 1.0:
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

func is_rescued() -> bool:
	return state == State.RESCUED

func _create_visual() -> void:
	var collision := CollisionShape3D.new()
	var capsule := CapsuleShape3D.new()
	capsule.radius = 0.30
	capsule.height = 1.55
	collision.shape = capsule
	add_child(collision)
	var packed := load("res://assets/curated/models/characters/civilian.glb")
	if packed:
		var model: Node3D = packed.instantiate()
		model.scale = Vector3(0.70, 0.70, 0.70)
		model.position = Vector3(0, -0.46, 0)
		var material := ModelVisuals.make_textured_material(
			"res://assets/curated/models/characters/Textures/texture-a.png",
			Color(0.76, 1.0, 0.92),
			0.86
		)
		ModelVisuals.apply_material(model, material)
		add_child(model)
	else:
		var mesh := MeshInstance3D.new()
		var visual := CapsuleMesh.new()
		visual.radius = 0.30
		visual.height = 1.55
		mesh.mesh = visual
		var mat := StandardMaterial3D.new()
		mat.albedo_color = Color(0.23, 0.43, 0.39)
		mat.roughness = 0.9
		mesh.material_override = mat
		add_child(mesh)
