package it.femco.textual.panel;

/**
 * Rappresentazione degli oggetti che possono essere creati su un pannello testuale.
 *
 * Una volta creati, gli attori possono essere spostati, nascosti, mostrati, sovrapposti,
 * ridimensionati e in generale recuperati e modificati in base alle loro peculiarità.
 *
 * Gli attori saranno creati sempre dalla relativa Factory, non avranno quindi costruttori
 * pubblici, infatti devono essere creati dentro un pannello attivo e valido.
 *
 * Trattandosi di interfacce testuali, gli attori sono sempre composti di stringhe colorate,
 * ogni stringa può avere solo un colore di sfondo e uno di primopiano:
 * questi elementi atomici che compongono gli attori sono detti appunto 'atomi' .
 *
 * Gli attori sono organizzati ad albero, quando un attore cambia "internamente" notifica
 * le sue foglie di riadattarsi (
 */
public interface TextualPanelActor {
}
