extends RigidBody3D

@export var display_name := "Bottle"
@export var display_name_key := ""
@export var model_path := ""
@export var model_scale := Vector3(0.55, 0.55, 0.55)
@export var model_offset := Vector3(0, -0.14, 0)
@export var collision_size := Vector3(0.38, 0.38, 0.38)
@export var throw_loudness := 16.0
@export var drop_loudness := 4.0

var picked := false

func _ready() -> void:
	add_to_group("interactable")
	add_to_group("throwable")
	if get_child_count() == 0:
		_create_visual()

func _create_visual() -> void:
	if model_path != "":
		var packed := load(model_path)
		if packed:
			var model: Node3D = packed.instantiate()
			model.scale = model_scale
			model.position = model_offset
			add_child(model)
			var collision := CollisionShape3D.new()
			var shape := BoxShape3D.new()
			shape.size = collision_size
			collision.shape = shape
			add_child(collision)
			return
	var mesh := MeshInstance3D.new()
	var box := BoxMesh.new()
	box.size = Vector3(0.22, 0.32, 0.18)
	mesh.mesh = box
	mesh.material_override = _material(Color(0.35, 0.42, 0.38))
	add_child(mesh)
	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = box.size
	collision.shape = shape
	add_child(collision)

func interact(player: Node) -> void:
	if player.has_method("pick_up"):
		player.pick_up(self)

func interaction_text(_player: Node) -> String:
	return "%s: %s" % [LocalizationManager.text("interact.pickup"), _display_name()]

func interaction_focus_point(_player: Node) -> Vector3:
	return global_position + Vector3(0, max(collision_size.y * 0.35, 0.08), 0)

func _display_name() -> String:
	return LocalizationManager.text(display_name_key) if display_name_key != "" else display_name

func pick_up(_player: Node) -> void:
	picked = true
	freeze = true
	collision_layer = 0
	collision_mask = 0

func throw_from_player(origin: Vector3, impulse: Vector3) -> void:
	global_position = origin
	picked = false
	freeze = false
	collision_layer = 1
	collision_mask = 1
	linear_velocity = impulse
	AudioManager.play_sfx("throw")
	SoundEventSystem.emit_sound(global_position, throw_loudness, self)

func drop_from_player(origin: Vector3) -> void:
	global_position = origin
	picked = false
	freeze = false
	collision_layer = 1
	collision_mask = 1
	linear_velocity = Vector3.ZERO
	AudioManager.play_sfx("throw")
	SoundEventSystem.emit_sound(global_position, drop_loudness, self)

func _material(color: Color) -> StandardMaterial3D:
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	mat.roughness = 0.82
	return mat
