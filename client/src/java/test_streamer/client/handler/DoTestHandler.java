package test_streamer.client.handler;

import com.ning.http.client.websocket.WebSocket;
import net.unit8.wscl.WebSocketClassLoader;
import org.junit.runner.JUnitCore;
import test_streamer.client.*;
import test_streamer.client.dto.ResultCommand;
import us.bpsm.edn.Keyword;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static test_streamer.client.ClientConfig.ClientConfigKey.*;

/**
 * @author kawasima
 */
public class DoTestHandler implements Handler {
    private static Map<UUID, ClassLoader> classLoaderCache = new HashMap<UUID, ClassLoader>();
    private ClientConfig config;

    public DoTestHandler(ClientConfig config) {
        this.config = config;
    }

    @Override
    public void handle(Map<Keyword, Object> msg, WebSocket websocket) {
        String className = msg.get(Keyword.newKeyword("name")).toString();

        ((ClientUI)config.getObject(UI)).beginTest(className);

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        UUID classLoaderId = (UUID) msg.get(Keyword.newKeyword("classloader-id"));
        String url = new StringBuilder(256)
                .append(config.getString(CLASS_PROVIDER_URL))
                .append("?classLoaderId=")
                .append(classLoaderId).toString();

        ClassLoader loader = classLoaderCache.get(classLoaderId);
        if (loader == null) {
            loader = new WebSocketClassLoader(url);
        }

        ClientRunListener runListener = new ClientRunListener(className);
        try {
            Class<?> testClass = loader.loadClass(className);
            Thread.currentThread().setContextClassLoader(loader);
            JUnitCore core = new JUnitCore();
            core.addListener(runListener);
            core.run(testClass);
            classLoaderCache.put(classLoaderId, loader);
        } catch(Exception ex) {
            runListener.getResult().setClientException(ex.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
        ((ClientUI)config.getObject(UI)).endTest(runListener.getResult());

        ResultCommand command = new ResultCommand(className, (UUID) msg.get(Keyword.newKeyword("shot-id")));
        command.setResult(runListener.getResult());
        WebSocketUtil.send(websocket, command);

        ((ClientUI)config.getObject(UI)).standby();
    }

    public void dispose() {
        for (Map.Entry<UUID, ClassLoader> entry : classLoaderCache.entrySet()) {
            if (entry.getValue() instanceof WebSocketClassLoader) {
                ((WebSocketClassLoader) entry.getValue()).dispose();
            }
        }
    }
}
