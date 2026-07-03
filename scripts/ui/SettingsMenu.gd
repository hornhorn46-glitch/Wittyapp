extends Control

signal close_requested

const UIFactory := preload("res://scripts/ui/UIFactory.gd")
const RESOLUTIONS := [Vector2i(1280, 720), Vector2i(1600, 900), Vector2i(1920, 1080)]

var stack: VBoxContainer

func _ready() -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var dim := ColorRect.new()
	dim.color = Color(0, 0, 0, 0.58)
	dim.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(dim)
	var panel := UIFactory.make_panel()
	panel.anchor_left = 0.31
	panel.anchor_right = 0.69
	panel.anchor_top = 0.10
	panel.anchor_bottom = 0.90
	add_child(panel)
	stack = VBoxContainer.new()
	stack.add_theme_constant_override("separation", 12)
	panel.add_child(stack)
	_build_controls()

func _build_controls() -> void:
	stack.add_child(UIFactory.make_label(LocalizationManager.text("settings.title"), 34))
	_add_slider("settings.master", SettingsManager.master_volume, func(value): SettingsManager.master_volume = value; SettingsManager.apply_settings())
	_add_slider("settings.music", SettingsManager.music_volume, func(value): SettingsManager.music_volume = value; SettingsManager.apply_settings())
	_add_slider("settings.sfx", SettingsManager.sfx_volume, func(value): SettingsManager.sfx_volume = value; SettingsManager.apply_settings())
	var fullscreen := CheckBox.new()
	fullscreen.text = LocalizationManager.text("settings.fullscreen")
	fullscreen.button_pressed = SettingsManager.fullscreen
	fullscreen.toggled.connect(func(value): SettingsManager.fullscreen = value; SettingsManager.apply_settings())
	stack.add_child(fullscreen)
	_add_resolution_selector()
	_add_language_selector()
	var back := UIFactory.make_button(LocalizationManager.text("settings.back"))
	back.pressed.connect(func(): close_requested.emit())
	stack.add_child(back)

func _add_slider(key: String, value: float, callback: Callable) -> void:
	var label := UIFactory.make_label(LocalizationManager.text(key), 18)
	stack.add_child(label)
	var slider := HSlider.new()
	slider.min_value = 0.0
	slider.max_value = 1.0
	slider.step = 0.01
	slider.value = value
	slider.value_changed.connect(callback)
	stack.add_child(slider)

func _add_resolution_selector() -> void:
	stack.add_child(UIFactory.make_label(LocalizationManager.text("settings.resolution"), 18))
	var option := OptionButton.new()
	for i in range(RESOLUTIONS.size()):
		var res: Vector2i = RESOLUTIONS[i]
		option.add_item("%dx%d" % [res.x, res.y], i)
		if res == SettingsManager.resolution:
			option.select(i)
	option.item_selected.connect(func(index): SettingsManager.resolution = RESOLUTIONS[index]; SettingsManager.apply_settings())
	stack.add_child(option)

func _add_language_selector() -> void:
	stack.add_child(UIFactory.make_label(LocalizationManager.text("settings.language"), 18))
	var option := OptionButton.new()
	option.add_item("English", 0)
	option.add_item("Русский", 1)
	option.select(1 if SettingsManager.language == "ru" else 0)
	option.item_selected.connect(func(index): SettingsManager.set_language("ru" if index == 1 else "en"); _rebuild())
	stack.add_child(option)

func _rebuild() -> void:
	for child in stack.get_children():
		child.queue_free()
	_build_controls()

