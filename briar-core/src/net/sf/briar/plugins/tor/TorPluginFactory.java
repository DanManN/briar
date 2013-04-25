package net.sf.briar.plugins.tor;

import java.util.concurrent.Executor;

import net.sf.briar.api.TransportId;
import net.sf.briar.api.plugins.duplex.DuplexPlugin;
import net.sf.briar.api.plugins.duplex.DuplexPluginCallback;
import net.sf.briar.api.plugins.duplex.DuplexPluginFactory;
import android.content.Context;

public class TorPluginFactory implements DuplexPluginFactory {

	private static final long MAX_LATENCY = 60 * 1000; // 1 minute
	private static final long POLLING_INTERVAL = 3 * 60 * 1000; // 3 minutes

	private final Executor pluginExecutor;
	private final Context appContext;

	public TorPluginFactory(Executor pluginExecutor, Context appContext) {
		this.pluginExecutor = pluginExecutor;
		this.appContext = appContext;
	}

	public TransportId getId() {
		return TorPlugin.ID;
	}

	public DuplexPlugin createPlugin(DuplexPluginCallback callback) {
		return new TorPlugin(pluginExecutor,appContext, callback, MAX_LATENCY,
				POLLING_INTERVAL);
	}
}