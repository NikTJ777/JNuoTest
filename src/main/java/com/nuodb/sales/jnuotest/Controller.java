package com.nuodb.sales.jnuotest;

import com.nuodb.sales.jnuotest.dao.SqlSession;
import com.nuodb.sales.jnuotest.domain.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nik on 7/6/15.
 */
public class Controller implements AutoCloseable {

    OwnerRepository ownerRepository;
    EventRepository eventRepository;
    GroupRepository groupRepository;
    DataRepository dataRepository;

    ExecutorService executor;

    Properties fileProperties;
    Properties appProperties;

    long runTime;
    float averageRate;
    float burstProbability;
    int minBurst, maxBurst;
    int maxQueued;

    SqlSession.Mode bulkCommitMode;

    volatile long totalInserts = 0;
    volatile long totalTime = 0;

    private Random random = new Random();

    private static final Properties defaultProperties = new Properties();

    public static final String PROPERTIES_PATH =    "properties.path";
    public static final String AVERAGE_RATE =       "average.rate";
    public static final String THREAD_COUNT =       "thread.count";
    public static final String MAX_QUEUED =          "max.queued";
    public static final String DB_PROPERTIES_PATH = "db.properties.path";
    public static final String RUN_TIME =           "run.time";
    public static final String BURST_PROBABILITY_PERCENTAGE = "burst.probability.percentage";
    public static final String MIN_BURST =          "min.burst.count";
    public static final String MAX_BURST =          "max.burst.count";
    public static final String DB_INIT_SQL =        "db.init.sql";
    public static final String BULK_COMMIT_MODE =   "bulk.commit.mode";

    private static Logger log = Logger.getLogger("JNuoTest");

    private static final double Nano2Millis = 1000000.0;
    private static final double Nano2Seconds = 1000000000.0;
    private static final double Millis2Seconds = 1000.0;

    private static final long Millis = 1000;

    private static final float Percent = 100.0f;

    public Controller() {
        defaultProperties.setProperty(PROPERTIES_PATH, "classpath://properties/Application.properties");
        defaultProperties.setProperty(DB_PROPERTIES_PATH, "classpath://properties/Database.properties");
        defaultProperties.setProperty(AVERAGE_RATE, "0");
        defaultProperties.setProperty(THREAD_COUNT, "1");
        defaultProperties.setProperty(MAX_QUEUED, "1");
        defaultProperties.setProperty(BURST_PROBABILITY_PERCENTAGE, "0");
        defaultProperties.setProperty(MIN_BURST, "0");
        defaultProperties.setProperty(MAX_BURST, "0");
        defaultProperties.setProperty(RUN_TIME, "5");
        defaultProperties.setProperty(BULK_COMMIT_MODE, "BATCH");
    }

    public void configure(String[] args)
        throws Exception
    {
        // create 2 levels of file properties (application.properties; and database.properties)
        Properties prop = new Properties(defaultProperties);
        fileProperties = new Properties(prop);

        // create app properties, using fileProperties as default values
        appProperties = new Properties(fileProperties);

        // parse the command line into app properties, as command line overrides all others
        parseCommandLine(args, appProperties);

        // load properties from application.properties file into first (lower-priority) level of fileProperties
        loadProperties(prop, PROPERTIES_PATH);

        // now load database properties into second (higher-priority) level of fileProperties
        loadProperties(fileProperties, DB_PROPERTIES_PATH);

        log.info(String.format("Properties: %s", appProperties));

        runTime = Integer.parseInt(appProperties.getProperty(RUN_TIME)) * Millis;
        averageRate = Float.parseFloat(appProperties.getProperty(AVERAGE_RATE));
        burstProbability = Float.parseFloat(appProperties.getProperty(BURST_PROBABILITY_PERCENTAGE));
        minBurst = Integer.parseInt(appProperties.getProperty(MIN_BURST));
        maxBurst = Integer.parseInt(appProperties.getProperty(MAX_BURST));
        maxQueued = Integer.parseInt(appProperties.getProperty(MAX_QUEUED));

        if (maxBurst <= minBurst) {
            log.info(String.format("maxBurst (%d) <= minBurst (%d); burst disabled"));
            burstProbability = minBurst = maxBurst = 0;
        }

        DataSource dataSource = new com.nuodb.jdbc.DataSource(fileProperties);
        SqlSession.init(dataSource);

        ownerRepository = new OwnerRepository();
        eventRepository = new EventRepository();
        groupRepository = new GroupRepository();
        dataRepository = new DataRepository();

        try { bulkCommitMode = Enum.valueOf(SqlSession.Mode.class, appProperties.getProperty(BULK_COMMIT_MODE)); }
        catch (Exception e) { bulkCommitMode = SqlSession.Mode.BATCH; }

        String threadParam = appProperties.getProperty(THREAD_COUNT);
        int threadCount = (threadParam != null ? Integer.parseInt(threadParam) : 1);
        executor = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * perform any logic required after configuration, and before the Controller can be used
     */
    public void init() {
        initializeDatabase();
    }

    /**
     * Start the controller.
     *
     * @throws InterruptedException
     */
    public void start()
        throws InterruptedException
    {
        long endTime = System.currentTimeMillis() + runTime;
        long start = System.currentTimeMillis();
        long now;

        double currentRate = 0.0;
        long averageSleep = (long) (Millis2Seconds / averageRate);

        int counter = 0;

        double burstRate = 0.0;
        int burstSize = 0;

        long settleTime = 2 * Millis;
        log.info(String.format("Settling for %d: ", settleTime));
        Thread.sleep(settleTime);

        do {
            executor.execute(new EventGenerator(counter++));

            log.info(String.format("Event scheduled. Queue size=%d", ((ThreadPoolExecutor) executor).getQueue().size()));

            now = System.currentTimeMillis();
            currentRate = (Millis2Seconds * counter) / (now - start);

            log.info(String.format("now=%d; endTime=%d;  elapsed=%d; time left=%d", now, endTime, now-start, endTime - now));

            // randomly create a burst
            if (burstSize == 0 && burstProbability > 0 && Percent * random.nextFloat() <= burstProbability) {
                burstSize = minBurst + random.nextInt(maxBurst - minBurst);
                log.info(String.format("Creating burst of %d", burstSize));
            }

            if (burstSize > 0) {
                burstSize--;
            } else {
                if (averageRate > 0) {
                    long sleepTime = (long) (averageSleep * (currentRate / averageRate));
                    if (now + sleepTime > endTime) sleepTime = 1 * Millis;

                    log.info(String.format("Current Rate= %.2f; sleeping for %,d ms", currentRate, sleepTime));
                    Thread.sleep(sleepTime);
                }

                while (maxQueued > 0 && ((ThreadPoolExecutor) executor).getQueue().size() > maxQueued) {
                    log.info(String.format("Queue size %d is over limit %d - sleeping", ((ThreadPoolExecutor) executor).getQueue().size(), maxQueued));
                    Thread.sleep(1 * Millis / 2);
                }

                log.info(String.format("Sleeping done. Queue size=%d", ((ThreadPoolExecutor) executor).getQueue().size()));

            }

            log.info(String.format("Total inserts=%,d; total time %.2f ms; rate=%.2f ips", totalInserts, totalTime / Nano2Millis, Nano2Seconds * totalInserts / totalTime));

        } while (System.currentTimeMillis() < endTime);
    }

    public void close()
    {
        executor.shutdownNow();
        try { executor.awaitTermination(10, TimeUnit.SECONDS); }
        catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for shutdown - exiting");
        }

        log.info(String.format("Exiting with %d items remaining in the queue. Total inserts %,d in %.2f sec at %.2f ips", ((ThreadPoolExecutor) executor).getQueue().size(),
                totalInserts, totalTime / Nano2Seconds, Nano2Seconds * totalInserts / totalTime));

        SqlSession.getCurrent().close();
    }

    protected void initializeDatabase() {
        log.info(String.format("appProperties: %s", appProperties.toString()));

        String script = appProperties.getProperty(DB_INIT_SQL);
        if (script == null) log.info("Somehow script is NULL");

        log.info(String.format("running init sql (length: %d): %s", script.length(), script));
        SqlSession.getCurrent().execute(script);
    }

    protected void parseCommandLine(String[] args, Properties props) {

        for (String param : args) {
            String[] keyVal = param.split("=");
            if (keyVal.length == 2) {
                props.setProperty(keyVal[0], keyVal[1]);
            }
            else {
                props.setProperty(param, "true");
            }
        }
    }

    protected void loadProperties(Properties props, String key)
        throws MalformedURLException, IOException
    {
        assert key != null && key.length() > 0;

        String path = appProperties.getProperty(key);
        if (path == null || path.length() == 0) {
            log.info(String.format("loadProperties: key %s not in app properties", key));
            return;
        }

        log.info(String.format("loading properties: %s from %s", key, path));

        InputStream is = null;

        if (path.startsWith("classpath://")) {
            is = getClass().getClassLoader().getResourceAsStream(path.substring("classpath://".length()));
            log.info(String.format("loading resource: %s", path.substring("classpath://".length())));
        } else {
            is = new URL(path).openStream();
        }

        if (is == null) return;

        try { props.load(is); }
        finally { is.close(); }

        resolveReferences(props);

        log.info(String.format("Loaded properties %s: %s", key, props));
    }

    protected void resolveReferences(Properties props) {
        Pattern var = Pattern.compile("\\$\\{.*\\}");
        StringBuffer newVar = new StringBuffer();

        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            Matcher match = var.matcher(entry.getValue().toString());
            while (match.find()) {
                String val = props.getProperty(match.group().replaceAll("\\$|\\{|\\}", ""));
                log.info(String.format("resolving var reference %s to %s", match.group(), val));

                if (val != null) match.appendReplacement(newVar, val);
            }

            if (newVar.length() > 0) {
                log.info(String.format("Replacing updated property %s=%s", entry.getKey(), newVar));
                match.appendTail(newVar);
                entry.setValue(newVar.toString());
                newVar.setLength(0);
            }
        }
    }

    class EventGenerator implements Runnable {

        private final long unique;
        private Date dateStamp = new Date();

        EventGenerator(long unique) {
            this.unique = unique;
        }

        public void run() {

            long start = System.nanoTime();

            long ownerId = generateOwner();
            report("Owner", 1, System.nanoTime() - start);

            long eventId = generateEvent(ownerId);

            int groupCount = 1 + random.nextInt(5);
            log.info(String.format("Creating %d groups", groupCount));

            int total = 2 + groupCount;

            for (int gx = 0; gx < groupCount; gx++) {
                long groupId = generateGroup(eventId, gx);
                int dataCount = 0;
                try (SqlSession session = SqlSession.start(bulkCommitMode)) {
                    dataCount = 500 + random.nextInt(1000);
                    total += dataCount;
                    for (int dx = 0; dx <= dataCount; dx++) {
                        generateData(groupId, dx);
                    }
                } catch (Exception e) {
                    log.info(String.format("Error inserting data row %s", e.toString()));
                }

                groupRepository.update(groupId, "dataCount", dataCount);
            }

            long duration = System.nanoTime() - start;
            report("All Data", total, duration);

            totalInserts += total;
            totalTime += duration;
        }

        protected long generateOwner() {

            Owner owner = new Owner();
            owner.setName(String.format("Owner-%d", unique));

            return ownerRepository.persist(owner);
        }

        protected long generateEvent(long ownerId) {

            Event event = new Event();
            event.setName(String.format("Event-%d", unique));
            event.setOwner(ownerId);
            event.setDate(dateStamp);

            return eventRepository.persist(event);
        }

        protected long generateGroup(long eventId, int index) {

            Group group = new Group();
            group.setEvent(eventId);
            group.setName(String.format("Group-%d-%d", unique, index));
            group.setDataCount(0);
            group.setDate(dateStamp);
            group.setDescription("Test data generated by JNuoTest");

            return groupRepository.persist(group);
        }

        protected void generateData(long groupId, int index) {
            Data data = new Data();
            data.setGroup(groupId);
            data.setName(String.format("Data-%d-%d-%d", unique, groupId, index));
            data.setDescription("Test data generated by JNuoTest");
            data.setPath(String.format("file:///remote/storage/data-%d-%d-%d.bin", unique, groupId, index));

            dataRepository.persist(data);
        }

        private void report(String name, int count, long duration) {
            double rate = (count > 0 && duration > 0 ? Nano2Seconds * count / duration : 0);
            log.info(String.format("Run %d; generated %s (%,d records); duration=%.2f ms; rate=%.2f", unique, name, count, 1d / Nano2Millis * duration, rate));
        }
    }
}