extends Area3D

@export var item_id := "security_badge"
@export var display_name_key := "item.security_badge"

func _ready() -> void:
	name = "SecurityBadge"
	add_to_group("interactable")
	add_to_group("key_item")
	_create_visual()

func interaction_text(_player: Node) -> String:
	return "%s: %s" % [LocalizationManager.text("interact.take"), LocalizationManager.text(display_name_key)]

func interaction_focus_point(_player: Node) -> Vector3:
	return global_position + Vector3(0, 0.04, 0)

func interact(player: Node) -> void:
	if player and player.has_method("give_item"):
		player.give_item(item_id)
		AudioManager.play_ui_click()
		queue_free()

func _create_visual() -> void:
	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = Vector3(0.34, 0.05, 0.22)
	collision.shape = shape
	add_child(collision)
	var card := MeshInstance3D.new()
	var mesh := BoxMesh.new()
	mesh.size = Vector3(0.34, 0.025, 0.22)
	card.mesh = mesh
	card.material_override = _card_material(Color(0.10, 0.17, 0.20), Color(0.02, 0.10, 0.12), 0.0)
	add_child(card)
	var stripe := MeshInstance3D.new()
	var stripe_mesh := BoxMesh.new()
	stripe_mesh.size = Vector3(0.28, 0.028, 0.035)
	stripe.mesh = stripe_mesh
	stripe.position = Vector3(0, 0.017, -0.06)
	stripe.material_override = _card_material(Color(0.82, 0.66, 0.28), Color(0.95, 0.72, 0.22), 0.45)
	add_child(stripe)

func _card_material(color: Color, emission_color: Color, emission_energy: float) -> StandardMaterial3D:
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	mat.roughness = 0.42
	if emission_energy > 0.0:
		mat.emission_enabled = true
		mat.emission = emission_color
		mat.emission_energy_multiplier = emission_energy
	return mat
