extends RefCounted

static func make_textured_material(texture_path: String, tint: Color, roughness: float, emission: Color = Color(0, 0, 0), emission_energy: float = 0.0) -> StandardMaterial3D:
	var material := StandardMaterial3D.new()
	material.albedo_color = tint
	material.roughness = roughness
	var texture := load(texture_path)
	if texture:
		material.albedo_texture = texture
	if emission_energy > 0.0:
		material.emission_enabled = true
		material.emission = emission
		material.emission_energy_multiplier = emission_energy
	return material

static func apply_material(root: Node, material: Material) -> void:
	if root is MeshInstance3D:
		root.material_override = material
		root.cast_shadow = GeometryInstance3D.SHADOW_CASTING_SETTING_ON
	for child in root.get_children():
		apply_material(child, material)
