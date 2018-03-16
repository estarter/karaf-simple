package org.maven.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Alexey Merezhin
 */
public class Simple implements SimpleI {
    private static final Logger LOG = LoggerFactory.getLogger(Simple.class);

    final private String sysPath;
    final private String myProp;

    public Simple(String sysPath, String myProp) {
        this.sysPath = sysPath;
        this.myProp = myProp;
        LOG.info("init {}", toString());
//        if (this.sysPath.contains("resources")) {
//            throw new RuntimeException("bad sys path");
//        }
    }

    @Override
    public String toString() {
        return String.format("Simple with path '%s' and prop '%s'", sysPath, myProp);
    }
}
