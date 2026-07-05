extends StaticBody3D

@export var opened := false
@export var locked := false
@export var required_item := ""
@export var locked_hint_key := "interact.locked_keycard"
@export var focus_offset := Vector3(0, 1.0, 0)

var closed_rotation := 0.0
var open_rotation := -1.35
var collision_shape: CollisionShape3D

func _ready() -> void:
	add_to_group("interactable")
	closed_rotation = rotation.y
	for child in get_children():
		if child is CollisionShape3D:
			collision_shape = child
			break
	_sync_collision()

func interaction_text(player: Node) -> String:
	if locked and required_item != "" and player and player.has_method("has_item") and not player.has_item(required_item):
		return LocalizationManager.text(locked_hint_key)
	return LocalizationManager.text("interact.close_door" if opened else "interact.open_door")

func interaction_focus_point(_player: Node) -> Vector3:
	return to_global(focus_offset)

func interact(player: Node) -> void:
	if locked and required_item != "" and player and player.has_method("has_item") and not player.has_item(required_item):
		AudioManager.play_ui_click()
		SoundEventSystem.emit_sound(global_position, 1.5, self)
		return
	opened = not opened
	rotation.y = open_rotation if opened else closed_rotation
	_sync_collision()
	AudioManager.play_sfx("door_open" if opened else "door_close")
	SoundEventSystem.emit_sound(global_position, 2.4, self)

func _sync_collision() -> void:
	if collision_shape:
		collision_shape.set_deferred("disabled", opened)
