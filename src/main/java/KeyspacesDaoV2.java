import com.datastax.oss.driver.api.core.CqlSession;
import software.aws.mcs.auth.SigV4AuthProvider;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class KeyspacesDaoV2 {

    CqlSession session;

    public KeyspacesDaoV2() {

    }

    public CqlSession getSession() throws NoSuchAlgorithmException {

        PropDao propDao = new PropDao();

        List<InetSocketAddress> contactPoints =
                Collections.singletonList(
                        InetSocketAddress.createUnresolved(propDao.getProperties().getProperty("contactPoint"), Integer.parseInt(propDao.getProperties().getProperty("port"))));

        session = CqlSession.builder()
                .addContactPoints(contactPoints)
                .withSslContext(SSLContext.getDefault())
                .withLocalDatacenter(propDao.getProperties().getProperty("region"))
                .withAuthProvider(new SigV4AuthProvider(propDao.getProperties().getProperty("region")))
                .build();

        return session;
    }
}
