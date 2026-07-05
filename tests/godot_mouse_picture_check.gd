extends SceneTree

const RESULT_PATH := "res://artifacts/mouse_picture_result.txt"
const SCREENSHOT_DIR := "res://artifacts/screenshots"

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path(SCREENSHOT_DIR))
	_write_result("started")
	var packed := load("res://scenes/Main.tscn")
	var main: Node = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	main._start_game()
	await process_frame
	await process_frame
	main.current_scene._start_tutorial_from_intro()
	await process_frame
	await process_frame
	var player: Node = get_nodes_in_group("player")[0]
	var yaw_before: float = player.rotation.y
	var pitch_before: float = player.camera.rotation.x
	var before := await _capture_image("mouse_before.png")

	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
	var click := InputEventMouseButton.new()
	click.button_index = MOUSE_BUTTON_LEFT
	click.pressed = true
	click.position = root.get_visible_rect().size * 0.5
	Input.parse_input_event(click)
	await process_frame

	var motion := InputEventMouseMotion.new()
	motion.position = root.get_visible_rect().size * 0.5 + Vector2(260, -70)
	motion.relative = Vector2(260, -70)
	Input.parse_input_event(motion)
	await process_frame
	await process_frame

	var after := await _capture_image("mouse_after.png")
	var yaw_delta: float = abs(player.rotation.y - yaw_before)
	var pitch_delta: float = abs(player.camera.rotation.x - pitch_before)
	var image_delta := _image_delta(before, after)
	if yaw_delta <= 0.01 or pitch_delta <= 0.01 or image_delta <= 0.015:
		var message := "failed yaw=%.4f pitch=%.4f image_delta=%.4f" % [yaw_delta, pitch_delta, image_delta]
		_write_result(message)
		push_error("Mouse picture check failed: " + message)
		quit(1)
		return
	var message := "passed yaw=%.4f pitch=%.4f image_delta=%.4f" % [yaw_delta, pitch_delta, image_delta]
	_write_result(message)
	print("Mouse picture check " + message)
	quit(0)

func _capture_image(file_name: String) -> Image:
	await process_frame
	var image := root.get_texture().get_image()
	image.save_png(SCREENSHOT_DIR + "/" + file_name)
	return image

func _image_delta(a: Image, b: Image) -> float:
	var width: int = min(a.get_width(), b.get_width())
	var height: int = min(a.get_height(), b.get_height())
	var step := 16
	var total := 0.0
	var count := 0
	for y in range(0, height, step):
		for x in range(0, width, step):
			var ca := a.get_pixel(x, y)
			var cb := b.get_pixel(x, y)
			total += abs(ca.r - cb.r) + abs(ca.g - cb.g) + abs(ca.b - cb.b)
			count += 3
	return total / max(1, count)

func _write_result(text: String) -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path("res://artifacts"))
	var file := FileAccess.open(ProjectSettings.globalize_path(RESULT_PATH), FileAccess.WRITE)
	if file:
		file.store_string(text)
