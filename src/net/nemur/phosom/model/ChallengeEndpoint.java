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

@Api(name = "challengeendpoint", namespace = @ApiNamespace(ownerDomain = "nemur.net", ownerName = "nemur.net", packagePath = "phosom.model"))
public class ChallengeEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listChallenge")
	public CollectionResponse<Challenge> listChallenge(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Challenge> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Challenge.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Challenge>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Challenge obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Challenge> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getChallenge")
	public Challenge getChallenge(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Challenge challenge = null;
		try {
			challenge = mgr.getObjectById(Challenge.class, id);
		} finally {
			mgr.close();
		}
		return challenge;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param challenge the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertChallenge")
	public Challenge insertChallenge(Challenge challenge) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsChallenge(challenge)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(challenge);
		} finally {
			mgr.close();
		}
		return challenge;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param challenge the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateChallenge")
	public Challenge updateChallenge(Challenge challenge) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsChallenge(challenge)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(challenge);
		} finally {
			mgr.close();
		}
		return challenge;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeChallenge")
	public void removeChallenge(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Challenge challenge = mgr.getObjectById(Challenge.class, id);
			mgr.deletePersistent(challenge);
		} finally {
			mgr.close();
		}
	}

	private boolean containsChallenge(Challenge challenge) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Challenge.class, challenge.getKey());
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
