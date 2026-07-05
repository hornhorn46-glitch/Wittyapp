extends SceneTree

const RESULT_PATH := "res://artifacts/exit_blockers.txt"

func _initialize() -> void:
	call_deferred("_run")

func _run() -> void:
	DirAccess.make_dir_recursive_absolute(ProjectSettings.globalize_path("res://artifacts"))
	var packed := load("res://scenes/Main.tscn")
	var main: Node = packed.instantiate()
	root.add_child(main)
	await process_frame
	await process_frame
	main._start_game()
	await process_frame
	await process_frame
	var world: Node = main.current_scene
	var lines: Array[String] = []
	lines.append("Exit doorway blockers near x=13 z=-10")
	for shape in world.find_children("*", "CollisionShape3D", true, false):
		var collision := shape as CollisionShape3D
		if not collision or not collision.shape:
			continue
		var pos := collision.global_position
		if absf(pos.x - 13.0) > 2.4 or absf(pos.z + 10.2) > 1.2:
			continue
		lines.append("%s parent=%s pos=%s shape=%s info=%s disabled=%s" % [
			str(collision.get_path()),
			str(collision.get_parent().name if collision.get_parent() else ""),
			str(pos),
			collision.shape.get_class(),
			_shape_info(collision.shape),
			str(collision.disabled)
		])
	var file := FileAccess.open(ProjectSettings.globalize_path(RESULT_PATH), FileAccess.WRITE)
	if file:
		file.store_string("\n".join(lines))
	quit(0)

func _shape_info(shape: Shape3D) -> String:
	if shape is BoxShape3D:
		return "size=%s" % str((shape as BoxShape3D).size)
	if shape is CapsuleShape3D:
		return "radius=%.3f height=%.3f" % [(shape as CapsuleShape3D).radius, (shape as CapsuleShape3D).height]
	if shape is SphereShape3D:
		return "radius=%.3f" % (shape as SphereShape3D).radius
	return ""
