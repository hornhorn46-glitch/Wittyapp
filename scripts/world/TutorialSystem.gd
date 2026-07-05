extends Node

signal objective_changed(key: String)
signal hint_changed(key: String)

var steps := ["hint.move", "hint.crouch", "hint.pickup", "hint.security", "hint.throw", "hint.rescue", "hint.exit"]
var index := 0

func current_hint() -> String:
	return steps[index] if index < steps.size() else ""

func start() -> void:
	index = 0
	hint_changed.emit(current_hint())
	objective_changed.emit("objective.move")

func advance_for(event_name: String) -> void:
	var wanted := {
		"moved": "hint.move",
		"crouched": "hint.crouch",
		"picked": "hint.pickup",
		"security": "hint.security",
		"thrown": "hint.throw",
		"rescue": "hint.rescue",
		"exit": "hint.exit"
	}
	var wanted_step: String = wanted.get(event_name, "")
	var wanted_index := steps.find(wanted_step)
	if wanted_index >= index:
		index = wanted_index + 1
		hint_changed.emit(current_hint())
	if event_name == "security":
		objective_changed.emit("objective.security")
	elif event_name == "thrown":
		objective_changed.emit("objective.distract")
	elif event_name == "rescue":
		objective_changed.emit("objective.exit")
