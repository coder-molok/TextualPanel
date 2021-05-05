package it.femco.textual.panel;

/**
 * Abstraction for the objects that can be created and placed on a textual panel.
 *
 * Ones it was created, an Actor can be moved, hidden, showed, overlapped, resized,
 * and generally it can be got back and controlled according to its definition.
 *
 * Actors are always created by the textual panel (that in fact work as a Factory),
 * they have no public constructors and must be instantiated into an active panel.
 *
 * Since it's about textual interfaces, actors are always made by colored strings,
 * each string has a background and a foreground color:
 * these atomic elements are named 'textoms' from text and atoms.
 *
 * Actors are organized in a tree structure, the leaf-branch relation implies that the
 * 'leaf' actor can't write text out of the 'branch' actor space and if it move out
 * of this space, it disappear.
 * The structure begin with a 'root' actor that has to be a container and inherit the
 * panel dimensions.
 * The tree structure allow to implement an efficient observer-notification logic between
 * elements, that notify each relative for modifications or other events.
 */
public interface TextualPanelActor {
}
