extends Node3D

signal main_menu_requested
signal restart_requested

const UIFactory := preload("res://scripts/ui/UIFactory.gd")
const FACTORY_MODELS := "res://assets/curated/models/factory/"
const FURNITURE_MODELS := "res://assets/curated/models/furniture/"
const PBR_TEXTURES := "res://assets/curated/textures/pbr/"
const FACTORY_MODEL_SCALE_BOOST := 1.14
const FURNITURE_MODEL_SCALE_BOOST := 1.22
const CHAIR_MODEL_SCALE_BOOST := 1.66
const BENCH_MODEL_SCALE_BOOST := 1.42
const RUG_MODEL_SCALE_BOOST := 1.36
const SIDE_TABLE_MODEL_SCALE_BOOST := 1.22
const TABLE_LAMP_MODEL_SCALE_BOOST := 1.48

var player: Node3D
var enemy: Node3D
var civilian: Node3D
var tutorial: Node
var hud_hint: Label
var hud_objective: Label
var hud_status: Label
var hud_interaction: Label
var hud_interaction_panel: PanelContainer
var hud_crosshair: Label
var hud_awareness: ProgressBar
var hud_awareness_label: Label
var pause_menu: Control
var pause_layer: CanvasLayer
var result_overlay: Control
var intro_overlay: Control
var minimap_player: ColorRect
var minimap_civilian: ColorRect
var minimap_enemy: ColorRect
var game_over := false
var safe_zone_pos := Vector3(13.0, 0, -12.85)

func _ready() -> void:
	name = "TutorialLevel"
	AudioManager.start_level_ambient()
	_build_environment()
	_spawn_actors()
	_build_hud()
	_build_pause()
	tutorial = preload("res://scripts/world/TutorialSystem.gd").new()
	add_child(tutorial)
	tutorial.hint_changed.connect(_set_hint)
	tutorial.objective_changed.connect(_set_objective)
	tutorial.start()
	LocalizationManager.language_changed.connect(_refresh_hud)
	_show_intro_legend()

func _exit_tree() -> void:
	AudioManager.stop_level_ambient()

func _unhandled_input(event: InputEvent) -> void:
	if event.is_action_pressed("pause"):
		_set_paused(not get_tree().paused)

func _build_environment() -> void:
	var world_env := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_COLOR
	env.background_color = Color(0.018, 0.026, 0.034)
	env.fog_enabled = true
	env.fog_light_color = Color(0.17, 0.24, 0.23)
	env.fog_density = 0.012
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = Color(0.09, 0.105, 0.10)
	env.ambient_light_energy = 0.24
	env.ssao_enabled = true
	env.ssao_radius = 1.15
	env.ssao_intensity = 0.95
	env.set("ssil_enabled", true)
	env.set("ssil_radius", 4.0)
	env.set("ssil_intensity", 0.42)
	env.set("sdfgi_enabled", true)
	env.set("sdfgi_use_occlusion", true)
	env.set("sdfgi_read_sky_light", true)
	env.set("sdfgi_energy", 0.62)
	env.set("sdfgi_bounce_feedback", 0.42)
	env.set("sdfgi_cascades", 4)
	env.set("sdfgi_min_cell_size", 0.25)
	env.set("sdfgi_probe_bias", 1.0)
	env.set("volumetric_fog_enabled", true)
	env.set("volumetric_fog_density", 0.012)
	env.set("volumetric_fog_albedo", Color(0.33, 0.42, 0.38))
	env.glow_enabled = true
	env.glow_intensity = 0.04
	env.tonemap_mode = Environment.TONE_MAPPER_FILMIC
	world_env.environment = env
	add_child(world_env)
	var sun := DirectionalLight3D.new()
	sun.light_energy = 0.16
	sun.light_color = Color(0.70, 0.84, 0.96)
	sun.light_indirect_energy = 1.65
	sun.rotation_degrees = Vector3(-42, -28, 0)
	sun.shadow_enabled = true
	sun.shadow_blur = 2.0
	add_child(sun)
	_create_outside_world()
	_create_room("StartRoom", Vector3(0, 0, 0), Vector3(6, 2.8, 5), Color(0.18, 0.21, 0.20), Color(0.11, 0.12, 0.12))
	_create_room("Corridor", Vector3(6.5, 0, -1), Vector3(7, 2.8, 2.3), Color(0.16, 0.18, 0.18), Color(0.10, 0.10, 0.095))
	_create_room("PatrolRoom", Vector3(13, 0, -1), Vector3(6, 2.8, 6), Color(0.13, 0.14, 0.145), Color(0.09, 0.085, 0.08))
	_create_room("RescueRoom", Vector3(13, 0, -7.8), Vector3(5.5, 2.8, 5), Color(0.15, 0.17, 0.16), Color(0.08, 0.10, 0.10))
	_create_floor_plan_extensions()
	_create_connector_hall()
	_create_doorway_closures()
	_create_door(Vector3(3.1, 0, -0.9))
	_create_door(Vector3(9.8, 0, -1.0), true, "security_badge")
	_create_cover(Vector3(12.0, 0.55, -2.6), Vector3(1.5, 1.1, 0.45))
	_create_cover(Vector3(14.3, 0.55, 0.9), Vector3(1.2, 1.1, 0.5))
	_create_cover(Vector3(12.1, 0.45, -7.7), Vector3(1.4, 0.9, 0.55))
	_create_material_zones()
	_create_props()
	_create_windows()
	_create_weather()
	_create_safe_zone()
	_create_exit_zone()
	_create_tension_scene()
	_create_throwables()
	_create_key_items()
	_add_path_lights()
	_create_window_lighting()
	_create_reflection_probes()
	_create_architectural_details()
	_create_wear_marks()

func _create_room(room_name: String, center: Vector3, size: Vector3, wall_color: Color, floor_color: Color) -> void:
	var floor := _box("%sFloor" % room_name, center + Vector3(0, -0.06, 0), Vector3(size.x, 0.12, size.z), floor_color)
	floor.add_to_group("room_zone")
	_box("%sCeiling" % room_name, center + Vector3(0, size.y + 0.06, 0), Vector3(size.x, 0.12, size.z), Color(0.055, 0.065, 0.066))
	if room_name not in ["PatrolRoom", "RescueRoom", "Corridor"]:
		_box("%sNorthWall" % room_name, center + Vector3(0, size.y * 0.5, -size.z * 0.5), Vector3(size.x, size.y, 0.18), wall_color)
	if room_name not in ["RescueRoom", "ExitStairwell", "RecordsRoom"]:
		_box("%sSouthWall" % room_name, center + Vector3(0, size.y * 0.5, size.z * 0.5), Vector3(size.x, size.y, 0.18), wall_color)
	if room_name not in ["Corridor", "PatrolRoom", "RescueRoom"]:
		_box("%sWestWall" % room_name, center + Vector3(-size.x * 0.5, size.y * 0.5, 0), Vector3(0.18, size.y, size.z), wall_color)
	if room_name not in ["StartRoom", "Corridor"]:
		_box("%sEastWall" % room_name, center + Vector3(size.x * 0.5, size.y * 0.5, 0), Vector3(0.18, size.y, size.z), wall_color)
	if room_name == "PatrolRoom":
		_create_passage_to_rescue(center, size, wall_color)
	_create_room_cove_edges(room_name, center, size)

func _create_outside_world() -> void:
	var sky := MeshInstance3D.new()
	sky.name = "OutsideWorldSky"
	var sphere := SphereMesh.new()
	sphere.radius = 42.0
	sphere.height = 84.0
	sphere.radial_segments = 48
	sphere.rings = 24
	sky.mesh = sphere
	sky.position = Vector3(6.0, 2.0, -4.0)
	var sky_mat := StandardMaterial3D.new()
	sky_mat.shading_mode = BaseMaterial3D.SHADING_MODE_UNSHADED
	sky_mat.cull_mode = BaseMaterial3D.CULL_DISABLED
	sky_mat.albedo_color = Color(0.018, 0.032, 0.045)
	sky_mat.emission_enabled = true
	sky_mat.emission = Color(0.020, 0.040, 0.060)
	sky_mat.emission_energy_multiplier = 0.8
	sky.material_override = sky_mat
	sky.cast_shadow = GeometryInstance3D.SHADOW_CASTING_SETTING_OFF
	add_child(sky)
	_visual_box("OutsideStreet", Vector3(7.0, -0.22, -14.0), Vector3(34.0, 0.08, 12.0), Color(0.045, 0.050, 0.052))
	_visual_box("OutsideCourtyard", Vector3(-9.0, -0.24, 1.0), Vector3(11.0, 0.08, 14.0), Color(0.036, 0.046, 0.042))
	for data in [
		[Vector3(-8.0, 2.3, -3.8), Vector3(2.6, 5.0, 1.2), Color(0.018, 0.025, 0.030)],
		[Vector3(2.5, 2.8, -15.8), Vector3(2.2, 6.0, 1.0), Color(0.016, 0.022, 0.027)],
		[Vector3(9.0, 3.5, -16.5), Vector3(2.8, 7.5, 1.0), Color(0.018, 0.026, 0.031)],
		[Vector3(16.0, 2.6, -15.0), Vector3(2.4, 5.6, 1.0), Color(0.020, 0.027, 0.032)],
		[Vector3(20.2, 2.4, -7.5), Vector3(1.0, 5.1, 2.6), Color(0.017, 0.024, 0.030)]
	]:
		_visual_box("OutsideBuilding", data[0], data[1], data[2])
	for data in [
		[Vector3(2.5, 3.8, -15.25), Vector3(1.45, 0.14, 0.04)],
		[Vector3(9.0, 4.2, -15.95), Vector3(1.8, 0.14, 0.04)],
		[Vector3(16.0, 3.6, -14.45), Vector3(1.6, 0.14, 0.04)],
		[Vector3(19.7, 3.2, -7.5), Vector3(0.04, 0.14, 1.55)]
	]:
		_visual_box("OutsideLitWindow", data[0], data[1], Color(0.75, 0.68, 0.46, 0.78))

func _create_passage_to_rescue(center: Vector3, size: Vector3, wall_color: Color) -> void:
	var z := center.z - size.z * 0.5
	_box("PatrolRoomNorthWallLeft", Vector3(center.x - 2.1, 1.4, z), Vector3(1.7, 2.8, 0.18), wall_color)
	_box("PatrolRoomNorthWallRight", Vector3(center.x + 2.1, 1.4, z), Vector3(1.7, 2.8, 0.18), wall_color)

func _create_connector_hall() -> void:
	_box("RescueConnectorFloor", Vector3(13.0, -0.055, -4.65), Vector3(2.2, 0.12, 1.35), Color(0.08, 0.09, 0.09))
	_box("RescueConnectorCeiling", Vector3(13.0, 2.86, -4.65), Vector3(2.2, 0.12, 1.35), Color(0.045, 0.055, 0.056))
	_box("RescueConnectorLeftWall", Vector3(11.9, 1.4, -4.65), Vector3(0.16, 2.8, 1.35), Color(0.13, 0.15, 0.15))
	_box("RescueConnectorRightWall", Vector3(14.1, 1.4, -4.65), Vector3(0.16, 2.8, 1.35), Color(0.13, 0.15, 0.15))

func _create_floor_plan_extensions() -> void:
	_create_room("RecordsRoom", Vector3(6.5, 0, -4.1), Vector3(4.6, 2.8, 3.9), Color(0.14, 0.16, 0.155), Color(0.075, 0.080, 0.078))
	_create_room("SecurityOffice", Vector3(6.5, 0, 1.65), Vector3(3.4, 2.8, 2.4), Color(0.12, 0.15, 0.15), Color(0.075, 0.085, 0.08))
	_create_room("MaintenanceBay", Vector3(17.7, 0, -1.0), Vector3(3.2, 2.8, 4.8), Color(0.12, 0.13, 0.13), Color(0.075, 0.075, 0.07))
	_create_room("ExitStairwell", Vector3(13.0, 0, -12.0), Vector3(4.2, 2.8, 3.4), Color(0.115, 0.135, 0.13), Color(0.062, 0.068, 0.066))
	_box("LockedOfficeDoor", Vector3(6.45, 1.16, 0.31), Vector3(1.0, 2.15, 0.08), Color(0.055, 0.075, 0.074))
	_create_window(Vector3(5.35, 1.55, 0.30), Vector3(1.1, 1.0, 0.05), Vector3.ZERO)
	_create_window(Vector3(7.65, 1.55, 0.30), Vector3(1.1, 1.0, 0.05), Vector3.ZERO)
	_create_window(Vector3(6.5, 1.55, -6.06), Vector3(1.35, 1.0, 0.05), Vector3.ZERO)
	_create_window(Vector3(16.05, 1.55, -0.3), Vector3(0.05, 1.0, 1.35), Vector3.ZERO)
	_create_window(Vector3(15.16, 1.55, -12.2), Vector3(0.05, 1.0, 1.15), Vector3.ZERO)
	_box("MaintenanceLockedHeader", Vector3(16.06, 2.35, -1.35), Vector3(0.12, 0.38, 1.2), Color(0.08, 0.09, 0.09))

func _create_material_zones() -> void:
	_visual_box("BrickFeatureWallStart", Vector3(-2.905, 1.38, -0.35), Vector3(0.035, 2.45, 3.1), Color.WHITE)
	_visual_box("BrickFeatureWallPatrol", Vector3(13.0, 1.38, 1.905), Vector3(4.9, 2.45, 0.035), Color.WHITE)
	_visual_box("BrickFeatureWallRescue", Vector3(15.735, 1.38, -8.0), Vector3(0.035, 2.45, 2.8), Color.WHITE)
	_visual_box("CarpetRugStart", Vector3(0.3, 0.014, 1.55), Vector3(3.7, 0.018, 1.75), Color.WHITE)
	_visual_box("CarpetRugOffice", Vector3(6.5, 0.014, 1.8), Vector3(2.8, 0.018, 1.65), Color.WHITE)
	_visual_box("CarpetRugRescue", Vector3(14.15, 0.014, -7.55), Vector3(1.65, 0.018, 1.25), Color.WHITE)
	_visual_box("CarpetRugRecords", Vector3(6.5, 0.014, -4.15), Vector3(2.55, 0.018, 1.35), Color.WHITE)
	_visual_box("ExitMetalFloorPlate", Vector3(13.0, 0.018, -12.35), Vector3(2.45, 0.018, 1.55), Color(0.12, 0.16, 0.15, 0.92))

func _create_doorway_closures() -> void:
	# Start room east wall around the first doorway.
	_box("StartRoomEastWallNorthSegment", Vector3(3.0, 1.4, -2.05), Vector3(0.18, 2.8, 0.9), Color(0.18, 0.21, 0.20))
	_box("StartRoomEastWallSouthSegment", Vector3(3.0, 1.4, 1.2), Vector3(0.18, 2.8, 2.6), Color(0.18, 0.21, 0.20))
	_box("StartRoomDoorHeader", Vector3(3.0, 2.45, -0.78), Vector3(0.20, 0.7, 1.55), Color(0.12, 0.15, 0.15))
	# Patrol room west wall around corridor doorway.
	_box("PatrolRoomWestWallNorthSegment", Vector3(10.0, 1.4, -2.75), Vector3(0.18, 2.8, 2.45), Color(0.13, 0.14, 0.145))
	_box("PatrolRoomWestWallSouthSegment", Vector3(10.0, 1.4, 0.95), Vector3(0.18, 2.8, 2.1), Color(0.13, 0.14, 0.145))
	_box("PatrolRoomDoorHeader", Vector3(10.0, 2.45, -0.8), Vector3(0.20, 0.7, 1.55), Color(0.10, 0.12, 0.12))
	# Corridor north doorway into the records room.
	_box("CorridorNorthWallLeftSegment", Vector3(4.30, 1.4, -2.15), Vector3(2.6, 2.8, 0.18), Color(0.16, 0.18, 0.18))
	_box("CorridorNorthWallRightSegment", Vector3(8.70, 1.4, -2.15), Vector3(2.6, 2.8, 0.18), Color(0.16, 0.18, 0.18))
	_box("CorridorRecordsDoorHeader", Vector3(6.5, 2.45, -2.15), Vector3(1.75, 0.7, 0.20), Color(0.10, 0.12, 0.12))
	_box("RecordsSouthWallLeftSegment", Vector3(4.85, 1.4, -2.15), Vector3(1.35, 2.8, 0.18), Color(0.14, 0.16, 0.155))
	_box("RecordsSouthWallRightSegment", Vector3(8.15, 1.4, -2.15), Vector3(1.35, 2.8, 0.18), Color(0.14, 0.16, 0.155))
	_box("RecordsDoorHeader", Vector3(6.5, 2.45, -2.15), Vector3(1.75, 0.7, 0.20), Color(0.10, 0.12, 0.12))
	# Rescue room south wall around connector doorway.
	_box("RescueRoomSouthWallLeftSegment", Vector3(11.0, 1.4, -5.3), Vector3(1.5, 2.8, 0.18), Color(0.15, 0.17, 0.16))
	_box("RescueRoomSouthWallRightSegment", Vector3(15.0, 1.4, -5.3), Vector3(1.5, 2.8, 0.18), Color(0.15, 0.17, 0.16))
	_box("RescueRoomDoorHeader", Vector3(13.0, 2.45, -5.3), Vector3(1.8, 0.7, 0.20), Color(0.10, 0.13, 0.13))
	# Rescue room north doorway into the exit stairwell.
	_box("RescueNorthWallLeftSegment", Vector3(11.18, 1.4, -10.3), Vector3(1.86, 2.8, 0.18), Color(0.15, 0.17, 0.16))
	_box("RescueNorthWallRightSegment", Vector3(14.82, 1.4, -10.3), Vector3(1.86, 2.8, 0.18), Color(0.15, 0.17, 0.16))
	_box("RescueExitDoorHeader", Vector3(13.0, 2.45, -10.3), Vector3(1.80, 0.7, 0.20), Color(0.10, 0.13, 0.13))
	_box("StairwellSouthWallLeftSegment", Vector3(11.0, 1.4, -10.3), Vector3(1.2, 2.8, 0.18), Color(0.115, 0.135, 0.13))
	_box("StairwellSouthWallRightSegment", Vector3(15.0, 1.4, -10.3), Vector3(1.2, 2.8, 0.18), Color(0.115, 0.135, 0.13))
	_box("StairwellDoorHeader", Vector3(13.0, 2.45, -10.3), Vector3(1.80, 0.7, 0.20), Color(0.08, 0.10, 0.10))

func _box(node_name: String, position: Vector3, size: Vector3, color: Color) -> StaticBody3D:
	var body := StaticBody3D.new()
	body.name = node_name
	body.position = position
	var mesh := MeshInstance3D.new()
	var box := BoxMesh.new()
	box.size = size
	mesh.mesh = box
	mesh.material_override = _material_for_node(node_name, color)
	body.add_child(mesh)
	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = size
	collision.shape = shape
	body.add_child(collision)
	add_child(body)
	return body

func _rounded_box(node_name: String, position: Vector3, size: Vector3, color: Color, radius: float = 0.12) -> StaticBody3D:
	var body := StaticBody3D.new()
	body.name = node_name
	body.position = position
	var mat := _material_for_node(node_name, color)
	var r: float = minf(radius, minf(size.x * 0.45, size.z * 0.45))
	_add_box_mesh_child(body, "SoftCoreX", Vector3(max(0.01, size.x - r * 2.0), size.y, size.z), Vector3.ZERO, mat)
	_add_box_mesh_child(body, "SoftCoreZ", Vector3(size.x, size.y, max(0.01, size.z - r * 2.0)), Vector3.ZERO, mat)
	for sx in [-1.0, 1.0]:
		for sz in [-1.0, 1.0]:
			var corner := MeshInstance3D.new()
			corner.name = "SoftCorner"
			var cylinder := CylinderMesh.new()
			cylinder.top_radius = r
			cylinder.bottom_radius = r
			cylinder.height = size.y
			cylinder.radial_segments = 20
			corner.mesh = cylinder
			corner.position = Vector3(sx * (size.x * 0.5 - r), 0.0, sz * (size.z * 0.5 - r))
			corner.material_override = mat
			body.add_child(corner)
	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = size
	collision.shape = shape
	body.add_child(collision)
	add_child(body)
	return body

func _soft_visual_box(node_name: String, position: Vector3, size: Vector3, color: Color, radius: float = 0.10) -> Node3D:
	var root := Node3D.new()
	root.name = node_name
	root.position = position
	var mat := _material_for_node(node_name, color)
	var r: float = minf(radius, minf(size.x * 0.45, size.z * 0.45))
	_add_box_mesh_child(root, "SoftVisualCoreX", Vector3(max(0.01, size.x - r * 2.0), size.y, size.z), Vector3.ZERO, mat)
	_add_box_mesh_child(root, "SoftVisualCoreZ", Vector3(size.x, size.y, max(0.01, size.z - r * 2.0)), Vector3.ZERO, mat)
	for sx in [-1.0, 1.0]:
		for sz in [-1.0, 1.0]:
			var corner := MeshInstance3D.new()
			corner.name = "SoftVisualCorner"
			var cylinder := CylinderMesh.new()
			cylinder.top_radius = r
			cylinder.bottom_radius = r
			cylinder.height = size.y
			cylinder.radial_segments = 24
			corner.mesh = cylinder
			corner.position = Vector3(sx * (size.x * 0.5 - r), 0.0, sz * (size.z * 0.5 - r))
			corner.material_override = mat
			root.add_child(corner)
	add_child(root)
	return root

func _add_box_mesh_child(parent: Node3D, node_name: String, size: Vector3, position: Vector3, material: Material) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	mesh_instance.name = node_name
	mesh_instance.position = position
	var box := BoxMesh.new()
	box.size = size
	mesh_instance.mesh = box
	mesh_instance.material_override = material
	parent.add_child(mesh_instance)
	return mesh_instance

func _material_for_node(node_name: String, color: Color) -> StandardMaterial3D:
	if "Brick" in node_name:
		return _pbr_material("Bricks097", Color(0.88, 0.82, 0.74), Vector2(1.25, 2.25), 0.86, 0.78)
	if "Carpet" in node_name or "Rug" in node_name:
		return _pbr_material("Carpet016", Color(0.55, 0.62, 0.54), Vector2(2.2, 1.4), 0.95, 0.55)
	if "WallSign" in node_name:
		var sign := StandardMaterial3D.new()
		sign.albedo_color = color
		sign.roughness = 0.48
		sign.emission_enabled = true
		sign.emission = color
		sign.emission_energy_multiplier = 0.12
		return sign
	if "OutsideLitWindow" in node_name:
		var lit := StandardMaterial3D.new()
		lit.albedo_color = color
		lit.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
		lit.shading_mode = BaseMaterial3D.SHADING_MODE_UNSHADED
		lit.emission_enabled = true
		lit.emission = Color(0.85, 0.72, 0.44)
		lit.emission_energy_multiplier = 0.8
		return lit
	if "Floor" in node_name:
		if "RescueRoom" in node_name or "SecurityOffice" in node_name:
			return _pbr_material("Tiles141", Color(0.62, 0.63, 0.58), Vector2(4.2, 4.2), 0.88, 0.42)
		return _pbr_material("Concrete048", Color(0.64, 0.61, 0.54), Vector2(3.2, 3.2), 0.90, 0.46)
	if "Cover" in node_name or "Table" in node_name or "DoorWood" in node_name:
		return _pbr_material("Wood095", Color(0.52, 0.42, 0.30), Vector2(2.5, 1.6), 0.74, 0.35)
	if "Ceiling" in node_name:
		return _pbr_material("Concrete048", Color(0.42, 0.43, 0.40), Vector2(2.0, 2.0), 0.92, 0.28)
	if "Wall" in node_name or "DoorHeader" in node_name or "Locked" in node_name:
		return _pbr_material("PaintedPlaster017", Color(0.56, 0.62, 0.60), Vector2(1.65, 2.1), 0.90, 0.42)
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	mat.roughness = 0.86
	mat.metallic = 0.0
	if color.a < 1.0:
		mat.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
	if "Lamp" in node_name:
		mat.albedo_color = Color(0.82, 0.86, 0.72)
		mat.emission_enabled = true
		mat.emission = Color(0.85, 0.86, 0.62)
		mat.emission_energy_multiplier = 0.8
	return mat

func _pbr_material(asset_id: String, tint: Color, uv_scale: Vector2, roughness: float, normal_scale_value: float) -> StandardMaterial3D:
	var mat := StandardMaterial3D.new()
	mat.albedo_color = tint
	mat.roughness = roughness
	mat.metallic = 0.0
	mat.albedo_texture = load(PBR_TEXTURES + asset_id + "_color.jpg")
	var normal := load(PBR_TEXTURES + asset_id + "_normal.jpg")
	if normal:
		mat.normal_enabled = true
		mat.normal_texture = normal
		mat.normal_scale = normal_scale_value
	var roughness_map := load(PBR_TEXTURES + asset_id + "_roughness.jpg")
	if roughness_map:
		mat.roughness_texture = roughness_map
	mat.uv1_scale = Vector3(uv_scale.x, uv_scale.y, 1.0)
	return mat

func _create_door(position: Vector3, locked: bool = false, required_item: String = "") -> void:
	var door := preload("res://scripts/world/Door.gd").new()
	var hinge_offset := Vector3(0, 0, -0.59)
	var panel_offset := Vector3(0, 1.05, 0.59)
	var latch_z := 1.02
	var hinge_z := 0.035
	door.position = position + hinge_offset
	door.locked = locked
	door.required_item = required_item
	door.focus_offset = Vector3(0, 1.05, latch_z)
	var panel := MeshInstance3D.new()
	panel.name = "DoorPanel"
	var box := BoxMesh.new()
	box.size = Vector3(0.16, 2.10, 1.18)
	panel.mesh = box
	panel.position = panel_offset
	panel.material_override = _material_for_node("DoorWood", Color(0.18, 0.12, 0.08))
	door.add_child(panel)
	var trim_mat := _material_for_node("DoorWood", Color(0.13, 0.075, 0.045))
	for side in [-1.0, 1.0]:
		for y in [0.62, 1.46]:
			var inset := MeshInstance3D.new()
			inset.name = "DoorInsetPanel"
			var inset_box := BoxMesh.new()
			inset_box.size = Vector3(0.018, 0.46, 0.58)
			inset.mesh = inset_box
			inset.position = Vector3(side * 0.091, y, 0.58)
			inset.material_override = trim_mat
			door.add_child(inset)
	var handle_mat := StandardMaterial3D.new()
	handle_mat.albedo_color = Color(0.82, 0.66, 0.34)
	handle_mat.roughness = 0.34
	handle_mat.metallic = 0.55
	for side in [-1.0, 1.0]:
		var latch_plate := MeshInstance3D.new()
		latch_plate.name = "DoorLatchPlate"
		var plate_box := BoxMesh.new()
		plate_box.size = Vector3(0.018, 0.28, 0.13)
		latch_plate.mesh = plate_box
		latch_plate.position = Vector3(side * 0.096, 1.08, latch_z)
		latch_plate.material_override = handle_mat
		door.add_child(latch_plate)
		var handle := MeshInstance3D.new()
		handle.name = "DoorHandle"
		var handle_box := BoxMesh.new()
		handle_box.size = Vector3(0.05, 0.12, 0.22)
		handle.mesh = handle_box
		handle.position = Vector3(side * 0.115, 1.08, latch_z)
		handle.material_override = handle_mat
		door.add_child(handle)
	for y in [0.38, 1.05, 1.72]:
		_add_cylinder_mesh(door, "DoorHingeKnuckle", Vector3(0.0, y, hinge_z), 0.045, 0.30, handle_mat)
	var collision := CollisionShape3D.new()
	var shape := BoxShape3D.new()
	shape.size = Vector3(0.18, 2.10, 1.18)
	collision.shape = shape
	collision.position = panel_offset
	door.add_child(collision)
	add_child(door)

func _create_cover(position: Vector3, size: Vector3) -> void:
	_rounded_box("CoverRounded", position, size, Color(0.12, 0.11, 0.09), 0.16)

func _create_props() -> void:
	for pos in [Vector3(-0.8, 0.45, -1.8), Vector3(2.0, 0.45, -1.7), Vector3(6.8, 0.38, -0.2), Vector3(13.9, 0.38, -6.2)]:
		_rounded_box("LowTableRounded", pos, Vector3(1.1, 0.28, 0.55), Color(0.16, 0.10, 0.07), 0.11)
	for pos in [Vector3(0.1, 1.1, -2.35), Vector3(7.0, 1.1, -2.05)]:
		_visual_box("WallSign", pos, Vector3(0.9, 0.42, 0.035), Color(0.04, 0.30, 0.27))
	_create_arrow_marker(Vector3(2.4, 0.04, -0.9), -90)
	_create_arrow_marker(Vector3(9.6, 0.04, -1.0), -90)
	_create_arrow_marker(Vector3(13.0, 0.04, -5.3), 180)
	_create_arrow_marker(Vector3(13.0, 0.04, -10.15), 180)
	_add_model("box-large.glb", Vector3(1.15, 0.02, -1.55), Vector3.ZERO, Vector3(0.48, 0.48, 0.48))
	_add_model("box-wide.glb", Vector3(5.7, 0.02, -1.45), Vector3(0, 22, 0), Vector3(0.46, 0.46, 0.46))
	_add_model("machine.glb", Vector3(11.0, 0.0, 1.25), Vector3(0, -90, 0), Vector3(0.9, 0.9, 0.9))
	_add_model("robot-arm-a.glb", Vector3(14.6, 0.0, 1.35), Vector3(0, -135, 0), Vector3(0.55, 0.55, 0.55))
	_add_model("conveyor-bars-stripe.glb", Vector3(13.0, 0.03, 1.35), Vector3(0, 90, 0), Vector3(0.62, 0.62, 0.62))
	_add_model("screen-panel-wide.glb", Vector3(14.7, 1.15, -3.0), Vector3(0, 180, 0), Vector3(0.7, 0.7, 0.7))
	_add_model("screen-wide.glb", Vector3(6.4, 1.15, 2.74), Vector3(0, 180, 0), Vector3(0.55, 0.55, 0.55))
	_add_model("pipe-large-long.glb", Vector3(8.4, 2.35, -1.95), Vector3(0, 90, 0), Vector3(0.65, 0.65, 0.65))
	_add_model("pipe-large-valve.glb", Vector3(14.7, 1.55, -6.2), Vector3(0, 0, 90), Vector3(0.45, 0.45, 0.45))
	_add_model("warning-orange.glb", Vector3(11.2, 0.02, -3.0), Vector3.ZERO, Vector3(0.5, 0.5, 0.5))
	_make_visual_only(_add_model("button-floor-round.glb", Vector3(13.0, 0.03, -5.45), Vector3.ZERO, Vector3(0.42, 0.42, 0.42)))
	_add_model("indicator-special-arrow.glb", Vector3(13.0, 0.08, -4.2), Vector3(0, 180, 0), Vector3(0.85, 0.85, 0.85))
	_add_model("catwalk-straight.glb", Vector3(17.7, 0.04, -1.0), Vector3(0, 90, 0), Vector3(0.55, 0.55, 0.55))
	_make_visual_only(_add_model("structure-doorway-wide.glb", Vector3(3.05, 0.0, -0.9), Vector3(0, 90, 0), Vector3(1.15, 1.15, 1.15)))
	_make_visual_only(_add_model("structure-doorway-wide.glb", Vector3(9.8, 0.0, -1.0), Vector3(0, 90, 0), Vector3(1.15, 1.15, 1.15)))
	_make_visual_only(_add_model("structure-doorway-wide.glb", Vector3(13.0, 0.0, -10.3), Vector3.ZERO, Vector3(1.08, 1.08, 1.08)))
	_create_furniture_set()
	_create_real_room_details()

func _add_model(file_name: String, position: Vector3, rotation_degrees_value: Vector3, scale_value: Vector3) -> Node3D:
	var packed := load(FACTORY_MODELS + file_name)
	if not packed:
		return null
	var model: Node3D = packed.instantiate()
	model.position = position
	model.rotation_degrees = rotation_degrees_value
	model.scale = scale_value * FACTORY_MODEL_SCALE_BOOST
	add_child(model)
	return model

func _make_visual_only(node: Node) -> void:
	if not node:
		return
	if node is CollisionObject3D:
		(node as CollisionObject3D).collision_layer = 0
		(node as CollisionObject3D).collision_mask = 0
	if node is CollisionShape3D:
		(node as CollisionShape3D).disabled = true
	for child in node.get_children():
		_make_visual_only(child)

func _add_furniture_model(file_name: String, position: Vector3, rotation_degrees_value: Vector3, scale_value: Vector3) -> Node3D:
	var packed := load(FURNITURE_MODELS + file_name)
	if not packed:
		return null
	var model: Node3D = packed.instantiate()
	model.position = position
	model.rotation_degrees = rotation_degrees_value
	model.scale = scale_value * FURNITURE_MODEL_SCALE_BOOST * _furniture_extra_scale(file_name)
	add_child(model)
	if _furniture_should_be_visual_only(file_name):
		_make_visual_only(model)
	return model

func _furniture_extra_scale(file_name: String) -> float:
	if "bench" in file_name:
		return BENCH_MODEL_SCALE_BOOST
	if "chair" in file_name:
		return CHAIR_MODEL_SCALE_BOOST
	if "rug" in file_name:
		return RUG_MODEL_SCALE_BOOST
	if "sideTable" in file_name:
		return SIDE_TABLE_MODEL_SCALE_BOOST
	if "lampRoundTable" in file_name:
		return TABLE_LAMP_MODEL_SCALE_BOOST
	return 1.0

func _furniture_should_be_visual_only(file_name: String) -> bool:
	for token in ["chair", "rug", "lampRound", "plantSmall", "books"]:
		if token in file_name:
			return true
	return false

func _create_furniture_set() -> void:
	_add_furniture_model("benchCushion.glb", Vector3(-1.1, 0.0, 2.05), Vector3(0, 180, 0), Vector3(0.72, 0.72, 0.72))
	_add_furniture_model("chairRounded.glb", Vector3(1.65, 0.0, 1.85), Vector3(0, -45, 0), Vector3(0.72, 0.72, 0.72))
	_add_furniture_model("coatRackStanding.glb", Vector3(-2.35, 0.0, 1.9), Vector3.ZERO, Vector3(0.60, 0.60, 0.60))
	_add_furniture_model("pottedPlant.glb", Vector3(2.35, 0.0, 2.0), Vector3.ZERO, Vector3(0.55, 0.55, 0.55))
	_add_furniture_model("desk.glb", Vector3(6.55, 0.0, 2.45), Vector3(0, 180, 0), Vector3(0.68, 0.68, 0.68))
	_add_furniture_model("chairDesk.glb", Vector3(6.55, 0.0, 1.58), Vector3.ZERO, Vector3(0.68, 0.68, 0.68))
	_add_furniture_model("computerScreen.glb", Vector3(6.25, 0.82, 2.35), Vector3(0, 180, 0), Vector3(0.42, 0.42, 0.42))
	_add_furniture_model("computerKeyboard.glb", Vector3(6.55, 0.83, 2.05), Vector3(0, 180, 0), Vector3(0.38, 0.38, 0.38))
	_add_furniture_model("bookcaseOpen.glb", Vector3(4.95, 0.0, 1.72), Vector3(0, 90, 0), Vector3(0.68, 0.68, 0.68))
	_add_furniture_model("books.glb", Vector3(4.92, 1.05, 1.7), Vector3(0, 90, 0), Vector3(0.42, 0.42, 0.42))
	_add_furniture_model("bookcaseClosedWide.glb", Vector3(4.55, 0.0, -4.95), Vector3(0, 90, 0), Vector3(0.66, 0.66, 0.66))
	_add_furniture_model("bookcaseOpen.glb", Vector3(8.48, 0.0, -4.95), Vector3(0, -90, 0), Vector3(0.66, 0.66, 0.66))
	_add_furniture_model("desk.glb", Vector3(6.5, 0.0, -5.55), Vector3.ZERO, Vector3(0.62, 0.62, 0.62))
	_add_furniture_model("chairDesk.glb", Vector3(6.5, 0.0, -4.82), Vector3(0, 180, 0), Vector3(0.64, 0.64, 0.64))
	_add_furniture_model("books.glb", Vector3(6.15, 0.86, -5.48), Vector3(0, 20, 0), Vector3(0.36, 0.36, 0.36))
	_add_furniture_model("sideTable.glb", Vector3(15.05, 0.0, -8.70), Vector3.ZERO, Vector3(0.72, 0.72, 0.72))
	_rounded_box("RescueNightstandSolid", Vector3(15.05, 0.40, -8.70), Vector3(0.72, 0.80, 0.58), Color(0.14, 0.09, 0.055), 0.08)
	_create_table_lamp(Vector3(15.05, 0.77, -8.70), Color(1.0, 0.72, 0.43), 1.25, 3.1)
	_add_furniture_model("bookcaseClosedWide.glb", Vector3(10.72, 0.0, -8.55), Vector3(0, 90, 0), Vector3(0.66, 0.66, 0.66))
	_add_furniture_model("tableCoffee.glb", Vector3(14.25, 0.0, -7.55), Vector3.ZERO, Vector3(0.50, 0.50, 0.50))
	_add_furniture_model("loungeDesignSofa.glb", Vector3(15.12, 0.0, -6.82), Vector3(0, -90, 0), Vector3(0.54, 0.54, 0.54))
	_add_furniture_model("cardboardBoxOpen.glb", Vector3(11.15, 0.0, -2.7), Vector3(0, 30, 0), Vector3(0.58, 0.58, 0.58))
	_add_furniture_model("cardboardBoxClosed.glb", Vector3(15.2, 0.0, 0.85), Vector3(0, -20, 0), Vector3(0.62, 0.62, 0.62))
	_add_furniture_model("radio.glb", Vector3(14.1, 0.95, 0.8), Vector3(0, -25, 0), Vector3(0.40, 0.40, 0.40))
	_add_furniture_model("lampRoundFloor.glb", Vector3(11.0, 0.0, 1.35), Vector3.ZERO, Vector3(0.64, 0.64, 0.64))
	_add_furniture_model("rugRectangle.glb", Vector3(0.3, 0.018, 1.55), Vector3.ZERO, Vector3(1.0, 1.0, 1.0))
	_add_furniture_model("rugRectangle.glb", Vector3(14.20, 0.018, -7.55), Vector3(0, 90, 0), Vector3(0.58, 0.58, 0.58))
	_add_furniture_model("rugRectangle.glb", Vector3(6.5, 0.018, -4.15), Vector3.ZERO, Vector3(0.74, 0.74, 0.74))
	_add_warm_lamp(Vector3(11.0, 1.4, 1.35), 0.8, 3.0)

func _create_real_room_details() -> void:
	_rounded_box("ReceptionCounterRounded", Vector3(1.25, 0.56, -1.95), Vector3(1.65, 1.12, 0.30), Color(0.18, 0.12, 0.075), 0.10)
	_visual_box("ReceptionKickPlate", Vector3(1.25, 0.28, -1.785), Vector3(1.42, 0.18, 0.025), Color(0.04, 0.055, 0.052))
	_add_furniture_model("chairCushion.glb", Vector3(0.25, 0.0, -1.62), Vector3(0, 180, 0), Vector3(0.66, 0.66, 0.66))
	_add_furniture_model("plantSmall1.glb", Vector3(2.5, 0.0, -1.9), Vector3.ZERO, Vector3(0.48, 0.48, 0.48))
	_add_furniture_model("cabinetTelevision.glb", Vector3(-2.35, 0.0, -1.75), Vector3(0, 90, 0), Vector3(0.62, 0.62, 0.62))
	_add_furniture_model("deskCorner.glb", Vector3(5.65, 0.0, 2.35), Vector3(0, 180, 0), Vector3(0.58, 0.58, 0.58))
	_add_furniture_model("computerMouse.glb", Vector3(6.88, 0.84, 2.02), Vector3(0, 170, 0), Vector3(0.35, 0.35, 0.35))
	_add_furniture_model("radio.glb", Vector3(7.32, 0.92, 2.38), Vector3(0, 145, 0), Vector3(0.34, 0.34, 0.34))
	_add_furniture_model("lampWall.glb", Vector3(8.0, 1.55, 1.55), Vector3(0, -90, 0), Vector3(0.46, 0.46, 0.46))
	_add_warm_lamp(Vector3(7.9, 1.55, 1.55), 0.35, 2.2)
	for data in [
		["bookcaseClosedWide.glb", Vector3(17.85, 0.0, -3.05), Vector3(0, -90, 0), Vector3(0.58, 0.58, 0.58)],
		["cardboardBoxClosed.glb", Vector3(18.65, 0.0, -2.25), Vector3(0, 16, 0), Vector3(0.55, 0.55, 0.55)],
		["cardboardBoxOpen.glb", Vector3(17.15, 0.0, 0.65), Vector3(0, -35, 0), Vector3(0.54, 0.54, 0.54)],
		["chairModernCushion.glb", Vector3(14.72, 0.0, -8.00), Vector3(0, -120, 0), Vector3(0.68, 0.68, 0.68)],
		["plantSmall2.glb", Vector3(10.75, 0.0, -6.05), Vector3.ZERO, Vector3(0.46, 0.46, 0.46)]
	]:
		_add_furniture_model(data[0], data[1], data[2], data[3])
	_rounded_box("MaintenanceWorkBenchRounded", Vector3(18.4, 0.48, 1.0), Vector3(1.15, 0.96, 0.36), Color(0.13, 0.10, 0.075), 0.10)
	_rounded_box("StorageShelfRounded", Vector3(16.25, 0.85, -2.8), Vector3(0.24, 1.7, 1.25), Color(0.06, 0.065, 0.06), 0.08)
	_box("StorageShelfB", Vector3(16.25, 1.48, -2.8), Vector3(0.34, 0.08, 1.35), Color(0.08, 0.08, 0.075))
	_rounded_box("BreakRoomCounterRounded", Vector3(11.05, 0.45, -6.2), Vector3(0.36, 0.9, 1.15), Color(0.16, 0.11, 0.08), 0.11)
	_visual_box("OfficeNoticeBoard", Vector3(6.5, 1.35, 0.365), Vector3(1.35, 0.72, 0.025), Color(0.20, 0.12, 0.055))

func _create_windows() -> void:
	_create_window(Vector3(-3.01, 1.55, 1.0), Vector3(0.05, 1.0, 1.55), Vector3(0, 0, 0))
	_create_window(Vector3(10.4, 1.55, -10.31), Vector3(1.6, 1.0, 0.05), Vector3(0, 0, 0))
	_create_window(Vector3(15.76, 1.55, -7.4), Vector3(0.05, 1.0, 1.5), Vector3(0, 0, 0))

func _create_window(position: Vector3, size: Vector3, _rotation: Vector3) -> void:
	_create_window_view(position, size)
	var glass := MeshInstance3D.new()
	glass.name = "WindowPane"
	glass.position = position
	var mesh := BoxMesh.new()
	mesh.size = size
	glass.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.42, 0.72, 0.86, 0.28)
	mat.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
	mat.roughness = 0.035
	mat.metallic = 0.0
	mat.emission_enabled = true
	mat.emission = Color(0.05, 0.17, 0.22)
	mat.emission_energy_multiplier = 0.15
	glass.material_override = mat
	glass.cast_shadow = GeometryInstance3D.SHADOW_CASTING_SETTING_OFF
	add_child(glass)
	_box("WindowFrame", position + Vector3(0, -0.56, 0), size + Vector3(0.08, -0.88, 0.08), Color(0.02, 0.025, 0.027))
	_box("WindowFrame", position + Vector3(0, 0.56, 0), size + Vector3(0.08, -0.88, 0.08), Color(0.02, 0.025, 0.027))
	if size.x > size.z:
		_box("WindowFrame", position + Vector3(-size.x * 0.5, 0, 0), Vector3(0.08, size.y + 0.08, size.z + 0.08), Color(0.02, 0.025, 0.027))
		_box("WindowFrame", position + Vector3(size.x * 0.5, 0, 0), Vector3(0.08, size.y + 0.08, size.z + 0.08), Color(0.02, 0.025, 0.027))
		_make_visual_only(_add_model("structure-window-wide.glb", position + Vector3(0, -0.52, 0.025 * sign(position.z)), Vector3(0, 0, 0), Vector3(0.30, 0.30, 0.30)))
	else:
		_box("WindowFrame", position + Vector3(0, 0, -size.z * 0.5), Vector3(size.x + 0.08, size.y + 0.08, 0.08), Color(0.02, 0.025, 0.027))
		_box("WindowFrame", position + Vector3(0, 0, size.z * 0.5), Vector3(size.x + 0.08, size.y + 0.08, 0.08), Color(0.02, 0.025, 0.027))
		_make_visual_only(_add_model("structure-window.glb", position + Vector3(0.025 * sign(position.x), -0.52, 0), Vector3(0, 90, 0), Vector3(0.30, 0.30, 0.30)))

func _create_window_view(position: Vector3, size: Vector3) -> void:
	var view := MeshInstance3D.new()
	view.name = "WindowStreetView"
	var mesh := BoxMesh.new()
	var horizontal := size.x > size.z
	mesh.size = Vector3(max(0.05, size.x - 0.18), max(0.05, size.y - 0.16), max(0.035, size.z - 0.01)) if horizontal else Vector3(max(0.035, size.x - 0.01), max(0.05, size.y - 0.16), max(0.05, size.z - 0.18))
	view.mesh = mesh
	var toward_center := (Vector3(7.5, position.y, -3.5) - position).normalized()
	view.position = position + toward_center * 0.035
	var mat := StandardMaterial3D.new()
	mat.shading_mode = BaseMaterial3D.SHADING_MODE_UNSHADED
	mat.albedo_color = Color(0.055, 0.085, 0.10)
	mat.emission_enabled = true
	mat.emission = Color(0.05, 0.09, 0.12)
	mat.emission_energy_multiplier = 0.65
	view.material_override = mat
	view.cast_shadow = GeometryInstance3D.SHADOW_CASTING_SETTING_OFF
	add_child(view)

func _create_weather() -> void:
	var rain_mat := StandardMaterial3D.new()
	rain_mat.albedo_color = Color(0.50, 0.72, 0.82, 0.55)
	rain_mat.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
	rain_mat.emission_enabled = true
	rain_mat.emission = Color(0.20, 0.35, 0.42)
	rain_mat.emission_energy_multiplier = 0.35
	for i in range(70):
		var drop := MeshInstance3D.new()
		var mesh := BoxMesh.new()
		mesh.size = Vector3(0.012, 0.75, 0.012)
		drop.mesh = mesh
		var x := -4.0 + float(i % 14) * 1.6
		var z := -11.5 + float(i / 14) * 1.25
		drop.position = Vector3(x, 1.7 + float(i % 5) * 0.45, z)
		drop.rotation_degrees.z = -12
		drop.material_override = rain_mat
		add_child(drop)

func _create_architectural_details() -> void:
	for pos in [Vector3(-2.9, 1.35, -2.4), Vector3(-2.9, 1.35, 2.4), Vector3(2.9, 1.35, 2.4), Vector3(10.1, 1.35, -3.9), Vector3(15.9, 1.35, 1.9), Vector3(10.4, 1.35, -10.2), Vector3(15.6, 1.35, -10.2)]:
		_create_corner_post(pos)
	for pos in [Vector3(0, 0.22, -2.36), Vector3(6.5, 0.22, -2.06), Vector3(13, 0.22, 1.92), Vector3(13, 0.22, -10.2), Vector3(6.5, 0.22, 0.46), Vector3(17.7, 0.22, 1.4)]:
		_visual_box("Baseboard", pos, Vector3(4.6, 0.16, 0.08), Color(0.035, 0.043, 0.043))
	for data in [
		[Vector3(3.0, 1.38, -0.78), "z"],
		[Vector3(10.0, 1.38, -0.8), "z"],
		[Vector3(6.5, 1.38, -2.15), "x"],
		[Vector3(13.0, 1.38, -5.3), "x"],
		[Vector3(13.0, 1.38, -10.3), "x"],
		[Vector3(6.45, 1.35, 0.31), "x"],
	]:
		_create_door_soft_trim(data[0], data[1])
	_create_extra_room_dressing()
	_create_room_interiors()

func _create_room_cove_edges(room_name: String, center: Vector3, size: Vector3) -> void:
	var trim_color := Color(0.050, 0.064, 0.062)
	var y := size.y - 0.08
	if room_name != "PatrolRoom":
		_cylinder_bar("%sNorthCove" % room_name, center + Vector3(0, y, -size.z * 0.5 + 0.06), 0.045, max(0.2, size.x - 0.12), "x", trim_color)
	if room_name != "RescueRoom":
		_cylinder_bar("%sSouthCove" % room_name, center + Vector3(0, y, size.z * 0.5 - 0.06), 0.045, max(0.2, size.x - 0.12), "x", trim_color)
	if room_name not in ["Corridor", "PatrolRoom", "RescueRoom"]:
		_cylinder_bar("%sWestCove" % room_name, center + Vector3(-size.x * 0.5 + 0.06, y, 0), 0.045, max(0.2, size.z - 0.12), "z", trim_color)
	if room_name not in ["StartRoom", "Corridor"]:
		_cylinder_bar("%sEastCove" % room_name, center + Vector3(size.x * 0.5 - 0.06, y, 0), 0.045, max(0.2, size.z - 0.12), "z", trim_color)

func _cylinder_bar(node_name: String, position: Vector3, radius: float, length: float, axis: String, color: Color) -> MeshInstance3D:
	var bar := MeshInstance3D.new()
	bar.name = node_name
	bar.position = position
	var mesh := CylinderMesh.new()
	mesh.top_radius = radius
	mesh.bottom_radius = radius
	mesh.height = length
	mesh.radial_segments = 18
	bar.mesh = mesh
	if axis == "x":
		bar.rotation_degrees.z = 90
	elif axis == "z":
		bar.rotation_degrees.x = 90
	bar.material_override = _material_for_node("WallTrim", color)
	add_child(bar)
	return bar

func _create_door_soft_trim(position: Vector3, width_axis: String) -> void:
	var trim_color := Color(0.036, 0.048, 0.047)
	if width_axis == "x":
		_cylinder_bar("DoorRoundedLeftJamb", position + Vector3(-0.78, 0.0, 0), 0.055, 2.45, "y", trim_color)
		_cylinder_bar("DoorRoundedRightJamb", position + Vector3(0.78, 0.0, 0), 0.055, 2.45, "y", trim_color)
		_cylinder_bar("DoorRoundedHeader", position + Vector3(0, 1.21, 0), 0.055, 1.56, "x", trim_color)
	else:
		_cylinder_bar("DoorRoundedLeftJamb", position + Vector3(0, 0.0, -0.78), 0.055, 2.45, "y", trim_color)
		_cylinder_bar("DoorRoundedRightJamb", position + Vector3(0, 0.0, 0.78), 0.055, 2.45, "y", trim_color)
		_cylinder_bar("DoorRoundedHeader", position + Vector3(0, 1.21, 0), 0.055, 1.56, "z", trim_color)

func _create_extra_room_dressing() -> void:
	for pos in [Vector3(-2.15, 0.0, 1.15), Vector3(-1.55, 0.0, 1.15), Vector3(0.95, 0.0, 2.05)]:
		_add_furniture_model("chairCushion.glb", pos, Vector3(0, 165, 0), Vector3(0.66, 0.66, 0.66))
	_add_furniture_model("chairCushion.glb", Vector3(14.68, 0.0, -6.55), Vector3(0, -135, 0), Vector3(0.62, 0.62, 0.62))
	for pos in [Vector3(5.35, 0.0, -0.15), Vector3(18.55, 0.0, -0.15)]:
		_add_furniture_model("plantSmall2.glb", pos, Vector3.ZERO, Vector3(0.54, 0.54, 0.54))
	_rounded_box("RoundishSideConsole", Vector3(4.95, 0.38, -0.22), Vector3(0.95, 0.76, 0.28), Color(0.12, 0.085, 0.062), 0.09)
	_rounded_box("RescueRoomCabinetSoft", Vector3(15.1, 0.42, -9.72), Vector3(0.38, 0.84, 1.05), Color(0.09, 0.075, 0.060), 0.10)

func _create_room_interiors() -> void:
	# Start/reception: lived-in desk wall, coat corner, and waiting-room clutter along the edges.
	_rounded_box("ReceptionFileCabinetA", Vector3(-2.42, 0.46, -1.05), Vector3(0.36, 0.92, 0.72), Color(0.075, 0.085, 0.080), 0.06)
	_rounded_box("ReceptionFileCabinetB", Vector3(-2.42, 0.46, -0.25), Vector3(0.36, 0.92, 0.72), Color(0.070, 0.078, 0.075), 0.06)
	_visual_box("ReceptionPaperStack", Vector3(-2.22, 0.94, -0.38), Vector3(0.30, 0.025, 0.22), Color(0.72, 0.68, 0.55))
	_visual_box("ReceptionClipboard", Vector3(1.55, 1.13, -1.78), Vector3(0.42, 0.025, 0.28), Color(0.22, 0.18, 0.13))
	_visual_box("ReceptionWallCalendar", Vector3(2.86, 1.55, 1.05), Vector3(0.028, 0.72, 0.54), Color(0.12, 0.17, 0.16))
	_visual_box("ReceptionWallCalendarPaper", Vector3(2.84, 1.43, 1.05), Vector3(0.028, 0.38, 0.44), Color(0.70, 0.67, 0.55))
	_rounded_box("WaitingSideTable", Vector3(-0.35, 0.34, 2.10), Vector3(0.58, 0.68, 0.44), Color(0.13, 0.08, 0.055), 0.08)
	_add_furniture_model("lampRoundTable.glb", Vector3(-0.35, 0.69, 2.10), Vector3.ZERO, Vector3(0.42, 0.42, 0.42))
	_add_warm_lamp(Vector3(-0.35, 1.07, 2.10), 0.38, 2.0)

	# Corridor/security: utility panels and storage so the hallway stops reading as empty.
	_visual_box("CorridorFireCabinet", Vector3(5.45, 1.12, -2.105), Vector3(0.62, 0.82, 0.035), Color(0.36, 0.065, 0.052))
	_visual_box("CorridorFirePanel", Vector3(5.45, 1.15, -2.126), Vector3(0.42, 0.48, 0.025), Color(0.20, 0.08, 0.065))
	_visual_box("CorridorFireCrossVertical", Vector3(5.45, 1.15, -2.142), Vector3(0.055, 0.30, 0.018), Color(0.84, 0.72, 0.50))
	_visual_box("CorridorFireCrossHorizontal", Vector3(5.45, 1.15, -2.144), Vector3(0.27, 0.055, 0.018), Color(0.84, 0.72, 0.50))
	_visual_box("CorridorCableTray", Vector3(6.55, 2.48, -2.08), Vector3(5.4, 0.08, 0.16), Color(0.045, 0.050, 0.048))
	_visual_box("CorridorFloorCableA", Vector3(7.1, 0.018, -1.72), Vector3(2.0, 0.018, 0.05), Color(0.018, 0.020, 0.021))
	_visual_box("CorridorFloorCableB", Vector3(6.25, 0.019, -1.55), Vector3(0.05, 0.018, 0.92), Color(0.018, 0.020, 0.021))
	_rounded_box("SecurityFilingPedestal", Vector3(7.85, 0.38, 2.35), Vector3(0.44, 0.76, 0.58), Color(0.060, 0.070, 0.067), 0.06)
	_visual_box("OfficeWallShelf", Vector3(5.7, 1.80, 0.39), Vector3(1.45, 0.08, 0.18), Color(0.12, 0.08, 0.055))
	_visual_box("OfficeWallBindersA", Vector3(5.25, 1.92, 0.32), Vector3(0.18, 0.34, 0.10), Color(0.17, 0.20, 0.24))
	_visual_box("OfficeWallBindersB", Vector3(5.48, 1.90, 0.32), Vector3(0.16, 0.30, 0.10), Color(0.28, 0.18, 0.10))
	_visual_box("OfficeWallBindersC", Vector3(5.72, 1.91, 0.32), Vector3(0.18, 0.32, 0.10), Color(0.10, 0.25, 0.20))
	_visual_box("RecordsDoorSign", Vector3(6.5, 1.62, -2.055), Vector3(0.82, 0.28, 0.026), Color(0.05, 0.22, 0.20))
	_visual_box("RecordsDoorSignGlow", Vector3(6.5, 1.62, -2.075), Vector3(0.46, 0.05, 0.018), Color(0.08, 0.64, 0.50))
	_visual_box("RecordsArchiveBoard", Vector3(6.5, 1.48, -6.035), Vector3(1.45, 0.72, 0.026), Color(0.12, 0.18, 0.17))
	_visual_box("RecordsPinnedNoteA", Vector3(6.08, 1.55, -6.055), Vector3(0.26, 0.28, 0.018), Color(0.70, 0.64, 0.45))
	_visual_box("RecordsPinnedNoteB", Vector3(6.50, 1.42, -6.055), Vector3(0.22, 0.24, 0.018), Color(0.48, 0.58, 0.52))
	_visual_box("RecordsPinnedNoteC", Vector3(6.92, 1.52, -6.055), Vector3(0.26, 0.28, 0.018), Color(0.58, 0.48, 0.38))
	_rounded_box("RecordsRollingCart", Vector3(5.25, 0.36, -3.05), Vector3(0.76, 0.72, 0.42), Color(0.08, 0.09, 0.085), 0.08)
	_visual_box("RecordsBoxStackA", Vector3(8.25, 0.24, -3.00), Vector3(0.56, 0.48, 0.46), Color(0.25, 0.18, 0.10))
	_visual_box("RecordsBoxStackB", Vector3(8.05, 0.62, -3.18), Vector3(0.48, 0.32, 0.40), Color(0.30, 0.22, 0.12))

	# Patrol room: industrial details and believable cover by the walls.
	_rounded_box("PatrolLockerA", Vector3(10.35, 0.82, 0.55), Vector3(0.40, 1.64, 0.72), Color(0.075, 0.088, 0.088), 0.06)
	_rounded_box("PatrolLockerB", Vector3(10.35, 0.82, 1.35), Vector3(0.40, 1.64, 0.72), Color(0.070, 0.082, 0.084), 0.06)
	_visual_box("PatrolLockerVentA", Vector3(10.58, 1.42, 0.55), Vector3(0.026, 0.16, 0.42), Color(0.030, 0.035, 0.036))
	_visual_box("PatrolLockerVentB", Vector3(10.58, 1.42, 1.35), Vector3(0.026, 0.16, 0.42), Color(0.030, 0.035, 0.036))
	_add_furniture_model("bench.glb", Vector3(13.45, 0.0, 1.60), Vector3(0, 90, 0), Vector3(0.56, 0.56, 0.56))
	_visual_box("PatrolToolPegboard", Vector3(10.08, 1.45, -2.25), Vector3(0.035, 0.82, 1.05), Color(0.13, 0.075, 0.045))
	_visual_box("PatrolToolStrip", Vector3(10.055, 1.52, -2.58), Vector3(0.035, 0.08, 0.34), Color(0.54, 0.48, 0.36))
	_visual_box("PatrolToolStrip", Vector3(10.055, 1.34, -2.20), Vector3(0.035, 0.08, 0.28), Color(0.44, 0.42, 0.36))
	_add_model("pipe-large-valve.glb", Vector3(10.25, 0.95, 1.45), Vector3(0, 90, 0), Vector3(0.52, 0.52, 0.52))
	_visual_box("PatrolStatusPanel", Vector3(14.35, 1.35, -3.92), Vector3(1.05, 0.52, 0.035), Color(0.025, 0.055, 0.058))
	_visual_box("PatrolStatusPanelGlow", Vector3(14.06, 1.35, -3.945), Vector3(0.16, 0.12, 0.020), Color(0.05, 0.50, 0.42))

	# Rescue room: more room-like composition while keeping the path to the civilian open.
	_rounded_box("RescueLowDresser", Vector3(10.62, 0.42, -7.25), Vector3(0.46, 0.84, 1.18), Color(0.12, 0.075, 0.052), 0.08)
	_rounded_box("RescueBedFrame", Vector3(11.80, 0.30, -9.76), Vector3(1.85, 0.38, 0.72), Color(0.13, 0.085, 0.055), 0.10)
	_soft_visual_box("RescueMattress", Vector3(11.80, 0.54, -9.76), Vector3(1.74, 0.23, 0.62), Color(0.54, 0.58, 0.54), 0.12)
	_soft_visual_box("RescuePillow", Vector3(11.10, 0.71, -9.76), Vector3(0.38, 0.12, 0.44), Color(0.68, 0.66, 0.58), 0.09)
	_soft_visual_box("RescueFoldedBlanket", Vector3(12.35, 0.72, -9.76), Vector3(0.54, 0.08, 0.50), Color(0.36, 0.16, 0.14), 0.08)
	_rounded_box("RescueSmallOttoman", Vector3(14.58, 0.26, -7.15), Vector3(0.52, 0.52, 0.48), Color(0.20, 0.11, 0.085), 0.12)
	_visual_box("RescueCoffeeTray", Vector3(14.25, 0.55, -7.55), Vector3(0.48, 0.035, 0.30), Color(0.42, 0.32, 0.19))
	_create_mug(Vector3(14.06, 0.62, -7.48), Color(0.50, 0.44, 0.36))
	_soft_visual_box("RescueFloorCushion", Vector3(13.45, 0.08, -8.15), Vector3(0.56, 0.16, 0.50), Color(0.16, 0.24, 0.22), 0.11)
	_visual_box("RescueCableAlongWall", Vector3(10.42, 0.08, -8.10), Vector3(0.05, 0.05, 1.50), Color(0.018, 0.020, 0.020))
	_visual_box("RescueWallPhotoA", Vector3(10.27, 1.60, -8.55), Vector3(0.032, 0.48, 0.42), Color(0.16, 0.20, 0.19))
	_visual_box("RescueWallPhotoB", Vector3(10.27, 1.52, -7.92), Vector3(0.032, 0.42, 0.34), Color(0.20, 0.16, 0.13))
	_visual_box("RescueCurtainRail", Vector3(15.58, 2.16, -7.42), Vector3(0.05, 0.05, 1.56), Color(0.055, 0.055, 0.050))
	_visual_box("RescueCurtain", Vector3(15.54, 1.48, -7.05), Vector3(0.045, 1.15, 0.44), Color(0.18, 0.12, 0.10, 0.82))
	_visual_box("RescueCurtain", Vector3(15.54, 1.48, -7.82), Vector3(0.045, 1.15, 0.44), Color(0.18, 0.12, 0.10, 0.82))
	_visual_box("RescueExitSign", Vector3(13.0, 1.72, -10.205), Vector3(0.84, 0.30, 0.026), Color(0.02, 0.34, 0.24))
	_visual_box("RescueExitSignArrow", Vector3(13.0, 1.72, -10.228), Vector3(0.42, 0.045, 0.018), Color(0.10, 0.90, 0.62))

	# Exit stairwell: readable final destination with stairs, railings, and emergency hardware.
	for i in range(5):
		_visual_box(
			"ExitStairStep",
			Vector3(13.65, 0.08 + i * 0.13, -13.10 + i * 0.24),
			Vector3(1.35, 0.16, 0.35),
			Color(0.12, 0.15, 0.14)
		)
	_cylinder_bar("ExitStairHandrailA", Vector3(12.35, 1.02, -12.65), 0.035, 2.15, "z", Color(0.50, 0.46, 0.36))
	_cylinder_bar("ExitStairHandrailB", Vector3(14.45, 1.02, -12.65), 0.035, 2.15, "z", Color(0.50, 0.46, 0.36))
	_soft_visual_box("ExitFireDoor", Vector3(13.0, 1.14, -13.62), Vector3(1.25, 2.18, 0.08), Color(0.08, 0.20, 0.17), 0.035)
	_soft_visual_box("ExitDoorInsetTop", Vector3(13.0, 1.60, -13.565), Vector3(0.78, 0.54, 0.025), Color(0.055, 0.15, 0.13), 0.018)
	_soft_visual_box("ExitDoorInsetBottom", Vector3(13.0, 0.74, -13.565), Vector3(0.78, 0.54, 0.025), Color(0.055, 0.15, 0.13), 0.018)
	_visual_box("ExitPushBar", Vector3(13.0, 1.08, -13.575), Vector3(0.82, 0.055, 0.035), Color(0.72, 0.64, 0.38))
	_visual_box("ExitWallPlan", Vector3(11.02, 1.42, -11.45), Vector3(0.026, 0.72, 0.56), Color(0.055, 0.15, 0.15))
	_visual_box("ExitPlanRouteLine", Vector3(11.00, 1.42, -11.45), Vector3(0.018, 0.045, 0.42), Color(0.08, 0.72, 0.50))
	_visual_box("ExitEmergencyBox", Vector3(15.08, 1.02, -11.45), Vector3(0.035, 0.50, 0.42), Color(0.34, 0.055, 0.045))
	_visual_box("ExitEmergencyGlass", Vector3(15.105, 1.02, -11.45), Vector3(0.020, 0.32, 0.25), Color(0.22, 0.40, 0.38, 0.55))

	# Maintenance bay: shelves, spare parts, and work-surface clutter.
	_visual_box("MaintenanceToolWall", Vector3(19.28, 1.48, 0.65), Vector3(0.035, 0.82, 1.22), Color(0.10, 0.07, 0.045))
	_visual_box("MaintenanceShelfTop", Vector3(17.15, 1.78, -3.90), Vector3(1.35, 0.08, 0.30), Color(0.08, 0.075, 0.065))
	_visual_box("MaintenanceShelfMid", Vector3(17.15, 1.20, -3.90), Vector3(1.35, 0.08, 0.30), Color(0.08, 0.075, 0.065))
	_add_model("cog-a.glb", Vector3(18.78, 0.98, 0.98), Vector3(0, 20, 0), Vector3(0.34, 0.34, 0.34))
	_add_model("box-small.glb", Vector3(18.20, 1.00, 0.92), Vector3(0, -20, 0), Vector3(0.35, 0.35, 0.35))
	_visual_box("MaintenanceRolledMat", Vector3(18.95, 0.12, -1.52), Vector3(0.95, 0.10, 0.26), Color(0.04, 0.09, 0.085))

func _create_wear_marks() -> void:
	for data in [
		[Vector3(0.2, 0.012, -0.7), Vector3(1.6, 0.018, 0.32), Color(0.02, 0.028, 0.025, 0.62)],
		[Vector3(7.3, 0.012, -1.1), Vector3(1.2, 0.018, 0.25), Color(0.015, 0.022, 0.021, 0.58)],
		[Vector3(12.7, 0.012, -1.9), Vector3(1.7, 0.018, 0.36), Color(0.018, 0.018, 0.016, 0.60)],
		[Vector3(13.5, 0.012, -7.2), Vector3(1.0, 0.018, 0.32), Color(0.02, 0.035, 0.031, 0.58)]
	]:
		_visual_box("FloorScuff", data[0], data[1], data[2])
	for data in [
		[Vector3(2.91, 1.25, 1.7), Vector3(0.018, 0.46, 0.82), Color(0.018, 0.025, 0.024, 0.72)],
		[Vector3(10.08, 1.8, 1.2), Vector3(0.018, 0.52, 0.75), Color(0.02, 0.022, 0.024, 0.70)],
		[Vector3(15.68, 1.45, -8.4), Vector3(0.018, 0.48, 0.88), Color(0.018, 0.030, 0.029, 0.68)]
	]:
		_visual_box("WallScuff", data[0], data[1], data[2])

func _visual_box(node_name: String, position: Vector3, size: Vector3, color: Color) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	mesh_instance.name = node_name
	mesh_instance.position = position
	var mesh := BoxMesh.new()
	mesh.size = size
	mesh_instance.mesh = mesh
	if "Scuff" in node_name:
		var material := StandardMaterial3D.new()
		material.albedo_color = color
		material.roughness = 0.95
		if color.a < 1.0:
			material.transparency = BaseMaterial3D.TRANSPARENCY_ALPHA
		mesh_instance.material_override = material
	else:
		mesh_instance.material_override = _material_for_node(node_name, color)
	add_child(mesh_instance)
	return mesh_instance

func _create_corner_post(position: Vector3) -> void:
	var post := MeshInstance3D.new()
	post.name = "RoundedCornerPost"
	post.position = position
	var mesh := CylinderMesh.new()
	mesh.top_radius = 0.07
	mesh.bottom_radius = 0.07
	mesh.height = 2.7
	mesh.radial_segments = 16
	post.mesh = mesh
	post.material_override = _material_for_node("WallTrim", Color(0.055, 0.068, 0.067))
	add_child(post)

func _create_arrow_marker(position: Vector3, yaw: float) -> void:
	var marker := MeshInstance3D.new()
	var prism := PrismMesh.new()
	prism.size = Vector3(0.55, 0.04, 0.9)
	marker.mesh = prism
	marker.position = position
	marker.rotation_degrees.y = yaw
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.08, 0.50, 0.42)
	mat.emission_enabled = true
	mat.emission = Color(0.04, 0.34, 0.28)
	mat.emission_energy_multiplier = 0.4
	marker.material_override = mat
	add_child(marker)

func _create_safe_zone() -> void:
	var zone := Area3D.new()
	zone.name = "SafeZone"
	zone.position = safe_zone_pos
	zone.add_to_group("safe_zone")
	var shape := CollisionShape3D.new()
	var box := BoxShape3D.new()
	box.size = Vector3(2.5, 2.0, 2.5)
	shape.shape = box
	zone.add_child(shape)
	add_child(zone)
	_box("SafeZoneGlow", safe_zone_pos + Vector3(0, 0.02, 0), Vector3(2.7, 0.04, 2.7), Color(0.03, 0.24, 0.22))
	var light := OmniLight3D.new()
	light.position = safe_zone_pos + Vector3(0, 1.7, 0)
	light.light_color = Color(0.22, 0.76, 0.66)
	light.light_energy = 0.55
	light.omni_range = 3.2
	add_child(light)

func _create_exit_zone() -> void:
	var exit := Area3D.new()
	exit.name = "ExitTrigger"
	exit.position = safe_zone_pos
	var shape := CollisionShape3D.new()
	var box := BoxShape3D.new()
	box.size = Vector3(3, 2.4, 1.2)
	shape.shape = box
	exit.add_child(shape)
	exit.body_entered.connect(_on_exit_body_entered)
	add_child(exit)

func _create_tension_scene() -> void:
	var wall_shadow := _box("ShadowWall", Vector3(9.9, 1.1, -2.25), Vector3(0.05, 1.8, 1.4), Color(0.018, 0.019, 0.020))
	wall_shadow.rotation_degrees.y = 0
	var silhouette := MeshInstance3D.new()
	var mesh := CapsuleMesh.new()
	mesh.radius = 0.22
	mesh.height = 1.25
	silhouette.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.0, 0.0, 0.0, 0.88)
	silhouette.material_override = mat
	silhouette.position = Vector3(9.85, 1.0, -2.25)
	silhouette.scale = Vector3(0.08, 1.0, 1.9)
	add_child(silhouette)
	var light := SpotLight3D.new()
	light.position = Vector3(12.4, 1.9, -3.4)
	light.light_color = Color(0.9, 0.56, 0.32)
	light.light_energy = 2.0
	light.spot_range = 7.0
	add_child(light)
	light.look_at(Vector3(9.9, 1.1, -2.25), Vector3.UP)
	AudioManager.play_sfx("voice")

func _create_throwables() -> void:
	var positions := [
		Vector3(1.6, 0.45, 1.4),
		Vector3(5.2, 0.45, -1.5),
		Vector3(11.8, 0.45, 1.3),
		Vector3(14.1, 0.55, 0.8),
		Vector3(5.25, 0.86, -3.05),
		Vector3(12.25, 0.45, -11.15),
	]
	var models := [
		FURNITURE_MODELS + "laptop.glb",
		FURNITURE_MODELS + "cardboardBoxClosed.glb",
		FACTORY_MODELS + "cog-a.glb",
		FURNITURE_MODELS + "radio.glb",
		FURNITURE_MODELS + "books.glb",
		FACTORY_MODELS + "box-small.glb",
	]
	for i in range(positions.size()):
		var item := preload("res://scripts/world/ThrowableItem.gd").new()
		item.display_name_key = ["item.laptop", "item.cardboard_box", "item.metal_cog", "item.radio", "item.cardboard_box", "item.cardboard_box"][i]
		item.model_path = models[i]
		if "laptop" in models[i]:
			item.model_scale = Vector3(0.42, 0.42, 0.42)
			item.collision_size = Vector3(0.46, 0.24, 0.36)
			item.throw_loudness = 16.0
			item.drop_loudness = 3.0
		elif "radio" in models[i]:
			item.model_scale = Vector3(0.38, 0.38, 0.38)
			item.collision_size = Vector3(0.34, 0.30, 0.24)
			item.throw_loudness = 18.0
			item.drop_loudness = 5.0
		elif "cardboard" in models[i]:
			item.throw_loudness = 8.0
			item.drop_loudness = 2.2
		elif "books" in models[i]:
			item.model_scale = Vector3(0.34, 0.34, 0.34)
			item.collision_size = Vector3(0.34, 0.18, 0.28)
			item.throw_loudness = 7.0
			item.drop_loudness = 2.0
		elif "box-small" in models[i]:
			item.model_scale = Vector3(0.42, 0.42, 0.42)
			item.collision_size = Vector3(0.38, 0.34, 0.34)
			item.throw_loudness = 9.0
			item.drop_loudness = 2.4
		else:
			item.model_scale = Vector3(0.52, 0.52, 0.52)
			item.collision_size = Vector3(0.42, 0.42, 0.42)
			item.throw_loudness = 20.0
			item.drop_loudness = 6.0
		item.position = positions[i]
		add_child(item)

func _create_key_items() -> void:
	var badge := preload("res://scripts/world/KeycardItem.gd").new()
	badge.position = Vector3(1.25, 1.15, -1.96)
	badge.rotation_degrees.y = 12
	add_child(badge)
	var light := OmniLight3D.new()
	light.name = "SecurityBadgeGlint"
	light.position = badge.position + Vector3(0, 0.18, 0)
	light.light_color = Color(0.95, 0.72, 0.28)
	light.light_energy = 0.18
	light.omni_range = 1.2
	add_child(light)

func _add_path_lights() -> void:
	for data in [
		["StartRoom", Vector3(0.15, 2.34, 0.10), Color(0.90, 0.74, 0.48), 1.10, 4.7],
		["Corridor", Vector3(6.5, 2.24, -1.0), Color(0.72, 0.86, 0.80), 0.82, 4.2],
		["RecordsRoom", Vector3(6.5, 2.30, -4.20), Color(0.86, 0.76, 0.58), 0.86, 3.9],
		["SecurityOffice", Vector3(6.55, 2.28, 1.68), Color(0.95, 0.70, 0.42), 0.72, 3.4],
		["PatrolRoom", Vector3(12.5, 2.34, -1.10), Color(0.88, 0.76, 0.56), 1.05, 5.0],
		["MaintenanceBay", Vector3(17.7, 2.30, -1.0), Color(0.82, 0.90, 0.78), 0.90, 4.1],
		["Connector", Vector3(13.0, 2.22, -4.65), Color(0.74, 0.86, 0.82), 0.58, 2.7],
		["RescueRoom", Vector3(13.15, 2.34, -7.75), Color(1.0, 0.72, 0.44), 0.96, 4.4],
		["ExitStairwell", Vector3(13.0, 2.24, -12.25), Color(0.64, 0.96, 0.74), 0.82, 3.8],
	]:
		_create_ceiling_fixture(data[0], data[1], data[2], data[3], data[4])
	var emergency := OmniLight3D.new()
	emergency.position = Vector3(10.6, 1.7, -2.7)
	emergency.light_color = Color(0.95, 0.28, 0.18)
	emergency.light_energy = 1.0
	emergency.omni_range = 4.0
	emergency.shadow_enabled = true
	add_child(emergency)

func _create_ceiling_fixture(room_name: String, position: Vector3, light_color: Color, energy: float, range_value: float) -> void:
	var root := Node3D.new()
	root.name = "%sCeilingFixture" % room_name
	root.position = position
	add_child(root)
	var metal := StandardMaterial3D.new()
	metal.albedo_color = Color(0.035, 0.040, 0.038)
	metal.roughness = 0.42
	metal.metallic = 0.35
	var shade_mat := StandardMaterial3D.new()
	shade_mat.albedo_color = Color(0.18, 0.20, 0.18)
	shade_mat.roughness = 0.58
	shade_mat.metallic = 0.10
	var glow_mat := StandardMaterial3D.new()
	glow_mat.albedo_color = light_color
	glow_mat.emission_enabled = true
	glow_mat.emission = light_color
	glow_mat.emission_energy_multiplier = 1.2
	_add_cylinder_mesh(root, "CeilingRose", Vector3(0, 0.36, 0), 0.24, 0.055, metal)
	_add_cylinder_mesh(root, "PendantCable", Vector3(0, 0.18, 0), 0.025, 0.33, metal)
	var shade := MeshInstance3D.new()
	shade.name = "RoundedLampShade"
	var shade_mesh := CylinderMesh.new()
	shade_mesh.top_radius = 0.20
	shade_mesh.bottom_radius = 0.38
	shade_mesh.height = 0.26
	shade_mesh.radial_segments = 28
	shade.mesh = shade_mesh
	shade.position = Vector3.ZERO
	shade.material_override = shade_mat
	root.add_child(shade)
	var bulb := MeshInstance3D.new()
	bulb.name = "GlowingBulb"
	var bulb_mesh := SphereMesh.new()
	bulb_mesh.radius = 0.105
	bulb_mesh.height = 0.21
	bulb_mesh.radial_segments = 20
	bulb_mesh.rings = 10
	bulb.mesh = bulb_mesh
	bulb.position = Vector3(0, -0.11, 0)
	bulb.material_override = glow_mat
	root.add_child(bulb)
	var light := OmniLight3D.new()
	light.name = "%sRoomLight" % room_name
	light.position = position + Vector3(0, -0.06, 0)
	light.light_color = light_color
	light.light_energy = energy
	light.light_indirect_energy = 0.62
	light.omni_range = range_value
	light.omni_attenuation = 1.35
	light.shadow_enabled = true
	add_child(light)

func _create_table_lamp(position: Vector3, light_color: Color, energy: float, range_value: float) -> void:
	var root := Node3D.new()
	root.name = "RescueRoomTableLamp"
	root.position = position
	add_child(root)
	var metal := StandardMaterial3D.new()
	metal.albedo_color = Color(0.035, 0.034, 0.030)
	metal.roughness = 0.36
	metal.metallic = 0.45
	var shade_mat := StandardMaterial3D.new()
	shade_mat.albedo_color = Color(0.82, 0.64, 0.42)
	shade_mat.roughness = 0.78
	shade_mat.metallic = 0.0
	var glow_mat := StandardMaterial3D.new()
	glow_mat.albedo_color = light_color
	glow_mat.emission_enabled = true
	glow_mat.emission = light_color
	glow_mat.emission_energy_multiplier = 1.45
	_add_cylinder_mesh(root, "TableLampBase", Vector3(0, 0.025, 0), 0.17, 0.05, metal)
	_add_cylinder_mesh(root, "TableLampStem", Vector3(0, 0.25, 0), 0.026, 0.42, metal)
	var shade := MeshInstance3D.new()
	shade.name = "TableLampShade"
	var shade_mesh := CylinderMesh.new()
	shade_mesh.top_radius = 0.22
	shade_mesh.bottom_radius = 0.34
	shade_mesh.height = 0.28
	shade_mesh.radial_segments = 32
	shade.mesh = shade_mesh
	shade.position = Vector3(0, 0.49, 0)
	shade.material_override = shade_mat
	root.add_child(shade)
	var bulb := MeshInstance3D.new()
	bulb.name = "TableLampBulb"
	var bulb_mesh := SphereMesh.new()
	bulb_mesh.radius = 0.085
	bulb_mesh.height = 0.17
	bulb_mesh.radial_segments = 20
	bulb_mesh.rings = 10
	bulb.mesh = bulb_mesh
	bulb.position = Vector3(0, 0.38, 0)
	bulb.material_override = glow_mat
	root.add_child(bulb)
	var light := OmniLight3D.new()
	light.name = "RescueRoomTableLampLight"
	light.position = position + Vector3(0, 0.43, 0)
	light.light_color = light_color
	light.light_energy = energy
	light.light_indirect_energy = 0.70
	light.omni_range = range_value
	light.omni_attenuation = 1.42
	light.shadow_enabled = true
	add_child(light)

func _create_mug(position: Vector3, color: Color) -> void:
	var root := Node3D.new()
	root.name = "RoundedCoffeeMug"
	root.position = position
	add_child(root)
	var ceramic := StandardMaterial3D.new()
	ceramic.albedo_color = color
	ceramic.roughness = 0.62
	ceramic.metallic = 0.0
	var body := MeshInstance3D.new()
	body.name = "MugBody"
	var body_mesh := CylinderMesh.new()
	body_mesh.top_radius = 0.052
	body_mesh.bottom_radius = 0.058
	body_mesh.height = 0.13
	body_mesh.radial_segments = 24
	body.mesh = body_mesh
	body.position = Vector3(0, 0.065, 0)
	body.material_override = ceramic
	root.add_child(body)
	var rim := MeshInstance3D.new()
	rim.name = "MugRim"
	var rim_mesh := TorusMesh.new()
	rim_mesh.inner_radius = 0.006
	rim_mesh.outer_radius = 0.055
	rim_mesh.rings = 18
	rim_mesh.ring_segments = 8
	rim.mesh = rim_mesh
	rim.position = Vector3(0, 0.135, 0)
	rim.material_override = ceramic
	root.add_child(rim)
	var handle := MeshInstance3D.new()
	handle.name = "MugHandle"
	var handle_mesh := TorusMesh.new()
	handle_mesh.inner_radius = 0.008
	handle_mesh.outer_radius = 0.043
	handle_mesh.rings = 18
	handle_mesh.ring_segments = 8
	handle.mesh = handle_mesh
	handle.position = Vector3(0.056, 0.075, 0)
	handle.rotation_degrees.z = 90
	handle.scale = Vector3(0.72, 1.0, 1.0)
	handle.material_override = ceramic
	root.add_child(handle)

func _add_cylinder_mesh(parent: Node3D, node_name: String, position: Vector3, radius: float, height: float, material: Material) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	mesh_instance.name = node_name
	mesh_instance.position = position
	var mesh := CylinderMesh.new()
	mesh.top_radius = radius
	mesh.bottom_radius = radius
	mesh.height = height
	mesh.radial_segments = 24
	mesh_instance.mesh = mesh
	mesh_instance.material_override = material
	parent.add_child(mesh_instance)
	return mesh_instance

func _create_window_lighting() -> void:
	_add_window_spot(Vector3(-3.35, 1.85, 1.0), Vector3(-0.7, 0.8, 1.1), 0.82, 5.2)
	_add_window_spot(Vector3(6.5, 1.85, -6.35), Vector3(6.5, 0.8, -4.6), 0.54, 3.4)
	_add_window_spot(Vector3(10.4, 1.9, -10.65), Vector3(13.0, 0.8, -8.2), 0.74, 5.4)
	_add_window_spot(Vector3(15.95, 1.85, -7.4), Vector3(13.2, 0.8, -7.4), 0.70, 5.1)
	_add_window_spot(Vector3(15.45, 1.85, -12.2), Vector3(13.2, 0.8, -12.3), 0.62, 3.6)
	_add_window_spot(Vector3(6.5, 1.85, 0.18), Vector3(6.5, 0.8, 1.75), 0.48, 3.2)

func _add_window_spot(position: Vector3, target: Vector3, energy: float, range_value: float) -> void:
	var light := SpotLight3D.new()
	light.name = "WindowSoftLight"
	light.position = position
	light.light_color = Color(0.56, 0.74, 0.92)
	light.light_energy = energy
	light.light_indirect_energy = 0.78
	light.spot_range = range_value
	light.spot_angle = 58.0
	light.spot_attenuation = 1.30
	light.shadow_enabled = true
	add_child(light)
	light.look_at(target, Vector3.UP)

func _add_warm_lamp(position: Vector3, energy: float, range_value: float) -> void:
	var light := OmniLight3D.new()
	light.name = "FurnitureWarmLamp"
	light.position = position
	light.light_color = Color(1.0, 0.72, 0.44)
	light.light_energy = energy
	light.light_indirect_energy = 0.62
	light.omni_range = range_value
	light.omni_attenuation = 1.45
	light.shadow_enabled = true
	add_child(light)

func _create_reflection_probes() -> void:
	_add_reflection_probe(Vector3(0.0, 1.25, 0.0), Vector3(6.2, 2.8, 5.2))
	_add_reflection_probe(Vector3(6.5, 1.25, -4.1), Vector3(4.8, 2.8, 4.0))
	_add_reflection_probe(Vector3(12.9, 1.25, -1.0), Vector3(6.5, 2.8, 6.3))
	_add_reflection_probe(Vector3(13.0, 1.25, -7.8), Vector3(5.9, 2.8, 5.4))
	_add_reflection_probe(Vector3(13.0, 1.25, -12.0), Vector3(4.4, 2.8, 3.6))

func _add_reflection_probe(position: Vector3, size_value: Vector3) -> void:
	var probe := ReflectionProbe.new()
	probe.name = "RoomReflectionProbe"
	probe.position = position
	probe.size = size_value
	probe.intensity = 0.28
	probe.max_distance = max(size_value.x, size_value.z) * 1.4
	probe.box_projection = true
	probe.enable_shadows = true
	add_child(probe)

func _spawn_actors() -> void:
	player = preload("res://scripts/player/PlayerController.gd").new()
	player.position = Vector3(-1.5, 0.35, 1.4)
	player.rotation_degrees.y = -90
	player.item_picked.connect(func(): tutorial.advance_for("picked"))
	player.item_thrown.connect(func(): tutorial.advance_for("thrown"))
	player.crouch_changed.connect(_on_player_crouch_changed)
	player.interacted.connect(_on_player_interacted)
	add_child(player)
	enemy = preload("res://scripts/world/EnemyAI.gd").new()
	enemy.position = Vector3(15.0, 0.35, 1.25)
	add_child(enemy)
	var patrol_points: Array[Vector3] = [Vector3(15.0, 0.35, 1.25), Vector3(13.4, 0.35, 1.10), Vector3(14.6, 0.35, -0.35)]
	enemy.setup(patrol_points, player)
	enemy.player_detected.connect(_fail_level)
	civilian = preload("res://scripts/world/RescueNPC.gd").new()
	civilian.position = Vector3(11.75, 0.35, -6.85)
	civilian.rotation_degrees.y = -25
	add_child(civilian)
	civilian.setup(player, safe_zone_pos)
	civilian.started_following.connect(func(): tutorial.advance_for("rescue"))
	civilian.rescued.connect(func(): tutorial.advance_for("exit"))

func _on_player_crouch_changed(crouched: bool) -> void:
	if crouched:
		tutorial.advance_for("crouched")

func _on_player_interacted() -> void:
	pass

func _build_hud() -> void:
	var canvas := CanvasLayer.new()
	canvas.name = "HUD"
	add_child(canvas)
	var top_panel := PanelContainer.new()
	top_panel.anchor_left = 0.02
	top_panel.anchor_top = 0.025
	top_panel.anchor_right = 0.37
	top_panel.anchor_bottom = 0.16
	top_panel.add_theme_stylebox_override("panel", _hud_box())
	canvas.add_child(top_panel)
	var stack := VBoxContainer.new()
	top_panel.add_child(stack)
	var objective_row := HBoxContainer.new()
	objective_row.add_theme_constant_override("separation", 10)
	hud_objective = UIFactory.make_label("", 18)
	hud_objective.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	hud_status = UIFactory.make_label("", 15, Color(0.54, 0.76, 0.68))
	objective_row.add_child(hud_objective)
	objective_row.add_child(UIFactory.make_round_icon("?", 24))
	stack.add_child(objective_row)
	stack.add_child(hud_status)
	hud_awareness_label = UIFactory.make_label(LocalizationManager.text("status.awareness"), 12, Color(0.70, 0.84, 0.78))
	stack.add_child(hud_awareness_label)
	hud_awareness = ProgressBar.new()
	hud_awareness.custom_minimum_size = Vector2(220, 8)
	hud_awareness.max_value = 100.0
	hud_awareness.value = 0.0
	hud_awareness.show_percentage = false
	stack.add_child(hud_awareness)
	hud_hint = UIFactory.make_label("", 20, Color(0.90, 0.95, 0.84))
	hud_hint.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	hud_hint.anchor_left = 0.24
	hud_hint.anchor_right = 0.76
	hud_hint.anchor_top = 0.88
	hud_hint.anchor_bottom = 0.97
	canvas.add_child(hud_hint)
	_build_interaction_overlay(canvas)
	_build_minimap(canvas)

func _build_interaction_overlay(canvas: CanvasLayer) -> void:
	hud_crosshair = UIFactory.make_label("+", 24, Color(0.84, 0.96, 0.88, 0.78))
	hud_crosshair.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	hud_crosshair.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	hud_crosshair.anchor_left = 0.49
	hud_crosshair.anchor_right = 0.51
	hud_crosshair.anchor_top = 0.48
	hud_crosshair.anchor_bottom = 0.52
	canvas.add_child(hud_crosshair)
	hud_interaction_panel = PanelContainer.new()
	hud_interaction_panel.anchor_left = 0.36
	hud_interaction_panel.anchor_right = 0.64
	hud_interaction_panel.anchor_top = 0.63
	hud_interaction_panel.anchor_bottom = 0.70
	hud_interaction_panel.add_theme_stylebox_override("panel", _interaction_box())
	hud_interaction_panel.visible = false
	canvas.add_child(hud_interaction_panel)
	hud_interaction = UIFactory.make_label("", 18, Color(0.96, 0.88, 0.60))
	hud_interaction.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	hud_interaction.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	hud_interaction_panel.add_child(hud_interaction)

func _build_minimap(canvas: CanvasLayer) -> void:
	var panel := PanelContainer.new()
	panel.anchor_left = 0.78
	panel.anchor_top = 0.035
	panel.anchor_right = 0.98
	panel.anchor_bottom = 0.31
	panel.add_theme_stylebox_override("panel", _hud_box())
	canvas.add_child(panel)
	var map_root := Control.new()
	panel.add_child(map_root)
	var title := UIFactory.make_label(LocalizationManager.text("minimap.title"), 13, Color(0.72, 0.93, 0.86))
	title.position = Vector2(10, 4)
	map_root.add_child(title)
	_minimap_rect(map_root, Vector2(18, 55), Vector2(60, 50), Color(0.13, 0.21, 0.20, 0.86))
	_minimap_rect(map_root, Vector2(78, 68), Vector2(64, 24), Color(0.12, 0.18, 0.18, 0.86))
	_minimap_rect(map_root, Vector2(90, 96), Vector2(42, 30), Color(0.10, 0.16, 0.15, 0.78))
	_minimap_rect(map_root, Vector2(88, 28), Vector2(50, 40), Color(0.13, 0.17, 0.15, 0.78))
	_minimap_rect(map_root, Vector2(142, 45), Vector2(62, 62), Color(0.16, 0.12, 0.12, 0.86))
	_minimap_rect(map_root, Vector2(204, 51), Vector2(32, 48), Color(0.12, 0.14, 0.14, 0.72))
	_minimap_rect(map_root, Vector2(146, 112), Vector2(56, 48), Color(0.10, 0.20, 0.17, 0.86))
	_minimap_rect(map_root, Vector2(150, 160), Vector2(46, 38), Color(0.08, 0.16, 0.13, 0.86))
	minimap_player = _minimap_marker(map_root, Color(0.85, 0.95, 0.88), Vector2(18, 55))
	minimap_civilian = _minimap_marker(map_root, Color(0.24, 0.90, 0.70), Vector2(146, 112))
	minimap_enemy = _minimap_marker(map_root, Color(0.90, 0.28, 0.20), Vector2(142, 45))

func _minimap_rect(parent: Control, pos: Vector2, size_value: Vector2, color: Color) -> void:
	var rect := ColorRect.new()
	rect.position = pos
	rect.size = size_value
	rect.color = color
	parent.add_child(rect)

func _minimap_marker(parent: Control, color: Color, pos: Vector2) -> ColorRect:
	var marker := ColorRect.new()
	marker.size = Vector2(8, 8)
	marker.position = pos
	marker.color = color
	parent.add_child(marker)
	return marker

func _world_to_minimap(pos: Vector3) -> Vector2:
	var x := remap(pos.x, -3.0, 19.3, 18.0, 236.0)
	var y := remap(pos.z, 2.5, -13.8, 55.0, 198.0)
	return Vector2(clamp(x, 18.0, 236.0), clamp(y, 45.0, 198.0))

func _hud_box() -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = Color(0.02, 0.035, 0.038, 0.74)
	box.border_color = Color(0.22, 0.39, 0.36, 0.65)
	box.set_border_width_all(1)
	box.corner_radius_top_left = 4
	box.corner_radius_top_right = 4
	box.corner_radius_bottom_left = 4
	box.corner_radius_bottom_right = 4
	box.content_margin_left = 14
	box.content_margin_right = 14
	box.content_margin_top = 8
	box.content_margin_bottom = 8
	return box

func _interaction_box() -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = Color(0.03, 0.035, 0.030, 0.82)
	box.border_color = Color(0.90, 0.72, 0.32, 0.85)
	box.set_border_width_all(1)
	box.corner_radius_top_left = 4
	box.corner_radius_top_right = 4
	box.corner_radius_bottom_left = 4
	box.corner_radius_bottom_right = 4
	box.content_margin_left = 16
	box.content_margin_right = 16
	box.content_margin_top = 8
	box.content_margin_bottom = 8
	return box

func _build_pause() -> void:
	pause_layer = CanvasLayer.new()
	pause_layer.name = "PauseLayer"
	pause_layer.process_mode = Node.PROCESS_MODE_ALWAYS
	add_child(pause_layer)
	pause_menu = preload("res://scripts/ui/PauseMenu.gd").new()
	pause_menu.process_mode = Node.PROCESS_MODE_ALWAYS
	pause_menu.resume_requested.connect(func(): _set_paused(false))
	pause_menu.restart_requested.connect(func(): get_tree().paused = false; restart_requested.emit())
	pause_menu.main_menu_requested.connect(func(): get_tree().paused = false; main_menu_requested.emit())
	pause_menu.exit_requested.connect(func(): get_tree().quit())
	pause_layer.add_child(pause_menu)

func _show_intro_legend() -> void:
	get_tree().paused = true
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
	if player and player.has_method("set_gameplay_input_enabled"):
		player.set_gameplay_input_enabled(false)
	var layer := CanvasLayer.new()
	layer.name = "IntroLayer"
	layer.process_mode = Node.PROCESS_MODE_ALWAYS
	add_child(layer)
	intro_overlay = Control.new()
	intro_overlay.set_anchors_preset(Control.PRESET_FULL_RECT)
	intro_overlay.process_mode = Node.PROCESS_MODE_ALWAYS
	layer.add_child(intro_overlay)
	var dim := ColorRect.new()
	dim.color = Color(0.0, 0.0, 0.0, 0.72)
	dim.set_anchors_preset(Control.PRESET_FULL_RECT)
	intro_overlay.add_child(dim)
	var panel := UIFactory.make_panel()
	panel.custom_minimum_size = Vector2(620, 360)
	panel.position = (get_viewport().get_visible_rect().size - panel.custom_minimum_size) * 0.5
	intro_overlay.add_child(panel)
	var stack := VBoxContainer.new()
	stack.add_theme_constant_override("separation", 18)
	panel.add_child(stack)
	stack.add_child(UIFactory.make_label(LocalizationManager.text("intro.title"), 34, Color(0.92, 1.0, 0.95)))
	var body := UIFactory.make_label(LocalizationManager.text("intro.body"), 18, Color(0.78, 0.90, 0.86))
	body.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	stack.add_child(body)
	var start := UIFactory.make_button(LocalizationManager.text("intro.start"))
	start.pressed.connect(_start_tutorial_from_intro)
	stack.add_child(start)

func _start_tutorial_from_intro() -> void:
	if intro_overlay:
		intro_overlay.get_parent().queue_free()
		intro_overlay = null
	get_tree().paused = false
	if player and player.has_method("set_gameplay_input_enabled"):
		player.set_gameplay_input_enabled(true)
	Input.set_mouse_mode(Input.MOUSE_MODE_CAPTURED)

func _set_paused(value: bool) -> void:
	if game_over:
		return
	get_tree().paused = value
	pause_menu.visible = value
	if player and player.has_method("set_gameplay_input_enabled"):
		player.set_gameplay_input_enabled(not value)
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE if value else Input.MOUSE_MODE_CAPTURED)

func _set_hint(key: String) -> void:
	hud_hint.text = LocalizationManager.text(key) if key != "" else ""

func _set_objective(key: String) -> void:
	hud_objective.text = "%s: %s" % [LocalizationManager.text("hud.objective"), LocalizationManager.text(key)]

func _refresh_hud() -> void:
	_set_hint(tutorial.current_hint() if tutorial else "")
	_update_status_text()

func _process(_delta: float) -> void:
	_update_status_text()
	_update_interaction_text()
	_update_awareness_meter()
	if player and player.velocity.length() > 0.3:
		tutorial.advance_for("moved")
	if minimap_player and player:
		minimap_player.position = _world_to_minimap(player.global_position)
	if minimap_civilian and civilian:
		minimap_civilian.position = _world_to_minimap(civilian.global_position)
	if minimap_enemy and enemy:
		minimap_enemy.position = _world_to_minimap(enemy.global_position)

func _update_status_text() -> void:
	if not hud_status or not player:
		return
	var key := "status.crouched" if player.get("is_crouched") else "status.hidden"
	if enemy and enemy.has_method("awareness_text_key"):
		var enemy_key: String = str(enemy.awareness_text_key())
		if enemy_key != "status.hidden":
			key = enemy_key
	hud_status.text = LocalizationManager.text(key)

func _update_interaction_text() -> void:
	if not hud_interaction or not hud_crosshair or not player or not player.has_method("get_current_interaction_text"):
		return
	if player.has_method("_update_interaction_focus"):
		player._update_interaction_focus()
	var text: String = str(player.get_current_interaction_text())
	hud_interaction.text = text
	if hud_interaction_panel:
		hud_interaction_panel.visible = text != ""
	hud_crosshair.text = "◇" if text != "" else "+"
	hud_crosshair.add_theme_color_override("font_color", Color(1.0, 0.82, 0.35, 0.95) if text != "" else Color(0.84, 0.96, 0.88, 0.78))

func _update_awareness_meter() -> void:
	if not hud_awareness or not enemy or not enemy.has_method("get_awareness_ratio"):
		return
	hud_awareness.value = enemy.get_awareness_ratio() * 100.0

func _on_exit_body_entered(body: Node) -> void:
	if body == player and civilian and civilian.has_method("is_rescued") and civilian.is_rescued():
		_win_level()

func _win_level() -> void:
	if game_over:
		return
	game_over = true
	_show_result(LocalizationManager.text("level.win"))

func _fail_level() -> void:
	if game_over:
		return
	game_over = true
	_show_result(LocalizationManager.text("level.fail"))

func _show_result(message: String) -> void:
	get_tree().paused = true
	if player and player.has_method("set_gameplay_input_enabled"):
		player.set_gameplay_input_enabled(false)
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
	result_overlay = Control.new()
	result_overlay.set_anchors_preset(Control.PRESET_FULL_RECT)
	result_overlay.process_mode = Node.PROCESS_MODE_ALWAYS
	var result_layer := CanvasLayer.new()
	result_layer.name = "ResultLayer"
	result_layer.process_mode = Node.PROCESS_MODE_ALWAYS
	add_child(result_layer)
	result_layer.add_child(result_overlay)
	var dim := ColorRect.new()
	dim.color = Color(0, 0, 0, 0.64)
	dim.set_anchors_preset(Control.PRESET_FULL_RECT)
	result_overlay.add_child(dim)
	var panel := UIFactory.make_panel()
	panel.anchor_left = 0.34
	panel.anchor_right = 0.66
	panel.anchor_top = 0.32
	panel.anchor_bottom = 0.68
	result_overlay.add_child(panel)
	var stack := VBoxContainer.new()
	stack.add_theme_constant_override("separation", 14)
	panel.add_child(stack)
	stack.add_child(UIFactory.make_label(message, 26))
	var restart := UIFactory.make_button(LocalizationManager.text("level.restart"))
	restart.pressed.connect(func():
		get_tree().paused = false
		Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
		restart_requested.emit()
	)
	stack.add_child(restart)
	var menu := UIFactory.make_button(LocalizationManager.text("level.menu"))
	menu.pressed.connect(func():
		get_tree().paused = false
		Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
		main_menu_requested.emit()
	)
	stack.add_child(menu)
