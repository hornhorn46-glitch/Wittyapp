extends RefCounted

static func fit_model_height(model: Node3D, target_height: float, bottom_y: float) -> void:
	model.position = Vector3.ZERO
	model.scale = Vector3.ONE
	var bounds := _collect_bounds(model)
	if bounds.size.y <= 0.001:
		return
	var scale_value := target_height / bounds.size.y
	model.scale = Vector3(scale_value, scale_value, scale_value)
	model.position.y = bottom_y - bounds.position.y * scale_value

static func find_animation_player(node: Node) -> AnimationPlayer:
	if node is AnimationPlayer:
		return node as AnimationPlayer
	for child in node.get_children():
		var found := find_animation_player(child)
		if found:
			return found
	return null

static func play_best_animation(player: AnimationPlayer, moving: bool, current: String) -> String:
	if not player:
		return current
	var names := player.get_animation_list()
	if names.is_empty():
		return current
	var selected := String(names[0])
	var preferred_terms := ["walk", "run"] if moving else ["idle", "stand"]
	var found_preferred := false
	for candidate in names:
		var candidate_name := String(candidate)
		var lower := candidate_name.to_lower()
		for term in preferred_terms:
			if lower.find(term) >= 0:
				selected = candidate_name
				found_preferred = true
				break
		if found_preferred:
			break
	if selected != current:
		player.play(selected)
		return selected
	return current

static func make_smooth_humanoid(node_name: String, body_color: Color, accent_color: Color, skin_color: Color, hostile: bool = false) -> Node3D:
	var root := Node3D.new()
	root.name = node_name
	var body_mat := _make_material(body_color, 0.82, 0.0)
	var accent_mat := _make_material(accent_color, 0.70, 0.0)
	var skin_mat := _make_material(skin_color, 0.86, 0.0)
	var dark_mat := _make_material(Color(0.045, 0.050, 0.052), 0.62, 0.08)
	var eye_mat := _make_material(Color(0.018, 0.022, 0.024), 0.45, 0.0)
	var shoe_mat := _make_material(Color(0.025, 0.026, 0.026), 0.54, 0.08)
	var belt_mat := _make_material(Color(0.055, 0.043, 0.034), 0.58, 0.0)
	_add_capsule(root, "Torso", Vector3(0, 0.82, 0), 0.205, 0.74, body_mat)
	_add_capsule(root, "Vest", Vector3(0, 0.86, -0.030), 0.155, 0.50, accent_mat)
	_add_capsule(root, "Neck", Vector3(0, 1.17, 0), 0.052, 0.14, skin_mat)
	_add_sphere(root, "Head", Vector3(0, 1.33, 0), Vector3(0.145, 0.175, 0.145), skin_mat)
	_add_sphere(root, "HairOrCap", Vector3(0, 1.455, -0.018), Vector3(0.140, 0.060, 0.132), dark_mat if hostile else accent_mat)
	for eye_x in [-0.060, 0.060]:
		_add_sphere(root, "Eye", Vector3(eye_x * 0.78, 1.36, -0.132), Vector3(0.014, 0.010, 0.008), eye_mat)
	_add_sphere(root, "Nose", Vector3(0, 1.31, -0.148), Vector3(0.017, 0.024, 0.014), skin_mat)
	_add_sphere(root, "ShoulderLeft", Vector3(-0.22, 1.03, 0.0), Vector3(0.082, 0.066, 0.082), body_mat)
	_add_sphere(root, "ShoulderRight", Vector3(0.22, 1.03, 0.0), Vector3(0.082, 0.066, 0.082), body_mat)
	_add_box(root, "ChestPanel", Vector3(0, 0.98, -0.178), Vector3(0.22, 0.17, 0.014), accent_mat)
	_add_box(root, "Belt", Vector3(0, 0.54, -0.177), Vector3(0.34, 0.045, 0.020), belt_mat)
	_add_box(root, "BeltBuckle", Vector3(0, 0.545, -0.195), Vector3(0.060, 0.048, 0.016), accent_mat)
	for side in [-1.0, 1.0]:
		var arm := _add_capsule(root, "Arm", Vector3(side * 0.285, 0.77, 0.0), 0.047, 0.61, body_mat)
		arm.rotation_degrees.z = side * -10.0
		_add_sphere(root, "Hand", Vector3(side * 0.335, 0.44, 0.02), Vector3(0.050, 0.046, 0.050), skin_mat)
		var leg := _add_capsule(root, "Leg", Vector3(side * 0.09, 0.31, 0.0), 0.060, 0.66, dark_mat)
		leg.rotation_degrees.z = side * 2.0
		_add_box(root, "KneePad", Vector3(side * 0.09, 0.23, -0.060), Vector3(0.082, 0.060, 0.020), belt_mat)
		_add_sphere(root, "Shoe", Vector3(side * 0.09, 0.03, -0.040), Vector3(0.074, 0.038, 0.132), shoe_mat)
	if hostile:
		_add_sphere(root, "ShoulderBeacon", Vector3(0.16, 1.11, -0.16), Vector3(0.040, 0.040, 0.040), _make_emissive(Color(0.9, 0.10, 0.04), 0.45))
	else:
		_add_sphere(root, "RescueBadge", Vector3(-0.13, 1.03, -0.16), Vector3(0.036, 0.036, 0.036), _make_emissive(Color(0.15, 0.90, 0.66), 0.30))
	return root

static func _collect_bounds(root: Node3D) -> AABB:
	var has_bounds := false
	var result := AABB()
	for child in root.get_children():
		if child is Node3D:
			var child_bounds := _collect_bounds(child as Node3D)
			if child_bounds.size != Vector3.ZERO:
				var relative := root.global_transform.affine_inverse() * (child as Node3D).global_transform
				var transformed := _transform_aabb(child_bounds, relative)
				if has_bounds:
					result = result.merge(transformed)
				else:
					result = transformed
					has_bounds = true
	if root is MeshInstance3D:
		var mesh_bounds := (root as MeshInstance3D).get_aabb()
		if has_bounds:
			result = result.merge(mesh_bounds)
		else:
			result = mesh_bounds
			has_bounds = true
	return result

static func _transform_aabb(bounds: AABB, transform: Transform3D) -> AABB:
	var has_point := false
	var result := AABB()
	for x in [0.0, 1.0]:
		for y in [0.0, 1.0]:
			for z in [0.0, 1.0]:
				var point := bounds.position + Vector3(bounds.size.x * x, bounds.size.y * y, bounds.size.z * z)
				var transformed := transform * point
				if has_point:
					result = result.expand(transformed)
				else:
					result = AABB(transformed, Vector3.ZERO)
					has_point = true
	return result

static func _make_material(color: Color, roughness: float, metallic: float) -> StandardMaterial3D:
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	mat.roughness = roughness
	mat.metallic = metallic
	return mat

static func _make_emissive(color: Color, energy: float) -> StandardMaterial3D:
	var mat := _make_material(color, 0.55, 0.0)
	mat.emission_enabled = true
	mat.emission = color
	mat.emission_energy_multiplier = energy
	return mat

static func _add_capsule(parent: Node3D, node_name: String, position: Vector3, radius: float, height: float, material: Material) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	mesh_instance.name = node_name
	mesh_instance.position = position
	var mesh := CapsuleMesh.new()
	mesh.radius = radius
	mesh.height = height
	mesh.radial_segments = 24
	mesh.rings = 12
	mesh_instance.mesh = mesh
	mesh_instance.material_override = material
	parent.add_child(mesh_instance)
	return mesh_instance

static func _add_sphere(parent: Node3D, node_name: String, position: Vector3, scale_value: Vector3, material: Material) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	mesh_instance.name = node_name
	mesh_instance.position = position
	mesh_instance.scale = scale_value
	var mesh := SphereMesh.new()
	mesh.radius = 1.0
	mesh.height = 2.0
	mesh.radial_segments = 24
	mesh.rings = 12
	mesh_instance.mesh = mesh
	mesh_instance.material_override = material
	parent.add_child(mesh_instance)
	return mesh_instance

static func _add_box(parent: Node3D, node_name: String, position: Vector3, size: Vector3, material: Material) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	mesh_instance.name = node_name
	mesh_instance.position = position
	var mesh := BoxMesh.new()
	mesh.size = size
	mesh_instance.mesh = mesh
	mesh_instance.material_override = material
	parent.add_child(mesh_instance)
	return mesh_instance
