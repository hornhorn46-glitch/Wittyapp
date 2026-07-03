extends Node

signal sound_emitted(position: Vector3, loudness: float, source: Node)

func emit_sound(position: Vector3, loudness: float, source: Node) -> void:
	sound_emitted.emit(position, loudness, source)

