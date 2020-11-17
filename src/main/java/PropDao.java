import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropDao {

    Properties props;

    public PropDao() {

        File configFile = new File(System.getProperty("user.dir")+"/src/main/resources/config.properties");
        try {
            FileReader reader = new FileReader(configFile);
            Properties properties = new Properties();
            properties.load(reader);
            this.props = properties;
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    Properties getProperties() {
        return props;
    }

}
