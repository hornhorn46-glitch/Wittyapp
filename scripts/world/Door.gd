extends StaticBody3D

@export var opened := false
@export var locked := false
@export var required_item := ""
@export var locked_hint_key := "interact.locked_keycard"

var closed_rotation := 0.0
var open_rotation := -1.35

func _ready() -> void:
	add_to_group("interactable")
	closed_rotation = rotation.y

func interaction_text(player: Node) -> String:
	if locked and required_item != "" and player and player.has_method("has_item") and not player.has_item(required_item):
		return LocalizationManager.text(locked_hint_key)
	return LocalizationManager.text("interact.close_door" if opened else "interact.open_door")

func interact(player: Node) -> void:
	if locked and required_item != "" and player and player.has_method("has_item") and not player.has_item(required_item):
		AudioManager.play_ui_click()
		SoundEventSystem.emit_sound(global_position, 1.5, self)
		return
	opened = not opened
	rotation.y = open_rotation if opened else closed_rotation
	AudioManager.play_sfx("door")
	SoundEventSystem.emit_sound(global_position, 5.0, self)
