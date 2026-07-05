extends StaticBody3D

signal security_disabled

var disabled_state := false
var focus_offset := Vector3(0, 0.02, 0.08)
var screen_mesh: MeshInstance3D
var status_light: MeshInstance3D

func _ready() -> void:
	add_to_group("interactable")
	_create_visual()

func interaction_text(_player: Node) -> String:
	return LocalizationManager.text("interact.security_disabled" if disabled_state else "interact.disable_security")

func interaction_focus_point(_player: Node) -> Vector3:
	return to_global(focus_offset)

func interact(_player: Node) -> void:
	if disabled_state:
		AudioManager.play_ui_click()
		return
	disabled_state = true
	_set_visual_disabled()
	AudioManager.play_ui_click()
	SoundEventSystem.emit_sound(global_position, 1.0, self)
	security_disabled.emit()

func _create_visual() -> void:
	var case_mat := StandardMaterial3D.new()
	case_mat.albedo_color = Color(0.030, 0.038, 0.036)
	case_mat.roughness = 0.46
	case_mat.metallic = 0.18

	var screen_mat := _screen_material(Color(0.15, 0.98, 0.58), 0.85)
	var amber_mat := _screen_material(Color(1.0, 0.56, 0.16), 0.55)

	var case_mesh := MeshInstance3D.new()
	case_mesh.name = "SecurityTerminalCase"
	var case_box := BoxMesh.new()
	case_box.size = Vector3(0.76, 0.48, 0.10)
	case_mesh.mesh = case_box
	case_mesh.material_override = case_mat
	add_child(case_mesh)

	screen_mesh = MeshInstance3D.new()
	screen_mesh.name = "SecurityTerminalScreen"
	var screen_box := BoxMesh.new()
	screen_box.size = Vector3(0.50, 0.26, 0.018)
	screen_mesh.mesh = screen_box
	screen_mesh.position = Vector3(-0.06, 0.04, 0.060)
	screen_mesh.material_override = screen_mat
	add_child(screen_mesh)

	status_light = MeshInstance3D.new()
	status_light.name = "SecurityTerminalStatusLight"
	var light_mesh := CylinderMesh.new()
	light_mesh.top_radius = 0.045
	light_mesh.bottom_radius = 0.045
	light_mesh.height = 0.018
	light_mesh.radial_segments = 24
	status_light.mesh = light_mesh
	status_light.rotation_degrees.x = 90
	status_light.position = Vector3(0.28, -0.13, 0.067)
	status_light.material_override = amber_mat
	add_child(status_light)

	for i in range(3):
		var button := MeshInstance3D.new()
		button.name = "SecurityTerminalButton"
		var button_mesh := BoxMesh.new()
		button_mesh.size = Vector3(0.08, 0.045, 0.018)
		button.mesh = button_mesh
		button.position = Vector3(0.21 + i * 0.085, 0.12, 0.068)
		button.material_override = amber_mat
		add_child(button)

	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = Vector3(0.86, 0.58, 0.18)
	collision.shape = shape
	add_child(collision)

func _screen_material(color: Color, energy: float) -> StandardMaterial3D:
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color.darkened(0.45)
	mat.roughness = 0.34
	mat.emission_enabled = true
	mat.emission = color
	mat.emission_energy_multiplier = energy
	return mat

func _set_visual_disabled() -> void:
	if screen_mesh:
		screen_mesh.material_override = _screen_material(Color(0.16, 0.42, 0.95), 0.28)
	if status_light:
		status_light.material_override = _screen_material(Color(0.18, 0.90, 0.56), 0.65)
