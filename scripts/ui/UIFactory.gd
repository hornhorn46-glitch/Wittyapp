extends RefCounted

const FONT_PATH := "res://assets/curated/fonts/KenneyFuture.ttf"
const BUTTON_NORMAL := "res://assets/curated/ui/button_normal.png"
const BUTTON_HOVER := "res://assets/curated/ui/button_hover.png"
const BUTTON_PRESSED := "res://assets/curated/ui/button_pressed.png"
const PANEL_TEXTURE := "res://assets/curated/ui/panel_blue.png"

static func make_label(text: String, size: int = 22, color: Color = Color(0.86, 0.93, 0.9)) -> Label:
	var label := Label.new()
	label.text = text
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_color_override("font_color", color)
	var font := load(FONT_PATH)
	if font and _can_use_display_font(text):
		label.add_theme_font_override("font", font)
	return label

static func make_button(text: String) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(360, 56)
	button.focus_mode = Control.FOCUS_ALL
	button.add_theme_font_size_override("font_size", 20)
	var font := load(FONT_PATH)
	if font and _can_use_display_font(text):
		button.add_theme_font_override("font", font)
	button.add_theme_stylebox_override("normal", _texture_box(BUTTON_NORMAL, Color(0.30, 0.55, 0.58, 0.96)))
	button.add_theme_stylebox_override("hover", _texture_box(BUTTON_HOVER, Color(0.45, 0.82, 0.72, 1.0)))
	button.add_theme_stylebox_override("pressed", _texture_box(BUTTON_PRESSED, Color(0.95, 0.82, 0.40, 1.0)))
	button.add_theme_stylebox_override("focus", _texture_box(BUTTON_HOVER, Color(0.70, 1.0, 0.88, 1.0)))
	button.add_theme_color_override("font_color", Color(0.86, 0.94, 0.9))
	button.add_theme_color_override("font_hover_color", Color(0.96, 1.0, 0.96))
	button.mouse_entered.connect(AudioManager.play_ui_hover)
	button.pressed.connect(AudioManager.play_ui_click)
	return button

static func make_panel() -> PanelContainer:
	var panel := PanelContainer.new()
	panel.add_theme_stylebox_override("panel", _texture_box(PANEL_TEXTURE, Color(0.08, 0.18, 0.20, 0.92), 16))
	return panel

static func make_round_icon(text: String, size: int = 26, fill: Color = Color(0.82, 0.63, 0.24, 0.96)) -> Label:
	var icon := make_label(text, 16, Color(0.08, 0.075, 0.045))
	icon.custom_minimum_size = Vector2(size, size)
	icon.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	icon.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	var box := StyleBoxFlat.new()
	box.bg_color = fill
	box.border_color = Color(1.0, 0.88, 0.45, 0.80)
	box.set_border_width_all(1)
	box.corner_radius_top_left = size / 2
	box.corner_radius_top_right = size / 2
	box.corner_radius_bottom_left = size / 2
	box.corner_radius_bottom_right = size / 2
	icon.add_theme_stylebox_override("normal", box)
	return icon

static func _can_use_display_font(text: String) -> bool:
	for index in range(text.length()):
		if text.unicode_at(index) > 127:
			return false
	return true

static func _texture_box(path: String, modulate: Color, margin: int = 20) -> StyleBox:
	var texture := load(path)
	if not texture:
		return _box(Color(0.04, 0.07, 0.075, 0.90), Color(0.32, 0.58, 0.54, 0.85))
	var box := StyleBoxTexture.new()
	box.texture = texture
	box.draw_center = true
	box.modulate_color = modulate
	box.set_content_margin(SIDE_LEFT, margin)
	box.set_content_margin(SIDE_RIGHT, margin)
	box.set_content_margin(SIDE_TOP, 10)
	box.set_content_margin(SIDE_BOTTOM, 10)
	return box

static func _box(fill: Color, border: Color) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = fill
	box.border_color = border
	box.set_border_width_all(1)
	box.corner_radius_top_left = 4
	box.corner_radius_top_right = 4
	box.corner_radius_bottom_left = 4
	box.corner_radius_bottom_right = 4
	box.content_margin_left = 18
	box.content_margin_right = 18
	box.content_margin_top = 8
	box.content_margin_bottom = 8
	return box
