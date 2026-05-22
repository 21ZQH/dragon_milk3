/**
 * Repository interfaces that define persistence boundaries.
 *
 * <p>Services depend on these interfaces instead of directly depending on the
 * text-file stores. This keeps the project architecture stable and preserves
 * the controller -> service -> repository -> store -> file flow.</p>
 */
package repository;
