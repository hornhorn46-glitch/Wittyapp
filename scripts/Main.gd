extends Node

var current_scene: Node

func _ready() -> void:
	_ensure_input_actions()
	SettingsManager.load_settings()
	SettingsManager.apply_settings()
	LocalizationManager.set_language(SettingsManager.language)
	_show_main_menu()

func _replace_scene(next_scene: Node) -> void:
	get_tree().paused = false
	if current_scene:
		current_scene.queue_free()
	current_scene = next_scene
	add_child(current_scene)

func _show_main_menu() -> void:
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
	var menu := preload("res://scripts/ui/MainMenu.gd").new()
	menu.new_game_requested.connect(_start_game)
	menu.exit_requested.connect(_exit_game)
	_replace_scene(menu)

func _start_game() -> void:
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)
	var game := preload("res://scripts/world/GameWorld.gd").new()
	game.main_menu_requested.connect(_show_main_menu)
	game.restart_requested.connect(_start_game)
	_replace_scene(game)

func _exit_game() -> void:
	get_tree().quit()

func _ensure_input_actions() -> void:
	_add_key_action("move_forward", KEY_W)
	_add_key_action("move_back", KEY_S)
	_add_key_action("move_left", KEY_A)
	_add_key_action("move_right", KEY_D)
	_add_key_action("crouch", KEY_CTRL)
	_add_key_action("interact", KEY_E)
	_add_key_action("pause", KEY_ESCAPE)
	_add_mouse_action("throw_item", MOUSE_BUTTON_LEFT)
	_add_mouse_action("drop_item", MOUSE_BUTTON_RIGHT)

func _add_key_action(action: String, keycode: Key) -> void:
	if not InputMap.has_action(action):
		InputMap.add_action(action)
	var event := InputEventKey.new()
	event.keycode = keycode
	if not InputMap.action_has_event(action, event):
		InputMap.action_add_event(action, event)

func _add_mouse_action(action: String, button: MouseButton) -> void:
	if not InputMap.has_action(action):
		InputMap.add_action(action)
	var event := InputEventMouseButton.new()
	event.button_index = button
	if not InputMap.action_has_event(action, event):
		InputMap.action_add_event(action, event)
