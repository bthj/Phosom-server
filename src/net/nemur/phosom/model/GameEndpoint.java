package net.nemur.phosom.model;

import net.nemur.phosom.model.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "gameendpoint", namespace = @ApiNamespace(ownerDomain = "nemur.net", ownerName = "nemur.net", packagePath = "phosom.model"))
public class GameEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listGame")
	public CollectionResponse<Game> listGame(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Game> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Game.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Game>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Game obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Game> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getGame")
	public Game getGame(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Game game = null;
		try {
			game = mgr.getObjectById(Game.class, id);
		} finally {
			mgr.close();
		}
		return game;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param game the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertGame")
	public Game insertGame(Game game) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsGame(game)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(game);
		} finally {
			mgr.close();
		}
		return game;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param game the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateGame")
	public Game updateGame(Game game) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsGame(game)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(game);
		} finally {
			mgr.close();
		}
		return game;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeGame")
	public void removeGame(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Game game = mgr.getObjectById(Game.class, id);
			mgr.deletePersistent(game);
		} finally {
			mgr.close();
		}
	}

	private boolean containsGame(Game game) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Game.class, game.getId());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
