extends Control

signal close_requested

const UIFactory := preload("res://scripts/ui/UIFactory.gd")

func _ready() -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var dim := ColorRect.new()
	dim.color = Color(0, 0, 0, 0.62)
	dim.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(dim)
	var panel := UIFactory.make_panel()
	panel.anchor_left = 0.26
	panel.anchor_right = 0.74
	panel.anchor_top = 0.18
	panel.anchor_bottom = 0.82
	add_child(panel)
	var stack := VBoxContainer.new()
	stack.add_theme_constant_override("separation", 18)
	panel.add_child(stack)
	stack.add_child(UIFactory.make_label(LocalizationManager.text("credits.title"), 34))
	var body := UIFactory.make_label(LocalizationManager.text("credits.body"), 18, Color(0.78, 0.88, 0.84))
	body.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	stack.add_child(body)
	var asset_note := UIFactory.make_label("See ASSET_CREDITS.md for generated audio and textures.", 16, Color(0.55, 0.70, 0.66))
	asset_note.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	stack.add_child(asset_note)
	var back := UIFactory.make_button(LocalizationManager.text("credits.back"))
	back.pressed.connect(func(): close_requested.emit())
	stack.add_child(back)

