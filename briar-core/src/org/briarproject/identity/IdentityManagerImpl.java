package org.briarproject.identity;

import org.briarproject.api.contact.Contact;
import org.briarproject.api.db.DatabaseComponent;
import org.briarproject.api.db.DbException;
import org.briarproject.api.db.Transaction;
import org.briarproject.api.identity.Author.Status;
import org.briarproject.api.identity.AuthorId;
import org.briarproject.api.identity.IdentityManager;
import org.briarproject.api.identity.LocalAuthor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import static org.briarproject.api.identity.Author.Status.OURSELVES;
import static org.briarproject.api.identity.Author.Status.UNKNOWN;
import static org.briarproject.api.identity.Author.Status.VERIFIED;

class IdentityManagerImpl implements IdentityManager {
	private final DatabaseComponent db;
	private final List<AddIdentityHook> addHooks;
	private final List<RemoveIdentityHook> removeHooks;

	@Inject
	IdentityManagerImpl(DatabaseComponent db) {
		this.db = db;
		addHooks = new CopyOnWriteArrayList<AddIdentityHook>();
		removeHooks = new CopyOnWriteArrayList<RemoveIdentityHook>();
	}

	@Override
	public void registerAddIdentityHook(AddIdentityHook hook) {
		addHooks.add(hook);
	}

	@Override
	public void registerRemoveIdentityHook(RemoveIdentityHook hook) {
		removeHooks.add(hook);
	}

	@Override
	public void addLocalAuthor(LocalAuthor localAuthor) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			db.addLocalAuthor(txn, localAuthor);
			for (AddIdentityHook hook : addHooks)
				hook.addingIdentity(txn, localAuthor);
			txn.setComplete();
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public LocalAuthor getLocalAuthor(AuthorId a) throws DbException {
		LocalAuthor author;
		Transaction txn = db.startTransaction(true);
		try {
			author = db.getLocalAuthor(txn, a);
			txn.setComplete();
		} finally {
			db.endTransaction(txn);
		}
		return author;
	}

	@Override
	public Collection<LocalAuthor> getLocalAuthors() throws DbException {
		Collection<LocalAuthor> authors;
		Transaction txn = db.startTransaction(true);
		try {
			authors = db.getLocalAuthors(txn);
			txn.setComplete();
		} finally {
			db.endTransaction(txn);
		}
		return authors;
	}

	@Override
	public void removeLocalAuthor(AuthorId a) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			LocalAuthor localAuthor = db.getLocalAuthor(txn, a);
			for (RemoveIdentityHook hook : removeHooks)
				hook.removingIdentity(txn, localAuthor);
			db.removeLocalAuthor(txn, a);
			txn.setComplete();
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public Status getAuthorStatus(AuthorId authorId) throws DbException {
		Transaction txn = db.startTransaction(false);
		try {
			return getAuthorStatus(txn, authorId);
		} finally {
			db.endTransaction(txn);
		}
	}

	@Override
	public Status getAuthorStatus(Transaction txn, AuthorId authorId)
			throws DbException {
		// Compare to the IDs of the user's identities
		for (LocalAuthor a : db.getLocalAuthors(txn))
			if (a.getId().equals(authorId)) return OURSELVES;
		// Compare to the IDs of contacts' identities
		for (Contact c : db.getContacts(txn))
			if (c.getAuthor().getId().equals(authorId)) return VERIFIED;

		// TODO also handle UNVERIFIED when #261 is implemented
		return UNKNOWN;
	}

}
