package it.femco.textual.panel;

/**
 * Abstraction for objects that can be created in a textual panel.
 *
 * Once those are created, actors can be moved, hidden, showed, overlapped
 * resized and in general can be pulled and operated according to their peculiarities.
 *
 * Actors are always created by the textual panel (that in fact work as a Factory),
 * they have no public constructors and must be instantiated into an active panel.
 *
 * Since it's about textual interfaces, actors are always made by colored strings,
 * each string has a background and a foreground color:
 * these atomic elements are named 'textoms' from text and atoms.
 *
 * The actors are organized in a tree structure: at the root there is the panel,
 * on the panel there is the PanelContainer (that is an Actor and inherit the
 * panel dimensions.), the container can contains other Actor objects
 * that can be containers or texts.
 * ActorContainer are containers and differ by the way they organize contained actors.
 * ActorText are texts in the sense that provide Atom objects to render the interface.
 *
 * In the tree structure, the leaf-branch relation implies that the 'leaf' actor
 * can't write text out of the 'branch' actor space and if it move out of this space,
 * it disappear.
 * The tree structure allow to implement an efficient observer-notification logic between
 * elements, that notify each relative for modifications or other events.
 */
public interface Actor {
    /**
     * @return the current size of the actor
     */
    public Size size();

    /**
     * Set the new visible size of the actor.
     * @param newsize new size to use as dimension of the rectangular text space, in characters.
     * @return the actor itself, for fluent programming
     */
    public Actor size(Size newsize);
    public Pos position();
}