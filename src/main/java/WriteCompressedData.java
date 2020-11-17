
import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xerial.snappy.Snappy;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.UUID;

public class WriteCompressedData implements Runnable {

    @Override
    public void run() {

        KeyspacesDaoV2 keyspacesDao = new KeyspacesDaoV2();
        CqlSession session;
        try {
            session = keyspacesDao.getSession();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        PreparedStatement ps = session.prepare("insert into test1.table_with_compressed_json(id, data) VALUES(?,?);");
        PropDao propDao = new PropDao();

        // Let's persist partition keys into a file
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(propDao.getProperties().getProperty("output_partitions_compressed"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert fstream != null;
        BufferedWriter out = new BufferedWriter(fstream);
        JSONParser parser = new JSONParser();
        try {

            Object obj = parser.parse(new FileReader(propDao.getProperties().getProperty("input_jsons")));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray results = (JSONArray) jsonObject.get("results");
            Iterator<JSONObject> iterator = results.iterator();

            iterator.forEachRemaining(json -> {
                try {
                    byte[] compressed = Snappy.compress(json.toJSONString().getBytes(StandardCharsets.UTF_8));
                    UUID uuid = Uuids.timeBased();
                    BoundStatement boundStatement = ps.bind(uuid,ByteBuffer.wrap(compressed)).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
                    session.execute(boundStatement);
                    out.write(uuid+"\n");

                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println(e);
                }

            });

            out.close();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
        }
        System.out.println("Write of compressed data is completed");
    }
}