extends Control

signal resume_requested
signal restart_requested
signal main_menu_requested
signal exit_requested

const UIFactory := preload("res://scripts/ui/UIFactory.gd")

var settings_overlay: Control
var panel: PanelContainer

func _ready() -> void:
	var viewport_size := get_viewport().get_visible_rect().size
	size = viewport_size
	visible = false
	var dim := ColorRect.new()
	dim.color = Color(0, 0, 0, 0.62)
	dim.size = viewport_size
	add_child(dim)
	panel = UIFactory.make_panel()
	panel.size = Vector2(380, 420)
	panel.position = (viewport_size - panel.size) * 0.5
	add_child(panel)
	var stack := VBoxContainer.new()
	stack.add_theme_constant_override("separation", 13)
	panel.add_child(stack)
	var title_row := HBoxContainer.new()
	title_row.add_theme_constant_override("separation", 12)
	title_row.alignment = BoxContainer.ALIGNMENT_CENTER
	title_row.add_child(UIFactory.make_round_icon("II", 30, Color(0.70, 0.88, 0.82, 0.95)))
	title_row.add_child(UIFactory.make_label(LocalizationManager.text("pause.title"), 34))
	stack.add_child(title_row)
	_add_button(stack, "pause.resume", func(): resume_requested.emit())
	_add_button(stack, "pause.settings", _open_settings)
	_add_button(stack, "pause.restart", func(): restart_requested.emit())
	_add_button(stack, "pause.main_menu", func(): main_menu_requested.emit())
	_add_button(stack, "pause.exit", func(): exit_requested.emit())

func _add_button(stack: VBoxContainer, key: String, callback: Callable) -> void:
	var button := UIFactory.make_button(LocalizationManager.text(key))
	button.pressed.connect(callback)
	stack.add_child(button)

func _open_settings() -> void:
	if settings_overlay:
		settings_overlay.queue_free()
	settings_overlay = preload("res://scripts/ui/SettingsMenu.gd").new()
	settings_overlay.close_requested.connect(func(): settings_overlay.queue_free(); settings_overlay = null)
	add_child(settings_overlay)
