extends Control

signal new_game_requested
signal exit_requested

const UIFactory := preload("res://scripts/ui/UIFactory.gd")

var menu_stack: VBoxContainer
var overlay: Control

func _ready() -> void:
	AudioManager.start_menu_music()
	LocalizationManager.language_changed.connect(_refresh_text)
	_build()

func _build() -> void:
	anchors_preset = Control.PRESET_FULL_RECT
	var bg := ColorRect.new()
	bg.color = Color(0.012, 0.015, 0.018, 1.0)
	bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(bg)
	var texture_bg := TextureRect.new()
	texture_bg.texture = load("res://assets/curated/textures/concrete_damaged.png")
	texture_bg.modulate = Color(0.12, 0.18, 0.18, 0.38)
	texture_bg.expand_mode = TextureRect.EXPAND_IGNORE_SIZE
	texture_bg.stretch_mode = TextureRect.STRETCH_TILE
	texture_bg.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(texture_bg)
	_add_corridor_composition()
	var shade := ColorRect.new()
	shade.color = Color(0, 0, 0, 0.35)
	shade.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(shade)
	menu_stack = VBoxContainer.new()
	menu_stack.anchor_left = 0.08
	menu_stack.anchor_top = 0.20
	menu_stack.anchor_right = 0.43
	menu_stack.anchor_bottom = 0.90
	menu_stack.add_theme_constant_override("separation", 14)
	add_child(menu_stack)
	_refresh_text()

func _add_corridor_composition() -> void:
	for i in range(7):
		var strip := ColorRect.new()
		strip.color = Color(0.04 + i * 0.01, 0.055 + i * 0.006, 0.057 + i * 0.004, 0.8)
		strip.anchor_left = 0.50 + i * 0.065
		strip.anchor_top = 0.08 + i * 0.035
		strip.anchor_right = 0.54 + i * 0.066
		strip.anchor_bottom = 0.94 - i * 0.035
		add_child(strip)
	var light := ColorRect.new()
	light.color = Color(0.30, 0.48, 0.42, 0.18)
	light.anchor_left = 0.62
	light.anchor_right = 0.84
	light.anchor_top = 0.0
	light.anchor_bottom = 1.0
	add_child(light)
	var door := ColorRect.new()
	door.color = Color(0.015, 0.025, 0.027, 0.95)
	door.anchor_left = 0.78
	door.anchor_right = 0.94
	door.anchor_top = 0.20
	door.anchor_bottom = 0.90
	add_child(door)

func _refresh_text() -> void:
	if not menu_stack:
		return
	for child in menu_stack.get_children():
		child.queue_free()
	var title := UIFactory.make_label(LocalizationManager.text("game.title"), 52, Color(0.90, 0.98, 0.93))
	var subtitle := UIFactory.make_label(LocalizationManager.text("menu.subtitle"), 18, Color(0.58, 0.72, 0.68))
	menu_stack.add_child(title)
	menu_stack.add_child(subtitle)
	menu_stack.add_child(_spacer(20))
	_add_button("menu.new_game", func(): new_game_requested.emit())
	_add_button("menu.settings", _show_settings)
	_add_button("menu.credits", _show_credits)
	_add_button("menu.exit", func(): exit_requested.emit())

func _add_button(key: String, callback: Callable) -> void:
	var button := UIFactory.make_button(LocalizationManager.text(key))
	button.pressed.connect(callback)
	menu_stack.add_child(button)

func _show_settings() -> void:
	_clear_overlay()
	overlay = preload("res://scripts/ui/SettingsMenu.gd").new()
	overlay.close_requested.connect(_clear_overlay)
	add_child(overlay)

func _show_credits() -> void:
	_clear_overlay()
	overlay = preload("res://scripts/ui/CreditsScreen.gd").new()
	overlay.close_requested.connect(_clear_overlay)
	add_child(overlay)

func _clear_overlay() -> void:
	if overlay:
		overlay.queue_free()
		overlay = null

func _spacer(height: int) -> Control:
	var control := Control.new()
	control.custom_minimum_size = Vector2(1, height)
	return control
