package org.slf4j;

/**
 * Created by kevinleperf on 03/02/2014.
 */
public class LoggerFactory {
    public static Logger getLogger(Class _cl){
        return new Logger();
    }
}
