import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xerial.snappy.Snappy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.UUID;

public class ReadCompressedData implements Runnable {

    @Override
    public void run() {
        KeyspacesDaoV2 keyspacesDao = new KeyspacesDaoV2();
        CqlSession session;
        try {
            session = keyspacesDao.getSession();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        PreparedStatement ps = session.prepare("select data from test1.table_with_compressed_json where id = ?");
        PropDao propDao = new PropDao();
        JSONParser parser = new JSONParser();

        File myPartitions = new File(propDao.getProperties().getProperty("output_partitions_compressed"));
        Scanner myPartitonReader = null;
        try {
            myPartitonReader = new Scanner(myPartitions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            assert myPartitonReader != null;
            if (!myPartitonReader.hasNextLine()) break;
            String partition = myPartitonReader.nextLine();
            BoundStatement boundStatement = ps.bind(UUID.fromString(partition));
            ResultSet resultSet = session.execute(boundStatement);
            Row data = resultSet.one();
            assert data != null;
            ByteBuffer raw_bytes = data.getByteBuffer("data");
            Object obj = null;
            try {
                assert raw_bytes != null;
                obj = parser.parse(new String(Snappy.uncompress(raw_bytes.array()), "UTF-8"));
            } catch (ParseException | IOException e) {
                e.printStackTrace();
                System.err.println(e);

            }
            JSONObject jsonObject = (JSONObject) obj;

        }
        session.close();

        System.out.println("Read of compressed data is completed");

    }
}
