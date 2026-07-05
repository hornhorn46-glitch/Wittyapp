extends SceneTree

const RESULT_PATH := "res://artifacts/room_showcase_result.txt"
const SCREENSHOT_DIR := "res://artifacts/screenshots"

var main: Node
var world: Node
var showcase_camera: Camera3D

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path(SCREENSHOT_DIR))
	_write_result("started")
	var packed := load("res://scenes/Main.tscn")
	main = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	main._start_game()
	await process_frame
	await process_frame
	world = main.current_scene
	world._start_tutorial_from_intro()
	await process_frame
	await process_frame
	_setup_camera()
	await _capture_from(Vector3(-1.70, 1.28, 1.58), Vector3(1.45, 1.08, 0.85), "showcase_01_start_room.png")
	await _capture_from(Vector3(0.85, 1.22, -0.98), Vector3(3.02, 1.08, -0.92), "showcase_02_door_hinge_handle.png")
	await _capture_from(Vector3(6.40, 1.24, -2.55), Vector3(6.70, 1.00, -5.65), "showcase_03_records_room.png")
	await _capture_from(Vector3(12.05, 1.24, -0.35), Vector3(14.92, 0.98, 1.10), "showcase_04_patrol_guard_scale.png")
	await _capture_from(Vector3(13.35, 1.25, -5.92), Vector3(14.90, 1.00, -8.45), "showcase_05_rescue_room_entry.png")
	await _capture_from(Vector3(13.10, 1.24, -8.85), Vector3(11.78, 0.98, -6.86), "showcase_06_rescue_npc_corner.png")
	await _capture_from(Vector3(12.15, 1.28, -10.72), Vector3(13.45, 1.02, -12.95), "showcase_07_exit_stairwell.png")
	_write_result("passed")
	print("Room showcase capture passed.")
	quit(0)

func _setup_camera() -> void:
	var players := get_nodes_in_group("player")
	if players.size() > 0:
		var player_camera := players[0].get("camera") as Camera3D
		if player_camera:
			player_camera.current = false
	showcase_camera = Camera3D.new()
	showcase_camera.name = "ShowcaseCamera"
	showcase_camera.fov = 68.0
	showcase_camera.current = true
	world.add_child(showcase_camera)

func _capture_from(position: Vector3, target: Vector3, file_name: String) -> void:
	showcase_camera.global_position = position
	showcase_camera.look_at(target, Vector3.UP)
	await process_frame
	await process_frame
	var image := root.get_texture().get_image()
	image.save_png(SCREENSHOT_DIR + "/" + file_name)

func _write_result(text: String) -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path("res://artifacts"))
	var file := FileAccess.open(ProjectSettings.globalize_path(RESULT_PATH), FileAccess.WRITE)
	if file:
		file.store_string(text)
