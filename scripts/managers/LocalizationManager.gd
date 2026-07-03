extends Node

signal language_changed

var language := "en"
var strings := {}

func _ready() -> void:
	set_language(language)

func set_language(value: String) -> void:
	language = value if value in ["en", "ru"] else "en"
	var path := "res://localization/%s.json" % language
	var file := FileAccess.open(path, FileAccess.READ)
	if file:
		var parsed = JSON.parse_string(file.get_as_text())
		strings = parsed if typeof(parsed) == TYPE_DICTIONARY else {}
	language_changed.emit()

func text(key: String) -> String:
	return str(strings.get(key, key))

