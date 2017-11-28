package org.briarproject.bramble.sync;

import org.briarproject.bramble.api.crypto.CryptoComponent;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;
import org.briarproject.bramble.api.sync.ClientId;
import org.briarproject.bramble.api.sync.Group;
import org.briarproject.bramble.api.sync.GroupFactory;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.bramble.util.StringUtils;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static org.briarproject.bramble.api.sync.GroupId.LABEL;
import static org.briarproject.bramble.api.sync.SyncConstants.PROTOCOL_VERSION;

@Immutable
@NotNullByDefault
class GroupFactoryImpl implements GroupFactory {

	private final CryptoComponent crypto;

	@Inject
	GroupFactoryImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public Group createGroup(ClientId c, byte[] descriptor) {
		byte[] hash = crypto.hash(LABEL, new byte[] {PROTOCOL_VERSION},
				StringUtils.toUtf8(c.getString()), descriptor);
		return new Group(new GroupId(hash), c, descriptor);
	}
}
