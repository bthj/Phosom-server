package net.nemur.phosom.model.gametypes;

import net.nemur.phosom.model.Challenge;
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
import javax.jdo.FetchGroup;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "autochallengegameendpoint", namespace = @ApiNamespace(ownerDomain = "nemur.net", ownerName = "nemur.net", packagePath = "phosom.model.gametypes"))
public class AutoChallengeGameEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listAutoChallengeGame")
	public CollectionResponse<AutoChallengeGame> listAutoChallengeGame(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<AutoChallengeGame> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(AutoChallengeGame.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<AutoChallengeGame>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (AutoChallengeGame obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<AutoChallengeGame> builder()
				.setItems(execute).setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getAutoChallengeGame")
	public AutoChallengeGame getAutoChallengeGame(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		AutoChallengeGame autochallengegame = null;
		try {
			AutoChallengeGame autochallengegameTemp = mgr.getObjectById(AutoChallengeGame.class, id);
			// let's eagerly fetch all challenges before closing the connection
			for( Challenge oneChallenge : autochallengegameTemp.getChallenges() )
				;
			autochallengegameTemp.getChallengeInfo();
			autochallengegame = mgr.detachCopy( autochallengegameTemp );
		} finally {
			mgr.close();
		}
		return autochallengegame;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param autochallengegame the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertAutoChallengeGame")
	public AutoChallengeGame insertAutoChallengeGame(
			AutoChallengeGame autochallengegame) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsAutoChallengeGame(autochallengegame)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(autochallengegame);
		} finally {
			mgr.close();
		}
		return autochallengegame;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param autochallengegame the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateAutoChallengeGame")
	public AutoChallengeGame updateAutoChallengeGame(
			AutoChallengeGame autochallengegame) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsAutoChallengeGame(autochallengegame)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(autochallengegame);
		} finally {
			mgr.close();
		}
		return autochallengegame;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeAutoChallengeGame")
	public void removeAutoChallengeGame(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			AutoChallengeGame autochallengegame = mgr.getObjectById(
					AutoChallengeGame.class, id);
			mgr.deletePersistent(autochallengegame);
		} finally {
			mgr.close();
		}
	}

	private boolean containsAutoChallengeGame(
			AutoChallengeGame autochallengegame) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(AutoChallengeGame.class,
					autochallengegame.getKey());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	protected static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
