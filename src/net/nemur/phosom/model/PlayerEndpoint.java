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

@Api(name = "playerendpoint", namespace = @ApiNamespace(ownerDomain = "nemur.net", ownerName = "nemur.net", packagePath = "phosom.model"))
public class PlayerEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listPlayer")
	public CollectionResponse<Player> listPlayer(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Player> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Player.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Player>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Player obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Player> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getPlayer")
	public Player getPlayer(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Player player = null;
		try {
			player = mgr.getObjectById(Player.class, id);
		} finally {
			mgr.close();
		}
		return player;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param player the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertPlayer")
	public Player insertPlayer(Player player) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsPlayer(player)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(player);
		} finally {
			mgr.close();
		}
		return player;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param player the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updatePlayer")
	public Player updatePlayer(Player player) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsPlayer(player)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(player);
		} finally {
			mgr.close();
		}
		return player;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removePlayer")
	public void removePlayer(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Player player = mgr.getObjectById(Player.class, id);
			mgr.deletePersistent(player);
		} finally {
			mgr.close();
		}
	}

	private boolean containsPlayer(Player player) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Player.class, player.getKey());
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
